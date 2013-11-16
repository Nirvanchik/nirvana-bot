/**
 *  @(#)PageListFetcherOneByOne.java 28.10.2013
 *  Copyright � 2013 Dmitry Trofimovich (KIN)(DimaTrofimovich@gmail.com)
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
 * Recommended code page for this file is CP1251 (also called Windows-1251).
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

import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.NirvanaWiki;

/**
 * @author kin
 *
 */
public abstract class PageListFetcherOneByOne extends PageListFetcher {

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
	public PageListFetcherOneByOne(List<String> cats, List<String> ignore, String lang,
	        int depth, int hours, int namespace) {
		super(cats, ignore, lang, depth, hours, namespace);		
	}

	/* (non-Javadoc)
	 * @see org.wikipedia.nirvana.nirvanabot.PageListFetcher#getNewPages(org.wikipedia.nirvana.NirvanaWiki, java.util.ArrayList, java.util.HashSet)
	 */
	@Override
	public ArrayList<Revision> getNewPages(NirvanaWiki wiki) throws IOException, InterruptedException {
//	public void getNewPages(NirvanaWiki wiki, ArrayList<Revision> pageInfoList,
//	        HashSet<String> pages) throws IOException, InterruptedException {
		ArrayList<Revision> pageInfoList = new ArrayList<Revision>(30);
		HashSet<String> pages = new HashSet<String>();
		getData(wiki);
		HashSet<String> ignore = getIgnorePages(wiki, null);
		
		for(String category : categories) {		
			log.info("Processing data of " + category);
			String pageList = pageLists.get(category);
			parsePageList(wiki, pages, pageInfoList, ignore, pageList);					    
		}//for
		return pageInfoList;
	}
	
	public void getData(Wiki wiki) throws IOException, InterruptedException {
		
		//log.info("Getting data for [[" + this.pageName+"]]");
		pageLists = new HashMap<String,String>();
		for(String category : this.categories)
		{
		    String text = getNewPagesForCat(category, language, depth, hours);
		    pageLists.put(category, text);
		}
		pageListsToIgnore = new HashMap<String,String>();
		for(String category : this.categoriesToIgnore)
		{
		    String text = getNewPagesForCat(category, language, depth, hours);
		    pageListsToIgnore.put(category, text);
		}	
		
	}
	
	protected abstract String getNewPagesForCat(String category, String language, int depth, int hours) throws IOException, InterruptedException;
	
	public HashSet<String> getIgnorePages(NirvanaWiki wiki, HashSet<String> ignorePages) throws IOException {
		HashSet<String> ignore = ignorePages;
		if(ignore==null)
			ignore = new HashSet<String>();
		for(String category : categoriesToIgnore) {		
			//log.info("Processing data of " + category);
	        String line;
	        String pageList = this.pageListsToIgnore.get(category);
	        BufferedReader br = new BufferedReader(new StringReader(pageList));
        	for(int j=0;j<SKIP_LINES;j++) br.readLine();
	        while ((line = br.readLine()) != null)
	        {
	            String[] groups = line.split("\t");
	            if (groups[NS_POS].equals(String.valueOf(this.namespace)))
	            {
	                String title = groups[TITLE_POS].replace('_', ' ');
	                ignore.add(title);
	            } else if (groups[NS_POS].equals(String.valueOf(Wiki.USER_NAMESPACE)) &&
	            		namespace!= Wiki.USER_NAMESPACE) {
	            	long revId=0;
                	if(REVID_POS>=0) {
		                try {
		                	revId = Long.parseLong(groups[REVID_POS]);
		                } catch(NumberFormatException e) {
		                	log.error(e.toString());
		                	continue;
		                }
                	
                	Revision r = wiki.getRevision(revId);
                	String title = r.getPage();
                	ignore.add(title);
                	}
	            }
	        }		    
		}
		return ignore;
	}

}