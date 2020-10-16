/**
 *  @(#)ArchiveTools.java 11.10.2014
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

package org.wikipedia.nirvana.archive;

import org.wikipedia.Wiki;
import org.wikipedia.nirvana.util.FileTools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.FailedLoginException;

/**
 * @author kin
 *
 */
public class ArchiveTools {
	public static final int PAGE_COUNT_PER_REQUEST = 20;
	private static Wiki wiki;
	private static final Logger log = Logger.getLogger("ArchiveTools");
	/**
	 * 
	 */
	public ArchiveTools() {
		// TODO Auto-generated constructor stub
	}
	
	public static class Page{
		String name;
		long id;
		public Page(String name, long id) {
			this.name = name;
			this.id = id;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) return;
		boolean asc = false;
		ArrayList<String> files = new ArrayList<String>();
		for (String arg:args) {
			if(arg.startsWith("-")) {
				if (arg.equals("-asc"))
					asc = true;
			} else {
				files.add(arg);
			}
		}
		final boolean sort_order_ascending = asc;
		if (files.size() == 0) {
			log.log(Level.SEVERE, "Please provide file name to read");
        	return;
		}
		String in = files.get(0);
		
		log.log(Level.INFO, "processing file: "+in);
		String out = "out.txt";
        List<String> pages;
        try {
            pages = FileTools.readFileToList(args[0], FileTools.UTF8, true);
        } catch (FileNotFoundException e1) {
            log.severe("File not found: " + args[0]);
            return;
        } catch (IOException e1) {
            log.severe("Failed to read file: " + args[0]);
            return;
        }
		wiki = new Wiki( "ru.wikipedia.org" );
		wiki.setMaxLag( 15 );
		//print( "db lag (seconds): " + wiki.getCurrentDatabaseLag() );

		// wiki.setThrottle( 5000 );
        // TODO: Remove it out of here.
		char pw[] = {'y','e','g','h','s','q','1'};
		try {
	        wiki.login( "NirvanaBot", pw );
        } catch (FailedLoginException e) {
        	log.log(Level.SEVERE, ""+e);
        	return;
        } catch (IOException e) {
        	log.log(Level.SEVERE, ""+e);
        	return;
        }

		int i = 0;
		Map results[];
		//Page pagesWithInfo[] = new Page[pages.length];
		ArrayList<Page> pagesWithInfo = new ArrayList<Page>(100000);
        while (i < pages.size()) {
            log.info("processing line: " + i + " of " + pages.size());
			int len = PAGE_COUNT_PER_REQUEST;
            if (i + len > pages.size()) {
                len = pages.size() - i;
			}
            List<String> part = pages.subList(i, i + len);
			try {
                results = wiki.getPageInfo(pages.toArray(new String[0]));
            } catch (IOException e) {
            	log.log(Level.SEVERE, ""+e);
	            wiki.logout();
	            return;
            }
			for (Map info:results) {
				long pageId = (Long)info.get("pageid");
				String pageName = (String) info.get("displaytitle");
				//if (pageId>0) {
				pagesWithInfo.add(new Page(pageName,pageId));
				//}
			}
			i+=len;
		}
		wiki.logout();
		java.util.Collections.sort(pagesWithInfo, new Comparator<Page>() {

			@Override
            public int compare(Page p1, Page p2) {
	            return (int)(sort_order_ascending?p1.id - p2.id:p2.id - p1.id);
            }
		});
		String text;
		//Arrays.sort(pagesWithInfo, fromIndex, toIndex, c)
		//StringBuffer buf = new StringBuffer();
		StringBuilder bld = new StringBuilder();
		for(Page p:pagesWithInfo) {
			bld.append(p.name);
			bld.append("\r\n");
		}
		text = bld.toString();
		try {
	        FileTools.writeFile(text, out, FileTools.UTF8);
        } catch (IOException e) {
        	log.log(Level.SEVERE, ""+e);            
            return;
        }		
		
	}

}
