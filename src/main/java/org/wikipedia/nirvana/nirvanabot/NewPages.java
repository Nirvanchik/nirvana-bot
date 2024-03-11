/**
 *  @(#)NewPages.java
 *  Copyright © 2023 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * WARNING: This file may contain Russian characters.
 * This file is encoded with UTF-8.
 * */

package org.wikipedia.nirvana.nirvanabot;

import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.archive.Archive;
import org.wikipedia.nirvana.archive.ArchiveFactory;
import org.wikipedia.nirvana.archive.ArchiveSettings;
import org.wikipedia.nirvana.base.BotFatalError;
import org.wikipedia.nirvana.error.AfterUpdateFailure;
import org.wikipedia.nirvana.error.ArchiveUpdateFailure;
import org.wikipedia.nirvana.error.DangerousEditException;
import org.wikipedia.nirvana.error.InvalidLineFormatException;
import org.wikipedia.nirvana.error.ServiceError;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.FetcherFactory;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.PageListFetcher;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.PageListProcessor;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.PageListProcessorFast;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.PageListProcessorSlow;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.ProcessorCombinator;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.RevisionWithId;
import org.wikipedia.nirvana.nirvanabot.report.ReportItem;
import org.wikipedia.nirvana.nirvanabot.templates.TemplateFilter;
import org.wikipedia.nirvana.nirvanabot.templates.TemplateFinder;
import org.wikipedia.nirvana.util.DateTools;
import org.wikipedia.nirvana.util.StringTools;
import org.wikipedia.nirvana.util.SystemTime;
import org.wikipedia.nirvana.util.XmlTools;
import org.wikipedia.nirvana.wiki.CatScanTools;
import org.wikipedia.nirvana.wiki.CatScanTools.Service;
import org.wikipedia.nirvana.wiki.CatScanTools.ServiceFeatures;
import org.wikipedia.nirvana.wiki.NirvanaWiki;
import org.wikipedia.nirvana.wiki.WikiBooster;
import org.wikipedia.nirvana.wiki.WikiUtils;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;

/**
 * What info we can know about page:
 * Info extracted from service and from Wikipedia
 * 1) current page name // used for showing in result
 * 2) page name when page was created // used to remove duplications
 * 3) page name when page at the moment was moved to current namespace
 *    // used for removing duplications
 * 4) the date of page creation // used for sorting and showing in result
 * 5) the date of moving this page to current namespace // used for sorting and showing in result
 * 6) page rev id // used for sorting by creation date,
 *    // bad if you want to consider pages moved from other namespaces
 * 7) page author // used to show in result
 * 
 * What info can be retrieved from "catscan" service without additional
 * requests?
 * 1) current page name
 * 2) page rev id
 *
 * What info can be shown in result?
 * 1) current page name
 * 2) author (optional)
 * 3) creation date (optional)
 *
 * How many new pages should extract the bot?
 * It should be enough to generate full page list.
 * 
 * If bot extracts not enough pages it should leave some of the old pages.
 * Leaving old pages should enable a lot of logic:
 * 1) pages with the title existing in new extracted list should be removed
 * 2) pages that were renamed should be removed if newer name already presents
 *    // check first revisiton -> getFirstRevision()
 * 3) deleted pages should be removed
 *    // check if page exists -> exists()
 * 4) pages order should be preserved after adding new items even if they are mixed
 * 
 *
 */
public class NewPages extends BasePortalModule {
    protected static final Logger sLog;

    private static final int WIKI_API_BUNCH_SIZE = 10;

    protected int hours;
    @Nullable
    protected ArchiveSettings archiveSettings;

    /**
     * When project page has this number or more items (old items) and bot is going to erase them
     * all (list generated by bot has 0 items) bot edit will be declined and this session will be
     * considered as error. This is self-protection from destructive edits. 
     */
    protected int dangerousEditThreshold = 1;

    protected Map<String,String> pageLists;
    protected Map<String,String> pageListsToIgnore;

