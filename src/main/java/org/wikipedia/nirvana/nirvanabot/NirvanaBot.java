/**
 *  @(#)NirvanaBot.java 1.19 20.12.2019
 *  Copyright © 2017 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.nirvanabot;

import static org.wikipedia.nirvana.nirvanabot.PortalConfig.LIST_TYPE_DISCUSSED_PAGES;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.LIST_TYPE_DISCUSSED_PAGES2;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.LIST_TYPE_DISCUSSED_PAGES_OLD;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.LIST_TYPE_NEW_PAGES;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.LIST_TYPE_NEW_PAGES_7_DAYS;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.LIST_TYPE_NEW_PAGES_7_DAYS_OLD;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.LIST_TYPE_NEW_PAGES_OLD;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.LIST_TYPE_NEW_PAGES_WITH_IMAGES;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig
        .LIST_TYPE_NEW_PAGES_WITH_IMAGES_IN_CARD;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig
        .LIST_TYPE_NEW_PAGES_WITH_IMAGES_IN_CARD_OLD;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig
        .LIST_TYPE_NEW_PAGES_WITH_IMAGES_IN_TEXT;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig
        .LIST_TYPE_NEW_PAGES_WITH_IMAGES_IN_TEXT_OLD;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.LIST_TYPE_NEW_PAGES_WITH_IMAGES_OLD;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.LIST_TYPE_PAGES;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.LIST_TYPE_PAGES_OLD;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.LIST_TYPE_PAGES_WITH_TEMPLATES;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.LIST_TYPE_WATCHLIST;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_AUTO;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_BOT;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_DEFAULT;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_NO;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_SMALL;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_YES;
import static org.wikipedia.nirvana.util.OptionsUtils.readIntegerProperty;
import static org.wikipedia.nirvana.util.OptionsUtils.readLongProperty;

import org.wikipedia.Wiki;
import org.wikipedia.nirvana.archive.ArchiveSettingsParser;
import org.wikipedia.nirvana.base.BasicBot;
import org.wikipedia.nirvana.base.BotFatalError;
import org.wikipedia.nirvana.base.BotSettingsError;
import org.wikipedia.nirvana.base.BotTemplateParser;
import org.wikipedia.nirvana.base.SettingsUtils;
import org.wikipedia.nirvana.error.ArchiveUpdateFailure;
import org.wikipedia.nirvana.error.BadFormatException;
import org.wikipedia.nirvana.error.DangerousEditException;
import org.wikipedia.nirvana.error.InvalidLineFormatException;
import org.wikipedia.nirvana.error.ServiceError;
import org.wikipedia.nirvana.localization.LocalizationManager;
import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.nirvanabot.imagefinder.ImageFinderInBody;
import org.wikipedia.nirvana.nirvanabot.imagefinder.ImageFinderInCard;
import org.wikipedia.nirvana.nirvanabot.imagefinder.ImageFinderUniversal;
import org.wikipedia.nirvana.nirvanabot.report.BotReporter;
import org.wikipedia.nirvana.nirvanabot.report.ReportItem;
import org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService.Status;
import org.wikipedia.nirvana.nirvanabot.serviceping.ServicePinger.AfterDowntimeCallback;
import org.wikipedia.nirvana.nirvanabot.templates.ComplexTemplateFilter;
import org.wikipedia.nirvana.nirvanabot.templates.SimpleTemplateFilter;
import org.wikipedia.nirvana.nirvanabot.templates.TemplateFindItem;
import org.wikipedia.nirvana.util.DateTools;
import org.wikipedia.nirvana.util.FileTools;
import org.wikipedia.nirvana.util.OptionsUtils;
import org.wikipedia.nirvana.util.SystemTime;
import org.wikipedia.nirvana.wiki.CatScanTools;
import org.wikipedia.nirvana.wiki.CatScanTools.EnumerationType;
import org.wikipedia.nirvana.wiki.NirvanaWiki;
import org.wikipedia.nirvana.wiki.WikiUtils;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

/**
 * Main NirvanaBot bot class.
 *
 */
public class NirvanaBot extends BasicBot {
    static final String SERVICE_AUTO = "auto";
    private static final String TYPE_ALL = "all";
    private String userNamespace;
    public static final String DEPTH_SEPARATOR = "|";
    public static final String ADDITIONAL_SEPARATOR = "#";
    
    private static Function<String, String> unescapeCategorylist =
            option -> option.replace(ADDITIONAL_SEPARATOR, DEPTH_SEPARATOR);
    
    private static int START_FROM = 0;
    private static int STOP_AFTER = 0;
    public static int UPDATE_PAUSE = 1000;
    
    public static String TIME_FORMAT = "short"; // short,long
    
    private boolean enableTask = false;
    private String taskListFile = "task.txt";

    protected String[] newpagesTemplates = null;

    private static final String PARAM_VALUE_DEFAULT_EN = "default";

    public static final String ERROR_PARSE_INTEGER_FORMAT_STRING_EN =
            "Error when parsing integer parameter \"%1$s\" integer value %2$s";
    private static final String ERROR_PARSE_INTEGER_FORMAT_STRING =
            "Ошибка при чтении параметра \"%1$s\". Значение %2$s не распознано как число.";
    private static final String ERROR_INTEGER_TOO_BIG_STRING =
            "Значение параметра \"%1$s\" слишком велико (%2$s). " +
            "Использовано максимальное значение (%3$s). Укажите разумную величину.";
    private static final String ERROR_NOTIFICATION_TITLE = "Ошибки в настройках бота";
    private static final String ERROR_NOTIFICATION_TEXT_BEGIN =
            "При проверке [[%1$s|параметров]] для обновления секции были обнаружены ошибки:";
    private static final String ERROR_NOTIFICATION_TEXT_END =
            "Пожалуйста исправьте ошибки, чтобы секция обновлялась корректно.";
    private static final String ERROR_NOTIFICATION_TEXT_ROBOT =
            "''Это сообщение написано роботом, не нужно на него отвечать!''";
    private static final String ERROR_NOTIFICATION_TEXT_LOCALISATION_HINT =
            "<small>Если вы видите здесь текст на русском языке, значит, в программе бота " +
            "не хватает переводов. Обратитесь к ботоводу. </small>";
    private static final String ERROR_NOTIFICATION_SUMMARY =
            "Бот обнаружил ошибки в настройках обновления автоматического списка.";
    private static final String ERROR_NOTIFICATION_TEXT_LOCALISATION_HINT2 =
            "<small>Если вы видите здесь текст на русском языке, значит, локализация бота " +
            "не завершена. Вы можете помочь, добавив переводы [[%1$s|на этой странице]].</small>";
    private static final String ERROR_INVALID_PARAMETER_EN =
            "Error in parameter \"%1$s\". Invalid value (%2$s).";
    private static final String ERROR_INVALID_PARAMETER =
            "Ошибка в параметре \"%1$s\". Задано неправильное значение (%2$s).";
    private static final String ERROR_MISS_PARAMETER =
            "Параметр \"%1$s\" не задан. Использовано значение по умолчанию.";
    private static final String ERROR_PARAMETER_NOT_ACCEPTED =
            "Значение параметра не принято.";
    private static final String ERROR_ABSENT_PARAMETER =
            "Ошибка в параметрах. Параметр \"%1$s\" не задан";
    private static final String ERROR_AUTHOR_PARAMETER_INVALID_USAGE =
            "Неверный параметр \"%1$s\". Поле %2$s не поддерживается для данного типа " +
            "списков. Удалите его!";
    private static final String ERROR_DISCUSSED_PAGES_SETTINGS_NOT_DEFINED =
            "Настройки обсуждаемых страниц не заданы или некорректны. " +
            "Невозможно обновлять этот вид списков.";
    private static final String ERROR_INVALID_TYPE =
            "Тип \"%1$s\" не поддерживается. " +
            "Используйте только разрешенные значения в параметре \"%2$s\".";

