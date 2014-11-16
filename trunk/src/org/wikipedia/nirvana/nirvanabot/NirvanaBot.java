/**
 *  @(#)NirvanaBot.java 1.10 16.11.2014
 *  Copyright � 2011 - 2014 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.StringUtils;
import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Error504;
import org.wikipedia.nirvana.FileTools;
import org.wikipedia.nirvana.NirvanaBasicBot;
import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.ServiceError;
import org.wikipedia.nirvana.WikiTools;
import org.wikipedia.nirvana.archive.ArchiveSettings;
import org.wikipedia.nirvana.archive.ArchiveSettings.Enumeration;
import org.wikipedia.nirvana.archive.ArchiveSettings.Period;
import org.wikipedia.nirvana.nirvanabot.imagefinder.ImageFinderInBody;
import org.wikipedia.nirvana.nirvanabot.imagefinder.ImageFinderInCard;
import org.wikipedia.nirvana.nirvanabot.imagefinder.ImageFinderUniversal;

/**
 * @author kin
 *
 */
public class NirvanaBot extends NirvanaBasicBot{
	private String userNamespace;
	public static final String DEPTH_SEPARATOR = "|";
    public static final String ADDITIONAL_SEPARATOR = "#";
    
	public static final String YES_RU = "��";
	public static final String NO_RU = "���";

	private static int START_FROM = 0;
	private static int STOP_AFTER = 0;
	public static int UPDATE_PAUSE = 1000;
	
	public static String TIME_FORMAT = "short"; // short,long
	
	private static boolean TASK = false;
	private static String TASK_LIST_FILE = "task.txt";
	
	private String newpagesTemplates[] = null;
	
	public static final String ERROR_PARSE_INTEGER_FORMAT_STRING = "Error when parsing integer parameter \"%1$s\" integer value %2$s";
	private static final String ERROR_PARSE_INTEGER_FORMAT_STRING_RU = "������ ��� ������ ��������� \"%1$s\". �������� %2$s �� ���������� ��� �����.";
	private static final String ERROR_INTEGER_TOO_BIG_STRING_RU = "�������� ��������� \"%1$s\" ������� ������ (%2$s). ������������ ������������ �������� (%3$s). ������� �������� ��������.";
	@SuppressWarnings("unused")
	private static final String ERROR_NOTIFICATION_TEXT = "��� �������� ���������� ���� ���������� ������. %1$s �������� [[%1$s|���]], ���� ����� ������ � ��������� ����������. ~~~~";
	private static final String ERROR_PARAMETER_HAS_MULTIPLE_VALUES_RU = "��� ��������� \"%1$s\" ���� ��������� ��������. ������������ �������� �� ���������.";
	private static final String ERROR_INVALID_PARAMETER = "Error in parameter \"%1$s\". Invalid value (%2$s).";
	private static final String ERROR_INVALID_PARAMETER_RU = "������ � ��������� \"%1$s\". ������ ������������ �������� (%2$s).";
	private static final String ERROR_ABSENT_PARAMETER_RU = "������ � ����������. �������� \"%1$s\" �� �����";
	//private static final String ERROR_NO_CATEGORY = "������ � ����������. ��������� �� ������ (��. �������� ���������/���������).";
	//private static final String ERROR_NO_ARTICLE = "������ � ����������. �������� \"������\" �� �����.";
	//private static final String ERROR_INVALID_SERVICE_NAME = "������ � ����������. �������� \"������\" �� �����.";
	
	private static int MAX_DEPTH = 30;
	private static int DEFAULT_DEPTH = 7;
	private static int MAX_MAXITEMS = 5000;
	private static int DEFAULT_MAXITEMS = 20;
	private static int DEFAULT_HOURS = 500;
	private static WikiTools.Service DEFAULT_SERVICE = WikiTools.Service.CATSCAN2;
	private static String DEFAULT_SERVICE_NAME = DEFAULT_SERVICE.name();
	private static boolean DEFAULT_USE_FAST_MODE = true;
	private static boolean ERROR_NOTIFICATION = false;
	private static String COMMENT = "����������";
	