    protected GetRevisionMethod getRevisionMethod = GetRevisionMethod.GET_REV;
    
    protected boolean enableFeatureUpdateFromOld = true;
    protected boolean enableFeatureArchive = true;

    protected boolean supportsTemplateFilter = true;
    protected TemplateFilter templateFilter = null;

    /**
     * True means that list item may contain new page author info.
     */
    protected boolean supportAuthor = true;

    /**
     * This happens when service doesn't support fetching page list with filtering by provided
     * templates list. In this case we filter it by ours. This flag enables manual filtering
     * by template filter.
     */
    protected boolean needsCustomTemlateFiltering = false;

    protected DateTools dateTools;
    
    protected final NewPageItemParser newPageItemParser;

    static {
        sLog = LogManager.getLogger(NewPages.class);
    }

    protected enum GetRevisionMethod {
        GET_FIRST_REV,
        GET_FIRST_REV_IF_NEED,
        GET_FIRST_REV_IF_NEED_SAVE_ORIGINAL,
        GET_REV,
        GET_TOP_REV
        //GET_LAST_REV_BY_PAGE_NAME
    }

    /**
     *  Constructs class instance using bot params.
     */
    public NewPages(PortalParam param, PageFormatter pageFormatter, SystemTime systemTime) {
        super(param, pageFormatter, systemTime);

        this.archiveSettings = param.archSettings;
        if (supportAuthor) {
            formatString = formatString.replace(BotVariables.AUTHOR, "%2$s");
        }
        this.hours = param.hours;
        this.templateFilter = param.templateFilter;

        dateTools = DateTools.getInstance();
        
        newPageItemParser = new NewPageItemParser();

        log.debug("Portal module created for portal subpage [[" + this.pageName + "]]");
    }

    public class NewPagesUpdateResults extends UpdateResults {
        List<String> archiveItems = null;
        int deletedCount = 0;

        NewPagesUpdateResults() {
            if (enableFeatureArchive && archiveSettings != null) {
                archiveItems = new ArrayList<String>();
            }
        }

        @Override
        void logStat() {
            super.logStat();
            log.debug("deleted count: {}", deletedCount);
        }

        @Override
        int newPagesCount() {
            if (totalCount == 0) return 0;
            int count = totalCount - (oldCount - outdateCount - deletedCount);
            if (count < 0) return 0;
            return count;
        }
        
        @Override
        void outdateItem(String item) {
            super.outdateItem(item); 
            if (archiveItems != null) {
                archiveItems.add(item);
            }
        }
    }

    protected class NewPagesBuffer {
        protected NirvanaWiki wiki;
        protected List<String> subset = new ArrayList<String>();
        List<String> includedPages = new ArrayList<String>();

        public NewPagesBuffer(NirvanaWiki wiki) {
            this.wiki = wiki;
        }

        public boolean contains(String item) {
            return subset.contains(item);
        }

        public int size() {
            return subset.size();
        }

        public boolean checkIsDuplicated(String archiveItem) {
            if (contains(archiveItem)) {
                return true;
            }
            for (String title:includedPages) {
                String variant1 = "[[" + title + "]]";                        
                String variant2 = "|" + WikiUtils.escapeTitle(
                        title.substring((namespace == 0) ? 0 : namespaceIdentifier.length() + 1))
                        + "|";
                if (archiveItem.contains(variant1) ||
                        archiveItem.contains(variant2)) {
                    return true;                    
                }
            }
            return false;
        }

        public String getNewText() {
            return pageFormatter.formatPage(subset);
        }

        protected void addOldItem(String item) {
            subset.add(item);
        }

