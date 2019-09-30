/**
 *  @(#)OptionsUtils.java
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Utility code for bot settings processing.
 */
public class OptionsUtils {
    public static final Logger sLog;

    static {
        sLog = LogManager.getLogger(OptionsUtils.class.getName());
    }

    /**
     * Converts String option with multiple values separated by comma into list of Strings.
     */
    public static List<String> optionToList(String option) {
        return optionToList(option, false, ",");
    }

    /**
     * Converts String option with multiple values separated by comma into list of Strings.
     *
     * If <code>withDQuotes</code> argument is <code>true</code>, items may be optionally folded
     * into double quotes - they will be removed. Double quotes are used when some of the items have
     * ',' in their contents. All items or no items should be with double quotes. Mixed content of
     * double-quoted and simple strings not allowed.
     * Limitations: it's not possible to parse list if items have both double quotes and comma.
     */
    public static List<String> optionToList(String option, boolean withDQuotes) {
        return optionToList(option, withDQuotes, ",");
    }

    /**
     * Converts String option with multiple values separated by specific separator into list of
     * Strings. Items may have double quotes - they will be removed.
     * Converts String option with multiple values separated by specific separator into list of
     * Strings.
     *
     * If <code>withDQuotes</code> argument is <code>true</code>, items may be optionally folded
     * into double quotes - they will be removed. Double quotes are used when some of the items have
     * ',' in their contents. All items or no items should be with double quotes. Mixed content of
     * double-quoted and simple strings not allowed.
     * Limitations: it's not possible to parse list if items have both double quotes and comma.
     *
     * @param option option string to parse.
     * @param withDQuotes set to <code>true</code> to remove optional double quotes from every item.
     * @param separator Separator string. This string is a regular expression put in split() method.
     * @return list of items parsed from input string.
     */
    public static List<String> optionToList(String option, boolean withDQuotes, String separator) {
        ArrayList<String> list = new ArrayList<String>();
        return (List<String>) optionToCollection(option, withDQuotes, separator, list);
    }

    /**
     * Converts String option with multiple values separated by comma into set of Strings.
     */
    public static Set<String> optionToSet(String option) {
        return optionToSet(option, false, ",");
    }

    /**
     * Converts String option with multiple values separated by specific separator into set of
     * Strings. Items may have double quotes - they will be removed.
     * 
     * The same as {@link #optionToList(String, boolean, String)} but returned collection is a set.
     * @param option option string to parse.
     * @param withDQuotes set to <code>true</code> to remove optional double quotes from every item.
     * @param separator Separator string. This string is a regular expression put in split() method.
     * @return set of items parsed from input string.
     */
    public static Set<String> optionToSet(String option, boolean withDQuotes, String separator) {
        Set<String> set = new HashSet<>();        
        return (Set<String>) optionToCollection(option, withDQuotes, separator, set);
    }

    /**
     * Returns integer value in given {@link Properties} object or default value if it's not found.
     *
     * @param props {@link Properties} object, properties must be loaded beforehand.
     * @param name the name of property to get
     * @param def default value
     * @param notifyNotFound write error to log if this property not found
     * @return property value (converted to int) or default value
     */
    public static int validateIntegerSetting(Properties props, String name, int def,
            boolean notifyNotFound) {
        try {
            if (!props.containsKey(name)) {
                if (notifyNotFound) {
                    sLog.info("settings: value of {} not found in settings, use default: {}",
                            name, def);
                }
                return def;
            }
            String str = props.getProperty(name);
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            sLog.error("invalid settings: error when parsing integer value of {}, use default: {}",
                    name, def);
            return def;
        }
    }

    private static Collection<String> optionToCollection(String option, boolean withDQuotes,
            String separator, Collection<String> result) {
        String separatorPattern;
        if (withDQuotes && option.contains("\"")) {
            separatorPattern = "(\"\\s*" + separator + "\\s*\"|^\\s*\"|\"\\s*$)"; 
        } else {
            separatorPattern = separator;
        }
        String[] items = option.split(separatorPattern);
        for (int i = 0; i < items.length; ++i) {
            String cat = items[i].trim();
            if (!cat.isEmpty()) {
                result.add(cat);
            }
        }
        return result;
    }
}
