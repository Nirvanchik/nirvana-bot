/**
 *  @(#)DiscussedPages.java
 *  Copyright © 2024 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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
import org.wikipedia.nirvana.base.BotFatalError;
import org.wikipedia.nirvana.error.InvalidLineFormatException;
import org.wikipedia.nirvana.error.ServiceError;
import org.wikipedia.nirvana.nirvanabot.DiscussionPagesSettings.DiscussionPageTemplate;
import org.wikipedia.nirvana.util.DateTools;
import org.wikipedia.nirvana.util.OptionsUtils;
import org.wikipedia.nirvana.util.SystemTime;
import org.wikipedia.nirvana.util.XmlTools;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Portal page that generates list of articles selected by categories and additional criteria
 * and having link to discussion optionally. This can be a discussion of one type, or mix of
 * different types. If page has many links, it will be added to list multiple times.
 * Some article issues are importan and discussed at special pages in wiki, some are minor and
 * not discussed. This page list generator can combine both types. 
 *
 */
public class DiscussedPages extends Pages {
    String prefix;
    DiscussionPagesSettings settings;
    List<DiscussionPageTemplate> discussionTemplates;
    private final DateTools dateTools;

    protected class DiscussedPagesBuffer extends NewPagesBuffer {

        public DiscussedPagesBuffer(NirvanaWiki wiki) {
            super(wiki);            
        }

        @Override
        protected String formatTimeString(Revision rev) {
            // TODO: Migrate it to Java8 dates
            return dateTools.printDateDayMonthYearGenitive(GregorianCalendar.from(
                    rev.getTimestamp().atZoneSameInstant(ZoneId.systemDefault())));
        }

        @Override
        String formatItemString(String title, boolean deleted, Revision rev)
                throws InvalidLineFormatException {
            log.debug("Format item for page: {}", title);
            String element;
            String user = rev.getUser();
            String time = formatTimeString(rev);            
            String titleToInsert = formatTitleString(title);
            
            RevisionWithDiscussion revDisc = (RevisionWithDiscussion) rev;
            DiscussionInfo discussion = revDisc.getDiscussion();

            if (discussion != null) {
                log.debug("This item has discussion link");
                // TODO: Why do we check this every time? Move it somewhere else.
                if (format.contains(BotVariables.DISCUSSION)) {
                    log.debug("format has discussion placeholder -> add discussion");
                    String formatExt = formatString.replace(BotVariables.DISCUSSION, "%4$s");
                    checkPlaceholders(formatExt);
                    String link = discussion.template.formatLinkForPage(
                            discussion.discussionPage, title,
                            stripFragment(discussion.discussionFragment));
                    element = String.format(formatExt, titleToInsert, XmlTools.removeEscape(user),
                            time, link);
                } else {
                    element = String.format(formatString, titleToInsert,
                            XmlTools.removeEscape(user), time);
                }
            } else {
                element = String.format(NirvanaBot.DEFAULT_FORMAT_STRING, titleToInsert);
            }
            return element;
        }
    }
    
    private String stripFragment(String fragment) {
        if (fragment == null) {
            return null;
        }
        if (fragment.isEmpty()) {
            return fragment;
        }
        return fragment.replace("<s>", "").replace("</s>", "").trim().replace("[[", "")
                .replace("]]", "");
    }
    
    private static class RevisionWithDiscussion extends Revision {
        DiscussionInfo discussion;

        /**
         * Constructs RevisionWithDiscussion object.
         */
        public RevisionWithDiscussion(Wiki wiki, Revision revision, DiscussionInfo discussion) {
            wiki.super(revision.getRevid(), revision.getTimestamp(), revision.getPage(),
                    revision.getSummary(), revision.getUser(),
                    revision.isMinor(), revision.isBot(), revision.isNew(), revision.getSize());
            this.discussion = discussion;
            sLog.debug("Created RevisionWithDiscussion for page {}: {}", revision.getPage(),
                    discussion == null ? "none" : discussion.discussionPage);
        }

        @Override
        public OffsetDateTime getTimestamp()
        {
            // This is for sort() only
            if (discussion != null) {
                return OffsetDateTime.ofInstant(discussion.discussionDate.toInstant(),
                        ZoneId.systemDefault());
            } else {
                return super.getTimestamp();
            }
        }

        public DiscussionInfo getDiscussion() {
            return discussion;
        }
        
    }

    public static class DiscussionInfo implements Comparable<DiscussionInfo> {
        // TODO: Use Java8 dates
        Calendar discussionDate;
        String discussionPage;
        DiscussionPageTemplate template;
        String discussionFragment;

        /**
         * Constructs DiscussionInfo using provided discussion date, discussion page name, and
         * discussion page template.
         */
        public DiscussionInfo(Calendar discussionDate, String discussionPage,
                DiscussionPageTemplate template) {
            this.discussionDate = discussionDate;
            this.discussionPage = discussionPage;
            this.template = template;
            this.discussionFragment = null;
        }

        @Override
        public int compareTo(DiscussionInfo obj) {            
            return discussionDate.compareTo(obj.discussionDate);
        }
    }

    /**
     * Constructs class instance using bot params and special {@link DiscussionPagesSettings}
     * settings. 
     */
    public DiscussedPages(PortalParam param, PageFormatter pageFormatter, SystemTime systemTime,
            DiscussionPagesSettings settings) {
        super(param, pageFormatter, systemTime);
        this.prefix = param.prefix;
        this.settings = settings;
        getRevisionMethod = GetRevisionMethod.GET_FIRST_REV_IF_NEED;
        this.discussionTemplates = filterDiscussionTemplates();
        dateTools = DateTools.getInstance();
        this.checkPlaceholdersBeforeUpdate = false;
    }

