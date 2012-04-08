/**
 *  @(#)WatchList.java 07/04/2012
 *  Copyright © 2011 - 2012 Dmitry Trofimovich (KIN)
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.security.auth.login.LoginException;

import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.NirvanaWiki;

/**
 * @author kin
 *
 */
public class WatchList extends NewPages {

	/**
	 * @param lang
	 * @param categories
	 * @param categoriesToIgnore
	 * @param usersToIgnore
	 * @param page
	 * @param archive
	 * @param ns
	 * @param depth
	 * @param hours
	 * @param maxItems
	 * @param format
	 * @param delimeter
	 * @param header
	 * @param footer
	 * @param minor
	 * @param bot
	 */
	public WatchList(String lang, ArrayList<String> categories,
			ArrayList<String> categoriesToIgnore,
			ArrayList<String> usersToIgnore, String page, String archive,
			int ns, int depth, int hours, int maxItems, String format,
			String delimeter, String header, String footer, boolean minor,
			boolean bot) {
		super(lang, categories, categoriesToIgnore, usersToIgnore, page,
				archive, null, ns, depth, hours, maxItems, format, delimeter, header,
				footer, minor, bot);
		GET_FIRST_REV = true;
		UPDATE_FROM_OLD = false;
		SKIP_LINES = 2;
	    NS_POS = 2;
	    TITLE_POS = 0;
	    REVID_POS = -1;
		
	}
	
	public String getData(Wiki wiki) throws IOException, InterruptedException {
		log.info("Getting data for [[" + this.pageName+"]]");
		pageLists = new HashMap<String,String>();
		for(String category : this.categories)
		{
		    String text = NirvanaWiki.loadPageList(category, language, depth);
		    pageLists.put(category, text);
		}
		pageListsToIgnore = new HashMap<String,String>();
		for(String category : this.categoriesToIgnore)
		{
		    String text = NirvanaWiki.loadPageList(category, language, depth);
		    pageListsToIgnore.put(category, text);
		}
		
		try
		{
		    return wiki.getPageText(this.pageName);
		} catch (java.io.FileNotFoundException e) {
			return "";
		}
	}
	
	
	public void sortPages(ArrayList<Revision> pageInfoList){
		
	}
	
	
	public void updateArchive(NirvanaWiki wiki, Data d, ReportItem reportData) throws LoginException, IOException {
		String str = "";
		if(archive!=null && !archive.isEmpty()) {
			str = archive+" is not updated";
		}
		log.info("updateArchive-> WatchList doesn't support archives, skip, "+str);

	}
}
