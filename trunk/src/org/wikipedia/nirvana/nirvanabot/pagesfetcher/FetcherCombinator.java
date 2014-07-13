/**
 *  @(#)FetcherCombinator.java 29.04.2014
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
import java.util.ArrayList;
import java.util.List;

import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.NirvanaWiki;

/**
 * @author kin
 *
 */
public class FetcherCombinator implements PageListFetcher {
	protected static org.apache.log4j.Logger log = null;
	private List<PageListFetcher> fetchers;

	/**
	 * @param cats
	 * @param ignore
	 * @param lang
	 * @param depth
	 * @param hours
	 * @param namespace
	 */
	public FetcherCombinator(List<PageListFetcher> fetchers) {
		this.fetchers = fetchers;
		log = org.apache.log4j.Logger.getLogger(this.getClass().getName());
	}

	/* (non-Javadoc)
	 * @see org.wikipedia.nirvana.nirvanabot.pagesfetcher.BasicFetcher#getNewPages(org.wikipedia.nirvana.NirvanaWiki)
	 */
	@Override
	public ArrayList<Revision> getNewPages(NirvanaWiki wiki)
	        throws IOException, InterruptedException {
		ArrayList<Revision> list;
		list = fetchers.get(0).getNewPages(wiki);
		for(int i=1;i<fetchers.size();i++) {
			log.debug("adding results from another fetcher: "+i);
			list.addAll(fetchers.get(i).getNewPages(wiki));
		}
		return list;
	}

	/* (non-Javadoc)
	 * @see org.wikipedia.nirvana.nirvanabot.pagesfetcher.BasicFetcher#revisionAvailable()
	 */
	@Override
	public boolean revisionAvailable() {		
		return fetchers.get(0).revisionAvailable();
	}

	/* (non-Javadoc)
     * @see org.wikipedia.nirvana.nirvanabot.pagesfetcher.PageListFetcher#mayHaveDuplicates()
     */
    @Override
    public boolean mayHaveDuplicates() {	    
	    return fetchers.size()>1;
    }

}
