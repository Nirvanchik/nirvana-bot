/**
 *  @(#)NewPagesWithImages.java
 *  Copyright © 2023 Dmitry Trofimovich (KIN)(DimaTrofimovich@gmail.com)
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
import org.wikipedia.nirvana.base.BasicBot;
import org.wikipedia.nirvana.base.BotFatalError;
import org.wikipedia.nirvana.error.ServiceError;
import org.wikipedia.nirvana.nirvanabot.imagefinder.ImageFinder;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.PageListProcessor;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.RevisionWithId;
import org.wikipedia.nirvana.util.FileTools;
import org.wikipedia.nirvana.util.OptionsUtils;
import org.wikipedia.nirvana.util.SystemTime;
import org.wikipedia.nirvana.util.XmlTools;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * New pages block generator that can find images on wiki page and make a new articles page
 * looking like image gallery.
 *
 */
public class NewPagesWithImages extends NewPages {
    private ImageFinder imageFinder;
    private NirvanaWiki commons;
    private List<String> fairUseImageTemplates;

    /**
     * {@link org.wikipedia.Wiki.Revision} extension that additionally can keep image link.
     *
     */
    private static class RevisionWithImage extends RevisionWithId {
        String image;

        public RevisionWithImage(Wiki wiki, long revid, OffsetDateTime timestamp,
                String title, String summary, String user, boolean minor,
                boolean bot, boolean rvnew, int size, long id, String image) {
            super(wiki, revid, timestamp, title, summary, user, minor, bot, rvnew, size, id);
            this.image = image;
        }

        public RevisionWithImage(Wiki wiki, Revision rev, long id, String image) {
            super(wiki, rev.getRevid(), rev.getTimestamp(), rev.getPage(),
                    rev.getSummary(), rev.getUser(), rev.isMinor(), rev.isBot(), rev.isNew(),
                    rev.getSize(), id);
            this.image = image;
        }

        public String getImage() { return this.image; }

        public void setImage(String image) { this.image = image; }
        
    }

    /**
     * Constructs class instance using bot params, and special for this module additional things
     * like Commons wiki client and {@link ImageFinder} helper class.
     */
    public NewPagesWithImages(PortalParam param, PageFormatter pageFormatter, SystemTime systemTime,
            NirvanaWiki commons, ImageFinder imageFinder) {
        super(param, pageFormatter, systemTime);
        this.enableFeatureArchive = false;
        this.archiveSettings = null;
        this.formatString = formatString.replace(BotVariables.FILE_NAME, "%4$s");
        this.imageFinder = imageFinder;
        this.commons = commons;
        this.fairUseImageTemplates = OptionsUtils.optionToList(param.fairUseImageTemplates);
    }

