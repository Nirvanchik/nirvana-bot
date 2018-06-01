/**
 *  @(#)StatisticsBot.java 1.3 10.01.2017
 *  Copyright � 2012-2017 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.statistics;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.time.StopWatch;
import org.wikipedia.Wiki;
import org.wikipedia.nirvana.FileTools;
import org.wikipedia.nirvana.NirvanaBasicBot;
import org.wikipedia.nirvana.NumberTools;
import org.wikipedia.nirvana.archive.ArchiveSettings;
import org.wikipedia.nirvana.archive.ArchiveSettings.Period;
import org.wikipedia.nirvana.nirvanabot.NirvanaBot;

/**
 * @author kin
 *
 */
public class StatisticsBot extends NirvanaBasicBot {
    public static final String DEFAULT_CACHE_FOLDER = "cache";
	boolean DEBUG = false;
	//public static final int START_YEAR = 2008;
	private static boolean TASK = false;
	private static String TASK_LIST_FILE = "task.txt";
	public static final String delimeter = "\n";
	public static final String YES_RU = "��";
	public static final String NO_RU = "���";
	public static final boolean SORT_DEFAULT = true;
	private static final String STATUS_INFO_FORMAT = "%1$-40s time spent:%2$s";
	private static final String DEFAULT_HEADER = "";
	private static final String DEFAULT_FOOTER = "";
	private static final int MIN_SIZE = 14 * 1024;  // 14 Kb
	
	private static String statSettingsTemplate = null;

    private static final String VERSION = "v1.3";

	public static final String INFO = 
        "StatisticsBot " + VERSION + " Makes statistics of new created pages and ratings\n" +
        "Copyright (C) 2012-2017 Dmitry Trofimovich (KIN)\n" +
        "\n";
	private static boolean USE_CACHE_ONLY = false;
	private static boolean USE_CACHE = true;
	
	public void showInfo() {
		System.out.print(INFO);
	}
	/**
	 * 
	 */
	public StatisticsBot() {
		// TODO Auto-generated constructor stub
	}

    public static void main(String[] args) {
		NirvanaBasicBot bot = new StatisticsBot();
        System.exit(bot.run(args));
	}

	@Override
	protected boolean loadCustomProperties(Map<String,String> launch_params) {
		statSettingsTemplate = properties.getProperty("stat-settings-template");
		if(statSettingsTemplate==null) {
			if(DEBUG_BUILD)
				statSettingsTemplate = "��������:NirvanaBot/test/��������� ����������";
			else {
				System.out.println("ABORT: properties not found");
				log.fatal("Statistics settings template name (stat-settings-template) is not specified in settings");
				return false;
			}
		}	
		log.info("new pages template : "+statSettingsTemplate);	
		TASK_LIST_FILE = properties.getProperty("task-list-file",TASK_LIST_FILE);
		log.info("task list file: "+TASK_LIST_FILE);
		//COMMENT = properties.getProperty("update-comment", COMMENT);
		
		TASK = properties.getProperty("task-list",TASK?YES:NO).equals(YES);
		log.info("task list : "+(TASK?YES:NO));
		
		USE_CACHE = properties.getProperty("use-cache",USE_CACHE?YES:NO).equals(YES);
		log.info("use cache: "+(USE_CACHE?YES:NO));
		
		USE_CACHE_ONLY = properties.getProperty("use-cache-only",USE_CACHE_ONLY?YES:NO).equals(YES);
		log.info("use cache only: "+(USE_CACHE_ONLY?YES:NO));
		
		ArchiveSettings.setDefaultStartYear(
				validateIntegerSetting(properties,"archive-start-year", ArchiveSettings.getDefaultStartYear(), false));
		
		RatingTotal.setDefaultStartYear(
				validateIntegerSetting(properties,"rating-total-start-year", RatingTotal.getDefaultStartYear(), false));
		
		return true;
	}
	
	protected void showStatus(String status, StopWatch w) {
		w.stop();
		log.info(String.format(STATUS_INFO_FORMAT,status,w));
		w.reset();w.start();
	}
	
