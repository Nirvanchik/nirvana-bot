/**
 *  @(#)MockWikiTools.java 06.06.2016
 *  Copyright © 2016 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.wiki;

import org.wikipedia.nirvana.wiki.CatScanTools;

import java.util.List;

/**
 * This is just a wrapper for {@link CatScanTools} to access its package-visible methods not intended
 * for public access.
 */
public class MockCatScanTools {

    public static void reset() {
        CatScanTools.resetFromTest();
    }
    
    public static void mockResponses(List<String> responces) {
        CatScanTools.mockResponces(responces);
    }

    public static List<String> getQueries() {
        return CatScanTools.getQueries();
    }
}
