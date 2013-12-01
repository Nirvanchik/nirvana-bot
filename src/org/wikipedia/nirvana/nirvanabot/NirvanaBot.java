/**
 *  @(#)NirvanaBot.java 1.7 01/12/2013
 *  Copyright � 2011 - 2013 Dmitry Trofimovich (KIN)
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
	public static final String DEPTH_SEPARATOR = "|";
    public static final String ADDITIONAL_SEPARATOR = "#";
    
	public static final String SERVICE_CATSCAN = "catscan";
	public static final String SERVICE_CATSCAN2 = "catscan2";
	public static final String YES_RU = "��";
	public static final String NO_RU = "���";

	private static int STOP_AFTER = 0;
	public static int UPDATE_PAUSE = 1000;
	
	public static String TIME_FORMAT = "short"; // short,long
	
	private static boolean TASK = false;
	private static String TASK_LIST_FILE = "task.txt";
	
	private static int START_FROM = 0;
	
	private String newpagesTemplate = null;
	
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
	private static int MAX_HOURS = 720;
	private static int MAX_HOURS_CATSCAN = 720;
	private static int MAX_HOURS_CATSCAN2 = 8760; // 1 year // 24*31*12 = 8928;
	private static int DEFAULT_HOURS = 720;
	private static String DEFAULT_SERVICE = SERVICE_CATSCAN2;
	private static boolean DEFAULT_USE_FAST_MODE = true;
	private static boolean ERROR_NOTIFICATION = false;
	private static String LANGUAGE= "ru";
	private static String COMMENT = "����������";
	
	private static boolean GENERATE_REPORT = false;
	private static String REPORT_FILE_NAME = "report.txt";
	private static String REPORT_FORMAT = "txt";
	private static String DEFAULT_FORMAT = "* [[%(��������)]]";
	private static String DEFAULT_TYPE = "������ ����� ������";
	
	private static String TYPE = "all";
	
	private static String DEFAULT_DELIMETER = "\n";
	
	private static int RETRY_MAX = 1;

	
	private static String overridenPropertiesPage = null;
	
	private static String PICTURE_SEARCH_TAGS = "image file,����,�������,�����������";
	
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

	public enum Status {
		NONE,
		SKIP,
		PROCESSED,
		UPDATED,
		//DENIED,
		ERROR
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
			"NirvanaBot v1.7 Updates Portal/Project sections at http://ru.wikipedia.org and collects statistics\n" +
			"Copyright (C) 2011-2013 Dmitry Trofimovich (KIN)\n" +
			"\n";
			
	public void showInfo() {
		System.out.print(PROGRAM_INFO);
	}
		
	/**
	 * 
	 */
	public NirvanaBot() {
		
	}
	
	public NirvanaBot(int flags) {
		super(flags);
	}
	
	public static void main(String[] args) {
		NirvanaBasicBot bot = new NirvanaBot(NirvanaBasicBot.FLAG_SHOW_LICENSE);
		bot.run(args);
	}
	
	@Override
	protected boolean loadCustomProperties() {
		newpagesTemplate = properties.getProperty("new-pages-template");
		if(newpagesTemplate==null) {
			if(DEBUG_BUILD)
				newpagesTemplate = "��������:NirvanaBot/test/����� ������";
			else {
				System.out.println("ABORT: properties not found");
				log.fatal("New pages template name (new-pages-template) is not specified in settings");
				return false;
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
		
		DEFAULT_SERVICE = validateService(properties.getProperty("service",DEFAULT_SERVICE), DEFAULT_SERVICE);
		
		DEFAULT_USE_FAST_MODE = properties.getProperty("fast-mode",DEFAULT_USE_FAST_MODE?YES:NO).equals(YES);
		
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
		
		return true;
	}
	
	private static boolean validateService(String service) {
		return service.equalsIgnoreCase(SERVICE_CATSCAN) || service.equalsIgnoreCase(SERVICE_CATSCAN2);
	}
	    
	private static String validateService(String service, String defaultValue) {
		if(validateService(service)) {
			return service;
		}
		return defaultValue;
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
		if(TryParseTemplate(newpagesTemplate, overridenPropertiesText,options)) {			
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
				DEFAULT_SERVICE = validateService(options.get(key), DEFAULT_SERVICE);
			}
			if (DEFAULT_SERVICE.equals(SERVICE_CATSCAN)) {
				MAX_HOURS = MAX_HOURS_CATSCAN;
			} else if (DEFAULT_SERVICE.equals(SERVICE_CATSCAN2)) {
				MAX_HOURS = MAX_HOURS_CATSCAN2;
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
				if(DEFAULT_HOURS>MAX_HOURS) {
					DEFAULT_HOURS = MAX_HOURS;				
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
		commons.setMaxLag( 15 );

		Calendar cStart = Calendar.getInstance();
		long start = cStart.getTimeInMillis();
		//Date d = cStart.getTime();
		log.info("BOT STARTED at "+cStart.get(Calendar.HOUR_OF_DAY)+":"+cStart.get(Calendar.MINUTE));
		log.info("template to check: "+newpagesTemplate);
		
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
			portalNewPagesLists = wiki.whatTranscludesHere(newpagesTemplate, Wiki.ALL_NAMESPACES);
		} catch (IOException e) {			
			log.fatal("failed to get portal list");
			return;
		}
		java.util.Arrays.sort(portalNewPagesLists);
		
		String [] tasks = null;
		if(TASK) {			
			 tasks = FileTools.readFileToList(TASK_LIST_FILE);
			 if (tasks == null) {
				 return;
			 }
		}
		
/*		for(String str:portalNewPagesLists) {
			log.debug(str);
		}*/
		int i = 0;	// ������� ������
		int t = 0;	// ���������� ����������� ��������
		int j = 0;  // ���������� ������������ ��������
		int k = 0;  // ���������� �� ����� ���� ����������� ��������
		int l = 0;	// ���������� ����������� ��������
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
				if(report!=null) report.add(reportItem);
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
			
			if(retry_count==0) k++;
			
			try {				
				
				String portalSettingsText = wiki.getPageText(portalName);
				
				if(DEBUG_MODE) {					
					FileTools.dump(portalSettingsText, "dump", portalName+".settings.txt");
				}
				Map<String, String> parameters = new HashMap<String, String>();
				if(TryParseTemplate(newpagesTemplate, portalSettingsText, parameters)) {
					log.info("validate portal settings OK");					
					logPortalSettings(parameters);
					NewPagesData data = new NewPagesData();
					createPortalModule(parameters, data);
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
				if(retry_count<RETRY_MAX) {
					log.info("RETRY AGAIN");
					retry = true;
				} else {
					er++;
					reportItem.status = Status.ERROR;
					//e.printStackTrace();
					log.error("OOOOPS!!!", e); // print stack trace
				}	
			} catch (NullPointerException e) {
				log.error(e.toString()); 
				if(retry_count<RETRY_MAX) {
					log.info("RETRY AGAIN");
					retry = true;
				} else {
					er++;
					reportItem.status = Status.ERROR;
					//e.printStackTrace();
					log.error("OOOOPS!!!", e); // print stack trace
				}	
			} catch (java.util.zip.ZipException e) {
				log.error(e.toString()); 
				if(retry_count<RETRY_MAX) {
					log.info("RETRY AGAIN");
					retry = true;
				} else {
					er++;
					reportItem.status = Status.ERROR;
					//e.printStackTrace();
					log.error("OOOOPS!!!", e); // print stack trace
				}	
			} catch (Error504 e) {				
					log.warn(e.toString());
					log.info("ignore error and continue ...");
					er++;
					reportItem.status = Status.ERROR;
			} catch (IOException e) {
				
				if(retry_count<RETRY_MAX) {
					log.warn(e.toString());
					log.info("RETRY AGAIN");
					retry = true;
				} else {
					log.error(e.toString());
					er++;
					reportItem.status = Status.ERROR;
					//e.printStackTrace();
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
					er++;
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
					er++;
					reportItem.status = Status.ERROR;
					//e.printStackTrace();
					log.error("OOOOPS!!!", e); // print stack trace
				}	
			}
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
			StringBuilder sb = new StringBuilder();
			//StringBuffer sbuf = new StringBuffer();
			sb.append(ReportItem.getHeader()).append("\r\n");
			for(ReportItem item : report) {
				sb.append(item.toString());
				sb.append("\r\n");
			}
			sb.append(ReportItem.getFooter());
			try {
				FileTools.writeFile(sb.toString(), REPORT_FILE_NAME);				
			} catch (IOException e) {
				log.error(e.toString());
				e.printStackTrace();				
			}			
			log.info("report is generated!");
		}
		
	}
	

	public boolean createPortalModule(Map<String, String> options, NewPagesData data) {
		log.debug("portal settings init started");
		String key;
		
		PortalParam param = new PortalParam();
		param.lang = NirvanaBot.LANGUAGE;
		
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
			if(validateService(service)) {
				param.service = service;
			} else {
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
			int maxHours = MAX_HOURS;
			if(!param.service.equals(DEFAULT_SERVICE)) {
				if(param.service.equals(SERVICE_CATSCAN)) {
					maxHours = MAX_HOURS_CATSCAN;
				} else if (param.service.equals(SERVICE_CATSCAN2)) {
					maxHours = MAX_HOURS_CATSCAN2;
				}
			}
			if(param.hours>maxHours) {
				data.errors.add(String.format(ERROR_INTEGER_TOO_BIG_STRING_RU, key, options.get(key),maxHours));
				param.hours = maxHours;				
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
		
		if (type.equals("������ ����� ������")) {
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
		else if (type.equals("������ ����� ������ � ������������� � ��������")) {
			data.portalModule = new NewPagesWithImages(param, commons, new ImageFinderInCard(param.imageSearchTags));
		}
		else if (type.equals("������ ����� ������ � ������������� � ������") ) {
			data.portalModule = new NewPagesWithImages(param, commons, new ImageFinderInBody());
		} 
		else if (type.equals("������ ����� ������ � �������������")) {
			data.portalModule = new NewPagesWithImages(param, commons, new ImageFinderUniversal(param.imageSearchTags));
		}
		else if (type.equals("������ ����� ������ �� ����")) {
			/*data.portalModule = new NewPagesWithWeeks(LANGUAGE,
				    categories,
				    categoriesToIgnore,
				    usersToIgnore,
				    title, archive, 
				    ns, depth, hours, maxItems,
				    format, delimeter, header, footer,
				    minor, bot);*/
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
		return retval;
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
		
		/*if(itemsVector.contains("���������")) {
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
	

	protected static ArrayList<String> optionToStringArray(Map<String, String> options, String key) {
		ArrayList<String> list = new ArrayList<String>();
		if (options.containsKey(key)) {			
			String option = options.get(key);
			option = option.replace(ADDITIONAL_SEPARATOR, DEPTH_SEPARATOR);
			return optionToStringArray(option, true);
		}
		return list;
	}
	
}