        protected String formatTimeString(Revision rev) {
            if (NirvanaBot.TIME_FORMAT.equalsIgnoreCase("long")) {
                // TODO: Is this needed? Is this used?
                throw new NotImplementedException("long date format not implemented");
            } else {
                return rev.getTimestamp().atZoneSameInstant(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
            }
        }

        String formatTitleString(String title) {
            String titleToInsert = title;
            if (namespace != 0) {
                log.debug("Namespace: {} / {}, title: ", namespaceIdentifier, namespace, title);
                assert title.contains(":");
                assert title.length() > namespaceIdentifier.length();
                titleToInsert = title.substring(namespaceIdentifier.length() + 1);
            }
            // There are pages like /etc or /dev/null which start with '/'
            if (titleToInsert.startsWith("/")) {
                titleToInsert = ":" + titleToInsert;
            }
            if (format.contains("{{") && format.contains("}}")) {
                titleToInsert = WikiUtils.escapeTitle(titleToInsert);
            }
            return titleToInsert;
        }

        String formatItemString(String title, boolean deleted, Revision rev)
                throws InvalidLineFormatException {
            String user = rev.getUser();
            String time = formatTimeString(rev);            
            String titleToInsert = formatTitleString(title);
            return String.format(formatString, titleToInsert, XmlTools.removeEscape(user), time);
        }

        // why 'title' item? because 'rev' can has old title if page was renamed
        // author, date are OK from this 'rev'
        protected void addNewItem(String title, Revision rev) throws IOException,
                InvalidLineFormatException {
            String element = formatItemString(title, false, rev);

            if (!subset.contains(element)) {
                subset.add(element);
                if (includedPages != null) {
                    includedPages.add(title);
                }
                log.debug("ADD new line: \t{}", element);
            }
        }

        protected boolean addNewPage(NirvanaWiki wiki, Revision pageInfo) throws IOException,
                InvalidLineFormatException {
            Revision page = null;
            switch (getRevisionMethod) {
                case GET_FIRST_REV:
                    page = wiki.getFirstRevision(pageInfo.getPage());
                    break;
                // Is this needed?
                case GET_REV:
                    page = wiki.getRevision(pageInfo.getRevid());
                    break;
                // Is this needed?
                case GET_TOP_REV:
                    page = wiki.getTopRevision(pageInfo.getPage());
                    break;
                // Is this needed?
                case GET_FIRST_REV_IF_NEED:
                    page = pageInfo;
                    break;
                // Is this needed?
                case GET_FIRST_REV_IF_NEED_SAVE_ORIGINAL:
                    page = pageInfo;
                    break;
                default:
                    throw new Error("not supported mode");
            }

            if (page == null) {
                String info = String.format("[rev id: %s, title: %s]",
                        pageInfo.getRevid() == 0 ? "unknown" : String.valueOf(pageInfo.getRevid()),
                        pageInfo.getPage() == null ? "unknown" : pageInfo.getPage());
                log.error("Sorry, revision information not found for page {}", info);
                return false;
            }

            String titleNew = XmlTools.removeEscape(pageInfo.getPage());
            String titleOld = XmlTools.removeEscape(page.getPage());
            log.debug("Check page, title old: {}, title new: {}", titleOld, titleNew);

            if (!titleNew.equals(titleOld)) {
                addNewItem(titleNew, page);
                if (includedPages != null) {
                    includedPages.add(titleOld);
                }
            } else {
                addNewItem(titleNew, page);
            }
            return true;
        }    
    }

    // TODO: Make factory and instantiate it from factory.
    // This will simplify unit-testing.
    protected PageListProcessor createPageListProcessorWithFetcher(CatScanTools.Service service,
            PageListFetcher fetcher, List<String> categories, List<String> categoriesToIgnore) {
        if (service.supportsFastMode() && fastMode) {
            return new PageListProcessorFast(service, categories, categoriesToIgnore, language,
                    depth, namespace, fetcher);
        } else {
            return new PageListProcessorSlow(service, categories, categoriesToIgnore, language,
                    depth, namespace, fetcher);            
        }
    }