    private static int MAX_DEPTH = 30;
    private static int DEFAULT_DEPTH = 7;
    private static int MAX_MAXITEMS = 5000;
    private static int DEFAULT_MAXITEMS = 20;
    private static int DEFAULT_HOURS = 500;
    private static final int MAX_TRY_COUNT = 100;
    private static final int MAX_CATSCAN_TRY_COUNT = 100;

    // Do not take it from CatScanTools.Service.PETSCAN.name - this will break logging system by
    // early log initializer.
    private static String DEFAULT_SERVICE_NAME = "petscan";
    private static String SELECTED_SERVICE_NAME = SERVICE_AUTO;

    private static boolean DEFAULT_USE_FAST_MODE = true;
    private static boolean ERROR_NOTIFICATION = false;
    private static String COMMENT = "обновление";

    protected boolean enableReport = false;
    protected boolean enableStatus = false;
    private static String REPORT_FILE_NAME = "report.txt";
    private static String REPORT_WIKI_PAGE = "Участник:NirvanaBot/Новые статьи/Отчёт";
    private BotReporter.Verbosity detailedReportWikiVerbosity = BotReporter.Verbosity.NORMAL;
    private BotReporter.Verbosity detailedReportTxtVerbosity = BotReporter.Verbosity.NORMAL;
    private static String STATUS_WIKI_PAGE = "Участник:NirvanaBot/Новые статьи/Статус";
    private static String STATUS_WIKI_TEMPLATE =
            "Участник:NirvanaBot/Новые статьи/Отображение статуса";
    protected static String REPORT_FORMAT = "txt";
    private static String DEFAULT_FORMAT = "* [[%1$s]]";
    public static String DEFAULT_FORMAT_STRING = "* [[%1$s]]";

    /**
     * This list type is used when user specified invalid list type or didn't specify any list type
     * at all.
     */
    private String listTypeDefault;

    private String enabledTypesString = TYPE_ALL;
    Set<String> enabledTypes = Collections.singleton(TYPE_ALL);

    private static String DEFAULT_DELIMETER = "\n";

    /**
     * This is how many times we will try each portal page update.
     * (Retry if failed). "1" means no retries.
     */
    private int globalTryCount = 2;

    /**
     * This is how many times we will try to make request to catscan to get data.
     * (Retry if failed). "1" means no retries.
     */
    private int globalCatscanTryCount = 2;

    @Nullable
    private static String overridenPropertiesPage = null;

    private static String PICTURE_SEARCH_TAGS = "image file,Фото,портрет,Изображение,Файл,File";
    // Example templates for Russian Wikipedia:
    // Обоснование добросовестного использования, Disputed-fairuse, ОДИ, Несвободный файл/ОДИ
    private static String FAIR_USE_IMAGE_TEMPLATES = "";
    private static DiscussionPagesSettings DISCUSSION_PAGES_SETTINGS =
            DiscussionPagesSettings.EMPTY;
    private static String DISCUSSION_PAGES_SETTINGS_WIKI = null;
    
    private static int DEFAULT_NAMESPACE = 0;

    private static String DEFAULT_HEADER = "";
    private static String DEFAULT_FOOTER = "";
    private static int DEFAULT_ARCHIVE_PARSE_LINES_MAX_COUNT = -1;
    
    private static int DEFAULT_UPDATES_PER_DAY = 1;
    private static int MAX_UPDATES_PER_DAY = 4;
    
    private static int DEFAULT_STARTS_PER_DAY = 1;

    private static final String DEFAULT_REPORT_PREAMBULA_FILE = "templates/Report_preambula.txt";
    private String reportPreambulaFile = DEFAULT_REPORT_PREAMBULA_FILE;

    private static final long NO_RELOGIN_MIN_DOWNTIME_MS = 60L * 1000L;

    private int startNumber = 1;

    private NirvanaWiki commons = null;
    
    private ServiceManager serviceManager;

    private int botTimeout = 0;

    private long servicesTimeout = 1L * 60L * 60L * 1000L;  // 1 hour;

    private String wikiTranslationPage = null;
    private String customTranslationFile = null;

    private LocalizationManager localizationManager;
    private Localizer localizer;
    private SystemTime systemTime;
    private String cacheDir;

    /**
     * Set default try count.
     *
     * @param count how many times to try update each portal page (must be >= 1).
     */
    public void setTryCount(int count) {
        if (count < 1) throw new RuntimeException("Try count must be not below 1");
        globalTryCount = count;
    }

    static class NewPagesData {
        String portalSettingsPage;
        String portalSettingsText;
        ArrayList<String> errors;
        PortalParam param;
        PortalModule portalModule;
        String type = "";

        NewPagesData() {
            errors = new ArrayList<String>();
            portalModule = null;
        }
    }

    public enum BotError {
        NONE,
        UNKNOWN_ERROR,
        BOT_ERROR,
        SERVICE_ERROR,
        INTERNAL_ERROR,
        IO_ERROR,
        SETTINGS_ERROR,
        ARCHIVE_UPDATE_ERROR,
        FATAL
    }

    /**
     * Global bot settings.
     * Class used to provide read access to global bot settings for other classes.
     */
    public static class BotGlobalSettings {
        @Nonnull
        public String getDefaultFooter() {
            return DEFAULT_FOOTER;
        }

        @Nonnull
        public String getDefaultHeader() {
            return DEFAULT_HEADER;
        }
    }

    private static final String VERSION = "1.19";

    public static String PROGRAM_INFO = 
            "NirvanaBot v" + VERSION + ".\n" +
            "Updates Portal/Project sections at http://ru.wikipedia.org and collects statistics\n" +
            "See also http://ru.wikipedia.org/User:NirvanaBot\n" +
            "Copyright (C) 2017 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)\n" +
            "\n";

    @Override
    public void showInfo() {
        System.out.print(PROGRAM_INFO);
    }

    /**
     * Returns bot version string.
     */
    public String getVersion() {
        return VERSION;
    }

    /**
     * Constructor with flags.
     */
    public NirvanaBot(int flags) {
        super(flags);
        // This is workaround to make tests OK.
        // TODO: Get rid of static variables
        MAX_DEPTH = 30;
        DEFAULT_DEPTH = 7;
        MAX_MAXITEMS = 5000;
        DEFAULT_MAXITEMS = 20;
        DEFAULT_HOURS = 500;
        DEFAULT_SERVICE_NAME = "petscan";
        SELECTED_SERVICE_NAME = SERVICE_AUTO;
        DEFAULT_FORMAT = "* [[%1$s]]";
        DEFAULT_FORMAT_STRING = "* [[%1$s]]";
        DEFAULT_USE_FAST_MODE = true;
        DEFAULT_DELIMETER = "\n";
        DEFAULT_NAMESPACE = 0;
        DEFAULT_HEADER = "";
        DEFAULT_FOOTER = "";
        DEFAULT_ARCHIVE_PARSE_LINES_MAX_COUNT = -1;
        DEFAULT_UPDATES_PER_DAY = 1;
        MAX_UPDATES_PER_DAY = 4;
        DEFAULT_STARTS_PER_DAY = 1;
        DISCUSSION_PAGES_SETTINGS_WIKI = null;
        DISCUSSION_PAGES_SETTINGS = DiscussionPagesSettings.EMPTY;
        PICTURE_SEARCH_TAGS = "image file,Фото,портрет,Изображение,Файл,File";
        overridenPropertiesPage = null;
    }

    public static void main(String[] args) {
        BasicBot bot = new NirvanaBot(BasicBot.FLAG_SHOW_LICENSE);
        System.exit(bot.run(args));
    }

