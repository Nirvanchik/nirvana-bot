/**
 *  @(#)PageListFetcher.java 13.12.2014
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

package org.wikipedia.nirvana.nirvanabot.pagesfetcher;

import org.wikipedia.nirvana.wiki.CatScanTools.Service;

import java.io.IOException;
import java.util.List;

// TODO: May be separate it into two interface classes?
// TODO: Why String is returned here (raw HTTP response)?
//     I think it should return List<String> with parsed results.
/**
 * Interface for page list fetcher classes.
 * Abstracts two methods to fetch lists of wiki pages:
 * 1) method that fetches pages of one category
 * 2) method that fetches pages of many categories and result is cleaned of pages of ignored
 * categories.
 * 
 * Implementations may fetch pages using different arguments and in a different way but mostly
 * they should do only one HTTP GET (or POST) request and return result as... as a String?.
 */
public interface PageListFetcher {
    /**
     * Fetches list of pages using specified requirements. Search is going in many categories.
     * Ignore categories specify which categories should be skipped (removed from result).
     * 
     * @param service Catscan service that will be used for fetching.
     * @param categories list of categories to search pages in
     * @param categoriesToIgnore list of ignored categories
     * @param language Wiki language
     * @param depth category depth to search in
     * @param namespace namespace number to search
     * @return list of page infos in TSV format (raw response from Catscan service)
     */
    String loadNewPagesForCatListAndIgnore(Service service, 
            List<String> categories, List<String> categoriesToIgnore,
            String language, int depth, int namespace) throws IOException, InterruptedException;
    
    /**
     * Fetches list of pages using specified requirements.
     *
     * @param service Catscan service that will be used for fetching.
     * @param category category to search pages in
     * @param language Wiki language
     * @param depth category depth to search in
     * @param namespace namespace number to search
     * @return list of page infos in TSV format (raw response from Catscan service)
     */
    String loadNewPagesForCat(Service service, 
            String category, String language, int depth, int namespace)
                    throws IOException, InterruptedException;

}