    protected PageListProcessor createPageListFetcherForGroup(List<String> categories,
            List<String> categoriesToIgnore) throws BotFatalError {
        PageListFetcher fetcher;
        needsCustomTemlateFiltering = false;
        if (templateFilter == null) {
            fetcher = createSimpleFetcher();
        } else {
            if (!service.supportsFeature(CatScanTools.ServiceFeatures.NEWPAGES_WITH_TEMPLATE)) {
                service = Service.getDefaultServiceForFeature(
                        ServiceFeatures.NEWPAGES_WITH_TEMPLATE, this.service);
            }
            if (service.supportsFeature(CatScanTools.ServiceFeatures.NEWPAGES_WITH_TEMPLATE)) {
                fetcher = new FetcherFactory.NewPagesWithTemplatesFetcher(templateFilter, hours);
            } else {
                needsCustomTemlateFiltering = true;
                fetcher = createSimpleFetcher();
            }
        }
        return createPageListProcessorWithFetcher(this.service, fetcher, categories,
                categoriesToIgnore);
    }

    private PageListFetcher createSimpleFetcher() throws BotFatalError {
        if (!service.supportsFeature(ServiceFeatures.NEWPAGES)) {
            service = Service.getDefaultServiceForFeature(
                    ServiceFeatures.NEWPAGES, this.service);
        }
        if (!service.supportsFeature(ServiceFeatures.NEWPAGES)) {
            throw new BotFatalError("No service available for this project page list.");
        }
        return new FetcherFactory.NewPagesFetcher(hours);
    }

    protected PageListProcessor createPageListProcessor() throws BotFatalError {
        List<PageListProcessor> fetchers = new ArrayList<PageListProcessor>(3);
        fetchers.add(createPageListFetcherForGroup(this.categories, this.categoriesToIgnore));
        for (int i = 0; i < categoryGroups.size(); i++) {
            if (categoryGroups.get(i).size() > 0) {
                fetchers.add(createPageListFetcherForGroup(categoryGroups.get(i),
                        categoryToIgnoreGroups.get(i)));
            }
        }
        if (fetchers.size() > 1) {
            return new ProcessorCombinator(fetchers);
        } else {
            return fetchers.get(0);
        }
    }
    
    protected void sortPagesByRevision(List<Revision> pageInfoList) {
        java.util.Collections.sort(pageInfoList, new Comparator<Revision>() {

            @Override
            public int compare(Revision r1, Revision r2) {                
                return (int)(r2.getRevid() - r1.getRevid());
            }        
            
        });
    }

    protected void sortPagesById(List<Revision> pageInfoList) {
        java.util.Collections.sort(pageInfoList, new Comparator<Revision>() {

            @Override
            public int compare(Revision r1, Revision r2) {                
                return (int)(((RevisionWithId)r2).getId() - ((RevisionWithId)r1).getId());
            }        
            
        });
    }
    
    protected void sortPagesByName(List<Revision> pageInfoList) {
        java.util.Collections.sort(pageInfoList, new Comparator<Revision>() {

            @Override
            public int compare(Revision r1, Revision r2) {                
                return r1.getPage().compareTo(r2.getPage());
            }        
            
        });
    }
    
    protected void sortPagesByDate(ArrayList<Revision> pageInfoList) {
        java.util.Collections.sort(pageInfoList, new Comparator<Revision>() {

            @Override
            public int compare(Revision r1, Revision r2) {                
                return r2.getTimestamp().compareTo(r1.getTimestamp());
            }        
            
        });
    }

    protected void sortPages(List<Revision> pageInfoList, boolean byRevision) {
        log.debug("Sort pages ({} items) by revision: {}", pageInfoList.size(), byRevision);
        if (byRevision) {
            sortPagesByRevision(pageInfoList);
        } else {
            sortPagesById(pageInfoList);
        }
    }