	private static boolean GENERATE_REPORT = false;
	private static boolean UPDATE_STATUS = false;
	private static String REPORT_FILE_NAME = "report.txt";
	private static String REPORT_WIKI_PAGE = "��������:NirvanaBot/����� ������/�����";
	private static String STATUS_WIKI_PAGE = "��������:NirvanaBot/����� ������/������";
	private static String STATUS_WIKI_TEMPLATE = "��������:NirvanaBot/����� ������/����������� �������";
	private static String REPORT_FORMAT = "txt";
	private static String DEFAULT_FORMAT = "* [[%(��������)]]";
	private static String DEFAULT_TYPE = "������ ����� ������";
	
	private static String TYPE = "all";
	
	private static String DEFAULT_DELIMETER = "\n";
	
	private static int RETRY_MAX = 1;

	
	private static String overridenPropertiesPage = null;
	
	private static String PICTURE_SEARCH_TAGS = "image file,����,�������,�����������,����,File";
	
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
		
	
	private int startNumber = 1;
	
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

	public enum Status {
		NONE,
		SKIP,
		PROCESSED,
		UPDATED,
		//DENIED,
		ERROR
	};
	
	public enum BotError {
		NONE,
		UNKNOWN_ERROR,
		BOT_ERROR,
		SERVICE_ERROR,		
		IO_ERROR,
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
	
	public static String PROGRAM_INFO = 
			"NirvanaBot v1.10 Updates Portal/Project sections at http://ru.wikipedia.org and collects statistics\n" +
			"See also http://ru.wikipedia.org/User:NirvanaBot\n" +
			"Copyright (C) 2011-2014 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)\n" +
			"\n";
			
	public void showInfo() {
		System.out.print(PROGRAM_INFO);
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
		bot.run(args);
	}
	
	@Override
	protected boolean loadCustomProperties(Map<String,String> launch_params) {
		String str = properties.getProperty("new-pages-template");
		newpagesTemplates = str.trim().split("\\s*,\\s*");
		if(newpagesTemplates==null || newpagesTemplates.length==0) {
			if(DEBUG_BUILD)
				newpagesTemplates = new String[]{"��������:NirvanaBot/test/����� ������"};
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
		
		DEFAULT_SERVICE_NAME = validateService(properties.getProperty("service",DEFAULT_SERVICE_NAME), DEFAULT_SERVICE_NAME);
		DEFAULT_SERVICE = WikiTools.Service.getServiceByName(DEFAULT_SERVICE_NAME, DEFAULT_SERVICE);
		
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
		
		TYPE = properties.getProperty("type",TYPE); 
		
		overridenPropertiesPage = properties.getProperty("overriden-properties-page",null);
		
		return true;
	}
	
	private static boolean validateService(String service) {
		return service.equalsIgnoreCase(WikiTools.Service.CATSCAN.name()) || service.equalsIgnoreCase(WikiTools.Service.CATSCAN2.name());
	}
	    
	private static String validateService(String service, String defaultValue) {
		if(validateService(service)) {
			return service;
		}
		return defaultValue;
	}
	
	private void loadOverridenProperties(String newpagesTemplate) throws IOException	{
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
		if(!TryParseTemplate(newpagesTemplate, userNamespace, overridenPropertiesText,options, true)) {
			log.info("no default settings for this template: "+newpagesTemplate);
			return;
		}
		if (options.containsKey("�����������") && !options.get("�����������").isEmpty())
		{
			DEFAULT_DELIMETER = options.get("�����������").replace("\"", "").replace("\\n", "\n");
		}
		
		String key = "������ ��������";
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{
			DEFAULT_FORMAT = options.get(key);//.replace("{", "{{").replace("}", "}}");
		}			
		
		key = "�������";
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
		
		key = "������";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			DEFAULT_SERVICE_NAME = validateService(options.get(key), DEFAULT_SERVICE_NAME);
			DEFAULT_SERVICE = WikiTools.Service.getServiceByName(DEFAULT_SERVICE_NAME, DEFAULT_SERVICE);			
		}
		
		key = "������� �����";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			DEFAULT_USE_FAST_MODE = options.get(key).equalsIgnoreCase(YES_RU);
		}
		
