/**
 *  @(#)SettingsUtils.java
 *  Copyright © 2024 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.base;

import java.util.Properties;
import java.util.function.Function;

/**
 * Basic utils used for bot settings processing.
 *
 */
public class SettingsUtils {

    /**
     * Parse property specified by key from properties dictionary using specified enum parser.
     * Return parsed enum value or provided default value if property not found.
     *
     * @param properties Properties dictionary.
     * @param key Property name.
     * @param defaultVal Default value for this property.
     * @param propertyParser Parser function for this property enum.
     * @return Property enum value.
     */
    public static <T> T propertyToEnum(Properties properties, String key, T defaultVal,
            Function<String, T> propertyParser) throws BotSettingsError {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) return defaultVal;
        T result = propertyParser.apply(value);
        if (result == null) {
            throw new BotSettingsError(
                    String.format("Cannot parse value \"%s\" for property \"%s\".",
                    value, key));
        }
        return result;
    }
}
