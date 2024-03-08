/**
 *  @(#)NewPagesWeek.java
 *  Copyright © 2011 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.base.BotFatalError;
import org.wikipedia.nirvana.error.ArchiveUpdateFailure;
import org.wikipedia.nirvana.error.InvalidLineFormatException;
import org.wikipedia.nirvana.error.ServiceError;
import org.wikipedia.nirvana.nirvanabot.report.ReportItem;
import org.wikipedia.nirvana.util.SystemTime;
import org.wikipedia.nirvana.wiki.CatScanTools;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.security.auth.login.LoginException;

/**
 * New pages block generator that generates 7 separate new pages lists, that show pages created 
 * at the last day and 6 days before last days respectively, they are shown in a block that
 * has links "today", "1 day before", "2 days before", etc.
 *
 */
public class NewPagesWeek extends NewPages {

    // TODO: Something is broken here. It should use this HOURS value as "max_age" argument for
    // catscan but it doesn't. Currently it uses default value (350 hours).
    // Someone can set smaller value of "hours" parameter and break the week.
    public static final int HOURS = 7 * 24;
    private static final int FIRST_DAY = 0;
    private static final int DAYS = 7;
    private static final int LAST_DAY = FIRST_DAY - DAYS + 1;

    private final String pageTitleFormat;

    /**
     * Constructs class instance using bot params.
     */
    public NewPagesWeek(PortalParam param, PageFormatter pageFormatter, SystemTime systemTime) {
        super(param, pageFormatter, systemTime);
        enableFeatureUpdateFromOld = false;
        pageTitleFormat = "%1$s/" + localizer.localize("День") + " %2$d";
    }

    public class WeekUpdateResults extends UpdateResults {
        Map<String, UpdateResults> days = new LinkedHashMap<String, UpdateResults>(DAYS);
    }

    private class WeekBuffer extends NewPagesBuffer {
        NewPagesBuffer[] buffers = new NewPagesBuffer[DAYS];
        Calendar[] dates = new Calendar[DAYS];

        // TODO: Migrate to Java8 dates.
        public WeekBuffer(NirvanaWiki wiki) {
            super(wiki);
            Calendar calStart = now();
            calStart.set(Calendar.HOUR_OF_DAY, 0);
            calStart.set(Calendar.MINUTE, 0);
            calStart.set(Calendar.SECOND, 0);
            calStart.set(Calendar.MILLISECOND, 0);    
            for (int i = 0; i < DAYS; i++) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(calStart.getTimeInMillis());
                dates[i] = c;
                buffers[i] = new NewPagesBuffer(wiki);
                calStart.add(Calendar.DAY_OF_MONTH, -1);
            }
        }

        protected Calendar now() {
            return NewPagesWeek.this.now();
        }

        @Override
        protected void addNewItem(String title, Revision rev) throws IOException,
                InvalidLineFormatException {
            String element = formatItemString(title, false, rev);

            if (!subset.contains(element)) {
                for (int i = 0; i < DAYS; i++) {
                    Calendar revTimestamp = GregorianCalendar.from(
                            rev.getTimestamp().atZoneSameInstant(ZoneId.systemDefault())); 
                    if (revTimestamp.compareTo(dates[i]) >= 0) {
                        buffers[i].addNewItem(title, rev);
                        subset.add(element);
                        if (includedPages != null) {
                            includedPages.add(title);
                        }
                        break;
                    }
                }
            }
        }
    }
    
    protected UpdateResults getData(NirvanaWiki wiki) throws IOException, InterruptedException,
            ServiceError, BotFatalError, InvalidLineFormatException {
        log.info("Processing data for [[{}]]", this.pageName);

        List<Revision> pageInfoList = getNewPages(wiki);

        WeekUpdateResults weekUpdateResults = new WeekUpdateResults();
        WeekBuffer buffer = new WeekBuffer(wiki);
        int count = pageInfoList.size();
        for (int i = 0; i < count ; ++i) {
            buffer.addNewPage(wiki, pageInfoList.get(i));
        }

        for (int day = 0, dayNum = 0; day < DAYS; day++, dayNum--) {
            String dayPageName = String.format(pageTitleFormat, this.pageName, dayNum);
            UpdateResults updateResults = new UpdateResults();
            NewPagesBuffer dayBuf = buffer.buffers[day];
            updateResults.newText = dayBuf.getNewText();
            updateResults.totalCount = dayBuf.size();
            if (dayNum == 0) {
                weekUpdateResults.totalCount = updateResults.totalCount; 
            } else if (dayNum == LAST_DAY) {
                String text = wiki.getPageText(pageName);
                if (text != null) {
                    analyzeOldText(wiki, text, updateResults, dayBuf);
                    weekUpdateResults.archiveCount = updateResults.archiveCount;
                    weekUpdateResults.archiveItems = updateResults.archiveItems;
                    weekUpdateResults.makeArchiveText();
                }
            }
            weekUpdateResults.days.put(dayPageName, updateResults);
        }
        return weekUpdateResults; 
    }

    @Override
    public boolean update(NirvanaWiki wiki, ReportItem reportData, String comment)
            throws IOException, LoginException, InterruptedException, ServiceError, BotFatalError,
            InvalidLineFormatException, ArchiveUpdateFailure {
        boolean updated = false;

        // TODO: This page may be not existing - handle this.
        String text = getOldText(wiki);
        log.debug("old text retrieved");
        if (!checkAllowBots(wiki, text)) {
            return false;
        }

        pageFormatter.getHeaderFooterChanges();
        WeekUpdateResults weekData;
        try {
            weekData = (WeekUpdateResults)getData(wiki);
        } finally {
            reportData.reportCatscanStat(CatScanTools.getQuieriesStat());
        }
        reportData.willUpdateNewPages();
        for (Entry<String,UpdateResults> entry: weekData.days.entrySet()) {
            UpdateResults updateResults = entry.getValue();
            String str;
            if (updateResults.newPagesCount() > 0) {
                str = new StringBuilder("+")
                        .append(updateResults.newPagesCount()).append(" ")
                        .append(localizer.localize("статей"))
                        .toString();
            } else {
                str = comment;
            } 
            try {
                log.info("Updating [[{}]] {}", entry.getKey(), str);
                if (wiki.editIfChanged(entry.getKey(), updateResults.newText, str, this.minor,
                        this.bot)) {
                    updated = true;
                    reportData.newPagesUpdated(updateResults.newPagesCount());
                    waitPauseIfNeed();
                }
            } catch (Exception e) {
                reportData.newPagesUpdateError();
                throw e;
            }
        }
        if (updated) {
            pageFormatter.notifyNewPagesUpdated();
        }
        updateArchiveIfNeed(wiki, weekData, reportData);

        return updated;
    }
}
