/**
 *  @(#)WatchList.java
 *  Copyright © 2011-2014 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.nirvanabot;

import java.util.ArrayList;

import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.WikiTools;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.BasicFetcher;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.PagesFetcherOBOCatScan2;

/**
 * @author kin
 *
 */
public class WatchList extends NewPages {

	/**
	 * @param param	 
	 */
	public WatchList(PortalParam param) {
		super(param);
		if(this.archiveSettings!=null) {
			this.archiveSettings = null;
		}
		//GET_FIRST_REV = true;
		getRevisionMethod = GetRevisionMethod.GET_FIRST_REV_IF_NEED;
		UPDATE_FROM_OLD = false;
		UPDATE_ARCHIVE = false;
				
	}
	
	@Override
	BasicFetcher createPageListFetcher() {
		return new PagesFetcherOBOCatScan2(WikiTools.Service.CATSCAN2, categories, categoriesToIgnore, language, depth, namespace);
	}
	
	@Override
	public void sortPages(ArrayList<Revision> pageInfoList, boolean byRevision) {
		// no sort (sorted by default)
		// In CATSCAN2 sort is disabled so we have to sort actually until it's fixed on catscan2
		sortPagesByName(pageInfoList);
	}	
	
}
