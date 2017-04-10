/**
 *  @(#)NirvanaBot.java 1.16 10.01.2017
 *  Copyright © 2011 - 2017 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_ABOVE;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_AUTO;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_BELOW;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_BOT;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_DEFAULT;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_DELETE;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_ENUMERATE_WITH_HASH;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_ENUMERATE_WITH_HASH2;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_ENUMERATE_WITH_HTML;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_ENUMERATE_WITH_HTML2;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_ENUMERATE_WITH_HTML_GLOBAL;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_ENUMERATE_WITH_HTML_GLOBAL2;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_LEAVE;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_MARK;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_NEW_TITLE;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_NO;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_OLD_TITLE;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_REMOVE_DUPLICATES;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_SMALL;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_SORT;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_TOSORT;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_YES;

import org.wikipedia.Wiki;
import org.wikipedia.nirvana.BotUtils;
import org.wikipedia.nirvana.DateTools;
import org.wikipedia.nirvana.FileTools;
import org.wikipedia.nirvana.NirvanaBasicBot;
import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.ServiceError;
import org.wikipedia.nirvana.WikiTools;
import org.wikipedia.nirvana.WikiTools.EnumerationType;
import org.wikipedia.nirvana.archive.ArchiveSettings;
import org.wikipedia.nirvana.archive.ArchiveSettings.Enumeration;
import org.wikipedia.nirvana.archive.ArchiveSettings.Period;
import org.wikipedia.nirvana.localization.LocalizationManager;
import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.nirvanabot.imagefinder.ImageFinderInBody;
import org.wikipedia.nirvana.nirvanabot.imagefinder.ImageFinderInCard;
import org.wikipedia.nirvana.nirvanabot.imagefinder.ImageFinderUniversal;
import org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService.Status;
import org.wikipedia.nirvana.nirvanabot.templates.ComplexTemplateFilter;
import org.wikipedia.nirvana.nirvanabot.templates.SimpleTemplateFilter;
import org.wikipedia.nirvana.nirvanabot.templates.TemplateFindItem;

import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.login.LoginException;

/**
 * @author kin
 *
 */
public class NirvanaBot extends NirvanaBasicBot{
	static final String SERVICE_AUTO = "auto";
    private static final String TYPE_ALL = "all";
	private String userNamespace;
	public static final String DEPTH_SEPARATOR = "|";
    public static final String ADDITIONAL_SEPARATOR = "#";
    
	private static int START_FROM = 0;
	private static int STOP_AFTER = 0;
	public static int UPDATE_PAUSE = 1000;
	
	public static String TIME_FORMAT = "short"; // short,long
	
	private boolean TASK = false;
	private String TASK_LIST_FILE = "task.txt";
	
	private String newpagesTemplates[] = null;

    private static final String PARAM_VALUE_DEFAULT_EN = "default";

    public static final String ERROR_PARSE_INTEGER_FORMAT_STRING_EN =
            "Error when parsing integer parameter \"%1$s\" integer value %2$s";
    private static final String ERROR_PARSE_INTEGER_FORMAT_STRING =
            "Ошибка при чтении параметра \"%1$s\". Значение %2$s не распознано как число.";
    private static final String ERROR_INTEGER_TOO_BIG_STRING =
            "Значение параметра \"%1$s\" слишком велико (%2$s). " +
            "Использовано максимальное значение (%3$s). Укажите разумную величину.";
    @SuppressWarnings("unused")
    private static final String ERROR_NOTIFICATION_TEXT =
            "При проверке параметров были обнаружены ошибки. " + 
            "%1$s Напишите [[%1$s|мне]], если нужна помощь в настройке параметров. ~~~~";
    private static final String ERROR_PARAMETER_HAS_MULTIPLE_VALUES =
            "Для параметра \"%1$s\" дано несколько значений. Использовано значение по умолчанию.";
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
    private static final String ERROR_PARAMETER_MISSING_VARIABLE =
            "В параметре бота \"%1$s\" не задан ключ с переменным значением. " +
            "Значение этого параметра отклонено.";
    private static final String ERROR_SAME_PERIOD =
            "Параметр \"%1$s\" и параметр \"%2$s\" имеют одинаковый период повторения: %3$s";
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

    private static String DEFAULT_SERVICE_NAME = WikiTools.Service.PETSCAN.name();
	private static String SELECTED_SERVICE_NAME = SERVICE_AUTO;

	private static boolean DEFAULT_USE_FAST_MODE = true;
	private static boolean ERROR_NOTIFICATION = false;
	private static String COMMENT = "обновление";
	
	private static boolean GENERATE_REPORT = false;
	private static boolean UPDATE_STATUS = false;
	private static String REPORT_FILE_NAME = "report.txt";
	private static String REPORT_WIKI_PAGE = "Участник:NirvanaBot/Новые статьи/Отчёт";
	private static String STATUS_WIKI_PAGE = "Участник:NirvanaBot/Новые статьи/Статус";
	private static String STATUS_WIKI_TEMPLATE = "Участник:NirvanaBot/Новые статьи/Отображение статуса";
	private static String REPORT_FORMAT = "txt";
    private static String DEFAULT_FORMAT = "* [[%1$s]]";
	public static String DEFAULT_FORMAT_STRING = "* [[%1$s]]";

	private String listTypeDefault = LIST_TYPE_NEW_PAGES;

    private String enabledTypesString = TYPE_ALL;
    Set<String> enabledTypes;

	private static String DEFAULT_DELIMETER = "\n";

    /**
     * This is how many times we will retry each portal page update if it fails.
     * 0 means that we will not retry.
     */
    private int retryMax = 1;

	private static String overridenPropertiesPage = null;
	
