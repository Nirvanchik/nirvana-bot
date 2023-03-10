/**
 *  @(#)XmlTools.java
 *  Copyright © 2019 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.util;

/**
 * Xml related utilities.
 *
 */
public class XmlTools {
    /**
     * Unescape some XML-escapted characters: ", ', &.
     * Mostly used to unescape some text string received from MediaWiki Rest API, which likes to
     * escape such symbols in their XML responces.
     *
     * @param text String to unescape.
     * @return Unescaped string.
     */
    public static String removeEscape(String text) {
        if (text == null) return text;
        return text.replace("&quot;","\"").replace("&#039;", "'").replace("&amp;", "&");
    }

    /**
     * Unescapes escaped string where "&quot;" words are replaced with '"' symbols.
     *
     * @param text text to unescape.
     * @return unescaped text.
     */
    public static String unescapeSimple(String text) {
        if (text == null) return text;
        return text.replace("&quot;","\"");
    }
}
