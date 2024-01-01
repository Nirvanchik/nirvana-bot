/**
 *  @(#)InvalidLineFormatException.java 16.04.2017
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

package org.wikipedia.nirvana.error;

import org.wikipedia.nirvana.localization.Localizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Exception thrown when user mistyped placeholders in the format string.
 * It's better not to update list with broken item format and report about problem.
 */
public class InvalidLineFormatException extends Exception {
    private static final long serialVersionUID = 1L;

    private static final Pattern FIND_UNRECOGNIZED_PLACEHOLDERS = 
            Pattern.compile("%\\([^\\)]+\\)");

    public InvalidLineFormatException(String paramName, String lineFormat) {
        super(generateErrorString(paramName, lineFormat));
    }

    private static String generateErrorString(String paramName, String lineFormat) {
        Localizer localizer = Localizer.getInstance();
        String format = localizer.localize("В параметре %1$s обнаружены нераспознанные ключи: ");
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(format, paramName));
        Matcher m = FIND_UNRECOGNIZED_PLACEHOLDERS.matcher(lineFormat);
        boolean has = false;
        while (m.find()) {
            sb.append(m.group()).append(", ");
            has = true;
        }
        if (has) {
            sb.setLength(sb.length() - ", ".length());
        }
        sb.append(".");
        return sb.toString();
    }
}