    private List<DiscussionPageTemplate> filterDiscussionTemplates() {
        List<DiscussionPageTemplate> list = new ArrayList<>();
        for (DiscussionPageTemplate t:this.settings.templates) {
            if (templateFilter.getTemplates().contains(t.template)) {
                list.add(t);
            }
        }
        if (this.prefix != null && !prefix.isEmpty()) {
            List<DiscussionPageTemplate> list2 = new ArrayList<>();
            List<String> prefixes = OptionsUtils.optionToList(prefix);
            for (String prefix:prefixes) {
                boolean found = false;
                for (DiscussionPageTemplate t:list) {
                    if (t.prefix.equalsIgnoreCase(prefix)) {
                        list2.add(t);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    DiscussionPageTemplate custom = DiscussionPageTemplate.fromPrefix(prefix);
                    if (custom != null) {
                        list2.add(custom);
                    }
                }
            }
        }
        return list;
    }
    
    @Override
    protected NewPagesBuffer createPagesBuffer(NirvanaWiki wiki) {
        return new DiscussedPagesBuffer(wiki);
    }
    
    @Override
    public void sortPages(List<Revision> pageInfoList, boolean byRevision) {
        // no sort (sort implemented in another place)
        log.debug("Skip sort (will do later)");
    }    

    @Override
    protected List<Revision> getNewPages(NirvanaWiki wiki) throws IOException,
            InterruptedException, ServiceError, BotFatalError {
        List<Revision> pageInfoList = super.getNewPages(wiki);
        ArrayList<Revision> pageInfoListNew = new ArrayList<Revision>();
        for (int i = 0; i < pageInfoList.size() && pageInfoListNew.size() < maxItems; ++i) {
            Revision revision = pageInfoList.get(i);
            log.debug("Search discussion links for page {}", revision.getPage());
            String[] linkedPages = wiki.whatLinksHere(revision.getPage(), Wiki.PROJECT_NAMESPACE);

            SortedSet<DiscussionInfo> discussionSet = new TreeSet<DiscussionInfo>();
            for (String linked:linkedPages) {
                for (DiscussionPageTemplate template: discussionTemplates) {
                    log.debug("Search discussion links at linked page {} for template: {}", linked,
                            template.template);
                    if (template.hasLink() && linked.startsWith(template.prefix)) {
                        String right = linked.substring(template.prefix.length());
                        Calendar c = dateTools.parseDateStringDayMonthYearGenitive(right);
                        if (c != null) {
                            log.debug("Found discussion link: {}", linked);
                            discussionSet.add(new DiscussionInfo(c, linked, template));
                        }
                    }
                }
            }
            DiscussionInfo lastDiscussion = null;
            if (discussionSet.size() > 0) {
                log.debug("Search last discussion for page {}", revision.getPage());
                lastDiscussion = discussionSet.last();
                if (lastDiscussion.template.needsToSearchFragment()) {
                    lastDiscussion.discussionFragment =
                            searchFragment(wiki, lastDiscussion.discussionPage, revision.getPage());
                }
            }
            pageInfoListNew.add(new RevisionWithDiscussion(wiki, revision, lastDiscussion));
        }
        sortPagesCustom(pageInfoListNew);
        return pageInfoListNew;
    }

    /**
     * Custom sort of list of pages.
     * Sort using 2 criteria:
     * 1) sort it by having or not discussion link;
     * 2) sort it by page title.
     */
    public void sortPagesCustom(List<Revision> pageInfoList) {
        // TODO Isn't it better to make compareTo() ?
        java.util.Collections.sort(pageInfoList, new Comparator<Revision>() {

            @Override
            public int compare(Revision r1, Revision r2) {
                assert r1 instanceof RevisionWithDiscussion;
                assert r2 instanceof RevisionWithDiscussion;

                RevisionWithDiscussion revD1 = (RevisionWithDiscussion) r1;
                RevisionWithDiscussion revD2 = (RevisionWithDiscussion) r2;
                if (revD1.getDiscussion() != null && revD2.getDiscussion() != null) {
                    return r2.getTimestamp().compareTo(r1.getTimestamp());
                } else if (revD1.getDiscussion() != null && revD2.getDiscussion() == null) {
                    return 1;
                } else if (revD1.getDiscussion() == null && revD2.getDiscussion() != null) {
                    return -1;
                } else {
                    return r1.getPage().compareToIgnoreCase(r2.getPage());
                }
            }
        });
    }

    /**
     * Search the section at the specified wiki discussion page that relates to the article
     * specified by user. The section name with "#" prefix can be used to make direct wiki link to
     * article discussion.
     */
    public static String searchFragment(NirvanaWiki wiki, String discussionPage, String page)
            throws IOException {
        sLog.debug("Search fragment for page {} on page {}", page, discussionPage);
        String[] lines = wiki.getPageLinesArray(discussionPage);
        Pattern p = Pattern.compile("\\s*===?\\s*([^=]+\\s*)=?==\\s*");
        for (String line:lines) {
            if (line.contains(page)) {
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    sLog.debug("Found fragment on line: {}", line);
                    return m.group(1);
                }
            }
        }        
        sLog.debug("Fragment not found.");
        return null;
    }
}
