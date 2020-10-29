/**
 *  @(#)ArchiveSettings.java
 *  Copyright © 2020 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.archive;

import org.wikipedia.nirvana.util.DateTools;

import java.util.Calendar;

// TODO: split it to updated archive settings and settings for archive processing bots.
/**
 * Collects archive settings.
 */
public class ArchiveSettings {
    private static int START_YEAR_DEFAULT = 2003;
    /**
     * Title of a wiki page that contains a list of archived new pages items. Can have placeholders.
     * For placeholders list see {@link ArchiveSettings.Period}.
     */
    public String archive;
    /**
     * Archive header format if archive has headers of 1 level.
     * Archive header format of the second level if archive has headers of 2 levels.
     */
    public String headerFormat;
    /**
     * Archive top-level header format if archive has headers of 2 levels.
     */
    public String superHeaderFormat;
    /**
     * If <code>true</code> new items should be added at the top. If <code>false</code> new items
     * should be added at the bottom.
     */
    public boolean addToTop;
    /**
     * Flag for processing existing archive. Deleted items should be removed from archive.
     */
    public boolean removeDeleted;
    /**
     * Rotation period of 1 achive page. After expiring this period new archive page is created.
     * Detected automatically from placeholders in the archive page name.
     */
    public Period archivePeriod;
    /**
     * Enumeration type selected to enumerate new page items in archive.
     */
    public Enumeration enumeration;
    /**
     * Flag for processing existing archive. If <code>true</code> archive should be sorted according
     * to new page creation dage and 'addToTop' parameter.
     */
    public boolean sorted;
    /**
     * Flag for processing existing archive. If <code>true</code> the code should search duplicated
     * items and remove them.
     */
    public boolean removeDuplicates;
    /**
     * How many items to parse when parsing old content of archive from wiki page.
     * Actual for archives with headers where bot must parse content in order to add new items to
     * specific paragraphs. Bot finds required paragraphs and parses content for that.
     * But parsing a lot of items can slow down the bot, so we parse it partially.
     * This value tells how many lines to parse instead of full parsing.
     */
    public int parseCount;
    /**
     * Starting year of Wiki Project new pages archive.
     * Used by StatisticsBot and FixArchiveBot.
     */
    public int startYear;
    /**
     * Second starting year of Wiki Project new pages archive.
     * Used when the first archive page has multiple years merged.
     * The next year after them will be set by this parameter.
     * Used by StatisticsBot and FixArchiveBot.
     */
    public int startYear2;
    /**
     * First archive wiki page.
     * Used to mark a year by year archive page which does not match "archive" parameter.
     * Usually this is a wiki page with multiple years merged into one.
     * Used by StatisticsBot and FixArchiveBot.
     */
    public String firstArchive;

    /**
     * Sets starting year of this archive.
     */
    public static void setDefaultStartYear(int year) {
        START_YEAR_DEFAULT = year;
    }

    /**
     * @return starting year of this archive.
     */
    public static int getDefaultStartYear() {
        return START_YEAR_DEFAULT;
    }

    /**
     * A methods to enumarate items in archive.
     */
    public enum Enumeration {
        // items are not enumerated by bot, they are may be already enumerated with wiki syntax
        NONE,
        // bot adds #
        HASH,
        // bot adds html tags. Every paragraph has independent enumeration
        HTML,
        // bot adds html tags. Enumeration is global through the whole archive page.
        HTML_GLOBAL,
        // ?
        DETECT
    }

    /**
     * Wiki archive page period types (archives split periodity). 
     *
     */
    public enum Period {
        NONE(100, null, false),
        DAY(1, "%(день)", true),
        WEEK(2, "%(неделя)", true),
        MONTH(3, "%(месяц)", false),
        MONTHRP(3, "%(месяц в родительном падеже)", false),
        QUARTER(4, "%(квартал)", true),
        SEASON(4, "%(сезон)", true),
        YEAR(6, "%(год)", true);

        private final int degree;
        private final String template;
        private final boolean numeric;

        Period(int degree, String template, boolean numeric) {
            this.degree = degree;
            this.template = template;
            this.numeric = numeric;
        }

        public int degree() { return degree; }

        public String template() { return template; }

        public boolean isNumeric() { return numeric; }
    }

    /**
     * Default constructor.
     */
    public ArchiveSettings() {
        archive = null;
        archivePeriod = Period.NONE;
        addToTop = true;
        removeDeleted = false;
        headerFormat = null;
        superHeaderFormat = null;
        enumeration = Enumeration.NONE;
        sorted = false;
        removeDuplicates = false;
        parseCount = ArchiveWithHeaders.HOW_MANY_ITEMS_TO_PARSE_DEFAULT; 
        startYear = START_YEAR_DEFAULT;
        startYear2 = -1;
        firstArchive = null;
    }