	private static String PICTURE_SEARCH_TAGS = "image file,Фото,портрет,Изображение,Файл,File";
    // Example templates for Russian Wikipedia:
    // Обоснование добросовестного использования, Disputed-fairuse, ОДИ, Несвободный файл/ОДИ
    private static String FAIR_USE_IMAGE_TEMPLATES = "";
    private static DiscussionPagesSettings DISCUSSION_PAGES_SETTINGS = DiscussionPagesSettings.EMPTY;
	private static String DISCUSSION_PAGES_SETTINGS_WIKI = null;
	
	private static int DEFAULT_NAMESPACE = 0;
	
	private static String DEFAULT_HEADER = null;
	private static String DEFAULT_FOOTER = null;
	private static String DEFAULT_MIDDLE = null;
	private static PortalParam.Deleted DEFAULT_DELETED_FLAG = PortalParam.Deleted.DONT_TOUCH;
	private static int DEFAULT_RENAMED_FLAG = PortalParam.RENAMED_NEW;
	private static int DEFAULT_PARSE_COUNT = -1;
	
	private static int DEFAULT_UPDATES_PER_DAY = 1;
	private static int MAX_UPDATES_PER_DAY = 4;
	
	private static int DEFAULT_STARTS_PER_DAY = 1;

    private static final String DEFAULT_REPORT_PREAMBULA_FILE = "templates/Report_preambula.txt";
    private String reportPreambulaFile = DEFAULT_REPORT_PREAMBULA_FILE;


	private int startNumber = 1;

    @Deprecated
    private boolean longTask = true;

	private NirvanaWiki commons = null;
	
	private ServiceManager serviceManager;

    private int botTimeout = 0;

    private int servicesTimeout = 0;

    private String wikiTranslationPage = null;
    private String customTranslationFile = null;

    private Localizer localizer;

    public void setRetryCount(int count) {
        if (count < 0) throw new RuntimeException("retry count must be 0 or positive value");
        retryMax = count;
    }

	private static class NewPagesData {
		ArrayList<String> errors;
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
		IO_ERROR,
		SETTINGS_ERROR,
        FATAL
	};
	
	public static String getDefaultFooter() {
		return DEFAULT_FOOTER;
	}
	
	public static String getDefaultHeader() {
		return DEFAULT_HEADER;
	}
	
	public static String getDefaultMiddle() {
		return DEFAULT_MIDDLE;
	}

    private static final String VERSION = "v1.16";

	public static String PROGRAM_INFO = 
			"NirvanaBot " + VERSION + " Updates Portal/Project sections at http://ru.wikipedia.org and collects statistics\n" +
			"See also http://ru.wikipedia.org/User:NirvanaBot\n" +
            "Copyright (C) 2011-2017 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)\n" +
			"\n";
			
	public void showInfo() {
		System.out.print(PROGRAM_INFO);
	}
	
	public String getVersion() {
		return VERSION;
	}
		
	/**
	 * Constructor with flags
	 * 
	 * @param flags
	 */
	public NirvanaBot(int flags) {
		super(flags);
	}

    public static void main(String[] args) {
		NirvanaBasicBot bot = new NirvanaBot(NirvanaBasicBot.FLAG_SHOW_LICENSE);
        System.exit(bot.run(args));
	}