    @Override
    protected boolean loadCustomProperties(Map<String, String> launchParams)
            throws BotSettingsError {
        systemTime = initSystemTime();
        String str = properties.getProperty("new-pages-template");
        if (str == null) {
            throw new BotSettingsError("Property \"new-pages-template\" not found");
        }
        newpagesTemplates = str.trim().split("\\s*,\\s*");
        if (newpagesTemplates == null || newpagesTemplates.length == 0) {
            if (DEBUG_BUILD) {
                newpagesTemplates = new String[]{"Участник:NirvanaBot/test/Новые статьи"};
            } else {
                System.out.println("ABORT: properties not found");
                log.fatal("New pages template name property (\"new-pages-template\") "
                        + "is not specified in settings");
                return false;
            }
        }    
        log.info("new pages templates : {}", newpagesTemplates.toString());    
        
        ERROR_NOTIFICATION = properties
                .getProperty("error-notification", ERROR_NOTIFICATION ? YES : NO).equals(YES);
        log.info("error_notification={}", ERROR_NOTIFICATION);
        

        DEFAULT_DEPTH = readIntegerProperty(properties, "default-depth", DEFAULT_DEPTH, true);
        log.info("depth={}", DEFAULT_DEPTH);
        DEFAULT_HOURS = readIntegerProperty(properties, "default-hours", DEFAULT_HOURS, true);
        log.info("hours={}", DEFAULT_HOURS);
        DEFAULT_MAXITEMS = readIntegerProperty(properties, "default-maxitems", DEFAULT_MAXITEMS,
                true);
        log.info("maxitems={}", DEFAULT_MAXITEMS);        
        MAX_MAXITEMS = readIntegerProperty(properties, "max-maxitems", MAX_MAXITEMS, true);
        log.info("maxmaxitems={}", MAX_MAXITEMS);
        START_FROM = readIntegerProperty(properties, "start-from", START_FROM, false);
        STOP_AFTER = readIntegerProperty(properties, "stop-after", STOP_AFTER, false);        
        UPDATE_PAUSE = readIntegerProperty(properties,"update-pause",UPDATE_PAUSE,false);
        
        DEFAULT_STARTS_PER_DAY = readIntegerProperty(properties,"starts-per-day",
                DEFAULT_STARTS_PER_DAY, false);

        DEFAULT_ARCHIVE_PARSE_LINES_MAX_COUNT = readIntegerProperty(properties,
                "archive-parse-lines-max-count", DEFAULT_ARCHIVE_PARSE_LINES_MAX_COUNT, false);

        DEFAULT_SERVICE_NAME = validateService(
                properties.getProperty("default-service", DEFAULT_SERVICE_NAME),
                DEFAULT_SERVICE_NAME);
        SELECTED_SERVICE_NAME = validateSelectedService(
                properties.getProperty("service", SELECTED_SERVICE_NAME), SELECTED_SERVICE_NAME);

        DEFAULT_USE_FAST_MODE = properties
                .getProperty("fast-mode", DEFAULT_USE_FAST_MODE ? YES : NO).equals(YES);
        
        TIME_FORMAT = properties.getProperty("time-format",TIME_FORMAT);
        if (!TIME_FORMAT.equalsIgnoreCase("short") && !TIME_FORMAT.equalsIgnoreCase("long") ) {
            TIME_FORMAT = "short";
        }
        
        enableTask = properties.getProperty("task-list", enableTask ? YES : NO).equals(YES);
        log.info("task list : {}", (enableTask ? YES : NO));
        taskListFile = properties.getProperty("task-list-file",taskListFile);
        log.info("task list file: {}", taskListFile);

        if (launchParams.containsKey("start_number")) {
            try {
                startNumber = Integer.parseInt(launchParams.get("start_number"));
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        enableReport = properties.getProperty("statistics", enableReport ? YES : NO).equals(YES);
        enableStatus = properties.getProperty("update-status", enableStatus ? YES : NO)
                .equals(YES);
        REPORT_FILE_NAME = properties.getProperty("statistics-file", REPORT_FILE_NAME);
        REPORT_WIKI_PAGE = properties.getProperty("statistics-wiki", REPORT_WIKI_PAGE);
        detailedReportWikiVerbosity = SettingsUtils.propertyToEnum(
                properties,
                "statistics-wiki-verbosity",
                detailedReportWikiVerbosity,
                BotReporter.Verbosity::fromString);
        detailedReportTxtVerbosity = SettingsUtils.propertyToEnum(
                properties,
                "statistics-txt-verbosity",
                detailedReportTxtVerbosity,
                BotReporter.Verbosity::fromString);
        STATUS_WIKI_PAGE = properties.getProperty("status-wiki", STATUS_WIKI_PAGE);
        STATUS_WIKI_TEMPLATE = properties.getProperty("status-wiki-template", STATUS_WIKI_TEMPLATE);
        REPORT_FORMAT = properties.getProperty("statistics-format", REPORT_FORMAT);
        if (REPORT_FILE_NAME.contains("%(date)")) {
            String date = String.format("%1$tF", systemTime.now());
            REPORT_FILE_NAME = REPORT_FILE_NAME.replace("%(date)", date);
        }
        if (REPORT_FILE_NAME.contains("%(time)")) {
            String time = String.format("%1$tT", systemTime.now());
            REPORT_FILE_NAME = REPORT_FILE_NAME.replace("%(time)", time).replace(':', '-');
        }
        if (REPORT_FILE_NAME.contains("%(launch_number)")) {
            REPORT_FILE_NAME = REPORT_FILE_NAME.replace("%(launch_number)",
                    String.valueOf(startNumber));
        }
        
        PICTURE_SEARCH_TAGS = properties.getProperty("picture-search-tags", PICTURE_SEARCH_TAGS);
        log.info("picture search tags = {}", PICTURE_SEARCH_TAGS);
        FAIR_USE_IMAGE_TEMPLATES = properties.getProperty("fair-use-image-templates",
                FAIR_USE_IMAGE_TEMPLATES);
        log.info("fair-use image templates = {}", FAIR_USE_IMAGE_TEMPLATES);
        String discussionSettings = properties.getProperty("discussion-pages-templates", "");
        log.info("discussion settings: {}", discussionSettings);
        DISCUSSION_PAGES_SETTINGS = DiscussionPagesSettings.parseFromSettingsString(
                discussionSettings);
        DISCUSSION_PAGES_SETTINGS_WIKI = properties
                .getProperty("discussion-pages-settings-wikipage", "");

        enabledTypesString = properties.getProperty("type", enabledTypesString);
        if (enabledTypesString.isEmpty()) enabledTypesString = TYPE_ALL;
        enabledTypes = OptionsUtils.optionToSet(enabledTypesString);
        if (enabledTypes.isEmpty()) {
            log.fatal("Specify at least 1 portal section type to update");
            return false;
        }

        overridenPropertiesPage = properties.getProperty("overriden-properties-page", null);

        botTimeout = readIntegerProperty(properties, "bot-timeout", botTimeout, false);
        servicesTimeout = readLongProperty(properties, "services-timeout", servicesTimeout,
                false);
        globalTryCount = readIntegerProperty(properties, "try-count", globalTryCount, false);
        globalCatscanTryCount = readIntegerProperty(properties, "catscan-try-count",
                globalCatscanTryCount, false);

        wikiTranslationPage = properties.getProperty("wiki-translation-page", null);
        customTranslationFile = properties.getProperty("custom-translation-file", null);

        return true;
    }

    @Override
    protected void onInterrupted(InterruptedException exception) {
        log.fatal("Bot execution interrupted", exception);
        System.out.println("Bot execution interrupted");
    }
    
    private static boolean validateService(String service) {
        return CatScanTools.Service.hasService(service);
    }

    private static String validateService(String service, String defaultValue) {
        if (validateService(service)) {
            return service;
        }
        return defaultValue;
    }

    private static boolean validateSelectedService(String service) {
        return validateService(service) || service.equalsIgnoreCase(SERVICE_AUTO);
    }
    
    private static String validateSelectedService(String service, String defaultValue) {
        if (validateSelectedService(service)) {
            return service;
        }
        return defaultValue;
    }

    private boolean loadOverridenProperties(String newpagesTemplate) throws IOException    {
        if (overridenPropertiesPage == null || overridenPropertiesPage.isEmpty()) {
            return false;
        }
        log.info("loading overriden properties from page {}", overridenPropertiesPage);
        String overridenPropertiesText = null;
        try {
            overridenPropertiesText = wiki.getPageText(overridenPropertiesPage);
        } catch (FileNotFoundException e) {            
            log.fatal("Failed to read overriden properties page: {}", overridenPropertiesPage);
            throw e;
        }
        if (overridenPropertiesText == null) {
            log.fatal("Failed to read overriden properties page: {}", overridenPropertiesPage);
            throw new IOException(
                    "Failed to read overriden properties page: " + overridenPropertiesPage);
        }
        if (overridenPropertiesText.trim().isEmpty()) {
            log.info("Default settings page for template \"{}\" is empty", newpagesTemplate);
            return false;
        }
        Map<String, String> options = new HashMap<String, String>();        
        if (!new BotTemplateParser(newpagesTemplate, userNamespace)
                .tryParseTemplate(overridenPropertiesText, options)) {
            log.info("no default settings for this template: {}", newpagesTemplate);
            return false;
        }
        PortalConfig config = new PortalConfig(options);
        DEFAULT_DELIMETER = config.getUnescapedUnquoted(PortalConfig.KEY_SEPARATOR,
                DEFAULT_DELIMETER);
        DEFAULT_FORMAT = config.get(PortalConfig.KEY_FORMAT, DEFAULT_FORMAT);

        List<String> errors = new ArrayList<String>();
        DEFAULT_DEPTH = parseIntegerKeyWithMaxVal(config, PortalConfig.KEY_DEPTH, errors,
                DEFAULT_DEPTH, MAX_DEPTH);

        if (config.hasKey(PortalConfig.KEY_SERVICE)) {
            SELECTED_SERVICE_NAME = validateSelectedService(config.get(PortalConfig.KEY_SERVICE),
                    SELECTED_SERVICE_NAME);
        }

        if (config.hasKey(PortalConfig.KEY_DEFAULT_SERVICE)) {
            DEFAULT_SERVICE_NAME = validateService(config.get(PortalConfig.KEY_DEFAULT_SERVICE),
                    DEFAULT_SERVICE_NAME);
        }

        if (config.hasKey(PortalConfig.KEY_FAST_MODE)) {
            DEFAULT_USE_FAST_MODE = config.get(PortalConfig.KEY_FAST_MODE)
                    .equalsIgnoreCase(STR_YES);
        }

        DEFAULT_HOURS = parseIntegerKey(config, PortalConfig.KEY_HOURS, errors, DEFAULT_HOURS);

        DEFAULT_MAXITEMS = parseIntegerKeyWithMaxVal(config, PortalConfig.KEY_MAX_ITEMS, errors,
                DEFAULT_MAXITEMS, MAX_MAXITEMS);

        DEFAULT_NAMESPACE = parseIntegerKey(config, PortalConfig.KEY_NAMESPACE, errors,
                DEFAULT_NAMESPACE);

        DEFAULT_HEADER = config.getUnescaped(PortalConfig.KEY_HEADER, DEFAULT_HEADER);
        DEFAULT_FOOTER = config.getUnescaped(PortalConfig.KEY_FOOTER, DEFAULT_FOOTER);

        listTypeDefault = config.get(PortalConfig.KEY_TYPE, listTypeDefault).toLowerCase();

        PICTURE_SEARCH_TAGS = config.get(PortalConfig.KEY_IMAGE_SEARCH, PICTURE_SEARCH_TAGS);

        FAIR_USE_IMAGE_TEMPLATES = config.get(PortalConfig.KEY_FAIR_USE_IMAGE_TEMPLATES,
                FAIR_USE_IMAGE_TEMPLATES);

        globalTryCount = parseIntegerKeyWithMaxVal(config, PortalConfig.KEY_TRY_COUNT,
                errors, globalTryCount, MAX_TRY_COUNT);

        globalCatscanTryCount = parseIntegerKeyWithMaxVal(config,
                PortalConfig.KEY_CATSCAN_TRY_COUNT, errors, globalCatscanTryCount,
                MAX_CATSCAN_TRY_COUNT);

        if (!DISCUSSION_PAGES_SETTINGS_WIKI.isEmpty()) {
            log.info("load discussion pages settings from {}", DISCUSSION_PAGES_SETTINGS_WIKI);
            String text = null;
            DiscussionPagesSettings newSettings = null;
            try {
                text = wiki.getPageText(DISCUSSION_PAGES_SETTINGS_WIKI);
            } catch (FileNotFoundException e) {
                log.error("Failed to get settings from wiki page: {}",
                        DISCUSSION_PAGES_SETTINGS_WIKI, e);
            }            
            if (text != null) {
                newSettings = DiscussionPagesSettings.parseFromSettingsString(text);
                if (newSettings == null) {
                    log.error("Failed to parse settings from wiki page: {}",
                            DISCUSSION_PAGES_SETTINGS_WIKI);
                }
            } 
            if (newSettings == null) {
                log.warn("locally provided settings will be used");
            } else {
                DISCUSSION_PAGES_SETTINGS = newSettings;
            }
        }
        return true;
    }
    
    private boolean checkPortalInTaskList(List<String> tasks, String portal) {
        if (tasks == null) {
            return true;
        }
        for (String task : tasks) {                    
            if (portal.startsWith(task)) {
                log.debug("task detected: " + task);
                return true;
            }
        }
        return false;
    }

    protected NirvanaWiki createCommonsWiki() {
        return new NirvanaWiki("commons.wikimedia.org");
    }

    protected ServiceManager createServiceManager() throws BotFatalError {
        log.debug("Create service manager with default service: " + DEFAULT_SERVICE_NAME +
                " and selected service: " + SELECTED_SERVICE_NAME);
        return new ServiceManager(DEFAULT_SERVICE_NAME, SELECTED_SERVICE_NAME, wiki, commons);
    }

    protected BotReporter createReporter(String cacheDir) {
        return new BotReporter(wiki, cacheDir, 1000, true, getVersion(),
                reportPreambulaFile);
    }

    protected SystemTime initSystemTime() {
        return new SystemTime();
    }

    protected void go() throws InterruptedException, BotFatalError {
        cacheDir = outDir + "/" + "cache";

        long startMillis = systemTime.currentTimeMillis();
        commons = createCommonsWiki();
        commons.setMaxLag(maxLag);

        serviceManager = createServiceManager();

        serviceManager.setTimeout(servicesTimeout);
        
        serviceManager.setAfterDowntimeCallback(new AfterDowntimeCallback() {

                @Override
                public void afterDowntime(long downtime) throws BotFatalError {
                    if (downtime > NO_RELOGIN_MIN_DOWNTIME_MS) {
                        try {
                            log.warn("Relogin after downtime of {} seconds", downtime / 1000);
                            serviceManager.getMainWikiService().relogin();
                            log.warn("Relogin was successful");
                        } catch (FailedLoginException | IOException e) {
                            log.error("Failed to relogin. Stopping bot run.", e);
                            throw new BotFatalError(e);
                        }
                    }
                }
            }
        );

        if (!serviceManager.checkServices()) {
            throw new BotFatalError("Some services are down.");
        }

        localizationManager = new LocalizationManager(outDir, cacheDir,
                LocalizationManager.DEFAULT_TRANSLATIONS_DIR, language, customTranslationFile);

        BotReporter reporter = createReporter(cacheDir);
        
        if (wikiTranslationPage != null && !wikiTranslationPage.isEmpty()) {
            try {
                localizationManager.load(wiki, wikiTranslationPage);
            } catch (IOException e) {
                log.error("Failed to load translations. Try to use default translations");
                localizationManager.loadDefault();
            }
        } else {
            localizationManager.loadDefault();
        }
        localizer = Localizer.getInstance();

        BotVariables.init();
        PortalConfig.initStatics();
        DateTools.init(language);

        initLocalizedStrings();

        if (enableStatus) {
            try {
                reporter.updateStartStatus(STATUS_WIKI_PAGE, STATUS_WIKI_TEMPLATE);
            } catch (LoginException | IOException e) {
                log.error(e);                
            }
        }

        try {
            userNamespace = wiki.namespaceIdentifier(Wiki.USER_NAMESPACE);
        } catch (UncheckedIOException e) {
            throw new BotFatalError("Failed to retrieve user namespace");
        }

        // this is a workaround for bad support of keep alive feature in HttpUrlConnection
        // without it, writing of big articles (~500KB) may hang 
        // (when reading answer from server after writing)
        System.setProperty("http.keepAlive", "false"); // adjust HTTP connections

        List<String> tasks = null;
        if (enableTask) {    
            log.info("reading tasks from file: {}", taskListFile);
            try {
                tasks = FileTools.readFileToList(taskListFile, true);
            } catch (IOException e) {
                throw new BotFatalError("Failed to read tasks file: " + taskListFile);
            }
            log.info("loaded tasks: {} total", tasks.size());
            for (String task : tasks) {
                log.debug("task: {}", task);
            }
        }
        boolean fatalProblem = false;
        boolean noServicesOrTimeout = false;
        for (String newpagesTemplate: newpagesTemplates) {

            final long startT = systemTime.currentTimeMillis();
            log.info("Template to check: {}", newpagesTemplate);
            BotTemplateParser botTemplateParser =
                    new BotTemplateParser(newpagesTemplate, userNamespace);

            String []portalNewPagesLists = null;
            int repeat = 2;
            boolean overridden = false;
            while (repeat > 0) {
                try {
                    overridden = loadOverridenProperties(newpagesTemplate);
                } catch (IOException e) {
                    log.error("failed to get page: " + newpagesTemplate);
                    if (!serviceManager.checkServices()) {
                        log.fatal("Some services are down. Stopping...");
                        fatalProblem = true;
                        break;
                    }
                    repeat--;
                    continue;
                }

                try {
                    portalNewPagesLists = wiki.whatTranscludesHere(newpagesTemplate,
                            Wiki.ALL_NAMESPACES);
                } catch (IOException e) {            
                    log.fatal("failed to get portal list");
                    if (!serviceManager.checkServices()) {
                        log.fatal("Some services are down. Stopping...");
                        fatalProblem = true;
                        break;
                    }
                    repeat--;
                    continue;
                }
                break;
            }
            if (repeat == 0 || fatalProblem) {
                break;
            }
            if (overridden) {
                try {
                    log.debug("Update service manager with default service: {} and "
                            + "selected service: {}", DEFAULT_SERVICE_NAME, SELECTED_SERVICE_NAME);
                    serviceManager.updateCatScan(DEFAULT_SERVICE_NAME, SELECTED_SERVICE_NAME);
                } catch (BotFatalError e1) {
                    log.fatal(e1);
                    return;
                }
            }
            log.info("loaded portal/project settings: {} items", portalNewPagesLists.length);
            Arrays.sort(portalNewPagesLists);
            
            int i = 0;  // текущий портал
            int t = 0;  // количество проверенных порталов
            int tryNumber = 1;
            boolean retry = false;
            int tryCount = globalTryCount;
            ReportItem reportItem = null;
            String portalName = null;

            while (i < portalNewPagesLists.length) {
                if (!noServicesOrTimeout && botTimeout > 0 &&
                        (systemTime.currentTimeMillis() - startMillis > botTimeout)) {
                    log.debug("Bot work time exceeded allowed maximum of {} ms. Stopping bot.",
                            botTimeout);
                    noServicesOrTimeout = true;
                }
                log.debug("start processing portal No: " + i);
                if (retry) {
                    retry = false;
                    log.info("Retrying portal ({}): {}", tryNumber, portalName);        
                    tryNumber++;
                    reportItem.restart();
                } else {
                    tryNumber = 1;
                    portalName = portalNewPagesLists[i];
                            
                    int percent = (i * 100) / portalNewPagesLists.length;
                    log.info(String.format(
                            "=[ %1$d/%2$d: %3$d%% ]="
                            + "==================================================================",
                            i + 1, portalNewPagesLists.length, percent));
                    log.info("processing portal: {}", portalName);
                    reportItem = new ReportItem(newpagesTemplate, portalName);
                    if (reporter != null) reporter.add(reportItem);
                    reportItem.start();
                }
                
                if (!checkPortalInTaskList(tasks, portalName)) {
                    log.info("SKIP portal: {}", portalName);
                    reportItem.skip();
                    i++;
                    continue;
                }
                if (noServicesOrTimeout) {
                    reportItem.error(BotError.FATAL);
                    i++;
                    continue;
                }
                if (tryNumber == 1) t++;
                if (t < START_FROM) {
                    log.info("SKIP portal: {}", portalName);
                    reportItem.skip();
                    i++;
                    continue;
                }

                if (tryNumber == 1) reporter.portalChecked();
                boolean mayBeSomeServiceProblem = false;
                try {
                    serviceManager.timeToFixProblems();
                    
                    String portalSettingsText = wiki.getPageText(portalName);

                    if (debugMode || BasicBot.DEBUG_BUILD) {
                        FileTools.dump(portalSettingsText, dumpDir, portalName + ".settings.txt");
                    }

                    if (!wiki.allowEditsByCurrentBot(portalSettingsText)) {
                        log.info("SKIP portal: {}", portalName);
                        reportItem.skip();
                        i++;
                        continue;
                    }
                    
                    Map<String, String> parameters = new HashMap<String, String>();
                    if (botTemplateParser.tryParseTemplate(portalSettingsText, parameters)) {
                        log.info("validate portal settings OK");                    
                        logPortalSettings(parameters);
                        NewPagesData data = new NewPagesData();
                        data.portalSettingsPage = portalName;
                        data.portalSettingsText = portalSettingsText;
                        if (createPortalModule(botTemplateParser, parameters, data)) {
                            tryCount = data.param.tryCount;
                            CatScanTools.setMaxRetryCount(data.param.catscanTryCount);
                            if ((enabledTypes.contains(TYPE_ALL) ||
                                    enabledTypes.contains(data.type)) &&
                                    data.portalModule != null) {
                                if (debugMode || !portalName.contains("ValidParam") ||
                                        !DEBUG_BUILD) {
                                    reporter.portalProcessed(tryNumber);
                                    reportItem.processed(tryNumber);
                                    if (data.portalModule.update(wiki, reportItem, COMMENT)) {
                                        reporter.portalUpdated();
                                        reportItem.updated();
                                    }
                                    wiki.dumpCookies();
                                    commons.dumpCookies();
                                    if (UPDATE_PAUSE > 0) systemTime.sleep(UPDATE_PAUSE);
                                }                                    
                            } else {
                                reportItem.skip();                        
                                log.info("SKIP portal: {}", portalName); 
                            }                            
                        } else {
                            tryCount = data.param.tryCount;
                            log.warn("portal module not created");
                            reportItem.error(BotError.SETTINGS_ERROR);
                        }

                        if (!data.errors.isEmpty()) {
                            log.warn("Errors occured during checking settings");
                            for (String str:data.errors) {
                                log.info(str);
                            }
                            reportItem.errors = data.errors.size();
                            String errorText = StringUtils.join(data.errors, "\n");
                            FileTools.dump(errorText, dumpDir, portalName + ".err");
                            if (ERROR_NOTIFICATION) {
                                log.info("Portal has errors. " +
                                        "Try send error message to discussion page.");
                                sendErrorNotification(portalName, data.errors);
                            }
                        }
                    } else {
                        reportItem.settingsValid = false;
                        reportItem.error(BotError.SETTINGS_ERROR);
                        log.error("validate portal settings FAILED");
                    }

                } catch (IllegalArgumentException | IndexOutOfBoundsException |
                        NullPointerException | java.util.zip.ZipException |
                        UnsupportedOperationException e) {
                    // includes ArrayIndexOfBoundsException
                    log.error(e.toString()); 
                    if (tryNumber < tryCount) {
                        log.info("RETRY AGAIN");
                        retry = true;
                    } else {
                        reporter.portalError();
                        reportItem.error(BotError.BOT_ERROR);
                        log.error("OOOOPS!!!", e); // print stack trace
                    }    
                } catch (DangerousEditException e) {
                    log.error(e.toString()); 
                    if (tryNumber < tryCount) {
                        log.info("RETRY AGAIN");
                        retry = true;
                    } else {
                        reporter.portalError();
                        reportItem.error(BotError.INTERNAL_ERROR);
                    }
                } catch (ServiceError | IOException e) {
                    log.error(e.toString());
                    
                    mayBeSomeServiceProblem = true;

                    if (tryNumber < tryCount) {
                        log.info("RETRY AGAIN");
                        retry = true;
                    } else {
                        reporter.portalError();
                        if (e instanceof ServiceError) {
                            reportItem.error(BotError.SERVICE_ERROR);
                        } else {
                            reportItem.error(BotError.IO_ERROR);
                        }
                        log.error("OOOOPS!!!", e); // print stack trace
                    }
                } catch (InvalidLineFormatException e) {
                    log.error(e.toString());
                    reporter.portalError();
                    reportItem.error(BotError.SETTINGS_ERROR);
                    if (ERROR_NOTIFICATION) {
                        log.info("Portal has errors. " +
                                "Try send error message to discussion page.");
                        try {
                            sendErrorNotification(portalName, e.toString());
                        } catch (LoginException e1) {
                            mayBeSomeServiceProblem = true;
                        } catch (IOException e1) {
                            mayBeSomeServiceProblem = true;
                        }
                    }
                } catch (ArchiveUpdateFailure e) {
                    log.error("Failed to update archive: {}", e.getCause());
                    reporter.portalError();
                    reportItem.error(BotError.ARCHIVE_UPDATE_ERROR);
                } catch (LoginException e) {
                    log.fatal(e.toString());
                    fatalProblem = true;
                    break;
                } catch (RuntimeException e) {
                    log.error(e.toString());
                    if (tryNumber < tryCount) {
                        if (e.getMessage().equals("Unexpected Dislogin")) {
                            try {
                                log.info("Dislogin detected. RETRY AGAIN after relogin...");
                                wiki.relogin();
                                retry = true;
                            } catch (FailedLoginException | IOException e1) {
                                log.fatal(e.toString());
                                reporter.portalError();
                                reportItem.error();
                                mayBeSomeServiceProblem = true;
                                serviceManager.getMainWikiService().setNeedsRelogin(true);
                                log.error("OOOOPS!!!", e);  // print stack trace
                            }
                        } else {
                            log.info("RETRY AGAIN");
                            retry = true;    
                        }
                    } else {
                        reporter.portalError();
                        reportItem.error();
                        //e.printStackTrace();
                        log.error("OOOOPS!!!", e); // print stack trace
                    }
                } catch (Exception e) {
                    log.error(e.toString());
                    if (tryNumber < tryCount) {
                        log.info("RETRY AGAIN");
                        retry = true;
                    } else {
                        reporter.portalError();
                        reportItem.error();
                        //e.printStackTrace();
                        log.error("OOOOPS!!!", e); // print stack trace
                    }    
                } catch (Error e) {
                    log.error(e.toString());
                    // Allow exceptions coming from unit tests
                    if (e.getClass().getName().equals("org.junit.ComparisonFailure")
                            || e.getClass().getName().equals("java.lang.AssertionError")) {
                        throw e;
                    }
                    if (tryNumber < tryCount) {
                        log.info("RETRY AGAIN");
                        retry = true;
                    } else {
                        reporter.portalError();
                        reportItem.error();
                        //e.printStackTrace();
                        log.error("OOOOPS!!!", e); // print stack trace
                    }    
                }
                reportItem.end();
                if (STOP_AFTER > 0 && t >= STOP_AFTER) break;
                if (!retry) i++;
                if (mayBeSomeServiceProblem) {
                    if (!serviceManager.checkServices()) {
                        log.fatal("Some services are down. Stopping...");
                        if (serviceManager.getMainWikiService().isOk() == Status.OK) {
                            noServicesOrTimeout = true;
                            continue;
                        } else {
                            fatalProblem = true;
                            break;
                        }
                    }
                }
            }
            reportItem.end();
            LocalDateTime endTime = systemTime.nowLdt();
            long endT = systemTime.currentTimeMillis();
            reporter.addToTotal(portalNewPagesLists.length);

            String finishStatus = (fatalProblem || noServicesOrTimeout) ? "STOPPED" : "FINISHED";
            log.info("TEMPLATE {} at {}", finishStatus, DateTools.printTime(endTime));
            log.info("WORK TIME for TEMPLATE: {}", BotReporter.printTimeDiff(endT - startT));
            if (fatalProblem) {
                break;
            }
        }

        reporter.logStatus();

        updateDetailedReport(reporter);

        reporter.botFinished(false);

        updateStatus(reporter);
        updateTranslations();

        reporter.logEndStatus();
    }

    private boolean ensureWikiIsOk() throws InterruptedException {
        if (serviceManager.getMainWikiService().isOk() != Status.OK) {
            log.error("Wiki site is not available.");
            return false;
        }
        try {
            serviceManager.getMainWikiService().reloginIfNeed();
        } catch (FailedLoginException | IOException e) {
            log.error("Failed to relogin to Wiki site: {}", e);
            return false;
        }
        return true;
    }

    private void updateDetailedReport(BotReporter reporter) throws InterruptedException {
        if (!enableReport) {
            return;
        }
        if (StringUtils.containsIgnoreCase(REPORT_FORMAT, "txt")) {                    
            reporter.reportTxt(outDir + "/" + REPORT_FILE_NAME);
        }
        if (!StringUtils.containsIgnoreCase(REPORT_FORMAT, "wiki")) {
            return;
        }
        if (!ensureWikiIsOk()) {
            log.warn("Detailed report update will be skipped");
            return;
        }
        reporter.reportWiki(REPORT_WIKI_PAGE, startNumber == 1, detailedReportWikiVerbosity);
    }

    private void updateStatus(BotReporter reporter) throws InterruptedException {
        if (!enableStatus) {
            return;
        }
        if (!ensureWikiIsOk()) {
            log.warn("Status report update will be skipped");
            return;
        }
        reporter.updateEndStatus(STATUS_WIKI_PAGE, STATUS_WIKI_TEMPLATE);
    }

    private void updateTranslations() throws InterruptedException {
        if (wikiTranslationPage == null || wikiTranslationPage.isEmpty()) {
            return;
        }
        if (!ensureWikiIsOk()) {
            log.warn("Updating translations will be skipped");
            return;
        }
        try {
            localizationManager.refreshWikiTranslations(wiki, wikiTranslationPage);
        } catch (LoginException | IOException e) {
            log.error("Failed to update translations: {}", e);
        }
    }

    private void sendErrorNotification(String portalSettingsPage, String error)
            throws IOException, LoginException {
        ArrayList<String> errors = new ArrayList<>();
        errors.add(error);
        sendErrorNotification(portalSettingsPage, errors);
    }

    private void sendErrorNotification(String portalSettingsPage, ArrayList<String> errors)
            throws IOException, LoginException {
        String portal = WikiUtils.getPortalFromSettingsSubPage(portalSettingsPage);
        log.debug("Get talk page for: {}", portal);
        String portalTalkPage = wiki.getTalkPage(portal);
        StringBuilder sb = new StringBuilder();
        String title = localizer.localize(ERROR_NOTIFICATION_TITLE);
        sb.append("== ").append(title).append(" ==\n");
        String format = localizer.localize(ERROR_NOTIFICATION_TEXT_BEGIN);
        sb.append(String.format(format, portalSettingsPage)).append('\n');
        for (String error: errors) {
            sb.append("* ").append(error).append('\n');
        }
        sb.append(localizer.localize(ERROR_NOTIFICATION_TEXT_END)).append(" ");
        sb.append(localizer.localize(ERROR_NOTIFICATION_TEXT_ROBOT));
        if (!language.equals(LocalizationManager.DEFAULT_LANG)) {
            // Add hint that message may be localized.
            String hint;
            if (wikiTranslationPage != null && !wikiTranslationPage.isEmpty()) {
                format = localizer.localize(ERROR_NOTIFICATION_TEXT_LOCALISATION_HINT2);
                hint = String.format(format, wikiTranslationPage);
            } else {
                hint = localizer.localize(ERROR_NOTIFICATION_TEXT_LOCALISATION_HINT);
            }
            sb.append('\n').append(hint);
        }        
        String summary = localizer.localize(ERROR_NOTIFICATION_SUMMARY);
        wiki.addTopicToDiscussionPage(portalTalkPage, sb.toString(), true, true, summary);
    }

    private void initLocalizedStrings() {
        listTypeDefault = LIST_TYPE_NEW_PAGES;
    }

    @SuppressWarnings("unchecked")
    protected boolean createPortalModule(BotTemplateParser botTemplateParser,
            Map<String, String> options, NewPagesData data) {
        log.debug("Scan portal settings...");

        PortalParam param = new PortalParam();
        data.param = param;
        param.lang = language;
        PortalConfig config = new PortalConfig(options);

        String type = null;
        if (!config.hasKey(PortalConfig.KEY_TYPE)) {
            String format = localizer.localize(ERROR_MISS_PARAMETER);
            data.errors.add(String.format(format, PortalConfig.KEY_TYPE));
            type = listTypeDefault;
        } else {
            type = config.get(PortalConfig.KEY_TYPE).toLowerCase();
        }    
        log.debug("Тип: " + type);
        data.type = type;

        param.categories = OptionsUtils.readStringListOption(options, PortalConfig.KEY_CATEGORIES,
                true, unescapeCategorylist);
        if (config.hasKey(PortalConfig.KEY_CATEGORY)) {
            param.categories = ListUtils.union(param.categories,
                    Arrays.asList(config.get(PortalConfig.KEY_CATEGORY)));
        }        

        param.categoriesToIgnore = OptionsUtils.readStringListOption(options,
                PortalConfig.KEY_IGNORE, true, unescapeCategorylist);

        param.categoryGroups = multiOptionToArray(options, PortalConfig.KEY_CATEGORIES, 1,
                PortalParam.MAX_CAT_GROUPS);
        param.categoryToIgnoreGroups = multiOptionToArray(options, PortalConfig.KEY_IGNORE, 1,
                PortalParam.MAX_CAT_GROUPS);

        param.page = config.get(PortalConfig.KEY_PAGE, "");

        param.service = serviceManager.getActiveService();
        if (config.hasKey(PortalConfig.KEY_SERVICE)) {
            String service = config.get(PortalConfig.KEY_SERVICE);

            if (service.equals(STR_DEFAULT) || 
                    service.equals(PARAM_VALUE_DEFAULT_EN) ||
                    service.equals(SERVICE_AUTO) ||
                    service.equals(STR_AUTO) ||
                    service.equalsIgnoreCase(SERVICE_AUTO)) {
                // do nothing - it's all done
            } else {
                param.service = CatScanTools.Service.getServiceByName(service); 
                if (param.service == null) {
                    param.service = serviceManager.getActiveService();
                    log.warn(String.format(ERROR_INVALID_PARAMETER_EN, PortalConfig.KEY_SERVICE,
                            service));
                    String format = localizer.localize(ERROR_INVALID_PARAMETER);
                    data.errors.add(String.format(format, PortalConfig.KEY_SERVICE, service));
                }
            }
        }

        if (config.hasKey(PortalConfig.KEY_ARCHIVE)) {
            param.archSettings = new ArchiveSettingsParser(localizer).parse(config);
            param.archSettings.parseCount = DEFAULT_ARCHIVE_PARSE_LINES_MAX_COUNT;
            data.errors.addAll(param.archSettings.errors);
        }

        param.prefix = config.get(PortalConfig.KEY_PREFIX, "");

        param.bot = true;
        param.minor = true;
        if (config.hasKey(PortalConfig.KEY_MARK_EDITS)) {
            String mark = config.get(PortalConfig.KEY_MARK_EDITS).toLowerCase();
            if (mark.equals(STR_NO)) {
                param.bot = false;
                param.minor = false;
            } else {
                if (!mark.contains(STR_BOT)) {
                    param.bot = false;
                }
                if (!mark.contains(STR_SMALL)) {
                    param.minor = false;
                }                
            }
        }

        param.fastMode = DEFAULT_USE_FAST_MODE;
        if (config.hasKey(PortalConfig.KEY_FAST_MODE)) {
            param.fastMode = config.get(PortalConfig.KEY_FAST_MODE).equalsIgnoreCase(STR_YES);
        }

        param.ns = DEFAULT_NAMESPACE;
        if (config.hasKey(PortalConfig.KEY_NAMESPACE)) {
            final String key = PortalConfig.KEY_NAMESPACE;
            final String val = config.get(PortalConfig.KEY_NAMESPACE);
            try {
                param.ns = Integer.parseInt(val);
            } catch (NumberFormatException e) {
                log.warn(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING_EN, key, val));
                String format = localizer.localize(ERROR_PARSE_INTEGER_FORMAT_STRING);
                data.errors.add(String.format(format, key, val));
            }
        }

        param.header = config.getUnescaped(PortalConfig.KEY_HEADER, DEFAULT_HEADER);
        param.footer = config.getUnescaped(PortalConfig.KEY_FOOTER, DEFAULT_FOOTER);

        if (config.hasKey(PortalConfig.KEY_TEMPLATES)) {
            String option = config.get(PortalConfig.KEY_TEMPLATES);
            // Этот хак корректирует различие в задании параметра "шаблоны"
            // между разными версиями ботов ClaymoreBot и NirvanaBot
            // NirvanaBot использует более human-readable формат: через запятую
            log.debug("templates param detected!!!");
            if (options.get("BotTemplate").contains("Claymore")) {
                log.debug("use hack: replace \n by ,");
                option = option.replace("\\n", ",");
            }
            CatScanTools.EnumerationType enumType = CatScanTools.EnumerationType.OR;
            if (option.startsWith("!") || option.startsWith("^")) {
                option = option.substring(1);
                enumType = CatScanTools.EnumerationType.NONE;
            }
            if (!option.isEmpty()) {
                String sep = ",";
                if (option.contains(";")) {
                    sep = ";";
                    if (enumType != CatScanTools.EnumerationType.NONE) {
                        enumType = CatScanTools.EnumerationType.AND;
                    }
                }
                List<String> templates = OptionsUtils.optionToList(option, true, sep);
                if (!templates.isEmpty()) {
                    param.templateFilter = new SimpleTemplateFilter(templates, enumType);
                }
            }
        }

        if (config.hasKey(PortalConfig.KEY_TEMPLATES_WITH_PARAM)) {
            String option = config.get(PortalConfig.KEY_TEMPLATES_WITH_PARAM);
            log.debug("param 'templates with parameter' detected");
            param.templateFilter = new ComplexTemplateFilter(
                    parseTemplatesWithData(option, data.errors),
                    EnumerationType.OR);
        }

        param.format = config.get(PortalConfig.KEY_FORMAT, DEFAULT_FORMAT);
        if (param.format.contains(BotVariables.AUTHOR)) {
            if (isTypePages(type) || isTypeDiscussedPages(type)) {
                String format = localizer.localize(ERROR_AUTHOR_PARAMETER_INVALID_USAGE);
                data.errors.add(String.format(format, PortalConfig.KEY_FORMAT,
                        BotVariables.AUTHOR));
                param.format.replace(BotVariables.AUTHOR, "");
            }
        }

        param.depth = parseIntegerKeyWithMaxVal(config, PortalConfig.KEY_DEPTH, data.errors,
                DEFAULT_DEPTH, MAX_DEPTH);

        param.hours = parseIntegerKeyWithMaxVal(config, PortalConfig.KEY_HOURS, data.errors,
                DEFAULT_HOURS, param.service.maxHours);

        int defaultMaxItems = DEFAULT_MAXITEMS;
        if (isTypeWithUnlimitedItems(type)) {
            defaultMaxItems = MAX_MAXITEMS;
        }
        param.maxItems = parseIntegerKeyWithMaxVal(config, PortalConfig.KEY_MAX_ITEMS, data.errors,
                defaultMaxItems, MAX_MAXITEMS);

        param.updatesPerDay = parseIntegerKeyWithMaxVal(config, PortalConfig.KEY_UPDATES_PER_DAY,
                data.errors, DEFAULT_UPDATES_PER_DAY, MAX_UPDATES_PER_DAY);

        param.delimeter = config.getUnescapedUnquoted(PortalConfig.KEY_SEPARATOR,
                DEFAULT_DELIMETER);

        param.imageSearchTags = config.get(PortalConfig.KEY_IMAGE_SEARCH, PICTURE_SEARCH_TAGS);

        param.fairUseImageTemplates = config.get(PortalConfig.KEY_FAIR_USE_IMAGE_TEMPLATES,
                FAIR_USE_IMAGE_TEMPLATES);

        param.tryCount = parseIntegerKeyWithMaxVal(config, PortalConfig.KEY_TRY_COUNT,
                data.errors, globalTryCount, MAX_TRY_COUNT);

        param.catscanTryCount = parseIntegerKeyWithMaxVal(config,
                PortalConfig.KEY_CATSCAN_TRY_COUNT,
                data.errors, globalCatscanTryCount, MAX_CATSCAN_TRY_COUNT);

        if (!validateParams(param,data.errors)) {
            return false;
        }
        
        if (!itsTimeToUpdate(param)) {
            // There is no errors but we should not update this portal now
            return true;
        }

        PageFormatter pageFormatter = new PageFormatter(botTemplateParser,
                param,
                data.portalSettingsPage, data.portalSettingsText,
                new BotGlobalSettings(), wiki, systemTime, cacheDir);

        if (isTypeNewPages(type)) {
            data.portalModule = new NewPages(param, pageFormatter, systemTime);
        } else if (isTypePages(type)) {
            data.portalModule = new Pages(param, pageFormatter, systemTime);
        } else if (type.equals(LIST_TYPE_NEW_PAGES_WITH_IMAGES_IN_CARD) ||
                type.equals(LIST_TYPE_NEW_PAGES_WITH_IMAGES_IN_CARD_OLD)) {
            data.portalModule = new NewPagesWithImages(param, pageFormatter, systemTime, commons,
                    new ImageFinderInCard(wiki, commons, param.imageSearchTags));
        } else if (type.equals(LIST_TYPE_NEW_PAGES_WITH_IMAGES_IN_TEXT) ||
                type.equals(LIST_TYPE_NEW_PAGES_WITH_IMAGES_IN_TEXT_OLD) ) {
            data.portalModule = new NewPagesWithImages(param, pageFormatter, systemTime, commons,
                    new ImageFinderInBody(wiki, commons));
        } else if (type.equals(LIST_TYPE_NEW_PAGES_WITH_IMAGES) ||
                type.equals(LIST_TYPE_NEW_PAGES_WITH_IMAGES_OLD)) {
            data.portalModule = new NewPagesWithImages(param, pageFormatter, systemTime, commons,
                    new ImageFinderUniversal(wiki, commons, param.imageSearchTags));
        } else if (type.equals(LIST_TYPE_NEW_PAGES_7_DAYS) ||
                type.equals(LIST_TYPE_NEW_PAGES_7_DAYS_OLD)) {
            data.portalModule = new NewPagesWeek(param, pageFormatter, systemTime);
        } else if (isTypeDiscussedPages(type)) {
            if (DISCUSSION_PAGES_SETTINGS == null) {
                String err = localizer.localize(ERROR_DISCUSSED_PAGES_SETTINGS_NOT_DEFINED);
                data.errors.add(err);
            } else {
                data.portalModule = new DiscussedPages(param, pageFormatter, systemTime,
                        DISCUSSION_PAGES_SETTINGS);
            }
        } else {
            String format = localizer.localize(ERROR_INVALID_TYPE);
            data.errors.add(String.format(format, type, PortalConfig.KEY_TYPE));
        }

        log.debug("portal settings init finished");
        return (data.portalModule != null);
    }

    private List<TemplateFindItem> parseTemplatesWithData(String option, List<String> errors) {
        List<TemplateFindItem> templateDataItems = new ArrayList<>();
        List<String> items = OptionsUtils.optionToList(option, true, ",");
        for (String item : items) {
            try {
                templateDataItems.add(TemplateFindItem.parseTemplateFindData(item));
            } catch (BadFormatException e) {
                String format = ERROR_INVALID_PARAMETER + ERROR_PARAMETER_NOT_ACCEPTED;
                errors.add(String.format(format, PortalConfig.KEY_TEMPLATES_WITH_PARAM, item));
            }
        }
        return templateDataItems;
    }

    private boolean isTypeWithUnlimitedItems(String type) {
        return isTypePages(type) || isTypeDiscussedPages(type);
    }

    private boolean isTypeNewPages(String type) {
        return type.equals(LIST_TYPE_NEW_PAGES) || type.equals(LIST_TYPE_NEW_PAGES_OLD);
    }

    private boolean isTypePages(String type) {        
        return type.equals(LIST_TYPE_WATCHLIST) || type.equals(LIST_TYPE_PAGES) ||
                type.equals(LIST_TYPE_PAGES_WITH_TEMPLATES) || type.equals(LIST_TYPE_PAGES_OLD);
    }

    private boolean isTypeDiscussedPages(String type) {
        return type.equals(LIST_TYPE_DISCUSSED_PAGES) || type.equals(LIST_TYPE_DISCUSSED_PAGES2) ||
                type.equals(LIST_TYPE_DISCUSSED_PAGES_OLD);
    }

    private int parseIntegerKey(PortalConfig config, String key, List<String> errors,
            int defaultVal) {
        int val = defaultVal;
        if (config.hasKey(key)) {
            String strVal = config.get(key);
            try {
                val = Integer.parseInt(strVal);
            } catch (NumberFormatException e) {
                log.warn(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING_EN, key, strVal));
                String format = localizer.localize(ERROR_PARSE_INTEGER_FORMAT_STRING);
                errors.add(String.format(format, key, strVal));
            }
        }
        return val;
    }

