/**
 *  @(#)EnumerationUtils.java
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

/**
 * Utilities related to HTML enumeration.
 *
 */
public class EnumerationUtils {
    public static final String OL = "<ol>";
    public static final String OL_END = "</ol>";

    /**
     * Removes html enumeration tags from text (if text starts/ends with them).
     *
     * @param text text to process
     * @return updated text (with enumerations tags stripped).
     */
    public static String trimEnumerationAndWhitespace(String text) {
        String oldText = text.trim();
        if (oldText.startsWith(OL)) {
            oldText = oldText.substring(OL.length());        
        }
        if (oldText.endsWith(OL_END)) {
            oldText = oldText.substring(0, oldText.length() - OL_END.length());
        }
        oldText = oldText.trim();
        return oldText;
    }
}
