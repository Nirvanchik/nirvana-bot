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
 * This file is encoded with UTF-8.
 * */

package org.wikipedia.nirvana;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wikipedia.nirvana.parser.format.Format;

import static org.wikipedia.nirvana.parser.format.TabularFormat.*;

/**
 * @author kin
 *
 */
public class WikiTools {
    private static final Logger log;
	private static final String CAT_SEPARATOR = "\r\n";
	private static final String TOOLSERVER_DOMAIN = "toolserver.org";
	private static final String LABS_DOMAIN = "tools.wmflabs.org";
	private static final String CATSCAN_DOMAIN = TOOLSERVER_DOMAIN;
	private static final String CATSCAN_PATH = "/~daniel/WikiSense/CategoryIntersect.php";
	private static final String CATSCAN2_DOMAIN = LABS_DOMAIN;
	private static final String CATSCAN3_DOMAIN = LABS_DOMAIN;
	private static final String CATSCAN2_PATH = "/catscan2/catscan2.php";
	private static final String CATSCAN3_PATH = "/catscan3/catscan2.php";
    private static final String PETSCAN_DOMAIN = "petscan.wmflabs.org";
    private static final String PETSCAN_PATH = "/";
	public static final String HTTP = "http";
	private static final int TIMEOUT_DELAY = 10000; // 10 sec

	private static boolean fastMode = false;

    private static boolean testMode = false;
    private static List<String> mockedResponses = null;
    private static List<String> savedQueries = null;

    private static final String ERR_SERVICE_DOESNT_SUPPORT_FEATURE =
            "Service %s doesn't support this feature.";

    static {
        log = LogManager.getLogger(WikiTools.class.getName());
    }

	public static class ServiceFeatures {
    	public static final int PAGES = 0b1;
    	public static final int NEWPAGES = 0b10;
    	public static final int FAST_MODE = 0b100;
    	public static final int PAGES_WITH_TEMPLATE = 0b1000;
        public static final int NEWPAGES_WITH_TEMPLATE = 0b10000;
    	@Deprecated
    	public static final int CATSCAN_FEATURES = NEWPAGES;
        @Deprecated
    	public static final int CATSCAN2_FEATURES = PAGES|NEWPAGES|FAST_MODE|PAGES_WITH_TEMPLATE;
    	public static final int CATSCAN3_FEATURES = PAGES|NEWPAGES|FAST_MODE|PAGES_WITH_TEMPLATE;
        public static final int PETSCAN_FEATURES =
                PAGES|NEWPAGES|FAST_MODE|PAGES_WITH_TEMPLATE|NEWPAGES_WITH_TEMPLATE;
	}

	public enum EnumerationType {
		AND,
		OR,
		NONE
	}

