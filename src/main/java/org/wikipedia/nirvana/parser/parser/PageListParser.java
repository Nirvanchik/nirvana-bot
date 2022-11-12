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

package org.wikipedia.nirvana.parser.parser;

import org.wikipedia.Wiki;
import org.wikipedia.nirvana.error.ServiceError;

import java.io.IOException;
import java.util.Collection;

/**
 * Interface for parsers used to read Catscan response and convert them to list of pages.
 */
public interface PageListParser {

    /**
     * Parse page list from raw text.
     *
     * @param rawPageList raw text to parse page list from (raw Catscan response).
     * @return list of page info objects.
     */
    Collection<Wiki.Revision> parsePagesList(String rawPageList) throws IOException, ServiceError;
}
