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

package org.wikipedia.nirvana.wiki;

import org.wikipedia.nirvana.annotation.VisibleForTesting;
import org.wikipedia.nirvana.util.HttpTools;
import org.wikipedia.nirvana.util.XmlTools;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * HTTP helper tools that scan categories and search for pages.
 * Those tools generates list of wiki pages from any namespaces.
 * The most typical usage scenarios:
 * 1) Search for new pages created in last x days in category A.
 * 2) Search for all pages in categories A,B,C but not in X, Y.
 * 3) Search for all pages in category A having template T.
 * All of those tools are developed by Magnus Manske and are heavy:
 * they require a lot of CPU, RAM and access to wiki database or replica.
 * For more info, see:
 * https://de.wikipedia.org/wiki/Wikipedia:Technik/Labs/PetScan
 * https://en.wikipedia.org/wiki/Wikipedia:PetScan
 * https://ru.wikipedia.org/wiki/Википедия:PetScan
 */
public class CatScanTools {
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
    private static final String PETSCANOLD_DOMAIN = "petscan1.wmflabs.org";
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
        log = LogManager.getLogger(CatScanTools.class.getName());
    }

    /**
     * Constant ids of service features or groups.
     */
    public static class ServiceFeatures {
        /**
         * Service can get pages for 1 category or more.
         */
        public static final int PAGES = 0b1;
        /**
         * Service can get new pages for 1 category or more. Only pages created
         * not before last X hours are returned.
         */
        public static final int NEWPAGES = 0b10;
        /**
         * Service can get list of pages or new pages specified by list of categories and list of
         * categories to ignore in one HTTP request. "Fast" means that instead of fetching lists of
         * pages for every category one by one, and then merging them we get resulting data in 1
         * shot - and this is fast.
         */
        public static final int FAST_MODE = 0b100;
        /**
         * Service can filter resulting list of pages to have only pages that have templates
         * from specified list of templates (all, one of, or none of).
         */
        public static final int PAGES_WITH_TEMPLATE = 0b1000;
        /**
         * The same as {@link #PAGES_WITH_TEMPLATE} but for new pages.
         */
        public static final int NEWPAGES_WITH_TEMPLATE = 0b10000;
        @Deprecated
        public static final int CATSCAN_FEATURES = NEWPAGES;
        @Deprecated
        public static final int CATSCAN2_FEATURES =
                PAGES | NEWPAGES | FAST_MODE | PAGES_WITH_TEMPLATE;
        @Deprecated
        /**
         * Features of {@link CatScanTools.Service#CATSCAN3}.
         */
        public static final int CATSCAN3_FEATURES =
                PAGES | NEWPAGES | FAST_MODE | PAGES_WITH_TEMPLATE;
        /**
         * Features of {@link CatScanTools.Service#PETSCAN_OLD}.
         */
        public static final int PETSCAN_FEATURES =
                PAGES | NEWPAGES | FAST_MODE | PAGES_WITH_TEMPLATE | NEWPAGES_WITH_TEMPLATE;
    }

    public enum EnumerationType {
        AND,
        OR,
        NONE
    }

    /**
     * CatScan service instances with preconfigured parameters.
     */
    public enum Service {
        @Deprecated
        CATSCAN("catscan", CATSCAN_DOMAIN, CATSCAN_PATH, 
                0, 0, 1, 5, 4, false, false, true, 720,
                ServiceFeatures.CATSCAN_FEATURES,
                null,
                null,
                null,
                null,
                "wikilang=%1$s&wikifam=.wikipedia.org&basecat=%2$s&basedeep=%3$d&mode=rc" +
                        "&hours=%4$d&onlynew=on&go=Сканировать&format=csv&userlang=ru",
                null,
                null,
                null,
                null,
                true),
        @Deprecated
        CATSCAN2("catscan2", CATSCAN2_DOMAIN, CATSCAN2_PATH,
                2, 2, 0, -1, 1, true, false, false, 8928,  // 8760 = 1 year = 24*31*12 = 8928;
                ServiceFeatures.CATSCAN2_FEATURES,
                "language=%1$s&depth=%2$d&categories=%3$s&ns[%4$d]=1&sortby=title&format=tsv" +
                    "&doit=submit",
                "language=%1$s&depth=%2$d&categories=%3$s&negcats=%4$s&ns[%5$d]=1&comb[union]=1" +
                    "&sortby=title&format=tsv&doit=submit",
                "language=%1$s&depth=%2$d&categories=%3$s&%4$s=%5$s&ns[%6$d]=1&sortby=title" +
                    "&format=tsv&doit=submit",
                "language=%1$s&depth=%2$d&categories=%3$s&negcats=%4$s&%5$s=%6$s&ns[%7$d]=1" +
                    "&comb[union]=1&sortby=title&format=tsv&doit=submit",
                "language=%1$s&depth=%2$d&categories=%3$s&ns[%5$d]=1&max_age=%4$d&only_new=1" +
                    "&sortby=title&format=tsv&doit=submit",
                "language=%1$s&depth=%2$d&categories=%3$s&negcats=%4$s&ns[%6$d]=1&comb[union]=1" +
                    "&max_age=%5$d&only_new=1&sortby=title&format=tsv&doit=1",
                null,
                null,
                "^\\S+\\s+\\d+\\s+\\d+\\s+\\d+\\s+\\S+\\s+\\S+$",
                true),
        @Deprecated
        CATSCAN3("catscan3", CATSCAN3_DOMAIN, CATSCAN3_PATH,
                2, 2, 0, -1, 1, true, true, false, 8928,  // 8760 = 1 year = 24*31*12 = 8928;
                ServiceFeatures.CATSCAN3_FEATURES,
                "language=%1$s&depth=%2$d&categories=%3$s&ns[%4$d]=1&sortby=title&format=tsv" +
                    "&doit=submit",
                "language=%1$s&depth=%2$d&categories=%3$s&negcats=%4$s&ns[%5$d]=1&comb[union]=1" +
                    "&sortby=title&format=tsv&doit=submit",
                "language=%1$s&depth=%2$d&categories=%3$s&%4$s=%5$s&ns[%6$d]=1&sortby=title" +
                    "&format=tsv&doit=submit",
                "language=%1$s&depth=%2$d&categories=%3$s&negcats=%4$s&%5$s=%6$s&ns[%7$d]=1" +
                    "&comb[union]=1&sortby=title&format=tsv&doit=submit",
                "language=%1$s&depth=%2$d&categories=%3$s&ns[%5$d]=1&max_age=%4$d&only_new=1" +
                    "&sortby=title&format=tsv&doit=submit",
                "language=%1$s&depth=%2$d&categories=%3$s&negcats=%4$s&ns[%6$d]=1&comb[union]=1" +
                    "&max_age=%5$d&only_new=1&sortby=title&format=tsv&doit=1",
                null,
                null,
                "^\\S+\\s+\\d+\\s+\\d+\\s+\\d+\\s+\\S+\\s+\\d+\\s+\\S+$",
                true),
        PETSCAN_OLD("petscan", PETSCANOLD_DOMAIN, PETSCAN_PATH,
                1, 3, 1, -1, 2,
                true, true, false, 17856,  // 2 year = 24*31*12*2 = 8928*2;
                ServiceFeatures.PETSCAN_FEATURES,
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&ns[%4$d]=1" +
                    "&sortby=title&format=tsv&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&negcats=%4$s" +
                    "&combination=union&ns[%5$d]=1&sortby=title&format=tsv&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&%4$s=%5$s&ns[%6$d]=1" +
                    "&sortby=title&format=tsv&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&negcats=%4$s" +
                    "&%5$s=%6$s&combination=union&ns[%7$d]=1&sortby=title&format=tsv&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&ns[%5$d]=1" +
                    "&max_age=%4$d&only_new=on&sortorder=descending&sortby=title&format=tsv&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&negcats=%4$s" +
                    "&combination=union&ns[%6$d]=1&max_age=%5$d&only_new=on&sortorder=descending" +
                    "&format=tsv&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&%6$s=%7$s&ns[%5$d]=1" +
                    "&max_age=%4$d&only_new=on&sortorder=descending&sortby=title&format=tsv&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&negcats=%4$s" +
                    "&%7$s=%8$s&combination=union&ns[%6$d]=1&max_age=%5$d&only_new=on" +
                    "&sortorder=descending&format=tsv&doit=",
                "^\\d+\\s+\\S+\\s+\\d+\\s+(\\S+\\s+)?\\d+\\s+\\d+\\s*$",
                false);
        public final String name;
        public final String domain;
        public final String path;
        public final int skipLines;
        public final int nsPos;
        public final int titlePos;
        public final int revidPos;
        public final int idPos;
        public final boolean filteredByNamespace;
        public final boolean hasSuffix;
        public final boolean hasDeleted;
        public final int maxHours;
        public final int features;
        public final String getPagesFormat;
        public final String getPagesFormatFast;
        public final String getPagesWithTemplateFormat;
        public final String getPagesWithTemplateFormatFast;
        public final String getNewPagesFormat;
        public final String getNewPagesFormatFast;
        public final String getNewPagesWithTemplateFormat;
        public final String getNewPagesWithTemplateFormatFast;
        public final String lineRule;
        public final boolean down;

        private static Integer testFeatures = null;

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
                String getNewPagesWithTemplateFormat,
                String getNewPagesWithTemplateFormatFast,
                String lineRule,
                boolean down) {
            this.name = name;
            this.domain = domain;
            this.path = path;
            this.skipLines = skipLines;
            this.nsPos = namespacePos;
            this.titlePos = titlePos;
            this.revidPos = revidPos;
            this.idPos = idPos;
            this.filteredByNamespace = filteredByNamespace;
            this.hasSuffix = hasSuffix;
            this.hasDeleted = hasDeleted;
            this.maxHours = maxHours;
            this.features = features;
            this.getPagesFormat = getPagesFormat;
            this.getPagesFormatFast = getPagesFormatFast;
            this.getPagesWithTemplateFormat = getPagesWithTemplateFormat;
            this.getPagesWithTemplateFormatFast = getPagesWithTemplateFormatFast;
            this.getNewPagesFormat = getNewPagesFormat;
            this.getNewPagesFormatFast = getNewPagesFormatFast;
            this.getNewPagesWithTemplateFormat = getNewPagesWithTemplateFormat;
            this.getNewPagesWithTemplateFormatFast = getNewPagesWithTemplateFormatFast;
            this.lineRule = lineRule;
            this.down = down;
            this.toString();
        }

        @Override
        public String toString() {
            return getName();
        }

        /**
         * Returns string name of the service.
         */
        public String getName() {
            return name;
        }

        /**
         * Check if service supports {@link CatScanTools.ServiceFeatures#FAST_MODE}.
         */
        public boolean supportsFastMode() {
            return supportsFeature(ServiceFeatures.FAST_MODE);
        }

        /**
         * Check if service supports feature specified by id.
         *
         * @param feature Id of the feature.
         * @return <code>true</code> if service supports this feature.
         */
        public boolean supportsFeature(int feature) {
            if (testMode && testFeatures != null) {
                return ((testFeatures & feature) != 0); 
            }
            return ((features & feature) != 0); 
        }

        @VisibleForTesting
        public static void setTestFeatures(Integer features) {
            testFeatures = features;
            testMode = true;
        }

        /**
         * Get {@link Service} instance with the specified name or <code>null</code> if not found.
         *
         * @param name Name of service to get.
         * @return {@link Service} instance.
         */
        public static Service getServiceByName(String name) {
            return getServiceByName(name, null);
        }

        /**
         * Get {@link Service} instance with the specified name or default service if there's no
         * service with such name.
         *
         * @param name Name of service to get.
         * @param defaultService Service to return if the specified service name not found.
         * @return {@link Service} instance.
         */
        public static Service getServiceByName(String name, Service defaultService) {
            for (Service s: Service.values()) {
                if (s.getName().equals(name)) return s;
            }
            return defaultService;
        }

        public static Service getDefaultServiceForFeature(int feature, Service defaultValue) {
            return Service.PETSCAN_OLD;
        }

        /**
         * Returns <code>true</code> if service list defined by this class includes service with
         * the specified <code>name</code>.
         *
         * @param name Name of service to check.
         * @return <code>true</code> if service exists.
         */
        public static boolean hasService(String name) {
            for (Service s: Service.values()) {
                if (s.name.equals(name)) return true;
            }
            return false;
        }
    }

    /**
     * Returns list of pages from wiki.
     *
     * The list includes only pages from given category
     * <code>category</code>.
     * The same as {@link #loadPagesForCatListAndIgnoreWithService} but works for one category only.
     *
     * @param service {@link Service} instance used to extract list.
     * @param category Category to search pages in.
     * @param language Language of wiki to get data from.
     * @param depth Default depth to search in categories.
     * @param namespace Namespace numeric id. Pages from this namespace only are taken. 
     * @return list of pages.
     */
    public static String loadPagesForCatWithService(Service service, 
            String category, String language, int depth, int namespace) throws IOException,
            InterruptedException {
        if (!service.supportsFeature(ServiceFeatures.PAGES)) {
            throw new IllegalArgumentException(
                    String.format(ERR_SERVICE_DOESNT_SUPPORT_FEATURE, service.name()));
        }
        log.debug("Download data for: {}", category);
        String urlQuery =
                String.format(service.getPagesFormat,
                language,
                depth,
                category,
                namespace);
        return fetchQuery(service, urlQuery);   
    }

    /**
     * Returns list of pages from wiki.
     *
     * The list includes only pages from given category list
     * <code>cats</code> but does not include pages from given category list <code>ignore</code>.
     * Pages are searched up to given default depth <code>depth</code> except those categories
     * where depth was specified explicitly (using "|" separator). Only pages from namespace
     * <code>namespace</code> are taken. Only pages created in last <code>hours</code> are searched.
     * Also, resulting page list is filtered to include only pages that contain some templates
     * given in <code>templates</code> argument (all, or any of, or none of).
     *
     * @param service {@link Service} instance used to extract list.
     * @param cats List of categories to search pages in.
     * @param ignore List of categories to ignore.
     * @param language Language of wiki to get data from.
     * @param depth Default depth to search in categories.
     * @param namespace Namespace numeric id. Pages from this namespace only are taken. 
     * @return list of pages.
     */
    public static String loadPagesForCatListAndIgnoreWithService(Service service, 
            List<String> cats, List<String> ignore, String language, int depth, int namespace)
            throws IOException, InterruptedException {
        if (!service.supportsFeature(ServiceFeatures.PAGES | ServiceFeatures.FAST_MODE)) {
            throw new IllegalArgumentException(
                    String.format(ERR_SERVICE_DOESNT_SUPPORT_FEATURE, service.name()));
        }
        log.debug("Download data for categories: {}", Arrays.toString(cats.toArray()));
        log.debug("and ignore categories: {}", Arrays.toString(ignore.toArray()));
        String urlQuery = 
                String.format(service.getPagesFormatFast,
                language,
                depth,
                StringUtils.join(cats, CAT_SEPARATOR),
                StringUtils.join(ignore, CAT_SEPARATOR),
                namespace);
        return fetchQuery(service, urlQuery);   
    }

    /**
     * Returns list of pages from wiki.
     *
     * The list includes only pages from given category
     * <code>category</code>.
     * The same as {@link #loadPagesWithTemplatesForCatListAndIgnoreWithService} but works for
     * one category only.
     *
     * @param service {@link Service} instance used to extract list.
     * @param category Category to search pages in.
     * @param language Language of wiki to get data from.
     * @param depth Default depth to search in categories.
     * @param templates Template list. Pages should contain template from this list (all or any
     *     or none).
     * @param enumType Specificator for <code>templates</code> argument.
     * @param namespace Namespace numeric id. Pages from this namespace only are taken. 
     * @return list of pages.
     */
    public static String loadPagesWithTemplatesForCatWithService(Service service, 
            String category, String language, int depth, 
            List<String> templates, EnumerationType enumType, int namespace) throws IOException,
            InterruptedException {
        if (!service.supportsFeature(ServiceFeatures.PAGES_WITH_TEMPLATE)) {
            throw new IllegalArgumentException(
                    String.format(ERR_SERVICE_DOESNT_SUPPORT_FEATURE, service.name()));
        }
        log.debug("Download data for: {}", category);
        String templatesParam = "templates_yes";
        switch (enumType) {
            case AND:
                templatesParam = "templates_yes";
                break;
            case OR:
                templatesParam = "templates_any";
                break;
            case NONE:
                templatesParam = "templates_no";
                break;
            default:
                throw new IllegalArgumentException("Unexpected enum type: " +
                        String.valueOf(enumType));
        }
        String urlQuery = 
                String.format(service.getPagesWithTemplateFormat,
                language,
                depth,
                category,
                templatesParam,
                StringUtils.join(templates, CAT_SEPARATOR),
                namespace);
        return fetchQuery(service, urlQuery);
    }

    /**
     * Returns list of pages from wiki.
     *
     * The same as {@link #loadPagesForCatListAndIgnoreWithService} but resulting list is
     * additionally filtered to have some templates from given list specified in
     * <code>templates</code> argument (all, or one of, or any of).
     *
     * @param service {@link Service} instance used to extract list.
     * @param cats List of categories to search pages in.
     * @param ignore List of categories to ignore.
     * @param language Language of wiki to get data from.
     * @param depth Default depth to search in categories.
     * @param templates Template list. Pages should contain template from this list (all or any
     *     or none).
     * @param enumType Specificator for <code>templates</code> argument.
     * @param namespace Namespace numeric id. Pages from this namespace only are taken. 
     * @return list of pages.
     */
    public static String loadPagesWithTemplatesForCatListAndIgnoreWithService(Service service, 
            List<String> cats, List<String> ignore, String language, int depth, 
            List<String> templates, EnumerationType enumType, int namespace) throws IOException,
            InterruptedException {
        if (!service.supportsFeature(ServiceFeatures.PAGES_WITH_TEMPLATE |
                ServiceFeatures.FAST_MODE)) {
            throw new IllegalArgumentException(
                    String.format(ERR_SERVICE_DOESNT_SUPPORT_FEATURE, service.name()));
        }
        log.debug("Download data for categories: {}", Arrays.toString(cats.toArray()));
        log.debug("and ignore categories: {}", Arrays.toString(ignore.toArray()));
        String templatesParam = "templates_yes";
        switch (enumType) {
            case AND:
                templatesParam = "templates_yes";
                break;
            case OR:
                templatesParam = "templates_any";
                break;
            case NONE:
                templatesParam = "templates_no";
                break;
            default:
                throw new IllegalArgumentException("Unexpected enum type: " +
                        String.valueOf(enumType));
        }
        String urlQuery = 
                String.format(service.getPagesWithTemplateFormatFast,
                language,
                depth,
                StringUtils.join(cats, CAT_SEPARATOR),
                StringUtils.join(ignore, CAT_SEPARATOR),
                templatesParam,
                StringUtils.join(templates, CAT_SEPARATOR),
                namespace);
        return fetchQuery(service, urlQuery);
    }

    /**
     * Returns list of new pages from wiki.
     *
     * The list includes only pages from given category
     * <code>category</code>.
     * The same as {@link #loadNewPagesForCatListAndIgnoreWithService} but works for
     * one category only.
     *
     * @param service {@link Service} instance used to extract list.
     * @param category Category to search pages in.
     * @param language Language of wiki to get data from.
     * @param depth Default depth to search in categories.
     * @param hours Take only pages created not before this count of hours.
     * @param namespace Namespace numeric id. Pages from this namespace only are taken. 
     * @return list of pages.
     */
    public static String loadNewPagesForCatWithService(Service service, String category,
            String language, int depth, int hours, int namespace) throws IOException,
            InterruptedException {
        log.debug("Download data for category: {}", category);
        if (!service.supportsFeature(ServiceFeatures.NEWPAGES)) {
            throw new IllegalArgumentException(
                    String.format(ERR_SERVICE_DOESNT_SUPPORT_FEATURE, service.name()));
        }

        String urlQuery;
        if (service.filteredByNamespace) {
            urlQuery = String.format(service.getNewPagesFormat,
                    language,
                    depth,
                    category,
                    hours,
                    namespace);
        } else {
            urlQuery = 
                    String.format(service.getNewPagesFormat,
                    language,
                    depth,
                    category,
                    hours);
        }
        return fetchQuery(service, urlQuery);
    }

    /**
     * Returns list of new pages from wiki.
     *
     * The same as {@link #loadPagesForCatListAndIgnoreWithService} but only pages
     * created in last <code>hours</code> hours are searched.
     *
     * The list includes only pages from given category list
     * <code>cats</code> but does not include pages from given category list <code>ignore</code>.
     * Pages are searched up to given default depth <code>depth</code> except those categories
     * where depth was specified explicitly (using "|" separator). Only pages from namespace
     * <code>namespace</code> are taken. Only pages created in last <code>hours</code> are searched.
     * Also, resulting page list is filtered to include only pages that contain some templates
     * given in <code>templates</code> argument (all, or any of, or none of).
     *
     * @param service {@link Service} instance used to extract list.
     * @param cats List of categories to search pages in.
     * @param ignore List of categories to ignore.
     * @param language Language of wiki to get data from.
     * @param depth Default depth to search in categories.
     * @param hours Take only pages created not before this count of hours.
     * @param namespace Namespace numeric id. Pages from this namespace only are taken. 
     * @return list of pages.
     */
    public static String loadNewPagesForCatListAndIgnoreWithService(Service service,
            List<String> cats, List<String> ignore, String language, int depth, int hours,
            int namespace) throws IOException, InterruptedException {
        if (!service.supportsFeature(ServiceFeatures.NEWPAGES | ServiceFeatures.FAST_MODE)) {
            throw new IllegalArgumentException(
                    String.format(ERR_SERVICE_DOESNT_SUPPORT_FEATURE, service.name()));
        }
        log.debug("Download data for categories: {}", Arrays.toString(cats.toArray()));
        log.debug("and ignore categories: {}", Arrays.toString(ignore.toArray()));
        String urlQuery = 
                String.format(service.getNewPagesFormatFast,
                language,
                depth,
                StringUtils.join(cats, CAT_SEPARATOR),
                StringUtils.join(ignore, CAT_SEPARATOR),
                hours,
                namespace
                );        
        return fetchQuery(service, urlQuery);
    }

    /**
     * Returns list of new pages from wiki.
     *
     * The list includes only pages from given category
     * <code>category</code>.
     * The same as {@link #loadNewPagesWithTemplatesForCatListAndIgnoreWithService} but works for
     * one category only.
     *
     * @param service {@link Service} instance used to extract list.
     * @param category Category to search pages in.
     * @param language Language of wiki to get data from.
     * @param depth Default depth to search in categories.
     * @param hours Take only pages created not before this count of hours.
     * @param templates Template list. Pages should contain template from this list (all or any
     *     or none).
     * @param enumType Specificator for <code>templates</code> argument.
     * @param namespace Namespace numeric id. Pages from this namespace only are taken. 
     * @return list of pages.
     */
    public static String loadNewPagesWithTemplatesForCatWithService(Service service,
            String category, String language, int depth, int hours, List<String> templates,
            EnumerationType enumType, int namespace) throws IOException, InterruptedException {
        if (!service.supportsFeature(ServiceFeatures.NEWPAGES_WITH_TEMPLATE)) {
            throw new IllegalArgumentException(
                    String.format(ERR_SERVICE_DOESNT_SUPPORT_FEATURE, service.name()));
        }
        log.debug("Download data for category: {}", category);
        String templatesParam = "templates_yes";
        switch (enumType) {
            case AND:
                templatesParam = "templates_yes";
                break;
            case OR:
                templatesParam = "templates_any";
                break;
            case NONE:
                templatesParam = "templates_no";
                break;
            default:
                throw new IllegalArgumentException("Unexpected enum type: " +
                        String.valueOf(enumType));
        }
        String url = String.format(
                service.getNewPagesFormat,
                language,
                depth,
                category,
                hours,
                namespace,
                templatesParam,
                StringUtils.join(templates, CAT_SEPARATOR));
        return fetchQuery(service, url);
    }

    /**
     * Returns list of new pages from wiki.
     *
     * The same as {@link #loadNewPagesForCatListAndIgnoreWithService} but resulting list is
     * additionally filtered to have some templates from given list specified in
     * <code>templates</code> argument (all, or one of, or any of).
     *
     * The same as {@link #loadPagesWithTemplatesForCatListAndIgnoreWithService} but only pages
     * created in last <code>hours</code> hours are searched.
     *
     * @param service {@link Service} instance used to extract list.
     * @param cats List of categories to search pages in.
     * @param ignore List of categories to ignore.
     * @param language Language of wiki to get data from.
     * @param depth Default depth to search in categories.
     * @param hours Take only pages created not before this count of hours.
     * @param templates Template list. Pages should contain template from this list (all or any
     *     or none).
     * @param enumType Specificator for <code>templates</code> argument.
     * @param namespace Namespace numeric id. Pages from this namespace only are taken. 
     * @return list of pages.
     */
    public static String loadNewPagesWithTemplatesForCatListAndIgnoreWithService(Service service,
            List<String> cats, List<String> ignore, String language, int depth, int hours,
            List<String> templates, EnumerationType enumType, int namespace) throws IOException,
                InterruptedException {
        if (!service.supportsFeature(ServiceFeatures.NEWPAGES_WITH_TEMPLATE |
                ServiceFeatures.FAST_MODE)) {
            throw new IllegalArgumentException(
                    String.format(ERR_SERVICE_DOESNT_SUPPORT_FEATURE, service.name()));
        }
        log.debug("Download data for categories: {}", Arrays.toString(cats.toArray()));
        log.debug("and ignore categories: {}", Arrays.toString(ignore.toArray()));
        String templatesParam = "templates_yes";
        switch (enumType) {
            case AND:
                templatesParam = "templates_yes";
                break;
            case OR:
                templatesParam = "templates_any";
                break;
            case NONE:
                templatesParam = "templates_no";
                break;
            default:
                throw new IllegalArgumentException("Unexpected enum type: " +
                        String.valueOf(enumType));
        }
        String url = String.format(
                service.getNewPagesWithTemplateFormatFast,
                language,
                depth,
                StringUtils.join(cats, CAT_SEPARATOR),
                StringUtils.join(ignore, CAT_SEPARATOR),
                hours,
                namespace,
                templatesParam,
                StringUtils.join(templates, CAT_SEPARATOR));        
        return fetchQuery(service, url);
    }

    private static String fetchQuery(Service service, String query) throws IOException,
            InterruptedException {
        URI uri = null;
        try {
            uri = new URI(HTTP, service.domain, service.path, query, null);
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
            page = HttpTools.fetch(uri.toASCIIString(), !fastMode, true);
        } catch (SocketTimeoutException e) {
            if (fastMode) {
                throw e;
            } else {
                log.warn("{}, retry again ...", e.getMessage());
                Thread.sleep(TIMEOUT_DELAY);
                page = HttpTools.fetch(uri.toASCIIString(), true, true);
            }
        }
        return XmlTools.unescapeSimple(page);
    }

    /**
     * Enable/disable Fast mode.
     * Usually, when HTTP request fails it's retried.
     * In Fast mode this retry is disabled.
     * @param fast <code>true</code> to enable Fast mode.
     */
    public static void setFastMode(boolean fast) {
        fastMode = fast;
    }

    @VisibleForTesting
    static void mockResponces(List<String> responces) {
        testMode = true;
        if (mockedResponses == null) {
            mockedResponses = new ArrayList<String>();
        }
        log.debug(String.format("Adding %d responces for mocking.", responces.size()));
        mockedResponses.addAll(responces);
    }

    @VisibleForTesting
    static void resetFromTest() {
        if (mockedResponses != null) mockedResponses.clear();
        if (savedQueries != null) savedQueries.clear();
        testMode = true;
        Service.setTestFeatures(null);
    }

    @VisibleForTesting
    static List<String> getQueries() {
        return savedQueries;
    }
}
