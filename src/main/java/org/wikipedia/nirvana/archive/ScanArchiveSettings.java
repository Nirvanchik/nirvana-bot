/**
 *  @(#)UberArchiveSettings.java
 *  Copyright © 2021 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import javax.annotation.Nullable;

/**
 * Extended version of {@link ArchiveSettings} that knows about archive pages range and can be used
 * to scan all existing archives.
 */
public class ScanArchiveSettings extends ArchiveSettings {

    protected static int START_YEAR_DEFAULT = 2003;
    /**
     * Starting year of Wiki Project new pages archive.
     * Used by StatisticsBot and FixArchiveBot.
     */
    public int startYear = START_YEAR_DEFAULT;
    /**
     * Second starting year of Wiki Project new pages archive.
     * Used when the first archive page has multiple years merged.
     * The next year after them will be set by this parameter.
     * Used by StatisticsBot and FixArchiveBot.
     */
    public int startYear2 = -1;
    /**
     * First archive wiki page.
     * Used to mark a year by year archive page which does not match "archive" parameter.
     * Usually this is a wiki page with multiple years merged into one.
     * Used by StatisticsBot and FixArchiveBot.
     */
    @Nullable
    public String firstArchive = null;

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
     * Default constructor. 
     */
    public ScanArchiveSettings() {
        super();
    }

}