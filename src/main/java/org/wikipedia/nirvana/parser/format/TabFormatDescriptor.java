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

import javax.annotation.Nullable;

/**
 * Describes options used to correctly parse TSV (tab-formatted) responce of Catscan service.
 */
public class TabFormatDescriptor implements FormatDescriptor {
    public static final int NO_POSITION = -1;
    public static final String NO_LINE_RULE = "";

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
            @Nullable String lineRule) {
        this.skipLines = checkNotNegative(skipLines);
        this.namespacePos = check(namespacePos);
        this.titlePos = checkNotNegative(titlePos);
        this.idPos = check(idPos);
        this.lineRule = StringUtils.isEmpty(lineRule) ? DEFAULT_LINE_RULE : lineRule;
    }
    
    private static int checkNotNegative(int number) {
        if (number < 0) throw new IllegalArgumentException();
        return number;
    }

    private static int check(int number) {
        if (number < 0 && number != NO_POSITION) throw new IllegalArgumentException();
        return number;
    }
    
    /**
     * Helper class to build {@link TabFormatDescriptor}.
     */
    public static class Builder {
        public int skipLines = 0;
        public int namespacePos = -1;
        public int titlePos = -1;
        public int idPos = -1;
        // Regex to check validity of line
        public String lineRule = NO_LINE_RULE;

        /**
         * Creates default builder.
         */
        public Builder() {
            
        }

        /**
         * Specify page title position.
         */
        public Builder withTitlePosition(int titlePos) {
            this.titlePos = titlePos;
            return this;
        }

        /**
         * Specify namespace position.
         */
        public Builder withNamespacePosition(int namespacePos) {
            this.namespacePos = namespacePos;
            return this;
        }
        
        /**
         * Specify page id position.
         */
        public Builder withPageIdPosition(int idPos) {
            this.idPos = idPos;
            return this;
        }

        /**
         * Specify how many lines parser must skip (header lines).
         */
        public Builder shouldSkipLines(int skipLines) {
            this.skipLines = skipLines;
            return this;
        }

        /**
         * Set regular expression to check each line. If line doesn't match we throw error. 
         */
        public Builder eachLineMustMatchRule(String lineRule) {
            this.lineRule = lineRule;
            return this;
        }

        /**
         * Builds {@link TabFormatDescriptor} instance.
         */
        public TabFormatDescriptor build() {
            return new TabFormatDescriptor(skipLines, namespacePos, titlePos, idPos, lineRule);
        }
    }

}
