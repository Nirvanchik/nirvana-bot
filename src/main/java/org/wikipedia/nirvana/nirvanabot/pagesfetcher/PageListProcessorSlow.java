/**
 *  @(#)PageListProcessorSlow.java
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

import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.error.ServiceError;
import org.wikipedia.nirvana.nirvanabot.NirvanaBot;
import org.wikipedia.nirvana.parser.format.TabFormatDescriptor;
import org.wikipedia.nirvana.parser.format.TabularFormat;
import org.wikipedia.nirvana.parser.parser.DefaultPageListParser;
import org.wikipedia.nirvana.parser.parser.PageListParser;
import org.wikipedia.nirvana.wiki.CatScanTools;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Page list processor implementation which is called "slow".
 * It fetches page lists for each requested category one by one using catscan fetcher,
 * then merges them, then fetches page lists of ignore categories one by one, and remove them
 * from final list. It can make many network requests and therefore it is referred to as slow.
 */
public class PageListProcessorSlow extends BasicProcessor {
    protected Map<String,String> pageLists;
    protected Map<String,String> pageListsToIgnore;
    
    /**
     * Constructs page list processor class with provided requirements and fetcher.
     *
     * @param service Catscan Service class instance.
     * @param cats list of categories to extract pages in
     * @param ignore list of ignored categories
     * @param lang Wiki language
     * @param depth depth to search in
     * @param namespace namespace number to search
     */
    public PageListProcessorSlow(CatScanTools.Service service, List<String> cats,
            List<String> ignore, String lang, int depth, int namespace, PageListFetcher fetcher) {
        super(service, cats, ignore, lang, depth, namespace, fetcher);        
    }

    @Override
    public ArrayList<Revision> getNewPages(NirvanaWiki wiki)
            throws IOException, InterruptedException, ServiceError {
        ArrayList<Revision> pageInfoList = new ArrayList<Revision>(30);
        HashSet<String> pages = new HashSet<String>();
        getData(wiki);
        HashSet<String> ignore = getIgnorePages(wiki);        
        for (String category : categories) {
            log.info("Processing data of {}", category);
            String pageList = pageLists.get(category);
            parsePageList(wiki, pages, pageInfoList, ignore, pageList);                        
        }
        return pageInfoList;
    }
    
    void getData(Wiki wiki) throws IOException, InterruptedException {        
        pageLists = getNewPagesForCategories(categories);        
        pageListsToIgnore = getNewPagesForCategories(categoriesToIgnore);        
    }
    
    private Map<String,String> getNewPagesForCategories(List<String> categoriList)
            throws IOException, InterruptedException {
        Map<String,String> result = new HashMap<String,String>();
        for (String category : categoriList) {            
            Pair<Integer, String> pair = extractDepthFromCat(category);            
            String text = fetcher.loadNewPagesForCat(service, pair.getRight(), language,
                    pair.getLeft(), namespace);
            result.put(category, text);
        }
        return result;
    }
    
    protected Pair<Integer, String> extractDepthFromCat(String category) {        
        int depth = this.depth;
        int depthIndex = category.indexOf(NirvanaBot.DEPTH_SEPARATOR);
        if (depthIndex > 0) {
            String depthStr = category.substring(depthIndex + NirvanaBot.DEPTH_SEPARATOR.length());
            try {
                depth = Integer.parseInt(depthStr);
            } catch (NumberFormatException e) {
                // ignore
            }
            category = category.substring(0, depthIndex);            
        }
        return new ImmutablePair<Integer, String>(depth, category);
    }

    HashSet<String> getIgnorePages(NirvanaWiki wiki)
            throws IOException, ServiceError {
        HashSet<String> ignore = new HashSet<String>();
        assert service.getFormat() instanceof TabularFormat;

        TabFormatDescriptor descriptor =
                ((TabularFormat) service.getFormat()).getFormatDescriptor();
        PageListParser parser = new DefaultPageListParser(service, wiki, descriptor, namespace);
        for (String category : categoriesToIgnore) {        
            log.debug("Processing data of ignore category: {}", category);
            String pageListRaw = this.pageListsToIgnore.get(category);
            Collection<Revision> pageInfos = parser.parsePagesList(pageListRaw);
            for (Revision pageInfo: pageInfos) {
                String title = pageInfo.getPage();
                log.debug("Add page to ignore list: {}", title);
                ignore.add(title);
            }
        }
        return ignore;
    }

    @Override
    public boolean mayHaveDuplicates() {        
        return false;
    }
}
