/**
 *  @(#)BasicFetcher.java 13.07.2014
 *  Copyright � 2014 Dmitry Trofimovich (KIN)(DimaTrofimovich@gmail.com)
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.ServiceError;
import org.wikipedia.nirvana.WikiTools;

/**
 * @author kin
 *
 */
public abstract class BasicFetcher implements PageListFetcher {
	protected static org.apache.log4j.Logger log = null;
	protected WikiTools.Service service;
    protected List<String> categories;
	protected List<String> categoriesToIgnore;
	protected String language;
	protected int depth;
	protected int hours;
	protected int namespace;

    // Regex to check validity of line
    protected String LINE_RULE;
    // We will not check every line if output has thousands lines for better performance
    private final int LINES_TO_CHECK = 25;

	/**
	 * 
	 */
	public BasicFetcher(WikiTools.Service service, List<String> cats, List<String> ignore, String lang, int depth, int hours, int namespace) {
		this.service = service;
		this.categories = cats;
		this.categoriesToIgnore = ignore;
		this.language = lang;
		this.depth = depth;
		this.hours = hours;
		this.namespace = namespace;
		this.LINE_RULE = "^.+$";
		if (service.LINE_RULE != null) {
			this.LINE_RULE = service.LINE_RULE;
		}
		log = org.apache.log4j.Logger.getLogger(this.getClass().getName());
	}

	//public abstract void getNewPages(NirvanaWiki wiki, ArrayList<Revision> pageInfoList, HashSet<String> pages) throws IOException, InterruptedException;
	
	public void parsePageList(NirvanaWiki wiki, HashSet<String> pages, ArrayList<Revision> pageInfoList, HashSet<String> ignore, String pageList) throws IOException, ServiceError {
		String line;
		
		//FileTools.dump(pageList, "dump", "pageList_"+category+".txt");
		StringReader sr = new StringReader(pageList);
		BufferedReader b = new BufferedReader(sr);
		for(int j=0;j<service.SKIP_LINES;j++) b.readLine();
		Pattern p = Pattern.compile(LINE_RULE);
		
		int j = 0;
        while ((line = b.readLine()) != null)
        {
        	j++;
        	if (line.isEmpty()) continue;
        	if (j<LINES_TO_CHECK && !p.matcher(line).matches()) {
        		throw new ServiceError("Invalid output of service: "+service.getName());
        	}
            String[] groups = line.split("\t");
            int thisNS = 0; // articles by default
            if (!service.filteredByNamespace) {
            	try {
            		thisNS = Integer.parseInt(groups[service.NS_POS]);
            	} catch (NumberFormatException e) {
            		log.warn("invalid namespace detected", e);
            	}
            }
            // �� ��� �� ���� ��������� � ���, ��� �����
            if (service.filteredByNamespace || thisNS == namespace)
            {
                String title = groups[service.TITLE_POS].replace('_', ' ');
                if (ignore != null && ignore.contains(title))
                {
                    continue;
                }
                if (!service.hasSuffix && namespace != 0)
                {	                	
                    title = wiki.namespaceIdentifier(namespace) + ":" + title;	                	
                	log.debug("Namespace is not 0");
                }	                
                
                if (!pages.contains(title))
                {
                	long revId=0;
                	if(service.REVID_POS>=0) {
		                try {
		                	revId = Long.parseLong(groups[service.REVID_POS]);
		                } catch(NumberFormatException e) {
		                	log.error(e.toString());
		                	continue;
		                }
                	}
                	long id = 0;
                	if(service.ID_POS>=0) {
                		try {
		                	id = Long.parseLong(groups[service.ID_POS]);
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
            	// ����� �� ������������ ������, ����� ������ ������� �������� ����� ������ ������������
            	// � ����� ����������������� � �������� ������������
            	//String title = groups[TITLE_POS].replace('_', ' ');
            	long revId=0;
            	if(service.REVID_POS>=0) {
	                try {
	                	revId = Long.parseLong(groups[service.REVID_POS]);
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
                
                // ������ ����� �� ���� ���������, ������� � �.�. ����� ������� ������� ������
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
	@Override
	public boolean revisionAvailable() {		
		return (service.REVID_POS>=0);
	}
	
}
