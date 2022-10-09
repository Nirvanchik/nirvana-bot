/**
 *  @(#)PageListProcessorSlow.java 14.12.2014
 *  Copyright © 2013 - 2014 Dmitry Trofimovich (KIN)(DimaTrofimovich@gmail.com)
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.error.ServiceError;
import org.wikipedia.nirvana.nirvanabot.NirvanaBot;
import org.wikipedia.nirvana.util.StringTools;
import org.wikipedia.nirvana.wiki.CatScanTools;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

/**
 * @author kin
 *
 */
public class PageListProcessorSlow extends BasicProcessor {
	protected Map<String,String> pageLists;
    protected Map<String,String> pageListsToIgnore;
    
	/**
	 * @param cats
	 * @param ignore
	 * @param lang
	 * @param depth
	 * @param hours
	 * @param namespace
	 */
    public PageListProcessorSlow(CatScanTools.Service service, List<String> cats,
            List<String> ignore, String lang, int depth, int namespace, PageListFetcher fetcher) {
		super(service, cats, ignore, lang, depth, namespace, fetcher);		
	}

	/** 
	 * 
	 */
	@Override
	public ArrayList<Revision> getNewPages(NirvanaWiki wiki) throws IOException, InterruptedException, ServiceError {
		ArrayList<Revision> pageInfoList = new ArrayList<Revision>(30);
		HashSet<String> pages = new HashSet<String>();
		getData(wiki);
		HashSet<String> ignore = getIgnorePages(wiki, null);		
		for(String category : categories) {		
			log.info("Processing data of " + category);
			String pageList = pageLists.get(category);
			parsePageList(wiki, pages, pageInfoList, ignore, pageList);					    
		}
		return pageInfoList;
	}
	
	public void getData(Wiki wiki) throws IOException, InterruptedException {		
		//log.info("Getting data for [[" + this.pageName+"]]");
		pageLists = getNewPagesForCategories(categories);		
		pageListsToIgnore = getNewPagesForCategories(categoriesToIgnore);		
	}
	
	private Map<String,String> getNewPagesForCategories(List<String> categoriList) throws IOException, InterruptedException {
		Map<String,String> result = new HashMap<String,String>();
		for (String category : categoriList) {			
			Pair<Integer, String> pair = PageListProcessorSlow.extractDepthFromCat(category);			
		    String text = fetcher.loadNewPagesForCat(service, pair.getRight(), language, (pair.getLeft() >= 0)? pair.getLeft(): depth, namespace);
		    result.put(category, text);
		}
		return result;
	}
	
	protected static Pair<Integer, String> extractDepthFromCat(String category) {    	
		int depth = -1;
		int depthIndex = category.indexOf(NirvanaBot.DEPTH_SEPARATOR);
		if (depthIndex > 0) {
			try {
				depth = Integer.parseInt(category.substring(depthIndex + NirvanaBot.DEPTH_SEPARATOR.length()));
			} catch (NumberFormatException e) {
				// ignore
			}
			category = category.substring(0, depthIndex);			
		}
		return new ImmutablePair<Integer, String>(depth, category);
	}
	
	public HashSet<String> getIgnorePages(NirvanaWiki wiki, HashSet<String> ignorePages) throws IOException, ServiceError {
		HashSet<String> ignore = ignorePages;
		if(ignore==null)
			ignore = new HashSet<String>();
		for(String category : categoriesToIgnore) {		
            log.debug("Processing data of ignore category: {}", category);
	        String line;
	        String pageList = this.pageListsToIgnore.get(category);
	        if (pageList.startsWith("ERROR : MYSQL error")) {
	        	log.error("Invalid service output: "+StringTools.trancateTo(pageList, 100));
	        	throw new ServiceError("Invalid output of service: "+service.getName());
	        }
	        BufferedReader br = new BufferedReader(new StringReader(pageList));
            for (int j = 0; j < service.skipLines; j++) {
                br.readLine();
            }
        	Pattern p = Pattern.compile(LINE_RULE);
        	int j = 0;
	        while ((line = br.readLine()) != null)
	        {
	        	j++;
	        	if (line.isEmpty()) continue;
	        	if (j<LINES_TO_CHECK && !p.matcher(line).matches()) {
	        		log.error("Invalid service output line: "+line);
	        		throw new ServiceError("Invalid output of service: "+service.getName());
	        	}
                // TODO: Move this low level TSV parsing out of here.
	            String[] groups = line.split("\t");

                if (!service.filteredByNamespace) {
                    throw new IllegalStateException(
                            CatScanTools.ERR_SERVICE_FILTER_BY_NAMESPACE_DISABLED);
                }

                if (service.filteredByNamespace) {
	                String title = groups[service.titlePos].replace('_', ' ');
                    log.debug("Add page to ignore list: {}", title);
	                ignore.add(title);
                }
	        }		    
		}
		return ignore;
	}

	/* (non-Javadoc)
     * @see org.wikipedia.nirvana.nirvanabot.pagesfetcher.PageListProcessor#mayHaveDuplicates()
     */
    @Override
    public boolean mayHaveDuplicates() {	    
	    return false;
    }
}
