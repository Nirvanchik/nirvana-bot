/**
 *  @(#)BotUtils.java 09.04.2017
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
 * Recommended code page for this file is CP1251 (also called Windows-1251).
 * */

package org.wikipedia.nirvana;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility code for bot settings processing.
 */
public class BotUtils {
    /**
     * Converts String option with multiple values separated by comma into list of Strings.
     */
    public static List<String> optionToList(String option) {
        return optionToList(option, false, ",");
    }

    /**
     * Converts String option with multiple values separated by comma into list of Strings.
     * Items may have double quotes - they will be removed.
     */
    public static List<String> optionToList(String option, boolean withDQuotes) {
        return optionToList(option, withDQuotes, ",");
    }

    /**
     * Converts String option with multiple values separated by specific separator into list of
     * Strings. Items may have double quotes - they will be removed.
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
     */
    public static Set<String> optionToSet(String option, boolean withDQuotes, String separator) {
        Set<String> set = new HashSet<>();        
        return (Set<String>) optionToCollection(option, withDQuotes, separator, set);
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
