/**
 *  @(#)PageListProcessorFast.java 26.11.2014
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.error.ServiceError;
import org.wikipedia.nirvana.wiki.NirvanaWiki;
import org.wikipedia.nirvana.wiki.CatScanTools.Service;

/**
 * @author kin
 *
 */
public class PageListProcessorFast extends BasicProcessor {

	/**
	 * @param service
	 * @param cats
	 * @param ignore
	 * @param lang
	 * @param depth
	 * @param namespace
	 */
	public PageListProcessorFast(Service service, List<String> cats,
	        List<String> ignore, String lang, int depth, int namespace,
	        PageListFetcher fetcher) {
		super(service, cats, ignore, lang, depth, namespace, fetcher);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.wikipedia.nirvana.nirvanabot.pagesfetcher.PageListProcessor#getNewPages(org.wikipedia.nirvana.NirvanaWiki)
	 */
	@Override
	public ArrayList<Revision> getNewPages(NirvanaWiki wiki)
	        throws IOException, InterruptedException, ServiceError {
		ArrayList<Revision> pageInfoList = new ArrayList<Revision>(50);
		HashSet<String> pages = new HashSet<String>();
		String text = fetcher.loadNewPagesForCatListAndIgnore(service, categories, categoriesToIgnore, language, depth, namespace);
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
