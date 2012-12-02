/**
 *  @(#)NirvanaBot.java 1.3 20/10/2012
 *  Copyright © 2011 - 2012 Dmitry Trofimovich (KIN)
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.wikipedia.Wiki;
import org.wikipedia.nirvana.FileTools;
import org.wikipedia.nirvana.NirvanaBasicBot;
import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.StringTools;
import org.wikipedia.nirvana.archive.ArchiveSettings;
import org.wikipedia.nirvana.archive.ArchiveSettings.Enumeration;
import org.wikipedia.nirvana.archive.ArchiveSettings.Period;

/**
 * @author kin
 *
 */
public class NirvanaBot extends NirvanaBasicBot{
//	private static final String TESTING_PORTAL = "Участник:NirvanaBot/test/Новые статьи";

	//private static final boolean DEBUG_BUILD = false;
	private static int STOP_AFTER = 0;
	public static int UPDATE_PAUSE = 1000;
	//private static boolean DEBUG_DESTINATION = true;
	//private static boolean DEBUG_MODE = false;
	
	public static String TIME_FORMAT = "short"; // short,long
	
	private static boolean TASK = false;
	private static String TASK_LIST_FILE = "task.txt";
	
	//private static Properties properties = null;
	
	private static int START_FROM = 0;
	
	
	
	//public static org.apache.log4j.Logger log = null;
	
	//public static NirvanaWiki wiki;
	
	private static String newpagesTemplateName = null;
	
	public static final String ERROR_PARSE_INTEGER_FORMAT_STRING = "Error when parsing integer parameter \"%1$s\" integer value %2$s";
	private static final String ERROR_PARSE_INTEGER_FORMAT_STRING_RU = "Ошибка при чтении параметра \"%1$s\". Значение %2$s не распознано как число.";
	private static final String ERROR_INTEGER_TOO_BIG_STRING_RU = "Значение параметра \"%1$s\" слишком велико (%2$s). Использовано максимальное значение (%3$s). Укажите разумную величину.";
	@SuppressWarnings("unused")
	private static final String ERROR_NOTIFICATION_TEXT = "При проверке параметров были обнаружены ошибки. %1$s Напишите [[%1$s|мне]], если нужна помощь в настройке параметров. ~~~~";
	private static final String ERROR_PARAMETER_HAS_MULTIPLE_VALUES_RU = "Для параметра \"%1$s\" дано несколько значений. Использовано значение по умолчанию.";
	private static final String ERROR_INVALID_PARAMETER = "Error in parameter \"%1$s\". Invalid value (%2$s).";
	private static final String ERROR_INVALID_PARAMETER_RU = "Ошибка в параметре \"%1$s\". Задано неправильное значение (%2$s).";
	
	private static int MAX_DEPTH = 30;
	private static int DEFAULT_DEPTH = 15;
	private static int MAX_MAXITEMS = 5000;
	private static int DEFAULT_MAXITEMS = 20;
	private static int MAX_HOURS = 720;
	private static int DEFAULT_HOURS = 720;
	private static boolean ERROR_NOTIFICATION = false;
	private static String LANGUAGE= "ru";
	private static String COMMENT = "обновление";
	
	private static boolean GENERATE_REPORT = false;
	private static String REPORT_FILE_NAME = "report.txt";
	private static String REPORT_FORMAT = "txt";
	private static String DEFAULT_FORMAT = "* [[%(название)]]";
	private static String DEFAULT_TYPE = "список новых статей";
	
	private static String TYPE = "all";
	
	private static String DEFAULT_DELIMETER = "\n";
	
	private static int RETRY_MAX = 1;
	
	private static String newpagesTemplate = null;
	
	private static String overridenPropertiesPage = null;
	
	private static String PICTURE_SEARCH_TAGS = "image file,Фото,портрет,Изображение";
	
	private static int DEFAULT_NAMESPACE = 0;
	
