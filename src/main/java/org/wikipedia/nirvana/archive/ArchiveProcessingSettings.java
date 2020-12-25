/**
 *  @(#)ArchiveProcessingSettings.java
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

import javax.annotation.Nullable;

// TODO: Split it to separate classes.
/**
 * Class to keep settings of archive update rules, wiki page locations and processing rules.
 * Used by StatisticsBot and FixArchiveBot.
 */
public class ArchiveProcessingSettings extends ArchiveSettings {
    private static int START_YEAR_DEFAULT = 2003;
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
    @Nullable
    public String firstArchive;

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
     * Constructs instance of ArchiveProcessingSettings.
     */
    public ArchiveProcessingSettings() {
        super();
        startYear = START_YEAR_DEFAULT;
        startYear2 = -1;
        firstArchive = null;
        sorted = false;
        removeDuplicates = false;
    }
}
