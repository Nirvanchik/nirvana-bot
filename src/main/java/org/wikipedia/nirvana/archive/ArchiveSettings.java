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

import javax.annotation.Nullable;

/**
 * Collects archive settings.
 */
public class ArchiveSettings {
    /**
     * Title of a wiki page that contains a list of archived new pages items. Can have placeholders.
     * For placeholders list see {@link ArchiveSettings.Period}.
     */
    @Nullable
    public String archive;
    /**
     * Archive header format if archive has headers of 1 level.
     * Archive header format of the second level if archive has headers of 2 levels.
     */
    @Nullable
    public String headerFormat;
    /**
     * Archive top-level header format if archive has headers of 2 levels.
     */
    @Nullable
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
    // This is a global setting, remove it out of here.
    /**
     * How many items to parse when parsing old content of archive from wiki page.
     * Actual for archives with headers where bot must parse content in order to add new items to
     * specific paragraphs. Bot finds required paragraphs and parses content for that.
     * But parsing a lot of items can slow down the bot, so we parse it partially.
     * This value tells how many lines to parse instead of full parsing.
     */
    public int parseCount;

    /**
     * A methods to enumarate items in archive.
     */
    public enum Enumeration {
        // items are not enumerated by bot, they are may be already enumerated with wiki syntax
        NONE,
        // bot adds #
        HASH,
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
        parseCount = ArchiveWithHeaders.HOW_MANY_ITEMS_TO_PARSE_DEFAULT; 
    }

    /**
     * Return <code>true</code> if new page list is with archive, it requires to update archive
     * according its settings.
     */
    public boolean withArchive() {
        return archive != null;
    }

    /**
     * @return <code>true</code> if archive is saved on a single-page (not split into many pages).
     */
    public boolean isSingle() {
        return (archivePeriod == Period.NONE);
    }

    /**
     * @return <code>true</code> if archive is split into many pages and each page is updated only
     *     a limited time range.
     */
    public boolean isSplitByPeriod() {
        return (archivePeriod != Period.NONE);
    }

    /**
     * @return <code>true</code> if archive has no headers.
     */
    public boolean withoutHeaders() {
        return (headerFormat == null);
    }

    /**
     * @return <code>true</code> if archive has headers.
     */
    public boolean hasHeaders() {
        return (headerFormat != null);
    }

    /**
     * @return <code>true</code> if archive requires item creation date to correctly add it to
     *     archive (select right section).
     */
    public boolean needItemDate() {
        return hasHeaders();
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