	protected void go() {
		String [] portalSettingsPages = null;
		String userNamespace;
        try {
	        userNamespace = wiki.namespaceIdentifier(Wiki.USER_NAMESPACE);
        } catch (IOException e1) {
	        log.fatal("failed to get user namespace");
	        return;
        }
		//List<String>
		try {
			portalSettingsPages = wiki.whatTranscludesHere(statSettingsTemplate, Wiki.ALL_NAMESPACES);
		} catch (IOException e) {			
			log.fatal("failed to get portal list");
			return;
		}
		java.util.Arrays.sort(portalSettingsPages);
		
		String [] tasks = null;
		if(TASK) {			
			 tasks = FileTools.readFileToArray(TASK_LIST_FILE, true);
			 if (tasks == null) {
				 return;
			 }
		}
		
		int i = 0;	// ������� ������
		String portalName = null;
		while(i < portalSettingsPages.length) {
			portalName = portalSettingsPages[i];
			log.info("processing portal: "+portalName);
			if(tasks!=null) {
				boolean skip = true;
				for(String task : tasks) {
					log.debug("task: "+task);
					if(portalName.startsWith(task.trim())) {
						skip = false;
						break;
					}
				}
				if(skip) { log.info("SKIP portal: "+portalName); i++; continue; }
			}
			
			StopWatch watch = new StopWatch();
			StopWatch wtotal = new StopWatch();
						
			try {
				wtotal.start();
				watch.start();
				
				String portalSettingsText = wiki.getPageText(portalName);

                if (DEBUG_MODE || NirvanaBasicBot.DEBUG_BUILD) {
                    FileTools.dump(portalSettingsText, portalName + ".settings.txt");
				}

				
				Map<String, String> options = new HashMap<String, String>();
				if(!TryParseTemplate(statSettingsTemplate, userNamespace, portalSettingsText, options, true)) {
					log.error("validate portal settings FAILED");
					continue;
				}
				log.info("validate portal settings OK");
				logPortalSettings(options);
				
				StatisticsParam params = new StatisticsParam();
				if(!readPortalSettings(options, params)) {
					log.error("Failed to read portal settings, portal ABORTED");
					continue;
				}
				
    
    
    			// create db
    			String name = params.archiveSettings.archive.substring(0,params.archive.indexOf("/"));
    			
    			//log.info("parameter processing finished, time: "+watch.toString());
    			showStatus("parameter processing finished",watch);

                String cacheDir = outDir + "/" + DEFAULT_CACHE_FOLDER;
                ArchiveDatabase2 db = new ArchiveDatabase2(name, params.cache, cacheDir,
                        params.filterBySize);

    			//log.info("database created, time: "+watch.toString());
    			showStatus("database created", watch);
    			
    			if(!params.cacheonly) {
                    ArchiveParser parser =
                            new ArchiveParser(params.archiveSettings, db, wiki, cacheDir);
    				try {
    					parser.getData(params.cache);
    				} catch (IOException e) {			
    					log.fatal(e);
    					return;
    				}
    			}
    			//log.info("data loaded to database, time: "+watch.toString());
    			showStatus("data loaded to database",watch);
    			
    			if(!db.isSorted()) {
    				log.warn("db is not sorted");
    				if(params.sort) {
    					db.sort();
    					db.markYears();
    					//log.info("data sorted, time: "+watch.toString());
    					showStatus("data sorted",watch);
    				}
    			}
    			
    			
    			if(params.cache && !params.cacheonly) 
    				db.save();
    			
    			Calendar c = Calendar.getInstance();
    			int endYear = c.get(Calendar.YEAR);
    			
    			Statistics.portal = name;
    			
    			// create statistics
			
				for(String type:params.reportTypes) {
					//log.info("creating statistics of type: "+type);
					String key = type;
					String destination = null;
					if (options.containsKey(key) && !options.get(key).isEmpty()) {
						destination = options.get(key);
					} else {
						log.error("Output destination for "+type+" is not defined");
						continue;
					}
					
					Map<String,String> suboptions = getSubOptions(options,type);
					if(suboptions.get("������ ���")==null)
						suboptions.put("������ ���",String.valueOf(params.archiveSettings.startYear));
					if(params.header != null && suboptions.get("�����") == null) {
						suboptions.put("�����", params.header);
					}
					if(params.footer != null && suboptions.get("������") == null) {
						suboptions.put("������", params.footer);
					}
					if(params.filterBySize && suboptions.get("������ ��")==null) {
						suboptions.put("������ ��", String.valueOf(params.minSize));
					}
					
					
					Statistics stat = null;
					if(destination.contains("%(���)")) {
						for(int year=params.archiveSettings.startYear;year<=endYear;year++) {
							log.info("creating report of type: "+type + " for year: "+String.valueOf(year));
                            stat = StatisticsFabric.createReporter(wiki, cacheDir, type, year);
							if(stat==null) {
								log.error("Report type "+type+" is not defined");
								break;
							}
							if(suboptions!=null && !suboptions.isEmpty()) stat.setOptions(suboptions);
							stat.put(db);
							String text = stat.toString();
							String article = destination.replace("%(���)", String.valueOf(year));
							//log.info("updating "+article+" : "+this.COMMENT);
							/*if(wiki.editIfChanged(article, text, this.COMMENT, true))
								log.info("done");
							else
								log.info("not changed");*/
							editIfChanged(article, text, params.comment, true);
						}
					} else {				
						log.info("creating report of type: "+type);
                        stat = StatisticsFabric.createReporter(wiki, cacheDir, type);
						if(stat==null) {
							log.error("Report type "+type+" is not defined");
							continue;
						} 
						if(suboptions!=null && !suboptions.isEmpty()) stat.setOptions(suboptions);
						stat.put(db);
						String text = stat.toString();
						String article = destination;
						//log.info("updating "+article+" : "+this.COMMENT);
						/*
						if(wiki.editIfChanged(article, text, this.COMMENT, true))
							log.info("done");
						else
							log.info("not changed");*/
						editIfChanged(article, text, params.comment, true);
					}
					
				}
			} catch(IOException e) {
				log.fatal(e,e);
				return;			
			} catch(LoginException e) {
				log.fatal(e,e);
				return;
			} catch(IllegalStateException e) {
				log.fatal(e);
				return;
			} catch(Exception e) {
				log.fatal(e,e);
				
				//log.
				e.printStackTrace();
				continue;
			} finally {
				showStatus("statistics creation finished", watch);
				watch.stop();		
				wtotal.stop();
				log.info(String.format(STATUS_INFO_FORMAT,"FINISHED. total time ->",wtotal));
				StatisticsFabric.purge();
			}
			//log.info("statistics generated, time: "+watch.toString());
//			showStatus("statistics generated", watch);
//			watch.stop();		
//			wtotal.stop();
			//log.info("total time -> "+wtotal.toString());		
			i++;
		}
		
	}
	