	private static String DEFAULT_HEADER = null;
	private static String DEFAULT_FOOTER = null;
	private static String DEFAULT_MIDDLE = null;
	private static PortalParam.Deleted DEFAULT_DELETED_FLAG = PortalParam.Deleted.DONT_TOUCH;
	private static int DEFAULT_RENAMED_FLAG = PortalParam.RENAMED_NEW;
	private static int DEFAULT_PARSE_COUNT = -1;
	private NirvanaWiki commons = null;
	
	private static class NewPagesData {
		ArrayList<String> errors;
		PortalModule portalModule;
		String type = "";
		NewPagesData() {
			errors = new ArrayList<String>();
			portalModule = null;
		}
	}
	//public static final int STATUS_SKIP = 0;
	//public static final int STATUS_PROCESSED = 1;
	enum Status {
		NONE,
		SKIP,
		PROCESSED,
		UPDATED,
		//DENIED,
		ERROR
	};
	public static String PROGRAM_INFO = 
			"NirvanaBot v1.4 Updates Portal/Project sections at http://ru.wikipedia.org\n" +
			"Copyright (C) 2011-2012 Dmitry Trofimovich (KIN)\n" +
			"\n";
			
	public void showInfo() {
		System.out.print(PROGRAM_INFO);
	}
		
	/**
	 * 
	 */
	public NirvanaBot() {
		
	}
	
	public static void main(String[] args) {
		NirvanaBasicBot bot = new NirvanaBot();
		bot.showInfo();
		bot.showLicense();
		System.out.print("-----------------------------------------------------------------\n");
		String configFile = bot.getConfig(args);		
		System.out.println("applying config file: "+configFile);
		bot.startWithConfig(configFile);
	}
	
	protected void initLog() {
		String log4jSettings = properties.getProperty("log4j-settings");
		if(log4jSettings==null || log4jSettings.isEmpty() || !(new File(log4jSettings)).exists()) {
			System.out.println("INFO: logs disabled");
			Properties prop_no_logs = new Properties();
			prop_no_logs.setProperty("log4j.rootLogger", "OFF");
			PropertyConfigurator.configure(prop_no_logs);
			log = org.apache.log4j.Logger.getLogger(NirvanaBot.class.getName());
			//log.setLevel(Level.OFF);
			//nologs = true;
		} else {
			PropertyConfigurator.configure(log4jSettings);
			log = org.apache.log4j.Logger.getLogger(NirvanaBot.class.getName());	
			System.out.println("INFO: using log settings : " + log4jSettings);
		}
	}
	