    private int parseIntegerKeyWithMaxVal(PortalConfig config, String key, List<String> errors,
            int defaultVal, int maxVal) {
        int val = defaultVal;
        if (config.hasKey(key)) {
            String strVal = config.get(key);
            try {
                val = Integer.parseInt(strVal);
            } catch (NumberFormatException e) {
                log.warn(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING_EN, key, strVal));
                String format = localizer.localize(ERROR_PARSE_INTEGER_FORMAT_STRING);
                errors.add(String.format(format, key, strVal));
            }
            if (val > maxVal) {
                String format = localizer.localize(ERROR_INTEGER_TOO_BIG_STRING);
                errors.add(String.format(format, key, strVal, maxVal));
                val = maxVal;
            }
        }
        return val;
    }

    private boolean validateParams(PortalParam param, List<String> errors) {
        boolean retval = true;
        if (param.categories.isEmpty()) {
            String format = localizer.localize(ERROR_ABSENT_PARAMETER);
            log.warn(String.format(format, PortalConfig.KEY_CATEGORIES));
            errors.add(String.format(format, PortalConfig.KEY_CATEGORIES));
            retval = false;
        }
        if (param.page.isEmpty()) {
            String format = localizer.localize(ERROR_ABSENT_PARAMETER);
            log.warn(String.format(format, PortalConfig.KEY_PAGE));
            errors.add(String.format(format, PortalConfig.KEY_PAGE));
            retval = false;
        }
        return retval;
    }
    
    private boolean itsTimeToUpdate(PortalParam param) {
        if (param.updatesPerDay > DEFAULT_STARTS_PER_DAY) {
            param.updatesPerDay = DEFAULT_STARTS_PER_DAY;
        }
        
        if (startNumber != 1) {
            int freq = DEFAULT_STARTS_PER_DAY / param.updatesPerDay;
            if ((startNumber - 1) % freq != 0) {
                return false;
            }
        }
        return true;
    }

    protected static List<List<String>> multiOptionToArray(
            Map<String, String> options, String key, int start, int end) {
        ArrayList<List<String>> listlist = new ArrayList<List<String>>(end - start + 1);
        String keyNumbered;
        for (int i = start; i <= end; i++) {
            keyNumbered = key + String.valueOf(i);
            List<String> list = OptionsUtils.readStringListOption(options, keyNumbered, true,
                    unescapeCategorylist);
            listlist.add(list);
        }
        return listlist;
    }
}