	private static ArrayList<String> optionToStringArray(Map<String, String> options, String key) {
		ArrayList<String> list = new ArrayList<String>();
		if (options.containsKey(key)) {			
			return NirvanaBasicBot.optionToStringArray(options.get(key), false);
		}
		return list;
	}

	
	private boolean readPortalSettings(Map<String, String> options, StatisticsParam params) {
		params.archiveSettings = new ArchiveSettings();
		params.archive = null;
		String key = "�����";
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{
			params.archive = options.get(key);
			NirvanaBot.parseArchiveName(params.archiveSettings,options.get(key));			
		} else {
			log.error("�������� \"�����\" �� ������ � ����������");
			return false;			
		}
		
		String str = "";
		key = "������ ��������� � ������";
		Period p1 = Period.NONE;
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			str = options.get(key);
			p1 = ArchiveSettings.getHeaderPeriod(str);
			if(p1==Period.NONE) {
				log.error("� ��������� \"������ ��������� � ������\" �� ����� ���������� ��������. �������� ����� ��������� �� �������.");
				return false;
			} else {
				params.archiveSettings.headerFormat = str;
			}
		}
		
		key = "��������� ������";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			NirvanaBot.parseArchiveSettings(params.archiveSettings,options.get(key));
			//archiveSettings.headerFormat = options.get(key); 
		}
		
		params.reportTypes = null;
		key = "���";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			params.reportTypes = optionToStringArray(options, key);
			//archiveSettings.headerFormat = options.get(key); 
		} else {
			log.error("�������� \"���\" �� ������ � ����������");
			return false;
		}
		
		if(params.reportTypes.isEmpty()) {
			log.error("�������� ��������� '���' ������");
			return false;
		}
		
		key = "�����������";
		params.comment = COMMENT;
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			params.comment = options.get(key);
		}
		
		//int startFromYear = START_YEAR;
		key = "������ ���";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			try {
				params.archiveSettings.startYear = Integer.parseInt(options.get(key));
			} catch(NumberFormatException e) {
				log.warn(String.format(NirvanaBot.ERROR_PARSE_INTEGER_FORMAT_STRING, key, options.get(key)));
			}
		}
		
		//String firstArchive = null;
		key = "������ �����";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			params.archiveSettings.firstArchive = options.get(key);
		}
		
		//int startFromYear2 = -1;
		key = "������ ��� ����� ������� ������";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			try {
				params.archiveSettings.startYear2 = Integer.parseInt(options.get(key));
			} catch(NumberFormatException e) {
				log.warn(String.format(NirvanaBot.ERROR_PARSE_INTEGER_FORMAT_STRING, key, options.get(key)));
			}
		}
		
		params.sort = SORT_DEFAULT;
		key = "�����������";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			if(options.get(key).equalsIgnoreCase(YES_RU)) params.sort = true;
			else if(options.get(key).equalsIgnoreCase(NO_RU)) params.sort = false;
		}
		
		params.cacheonly = USE_CACHE_ONLY;
		key = "������ ���";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			if(options.get(key).equalsIgnoreCase(YES_RU)) params.cacheonly = true;
			else if(options.get(key).equalsIgnoreCase(NO_RU)) params.cacheonly = false;
		}
		
		params.cache = USE_CACHE;
		key = "���";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			if(options.get(key).equalsIgnoreCase(YES_RU)) params.cache = true;
			else if(options.get(key).equalsIgnoreCase(NO_RU)) params.cache = false;
		}
		
		params.filterBySize = false;
		params.minSize = MIN_SIZE;
		key = "������ ��";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			params.filterBySize = true;
			try {
				params.minSize = NumberTools.parseFileSize(options.get(key));
			} catch(NumberFormatException e) {
				log.warn(String.format(NirvanaBot.ERROR_PARSE_INTEGER_FORMAT_STRING, key, options.get(key)));
			}
		}
		
		params.header = DEFAULT_HEADER;
		key = "�����";
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{
			params.header = options.get(key).replace("\\n", "\n");
		}
		
		params.footer = DEFAULT_FOOTER;
		key = "������";
		if (options.containsKey(key) && !options.get(key).isEmpty())
		{
			params.footer = options.get(key).replace("\\n", "\n");
		}
		
		return true;
	}
	
	public boolean editIfChanged(String title, String text, String summary, boolean minor) throws IOException, LoginException
    {
    	String old = null;
    	try {
    		old = wiki.getPageText(title);
    	} catch(FileNotFoundException e) {
    		// ignore. all job is done not here :)
    	}
    	if(old==null || old.length()!=text.length() || !old.equals(text)) { // to compare lengths is faster than comparing 5k chars
    		log.info("updating "+title+" : "+summary);
            wiki.edit(title, text, summary, minor, true, -2, null);
    		return true;
    	} else {
    		log.debug("skip updating "+title+ " (no changes)");
    	}
    	return false;
    }
	
	Map<String,String> getSubOptions(Map<String,String> options,String option) {
		Map<String,String> suboptions = new HashMap<String,String>();
		for(Map.Entry<String,String> entry:options.entrySet()) {
			String key = entry.getKey();
			if(key.startsWith(option) && key.contains(".")) {
				if(suboptions==null) suboptions = new HashMap<String,String>();
				String subkey = key.substring(key.indexOf('.')+1).trim();
				suboptions.put(subkey, entry.getValue());
			}
		}
		return suboptions;
	}
	
}