    protected List<Revision> getNewPages(NirvanaWiki wiki) throws IOException,
            InterruptedException, ServiceError, BotFatalError {
        PageListProcessor pageListProcessor = createPageListProcessor();
        CatScanTools.resetStat();
        log.info("Using pagelist fetcher: {}", pageListProcessor);
        if (getRevisionMethod == GetRevisionMethod.GET_REV) {
            getRevisionMethod = GetRevisionMethod.GET_FIRST_REV;
        }
        List<Revision> pageInfoList = pageListProcessor.getNewPages(wiki);
        
        pageInfoList = filterPagesByCondition(pageInfoList, wiki);

        sortPages(pageInfoList, false);

        if (pageListProcessor.mayHaveDuplicates()) {
            removeDuplicatesInSortedList(pageInfoList);
        }
        return pageInfoList;
    }
    
    protected List<Revision> filterPagesByCondition(List<Revision> pageInfoList, NirvanaWiki wiki)
            throws IOException {
        if (this.templateFilter == null ||
                (!templateFilter.paramValueFiltering() && !needsCustomTemlateFiltering)) {
            return pageInfoList;
        }
        ArrayList<Revision> list = new ArrayList<>();
        String templatePrefix = wiki.namespaceIdentifier(Wiki.TEMPLATE_NAMESPACE) + ":";
        WikiBooster booster = WikiBooster.create(wiki, pageInfoList, 
                StringTools.addPrefixToList(templateFilter.getTemplates(), templatePrefix));
        TemplateFinder finder =
                new TemplateFinder(templateFilter.getParamFilterItems(), wiki, booster);
        for (Revision r: pageInfoList) {
            if (finder.find(r.getPage())) {
                list.add(r);
            }
            booster.removePage(r.getPage());
        }
        return list;
    }
    
    public class PageInfo {
        String item;
        String title;
        boolean exists;

        PageInfo(String item) {
            this.item = item;
            this.exists = true;
            extractTitle();
        }

        void extractTitle() {
            title = newPageItemParser.getNewPagesItemArticle(item);
            if (title != null) title = WikiUtils.unescapeTitle(title);
        }

        String fullTitle() {
            if (!namespaceIdentifier.isEmpty() && title != null && !title.contains(":")) {
                return namespaceIdentifier + ":" + title;
            }
            return title;
        }
    }
    
    private void checkDeleted(NirvanaWiki wiki, List<PageInfo> pages) throws IOException {
        ArrayList<String> titles = new ArrayList<String>();
        for (int j = 0; j < pages.size(); j++) {
            String fullTitle = pages.get(j).fullTitle(); 
            if (fullTitle != null) {
                titles.add(fullTitle);
            }
        }
        boolean[] pagesExist = wiki.exists(titles.toArray(new String[0]));
        for (int j = 0, k = 0; j < pages.size(); j++) {
            if (pages.get(j).fullTitle() != null) {
                pages.get(j).exists = pagesExist[k];
                k++;
            }    
        }
    }

    protected void analyzeOldText(NirvanaWiki wiki, String text,
            UpdateResults updateResults, NewPagesBuffer buffer) throws IOException {
        log.debug("analyzing old text");
        String oldText = text;
        String[] oldItems;
        
        // remove {{bots|allow=}} record
        log.debug("analyzing old text -> trancate header/footer");
        oldText = pageFormatter.stripBotsAllowString(oldText);
        oldText = pageFormatter.stripDecoration(oldText);

        oldItems = StringUtils.splitByWholeSeparator(oldText, delimeter); // removes empty items
        if (delimeter.equals("\n")) {
            log.debug("delimeter is \\n");
        } else if (delimeter.equals("\r")) {
            log.debug("delimeter is \\r");
        } else {
            log.debug("delimeter is \"{}\"", delimeter);
        }
        
    
        log.debug("analyzing old text -> parse items, len = {}", oldItems.length);
        
        updateResults.oldCount = oldItems.length;
        int pos = 0;        
        while (pos < oldItems.length && enableFeatureUpdateFromOld && buffer.size() < maxItems) {
            assert updateResults instanceof NewPagesUpdateResults;
            NewPagesUpdateResults newPagesUpdateResults = (NewPagesUpdateResults) updateResults;
            ArrayList<PageInfo> bunch = new ArrayList<PageInfo>(WIKI_API_BUNCH_SIZE);
            for (; bunch.size() < WIKI_API_BUNCH_SIZE && pos < oldItems.length; pos++) {
                String item = oldItems[pos];
                if (item.isEmpty()) continue;
                log.trace("check old line: \t{}", item);
                if (buffer.checkIsDuplicated(item)) {
                    log.debug("SKIP old line: \t{}",item);
                    continue;
                }
                bunch.add(new PageInfo(item));                
            }            
            checkDeleted(wiki, bunch);
            int j = 0;
            for (; j < bunch.size() && buffer.size() < maxItems; j++) {
                String item = bunch.get(j).item;
                if (!bunch.get(j).exists) {
                    log.debug("REMOVE old line: \t{}", item);
                    newPagesUpdateResults.deletedCount++;
                } else { 
                    log.debug("ADD old line: \t{}", item);
                    buffer.addOldItem(item);
                }
            }
            pos = pos - bunch.size() + j;            
        }
        for (; pos < oldItems.length; pos++) {
            String item = oldItems[pos];
            if (item.isEmpty()) continue;
            log.trace("check old line: \t{}", item);
            if (buffer.checkIsDuplicated(item)) {
                log.debug("SKIP old line: \t{}", item);
                continue;
            }
            log.debug("OUTDATE/ARCHIVE old line: \t{}", item);
            updateResults.outdateItem(item);
        }
    }

