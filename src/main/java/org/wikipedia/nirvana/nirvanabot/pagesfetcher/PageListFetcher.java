/**
 *  @(#)PageListFetcher.java 13.12.2014
 *  Copyright © 2014 Dmitry Trofimovich (KIN)(DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.nirvanabot.pagesfetcher;

import org.wikipedia.nirvana.wiki.CatScanTools.Service;

import java.io.IOException;
import java.util.List;

/**
 * @author kin
 *
 */
public interface PageListFetcher {
	/**
     * @param service
     * @param categories
     * @param categoriesToIgnore
     * @param language
     * @param depth
     * @param namespace
     * @return
     */
    String loadNewPagesForCatListAndIgnore(Service service, 
            List<String> categories, List<String> categoriesToIgnore,
            String language, int depth, int namespace) throws IOException, InterruptedException;
	
    String loadNewPagesForCat(Service service, 
    		String category, String language, int depth, int namespace) throws IOException, InterruptedException;

}