	protected void loadCustomProperties() {
		newpagesTemplate = properties.getProperty("new-pages-template");
		if(newpagesTemplate==null) {
			if(DEBUG_BUILD)
				newpagesTemplate = "Участник:NirvanaBot/test/Новые статьи";
			else {
				System.out.println("ABORT: properties not found");
				log.fatal("New pages template name (new-pages-template) is not specified in settings");
				return;
			}
		}	
		log.info("new pages template : "+newpagesTemplate);	
		
		
		ERROR_NOTIFICATION = properties.getProperty("error-notification",ERROR_NOTIFICATION?YES:NO).equals(YES);
		log.info("error_notification="+ERROR_NOTIFICATION);
		

		DEFAULT_DEPTH = validateIntegerSetting(properties,"default-depth",DEFAULT_DEPTH,true);		
		log.info("depth="+DEFAULT_DEPTH);
		DEFAULT_HOURS = validateIntegerSetting(properties,"default-hours",DEFAULT_HOURS,true);
		log.info("hours="+DEFAULT_HOURS);
		DEFAULT_MAXITEMS = validateIntegerSetting(properties,"default-maxitems",DEFAULT_MAXITEMS,true);
		log.info("maxitems="+DEFAULT_MAXITEMS);		
		STOP_AFTER = validateIntegerSetting(properties,"stop-after",STOP_AFTER,false);		
		UPDATE_PAUSE = validateIntegerSetting(properties,"update-pause",UPDATE_PAUSE,false);
		
		DEFAULT_PARSE_COUNT = validateIntegerSetting(properties,"parse-count",DEFAULT_PARSE_COUNT,false);
		
		//if(UPDATE_LIMIT>0) UPDATE_LIMIT_ENABLED = true;
		START_FROM = validateIntegerSetting(properties,"start-from",START_FROM,false);

		TIME_FORMAT = properties.getProperty("time-format",TIME_FORMAT);
		if(!TIME_FORMAT.equalsIgnoreCase("short") && !TIME_FORMAT.equalsIgnoreCase("long") )
			TIME_FORMAT = "short";
		
		TASK = properties.getProperty("task-list",TASK?YES:NO).equals(YES);
		log.info("task list : "+(TASK?YES:NO));
		TASK_LIST_FILE = properties.getProperty("task-list-file",TASK_LIST_FILE);
		log.info("task list file: "+TASK_LIST_FILE);
		
		GENERATE_REPORT = properties.getProperty("statistics",GENERATE_REPORT?YES:NO).equals(YES);
		REPORT_FILE_NAME = properties.getProperty("statistics-file",REPORT_FILE_NAME);
		REPORT_FORMAT = properties.getProperty("statistics-format",REPORT_FORMAT);
		if(REPORT_FILE_NAME.contains("%(date)")) {
			//Calendar c = Calendar.getInstance();
			String date = String.format("%1$tF", Calendar.getInstance());
			REPORT_FILE_NAME = REPORT_FILE_NAME.replace("%(date)", date);
		}
		
		PICTURE_SEARCH_TAGS = properties.getProperty("picture-search-tags",PICTURE_SEARCH_TAGS);
		log.info("picture search tags = "+PICTURE_SEARCH_TAGS);
		
		TYPE = properties.getProperty("type",TYPE); 
		
		overridenPropertiesPage = properties.getProperty("overriden-properties-page",null);
	}
	
	protected void go() {	
		commons = new NirvanaWiki("commons.wikimedia.org");
		commons.setMaxLag( 15 );
		go(newpagesTemplate);
	}
	