	public enum Service {
		@Deprecated
		CATSCAN ("catscan", CATSCAN_DOMAIN, CATSCAN_PATH,
				CSV_CATSCAN_FORMAT, false, false, true, 720,
				ServiceFeatures.CATSCAN_FEATURES,
				null,
				null,
				null,
				null,
                "wikilang=%1$s&wikifam=.wikipedia.org&basecat=%2$s&basedeep=%3$d&mode=rc" +
                        "&hours=%4$d&onlynew=on&go=Сканировать&format=%1$5&userlang=ru",
                null,
                null,
                null,
                null,
                true),
        @Deprecated
        CATSCAN2("catscan2", CATSCAN2_DOMAIN, CATSCAN2_PATH,
                TSV_CATSCAN2_FORMAT, true, false, false, 8928,  // 8760 = 1 year = 24*31*12 = 8928;
                ServiceFeatures.CATSCAN2_FEATURES,
                "language=%1$s&depth=%2$d&categories=%3$s&ns[%4$d]=1&sortby=title&format=%5$s&doit=submit",
                "language=%1$s&depth=%2$d&categories=%3$s&negcats=%4$s&ns[%5$d]=1&comb[union]=1&sortby=title&format=%6$s&doit=submit",
                "language=%1$s&depth=%2$d&categories=%3$s&%4$s=%5$s&ns[%6$d]=1&sortby=title&format=%7$s&doit=submit",
                "language=%1$s&depth=%2$d&categories=%3$s&negcats=%4$s&%5$s=%6$s&ns[%7$d]=1&comb[union]=1&sortby=title&format=%8$s&doit=submit",
                "language=%1$s&depth=%2$d&categories=%3$s&ns[%5$d]=1&max_age=%4$d&only_new=1&sortby=title&format=%5$s&doit=submit",
                "language=%1$s&depth=%2$d&categories=%3$s&negcats=%4$s&ns[%6$d]=1&comb[union]=1&max_age=%5$d&only_new=1&sortby=title&format=%6$s&doit=1",
                null,
                null,
                "^\\S+\\s+\\d+\\s+\\d+\\s+\\d+\\s+\\S+\\s+\\S+$",
                true),
        CATSCAN3("catscan3", CATSCAN3_DOMAIN, CATSCAN3_PATH,
                TSV_CATSCAN3_FORMAT, true, true, false, 8928,  // 8760 = 1 year = 24*31*12 = 8928;
                ServiceFeatures.CATSCAN3_FEATURES,
                "language=%1$s&depth=%2$d&categories=%3$s&ns[%4$d]=1&sortby=title&format=%5$s&doit=submit",
                "language=%1$s&depth=%2$d&categories=%3$s&negcats=%4$s&ns[%5$d]=1&comb[union]=1&sortby=title&format=%6$s&doit=submit",
                "language=%1$s&depth=%2$d&categories=%3$s&%4$s=%5$s&ns[%6$d]=1&sortby=title&format=%7$s&doit=submit",
                "language=%1$s&depth=%2$d&categories=%3$s&negcats=%4$s&%5$s=%6$s&ns[%7$d]=1&comb[union]=1&sortby=title&format=%8$s&doit=submit",
                "language=%1$s&depth=%2$d&categories=%3$s&ns[%5$d]=1&max_age=%4$d&only_new=1&sortby=title&format=%5$s&doit=submit",
                "language=%1$s&depth=%2$d&categories=%3$s&negcats=%4$s&ns[%6$d]=1&comb[union]=1&max_age=%5$d&only_new=1&sortby=title&format=%6$s&doit=1",
                null,
                null,
                "^\\S+\\s+\\d+\\s+\\d+\\s+\\d+\\s+\\S+\\s+\\d+\\s+\\S+$",
                false),
        PETSCAN("petscan", PETSCAN_DOMAIN, PETSCAN_PATH,
                TSV_PETSCAN_FORMAT,
                true, true, false, 17856,  // 2 year = 24*31*12*2 = 8928*2;
                ServiceFeatures.PETSCAN_FEATURES,
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&ns[%4$d]=1&sortby=title&format=%5$s&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&negcats=%4$s&combination=union&ns[%5$d]=1&sortby=title&format=%6$s&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&%4$s=%5$s&ns[%6$d]=1&sortby=title&format=%7$s&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&negcats=%4$s&%5$s=%6$s&combination=union&ns[%7$d]=1&sortby=title&format=%8$s&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&ns[%5$d]=1&max_age=%4$d&only_new=on&sortorder=descending&sortby=title&format=%5$s&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&negcats=%4$s&combination=union&ns[%6$d]=1&max_age=%5$d&only_new=on&sortorder=descending&format=%6$s&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&%6$s=%7$s&ns[%5$d]=1" +
                        "&max_age=%4$d&only_new=on&sortorder=descending&sortby=title&format=%5$s&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&negcats=%4$s" +
                        "&%7$s=%8$s&combination=union&ns[%6$d]=1&max_age=%5$d&only_new=on" +
                        "&sortorder=descending&format=%9$s&doit=",
                "^\\d+\\s+\\S+\\s+\\d+\\s+(\\S+\\s+)?\\d+\\s+\\d+\\s*$",
                false);
        public final String name;
		public final String DOMAIN;
		public final String PATH;
		private final Format format;
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
        public final String GET_NEWPAGES_WITH_TEMPLATE_FORMAT;
        public final String GET_NEWPAGES_WITH_TEMPLATE_FORMAT_FAST;
		public final String LINE_RULE;
        public final boolean DOWN;

        private static Integer testFeatures = null;

		Service(String name, String domain, String path,
				Format format,
				boolean filteredByNamespace, boolean hasSuffix, boolean hasDeleted, int maxHours,
				int features,
				String getPagesFormat,
				String getPagesFormatFast,
				String getPagesWithTemplateFormat,
				String getPagesWithTemplateFormatFast,
				String getNewPagesFormat,
				String getNewPagesFormatFast,
                String getNewPagesWithTemplateFormat,
                String getNewPagesWithTemplateFormatFast,
                String lineRule,
                boolean down) {
			this.name = name;
			this.DOMAIN = domain;
			this.PATH = path;
			this.format = format;
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
            this.GET_NEWPAGES_WITH_TEMPLATE_FORMAT = getNewPagesWithTemplateFormat;
            this.GET_NEWPAGES_WITH_TEMPLATE_FORMAT_FAST = getNewPagesWithTemplateFormatFast;
			this.LINE_RULE = lineRule;
            this.DOWN = down;
			this.toString();
		}
		@Override
		public String toString() {
			return getName();
		}
		public String getName() { return name; }
		public boolean supportsFastMode() { return supportsFeature (ServiceFeatures.FAST_MODE); }
        public boolean supportsFeature(int feature) {
            if (testMode && testFeatures != null) {
                return ((testFeatures & feature) != 0);
            }
            return ((FEATURES & feature) != 0);
        }

