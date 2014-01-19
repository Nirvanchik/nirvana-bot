/**
 *  @(#)NewPagesFetcherOBOCatScan.java 28.10.2013
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

import java.io.IOException;
import java.util.List;

import org.wikipedia.nirvana.WikiTools;

/**
 * @author kin
 *
 */
public class NewPagesFetcherOBOCatScan extends PageListFetcherOneByOne {

	/**
	 * @param cats
	 * @param ignore
	 * @param lang
	 * @param depth
	 * @param hours
	 * @param namespace
	 */
	public NewPagesFetcherOBOCatScan(List<String> cats, List<String> ignore,
	        String lang, int depth, int hours, int namespace) {
		super(cats, ignore, lang, depth, hours, namespace);
		SKIP_LINES = 0;
	    NS_POS = 0;
	    TITLE_POS = 1;
	    REVID_POS = 5;
	    ID_POS = 4;
	    filteredByNamespace = false;
	    hasSuffix = false;
	}

	/* (non-Javadoc)
	 * @see org.wikipedia.nirvana.nirvanabot.PageListFetcherOneByOne#getNewPagesForCat(java.lang.String, java.lang.String, int, int)
	 */
	@Override
	protected String getNewPagesForCat(String category, String language,
	        int depth, int hours) throws IOException, InterruptedException {		
		return WikiTools.loadNewPagesForCatWithCatScan(category, language, depth, hours);
	}

	public boolean revisionAvailable() {
		return true;
	}
}
