/**
 *  @(#)TextUtils.java
 *  Copyright © 2017 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Utility code related to text parsing.
 */
public class TextUtils {
    private static final Logger sLog;

    static {
        sLog = LogManager.getLogger(TextUtils.class);
    }

    public static boolean textOptionsToMap(String text, Map<String, String> parameters) {
        return textOptionsToMap(text, parameters, false, "#", "//");
    }

    public static boolean textOptionsToMap(String text, Map<String, String> parameters,
            boolean emptyIsNull) {        
        return textOptionsToMap(text, parameters, emptyIsNull, "#", "//");
    }

    /**
     * Parse text with parameters written in INI format. Puts parsed key/value pairs to map.
     * Example text:
     *   param1 = value1
     *   A=B
     * This will be parsed as a map with param1=value1 and A=B params.
     *
     * @param text text to parse
     * @param parameters map to put result data
     * @param emptyIsNull will put null for params specified as empty ("A=")
     * @param commentSeparators Lines starting with these strings will be skipped.
     * @return <code>false</code> if there were lines without "=" symbol.
     */
    public static boolean textOptionsToMap(String text, Map<String, String> parameters,
            boolean emptyIsNull, String... commentSeparators) {
        String[] lines = text.split("\r|\n");
        boolean allLinesHaveEqualSign = true;
        for (String line: lines) {
            String trimLine = line.trim();
            if (trimLine.isEmpty()) continue;
            if (commentSeparators != null && commentSeparators.length > 0) {
                if (StringUtils.startsWithAny(trimLine, commentSeparators)) {
                    continue;
                }
            }
            int index = trimLine.indexOf("=");
            String left;
            String right;
            if (index < 0) {
                allLinesHaveEqualSign = false;
                left = trimLine;
                right = "";
            } else {
                left = trimLine.substring(0, index).trim();
                right = trimLine.substring(index + 1).trim();
            }            
            if (emptyIsNull && right.isEmpty()) {
                right = null;
            }
            parameters.put(left, right);
        }
        return allLinesHaveEqualSign;
    }
}