        public static void setTestFeatures(Integer features) {
            testFeatures = features;
        }

		public static Service getServiceByName(String name) {
			return getServiceByName(name, null);
		}

		public static Service getServiceByName(String name, Service defaultService) {
            for (Service s: Service.values()) {
                if (s.getName().equals(name)) return s;
            }
			return defaultService;
		}

        public static Service getDefaultServiceForFeature(int feature, Service defaultValue) {
            return Service.PETSCAN;
        }

        public static boolean hasService(String name) {
            for (Service s: Service.values()) {
                if (s.name.equals(name)) return true;
            }
            return false;
        }

		public Format getFormat() {
			return format;
		}
	}

	public static String loadPagesForCatWithService(Service service,
			String category, String language, int depth, int namespace) throws IOException, InterruptedException
    {
        if (!service.supportsFeature(ServiceFeatures.PAGES)) {
            throw new IllegalArgumentException(
                    String.format(ERR_SERVICE_DOESNT_SUPPORT_FEATURE, service.name()));
		}
		log.debug("Downloading data for " + category);
        String url_query =
        		String.format(service.GET_PAGES_FORMAT,
                language,
                depth,
                category,
                namespace,
                service.format.getFormatType().name());
        return fetchQuery(service, url_query);
    }

	public static String loadPagesForCatListAndIgnoreWithService(Service service,
			List<String> cats, List<String> ignore, String language, int depth, int namespace) throws IOException, InterruptedException
    {
        if (!service.supportsFeature(ServiceFeatures.PAGES | ServiceFeatures.FAST_MODE)) {
            throw new IllegalArgumentException(
                    String.format(ERR_SERVICE_DOESNT_SUPPORT_FEATURE, service.name()));
		}
		log.debug("Downloading data for categories: " + Arrays.toString(cats.toArray()));
        log.debug("and ignore categories: " + Arrays.toString(ignore.toArray()));
        String url_query =
        		String.format(service.GET_PAGES_FORMAT_FAST,
                language,
                depth,
                StringUtils.join(cats, CAT_SEPARATOR),
                StringUtils.join(ignore, CAT_SEPARATOR),
                namespace,
                service.format.getFormatType().name());
        return fetchQuery(service, url_query);
    }

	public static String loadPagesWithTemplatesForCatWithService(Service service,
			String category, String language, int depth,
			List<String> templates, EnumerationType enumType, int namespace) throws IOException, InterruptedException
    {
        if (!service.supportsFeature(ServiceFeatures.PAGES_WITH_TEMPLATE)) {
            throw new IllegalArgumentException(
                    String.format(ERR_SERVICE_DOESNT_SUPPORT_FEATURE, service.name()));
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
                namespace,
                service.format.getFormatType().name());
        return fetchQuery(service, url_query);
    }

	public static String loadPagesWithTemplatesForCatListAndIgnoreWithService(Service service,
			List<String> cats, List<String> ignore, String language, int depth,
			List<String> templates, EnumerationType enumType, int namespace) throws IOException, InterruptedException
    {
        if (!service.supportsFeature(ServiceFeatures.PAGES_WITH_TEMPLATE |
                ServiceFeatures.FAST_MODE)) {
            throw new IllegalArgumentException(
                    String.format(ERR_SERVICE_DOESNT_SUPPORT_FEATURE, service.name()));
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
                namespace,
                service.format.getFormatType().name());
        return fetchQuery(service, url_query);
    }

	public static String loadNewPagesForCatWithService(Service service, String category, String language, int depth, int hours, int namespace) throws IOException, InterruptedException {
        log.debug("Downloading data for category " + category);
        if (!service.supportsFeature(ServiceFeatures.NEWPAGES)) {
            throw new IllegalArgumentException(
                    String.format(ERR_SERVICE_DOESNT_SUPPORT_FEATURE, service.name()));
        }

        String url_query;
        if (service.filteredByNamespace) {
        	url_query = String.format(service.GET_NEW_PAGES_FORMAT,
                    language,
                    depth,
                    category,
                    hours,
                    namespace,
                    service.format.getFormatType().name());
        } else {
            url_query =
                    String.format(service.GET_NEW_PAGES_FORMAT,
                            language,
                            depth,
                            category,
                            hours,
                            service.format.getFormatType().name());
        }
        return fetchQuery(service, url_query);
	}

