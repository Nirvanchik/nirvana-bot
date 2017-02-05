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

import org.wikipedia.Wiki;
import org.wikipedia.nirvana.DateTools;
import org.wikipedia.nirvana.FileTools;
import org.wikipedia.nirvana.NirvanaBasicBot;
import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.ServiceError;
import org.wikipedia.nirvana.StringTools;
import org.wikipedia.nirvana.WikiTools;
import org.wikipedia.nirvana.WikiTools.EnumerationType;
import org.wikipedia.nirvana.archive.ArchiveSettings;
import org.wikipedia.nirvana.archive.ArchiveSettings.Enumeration;
import org.wikipedia.nirvana.archive.ArchiveSettings.Period;
import org.wikipedia.nirvana.localization.LocalizationManager;
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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

/**
 * @author kin
 *
 */
public class NirvanaBot extends NirvanaBasicBot{
	static final String SERVICE_AUTO = "auto";
	private String userNamespace;
	public static final String DEPTH_SEPARATOR = "|";
    public static final String ADDITIONAL_SEPARATOR = "#";
    
	public static final String YES_RU = "да";
	public static final String NO_RU = "нет";

	private static int START_FROM = 0;
	private static int STOP_AFTER = 0;
	public static int UPDATE_PAUSE = 1000;
	
	public static String TIME_FORMAT = "short"; // short,long
	
	private boolean TASK = false;
	private String TASK_LIST_FILE = "task.txt";
	
	private String newpagesTemplates[] = null;
	
	public static final String ERROR_PARSE_INTEGER_FORMAT_STRING = "Error when parsing integer parameter \"%1$s\" integer value %2$s";
	private static final String ERROR_PARSE_INTEGER_FORMAT_STRING_RU = "Ошибка при чтении параметра \"%1$s\". Значение %2$s не распознано как число.";
	private static final String ERROR_INTEGER_TOO_BIG_STRING_RU = "Значение параметра \"%1$s\" слишком велико (%2$s). Использовано максимальное значение (%3$s). Укажите разумную величину.";
	@SuppressWarnings("unused")
	private static final String ERROR_NOTIFICATION_TEXT = "При проверке параметров были обнаружены ошибки. %1$s Напишите [[%1$s|мне]], если нужна помощь в настройке параметров. ~~~~";
	private static final String ERROR_PARAMETER_HAS_MULTIPLE_VALUES_RU = "Для параметра \"%1$s\" дано несколько значений. Использовано значение по умолчанию.";
	private static final String ERROR_INVALID_PARAMETER = "Error in parameter \"%1$s\". Invalid value (%2$s).";
	private static final String ERROR_INVALID_PARAMETER_RU = "Ошибка в параметре \"%1$s\". Задано неправильное значение (%2$s).";
	private static final String ERROR_PARAMETER_NOT_ACCEPTED_RU = "Значение параметра не принято.";
	private static final String ERROR_ABSENT_PARAMETER_RU = "Ошибка в параметрах. Параметр \"%1$s\" не задан";
    private static final String ERROR_AUTHOR_PARAMETER_INVALID_USAGE_RU =
            "Неверный параметр \"%1$s\". Поле %(автор) не поддерживается для данного типа типа " +
            "списков. Удалите его!";
	//private static final String ERROR_NO_CATEGORY = "Ошибка в параметрах. Категории не заданы (см. параметр категория/категории).";
	//private static final String ERROR_NO_ARTICLE = "Ошибка в параметрах. Параметр \"статья\" не задан.";
	//private static final String ERROR_INVALID_SERVICE_NAME = "Ошибка в параметрах. Параметр \"статья\" не задан.";
	
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
	private static String DEFAULT_FORMAT = "* [[%(название)]]";
	public static String DEFAULT_FORMAT_STRING = "* [[%1$s]]";
	private static final String LIST_TYPE_NEW_PAGES = "новые статьи";
	private static final String LIST_TYPE_NEW_PAGES_OLD = "список новых статей";
	private static final String LIST_TYPE_NEW_PAGES_7_DAYS = "новые статьи по дням";
	private static final String LIST_TYPE_NEW_PAGES_7_DAYS_OLD = "списки новых статей по дням";
	private String listTypeDefault = LIST_TYPE_NEW_PAGES;
	
