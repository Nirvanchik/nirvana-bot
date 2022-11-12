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

/**
 * Formats of Catscan responses.
 */

public interface Format {

    enum FormatType {
        CSV("csv"),
        TSV("tsv");

        private final String formatType;

        FormatType(String formatType) {
            this.formatType = formatType;
        }

        public String getFormatType() {
            return formatType;
        }
    }

    FormatDescriptor getFormatDescriptor();

    FormatType getFormatType();
}
