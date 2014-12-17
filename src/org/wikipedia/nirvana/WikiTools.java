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
	private static final String CAT_SEPARATOR = "\r\n";
	private static final String TOOLSERVER_DOMAIN = "toolserver.org";
	private static final String LABS_DOMAIN = "tools.wmflabs.org";
	private static final String CATSCAN_DOMAIN = TOOLSERVER_DOMAIN;
	private static final String CATSCAN_PATH = "/~daniel/WikiSense/CategoryIntersect.php";
	private static final String CATSCAN2_DOMAIN = LABS_DOMAIN;
	private static final String CATSCAN3_DOMAIN = LABS_DOMAIN;
	private static final String CATSCAN2_PATH = "/catscan2/catscan2.php";
	private static final String CATSCAN3_PATH = "/catscan3/catscan2.php";
	private static final String HTTP = "http";
	private static final int TIMEOUT_DELAY = 10000; // 10 sec
	
	public static class ServiceFeatures {
    	public static final int PAGES = 0b1;
    	public static final int NEWPAGES = 0b10;
    	public static final int FAST_MODE = 0b100;
    	public static final int PAGES_WITH_TEMPLATE = 0b1000;
    	public static final int CATSCAN_FEATURES = NEWPAGES;
    	public static final int CATSCAN2_FEATURES = PAGES|NEWPAGES|FAST_MODE;
    	public static final int CATSCAN3_FEATURES = PAGES|NEWPAGES|FAST_MODE|PAGES_WITH_TEMPLATE;
	}
	
	public enum EnumerationType {
		AND,
		OR,
		NONE
	}
	
	public enum Service {
		CATSCAN ("catscan", CATSCAN_DOMAIN, CATSCAN_PATH, 
				0, 0, 1, 5, 4, false, false, true, 720,
				ServiceFeatures.CATSCAN_FEATURES,
				null,
				null,
				null,
				null,
				"wikilang=%1$s&wikifam=.wikipedia.org&basecat=%2$s&basedeep=%3$d&mode=rc&hours=%4$d&onlynew=on&go=Сканировать&format=csv&userlang=ru",
				null,
				null),
		CATSCAN2 ("catscan2", CATSCAN2_DOMAIN, CATSCAN2_PATH, 
				2, 2, 0, -1, 1, true, false, false, 8928, ////	8760 = 1 year = 24*31*12 = 8928;
				ServiceFeatures.CATSCAN2_FEATURES,
				"language=%1$s&depth=%2$d&categories=%3$s&ns[%4$d]=1&sortby=title&format=tsv&doit=submit",
				"language=%1$s&depth=%2$d&categories=%3$s&negcats=%4$s&ns[%5$d]=1&comb[union]=1&sortby=title&format=tsv&doit=submit",
				null,
				null,
				"language=%1$s&depth=%2$d&categories=%3$s&ns[%5$d]=1&max_age=%4$d&only_new=1&sortby=title&format=tsv&doit=submit",
				"language=%1$s&depth=%2$d&categories=%3$s&negcats=%4$s&ns[%6$d]=1&comb[union]=1&max_age=%5$d&only_new=1&sortby=title&format=tsv&doit=1",
				"^\\S+\\s+\\d+\\s+\\d+\\s+\\d+\\s+\\S+\\s+\\S+$"),
		CATSCAN3 ("catscan3", CATSCAN3_DOMAIN, CATSCAN3_PATH, 
				2, 2, 0, -1, 1, true, false, false, 8928, ////	8760 = 1 year = 24*31*12 = 8928;
				ServiceFeatures.CATSCAN3_FEATURES,
				"language=%1$s&depth=%2$d&categories=%3$s&ns[%4$d]=1&sortby=title&format=tsv&doit=submit",
				"language=%1$s&depth=%2$d&categories=%3$s&negcats=%4$s&ns[%5$d]=1&comb[union]=1&sortby=title&format=tsv&doit=submit",
				"language=%1$s&depth=%2$d&categories=%3$s&%4$s=%5$s&ns[%6$d]=1&sortby=title&format=tsv&doit=submit",
				"language=%1$s&depth=%2$d&categories=%3$s&negcats=%4$s&%5$s=%6$s&ns[%7$d]=1&comb[union]=1&sortby=title&format=tsv&doit=submit",
				"language=%1$s&depth=%2$d&categories=%3$s&ns[%5$d]=1&max_age=%4$d&only_new=1&sortby=title&format=tsv&doit=submit",
				"language=%1$s&depth=%2$d&categories=%3$s&negcats=%4$s&ns[%6$d]=1&comb[union]=1&max_age=%5$d&only_new=1&sortby=title&format=tsv&doit=1",
				"^\\S+\\s+\\d+\\s+\\d+\\s+\\d+\\s+\\S+\\s+\\d+\\s+\\S+$");
		private final String name;
		public final String DOMAIN;
		public final String PATH;
		public final int SKIP_LINES;
		public final int NS_POS;
		public final int TITLE_POS;
		public final int REVID_POS;
		public final int ID_POS;
		public final boolean filteredByNamespace;
		public final boolean hasSuffix;
		public final boolean hasDeleted;
		public final int MAX_HOURS;
		public final int FEATURES;
		public final String GET_PAGES_FORMAT;
		public final String GET_PAGES_FORMAT_FAST;
		public final String GET_PAGES_WITH_TEMPLATE_FORMAT;
		public final String GET_PAGES_WITH_TEMPLATE_FORMAT_FAST;
		public final String GET_NEW_PAGES_FORMAT;
		public final String GET_NEW_PAGES_FORMAT_FAST;
		public final String LINE_RULE;

		Service(String name, String domain, String path, 
				int skipLines, int namespacePos, int titlePos, int revidPos, int idPos,
				boolean filteredByNamespace, boolean hasSuffix, boolean hasDeleted, int maxHours, 
				int features,
				String getPagesFormat,
				String getPagesFormatFast,
				String getPagesWithTemplateFormat,
				String getPagesWithTemplateFormatFast,
				String getNewPagesFormat,
				String getNewPagesFormatFast,
				String lineRule) {
			this.name = name;
			this.DOMAIN = domain;
			this.PATH = path;
			this.SKIP_LINES = skipLines;
			this.NS_POS = namespacePos;
			this.TITLE_POS = titlePos;
			this.REVID_POS = revidPos;
			this.ID_POS = idPos;
			this.filteredByNamespace = filteredByNamespace;
			this.hasSuffix = hasSuffix;
			this.hasDeleted = hasDeleted;
			this.MAX_HOURS = maxHours;
			this.FEATURES = features;
			this.GET_PAGES_FORMAT = getPagesFormat;
			this.GET_PAGES_FORMAT_FAST = getPagesFormatFast;
			this.GET_PAGES_WITH_TEMPLATE_FORMAT = getPagesWithTemplateFormat;
			this.GET_PAGES_WITH_TEMPLATE_FORMAT_FAST = getPagesWithTemplateFormatFast;
			this.GET_NEW_PAGES_FORMAT = getNewPagesFormat;
			this.GET_NEW_PAGES_FORMAT_FAST = getNewPagesFormatFast;
			this.LINE_RULE = lineRule;
		}
		public String getName() { return name; }
		public boolean supportsPages() { return supportsFeature (ServiceFeatures.PAGES); }
		public boolean supportsNewPages() { return supportsFeature (ServiceFeatures.NEWPAGES); }
		public boolean supportsFastMode() { return supportsFeature (ServiceFeatures.FAST_MODE); }
		public boolean supportsFeature(int feature) { return ((FEATURES & feature) != 0); }
		
		public static Service getServiceByName(String name) {
			Service service = null;
			if (name.equals(CATSCAN.getName())) return CATSCAN;
			if (name.equals(CATSCAN2.getName())) return CATSCAN2;
			if (name.equals(CATSCAN3.getName())) return CATSCAN3;
			return service;
		}
		
		public static Service getServiceByName(String name, Service defaultService) {
			if (name.equals(CATSCAN.getName())) return CATSCAN;
			if (name.equals(CATSCAN2.getName())) return CATSCAN2;
			if (name.equals(CATSCAN3.getName())) return CATSCAN3;
			return defaultService;
		}
		
		public static Service getDefaultServiceForFeature(int feature) {
			switch (feature) {
				case ServiceFeatures.PAGES_WITH_TEMPLATE: return Service.CATSCAN3;
				default: return Service.CATSCAN2;
			}
		}
	}

	public static String loadPagesForCatWithService(Service service, 
			String category, String language, int depth, int namespace) throws IOException, InterruptedException
    {
		if (!service.supportsPages()) {
			service = Service.getDefaultServiceForFeature(ServiceFeatures.PAGES);
		}
		log.debug("Downloading data for " + category);
        String url_query = 
        		String.format(service.GET_PAGES_FORMAT,
                language,
                depth,
                category,
                namespace);
        return fetchQueryWithTimeoutRetry(service, url_query);   
    }
	
	public static String loadPagesForCatListAndIgnoreWithService(Service service, 
			List<String> cats, List<String> ignore, String language, int depth, int namespace) throws IOException, InterruptedException
    {
		if (!service.supportsPages()) {
			service = Service.getDefaultServiceForFeature(ServiceFeatures.PAGES);
		}
		log.debug("Downloading data for categories: " + Arrays.toString(cats.toArray()));
        log.debug("and ignore categories: " + Arrays.toString(ignore.toArray()));
        String url_query = 
        		String.format(service.GET_PAGES_FORMAT_FAST,
                language,
                depth,
                StringUtils.join(cats, CAT_SEPARATOR),
                StringUtils.join(ignore, CAT_SEPARATOR),
                namespace);
        return fetchQueryWithTimeoutRetry(service, url_query);   
    }
	
	public static String loadPagesWithTemplatesForCatWithService(Service service, 
			String category, String language, int depth, 
			List<String> templates, EnumerationType enumType, int namespace) throws IOException, InterruptedException
    {
		if (!service.supportsPages()) {
			service = Service.getDefaultServiceForFeature(ServiceFeatures.PAGES_WITH_TEMPLATE);
		}
		log.debug("Downloading data for " + category);
		String templatesParam = "templates_yes";
		switch (enumType) {
    		case AND: templatesParam = "templates_yes"; break;
    		case OR: templatesParam = "templates_any"; break;
    		case NONE: templatesParam = "templates_no"; break;
		}
        String url_query = 
        		String.format(service.GET_PAGES_WITH_TEMPLATE_FORMAT,
                language,
                depth,
                category,
                templatesParam,
                StringUtils.join(templates, CAT_SEPARATOR),
                namespace);
        return fetchQueryWithTimeoutRetry(service, url_query);
    }
	
	public static String loadPagesWithTemplatesForCatListAndIgnoreWithService(Service service, 
			List<String> cats, List<String> ignore, String language, int depth, 
			List<String> templates, EnumerationType enumType, int namespace) throws IOException, InterruptedException
    {
		if (!service.supportsPages()) {
			service = Service.getDefaultServiceForFeature(ServiceFeatures.PAGES_WITH_TEMPLATE);
		}
		log.debug("Downloading data for categories: " + Arrays.toString(cats.toArray()));
        log.debug("and ignore categories: " + Arrays.toString(ignore.toArray()));
		String templatesParam = "templates_yes";
		switch (enumType) {
    		case AND: templatesParam = "templates_yes"; break;
    		case OR: templatesParam = "templates_any"; break;
    		case NONE: templatesParam = "templates_no"; break;
		}
        String url_query = 
        		String.format(service.GET_PAGES_WITH_TEMPLATE_FORMAT_FAST,
                language,
                depth,
                StringUtils.join(cats, CAT_SEPARATOR),
                StringUtils.join(ignore, CAT_SEPARATOR),
                templatesParam,
                StringUtils.join(templates, CAT_SEPARATOR),
                namespace);
        return fetchQueryWithTimeoutRetry(service, url_query);
    }
	
	public static String loadNewPagesForCatWithService(Service service, String category, String language, int depth, int hours, int namespace) throws IOException, InterruptedException {
		log.debug("Downloading data for category " + category);		
        String url_query;
        if (service.filteredByNamespace) {
        	url_query = String.format(service.GET_NEW_PAGES_FORMAT,
                    language,
                    depth,
                    category,
                    hours,
                    namespace);
        } else {
        	url_query = 
            		String.format(service.GET_NEW_PAGES_FORMAT,
                    language,
                    depth,
                    category,
                    hours);
        }
        return fetchQueryWithTimeoutRetry(service, url_query);
	}
	
	public static String loadNewPagesForCatListAndIgnoreWithService(Service service, List<String> cats, List<String> ignore, String language, int depth, int hours, int namespace) throws IOException, InterruptedException
    {
        log.debug("Downloading data for categories: " + Arrays.toString(cats.toArray()));
        log.debug("and ignore categories: " + Arrays.toString(ignore.toArray()));
        String url_query = 
        		String.format(service.GET_NEW_PAGES_FORMAT_FAST,
                language,
                depth,
                StringUtils.join(cats, CAT_SEPARATOR),
                StringUtils.join(ignore, CAT_SEPARATOR),
                hours,
                namespace
                );        
		return fetchQueryWithTimeoutRetry(service, url_query);
    }
	
	private static String fetchQueryWithTimeoutRetry(Service service, String query) throws IOException, InterruptedException {
		URI uri = null;
		try {
			uri = new URI(HTTP, service.DOMAIN, service.PATH, query, null);
		} catch (URISyntaxException e) {			
			log.error(e.toString());
			return null;
		}
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