    /**
     * @return <code>true</code> if archive is saved on a single-page (not split into many pages).
     */
    public boolean isSingle() {
        return (archivePeriod == Period.NONE);
    }
    
    /**
     * @return <code>true</code> if archive has no headers.
     */
    public boolean withoutHeaders() {
        return (headerFormat == null);
    }

    // TODO: This method is unclear.
    /**
     * @return <code>true</code> if archive is simple (single-page, no headers, no html enumeration.
     */
    public boolean isSimple() {
        return ((addToTop == true) && isSingle() && withoutHeaders() && !hasHtmlEnumeration()); 
    }

    /**
     * @return <code>true</code> if archive has html enumeration.
     */
    public boolean hasHtmlEnumeration() {
        return (enumeration == Enumeration.HTML || enumeration == Enumeration.HTML_GLOBAL);
    }

    /**
     * Parses header format string and returns header period. If format string contains multiple
     * period placeholders, the shorter period will be selected.
     *
     * @param format header format string.
     * @return header period.
     */
    public static ArchiveSettings.Period getHeaderPeriod(String format) {
        Period periodMin = Period.NONE;
        for (Period p : Period.values()) {
            if (p.template != null && format.contains(p.template)) {
                if (p.degree < periodMin.degree) {
                    periodMin = p;
                }
            }
        }
        return periodMin;
    }

    /**
     * Resolve wiki page name of the archive for the specified date.
     *
     * @param date A date for which to select archive wiki page.
     * @return resolved wiki page name. 
     */
    public String getArchiveForDate(Calendar date) {
        if (archivePeriod == Period.NONE) {
            return archive;
        }
        String name = archive;
        if (name.contains(Period.YEAR.template)) {
            name = name.replace(Period.YEAR.template, String.valueOf(date.get(Calendar.YEAR)));
        }
        if (name.contains(Period.SEASON.template)) {
            name = name.replace(Period.SEASON.template,
                    DateTools.getInstance().seasonString(date.get(Calendar.MONTH) / 3));
        }
        if (name.contains(Period.QUARTER.template)) {
            name = name.replace(Period.QUARTER.template,
                    String.valueOf(date.get(Calendar.MONTH) / 3 + 1));
        }
        if (name.contains(Period.MONTH.template)) {
            name = name.replace(Period.MONTH.template,
                    DateTools.getInstance().monthString(date.get(Calendar.MONTH)));
        }
        return name;
    }

    /**
     * Resolve header name for the specified date using specified header format string (replace
     * placeholders in the header format string with real values and return result).
     *
     * @param date a date for which to get header name.
     * @param format header format string (with placeholders).
     * @return header name.
     */
    public static String getHeaderForDate(Calendar date, String format) {
        String header = format;
        if (header == null) {
            return header;
        }
        DateTools dateTools = DateTools.getInstance();
        String year = String.valueOf(date.get(Calendar.YEAR));
        String season = dateTools.seasonString(date.get(Calendar.MONTH) / 3);
        String quarter = String.valueOf(date.get(Calendar.MONTH) / 3);
        String month = dateTools.monthString(date.get(Calendar.MONTH));
        String monthMonat = dateTools.monthMonatString(date.get(Calendar.MONTH));
        String week = String.valueOf(date.get(Calendar.WEEK_OF_YEAR));
        String day = String.valueOf(date.get(Calendar.DAY_OF_MONTH));
        return header
                .replace(Period.YEAR.template, year)
                .replace(Period.SEASON.template, season)
                .replace(Period.QUARTER.template, quarter)
                .replace(Period.MONTH.template, month)
                .replace(Period.MONTHRP.template, monthMonat)
                .replace(Period.WEEK.template, week)
                .replace(Period.DAY.template, day);
    }

    /**
     * Returns resolved header name for the specified date (placeholders will be replaced with real
     * values).
     *
     * @param date a page creation date.
     * @return header name
     */
    public String getHeaderForDate(Calendar date) {
        return ArchiveSettings.getHeaderForDate(date, headerFormat);
    }

    /**
     * Returns resolved super header name for the specified date (placeholders will be replaced with
     * real values). Super-header is a top-level header if archive has headers of two levels.
     *
     * @param date a page creation date.
     * @return header name
     */
    public String getSuperHeaderForDate(Calendar date) {
        return ArchiveSettings.getHeaderForDate(date, superHeaderFormat);
    }

}
