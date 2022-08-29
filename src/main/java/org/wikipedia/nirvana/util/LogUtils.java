/**
 *  @(#)LogUtils.java
 *  Copyright © 2022 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Logging utilities.
 *
 */
public class LogUtils {

    private LogUtils() {        
    }
    
    /**
     * Logs map of String keys and values.
     *
     * @param log logger.
     * @param parameters parameters map.
     */
    public static void logParametersMap(Logger log, Map<String, String> parameters) {
        logParametersMap(log, parameters, false);
    }

    /**
     * Logs map of String keys and values.
     *
     * @param log logger.
     * @param parameters parameters map.
     * @param border print border line before and after.
     */
    public static void logParametersMap(Logger log, Map<String, String> parameters,
            boolean border) {
        Set<Entry<String,String>> set = parameters.entrySet();
        Iterator<Entry<String,String>> it = set.iterator();
        if (border) {
            log.debug("----< params >----");
        }
        while (it.hasNext()) {
            Entry<String,String> next = it.next();
            log.debug("{} = {}", next.getKey(), next.getValue());
        }        
        if (border) {
            log.debug("------------------");
        }
    }
}
