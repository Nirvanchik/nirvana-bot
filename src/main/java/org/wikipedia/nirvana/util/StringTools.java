/**
 *  @(#)StringTools.java
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

package org.wikipedia.nirvana.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Some utility methods for strings manipulations which you won't find in
 * the {@link org.apache.commons.lang3.StringUtils}
 */
public class StringTools {
    public static final String DOUBLE_QUOTE = "\"";

    /**
     * Trims double quotes if incoming string is quoted.
     *
     * @param str a string to trim.
     * @return trimmed string.
     */
    public static String trimDoubleQuoteIfFound(String str) {
        if (str.startsWith(DOUBLE_QUOTE) && str.endsWith(DOUBLE_QUOTE)) {
            str = StringUtils.removeStart(str, DOUBLE_QUOTE);
            str = StringUtils.removeEnd(str, DOUBLE_QUOTE);
        }
        return str;
    }

    /**
     * Counts how many times the specified string contains the specified character.
     *
     * @param str string to check.
     * @param ch character to count.
     * @return number of occurrences of the character in the string.
     */
    public static int howMany(String str, char ch) {
        return howMany(str, ch, false);
    }

    /**
     * Counts how many times the specified string contains the specified character.
     *
     * @param str string to check.
     * @param ch character to count.
     * @param continuous if <code>true</code> only adjacent characters will be counted (the first
     *     sequence).
     * @return number of occurrences of the character in the string.
     */
    public static int howMany(String str, char ch, boolean continuous) {
        int n = 0;
        for (char x : str.toCharArray()) {
            if (x == ch) {
                n++;
            } else if (continuous) {
                break;
            }
        }
        return n;
    }

    /**
     * Trims empty spaces from the left side of the string.
     *
     * @param str string to trim.
     * @return trimmed string.
     */
    public static String trimLeft(String str) {
        return str.replaceAll("^\\s+", "");
    }

    /**
     * Trims empty spaces from the right side of the string.
     *
     * @param str string to trim.
     * @return trimmed string.
     */
    public static String trimRight(String str) {
        return str.replaceAll("\\s+$", "");
    } 

    /**
     * Splits the string by the specified separator characters from the end (bottom).
     * Only 1 split character is supported.
     *
     * @param str a string to split.
     * @param separatorChars string with chars used to split (1 character is allowed only).
     * @param max maximum number of splits to do.
     * @return array with resulting string split to pieces.
     */
    public static String[] splitBottom(String str, String separatorChars, int max) {

        if (str == null) {
            return null;
        }
        int len = str.length();
        if (len == 0) {
            return new String[0];
        }
        List<String> list = new ArrayList<String>();
        int sizePlus1 = 1;
        int i = len - 1;
        int start = len;
        boolean match = false;
        if (separatorChars.length() == 1) {
            // Optimise 1 character case
            char sep = separatorChars.charAt(0);
            while (i >= 0) {
                if (str.charAt(i) == sep) {
                    if (match) {
                        if (sizePlus1++ == max) {
                            i = -1;
                        }
                        list.add(0, str.substring(i + 1, start));
                        match = false;
                    }
                    start = i--;
                    continue;
                }
                match = true;
                i--;
            }
        } else {
            throw new IllegalArgumentException(String.format(
                    "\"separatorChars\" argument must have 1 character, but has %d",
                    separatorChars.length()));
        }
        if (match) {
            list.add(0, str.substring(i + 1, start));
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Truncates the string from right to specified length. Does nothing if the string is shorter.
     *
     * @param text a string to truncate.
     * @param len length to truncate to.
     * @return truncated string.
     */
    public static String trancateTo(String text, int len) {
        return text.length() > len ? text.substring(0, len) : text;
    }

    /**
     * Adds same prefix to all list items.
     *
     * @param list a list of strings to add prefix
     * @param prefix the prefix
     * @return new list of prefixed strings
     */
    public static List<String> addPrefixToList(List<String> list, String prefix) {
        List<String> result = new ArrayList<String>(list.size());
        for (String str: list) {
            result.add(prefix + str);
        }
        return result;
    }
}