	private void loadOverridenProperties() throws IOException	{
		if(overridenPropertiesPage==null || overridenPropertiesPage.isEmpty())
			return;
		log.info("loading overriden properties from page "+overridenPropertiesPage);
		String overridenPropertiesText = null;
		try {
			overridenPropertiesText = wiki.getPageText(overridenPropertiesPage);
		} catch (IOException e) {			
			log.fatal("Failed to read overriden properties page: "+overridenPropertiesPage);
			throw e;
		}
		Map<String, String> options = new HashMap<String, String>();
		if(TryParseTemplate(overridenPropertiesText,options)) {			
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
			
			key = "часов";
			if (options.containsKey(key) && !options.get(key).isEmpty()) {		
				try {
					DEFAULT_HOURS = Integer.parseInt(options.get(key));
				} catch(NumberFormatException e) {
					log.warn(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING, key, options.get(key)));
				}
				if(DEFAULT_HOURS>MAX_HOURS) {
					DEFAULT_HOURS = MAX_HOURS;				
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
				DEFAULT_TYPE = options.get("тип").toLowerCase();
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
		}
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
	
	@SuppressWarnings("unused")
	void go(String template) {
		Calendar cStart = Calendar.getInstance();
		long start = cStart.getTimeInMillis();
		//Date d = cStart.getTime();
		log.info("BOT STARTED at "+cStart.get(Calendar.HOUR_OF_DAY)+":"+cStart.get(Calendar.MINUTE));
		log.info("template to check: "+template);
		
		newpagesTemplateName = template;
		
		try {
			loadOverridenProperties();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		// 1 extract portal list
		String []portalNewPagesLists = null;
		//List<String>
		try {
			portalNewPagesLists = wiki.whatTranscludesHere(template, Wiki.ALL_NAMESPACES);
		} catch (IOException e) {			
			log.fatal("failed to get portal list");
			return;
		}
		java.util.Arrays.sort(portalNewPagesLists);
		
		ArrayList<String> tasks = null;
		if(TASK) {
			File taskFile = new File(TASK_LIST_FILE);
			FileReader fr = null;
			try {
				fr = new FileReader(taskFile);
				BufferedReader br = new BufferedReader(fr);
				tasks = new ArrayList<String>();
				String line;
				while((line=br.readLine())!=null) {
					tasks.add(line);
				}
			} catch (FileNotFoundException e) {
				log.error(e.toString());
				return;
			} catch (IOException e) {
				log.error(e.toString());
				return;
			}
			
		}
		
/*		for(String str:portalNewPagesLists) {
			log.debug(str);
		}*/
		int i = 0;	// текущий портал
		int t = 0;	// количество проверенных порталов
		int j = 0;  // количество обработанных порталов
		int k = 0;  // количество на самом деле проверенных порталов
		int l = 0;	// количество обновленных порталов
		//int max = 10;
		int er = 0;
		int retry_count = 0;
		boolean retry = false;
		ArrayList<ReportItem> report = null;
		if(GENERATE_REPORT) {
			report = new ArrayList<ReportItem>();
		}
		//ArrayList<String> errorPortals = new ArrayList<String>();
		ReportItem reportItem = null;
		String portalName = null;
		while(i < portalNewPagesLists.length) {						
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
				reportItem = new ReportItem(portalName);
				if(report!=null) report.add(reportItem);
				reportItem.startTime = System.currentTimeMillis();
			}
			
			
			
			//if(DEBUG_BUILD && !portalName.endsWith(TESTING_PORTAL)) {log.info("SKIP portal: "+portalName);	continue;}
			
			if(tasks!=null) {
				boolean skip = true;
				for(String task : tasks) {
					log.debug("task: "+task);
					if(portalName.startsWith(task)) {
						skip = false;
						break;
					}
				}
				if(skip) { log.info("SKIP portal: "+portalName); reportItem.skip(); i++; continue; }
			}
			if(retry_count==0) t++;
			if(t<START_FROM) {log.info("SKIP portal: "+portalName);	reportItem.skip(); i++; continue;}
			
			if(retry_count==0) k++;
			
			try {				
				
				String portalSettingsText = wiki.getPageText(portalName);
				
				if(DEBUG_MODE) {					
					FileTools.dump(portalSettingsText, "dump", portalName+".settings.txt");
				}
				Map<String, String> parameters = new HashMap<String, String>();
				if(TryParseTemplate(portalSettingsText,parameters)) {
					log.info("validate portal settings OK");					
					Set<Entry<String,String>> set = parameters.entrySet();
					Iterator<Entry<String,String>> it = set.iterator();
					while(it.hasNext()) {
						Entry<String,String> next = it.next();
						log.debug(next.getKey()+" = "+next.getValue());
					}
					NewPagesData data = new NewPagesData();
					createPortalModule(parameters,data);
					if(TYPE.equals("all") || TYPE.equals(data.type)) {
						if(data.portalModule!=null) {							
								if(DEBUG_MODE || !DEBUG_BUILD || !portalName.contains("ValidParam") /*&& !portalName.contains("Testing")*/) {
									if(data.portalModule.update(wiki, reportItem, COMMENT)) {
										l++;
										reportItem.status = Status.UPDATED;
									} else {
										reportItem.status = Status.PROCESSED;
									}
									
									j++;
									if(UPDATE_PAUSE>0) Thread.sleep(UPDATE_PAUSE);
								}
							
						} else {
							log.warn("portal module not created");
						}
					} else {
						reportItem.skip();						
						log.info("SKIP portal: "+portalName); 
					}
					if(!data.errors.isEmpty()) {
						log.warn("errors occured during checking settings");
						for(String str:data.errors) {
							log.info(str);
						}
						reportItem.errors = data.errors.size();
						String errorText = StringUtils.join(data.errors, "\n");
						FileTools.dump(errorText, "dump", portalName+".err");
						if(ERROR_NOTIFICATION) {
							for(String err:data.errors) {
								log.info(err);
							}
						}
					}
				} else {
					reportItem.settingsValid = false;
					log.error("validate portal settings FAILED");
				}
				
			} catch (IndexOutOfBoundsException e) { // includes ArrayIndexOfBoundsException
				log.error(e.toString());
				log.trace("OOOOPS!!!", e);
				if(retry_count<RETRY_MAX) {
					log.info("RETRY AGAIN");
					retry = true;
				} else {
					er++;
					reportItem.status = Status.ERROR;
					e.printStackTrace();
				}	
			} catch (NullPointerException e) {
				log.error(e.toString());
				log.trace("OOOOPS!!!", e);
				if(retry_count<RETRY_MAX) {
					log.info("RETRY AGAIN");
					retry = true;
				} else {
					er++;
					reportItem.status = Status.ERROR;
					e.printStackTrace();
				}	
			} catch (java.util.zip.ZipException e) {
				log.error(e.toString());
				if(retry_count<RETRY_MAX) {
					log.info("RETRY AGAIN");
					retry = true;
				} else {
					er++;
					reportItem.status = Status.ERROR;
					e.printStackTrace();
				}
			} catch (IOException e) {
				
				if(retry_count<RETRY_MAX) {
					log.warn(e.toString());
					log.info("RETRY AGAIN");
					retry = true;
				} else {
					log.error(e.toString());
					er++;
					reportItem.status = Status.ERROR;
					e.printStackTrace();
				}
			} catch (LoginException e) {
				log.fatal(e.toString());
				break;
				//e.printStackTrace();
			} catch (InterruptedException e) {				
				log.fatal(e.toString());
				break;
			}/* catch (Exception e) {
				log.error(e.toString());
				log.trace("OOOOPS!!!", e);
				if(retry_count<RETRY_MAX) {
					log.info("RETRY AGAIN");
					retry = true;
				} else {
					er++;
					reportItem.status = Status.ERROR;
					e.printStackTrace();
				}	
			}*/
			reportItem.endTime = System.currentTimeMillis();
			if(STOP_AFTER>0 && j>=STOP_AFTER) break;
			if(!retry) i++;
		}
		//wiki.n
		Calendar cEnd = Calendar.getInstance();
		long end = cEnd.getTimeInMillis();
		//long start = cStart.getTimeInMillis();
		//log.debug("start "+start+" end "+end);
		int hours = (int) ((end-start)/(60L*60L*1000L));
		int min = (int) ((end-start)/(60L*1000L) - (long)hours*60L);
		int sec = (int) ((end-start)/(1000L) - (long)hours*60L*60L - (long)min*60L);
		
		log.info("BOT FINISHED at "+String.format("%1$tT", cEnd));
		log.info("WORK TIME: "+String.valueOf(hours)+" h "+String.valueOf(min)+" m "+sec+" s");
		log.info("portals: "+String.valueOf(portalNewPagesLists.length)
				+", checked: "+String.valueOf(k)
				+", processed: "+String.valueOf(j)
				+", updated: "+String.valueOf(l)
				+", errors: "+String.valueOf(er));
		
		if(GENERATE_REPORT) {
			log.info("generating report . . .");
			File rep = new File(REPORT_FILE_NAME);
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(rep));
				out.append(ReportItem.getHeader());
				out.newLine();
				for(ReportItem item : report) {
					out.append(item.toString());
					out.newLine();
				}
				out.append(ReportItem.getFooter());				
				out.close();
			} catch (IOException e) {
				log.error(e.toString());
				e.printStackTrace();				
			}
			log.info("report is generated!");
		}
		
	}
	