    protected NewPagesBuffer createPagesBuffer(NirvanaWiki wiki) {
        return new NewPagesBuffer(wiki);
    }

    protected UpdateResults getData(NirvanaWiki wiki, String text)
            throws IOException, InterruptedException, ServiceError, BotFatalError,
            InvalidLineFormatException, DangerousEditException {
        log.info("Get data for [[{}]]", this.pageName);

        List<Revision> pageInfoList = getNewPages(wiki);

        NewPagesBuffer buffer = createPagesBuffer(wiki);
        for (int i = 0; i < pageInfoList.size() && buffer.size() < maxItems; i++) {
            buffer.addNewPage(wiki, pageInfoList.get(i));
        }
    
        // Add elements from old page
        NewPagesUpdateResults updateResults = new NewPagesUpdateResults();

        if (true/*count < maxItems /*|| archive!=null*/) { 
            analyzeOldText(wiki, text, updateResults, buffer);
        }
        updateResults.newText = buffer.getNewText();
        updateResults.totalCount = buffer.size(); 
        
        updateResults.logStat();

        if (updateResults.totalCount == 0 && updateResults.oldCount > dangerousEditThreshold) {
            throw new DangerousEditException(pageName, updateResults.oldCount,
                    updateResults.totalCount);
        }

        return updateResults;
    }

    private void removeDuplicatesInSortedList(List<Revision> list) {
        log.debug("removing duplicates from list");
        int i = 1;
        while (i < list.size()) {
            if (list.get(i).getPage().equals(list.get(i - 1).getPage())) {
                list.remove(i);
            } else {
                i++;
            }
        }
    }

    protected void enumerateWithHash(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            String item = list.get(i);
            if (item.startsWith("#")) {
                // do nothing
            } else if (item.startsWith("*")) {
                item = "#" + item.substring(1);
            } else {
                item = "# " + item;
            }
            list.set(i, item);
        }
        return;
    }
    
    @Override
    protected StringBuilder makeSummary(UpdateResults updateResults) {
        StringBuilder summaryBuilder = super.makeSummary(updateResults);
        assert updateResults instanceof NewPagesUpdateResults;
        NewPagesUpdateResults newPagesUpdateResults = (NewPagesUpdateResults) updateResults;
        if (enableFeatureArchive && archiveSettings != null
                && newPagesUpdateResults.outdateCount > 0) {
            summaryBuilder.append(", -").append(newPagesUpdateResults.outdateCount).append(" ")
                    .append(localizer.localize("в архив"));
        }
        if (newPagesUpdateResults.deletedCount > 0) {
            summaryBuilder.append(", -").append(newPagesUpdateResults.deletedCount).append(" ")
                    .append(localizer.localize("удаленных"));
        }
        return summaryBuilder;
    }
    
