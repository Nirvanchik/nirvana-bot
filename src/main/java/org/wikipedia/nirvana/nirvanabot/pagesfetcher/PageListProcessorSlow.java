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
import org.wikipedia.nirvana.util.StringTools;
import org.wikipedia.nirvana.wiki.CatScanTools;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
        HashSet<String> ignore = getIgnorePages(wiki, null);        
        for (String category : categories) {        
            log.info("Processing data of " + category);
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
    
    // TODO: What is this? Most of code is a copy-paste of parsePageList() Refactor it.
    HashSet<String> getIgnorePages(NirvanaWiki wiki, HashSet<String> ignorePages)
            throws IOException, ServiceError {
        HashSet<String> ignore = ignorePages;
        if (ignore == null) {
            ignore = new HashSet<String>();
        }
        assert service.getFormat() instanceof TabularFormat;

        TabFormatDescriptor descriptor = ((TabularFormat) service.getFormat()).getFormatDescriptor();
        for (String category : categoriesToIgnore) {        
            log.debug("Processing data of ignore category: {}", category);
            String line;
            String pageList = this.pageListsToIgnore.get(category);
            if (pageList.startsWith("ERROR : MYSQL error")) {
                log.error("Invalid service output: {}", StringTools.trancateTo(pageList, 100));
                throw new ServiceError("Invalid output of service: " + service.getName());
            }
            BufferedReader br = new BufferedReader(new StringReader(pageList));
            for (int j = 0; j < descriptor.getSkipLines(); j++) {
                br.readLine();
            }
            Pattern p = Pattern.compile(descriptor.getLineRule());
            int j = 0;
            while ((line = br.readLine()) != null) {
                j++;
                if (line.isEmpty()) continue;
                if (j < LINES_TO_CHECK && !p.matcher(line).matches()) {
                    log.error("Invalid service output line: {}", line);
                    throw new ServiceError("Invalid output of service: " + service.getName());
                }
                // TODO: Move this low level TSV parsing out of here.
                String[] groups = line.split("\t");

                if (!service.filteredByNamespace) {
                    throw new IllegalStateException(
                            CatScanTools.ERR_SERVICE_FILTER_BY_NAMESPACE_DISABLED);
                }

                if (service.filteredByNamespace) {
                    String title = groups[descriptor.getTitlePos()].replace('_', ' ');
                    log.debug("Add page to ignore list: {}", title);
                    ignore.add(title);
                }
            }            
        }
        return ignore;
    }

    @Override
    public boolean mayHaveDuplicates() {        
        return false;
    }
}
