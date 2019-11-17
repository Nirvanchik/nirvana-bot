/**
 *  @(#)NumberTools.java
 *  Copyright © 2014 Dmitry Trofimovich (KIN, Nirvanchik) (DimaTrofimovich@gmail.com)
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

import org.wikipedia.nirvana.annotation.LocalizedByHardcode;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility methods for processing numeric data.
 *
 */
@LocalizedByHardcode
public class NumberTools {
    public static final int K = 1024;
    public static final int M = K * K;

    /**
     * Reads file size from string where size can be specified in bytes, kilobytes or megabytes.
     * Examples: 5 = 5 bytes, 5 KB = 5 kilobytes, 5 MB = 5 megabytes.
     *
     * 'B' character is optional (5 KB = 5 K, 5 MB = 5 M). 
     *
     * 'KB' strings allowed in Latin or Cyrillic alphabet.
     *
     * @param str a string to parse file size from.
     * @return file size in bytes.
     */
    public static int parseFileSize(String str) throws NumberFormatException {
        int size = 0;        
        int multi = 1;
        if (StringUtils.endsWithIgnoreCase(str, "K") || StringUtils.endsWithIgnoreCase(str, "К")) {
            multi = K;
            str = str.substring(0, str.length() - 1).trim();
        } else if (StringUtils.endsWithIgnoreCase(str, "KB") ||
                StringUtils.endsWithIgnoreCase(str, "КБ")) {
            multi = K;
            str = str.substring(0, str.length() - 2).trim();
        } else if (StringUtils.endsWithIgnoreCase(str, "M") ||
                StringUtils.endsWithIgnoreCase(str, "М")) {
            multi = M;
            str = str.substring(0, str.length() - 1).trim();
        } else if (StringUtils.endsWithIgnoreCase(str, "MB") ||
                StringUtils.endsWithIgnoreCase(str, "МБ")) {
            multi = M;
            str = str.substring(0, str.length() - 2).trim();
        }
        size = Integer.parseInt(str);        
        return (size * multi);
    }
}