	public static String loadNewPagesForCatListAndIgnoreWithService(Service service, List<String> cats, List<String> ignore, String language, int depth, int hours, int namespace) throws IOException, InterruptedException
    {
        if (!service.supportsFeature(ServiceFeatures.NEWPAGES | ServiceFeatures.FAST_MODE)) {
            throw new IllegalArgumentException(
                    String.format(ERR_SERVICE_DOESNT_SUPPORT_FEATURE, service.name()));
        }
        log.debug("Downloading data for categories: " + Arrays.toString(cats.toArray()));
        log.debug("and ignore categories: " + Arrays.toString(ignore.toArray()));
        String url_query =
                String.format(service.GET_NEW_PAGES_FORMAT_FAST,
                        language,
                        depth,
                        StringUtils.join(cats, CAT_SEPARATOR),
                        StringUtils.join(ignore, CAT_SEPARATOR),
                        hours,
                        namespace,
                        service.format.getFormatType().name()
                );
        return fetchQuery(service, url_query);
    }

    public static String loadNewPagesWithTemplatesForCatWithService(Service service,
            String category, String language, int depth, int hours, List<String> templates,
            EnumerationType enumType, int namespace) throws IOException, InterruptedException {
        if (!service.supportsFeature(ServiceFeatures.NEWPAGES_WITH_TEMPLATE)) {
            throw new IllegalArgumentException(
                    String.format(ERR_SERVICE_DOESNT_SUPPORT_FEATURE, service.name()));
        }
        log.debug("Downloading data for category " + category);
        String templatesParam = "templates_yes";
        switch (enumType) {
            case AND: templatesParam = "templates_yes"; break;
            case OR: templatesParam = "templates_any"; break;
            case NONE: templatesParam = "templates_no"; break;
        }
        String url = String.format(
                service.GET_NEW_PAGES_FORMAT,
                language,
                depth,
                category,
                hours,
                namespace,
                templatesParam,
                StringUtils.join(templates, CAT_SEPARATOR),
                service.format.getFormatType().name());
        return fetchQuery(service, url);
    }

    public static String loadNewPagesWithTemplatesForCatListAndIgnoreWithService(Service service,
            List<String> cats, List<String> ignore, String language, int depth, int hours,
            List<String> templates, EnumerationType enumType, int namespace) throws IOException,
                InterruptedException {
        if (!service.supportsFeature(ServiceFeatures.NEWPAGES_WITH_TEMPLATE |
                ServiceFeatures.FAST_MODE)) {
            throw new IllegalArgumentException(
                    String.format(ERR_SERVICE_DOESNT_SUPPORT_FEATURE, service.name()));
        }
        log.debug("Downloading data for categories: " + Arrays.toString(cats.toArray()));
        log.debug("and ignore categories: " + Arrays.toString(ignore.toArray()));
        String templatesParam = "templates_yes";
        switch (enumType) {
            case AND: templatesParam = "templates_yes"; break;
            case OR: templatesParam = "templates_any"; break;
            case NONE: templatesParam = "templates_no"; break;
        }
        String url = String.format(
                service.GET_NEWPAGES_WITH_TEMPLATE_FORMAT_FAST,
                language,
                depth,
                StringUtils.join(cats, CAT_SEPARATOR),
                StringUtils.join(ignore, CAT_SEPARATOR),
                hours,
                namespace,
                templatesParam,
                StringUtils.join(templates, CAT_SEPARATOR),
                service.format.getFormatType().name()
        );
        return fetchQuery(service, url);
    }

	private static String fetchQuery(Service service, String query) throws IOException, InterruptedException {
		URI uri = null;
		try {
			uri = new URI(HTTP, service.DOMAIN, service.PATH, query, null);
		} catch (URISyntaxException e) {
			log.error(e.toString());
			return null;
		}
        if (testMode) {
            if (savedQueries == null) savedQueries = new ArrayList<>();
            savedQueries.add(query);
        }
        if (testMode && mockedResponses != null) {
            if (mockedResponses.isEmpty()) {
                throw new RuntimeException(
                        "fetchQuery() called in test mode when no mocked responces is available!");
            }
            return mockedResponses.remove(0);
        }

		String page = null;
		try {
			page = HTTPTools.fetch(uri.toASCIIString(), !fastMode, true, true);
		} catch (java.net.SocketTimeoutException e) {
			if (fastMode) {
				throw e;
			} else {
				log.warn(e.toString()+", retry again ...");
				Thread.sleep(TIMEOUT_DELAY);
				page = HTTPTools.fetch(uri.toASCIIString(), true, true, true);
			}
		}
		return page;
	}

	public static void setFastMode(boolean fast) {
		fastMode = fast;
	}

    static void mockResponces(List<String> responces) {
        testMode = true;
        if (mockedResponses == null) {
            mockedResponses = new ArrayList<String>();
        }
        log.debug(String.format("Adding %d responces for mocking.", responces.size()));
        mockedResponses.addAll(responces);
    }

    static void resetFromTest() {
        if (mockedResponses != null) mockedResponses.clear();
        if (savedQueries != null) savedQueries.clear();
        testMode = true;
        Service.setTestFeatures(null);
    }

    static List<String> getQueries() {
        return savedQueries;
    }
}