    protected void beforeGetData() throws IOException {
        // Disable this logic for pages which are not updated but recreated
        if (enableFeatureUpdateFromOld) {
            pageFormatter.getHeaderFooterChanges();
        }
    }
    
    protected void afterUpdate(NirvanaWiki wiki, UpdateResults updateResults,
            ReportItem reportData) throws InterruptedException, AfterUpdateFailure {
        super.afterUpdate(wiki, updateResults, reportData);

        assert updateResults instanceof NewPagesUpdateResults;
        NewPagesUpdateResults newPagesUpdateResults = (NewPagesUpdateResults) updateResults;
        
        updateArchiveIfNeed(wiki, newPagesUpdateResults, reportData);
    }
    
    protected void updateArchiveIfNeed(NirvanaWiki wiki, NewPagesUpdateResults updateResults,
            ReportItem reportData) throws InterruptedException, ArchiveUpdateFailure {
        if (!enableFeatureArchive || archiveSettings == null) {
            return;
        }
        reportData.willUpdateArchive();
        if (updateResults.archiveItems != null && updateResults.archiveItems.size() > 0) {
            waitPauseIfNeed();
            log.info("Updating archive");
            try {
                updateArchive(wiki, updateResults);
                reportData.archiveUpdated(updateResults.outdateCount);
            } catch (Exception e) {
                reportData.archiveUpdateError();
                throw new ArchiveUpdateFailure(e);
            }
        }
    }

    protected void waitPauseIfNeed() throws InterruptedException {
        if (NirvanaBot.UPDATE_PAUSE > 0) {
            log.debug("Waiting {} ms", NirvanaBot.UPDATE_PAUSE);
            Thread.sleep(NirvanaBot.UPDATE_PAUSE);
        }
    }

    protected void updateArchive(NirvanaWiki wiki, NewPagesUpdateResults updateResults)
            throws LoginException, IOException {
        Archive defaultArchive = null;
        String defaultArhiveName = null;
        assert archiveSettings != null;
        if (archiveSettings.isSingle()) {
            defaultArhiveName = archiveSettings.archive;            
        } else {
            defaultArhiveName = archiveSettings.getArchiveForDate(systemTime.now());
        }
        defaultArchive = ArchiveFactory.createArchiveAndRead(archiveSettings, wiki,
                defaultArhiveName);
        
        HashMap<String,Archive> hmap = null;
        hmap = new HashMap<String,Archive>(3);
        hmap.put(defaultArhiveName, defaultArchive);

        
        for (int i = updateResults.archiveItems.size() - 1; i >= 0; i--) {
            String item = updateResults.archiveItems.get(i);
            log.debug("Archiving item: {}", item);
            Archive targetArchive = defaultArchive;
            Calendar itemDate = null;
            if (archiveSettings.needItemDate() || archiveSettings.isSplitByPeriod()) {
                // TODO: Make new page item class with a lazy date detection
                itemDate = newPageItemParser.getNewPagesItemDate(wiki, item);
            }
            if (archiveSettings.isSplitByPeriod() && itemDate != null) {
                String arname = archiveSettings.getArchiveForDate(itemDate);
                targetArchive = hmap.get(arname);
                if (targetArchive == null) {
                    targetArchive = ArchiveFactory.createArchiveAndRead(archiveSettings, wiki,
                            arname);
                    hmap.put(arname, targetArchive);
                }
            }
            targetArchive.add(item, itemDate);
        }        

        List<String> archiveNames = hmap.keySet().stream().sorted().collect(Collectors.toList());
        for (String archiveName: archiveNames) {
            Archive thisArchive = hmap.get(archiveName);
            if (thisArchive.newItemsCount() > 0) {
                log.info("Updating {}", archiveName);
                thisArchive.update(wiki, archiveName, minor, bot);
            }
        }           
        return;
    }

}