	@Override
	protected boolean loadCustomProperties(Map<String,String> launch_params) {
		String str = properties.getProperty("new-pages-template");
		newpagesTemplates = str.trim().split("\\s*,\\s*");
		if(newpagesTemplates==null || newpagesTemplates.length==0) {
			if(DEBUG_BUILD)
				newpagesTemplates = new String[]{"Участник:NirvanaBot/test/Новые статьи"};
			else {
				System.out.println("ABORT: properties not found");
				log.fatal("New pages template name (new-pages-template) is not specified in settings");
				return false;
			}
		}	
		log.info("new pages templates : "+newpagesTemplates.toString());	
		
		
		ERROR_NOTIFICATION = properties.getProperty("error-notification",ERROR_NOTIFICATION?YES:NO).equals(YES);
		log.info("error_notification="+ERROR_NOTIFICATION);
		

		DEFAULT_DEPTH = validateIntegerSetting(properties,"default-depth",DEFAULT_DEPTH,true);		
		log.info("depth="+DEFAULT_DEPTH);
		DEFAULT_HOURS = validateIntegerSetting(properties,"default-hours",DEFAULT_HOURS,true);
		log.info("hours="+DEFAULT_HOURS);
		DEFAULT_MAXITEMS = validateIntegerSetting(properties,"default-maxitems",DEFAULT_MAXITEMS,true);
		log.info("maxitems="+DEFAULT_MAXITEMS);		
		START_FROM = validateIntegerSetting(properties,"start-from",START_FROM,false);
		STOP_AFTER = validateIntegerSetting(properties,"stop-after",STOP_AFTER,false);		
		UPDATE_PAUSE = validateIntegerSetting(properties,"update-pause",UPDATE_PAUSE,false);
		
		DEFAULT_STARTS_PER_DAY = validateIntegerSetting(properties,"starts-per-day", DEFAULT_STARTS_PER_DAY, false);
		
		DEFAULT_PARSE_COUNT = validateIntegerSetting(properties,"parse-count",DEFAULT_PARSE_COUNT,false);

		DEFAULT_SERVICE_NAME = validateService(properties.getProperty("default-service", DEFAULT_SERVICE_NAME), DEFAULT_SERVICE_NAME);
		SELECTED_SERVICE_NAME = validateSelectedService(properties.getProperty("service", SELECTED_SERVICE_NAME), SELECTED_SERVICE_NAME);

		DEFAULT_USE_FAST_MODE = properties.getProperty("fast-mode",DEFAULT_USE_FAST_MODE?YES:NO).equals(YES);
		
		//if(UPDATE_LIMIT>0) UPDATE_LIMIT_ENABLED = true;

		TIME_FORMAT = properties.getProperty("time-format",TIME_FORMAT);
		if(!TIME_FORMAT.equalsIgnoreCase("short") && !TIME_FORMAT.equalsIgnoreCase("long") )
			TIME_FORMAT = "short";
		
		TASK = properties.getProperty("task-list",TASK?YES:NO).equals(YES);
		log.info("task list : "+(TASK?YES:NO));
		TASK_LIST_FILE = properties.getProperty("task-list-file",TASK_LIST_FILE);
		log.info("task list file: "+TASK_LIST_FILE);

		if (launch_params.containsKey("start_number")) {
			try {
			    startNumber = Integer.parseInt(launch_params.get("start_number"));
			} catch(NumberFormatException e){
				// ignore
			}
		}
		
		GENERATE_REPORT = properties.getProperty("statistics",GENERATE_REPORT?YES:NO).equals(YES);
		UPDATE_STATUS = properties.getProperty("update-status",UPDATE_STATUS?YES:NO).equals(YES);
		REPORT_FILE_NAME = properties.getProperty("statistics-file",REPORT_FILE_NAME);
		REPORT_WIKI_PAGE = properties.getProperty("statistics-wiki",REPORT_WIKI_PAGE);
		STATUS_WIKI_PAGE = properties.getProperty("status-wiki",STATUS_WIKI_PAGE);
		STATUS_WIKI_TEMPLATE = properties.getProperty("status-wiki-template",STATUS_WIKI_TEMPLATE);
		REPORT_FORMAT = properties.getProperty("statistics-format",REPORT_FORMAT);
		if (REPORT_FILE_NAME.contains("%(date)")) {
			//Calendar c = Calendar.getInstance();
			String date = String.format("%1$tF", Calendar.getInstance());
			REPORT_FILE_NAME = REPORT_FILE_NAME.replace("%(date)", date);
		}
		if (REPORT_FILE_NAME.contains("%(time)")) {
			//Calendar c = Calendar.getInstance();
			String time = String.format("%1$tT", Calendar.getInstance());
			REPORT_FILE_NAME = REPORT_FILE_NAME.replace("%(time)", time).replace(':', '-');
		}
		if (REPORT_FILE_NAME.contains("%(launch_number)")) {
			REPORT_FILE_NAME = REPORT_FILE_NAME.replace("%(launch_number)", String.valueOf(startNumber));
		}
		
		PICTURE_SEARCH_TAGS = properties.getProperty("picture-search-tags",PICTURE_SEARCH_TAGS);
		log.info("picture search tags = "+PICTURE_SEARCH_TAGS);
		FAIR_USE_IMAGE_TEMPLATES = properties.getProperty("fair-use-image-templates",FAIR_USE_IMAGE_TEMPLATES);
		log.info("fair-use image templates = "+FAIR_USE_IMAGE_TEMPLATES);
		String discussionSettings = properties.getProperty("discussion-pages-templates", "");
		log.info("discussion settings: "+discussionSettings);
		DISCUSSION_PAGES_SETTINGS = DiscussionPagesSettings.parseFromSettingsString(
				discussionSettings);
		DISCUSSION_PAGES_SETTINGS_WIKI = properties.getProperty("discussion-pages-settings-wikipage", "");

        enabledTypesString = properties.getProperty("type", enabledTypesString);
        if (enabledTypesString.isEmpty()) enabledTypesString = TYPE_ALL;
        enabledTypes = BotUtils.optionToSet(enabledTypesString);
        if (enabledTypes.isEmpty()) {
            log.fatal("Specify at least 1 portal section type to update");
            return false;
        }

		longTask = properties.getProperty("long-task",longTask?YES:NO).equals(YES);

		overridenPropertiesPage = properties.getProperty("overriden-properties-page",null);

        botTimeout = validateIntegerSetting(properties, "bot-timeout", botTimeout, false);
        servicesTimeout = validateIntegerSetting(properties, "services-timeout", servicesTimeout, false);

        wikiTranslationPage = properties.getProperty("wiki-translation-page", null);
        customTranslationFile = properties.getProperty("custom-translation-file", null);

		return true;
	}
	
	protected void onInterrupted(InterruptedException e) {
		log.fatal("Bot execution interrupted", e);
		System.out.println("Bot execution interrupted");
	}
	
	private static boolean validateService(String service) {
        return WikiTools.Service.hasService(service);
	}

	private static boolean validateSelectedService(String service) {
		return validateService(service) || service.equalsIgnoreCase(SERVICE_AUTO);
	}
	    
	private static String validateService(String service, String defaultValue) {
		if(validateService(service)) {
			return service;
		}
		return defaultValue;
	}
	
	private static String validateSelectedService(String service, String defaultValue) {
		if (validateSelectedService(service)) {
			return service;
		}
		return defaultValue;
	}
	
