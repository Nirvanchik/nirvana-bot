/**
 *  Copyright © 2022 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
 *  @author kmorozov
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

package org.wikipedia.nirvana.parser.format;

import org.apache.commons.lang3.StringUtils;

/**
 * Describes options used to correctly parse TSV (tab-formatted) responce of Catscan service.
 */
public class TabFormatDescriptor implements FormatDescriptor {

    private static final String DEFAULT_LINE_RULE = "^.+$";

    public final int skipLines;
    public final int namespacePos;
    public final int titlePos;
    public final int idPos;
    // Regex to check validity of line
    public final String lineRule;

    /**
     * Constructs descriptor class using specified options.
     *
     * @param skipLines how many non-informative lines should be skipped
     * @param namespacePos position of namespace item (set -1 if it's absent).
     * @param titlePos position of page title item.
     * @param idPos position of page id item (set -1 if it's absent).
     * @param lineRule Regex to check validity of line. If any line doesn't match this rule parser
     *     will throw error.
     */
    public TabFormatDescriptor(int skipLines, int namespacePos, int titlePos, int idPos,
            String lineRule) {
        this.skipLines = skipLines;
        this.namespacePos = namespacePos;
        this.titlePos = titlePos;
        this.idPos = idPos;
        this.lineRule = StringUtils.isEmpty(lineRule) ? DEFAULT_LINE_RULE : lineRule;
    }

}