	private static boolean TryParseTemplate(String text, Map<String, String> parameters)
    {
		log.debug("portal settings parse started");
        //parameters = null;
        //String str = "^{{"+newpagesTemplateName+".*({{.+({{.+}})?.*}})?.*}}.*$";
        //String str = "^\\{\\{"+newpagesTemplateName+".*\\}\\}.*$"; // works
        //String str = "^(\\{\\{"+newpagesTemplateName+".*(\\{\\{.+\\}\\})?.*\\}\\})(.*)$";
        //String name = newpagesTemplateName.substring(0, newpagesTemplateName.length()-1);
        //char last = newpagesTemplateName.charAt(newpagesTemplateName.length()-1);
		String recognizeTemplate = newpagesTemplateName;
		String ns1 = "Участник:";
		String ns2 = "User:";
		if(recognizeTemplate.startsWith(ns1))  {
			recognizeTemplate = recognizeTemplate.substring(ns1.length());			
		} else if (recognizeTemplate.startsWith(ns2)) {
			recognizeTemplate = recognizeTemplate.substring(ns2.length());			
		}
		recognizeTemplate = "("+ns1+"|"+ns2+")"+recognizeTemplate;
        String str = "^(\\{\\{"+recognizeTemplate+")(.+)$"; // GOOD
        //String str = "(\\{\\{"+newpagesTemplateName+"(.*(\\{\\{.+?\\}\\})*.*))+?\\}\\}";
        //String str = "(\\{\\{"+newpagesTemplateName+"(.*(\\{\\{[^\\{\\}]+\\}\\})*[^\\{]*))\\}\\}";
        //log.debug("pattern:"+str);
        Pattern pattern = Pattern.compile(str,Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
        //Regex templateRE = new Regex(@"\{\{(User):ClaymoreBot/Новые стать(и).",
          //  RegexOptions.IgnoreCase | RegexOptions.Singleline);
        Matcher m = pattern.matcher(text);
       // Match m = templateRE.Match(text);
        /*if(!m.find())
        	return false;
        */
        if (!m.matches())
        {
        	log.error("portal settings parse error (doesn't match pattern)");
            return false;
        }
        
        log.debug("group count = "+m.groupCount());
        //for(int i=0;i<m.groupCount();i++) {
        //	log.debug("group "+(i)+": "+m.group(i));
        //}
        
        //text.substring(m.end(1),text.length());
        
        
        int index = 1;
        int begin = m.end(1) + 1;
        int end = -1;
        for (int i = begin; i < text.length() - 1; ++i)
        {
            if (text.charAt(i) == '{' && text.charAt(i+1) == '{')
            {
                ++index;
            }
            else if (text.charAt(i) == '}' && text.charAt(i+1) == '}')
            {
                --index;
                if (index == 0)
                {
                    end = i;
                    break;
                }
            }
        }

        if (end == -1)
        {
            return false;
        }
        Pattern commentPattern = Pattern.compile("<!--(.+?)-->");
        
        String parameterString = text.substring(begin, end);
        //log.debug("parameter string: "+parameterString);
        String[] ps = parameterString.split("\\|");
        String lastKey = "";
        String lastVal = "";
        for(int i =0;i<ps.length;i++) {
        	String p = ps[i];
            //p.
            //Pattern equalPattern = Pattern.compile("([^=])=");
        	//log.debug("checking string: "+p);
        	boolean newStringToLastVal = false;
        	int count = StringTools.howMany(p, '=');
        	if(count==0 && i==0) continue;
        	if(!lastVal.isEmpty() && lastVal.endsWith("{")) { // {| означает начало таблицы
        		newStringToLastVal = true;        		
        	} else if(count>0) {
        		int eq = p.indexOf('=');
        		String first = p.substring(0,eq); 
        		String last = p.substring(eq+1);
        		String key = first.trim().toLowerCase();
        		if(key.equals("align") || key.equals("style")) {
        			newStringToLastVal = true;
        		} else {
	        		Matcher mComment = commentPattern.matcher(last);
	            	String value = mComment.replaceAll("").trim();
	        		parameters.put(key, value);
	                lastKey = key;        	
	                lastVal = value;
        		}
        	} else {
        		if (!lastKey.isEmpty())
                {
        			newStringToLastVal = true;                	
                }
        	}  
        	if(newStringToLastVal) {
        		Matcher mkey = commentPattern.matcher(p);
                String value = mkey.replaceAll("").trim();
                //parameters[lastKey] = parameters[lastKey] + "|" + value;
                lastVal = parameters.get(lastKey)+"|"+value;
                parameters.put(lastKey, lastVal);
        	}
        }
        log.debug("portal settings parse finished");
        return true;
    }


	public boolean createPortalModule(Map<String, String> options, NewPagesData data) {
		log.debug("portal settings init started");
		String key;
		
		PortalParam param = new PortalParam();
		param.lang = NirvanaBot.LANGUAGE;
		
		String type = null;
		if(!options.containsKey("тип") || options.get("тип").isEmpty()) {
			data.errors.add("Параметр \"тип\" не задан. Использовано значение по умолчанию ");
			type = DEFAULT_TYPE;
		} else {
			type = options.get("тип").toLowerCase();
		}	
		log.debug("тип: "+type);
		data.type = type;
		
		param.categories = optionToStringArray(options,"категории");
		if (options.containsKey("категория"))
		{
			param.categories.add(options.get("категория"));
		}		
		
		param.categoriesToIgnore = optionToStringArray(options,"игнорировать");
				
		param.usersToIgnore = optionToStringArray(options,"игнорировать авторов");
		
		param.page = "";
		if (options.containsKey("страница"))
		{
			param.page = options.get("страница");
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
		
		/*
		String prefix = "";
		if (options.containsKey("префикс"))
		{
			prefix = options.get("префикс");
		}*/
		
		//boolean markEdits = true;
		param.bot = true;
		param.minor = true;
		if (options.containsKey("помечать правки") && !options.get("помечать правки").isEmpty()) {
			String mark = options.get("помечать правки").toLowerCase();
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
		
		/*
		String templates = "";
		if (options.containsKey("шаблоны"))
		{
			templates = options.get("шаблоны").replace("\\n", "\n");
		}*/
		
		param.format = DEFAULT_FORMAT;
		if (options.containsKey("формат элемента"))
		{
			param.format = options.get("формат элемента");//.replace("{", "{{").replace("}", "}}");
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
			if(param.hours>MAX_HOURS) {
				data.errors.add(String.format(ERROR_INTEGER_TOO_BIG_STRING_RU, key, options.get(key),MAX_HOURS));
				param.hours = MAX_HOURS;				
			}
		}
		
		param.maxItems = DEFAULT_MAXITEMS;	
		key = "элементов";
		if(type.equals("список наблюдения"))
			param.maxItems = MAX_MAXITEMS;
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
			if(param.maxItems>MAX_MAXITEMS) {
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
		
		/*
		int normalSize = 40 * 1000;
		key = "нормальная";
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{		
			try {
				normalSize = Integer.parseInt(options.get(key));
			} catch(NumberFormatException e) {
				log.warn(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING, key, options.get(key)));
				data.errors.add(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING_RU, key, options.get(key)));
			}
		}
		
		int shortSize = 10 * 1000;
		key = "небольшая";
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{		
			try {
				shortSize = Integer.parseInt(options.get(key));
			} catch(NumberFormatException e) {
				log.warn(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING, key, options.get(key)));
				data.errors.add(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING_RU, key, options.get(key)));
			}
		}
		
		String longColor = "#F2FFF2";
		if (options.containsKey("цвет крупной"))
		{
			longColor = options.get("цвет крупной");
		}
		
		String shortColor = "#FFE8E9";
		if (options.containsKey("цвет небольшой"))
		{
			//archive = options.get("цвет небольшой");
		}
		
		String normalColor = "#FFFDE8";
		if (options.containsKey("цвет нормальной"))
		{
			//archive = options.get("цвет нормальной");
		}*/
		
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
		
		
		if (type.equals("список новых статей")) {
			data.portalModule = new NewPages(param);						
		}
		else if (type.equals("список наблюдения")) {
			data.portalModule = new WatchList(param);
		}
		else if (type.equals("отсортированный список статей, которые должны быть во всех проектах")) {
			/*module = new EncyShell(portal,
			title,
			shortSize,
			normalSize,
			shortColor,
			normalColor,
			longColor);*/
			}//"Монета:Аверс,Реверс,Изображение аверса,Изображение реверса;image file,Фото,портрет,Изображение"
		else if (type.equals("список новых статей с изображениями в карточке")) {
			data.portalModule = new NewPagesWithImages(param, commons, new ImageFinderInCard(param.imageSearchTags));
		}
		else if (type.equals("список новых статей с изображениями в тексте") ) {
			data.portalModule = new NewPagesWithImages(param, commons, new ImageFinderInBody());
		} 
		else if (type.equals("список новых статей с изображениями")) {
			data.portalModule = new NewPagesWithImages(param, commons, new ImageFinderUniversal(param.imageSearchTags));
		}
		else if (type.equals("списки новых статей по дням")) {
			/*data.portalModule = new NewPagesWithWeeks(LANGUAGE,
				    categories,
				    categoriesToIgnore,
				    usersToIgnore,
				    title, archive, 
				    ns, depth, hours, maxItems,
				    format, delimeter, header, footer,
				    minor, bot);*/
		}
		else if (type.equals("список страниц с заданными категориями и шаблонами")) {
			/*
		module = new CategoryTemplateIntersection(portal,
		    categories,
		    categoriesToIgnore,
		    templates,
		    title,
		    ns,
		    depth,
		    hours,
		    maxItems,
		    format,
		    delimeter,
		    header,
		    footer,
		    markEdits);*/
		}
		else if (type.equals("список страниц с заданными категориями, шаблонами и обсуждением"))
		{/*
		module = new CategoryIntersectionAndTalkPages(portal,
		    categories,
		    categoriesToIgnore,
		    templates,
		    prefix,
		    title,
		    ns,
		    depth,
		    hours,
		    maxItems,
		    format,
		    delimeter,
		    header,
		    footer,
		    markEdits);*/
		} else {
			data.errors.add("Тип \""+type+"\" не поддерживается. Используйте только разрешенные значения в параметре \"тип\".");			
		}
	
	log.debug("portal settings init finished");
	return true;
	}
	/*
	private static Collection<? extends String> validateArchiveFormat(
			ArchiveSettings archiveSettings) {
		ArrayList<String> errors = new ArrayList<String>();
		
		if(archiveSettings.headerHeaderFormat!=null) {
			ArchiveSettings.Period p1 = ArchiveSettings.getHeaderPeriod(archiveSettings.headerHeaderFormat);
			if(p1==Period.NONE) {
				errors.add("")
			}
		}
		return errors;
	}*/
	
	

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
		
		/*if(itemsVector.contains("категории")) {
			archiveSettings.supportCategory = true;
		}*/
		
		//boolean byYear = false,byQarter=false,byMonth=false,byWeek=false,byDay=false;
		
		

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
	

	public static ArrayList<String> optionToStringArray(Map<String, String> options, String key) {
		ArrayList<String> list = new ArrayList<String>();
		if (options.containsKey(key)) {
			String separator;
			String str = options.get(key);
			if (str.contains("\"")) {
				separator = "\",";
			} else {
				separator = ",";
			}
			String[] items = str.split(separator);
			for (int i = 0; i < items.length; ++i) {
				String cat = items[i].replace("\"", "").trim();
				if (!cat.isEmpty()) {
					list.add(cat);
				}
			}
		}
		return list;
	}
	
	
	
}