	private static String TYPE = "all";
	
	private static String DEFAULT_DELIMETER = "\n";

    /**
     * This is how many times we will retry each portal page update if it fails.
     * 0 means that we will not retry.
     */
    private int retryMax = 1;

	private static String overridenPropertiesPage = null;
	
	private static String PICTURE_SEARCH_TAGS = "image file,Фото,портрет,Изображение,Файл,File";
	private static String FAIR_USE_IMAGE_TEMPLATES = "Обоснование добросовестного использования, Disputed-fairuse, ОДИ, Несвободный файл/ОДИ";
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
		
		TYPE = properties.getProperty("type",TYPE); 
		
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
		if (options.containsKey("разделитель") && !options.get("разделитель").isEmpty())
		{
			DEFAULT_DELIMETER = options.get("разделитель").replace("\"", "").replace("\\n", "\n");
		}
		
		String key = "формат элемента";
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{
			DEFAULT_FORMAT = options.get(key);//.replace("{", "{{").replace("}", "}}");
		}			
		
		key = "глубина";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {			
			try {
				DEFAULT_DEPTH = Integer.parseInt(options.get(key));
			} catch(NumberFormatException e) {
				log.warn(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING, key, options.get(key)));					
			}
			if(DEFAULT_DEPTH>MAX_DEPTH) {					
				DEFAULT_DEPTH = MAX_DEPTH;				
			}
		}			
		
		key = "сервис";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			SELECTED_SERVICE_NAME = validateSelectedService(options.get(key), SELECTED_SERVICE_NAME);			
		}

        key = "сервис по умолчанию";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            DEFAULT_SERVICE_NAME = validateService(options.get(key), DEFAULT_SERVICE_NAME);
        }

		key = "быстрый режим";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			DEFAULT_USE_FAST_MODE = options.get(key).equalsIgnoreCase(YES_RU);
		}
		
		key = "часов";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {		
			try {
				DEFAULT_HOURS = Integer.parseInt(options.get(key));
			} catch(NumberFormatException e) {
				log.warn(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING, key, options.get(key)));
			}
		}
		
		key = "элементов";
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{		
			try {
				DEFAULT_MAXITEMS = Integer.parseInt(options.get(key));
			} catch(NumberFormatException e) {
				log.warn(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING, key, options.get(key)));
			}
			if(DEFAULT_MAXITEMS>MAX_MAXITEMS) {
				DEFAULT_MAXITEMS = MAX_MAXITEMS;
			}
		}
		
		key = "пространство имён";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {			
			try {
				DEFAULT_NAMESPACE = Integer.parseInt(options.get(key));
			} catch(NumberFormatException e) {
				log.warn(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING, key, options.get(key)));
			}
		}			
		
		if (options.containsKey("шапка") && !options.get("шапка").isEmpty())
		{
			DEFAULT_HEADER = options.get("шапка").replace("\\n", "\n");
		}			
		
		if (options.containsKey("середина") && !options.get("середина").isEmpty())
		{
			DEFAULT_MIDDLE = options.get("середина").replace("\\n", "\n");
		}
		
		if (options.containsKey("подвал") && !options.get("подвал").isEmpty())
		{
			DEFAULT_FOOTER = options.get("подвал").replace("\\n", "\n");
		}
		
		if(options.containsKey("тип") && !options.get("тип").isEmpty()) {				
			listTypeDefault = options.get("тип").toLowerCase();
		}	
		
		key = "удаленные статьи";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			DEFAULT_DELETED_FLAG = parseDeleted(options.get(key),DEFAULT_DELETED_FLAG,null);
		}
		
		key = "переименованные статьи";			
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			DEFAULT_RENAMED_FLAG = parseRenamed(options.get(key),DEFAULT_RENAMED_FLAG,null);
		}
		
		key = "поиск картинки";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			PICTURE_SEARCH_TAGS = options.get(key);
		}		

		key = "шаблоны запрещенных картинок";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			FAIR_USE_IMAGE_TEMPLATES = options.get(key);
		}
		
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
		PortalParam.Deleted flag = defaultValue;
		if(value.equalsIgnoreCase("удалять")) {
			flag = PortalParam.Deleted.REMOVE;
		} else if(value.equalsIgnoreCase("помечать")) {
			flag = PortalParam.Deleted.MARK;
		} else if(value.equalsIgnoreCase("оставлять")) {
			flag = PortalParam.Deleted.DONT_TOUCH;
		} else {
			log.warn(String.format(ERROR_INVALID_PARAMETER,"удаленные статьи", value));
			if(errors!=null) {
				errors.add(String.format(ERROR_INVALID_PARAMETER_RU,"удаленные статьи", value));
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

	@SuppressWarnings("unused")
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
        					if((TYPE.equals("all") || TYPE.equals(data.type)) && data.portalModule != null) {
								if(DEBUG_MODE || !DEBUG_BUILD || !portalName.contains("ValidParam") /*&& !portalName.contains("Testing")*/) {
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

	public boolean createPortalModule(Map<String, String> options, NewPagesData data) throws 
			NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		log.debug("portal settings init started");
		String key;
		
		PortalParam param = new PortalParam();
		param.lang = LANGUAGE;
		
		String type = null;
		key = "тип";
		if(!options.containsKey(key) || options.get(key).isEmpty()) {
			data.errors.add("Параметр \"тип\" не задан. Использовано значение по умолчанию ");
			type = listTypeDefault;
		} else {
			type = options.get(key).toLowerCase();
		}	
		log.debug("тип: "+type);
		data.type = type;
		
		param.categories = optionToStringArray(options,"категории", true);		
		key = "категория";
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{
			param.categories.add(options.get(key));
		}		
		
		param.categoriesToIgnore = optionToStringArray(options,"игнорировать", true);
		
		param.categoryGroups = multiOptionToArray(options, "категории", 1, PortalParam.MAX_CAT_GROUPS);
		param.categoryToIgnoreGroups = multiOptionToArray(options, "игнорировать", 1, PortalParam.MAX_CAT_GROUPS);
				
		param.usersToIgnore = optionToStringArray(options,"игнорировать авторов", false);
		
		param.page = "";
		if (options.containsKey("страница"))
		{
			param.page = options.get("страница");
		}
		
		param.service = serviceManager.getActiveService();
		key = "сервис";
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{
			String service = options.get(key);
			
			if (service.equals("по умолчанию") || 
					service.equals("default") ||
					service.equals("auto") ||
					service.equals("авто") ||
					service.equalsIgnoreCase(SERVICE_AUTO)) {
				// do nothing - it's all done
			} else {
    			param.service = WikiTools.Service.getServiceByName(service); 
    			if (param.service == null) {
    				param.service = serviceManager.getActiveService();
    				log.warn(String.format(ERROR_INVALID_PARAMETER, key, service));
    			    data.errors.add(String.format(ERROR_INVALID_PARAMETER_RU, key, service));
    			}
			}
		}
		
		param.archSettings = new ArchiveSettings();
		param.archSettings.parseCount = DEFAULT_PARSE_COUNT;
		
		param.archive = null;
		key = "архив";
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{
			param.archive = options.get(key);
			parseArchiveName(param.archSettings,options.get(key));			
		}
		
		String str = "";
		key = "формат заголовка в архиве";
		Period p1 = Period.NONE;
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			str = options.get(key);
            str = StringTools.trimDoubleQuoteIfFound(str);
			p1 = ArchiveSettings.getHeaderPeriod(str);
			if(p1==Period.NONE) {
				data.errors.add("В параметре \"формат заголовка в архиве\" не задан переменный параметр. Значение этого параметра не принято.");
			} else {
				param.archSettings.headerFormat = str;
			}
		}
		
		key = "формат подзаголовка в архиве";
		Period p2 = Period.NONE;
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			str = options.get(key); 
            str = StringTools.trimDoubleQuoteIfFound(str);
			p2 = ArchiveSettings.getHeaderPeriod(str);
			if(p2==Period.NONE) {
				data.errors.add("В параметре \"формат подзаголовка в архиве\" не задан переменный параметр. Значение этого параметра не принято.");
			} else {
				param.archSettings.headerHeaderFormat = param.archSettings.headerFormat;
				param.archSettings.headerFormat = str;
			}			
		}
		
		if(p1!=Period.NONE && p2!=Period.NONE && p1==p2) {
			data.errors.add("Параметр \"формат заголовка в архиве\" и параметр \"формат подзаголовка в архиве\" имеют одинаковый период повторения "+p1.template());
		}
		
		//data.errors.addAll(validateArchiveFormat(archiveSettings));
		
		/*
		if(archiveSettings.headerFormat==null && archiveSettings.headerHeaderFormat==null) {
			archiveSettings.headerFormat = ArchiveSettings.DEFAULT_HEADER_FORMAT;
		} */
		
		
		
		key = "параметры архива";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			data.errors.addAll(parseArchiveSettings(param.archSettings,options.get(key)));
			//archiveSettings.headerFormat = options.get(key); 
		}
		
		key = "префикс";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {		
			param.prefix = options.get(key);
		}
		
		//boolean markEdits = true;
		param.bot = true;
		param.minor = true;
		key = "помечать правки";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			String mark = options.get(key).toLowerCase();
			if(mark.equalsIgnoreCase("нет")) {
				//markEdits = false;
				param.bot = false;
				param.minor = false;
			} else {				
				if(!mark.contains("бот") && mark.contains("малая")) {
					param.bot = false;
				} else if(mark.contains("бот") && !mark.contains("малая")) {
					param.minor = false;
				}				
			}
		}
		
		param.fastMode = DEFAULT_USE_FAST_MODE;
		key = "быстрый режим";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			param.fastMode = options.get(key).equalsIgnoreCase(YES_RU);
		}
		
		param.ns = DEFAULT_NAMESPACE;
		key = "пространство имён";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {			
			try {
				param.ns = Integer.parseInt(options.get(key));
			} catch(NumberFormatException e) {
				log.warn(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING, key, options.get(key)));
				data.errors.add(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING_RU, key, options.get(key)));
			}
		}
		
		param.header = DEFAULT_HEADER;
		key = "шапка";
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{
			param.header = options.get(key).replace("\\n", "\n");
		}
		
		param.footer = DEFAULT_FOOTER;
		key = "подвал";
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{
			param.footer = options.get(key).replace("\\n", "\n");
		}
		
		param.middle = DEFAULT_MIDDLE;
		key = "середина";
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{
			param.middle = options.get(key).replace("\\n", "\n");
		}		
		
		key = "шаблоны";
		if (options.containsKey(key)) {			
			String option = options.get(key);
			if (!option.isEmpty()) {
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
                    List<String> templates = optionToStringArray(option, true, sep);
                    if (!templates.isEmpty()) {
                        param.templateFilter = new SimpleTemplateFilter(templates, enumType);
    				}
				}
			}
		}
		
		key = "шаблоны с параметром";
		if (options.containsKey(key)) {			
			String option = options.get(key);
			if (!option.isEmpty()) {
				log.debug("param 'templates with parameter' detected");
                param.templateFilter = new ComplexTemplateFilter(
                        parseTemplatesWithData(option, data.errors),
                        EnumerationType.OR);
			}
		}
		
		param.format = DEFAULT_FORMAT;
		key = "формат элемента";
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{
            param.format = options.get(key);
            if (param.format.contains("%(автор)")) {
                if (isTypePages(type) || isTypeDiscussedPages(type)) {
                    data.errors.add(String.format(ERROR_AUTHOR_PARAMETER_INVALID_USAGE_RU, key));
                    param.format.replace("%(автор)", "");
                }
            }
		}


		param.depth = DEFAULT_DEPTH;
		key = "глубина";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {			
			try {
				param.depth = Integer.parseInt(options.get(key));
			} catch(NumberFormatException e) {
				log.warn(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING, key, options.get(key)));
				data.errors.add(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING_RU, key, options.get(key)));
			}
			if(param.depth>MAX_DEPTH) {
				data.errors.add(String.format(ERROR_INTEGER_TOO_BIG_STRING_RU, key, options.get(key),MAX_DEPTH));
				param.depth = MAX_DEPTH;				
			}
		}
		
		
		param.hours = DEFAULT_HOURS;
		key = "часов";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {		
			try {
				param.hours = Integer.parseInt(options.get(key));
			} catch(NumberFormatException e) {
				log.warn(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING, key, options.get(key)));
				data.errors.add(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING_RU, key, options.get(key)));
			}
			if(param.hours>param.service.MAX_HOURS) {
				data.errors.add(String.format(ERROR_INTEGER_TOO_BIG_STRING_RU, key, options.get(key),param.service.MAX_HOURS));
				param.hours = param.service.MAX_HOURS;				
			}
		}
		
		param.maxItems = DEFAULT_MAXITEMS;	
		key = "элементов";
		if (isTypeWithUnlimitedItems(type)) {
			param.maxItems = MAX_MAXITEMS;
		}
		//boolean maxItemsSetting = false;
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{		
			try {
				param.maxItems = Integer.parseInt(options.get(key));
				//maxItemsSetting = true;
			} catch(NumberFormatException e) {
				log.warn(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING, key, options.get(key)));
				data.errors.add(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING_RU, key, options.get(key)));
			}
			if (param.maxItems>MAX_MAXITEMS) {
				data.errors.add(String.format(ERROR_INTEGER_TOO_BIG_STRING_RU, key, options.get(key),MAX_MAXITEMS));
				param.maxItems = MAX_MAXITEMS;
			}
		}
		
		param.deletedFlag = DEFAULT_DELETED_FLAG;
		key = "удаленные статьи";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			param.deletedFlag = parseDeleted(options.get(key),param.deletedFlag,data.errors);
		}
		
		param.renamedFlag = DEFAULT_RENAMED_FLAG;
		key = "переименованные статьи";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {			
			param.renamedFlag = parseRenamed(options.get(key),param.renamedFlag,data.errors);
		}
				
		parseIntegerKeyWithMaxVal(options, "частота обновлений",param,data,"updatesPerDay",DEFAULT_UPDATES_PER_DAY, MAX_UPDATES_PER_DAY);
	
		param.delimeter = DEFAULT_DELIMETER;
		key = "разделитель";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			param.delimeter = options.get("разделитель").replace("\"", "").replace("\\n", "\n");
		}
		
		param.imageSearchTags = PICTURE_SEARCH_TAGS;
		key = "поиск картинки";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			param.imageSearchTags = options.get(key);
		}
		
		param.fairUseImageTemplates = FAIR_USE_IMAGE_TEMPLATES;
		key = "шаблоны запрещенных картинок";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			param.fairUseImageTemplates = options.get(key);
		}

		if (!validateParams(param,data.errors)) {
			return false;
		}
		
		if (!itsTimeToUpdate(param)) {
			// There is no errors but we should not update this portal now
			return true;
		}
		
		if (type.equals(LIST_TYPE_NEW_PAGES) || type.equals(LIST_TYPE_NEW_PAGES_OLD)) {
			data.portalModule = new NewPages(param);						
		} else if (isTypePages(type)) {
			data.portalModule = new Pages(param);
		} else if (type.equals("список новых статей с изображениями в карточке") || type.equals("новые статьи с изображениями в карточке")) {
			data.portalModule = new NewPagesWithImages(param, commons, new ImageFinderInCard(param.imageSearchTags));
		} else if (type.equals("список новых статей с изображениями в тексте") || type.equals("новые статьи с изображениями в тексте") ) {
			data.portalModule = new NewPagesWithImages(param, commons, new ImageFinderInBody());
		} else if (type.equals("список новых статей с изображениями") || type.equals("новые статьи с изображениями")) {
			data.portalModule = new NewPagesWithImages(param, commons, new ImageFinderUniversal(param.imageSearchTags));
		} else if (type.equals(LIST_TYPE_NEW_PAGES_7_DAYS) || type.equals(LIST_TYPE_NEW_PAGES_7_DAYS_OLD)) {
			data.portalModule = new NewPagesWeek(param);
		} else if (isTypeDiscussedPages(type)) {
			if (DISCUSSION_PAGES_SETTINGS == null) {
				data.errors.add("Настройки обсуждаемых страниц не заданы или некорректны. Невозможно обновлять этот вид списков.");
			} else {
				data.portalModule = new DiscussedPages(param, DISCUSSION_PAGES_SETTINGS);
			}
		} else {
			data.errors.add("Тип \""+type+"\" не поддерживается. Используйте только разрешенные значения в параметре \"тип\".");			
		}
	
		log.debug("portal settings init finished");
		return (data.portalModule != null);
	}

	/**
     * @param option
     * @return
     */
    private List<TemplateFindItem> parseTemplatesWithData(String option, List<String> errors) {
    	List<TemplateFindItem> templateDataItems = new ArrayList<>();
    	List<String> items = optionToStringArray(option, true, ",");
    	for (String item : items) {
    		try {
	            templateDataItems.add(TemplateFindItem.parseTemplateFindData(item));
            } catch (BadFormatException e) {
            	errors.add(String.format(ERROR_INVALID_PARAMETER_RU + ERROR_PARAMETER_NOT_ACCEPTED_RU, "шаблоны с параметром", item));
            }
    	}
	    return templateDataItems;
    }

	/**
     * @param type
     * @return
     */
    private boolean isTypeWithUnlimitedItems(String type) {
	    
	    return isTypePages(type) || isTypeDiscussedPages(type);
    }
    
    /**
     * @param type
     * @return
     */
    private boolean isTypePages(String type) {	    
	    return type.equals("список наблюдения") || type.equals("статьи") || type.equals("статьи с шаблонами") 
	    		|| type.equals("список страниц с заданными категориями и шаблонами");
    }
    
    private boolean isTypeDiscussedPages(String type) {
    	return type.equals("обсуждаемые статьи") || type.equals("список страниц с заданными категориями, шаблонами и обсуждением")
    			|| type.equals("статьи с шаблонами и обсуждением");
    }

	private void parseIntegerKeyWithMaxVal(Map<String, String> options, String key, PortalParam params, 
			NewPagesData data, String paramName, int DEF_VAL, int MAX_VAL) throws NoSuchFieldException, 
			SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = params.getClass().getField(paramName);
		//param.updatesPerDay = DEFAULT_UPDATES_PER_DAY;
		int val = DEF_VAL;
		if (options.containsKey(key) && !options.get(key).isEmpty()) {	
			try {
				val = Integer.parseInt(options.get(key));
			} catch(NumberFormatException e) {
				log.warn(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING, key, options.get(key)));
				data.errors.add(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING_RU, key, options.get(key)));
			}
			if(val>MAX_VAL) {
				data.errors.add(String.format(ERROR_INTEGER_TOO_BIG_STRING_RU, key, options.get(key),MAX_VAL));
				val = MAX_VAL;				
			}
		}
		field.set(params, val);		
	}
	
	private boolean validateParams(PortalParam param, List<String> errors) {
		boolean retval = true;
		if(param.categories.isEmpty()) {
			log.warn(String.format(ERROR_ABSENT_PARAMETER_RU, "категории"));
			errors.add(String.format(ERROR_ABSENT_PARAMETER_RU,"категории"));
			retval = false;
		}
		if(param.page.isEmpty()) {
			log.warn(String.format(ERROR_ABSENT_PARAMETER_RU, "статья"));
			errors.add(String.format(ERROR_ABSENT_PARAMETER_RU,"статья"));
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
			if(str.equals("старое название")) {
				flag = flag|PortalParam.RENAMED_OLD;
			} else if(str.equals("новое название")) {
				flag = flag|PortalParam.RENAMED_NEW;
			} /*else if(str.equals("помечать")) {
				flag = flag|PortalParam.RENAMED_MARK;
			}*/
		}
		if(flag==0) {
			flag = defaultValue;
			if(errors!=null) errors.add("Ошибка в параметре \"переименованные статьи\"");
		}		
		
		return flag;
	}

	public static ArrayList<String> parseArchiveSettings(ArchiveSettings archiveSettings,
			String settings) {
		ArrayList<String> errors = new ArrayList<String>();
		String items[];
		items = settings.split(",");
		ArrayList<String> itemsVector = new ArrayList<String>(items.length);
		for(String item:items) {
			itemsVector.add(item.trim());
		}		
		if(itemsVector.contains("сверху") && itemsVector.contains("снизу")) {
			errors.add(String.format(ERROR_PARAMETER_HAS_MULTIPLE_VALUES_RU, "параметры архива -> сверху/снизу"));
		} else if(itemsVector.contains("сверху")) {
			archiveSettings.addToTop = true;
		} else if(itemsVector.contains("снизу")) {
			archiveSettings.addToTop = false;
		}
		if(itemsVector.contains("сортировать")||itemsVector.contains("сортировка")) {
			archiveSettings.sorted = true;
		}
		if(itemsVector.contains("удалить дубликаты")) {
			archiveSettings.removeDuplicates = true;
		}
		int cnt = 0;
		if(itemsVector.contains("нумерация решетками")||itemsVector.contains("нумерация решётками")) {
			cnt++;
			archiveSettings.enumeration = Enumeration.HASH;
		}
		if(itemsVector.contains("нумерация кодом html")||itemsVector.contains("нумерация решётками")) {
			cnt++;
			archiveSettings.enumeration = Enumeration.HTML;
		}
		if(itemsVector.contains("глобальная нумерация кодом html")||itemsVector.contains("нумерация решётками")) {
			cnt++;
			archiveSettings.enumeration = Enumeration.HTML_GLOBAL;
		}
		if(cnt>1) {
			archiveSettings.enumeration = Enumeration.NONE;
			errors.add(String.format(ERROR_PARAMETER_HAS_MULTIPLE_VALUES_RU, "параметры архива -> нумерация"));
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
		
	protected static ArrayList<String> optionToStringArray(Map<String, String> options, String key, boolean replaceAdditionalSeparator) {
		ArrayList<String> list = new ArrayList<String>();
		if (options.containsKey(key)) {			
			String option = options.get(key);
			if (replaceAdditionalSeparator) {
				option = option.replace(ADDITIONAL_SEPARATOR, DEPTH_SEPARATOR);
			}
			return optionToStringArray(option, true);
		}
		return list;
	}
	
	protected static ArrayList<List<String>> multiOptionToArray(Map<String, String> options, String key, int start, int end) {
		ArrayList<List<String>> listlist = new ArrayList<List<String>>(end-start+1);
		String keyNumbered;
		for(int i=start;i<=end;i++) {
			keyNumbered = key + String.valueOf(i);
			List<String> list = optionToStringArray(options, keyNumbered, true);
			listlist.add(list);
		}
		return listlist;
	}
	
}
