/**
 *  @(#)PageListProcessor.java
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

import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.error.ServiceError;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import java.io.IOException;
import java.util.List;

/**
 * Interface for page list processing implementation classes.
 *
 * They take page list requirements (categories, namespaces, depth, time range, etc.),
 * fetch page list using provided fetcher, parse it, convert item to desired fromat 
 * (Revision class), make some post processing (merge multiple lists into one) and return result.
 * 
 * So basically, we have inputs: page list fetcher class, page list requirements, and 
 * we produce output: list of pages put in Revision objects. 
 */
public interface PageListProcessor {
    /**
     * Fetches new pages list using specified fetcher and pages requirements and 
     * produces list of pages.
     * 
     * @param wiki {@class NirvanaWiki} instance.
     * @return list of page items.
     */
    List<Revision> getNewPages(NirvanaWiki wiki)
            throws IOException, InterruptedException, ServiceError;

    // TODO: Remove duplicates at processor level and stop returning them.
    /**
     * @return "true" if page list processor can produce duplicated results.
     */
    boolean mayHaveDuplicates();
}
