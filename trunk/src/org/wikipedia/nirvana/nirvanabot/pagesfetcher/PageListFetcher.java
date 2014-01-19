/**
 *  @(#)PageListFetcher.java 27.10.2013
 *  Copyright © 2013 Dmitry Trofimovich (KIN)(DimaTrofimovich@gmail.com)
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
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.NirvanaWiki;

/**
 * @author kin
 *
 */
public abstract class PageListFetcher {
	protected static org.apache.log4j.Logger log = null;
	protected List<String> categories;
	protected List<String> categoriesToIgnore;
	protected String language;
	protected int depth;
	protected int hours;
	protected int namespace;

	protected int SKIP_LINES;
    protected int NS_POS;
    protected int TITLE_POS;
    protected int REVID_POS;
    protected int ID_POS;
    
    protected boolean filteredByNamespace;
    protected boolean hasSuffix;

	/**
	 * 
	 */
	public PageListFetcher(List<String> cats, List<String> ignore, String lang, int depth, int hours, int namespace) {
		this.categories = cats;
		this.categoriesToIgnore = ignore;
		this.language = lang;
		this.depth = depth;
		this.hours = hours;
		this.namespace = namespace;
		log = org.apache.log4j.Logger.getLogger(this.getClass().getName());
	}

	//public abstract void getNewPages(NirvanaWiki wiki, ArrayList<Revision> pageInfoList, HashSet<String> pages) throws IOException, InterruptedException;
	
	public abstract ArrayList<Revision> getNewPages(NirvanaWiki wiki) throws IOException, InterruptedException;
	
	public abstract boolean revisionAvailable();
	
	public void parsePageList(NirvanaWiki wiki, HashSet<String> pages, ArrayList<Revision> pageInfoList, HashSet<String> ignore, String pageList) throws IOException {
		String line;
		
		//FileTools.dump(pageList, "dump", "pageList_"+category+".txt");
		StringReader sr = new StringReader(pageList);
		BufferedReader b = new BufferedReader(sr);
		for(int j=0;j<SKIP_LINES;j++) b.readLine();
        while ((line = b.readLine()) != null)
        {
            String[] groups = line.split("\t");
            int thisNS = 0; // articles by default
            if (!filteredByNamespace) {
            	try {
            		thisNS = Integer.parseInt(groups[NS_POS]);
            	} catch (NumberFormatException e) {
            		log.warn("invalid namespace detected", e);
            	}
            }
            // то что мы ищем совпадает с тем, что нашли
            if (filteredByNamespace || thisNS == namespace)
            {
                String title = groups[TITLE_POS].replace('_', ' ');
                if (ignore != null && ignore.contains(title))
                {
                    continue;
                }
                if (!hasSuffix && namespace != 0)
                {	                	
                    title = wiki.namespaceIdentifier(namespace) + ":" + title;	                	
                	log.debug("Namespace is not 0");
                }	                
                
                if (!pages.contains(title))
                {
                	long revId=0;
                	if(REVID_POS>=0) {
		                try {
		                	revId = Long.parseLong(groups[REVID_POS]);
		                } catch(NumberFormatException e) {
		                	log.error(e.toString());
		                	continue;
		                }
                	}
                	long id = 0;
                	if(ID_POS>=0) {
                		try {
		                	id = Long.parseLong(groups[ID_POS]);
		                } catch(NumberFormatException e) {
		                	log.error(e.toString());
		                	continue;
		                }
                	}
                	RevisionWithId page = new RevisionWithId(wiki, revId, Calendar.getInstance(), title, "", "", false, false, true, 0, id);
                    pages.add(title);
                    log.debug("adding page to list:"+title);
                    pageInfoList.add(page);
                }
            } else if(thisNS == Wiki.USER_NAMESPACE &&
            		namespace!= Wiki.USER_NAMESPACE) {
            	// Здесь мы обрабатываем случаи, когда статьи сначала проходят через личное пространство
            	// а потом переименовываются в основное пространство
            	//String title = groups[TITLE_POS].replace('_', ' ');
            	long revId=0;
            	if(REVID_POS>=0) {
	                try {
	                	revId = Long.parseLong(groups[REVID_POS]);
	                } catch(NumberFormatException e) {
	                	log.error(e.toString());
	                	continue;
	                }
            	}
            	Revision r = wiki.getRevision(revId);
            	String title = r.getPage();
                if (ignore.contains(title))
                {
                    continue;
                }	                
                
                /*if(namespace!= Wiki.USER_NAMESPACE && userNamespace(title))
                	continue;*/
                
                // Случай когда мы ищем категории, шаблоны и т.д. чтобы отсеять обычные статьи
                int n = wiki.namespace(title);
                if(n!=namespace) {
                	continue;
                }
                /*
                if (namespace != 0 && !title.startsWith(wiki.namespaceIdentifier(namespace)))
                {	
                	log.debug("Namespace is other than we seek");
                	continue;	                	
                } //else if (namespace==0 && wiki.n)
                */
                
                if (!pages.contains(title))
                {   
                	pages.add(title);
                    log.debug("adding page to list:"+title);
                    pageInfoList.add(r);
                }
            }
        }//while
	}
	
}
