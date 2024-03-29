/**
 *  @(#)WikiTools.java 
 *  Copyright © 2020 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import static org.wikipedia.nirvana.parser.format.Format.FormatType.TSV;

import org.wikipedia.nirvana.annotation.VisibleForTesting;
import org.wikipedia.nirvana.parser.format.Format;
import org.wikipedia.nirvana.parser.format.TabFormatDescriptor;
import org.wikipedia.nirvana.parser.format.TabularFormat;
import org.wikipedia.nirvana.util.HttpTools;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// TODO: Rewrite it using classes. That enum Service is horrible.
//   That url strings are ugly and not suitable.
//   API methods are monstrous and not flexible.
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
    public static int RETRY_COUNT_DEFAULT = 2;
    private static final Logger log;
    private static final String CAT_SEPARATOR = "\r\n";
    private static final String LABS_DOMAIN = "tools.wmflabs.org";
    private static final String PETSCANOLD_DOMAIN = "petscan1.wmflabs.org";
    private static final String PETSCAN_DOMAIN = "petscan.wmflabs.org";
    private static final String PETSCAN_PATH = "/";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private static final int TIMEOUT_DELAY = 10000; // 10 sec

    private static boolean fastMode = false;
    private static boolean enableRetries = true;

    private static boolean testMode = false;
    private static List<String> mockedResponses = null;
    private static boolean testNeverSleepingMode = false;

    private static boolean enableStat = true;
    private static List<String> savedQueries = null;
    private static List<MutableInt> queriesStat = new ArrayList<>();
    private static int maxRetryCount = RETRY_COUNT_DEFAULT;
    private static MutableInt tryCount = new MutableInt(0);

    private static final String ERR_SERVICE_DOESNT_SUPPORT_FEATURE =
            "Service %s doesn't support this feature.";

    public static final String ERR_SERVICE_FILTER_BY_NAMESPACE_DISABLED =
            "We do not support service without filtering by namespace feature. Please fix it.";

    private static final Pattern HTTP_ERROR_CODE_PATTERN =
            Pattern.compile("^Server returned HTTP response code: (\\d+).*");

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
        /**
         * Features of {@link CatScanTools.Service#PETSCAN_OLD, CatScanTools.Service#PETSCAN}.
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
     * All possible namespace identifier formats.
     */
    public enum NamespaceFormat {
        UNKNOWN(false),
        // For example: 4
        NUMBER(false),
        // For example: Проект
        NAME_STRING(true),
        // For example: Project
        CANONICAL_STRING(true);

        /**
         * Has <code>true</code> if this namespace format is of string type. 
         */
        public final boolean isString;

        NamespaceFormat(boolean isString) {
            this.isString = isString;
        }
    }

    public static final TabFormatDescriptor TSV_PETSCAN_RULES = new TabFormatDescriptor.Builder()
            .shouldSkipLines(1)
            .withTitlePosition(1)
            .withPageIdPosition(2)
            .withNamespacePosition(3)
            .eachLineMustMatchRule("^\\d+\\s+\\S+\\s+\\d+\\s+(\\S+\\s+)?\\d+\\s+\\d+\\s*$")
            .build();
    public static final TabularFormat TSV_PETSCAN_FORMAT =
            new TabularFormat(TSV_PETSCAN_RULES, TSV);

    /**
     * CatScan service instances with preconfigured parameters.
     */
    public enum Service {
        PETSCAN_OLD("petscan1", PETSCANOLD_DOMAIN, PETSCAN_PATH,
                TSV_PETSCAN_FORMAT,
                NamespaceFormat.NAME_STRING,
                true, true, 17856,  // 2 year = 24*31*12*2 = 8928*2;
                ServiceFeatures.PETSCAN_FEATURES,
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&ns[%4$d]=1" +
                    "&sortby=title&format=%5$s&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&negcats=%4$s" +
                    "&combination=union&ns[%5$d]=1&sortby=title&format=%6$s&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&%4$s=%5$s&ns[%6$d]=1" +
                    "&sortby=title&format=%7$s&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&negcats=%4$s" +
                    "&%5$s=%6$s&combination=union&ns[%7$d]=1&sortby=title&format=%8$s&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&ns[%5$d]=1" +
                    "&max_age=%4$d&only_new=on&sortorder=descending&sortby=title&format=%6$s&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&negcats=%4$s" +
                    "&combination=union&ns[%6$d]=1&max_age=%5$d&only_new=on&sortorder=descending" +
                    "&format=%7$s&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&%6$s=%7$s&ns[%5$d]=1" +
                    "&max_age=%4$d&only_new=on&sortorder=descending&sortby=title&format=%8$s&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&negcats=%4$s" +
                    "&%7$s=%8$s&combination=union&ns[%6$d]=1&max_age=%5$d&only_new=on" +
                    "&sortorder=descending&format=%9$s&doit=",
                false),
        PETSCAN("petscan", PETSCAN_DOMAIN, PETSCAN_PATH,
                TSV_PETSCAN_FORMAT,
                NamespaceFormat.CANONICAL_STRING,
                true, false, 87600,  // 10 years = 24*365*10 = 8760*10;
                ServiceFeatures.PETSCAN_FEATURES,
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&ns[%4$d]=1" +
                    "&sortby=title&format=%5$s&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&negcats=%4$s" +
                    "&combination=union&ns[%5$d]=1&sortby=title&format=%6$s&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&%4$s=%5$s&ns[%6$d]=1" +
                    "&sortby=title&format=%7$s&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&negcats=%4$s" +
                    "&%5$s=%6$s&combination=union&ns[%7$d]=1&sortby=title&format=%8$s&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&ns[%5$d]=1" +
                    "&max_age=%4$d&only_new=on&sortorder=descending&sortby=title&format=%6$s&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&negcats=%4$s" +
                    "&combination=union&ns[%6$d]=1&max_age=%5$d&only_new=on&sortorder=descending" +
                    "&format=%7$s&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&%6$s=%7$s&ns[%5$d]=1" +
                    "&max_age=%4$d&only_new=on&sortorder=descending&sortby=title&format=%8$s&doit=",
                "language=%1$s&project=wikipedia&depth=%2$d&categories=%3$s&negcats=%4$s" +
                    "&%7$s=%8$s&combination=union&ns[%6$d]=1&max_age=%5$d&only_new=on" +
                    "&sortorder=descending&format=%9$s&doit=",
                false);
        public final String name;
        public final String domain;
        public final String path;
        private final Format format;
        /**
         * Namespace format of the namespace identifier returned by the service.
         */
        public final NamespaceFormat namespaceFormat;

        @Deprecated
        /**
         * If service request can be filtered by the required namespace or namespaces list.
         * Currently is true for all services.
         */
        public final boolean filteredByNamespace;
        /**
         * Should be <code>true</code> if page list has titles with namespace suffix.
         */
        public final boolean hasSuffix;

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
        public final boolean down;

        private static Integer testFeatures = null;

        Service(String name, String domain, String path, 
                Format format,
                NamespaceFormat namespaceFormat,
                boolean filteredByNamespace, boolean hasSuffix, int maxHours, 
                int features,
                String getPagesFormat,
                String getPagesFormatFast,
                String getPagesWithTemplateFormat,
                String getPagesWithTemplateFormatFast,
                String getNewPagesFormat,
                String getNewPagesFormatFast,
                String getNewPagesWithTemplateFormat,
                String getNewPagesWithTemplateFormatFast,
                boolean down) {
            this.name = name;
            this.domain = domain;
            this.path = path;
            this.format = format;
            this.namespaceFormat = namespaceFormat;
            this.filteredByNamespace = filteredByNamespace;
            this.hasSuffix = hasSuffix;
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
            return Service.PETSCAN;
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

        public Format getFormat() {
            return format;
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
                namespace,
                service.format.getFormatType().getFormatType());
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
                namespace,
                service.format.getFormatType().getFormatType());
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
                namespace,
                service.format.getFormatType().getFormatType());
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
                namespace,
                service.format.getFormatType().getFormatType());
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
                    namespace,
                    service.format.getFormatType().getFormatType());
        } else {
            throw new IllegalStateException(ERR_SERVICE_FILTER_BY_NAMESPACE_DISABLED);
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
        String urlQuery = String.format(service.getNewPagesFormatFast,
                language,
                depth,
                StringUtils.join(cats, CAT_SEPARATOR),
                StringUtils.join(ignore, CAT_SEPARATOR),
                hours,
                namespace,
                service.format.getFormatType().getFormatType()
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
                StringUtils.join(templates, CAT_SEPARATOR),
                service.format.getFormatType().getFormatType());
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
                StringUtils.join(templates, CAT_SEPARATOR),
                service.format.getFormatType().getFormatType()
        );
        return fetchQuery(service, url);
    }

    private static String fixEncodedUrl(String url) {
        // Fix for URI#toASCIIString which doesn't encode some chars properly.
        // '[', ']' chars are not allowed in the query part of URL.
        // See details here:
        // - https://stackoverflow.com/questions/40568/are-square-brackets-permitted-in-urls
        // - https://www.ietf.org/rfc/rfc3986.txt
        // - https://www.w3.org/Addressing/URL/url-spec.txt
        return url.replace("[", "%5B").replace("]", "%5D");
    }

    private static void sleep(long ms) throws InterruptedException {
        if (!testNeverSleepingMode) {
            Thread.sleep(ms);
        }
    }

    private static String fetchQuery(Service service, String query) throws IOException,
            InterruptedException {
        URI uri = null;
        try {
            uri = new URI(HTTPS, service.domain, service.path, query, null);
        } catch (URISyntaxException e) {            
            throw new RuntimeException(e);
        }
        tryCount = new MutableInt(0);
        if (enableStat) {
            queriesStat.add(tryCount);
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
            String response = mockedResponses.remove(0);
            if (response.equals("@java.net.SocketTimeoutException")) {
                throw new SocketTimeoutException();
            }
            return response;
        }

        // query is not url-encoded and uri isn't too.
        // After calling uri.toASCIIString() we get url-encoded string,
        // but it still has forbidden characters: '[', ']'.
        // So, we replace them manually knowing that in host part we don't have them.
        String endodedUrl = fixEncodedUrl(uri.toASCIIString());
        boolean needRetry = true;
        IOException lastError = null;
        while (needRetry && tryCount.getValue() < maxRetryCount) {
            needRetry = false;
            tryCount.add(1);
            if (tryCount.getValue() > 1) {
                log.warn("{} -> retry again after sleeping {} seconds ...",
                        lastError.getMessage(), TIMEOUT_DELAY / 1000);
                sleep(TIMEOUT_DELAY);
            }
            try {
                String page = HttpTools.fetch(endodedUrl, !fastMode, true);
                return page;
            } catch (SocketTimeoutException e) {
                lastError = e;
                if (enableRetries) {
                    needRetry = true;
                }
            } catch (IOException e) {
                lastError = e;
                if (enableRetries) {
                    Matcher m = HTTP_ERROR_CODE_PATTERN.matcher(e.getMessage());
                    if (m.matches()) {
                        String codeString = m.group(1);
                        int httpCode = Integer.parseInt(codeString);
                        if (httpCode == 504) {
                            needRetry = true;
                        }
                    }
                }
            }
        }
        throw lastError;
    }

    /**
     * Enable/disable Fast mode.
     * In Fast mode timeouts are minimal.
     * @param fast <code>true</code> to enable Fast mode.
     * @return Previous state of fast mode flag.
     */
    public static boolean setFastMode(boolean fast) {
        boolean result = fastMode;
        fastMode = fast;
        return result;
    }
    
    /**
     * Enable/disable retry system.
     * Retry system will retry request in the next known cases:
     * - SocketTimeoutException (slow connection, too big data, too long request processing)
     * - HTTP 504 error (catscan failed to process data because of internal error or it was just
     * too heavy data)
     *
     * @param enabled <code>true</code> to enable retry system.
     * @return Previous state of this flag.
     */
    public static boolean enableRetries(boolean enabled) {
        boolean result = enableRetries;
        enableRetries = enabled;
        return result;
    }

    /**
     * Set max retry count. If max retry count is greater than 1 this enables retry mechanism which
     * is disabled by default (max retry count is 1 by default).
     *
     * @param maxRetryCount max retry count (including starting try).
     */
    public static void setMaxRetryCount(int maxRetryCount) {
        CatScanTools.maxRetryCount = maxRetryCount;
    }

    /**
     * Enable or disable statistics.
     *
     * @param enabled If <code>true</code> statistics will be enabled, or disabled otherwise.
     * @return Previous statistics enabled state.
     */
    public static boolean enableStat(boolean enabled) {
        boolean result = enableStat;
        enableStat = enabled;
        return result;
    }

    /**
     * Reset statistics and some settings (max retry count).
     */
    public static void reset() {
        maxRetryCount = RETRY_COUNT_DEFAULT;
        resetStat();
    }

    /**
     * Reset statistics and some settings (max retry count).
     */
    public static void resetStat() {
        queriesStat = new ArrayList<>();
        tryCount = new MutableInt(0);
    }

    /**
     * @return queries statistics. Each integer in list - how many tries was done to make request.
     */
    public static List<Integer> getQuieriesStat() {
        return queriesStat.stream().map(MutableInt::getValue).collect(Collectors.toList());
    }

    @VisibleForTesting
    static void testsNeverSleep() {
        testNeverSleepingMode = true;
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
        reset();
    }

    @VisibleForTesting
    static void resetForInternalTesting() {
        testMode = false;
    }

    @VisibleForTesting
    static List<String> getQueries() {
        if (savedQueries == null) return Collections.emptyList();
        return savedQueries;
    }
}