    private boolean loadOverridenProperties(String newpagesTemplate) throws IOException	{
        if(overridenPropertiesPage==null || overridenPropertiesPage.isEmpty()) {
            return false;
        }
		log.info("loading overriden properties from page "+overridenPropertiesPage);
		String overridenPropertiesText = null;
		try {
			overridenPropertiesText = wiki.getPageText(overridenPropertiesPage);
		} catch (IOException e) {			
			log.fatal("Failed to read overriden properties page: "+overridenPropertiesPage);
			throw e;
		}
		Map<String, String> options = new HashMap<String, String>();		
		if(!TryParseTemplate(newpagesTemplate, userNamespace, overridenPropertiesText,options, true)) {
			log.info("no default settings for this template: "+newpagesTemplate);
            return false;
		}
        PortalConfig config = new PortalConfig(options);
        DEFAULT_DELIMETER = config.getUnescaped(PortalConfig.KEY_SEPARATOR, DEFAULT_DELIMETER);
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
        DEFAULT_MIDDLE = config.getUnescaped(PortalConfig.KEY_MIDDLE, DEFAULT_MIDDLE);

        listTypeDefault = config.get(PortalConfig.KEY_TYPE, listTypeDefault).toLowerCase();

        if (config.hasKey(PortalConfig.KEY_DELETED_PAGES)) {
            DEFAULT_DELETED_FLAG = parseDeleted(config.get(PortalConfig.KEY_DELETED_PAGES),
                    DEFAULT_DELETED_FLAG, null);
		}

        if (config.hasKey(PortalConfig.KEY_RENAMED_PAGES)) {
            DEFAULT_RENAMED_FLAG = parseRenamed(config.get(PortalConfig.KEY_RENAMED_PAGES),
                    DEFAULT_RENAMED_FLAG, null);
		}

        PICTURE_SEARCH_TAGS = config.get(PortalConfig.KEY_IMAGE_SEARCH, PICTURE_SEARCH_TAGS);

        FAIR_USE_IMAGE_TEMPLATES = config.get(PortalConfig.KEY_FAIR_USE_IMAGE_TEMPLATES,
                FAIR_USE_IMAGE_TEMPLATES);

		if (!DISCUSSION_PAGES_SETTINGS_WIKI.isEmpty()) {
			log.info("load discussion pages settings from "+DISCUSSION_PAGES_SETTINGS_WIKI);
			String text = null;
			DiscussionPagesSettings newSettings = null;
			try{
				text = wiki.getPageText(DISCUSSION_PAGES_SETTINGS_WIKI);
			} catch (FileNotFoundException e) {
				log.error("Failed to get settings from wiki page: "+DISCUSSION_PAGES_SETTINGS_WIKI, e);
			}			
			if (text != null) {
				newSettings = DiscussionPagesSettings.parseFromSettingsString(text);
				if (newSettings == null) {
					log.error("Failed to parse settings from wiki page: "+DISCUSSION_PAGES_SETTINGS_WIKI);
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

	protected static PortalParam.Deleted parseDeleted(String value, PortalParam.Deleted defaultValue,ArrayList<String> errors) {
        Localizer localizer = Localizer.getInstance();
		PortalParam.Deleted flag = defaultValue;
		if (value.equalsIgnoreCase(STR_DELETE)) {
			flag = PortalParam.Deleted.REMOVE;
		} else if (value.equalsIgnoreCase(STR_MARK)) {
			flag = PortalParam.Deleted.MARK;
		} else if (value.equalsIgnoreCase(STR_LEAVE)) {
			flag = PortalParam.Deleted.DONT_TOUCH;
		} else {
            log.warn(String.format(ERROR_INVALID_PARAMETER_EN, PortalConfig.KEY_DELETED_PAGES,
                    value));
            if (errors != null) {
                String format = localizer.localize(ERROR_INVALID_PARAMETER);
                errors.add(String.format(format, PortalConfig.KEY_DELETED_PAGES, value));
			}
		}
		return flag;
	}
	
	private boolean checkPortalInTaskList(List<String> tasks, String portal) {
		if (tasks == null) {
			return true;
		}
		for (String task : tasks) {					
			if(portal.startsWith(task)) {
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

    protected void go() throws InterruptedException, BotFatalError {
        String cacheDir = outDir + "/" + "cache";

        long startMillis = Calendar.getInstance().getTimeInMillis();
        commons = createCommonsWiki();
		commons.setMaxLag( MAX_LAG );

        serviceManager = createServiceManager();

        serviceManager.setTimeout(servicesTimeout);

		if (!serviceManager.checkServices()) {
            throw new BotFatalError("Some services are down.");
        }

        LocalizationManager localizationManager = new LocalizationManager(outDir, cacheDir,
                LocalizationManager.DEFAULT_TRANSLATIONS_DIR, LANGUAGE, customTranslationFile);

        BotReporter reporter = new BotReporter(wiki, cacheDir, 1000, true, getVersion(),
                reportPreambulaFile);
        
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

        initLocalizedStrings();
        BotVariables.init();
        PortalConfig.initStatics();
        DateTools.init(LANGUAGE);

        if (UPDATE_STATUS) {
        	try {
	            reporter.updateStartStatus(STATUS_WIKI_PAGE, STATUS_WIKI_TEMPLATE);
            } catch (LoginException | IOException e) {
            	log.error(e);	            
            }
        }
		
        try {
	        userNamespace = wiki.namespaceIdentifier(Wiki.USER_NAMESPACE);
        } catch (IOException e) {
	        log.fatal("Failed to retrieve user namespace");
	        return;
        }
        
		// this is a workaround for bad support of keep alive feature in HttpUrlConnection
		// without it, writing of big articles (~500KB) may hang 
		// (when reading answer from server after writing)
		System.setProperty("http.keepAlive", "false"); // adjust HTTP connections

		List<String> tasks = null;
		if (TASK) {	
			log.info("reading tasks from file: "+TASK_LIST_FILE);
			tasks = FileTools.readFileToList(TASK_LIST_FILE, true);
			if (tasks == null) {
				log.fatal("Failed to read tasks file: " + TASK_LIST_FILE);
				return;
			}
			log.info("loaded tasks: "+tasks.size()+" total");
			for(String task : tasks) {
				log.debug("task: "+task);
			}
		}
        boolean fatalProblem = false;
        boolean noServicesOrTimeout = false;
        for (String newpagesTemplate: newpagesTemplates) {

        	long startT = Calendar.getInstance().getTimeInMillis();
    		log.info("template to check: "+newpagesTemplate);

            String []portalNewPagesLists = null;
            int repeat = 2;
            boolean overridden = false;
            while(repeat > 0) {
                try {
                    overridden = loadOverridenProperties(newpagesTemplate);
                } catch (IOException e) {
                    log.error("failed to get page: " + newpagesTemplate);
                    if(!serviceManager.checkServices()) {
                        log.fatal("Some services are down. Stopping...");
                        fatalProblem = true;
                        break;
                    }
                    repeat--;
                    continue;
                }

                try {
                    portalNewPagesLists = wiki.whatTranscludesHere(newpagesTemplate, Wiki.ALL_NAMESPACES);
                } catch (IOException e) {            
                    log.fatal("failed to get portal list");
                    if(!serviceManager.checkServices()) {
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
                    log.debug("Update service manager with default service: " + DEFAULT_SERVICE_NAME + " and selected service: " + SELECTED_SERVICE_NAME);
                    serviceManager.updateCatScan(DEFAULT_SERVICE_NAME, SELECTED_SERVICE_NAME);
                } catch (BotFatalError e1) {
                    log.fatal(e1);
                    return;
                }
            }
    		log.info("loaded portal settings: "+portalNewPagesLists.length);
    		java.util.Arrays.sort(portalNewPagesLists);
    		
    		int i = 0;	// текущий портал
    		int t = 0;	// количество проверенных порталов
    		int retry_count = 0;
    		boolean retry = false;
    		ReportItem reportItem = null;
    		String portalName = null;

    		while(i < portalNewPagesLists.length) {
                if (!noServicesOrTimeout && botTimeout > 0 &&
                        (Calendar.getInstance().getTimeInMillis() - startMillis > botTimeout)) {
                    log.debug("Bot work time exceeded allowed maximum of " + botTimeout + " ms. Stopping bot.");
                    noServicesOrTimeout = true;
                }
    			log.debug("start processing portal No: "+i);
    			if(retry) {
    				retry = false;
    				log.info("retrying portal: "+portalName);		
    				retry_count++;
    				//reportItem = report.get(report.size()-1);
    			} else {
    				retry_count = 0;
    				portalName = portalNewPagesLists[i];
    						
    				//i++;	
    				int percent = (i*100)/portalNewPagesLists.length;
    				log.info(String.format(
    						"=[ %1$d/%2$d: %3$d%% ]===================================================================",
    						i+1,portalNewPagesLists.length,percent));
    				log.info("processing portal: "+portalName);
    				//Calendar startPortalTime = Calendar.getInstance();
    				reportItem = new ReportItem(newpagesTemplate, portalName);
    				if(reporter!=null) reporter.add(reportItem);
    				reportItem.start();
    			}
    			
    			if (!checkPortalInTaskList(tasks, portalName)) {
    				log.info("SKIP portal: "+portalName);
    				reportItem.skip();
    				i++;
    				continue;
    			}
                if (noServicesOrTimeout) {
                    reportItem.error(BotError.FATAL);
                    i++;
                    continue;
                }
    			if(retry_count==0) t++;
    			if(t<START_FROM) {log.info("SKIP portal: "+portalName);	reportItem.skip(); i++; continue;}
    			
    			if(retry_count==0) reporter.portalChecked();
    			boolean mayBeSomeServiceProblem = false;
    			try {
    				serviceManager.timeToFixProblems();
    				
    				String portalSettingsText = wiki.getPageText(portalName);

                    if (DEBUG_MODE || NirvanaBasicBot.DEBUG_BUILD) {
                        FileTools.dump(portalSettingsText, dumpDir, portalName + ".settings.txt");
    				}

    				if (!wiki.allowEditsByCurrentBot(portalSettingsText)) {
    					log.info("SKIP portal: "+portalName);	reportItem.skip(); i++; continue;
    				}
    				
    				Map<String, String> parameters = new HashMap<String, String>();
    				if(TryParseTemplate(newpagesTemplate, userNamespace, portalSettingsText, parameters, true)) {
    					log.info("validate portal settings OK");					
    					logPortalSettings(parameters);
    					NewPagesData data = new NewPagesData();
    					if (createPortalModule(parameters, data)) {
                            if ((enabledTypes.contains(TYPE_ALL) ||
                                    enabledTypes.contains(data.type)) &&
                                    data.portalModule != null) {
                                if (DEBUG_MODE || !portalName.contains("ValidParam") ||
                                        !DEBUG_BUILD) {
									if (retry_count==0) { 
										reporter.portalProcessed();
										reportItem.processed();
									}
									if(data.portalModule.update(wiki, reportItem, COMMENT)) {
										reporter.portalUpdated();
										reportItem.updated();
									} 
									if(UPDATE_PAUSE>0) Thread.sleep(UPDATE_PAUSE);
								}        							
        					} else {
        						reportItem.skip();						
        						log.info("SKIP portal: "+portalName); 
        					}    						
    					} else {
    						log.warn("portal module not created");
    						reportItem.error(BotError.SETTINGS_ERROR);
    					}

    					if(!data.errors.isEmpty()) {
    						log.warn("errors occured during checking settings");
    						for(String str:data.errors) {
    							log.info(str);
    						}
    						reportItem.errors = data.errors.size();
    						String errorText = StringUtils.join(data.errors, "\n");
                            FileTools.dump(errorText, dumpDir, portalName + ".err");
    						if(ERROR_NOTIFICATION) {
    							for(String err:data.errors) {
    								log.info(err);
    							}
    						}
    					}
    				} else {
    					reportItem.settingsValid = false;
    					reportItem.error(BotError.SETTINGS_ERROR);
    					log.error("validate portal settings FAILED");
    				}
    				
    			} catch (IllegalArgumentException | IndexOutOfBoundsException | NullPointerException | java.util.zip.ZipException e) { 
    				// includes ArrayIndexOfBoundsException
    				log.error(e.toString()); 
                    if (retry_count < retryMax) {
    					log.info("RETRY AGAIN");
    					retry = true;
    				} else {
    					reporter.portalError();
    					reportItem.error(BotError.BOT_ERROR);
    					//e.printStackTrace();
    					log.error("OOOOPS!!!", e); // print stack trace
    				}	
    			} catch (ServiceError | IOException e) {
    				log.error(e.toString());
    				
    				mayBeSomeServiceProblem = true;

                    if (retry_count < retryMax) {
    					log.info("RETRY AGAIN");
    					retry = true;
    				} else {
    					reporter.portalError();
    					if (e instanceof ServiceError)
    						reportItem.error(BotError.SERVICE_ERROR);
    					else 
    						reportItem.error(BotError.IO_ERROR);
    					//e.printStackTrace();
    					log.error("OOOOPS!!!", e); // print stack trace
    				}
    			} catch (LoginException e) {
    				log.fatal(e.toString());
    				fatalProblem = true;
    				break;
    				//e.printStackTrace();
    			} catch (Exception e) {
    				log.error(e.toString()); 
                    if (retry_count < retryMax) {
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
                    if (retry_count < retryMax) {
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
    			if(STOP_AFTER>0 && t>=STOP_AFTER) break;
    			if(!retry) i++;
    			if (mayBeSomeServiceProblem) {
                    if(!serviceManager.checkServices()) {
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
    		Calendar cEnd = Calendar.getInstance();
    		long endT = cEnd.getTimeInMillis();
    		reporter.addToTotal(portalNewPagesLists.length);

            log.info("TEMPLATE "+ ((fatalProblem||noServicesOrTimeout)? "STOPPED": "FINISHED")+" at "+String.format("%1$tT", cEnd));
    		log.info("WORK TIME for TEMPLATE: "+BotReporter.printTimeDiff(endT-startT));
    		if (fatalProblem) {
    			break;
    		}
    	}
        reporter.logStatus();
        if (GENERATE_REPORT) {
        	if (StringUtils.containsIgnoreCase(REPORT_FORMAT, "txt")) {        			
                reporter.reportTXT(outDir + "/" + REPORT_FILE_NAME);
        	}
            if (StringUtils.containsIgnoreCase(REPORT_FORMAT, "wiki")) {
                if (serviceManager.getMainWikiService().isOk() == Status.OK) {
                    reporter.reportWiki(REPORT_WIKI_PAGE, startNumber == 1);
                } else {
                    log.warn("Wiki is not available. Reporting to wiki skipped.");
                }
            }
		}
        if (UPDATE_STATUS) {
        	try {
	            reporter.updateEndStatus(STATUS_WIKI_PAGE, STATUS_WIKI_TEMPLATE);
            } catch (LoginException | IOException e) {
            	log.error(e);	            
            }
        }
        reporter.botFinished(true);
        if (wikiTranslationPage != null && !wikiTranslationPage.isEmpty()) {
            try {
                localizationManager.refreshWikiTranslations(wiki, wikiTranslationPage);
            } catch (LoginException | IOException e) {
                throw new BotFatalError(e);
            }
        }
	}

    private void initLocalizedStrings() {
    }

	public boolean createPortalModule(Map<String, String> options, NewPagesData data) throws 
			NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        log.debug("Scan portal settings...");

		PortalParam param = new PortalParam();
		param.lang = LANGUAGE;
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

        param.categories = optionToList(options, PortalConfig.KEY_CATEGORIES, true);
        if (config.hasKey(PortalConfig.KEY_CATEGORY)) {
            param.categories.add(config.get(PortalConfig.KEY_CATEGORY));
		}		

        param.categoriesToIgnore = optionToList(options, PortalConfig.KEY_IGNORE, true);

        param.categoryGroups = multiOptionToArray(options, PortalConfig.KEY_CATEGORIES, 1,
                PortalParam.MAX_CAT_GROUPS);
        param.categoryToIgnoreGroups = multiOptionToArray(options, PortalConfig.KEY_IGNORE, 1,
                PortalParam.MAX_CAT_GROUPS);

        param.usersToIgnore = optionToList(options, PortalConfig.KEY_IGNORE_AUTHORS, false);

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
    			param.service = WikiTools.Service.getServiceByName(service); 
    			if (param.service == null) {
    				param.service = serviceManager.getActiveService();
                    log.warn(String.format(ERROR_INVALID_PARAMETER_EN, PortalConfig.KEY_SERVICE,
                            service));
                    String format = localizer.localize(ERROR_INVALID_PARAMETER);
                    data.errors.add(String.format(format, PortalConfig.KEY_SERVICE, service));
    			}
			}
		}

		param.archSettings = new ArchiveSettings();
		param.archSettings.parseCount = DEFAULT_PARSE_COUNT;

		param.archive = null;
        if (config.hasKey(PortalConfig.KEY_ARCHIVE)) {
            param.archive = config.get(PortalConfig.KEY_ARCHIVE);
            parseArchiveName(param.archSettings, param.archive);
		}

        if (config.hasKey(PortalConfig.KEY_ARCHIVE_HEADER_FORMAT) ||
                config.hasKey(PortalConfig.KEY_ARCHIVE_SUBHEADER_FORMAT)) {
            data.errors.addAll(parseArchiveHeaders(param.archSettings, config));
		}

        if (config.hasKey(PortalConfig.KEY_ARCHIVE_PARAMS)) {
            data.errors.addAll(parseArchiveSettings(param.archSettings,
                    config.get(PortalConfig.KEY_ARCHIVE_PARAMS)));
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
        param.middle = config.getUnescaped(PortalConfig.KEY_MIDDLE, DEFAULT_MIDDLE);

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
            WikiTools.EnumerationType enumType = WikiTools.EnumerationType.OR;
            if (option.startsWith("!") || option.startsWith("^")) {
                option = option.substring(1);
                enumType = WikiTools.EnumerationType.NONE;
            }
			if (!option.isEmpty()) {
                String sep = ",";
                if (option.contains(";")) {
                    sep = ";";
                    if (enumType != WikiTools.EnumerationType.NONE) {
                        enumType = WikiTools.EnumerationType.AND;
                    }
				}
                List<String> templates = BotUtils.optionToList(option, true, sep);
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
                DEFAULT_HOURS, param.service.MAX_HOURS);

        int defaultMaxItems = DEFAULT_MAXITEMS;
        if (isTypeWithUnlimitedItems(type)) {
            defaultMaxItems = MAX_MAXITEMS;
        }
        param.maxItems = parseIntegerKeyWithMaxVal(config, PortalConfig.KEY_MAX_ITEMS, data.errors,
                defaultMaxItems, MAX_MAXITEMS);

		param.deletedFlag = DEFAULT_DELETED_FLAG;
        if (config.hasKey(PortalConfig.KEY_DELETED_PAGES)) {
            param.deletedFlag = parseDeleted(config.get(PortalConfig.KEY_DELETED_PAGES),
                    param.deletedFlag, data.errors);
		}

		param.renamedFlag = DEFAULT_RENAMED_FLAG;
        if (config.hasKey(PortalConfig.KEY_RENAMED_PAGES)) {
            param.renamedFlag = parseRenamed(config.get(PortalConfig.KEY_RENAMED_PAGES),
                    param.renamedFlag,data.errors);
		}

        param.updatesPerDay = parseIntegerKeyWithMaxVal(config, PortalConfig.KEY_UPDATES_PER_DAY,
                data.errors, DEFAULT_UPDATES_PER_DAY, MAX_UPDATES_PER_DAY);

        param.delimeter = config.getUnescaped(PortalConfig.KEY_SEPARATOR, DEFAULT_DELIMETER);

        param.imageSearchTags = config.get(PortalConfig.KEY_IMAGE_SEARCH, PICTURE_SEARCH_TAGS);

        param.fairUseImageTemplates = config.get(PortalConfig.KEY_FAIR_USE_IMAGE_TEMPLATES,
                FAIR_USE_IMAGE_TEMPLATES);

		if (!validateParams(param,data.errors)) {
			return false;
		}
		
		if (!itsTimeToUpdate(param)) {
			// There is no errors but we should not update this portal now
			return true;
		}

        if (isTypeNewPages(type)) {
			data.portalModule = new NewPages(param);						
		} else if (isTypePages(type)) {
			data.portalModule = new Pages(param);
        } else if (type.equals(LIST_TYPE_NEW_PAGES_WITH_IMAGES_IN_CARD) ||
                type.equals(LIST_TYPE_NEW_PAGES_WITH_IMAGES_IN_CARD_OLD)) {
			data.portalModule = new NewPagesWithImages(param, commons, new ImageFinderInCard(param.imageSearchTags));
        } else if (type.equals(LIST_TYPE_NEW_PAGES_WITH_IMAGES_IN_TEXT) ||
                type.equals(LIST_TYPE_NEW_PAGES_WITH_IMAGES_IN_TEXT_OLD) ) {
			data.portalModule = new NewPagesWithImages(param, commons, new ImageFinderInBody());
        } else if (type.equals(LIST_TYPE_NEW_PAGES_WITH_IMAGES) ||
                type.equals(LIST_TYPE_NEW_PAGES_WITH_IMAGES_OLD)) {
			data.portalModule = new NewPagesWithImages(param, commons, new ImageFinderUniversal(param.imageSearchTags));
        } else if (type.equals(LIST_TYPE_NEW_PAGES_7_DAYS) ||
                type.equals(LIST_TYPE_NEW_PAGES_7_DAYS_OLD)) {
			data.portalModule = new NewPagesWeek(param);
		} else if (isTypeDiscussedPages(type)) {
			if (DISCUSSION_PAGES_SETTINGS == null) {
                String err = localizer.localize(ERROR_DISCUSSED_PAGES_SETTINGS_NOT_DEFINED);
                data.errors.add(err);
			} else {
				data.portalModule = new DiscussedPages(param, DISCUSSION_PAGES_SETTINGS);
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
        List<String> items = BotUtils.optionToList(option, true, ",");
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
            } catch(NumberFormatException e) {
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
			} catch(NumberFormatException e) {
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
		if(param.categories.isEmpty()) {
            String format = localizer.localize(ERROR_ABSENT_PARAMETER);
            log.warn(String.format(format, PortalConfig.KEY_CATEGORIES));
            errors.add(String.format(format, PortalConfig.KEY_CATEGORIES));
			retval = false;
		}
		if(param.page.isEmpty()) {
            String format = localizer.localize(ERROR_ABSENT_PARAMETER);
            log.warn(String.format(format, PortalConfig.KEY_PAGE));
            errors.add(String.format(format, PortalConfig.KEY_PAGE));
			retval = false;
		}
		return retval;
	}
	
	private boolean itsTimeToUpdate(PortalParam param) {
		if(param.updatesPerDay>DEFAULT_STARTS_PER_DAY) {
			param.updatesPerDay = DEFAULT_STARTS_PER_DAY;
		}
		
		if (startNumber != 1) {
			int freq = DEFAULT_STARTS_PER_DAY / param.updatesPerDay;
			if ((startNumber-1)%freq != 0) {
				return false;
			}
		}
		return true;
	}
	
	private int parseRenamed(String string, int defaultValue,
			ArrayList<String> errors) {
		int flag = 0;
		String items[];
		items = string.split(",");
		for(String item:items) {
			String str = item.trim();
            if (str.equals(STR_OLD_TITLE)) {
				flag = flag|PortalParam.RENAMED_OLD;
            } else if (str.equals(STR_NEW_TITLE)) {
				flag = flag|PortalParam.RENAMED_NEW;
            }
		}
		if(flag==0) {
			flag = defaultValue;
            if (errors != null) {
                String format = localizer.localize(ERROR_INVALID_PARAMETER);
                errors.add(String.format(format, PortalConfig.KEY_RENAMED_PAGES));
            }
		}		
		
		return flag;
	}

    private ArrayList<String> parseArchiveHeaders(ArchiveSettings archiveSettings,
            PortalConfig config) {
        ArrayList<String> errors = new ArrayList<String>();

        Period p1 = Period.NONE;
        final String key1 = PortalConfig.KEY_ARCHIVE_HEADER_FORMAT;
        if (config.hasKey(key1)) {
            String str = config.getUnquoted(key1);
            p1 = ArchiveSettings.getHeaderPeriod(str);
            if (p1 != Period.NONE) {
                archiveSettings.headerFormat = str;
            } else {
                String format = localizer.localize(ERROR_PARAMETER_MISSING_VARIABLE);
                errors.add(String.format(format, key1));
            }
        }

        Period p2 = Period.NONE;
        final String key2 = PortalConfig.KEY_ARCHIVE_SUBHEADER_FORMAT;
        if (config.hasKey(key2)) {
            String str = config.getUnquoted(key2);
            p2 = ArchiveSettings.getHeaderPeriod(str);
            if (p2 != Period.NONE) {
                archiveSettings.headerHeaderFormat = archiveSettings.headerFormat;
                archiveSettings.headerFormat = str;
            } else {
                String format = localizer.localize(ERROR_PARAMETER_MISSING_VARIABLE);
                errors.add(String.format(format, key2));
            }
        }

        if (p1 != Period.NONE && p1 == p2) {
            String format = localizer.localize(ERROR_SAME_PERIOD);
            errors.add(String.format(format, key1, key2, p1.template()));
        }
        return errors;
    }

	public static ArrayList<String> parseArchiveSettings(ArchiveSettings archiveSettings,
			String settings) {
        Localizer localizer = Localizer.getInstance();
		ArrayList<String> errors = new ArrayList<String>();
		String items[];
		items = settings.split(",");
		ArrayList<String> itemsVector = new ArrayList<String>(items.length);
		for(String item:items) {
			itemsVector.add(item.trim());
		}		
        if (itemsVector.contains(STR_ABOVE) && itemsVector.contains(STR_BELOW)) {
            String format = localizer.localize(ERROR_PARAMETER_HAS_MULTIPLE_VALUES);
            String param = String.format("%1$s (%2$s/%3$s)", PortalConfig.KEY_ARCHIVE_PARAMS,
                    STR_ABOVE, STR_BELOW);
            errors.add(String.format(format, param));
        } else if (itemsVector.contains(STR_ABOVE)) {
			archiveSettings.addToTop = true;
        } else if (itemsVector.contains(STR_BELOW)) {
			archiveSettings.addToTop = false;
		}
        if (itemsVector.contains(STR_TOSORT)||itemsVector.contains(STR_SORT)) {
			archiveSettings.sorted = true;
		}
        if (itemsVector.contains(STR_REMOVE_DUPLICATES)) {
			archiveSettings.removeDuplicates = true;
		}
		int cnt = 0;
        if (itemsVector.contains(STR_ENUMERATE_WITH_HASH) ||
                itemsVector.contains(STR_ENUMERATE_WITH_HASH2)) {
			cnt++;
			archiveSettings.enumeration = Enumeration.HASH;
		}
        if (itemsVector.contains(STR_ENUMERATE_WITH_HTML) ||
                itemsVector.contains(STR_ENUMERATE_WITH_HTML2)) {
			cnt++;
			archiveSettings.enumeration = Enumeration.HTML;
		}
        if (itemsVector.contains(STR_ENUMERATE_WITH_HTML_GLOBAL) ||
                itemsVector.contains(STR_ENUMERATE_WITH_HTML_GLOBAL2)) {
			cnt++;
			archiveSettings.enumeration = Enumeration.HTML_GLOBAL;
		}
		if(cnt>1) {
			archiveSettings.enumeration = Enumeration.NONE;
            String format = localizer.localize(ERROR_PARAMETER_HAS_MULTIPLE_VALUES);
            String param = String.format("%1$s (%2$s)", PortalConfig.KEY_ARCHIVE_PARAMS,
                    localizer.localize("нумерация"));
            errors.add(String.format(format, param));
		}
		return errors;
	}

	public static ArrayList<String> parseArchiveName(ArchiveSettings archiveSettings,
			String name) {
		ArrayList<String> errors = new ArrayList<String>();		
		archiveSettings.archive = name;
		if(name.contains(Period.YEAR.template())) {
			archiveSettings.archivePeriod = Period.YEAR;
		}
		if(name.contains(Period.SEASON.template())) {
			archiveSettings.archivePeriod = Period.SEASON;
		}
		if(name.contains(Period.QUARTER.template())) {
			archiveSettings.archivePeriod = Period.QUARTER;
		}
		if(name.contains(Period.MONTH.template())) {
			archiveSettings.archivePeriod = Period.MONTH;
		}			
		return errors;
	}

    protected static List<String> optionToList(Map<String, String> options, String key,
            boolean replaceAdditionalSeparator) {
		if (options.containsKey(key)) {			
			String option = options.get(key);
			if (replaceAdditionalSeparator) {
				option = option.replace(ADDITIONAL_SEPARATOR, DEPTH_SEPARATOR);
			}
            return BotUtils.optionToList(option, true);
		}
        return Collections.emptyList();
	}

	protected static ArrayList<List<String>> multiOptionToArray(Map<String, String> options, String key, int start, int end) {
		ArrayList<List<String>> listlist = new ArrayList<List<String>>(end-start+1);
		String keyNumbered;
		for(int i=start;i<=end;i++) {
			keyNumbered = key + String.valueOf(i);
            List<String> list = optionToList(options, keyNumbered, true);
			listlist.add(list);
		}
		return listlist;
	}
}
