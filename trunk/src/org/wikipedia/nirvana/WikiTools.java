/**
 *  @(#)WikiTools.java 
 *  Copyright © 2013-2014 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * @author kin
 *
 */
public class WikiTools {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WikiTools.class.getName());
	private static final String TOOLSERVER_DOMAIN = "toolserver.org";
	private static final String LABS_DOMAIN = "tools.wmflabs.org";
	private static final String CATSCAN_DOMAIN = TOOLSERVER_DOMAIN;
//	private static final String CATSCAN_PATH = "/~daniel/WikiSense/CategoryIntersect.php";
	private static final String CATSCAN2_DOMAIN = LABS_DOMAIN;
	private static final String CATSCAN2_PATH = "/catscan2/catscan2.php";
	private static final String HTTP = "http";
	private static final int TIMEOUT_DELAY = 10000; // 10 sec
	
	public enum Service {
		CATSCAN ("catscan", CATSCAN_DOMAIN, "/~daniel/WikiSense/CategoryIntersect.php", 
				true, true, false, 0, 0, 1, 5, 4, false, false, true, 720,
				"wikilang=%1$s&wikifam=.wikipedia.org&basecat=%2$s&basedeep=%3$d&mode=rc&hours=%4$d&onlynew=on&go=Сканировать&format=csv&userlang=ru",
				null,
				null),
		CATSCAN2 ("catscan2", CATSCAN2_DOMAIN, "/catscan2/catscan2.php", 
				true, true, true, 2, 2, 0, -1, 1, true, false, false, 8928, ////	8760 = 1 year = 24*31*12 = 8928;
				"language=%1$s&depth=%3$d&categories=%2$s&ns[%5$d]=1&max_age=%4$d&only_new=1&sortby=title&format=tsv&doit=submit",
				"language=%1$s&depth=%2$d&categories=%3$s&negcats=%4$s&ns[%6$d]=1&comb[union]=1&max_age=%5$d&only_new=1&sortby=title&format=tsv&doit=1",
				"^\\S+\\s+\\d+\\s+\\d+\\s+\\d+\\s+\\S+\\s+\\S+$"); 

		private final String name;
		public final String DOMAIN;
		public final String PATH;
		private final boolean pages;
		private final boolean newPages;
		private final boolean newPagesManyCats;
		public final int SKIP_LINES;
		public final int NS_POS;
		public final int TITLE_POS;
		public final int REVID_POS;
		public final int ID_POS;
		public final boolean filteredByNamespace;
		public final boolean hasSuffix;
		public final boolean hasDeleted;
		public final int MAX_HOURS;
		public final String GET_NEW_PAGES_FORMAT;
		public final String GET_NEW_PAGES_FORMAT_MANY_CATS;
		public final String LINE_RULE;

		Service(String name, String domain, String path, boolean pages, boolean newPages, boolean newPagesManyCats,
				int skipLines, int namespacePos, int titlePos, int revidPos, int idPos,
				boolean filteredByNamespace, boolean hasSuffix, boolean hasDeleted, int maxHours, 
				String getNewPagesFormat,
				String getNewPagesFormatManyCats,
				String lineRule) {
			this.name = name;
			this.DOMAIN = domain;
			this.PATH = path;
			this.pages = pages;
			this.newPages = newPages;
			this.newPagesManyCats = newPagesManyCats;
			this.SKIP_LINES = skipLines;
			this.NS_POS = namespacePos;
			this.TITLE_POS = titlePos;
			this.REVID_POS = revidPos;
			this.ID_POS = idPos;
			this.filteredByNamespace = filteredByNamespace;
			this.hasSuffix = hasSuffix;
			this.hasDeleted = hasDeleted;
			this.MAX_HOURS = maxHours;
			this.GET_NEW_PAGES_FORMAT = getNewPagesFormat;
			this.GET_NEW_PAGES_FORMAT_MANY_CATS = getNewPagesFormatManyCats;
			this.LINE_RULE = lineRule;
		}
		public String getName() { return name; }
		public boolean supportsPages() { return pages; }
		public boolean supportsNewPages() { return newPages; }
		public boolean supportsNewPagesManyCats() { return newPagesManyCats; }
		
		public static Service getServiceByName(String name) {
			Service service = null;
			if (name.equals(CATSCAN.getName())) return CATSCAN;
			if (name.equals(CATSCAN2.getName())) return CATSCAN2;
			return service;
		}
		
		public static Service getServiceByName(String name, Service defaultService) {
			if (name.equals(CATSCAN.getName())) return CATSCAN;
			if (name.equals(CATSCAN2.getName())) return CATSCAN2;
			return defaultService;
		}
	}
	
	public static String loadNewPagesForCatWithService(Service service, String category, String language, int depth, int hours, int namespace) throws IOException, InterruptedException {
		log.debug("Downloading data for category " + category);		
        String url_query;
        if (service.filteredByNamespace) {
        	url_query = String.format(service.GET_NEW_PAGES_FORMAT,
                    language,
                    category,
                    depth,
                    hours,
                    namespace);
        } else {
        	url_query = 
            		String.format(service.GET_NEW_PAGES_FORMAT,
                    language,
                    category,
                    depth,
                    hours);
        }
        URI uri = null;
		try {
			uri = new URI(HTTP,service.DOMAIN,service.PATH,url_query,null);
		} catch (URISyntaxException e) {			
			log.error(e.toString());
			return null;
		}
		return fetchUriWithTimeoutRetry(uri);   
	}
	
	public static String loadNewPagesForCatListAndIgnoreWithService(Service service, List<String> cats, List<String> ignore, String language, int depth, int hours, int namespace) throws IOException, InterruptedException
    {
        log.debug("Downloading data for categories: " + Arrays.toString(cats.toArray()));
        log.debug("and ignore categories: " + Arrays.toString(ignore.toArray()));
        String url_query = 
        		String.format(service.GET_NEW_PAGES_FORMAT_MANY_CATS,
                language,
                depth,
                StringUtils.join(cats, "\r\n"),
                StringUtils.join(ignore, "\r\n"),
                hours,
                namespace
                );
        URI uri=null;
		try {
			uri = new URI(HTTP,service.DOMAIN,service.PATH,url_query,null);
		} catch (URISyntaxException e) {			
			log.error(e.toString());
			return null;
		}
		return fetchUriWithTimeoutRetry(uri);
    }

	public static String loadNewPagesForCatWithCatScan(String category, String language, int depth, int hours) throws IOException, InterruptedException
    {
		return loadNewPagesForCatWithService(Service.CATSCAN, category, language, depth, hours, 0);
    }
	public static String loadNewPagesForCatWithCatScan2(String category, String language, int depth, int hours, int namespace) throws IOException, InterruptedException
    {        
		return loadNewPagesForCatWithService(Service.CATSCAN2, category, language, depth, hours, namespace);
    }
	
	public static String loadPagesForCatWithCatScan2(String category, String language, int depth) throws IOException, InterruptedException
    {
		log.debug("Downloading data for " + category);
        String url_query = 
        		String.format("language=%1$s&depth=%3$d&categories=%2$s&sortby=title&format=tsv&doit=submit",
                language,
                category,
                depth);
        URI uri=null;
		try {
			uri = new URI(HTTP,CATSCAN2_DOMAIN,CATSCAN2_PATH,url_query,null);
		} catch (URISyntaxException e) {			
			log.error(e.toString());
			return null;
		}
		return fetchUriWithTimeoutRetry(uri);    
    }
	
	
	public static String loadNewPagesForCatListAndIgnoreWithCatScan2(List<String> cats, List<String> ignore, String language, int depth, int hours, int namespace) throws IOException, InterruptedException
    {        
		return loadNewPagesForCatListAndIgnoreWithService(Service.CATSCAN2, cats, ignore, language, depth, hours, namespace);
    }
	
	private static String fetchUriWithTimeoutRetry(URI uri) throws IOException, InterruptedException {
		String page = null;
		try {
			page = HTTPTools.fetch(uri.toASCIIString(), true, true);
		} catch (java.net.SocketTimeoutException e) {
			log.warn(e.toString()+", retry again ...");
			Thread.sleep(TIMEOUT_DELAY);
			page = HTTPTools.fetch(uri.toASCIIString(), true, true);
		}
		return page;  
	}
}
