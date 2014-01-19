/**
 *  @(#)WikiTools.java 26.10.2013
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
	private static final String CATSCAN_PATH = "/~daniel/WikiSense/CategoryIntersect.php";
	private static final String CATSCAN2_DOMAIN = LABS_DOMAIN;
	private static final String CATSCAN2_PATH = "/catscan2/catscan2.php";
	private static final String HTTP = "http";
	private static final int TIMEOUT_DELAY = 10000; // 10 sec

	public static String loadNewPagesForCatWithCatScan(String category, String language, int depth, int hours) throws IOException, InterruptedException
    {
		log.debug("Downloading data for category " + category);		
        String url_query = 
        		String.format("wikilang=%1$s&wikifam=.wikipedia.org&basecat=%2$s&basedeep=%3$d&mode=rc&hours=%4$d&onlynew=on&go=Сканировать&format=csv&userlang=ru",
                language,
                category,
                depth,
                hours);
        URI uri=null;
		try {
			uri = new URI(HTTP,CATSCAN_DOMAIN,CATSCAN_PATH,url_query,null);
		} catch (URISyntaxException e) {			
			log.error(e.toString());
			return null;
		}
		return fetchUriWithTimeoutRetry(uri);   
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
	
	public static String loadNewPagesForCatWithCatScan2(String category, String language, int depth, int hours, int namespace) throws IOException, InterruptedException
    {
        log.debug("Downloading data for " + category);
        
        String url_query = 
        		String.format("language=%1$s&depth=%2$d&categories=%3$s&ns[%4$d]=1&max_age=%5$d&only_new=1&sortby=title&format=tsv&doit=submit",
                language,
                depth,
                category,
                namespace,
                hours);
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
        log.debug("Downloading data for categories: " + Arrays.toString(cats.toArray()));
        log.debug("and ignore categories: " + Arrays.toString(ignore.toArray()));
        String url_query = 
        		String.format("language=%1$s&depth=%2$d&categories=%3$s&negcats=%4$s&ns[%5$d]=1&comb[union]=1&max_age=%6$d&only_new=1&sortby=title&format=tsv&doit=1",
                language,
                depth,
                StringUtils.join(cats, "\r\n"),
                StringUtils.join(ignore, "\r\n"),
                namespace,
                hours);
        URI uri=null;
		try {
			uri = new URI(HTTP,CATSCAN2_DOMAIN,CATSCAN2_PATH,url_query,null);
		} catch (URISyntaxException e) {			
			log.error(e.toString());
			return null;
		}
		return fetchUriWithTimeoutRetry(uri);
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