		key = "�����";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {		
			try {
				DEFAULT_HOURS = Integer.parseInt(options.get(key));
			} catch(NumberFormatException e) {
				log.warn(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING, key, options.get(key)));
			}
			if(DEFAULT_HOURS>DEFAULT_SERVICE.MAX_HOURS) {
				DEFAULT_HOURS = DEFAULT_SERVICE.MAX_HOURS;				
			}
		}
		
		key = "���������";
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
		
		key = "������������ ���";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {			
			try {
				DEFAULT_NAMESPACE = Integer.parseInt(options.get(key));
			} catch(NumberFormatException e) {
				log.warn(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING, key, options.get(key)));
			}
		}			
		
		if (options.containsKey("�����") && !options.get("�����").isEmpty())
		{
			DEFAULT_HEADER = options.get("�����").replace("\\n", "\n");
		}			
		
		if (options.containsKey("��������") && !options.get("��������").isEmpty())
		{
			DEFAULT_MIDDLE = options.get("��������").replace("\\n", "\n");
		}
		
		if (options.containsKey("������") && !options.get("������").isEmpty())
		{
			DEFAULT_FOOTER = options.get("������").replace("\\n", "\n");
		}
		
		if(options.containsKey("���") && !options.get("���").isEmpty()) {				
			DEFAULT_TYPE = options.get("���").toLowerCase();
		}	
		
		key = "��������� ������";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			DEFAULT_DELETED_FLAG = parseDeleted(options.get(key),DEFAULT_DELETED_FLAG,null);
		}
		
		key = "��������������� ������";			
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			DEFAULT_RENAMED_FLAG = parseRenamed(options.get(key),DEFAULT_RENAMED_FLAG,null);
		}
		
		key = "����� ��������";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			PICTURE_SEARCH_TAGS = options.get(key);
		}		
	}
	
	protected static PortalParam.Deleted parseDeleted(String value, PortalParam.Deleted defaultValue,ArrayList<String> errors) {
		PortalParam.Deleted flag = defaultValue;
		if(value.equalsIgnoreCase("�������")) {
			flag = PortalParam.Deleted.REMOVE;
		} else if(value.equalsIgnoreCase("��������")) {
			flag = PortalParam.Deleted.MARK;
		} else if(value.equalsIgnoreCase("���������")) {
			flag = PortalParam.Deleted.DONT_TOUCH;
		} else {
			log.warn(String.format(ERROR_INVALID_PARAMETER,"��������� ������", value));
			if(errors!=null) {
				errors.add(String.format(ERROR_INVALID_PARAMETER_RU,"��������� ������", value));
			}
		}
		return flag;
	}
	
	@SuppressWarnings("unused")
	protected void go() {	
		commons = new NirvanaWiki("commons.wikimedia.org");
		commons.setMaxLag( MAX_LAG );

		BotReporter reporter;
        reporter = new BotReporter(wiki, 700, true);
        //reporter.botStarted(true);
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
        
        for (String newpagesTemplate: newpagesTemplates) {
        	long startT = Calendar.getInstance().getTimeInMillis();
    		log.info("template to check: "+newpagesTemplate);
    		
    		try {
    			loadOverridenProperties(newpagesTemplate);
    		} catch (IOException e) {
    			e.printStackTrace();
    			return;
    		}
    		
    		// 1 extract portal list
    		String []portalNewPagesLists = null;
    		try {
    			portalNewPagesLists = wiki.whatTranscludesHere(newpagesTemplate, Wiki.ALL_NAMESPACES);
    		} catch (IOException e) {			
    			log.fatal("failed to get portal list");
    			return;
    		}
    		log.info("loaded portal settings: "+portalNewPagesLists.length);
    		java.util.Arrays.sort(portalNewPagesLists);
    		
    		String [] tasks = null;
    		if(TASK) {	
    			log.info("reading tasks from file: "+TASK_LIST_FILE);
    			tasks = FileTools.readFileToList(TASK_LIST_FILE, true);
    			if (tasks == null) {
    				return;
    			}
    			log.info("loaded tasks: "+tasks.length+" total");
    		}
    		
    		int i = 0;	// ������� ������
    		int t = 0;	// ���������� ����������� ��������
    		int retry_count = 0;
    		boolean retry = false;
    		ReportItem reportItem = null;
    		String portalName = null;
    		
    		// this is a workaround for bad support of keep alive feature in HttpUrlConnection
    		// without it, writing of big articles (~500KB) may hang 
    		// (when reading answer from server after writing)
    		System.setProperty("http.keepAlive", "false"); // adjust HTTP connections
    		
    		// show task list
    		if(tasks!=null) {
    			for(String task : tasks) {
    				log.debug("task: "+task);
    			}
    		}
    		
    		while(i < portalNewPagesLists.length) {	
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
    				reportItem.startTime = System.currentTimeMillis();
    			}
    			
    			
    			
    			//if(DEBUG_BUILD && !portalName.endsWith(TESTING_PORTAL)) {log.info("SKIP portal: "+portalName);	continue;}
    			
    			if(tasks!=null) {
    				boolean skip = true;
    				for(String task : tasks) {					
    					if(portalName.startsWith(task)) {
    						log.debug("task detected: "+task);
    						skip = false;
    						break;
    					}
    				}
    				if(skip) { log.info("SKIP portal: "+portalName); reportItem.skip(); i++; continue; }
    			}
    			if(retry_count==0) t++;
    			if(t<START_FROM) {log.info("SKIP portal: "+portalName);	reportItem.skip(); i++; continue;}
    			
    			if(retry_count==0) reporter.portalChecked();
    			
    			try {				
    				
    				String portalSettingsText = wiki.getPageText(portalName);
    				
    				if(DEBUG_MODE) {					
    					FileTools.dump(portalSettingsText, "dump", portalName+".settings.txt");
    				}
    				
    				if (!wiki.allowEditsByCurrentBot(portalSettingsText)) {
    					log.info("SKIP portal: "+portalName);	reportItem.skip(); i++; continue;
    				}
    				
    				Map<String, String> parameters = new HashMap<String, String>();
    				if(TryParseTemplate(newpagesTemplate, userNamespace, portalSettingsText, parameters, true)) {
    					log.info("validate portal settings OK");					
    					logPortalSettings(parameters);
    					NewPagesData data = new NewPagesData();
    					createPortalModule(parameters, data);
    					if(TYPE.equals("all") || TYPE.equals(data.type)) {
    						if(data.portalModule!=null) {							
    								if(DEBUG_MODE || !DEBUG_BUILD || !portalName.contains("ValidParam") /*&& !portalName.contains("Testing")*/) {
    									reporter.portalProcessed();
    									if(data.portalModule.update(wiki, reportItem, COMMENT)) {
    										reporter.portalUpdated();
    										reportItem.status = Status.UPDATED;
    									} else {
    										reportItem.status = Status.PROCESSED;
    									}   									
    									
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
    				
    			} catch (IllegalArgumentException | IndexOutOfBoundsException | NullPointerException | java.util.zip.ZipException e) { 
    				// includes ArrayIndexOfBoundsException
    				log.error(e.toString()); 
    				if(retry_count<RETRY_MAX) {
    					log.info("RETRY AGAIN");
    					retry = true;
    				} else {
    					reporter.portalError();
    					reportItem.status = Status.ERROR;
    					reportItem.error = BotError.BOT_ERROR;
    					//e.printStackTrace();
    					log.error("OOOOPS!!!", e); // print stack trace
    				}	
    			} catch (ServiceError e) {
    				log.error(e.toString());
    				if(retry_count<RETRY_MAX) {
    					log.info("RETRY AGAIN");
    					retry = true;
    				} else {
    					reporter.portalError();
    					reportItem.status = Status.ERROR;
    					reportItem.error = BotError.SERVICE_ERROR;
    					//e.printStackTrace();
    					log.error("OOOOPS!!!", e); // print stack trace
    				}
    			} catch (Error504 e) {				
    					log.warn(e.toString());
    					log.info("ignore error and continue ...");
    					reporter.portalError();
    					reportItem.status = Status.ERROR;
    					reportItem.error = BotError.SERVICE_ERROR;
    			} catch (IOException e) {
    				
    				if(retry_count<RETRY_MAX) {
    					log.warn(e.toString());
    					log.info("RETRY AGAIN");
    					retry = true;
    				} else {
    					log.error(e.toString());
    					reporter.portalError();
    					reportItem.status = Status.ERROR;
    					reportItem.error = BotError.IO_ERROR;
    					log.error("OOOOPS!!!", e); // print stack trace
    				}
    			} catch (LoginException e) {
    				log.fatal(e.toString());
    				break;
    				//e.printStackTrace();
    			} catch (InterruptedException e) {				
    				log.fatal(e.toString(),e);
    				break;
    			} catch (Exception e) {
    				log.error(e.toString()); 
    				if(retry_count<RETRY_MAX) {
    					log.info("RETRY AGAIN");
    					retry = true;
    				} else {
    					reporter.portalError();
    					reportItem.status = Status.ERROR;
    					//e.printStackTrace();
    					log.error("OOOOPS!!!", e); // print stack trace
    				}	
    			} catch (Error e) {
    				log.error(e.toString()); 
    				if(retry_count<RETRY_MAX) {
    					log.info("RETRY AGAIN");
    					retry = true;
    				} else {
    					reporter.portalError();
    					reportItem.status = Status.ERROR;
    					//e.printStackTrace();
    					log.error("OOOOPS!!!", e); // print stack trace
    				}	
    			}
    			reportItem.endTime = System.currentTimeMillis();
    			if(STOP_AFTER>0 && t>=STOP_AFTER) break;
    			if(!retry) i++;
    		}
    		Calendar cEnd = Calendar.getInstance();
    		long endT = cEnd.getTimeInMillis();
    		reporter.addToTotal(portalNewPagesLists.length);
    		
    		log.info("TEMPLATE FINISHED at "+String.format("%1$tT", cEnd));
    		log.info("WORK TIME for TEMPLATE: "+BotReporter.printTimeDiff(endT-startT));
    	}
        reporter.logStatus();
        if (GENERATE_REPORT) {
			reporter.reportTXT(REPORT_FILE_NAME);
		}
        if (UPDATE_STATUS) {
        	try {
	            reporter.updateEndStatus(STATUS_WIKI_PAGE, STATUS_WIKI_TEMPLATE);
            } catch (LoginException | IOException e) {
            	log.error(e);	            
            }
        }
        reporter.botFinished(true);
	}
	

	public boolean createPortalModule(Map<String, String> options, NewPagesData data) throws 
			NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		log.debug("portal settings init started");
		String key;
		
		PortalParam param = new PortalParam();
		param.lang = LANGUAGE;
		
		String type = null;
		key = "���";
		if(!options.containsKey(key) || options.get(key).isEmpty()) {
			data.errors.add("�������� \"���\" �� �����. ������������ �������� �� ��������� ");
			type = DEFAULT_TYPE;
		} else {
			type = options.get(key).toLowerCase();
		}	
		log.debug("���: "+type);
		data.type = type;
		
		param.categories = optionToStringArray(options,"���������");		
		key = "���������";
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{
			param.categories.add(options.get(key));
		}		
		
		param.categoriesToIgnore = optionToStringArray(options,"������������");
		
		param.categoryGroups = multiOptionToArray(options, "���������", 1, PortalParam.MAX_CAT_GROUPS);
		param.categoryToIgnoreGroups = multiOptionToArray(options, "������������", 1, PortalParam.MAX_CAT_GROUPS);
				
		param.usersToIgnore = optionToStringArray(options,"������������ �������");
		
		param.page = "";
		if (options.containsKey("��������"))
		{
			param.page = options.get("��������");
		}
		
		param.service = DEFAULT_SERVICE;
		key = "������";
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{
			String service = options.get(key);
			if (service.equals("�� ���������") || service.equals("default") || service.equals("auto") || service.equals("����")) {
				// nothing
			} 
			param.service = WikiTools.Service.getServiceByName(service); 
			if (param.service == null) {
				param.service = DEFAULT_SERVICE;
				log.warn(String.format(ERROR_INVALID_PARAMETER, key, service));
			    data.errors.add(String.format(ERROR_INVALID_PARAMETER_RU, key, service));
			}
		}
		
		param.archSettings = new ArchiveSettings();
		param.archSettings.parseCount = DEFAULT_PARSE_COUNT;
		
		param.archive = null;
		key = "�����";
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{
			param.archive = options.get(key);
			parseArchiveName(param.archSettings,options.get(key));			
		}
		
		String str = "";
		key = "������ ��������� � ������";
		Period p1 = Period.NONE;
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			str = options.get(key);
			p1 = ArchiveSettings.getHeaderPeriod(str);
			if(p1==Period.NONE) {
				data.errors.add("� ��������� \"������ ��������� � ������\" �� ����� ���������� ��������. �������� ����� ��������� �� �������.");
			} else {
				param.archSettings.headerFormat = str;
			}
		}
		
		key = "������ ������������ � ������";
		Period p2 = Period.NONE;
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			str = options.get(key); 
			p2 = ArchiveSettings.getHeaderPeriod(str);
			if(p2==Period.NONE) {
				data.errors.add("� ��������� \"������ ������������ � ������\" �� ����� ���������� ��������. �������� ����� ��������� �� �������.");
			} else {
				param.archSettings.headerHeaderFormat = param.archSettings.headerFormat;
				param.archSettings.headerFormat = str;
			}			
		}
		
		if(p1!=Period.NONE && p2!=Period.NONE && p1==p2) {
			data.errors.add("�������� \"������ ��������� � ������\" � �������� \"������ ������������ � ������\" ����� ���������� ������ ���������� "+p1.template());
		}
		
		//data.errors.addAll(validateArchiveFormat(archiveSettings));
		
		/*
		if(archiveSettings.headerFormat==null && archiveSettings.headerHeaderFormat==null) {
			archiveSettings.headerFormat = ArchiveSettings.DEFAULT_HEADER_FORMAT;
		} */
		
		
		
		key = "��������� ������";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			data.errors.addAll(parseArchiveSettings(param.archSettings,options.get(key)));
			//archiveSettings.headerFormat = options.get(key); 
		}
		
		/*
		String prefix = "";
		if (options.containsKey("�������"))
		{
			prefix = options.get("�������");
		}*/
		
		//boolean markEdits = true;
		param.bot = true;
		param.minor = true;
		key = "�������� ������";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			String mark = options.get(key).toLowerCase();
			if(mark.equalsIgnoreCase("���")) {
				//markEdits = false;
				param.bot = false;
				param.minor = false;
			} else {				
				if(!mark.contains("���") && mark.contains("�����")) {
					param.bot = false;
				} else if(mark.contains("���") && !mark.contains("�����")) {
					param.minor = false;
				}				
			}
		}
		
		param.fastMode = DEFAULT_USE_FAST_MODE;
		key = "������� �����";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			param.fastMode = options.get(key).equalsIgnoreCase(YES_RU);
		}
		
		param.ns = DEFAULT_NAMESPACE;
		key = "������������ ���";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {			
			try {
				param.ns = Integer.parseInt(options.get(key));
			} catch(NumberFormatException e) {
				log.warn(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING, key, options.get(key)));
				data.errors.add(String.format(ERROR_PARSE_INTEGER_FORMAT_STRING_RU, key, options.get(key)));
			}
		}
		
		param.header = DEFAULT_HEADER;
		key = "�����";
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{
			param.header = options.get(key).replace("\\n", "\n");
		}
		
		param.footer = DEFAULT_FOOTER;
		key = "������";
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{
			param.footer = options.get(key).replace("\\n", "\n");
		}
		
		param.middle = DEFAULT_MIDDLE;
		key = "��������";
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{
			param.middle = options.get(key).replace("\\n", "\n");
		}
		
		/*
		String templates = "";
		if (options.containsKey("�������"))
		{
			templates = options.get("�������").replace("\\n", "\n");
		}*/
		
		param.format = DEFAULT_FORMAT;
		key = "������ ��������";
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{
			param.format = options.get(key);//.replace("{", "{{").replace("}", "}}");
		}
		
		
		param.depth = DEFAULT_DEPTH;
		key = "�������";
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
		key = "�����";
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
		key = "���������";
		if(type.equals("������ ����������"))
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
		key = "��������� ������";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			param.deletedFlag = parseDeleted(options.get(key),param.deletedFlag,data.errors);
		}
		
		param.renamedFlag = DEFAULT_RENAMED_FLAG;
		key = "��������������� ������";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {			
			param.renamedFlag = parseRenamed(options.get(key),param.renamedFlag,data.errors);
		}
				
		parseIntegerKeyWithMaxVal(options, "������� ����������",param,data,"updatesPerDay",DEFAULT_UPDATES_PER_DAY, MAX_UPDATES_PER_DAY);
		
		/*
		int normalSize = 40 * 1000;
		key = "����������";
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
		key = "���������";
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
		if (options.containsKey("���� �������"))
		{
			longColor = options.get("���� �������");
		}
		
		String shortColor = "#FFE8E9";
		if (options.containsKey("���� ���������"))
		{
			//archive = options.get("���� ���������");
		}
		
		String normalColor = "#FFFDE8";
		if (options.containsKey("���� ����������"))
		{
			//archive = options.get("���� ����������");
		}*/
		
		param.delimeter = DEFAULT_DELIMETER;
		key = "�����������";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			param.delimeter = options.get("�����������").replace("\"", "").replace("\\n", "\n");
		}
		
		param.imageSearchTags = PICTURE_SEARCH_TAGS;
		key = "����� ��������";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			param.imageSearchTags = options.get(key);
		}
		
		if(!validateParams(param,data.errors)) {
			return true;
		}
		
		if (type.equals("������ ����� ������") || type.equals("����� ������")) {
			data.portalModule = new NewPages(param);						
		}
		else if (type.equals("������ ����������")) {
			data.portalModule = new WatchList(param);
		}
		else if (type.equals("��������������� ������ ������, ������� ������ ���� �� ���� ��������")) {
			/*module = new EncyShell(portal,
			title,
			shortSize,
			normalSize,
			shortColor,
			normalColor,
			longColor);*/
			}//"������:�����,������,����������� ������,����������� �������;image file,����,�������,�����������"
		else if (type.equals("������ ����� ������ � ������������� � ��������") || type.equals("����� ������ � ������������� � ��������")) {
			data.portalModule = new NewPagesWithImages(param, commons, new ImageFinderInCard(param.imageSearchTags));
		}
		else if (type.equals("������ ����� ������ � ������������� � ������") || type.equals("����� ������ � ������������� � ������") ) {
			data.portalModule = new NewPagesWithImages(param, commons, new ImageFinderInBody());
		} 
		else if (type.equals("������ ����� ������ � �������������") || type.equals("����� ������ � �������������")) {
			data.portalModule = new NewPagesWithImages(param, commons, new ImageFinderUniversal(param.imageSearchTags));
		}
		else if (type.equals("������ ����� ������ �� ����") || type.equals("����� ������ �� ����")) {
			data.portalModule = new NewPagesWeek(param);
		}
		else if (type.equals("������ ������� � ��������� ����������� � ���������")) {
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
		else if (type.equals("������ ������� � ��������� �����������, ��������� � �����������"))
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
			data.errors.add("��� \""+type+"\" �� ��������������. ����������� ������ ����������� �������� � ��������� \"���\".");			
		}
	
	log.debug("portal settings init finished");
	return true;
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
			log.warn(String.format(ERROR_ABSENT_PARAMETER_RU, "���������"));
			errors.add(String.format(ERROR_ABSENT_PARAMETER_RU,"���������"));
			retval = false;
		}
		if(param.page.isEmpty()) {
			log.warn(String.format(ERROR_ABSENT_PARAMETER_RU, "������"));
			errors.add(String.format(ERROR_ABSENT_PARAMETER_RU,"������"));
			retval = false;
		}
		if(param.updatesPerDay>DEFAULT_STARTS_PER_DAY) {
			param.updatesPerDay = DEFAULT_STARTS_PER_DAY;
		}
		
		if (startNumber != 1) {
			int freq = DEFAULT_STARTS_PER_DAY / param.updatesPerDay;
			if ((startNumber-1)%freq != 0) {
				retval = false;
			}
		}
		return retval;
	}
	
	private int parseRenamed(String string, int defaultValue,
			ArrayList<String> errors) {
		int flag = 0;
		String items[];
		items = string.split(",");
		for(String item:items) {
			String str = item.trim();
			if(str.equals("������ ��������")) {
				flag = flag|PortalParam.RENAMED_OLD;
			} else if(str.equals("����� ��������")) {
				flag = flag|PortalParam.RENAMED_NEW;
			} /*else if(str.equals("��������")) {
				flag = flag|PortalParam.RENAMED_MARK;
			}*/
		}
		if(flag==0) {
			flag = defaultValue;
			if(errors!=null) errors.add("������ � ��������� \"��������������� ������\"");
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
		if(itemsVector.contains("������") && itemsVector.contains("�����")) {
			errors.add(String.format(ERROR_PARAMETER_HAS_MULTIPLE_VALUES_RU, "��������� ������ -> ������/�����"));
		} else if(itemsVector.contains("������")) {
			archiveSettings.addToTop = true;
		} else if(itemsVector.contains("�����")) {
			archiveSettings.addToTop = false;
		}
		if(itemsVector.contains("�����������")||itemsVector.contains("����������")) {
			archiveSettings.sorted = true;
		}
		if(itemsVector.contains("������� ���������")) {
			archiveSettings.removeDuplicates = true;
		}
		int cnt = 0;
		if(itemsVector.contains("��������� ���������")||itemsVector.contains("��������� ���������")) {
			cnt++;
			archiveSettings.enumeration = Enumeration.HASH;
		}
		if(itemsVector.contains("��������� ����� html")||itemsVector.contains("��������� ���������")) {
			cnt++;
			archiveSettings.enumeration = Enumeration.HTML;
		}
		if(itemsVector.contains("���������� ��������� ����� html")||itemsVector.contains("��������� ���������")) {
			cnt++;
			archiveSettings.enumeration = Enumeration.HTML_GLOBAL;
		}
		if(cnt>1) {
			archiveSettings.enumeration = Enumeration.NONE;
			errors.add(String.format(ERROR_PARAMETER_HAS_MULTIPLE_VALUES_RU, "��������� ������ -> ���������"));
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
	

	protected static ArrayList<String> optionToStringArray(Map<String, String> options, String key) {
		ArrayList<String> list = new ArrayList<String>();
		if (options.containsKey(key)) {			
			String option = options.get(key);
			option = option.replace(ADDITIONAL_SEPARATOR, DEPTH_SEPARATOR);
			return optionToStringArray(option, true);
		}
		return list;
	}
	
	protected static ArrayList<List<String>> multiOptionToArray(Map<String, String> options, String key, int start, int end) {
		ArrayList<List<String>> listlist = new ArrayList<List<String>>(end-start+1);
		String keyNumbered;
		for(int i=start;i<=end;i++) {
			keyNumbered = key + String.valueOf(i);
			List<String> list = optionToStringArray(options, keyNumbered);
			listlist.add(list);
		}
		return listlist;
	}
	
}