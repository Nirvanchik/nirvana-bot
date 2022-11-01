/**
 *  @(#)PageListProcessorFast.java
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
import org.wikipedia.nirvana.wiki.CatScanTools.Service;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Page list processor which is called "fast" because it extracts page list in one HTTP request.
 *
 */
public class PageListProcessorFast extends BasicProcessor {

    /**
     * Creates page list processor using provided requirements and fetcher.
     *
     * @param service Catscan Service class instance.
     * @param cats list of categories to extract pages in
     * @param ignore list of ignored categories
     * @param lang Wiki language
     * @param depth depth to search in
     * @param namespace namespace number to search
     */
    public PageListProcessorFast(Service service, List<String> cats,
            List<String> ignore, String lang, int depth, int namespace,
            PageListFetcher fetcher) {
        super(service, cats, ignore, lang, depth, namespace, fetcher);
    }

    @Override
    public ArrayList<Revision> getNewPages(NirvanaWiki wiki)
            throws IOException, InterruptedException, ServiceError {
        ArrayList<Revision> pageInfoList = new ArrayList<Revision>(50);
        HashSet<String> pages = new HashSet<String>();
        String text = fetcher.loadNewPagesForCatListAndIgnore(service, categories,
                categoriesToIgnore, language, depth, namespace);
        parsePageList(wiki, pages, pageInfoList, null, text);    
        return pageInfoList;
    }

    /* (non-Javadoc)
     * @see org.wikipedia.nirvana.nirvanabot.pagesfetcher.PageListProcessor#mayHaveDuplicates()
     */
    @Override
    public boolean mayHaveDuplicates() {
        // TODO Auto-generated method stub
        return false;
    }

}