    @Override
    public UpdateResults getData(NirvanaWiki wiki, String text)
            throws IOException, InterruptedException, ServiceError, BotFatalError {
        log.info("Processing data for [[{}]]", this.pageName);

        PageListProcessor pageListProcessor = createPageListProcessor();
        List<Revision> pageInfoListNotFiltered = pageListProcessor.getNewPages(wiki);
        ArrayList<Revision> pageInfoList = new ArrayList<Revision>(30);

        for (Revision r: pageInfoListNotFiltered) {
            long revId = r.getRevid();
            long id = ((RevisionWithId)r).getId();
            String title = r.getPage();
            Revision page = null;
            String article = wiki.getPageText(r.getPage());
            if (article == null) {
                // Page was created and renamed or deleted after that
                log.warn("Page {} does not exist.", r.getPage()); 
                continue;
            }
            if (wiki.isRedirect(article)) {            
                page = new RevisionWithImage(wiki, wiki.getFirstRevision(title, true), id, null);
                article = wiki.getPageText(page.getPage());
                if (article == null) {
                    // page was created and renamed or deleted after that
                    log.warn("Page {} does not exist. ", page.getPage());
                    continue;
                }
                if (BasicBot.DEBUG_BUILD) {
                    FileTools.dump(article, page.getPage());
                }
            }
            String image = imageFinder.findImage(article);
            if (image != null && checkImageFree(wiki, image)) {    
                log.debug("Image found: {}", image);
                if (page == null) {
                    // TODO: This looks ugly. Revision object is actually fake.
                    page = new RevisionWithImage(wiki, revId, OffsetDateTime.now(), title, "",
                            "", false, false, true, 0, id, image);
                } else {
                    ((RevisionWithImage)page).setImage(image);
                }                    
                pageInfoList.add(page);
                log.debug("adding page to list: {}", title);
            }
        }

        sortPages(pageInfoList, false);

        List<String> subset = new ArrayList<String>();
        List<String> includedPages = new ArrayList<String>();
        List<String> includedImages = new ArrayList<String>();
        int count = pageInfoList.size();
        count = count < maxItems ? count : maxItems;
        for (int i = 0; i < count ; ++i) {

            RevisionWithImage page = (RevisionWithImage)pageInfoList.get(i);
            if (page.getSize() == 0) {
                page = new RevisionWithImage(wiki, wiki.getFirstRevision(page.getPage()),
                        page.getId(), page.getImage()); 
            }

            if (page != null) {                
                String title = XmlTools.removeEscape(page.getPage());
                String time = null;
                if (NirvanaBot.TIME_FORMAT.equalsIgnoreCase("long")) { 
                    // TODO: Is this needed? Is this used?
                    throw new NotImplementedException("long date format not implemented");
                } else {
                    time = page.getTimestamp().atZoneSameInstant(ZoneOffset.UTC)
                            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
                }
                String element = String.format(this.formatString, namespace != 0 
                        ? title.substring(wiki.namespaceIdentifier(this.namespace).length() + 1)
                                : title,
                        XmlTools.removeEscape(page.getUser()), 
                        time, 
                        page.getImage()
                        );
                
                if (!subset.contains(element))
                {
                    subset.add(element);
                    includedPages.add(title);
                    includedImages.add(page.getImage());
                    log.debug("ADD new line: \t{}", element);
                }
            }
        }

        int oldCount = 0;
    
        // Add elements from old page
        if (true/*count < maxItems /*|| archive!=null*/) { 
            String oldText = text;
            String[] oldItems;
            oldText = pageFormatter.stripBotsAllowString(oldText);
            oldText = pageFormatter.stripDecoration(oldText);
            oldText = oldText.trim();
            oldItems = oldText.split(delimeter.replace("|", "\\|"));
            if (delimeter.equals("\n")) {
                log.debug("delimeter is \\n");
            } else if (delimeter.equals("\r")) {
                log.debug("delimeter is \\r");
            } else {
                log.debug("delimeter is {}", delimeter);
            }
     
            oldCount = oldItems.length;
            for (int i = 0; i < oldItems.length; ++i) {
                boolean skip = false;
                if (oldItems[i].isEmpty()) continue;
                for (String image: includedImages) {
                    if (oldItems[i].contains(image + "|")) {
                        skip = true;
                        log.debug("SKIP old line: \t{}", oldItems[i]);
                        break;
                    }
                }
                if (skip) continue;
                if (subset.size() < maxItems)  {
                    if (!subset.contains(oldItems[i])) { 
                        subset.add(oldItems[i]);        
                        log.debug("ADD old line: \t{}", oldItems[i]);
                    } else {
                        log.debug("SKIP old line: \t{}", oldItems[i]);
                    }
                }
            }
        }

        UpdateResults updateResults = new UpdateResults();
        updateResults.newText = pageFormatter.formatPage(subset);
        updateResults.totalCount = subset.size();
        updateResults.logStat();
        
        return updateResults;
    }

    /**
     * We have no power to add thumbnails of images which are non-free (fair use of images)
     * So we check for fair-use templates in image description before using it.
     * 
     * @return true if image looks like free (doesn't have a description with fair use template)
     */
    private boolean checkImageFree(NirvanaWiki wiki, String image) throws IOException {
        String text = wiki.getPageText(
                wiki.namespaceIdentifier(NirvanaWiki.FILE_NAMESPACE) + ":" + image);
        if (text == null) {
            log.info("Failed to get text for image (probably it's on commons): {}", image);
            // Most likely this image comes from Commons and is free to use therefore
            return true;
        }

        for (String tmpl : this.fairUseImageTemplates) {
            if (StringUtils.containsIgnoreCase(text, "{{" + tmpl)) {
                return false;
            }
        }
        return true;
    }

}
