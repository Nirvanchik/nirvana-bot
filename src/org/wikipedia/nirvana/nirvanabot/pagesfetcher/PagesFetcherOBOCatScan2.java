/**
 *  @(#)PagesFetcherOBOCatScan2.java 13.07.2014
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
 * Recommended code page for this file is CP1251 (also called Windows-1251).
 * */

package org.wikipedia.nirvana.nirvanabot.pagesfetcher;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.wikipedia.Wiki;
import org.wikipedia.nirvana.WikiTools;

/**
 * @author kin
 *
 */
public class PagesFetcherOBOCatScan2 extends PageListFetcherOneByOne {

	/**
	 * @param cats
	 * @param ignore
	 * @param lang
	 * @param depth
	 * @param hours
	 * @param namespace
	 */
	public PagesFetcherOBOCatScan2(List<String> cats, List<String> ignore,
	        String lang, int depth, int namespace) {
		super(cats, ignore, lang, depth, 0, namespace);
		SKIP_LINES = 2;
	    NS_POS = 2;
	    TITLE_POS = 0;
	    REVID_POS = -1;
	    ID_POS = 1;
	}
	/* (non-Javadoc)
	 * @see org.wikipedia.nirvana.nirvanabot.PageListFetcherOneByOne#getNewPagesForCat(java.lang.String, java.lang.String, int, int)
	 */
	@Override
	protected String getNewPagesForCat(String category, String language,
	        int depth, int hours) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected String getPagesForCat(String category, String language,
	        int depth) throws IOException, InterruptedException {		
		return WikiTools.loadPagesForCatWithCatScan2(category, language, depth);
	}
	
	public void getData(Wiki wiki) throws IOException, InterruptedException {
		//log.info("Getting data for [[" + this.pageName+"]]");
		pageLists = new HashMap<String,String>();
		for(String category : this.categories)
		{
		    String text = getPagesForCat(category, language, depth);
		    pageLists.put(category, text);
		}
		pageListsToIgnore = new HashMap<String,String>();
		for(String category : this.categoriesToIgnore)
		{
		    String text = getPagesForCat(category, language, depth);
		    pageListsToIgnore.put(category, text);
		}
		
		return;
	}
	/* (non-Javadoc)
     * @see org.wikipedia.nirvana.nirvanabot.pagesfetcher.BasicFetcher#revisionAvailable()
     */
    @Override
    public boolean revisionAvailable() {	    
	    return false;
    }

}
