/**
 *  @(#)StatisticsBot.java 1.0 02/12/2012
 *  Copyright © 2012 Dmitry Trofimovich (KIN)
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
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.time.StopWatch;
import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.DateTools;
import org.wikipedia.nirvana.FileTools;
import org.wikipedia.nirvana.NirvanaBasicBot;
import org.wikipedia.nirvana.archive.ArchiveSettings;
import org.wikipedia.nirvana.archive.ArchiveSettings.Period;
import org.wikipedia.nirvana.nirvanabot.NewPages;
import org.wikipedia.nirvana.nirvanabot.NirvanaBot;

/**
 * @author kin
 *
 */
public class StatisticsBot extends NirvanaBasicBot {
	boolean DEBUG = false;
	//public static final int START_YEAR = 2008;
	private static String TASK_LIST_FILE = "task.txt";
	public static final String delimeter = "\n";
	public static final String YES_RU = "да";
	public static final String NO_RU = "нет";
	public static final boolean SORT_DEFAULT = true;
	private static final String STATUS_INFO_FORMAT = "%1$-40s time spent:%2$s";
	
	
	//public static String COMMENT = "Проставление заголовков и нумерации в архиве";
	
	public static final String INFO = 
		"StatisticsBot v1.0 Makes statistics of new created pages and ratings\n" +
		"Copyright (C) 2012 Dmitry Trofimovich (KIN)\n" +
		"\n";
	private static final boolean USE_CACHE_ONLY = false;
	private static final boolean USE_CACHE = true;
	
	public void showInfo() {
		System.out.print(INFO);
	}
	/**
	 * 
	 */
	public StatisticsBot() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NirvanaBasicBot bot = new StatisticsBot();
		bot.showInfo();
		//bot.showLicense();
		System.out.print("-----------------------------------------------------------------\n");
		String configFile = bot.getConfig(args);		
		System.out.println("applying config file: "+configFile);
		bot.startWithConfig(configFile);
	}
	
	protected void loadCustomProperties() {
		TASK_LIST_FILE = properties.getProperty("task-list-file",TASK_LIST_FILE);
		log.info("task list file: "+TASK_LIST_FILE);
		//COMMENT = properties.getProperty("update-comment", COMMENT);
	}
	
	protected void showStatus(String status,StopWatch w) {
		w.stop();
		log.info(String.format(STATUS_INFO_FORMAT,status,w));
		w.reset();w.start();
	}
	
	protected void go() {
		String tasklist = TASK_LIST_FILE; 
		
		if(tasklist==null)
			return;
		
		String tasks[] = tasklist.split(",");
		
		for(String task:tasks) {
			task = task.trim();
			String taskSettingsTxt = FileTools.readFile(task);
			if(taskSettingsTxt==null) {
				log.error("Failed to read file "+task);
				continue;
			}
			
			StopWatch watch = new StopWatch();
			StopWatch wtotal = new StopWatch();
			wtotal.start();
			watch.start();
		
		
			Map<String, String> options = new HashMap<String, String>();
			if(!textOptionsToMap(taskSettingsTxt,options)) {
				log.error("incorrect settings");
				return;
			}
			
			ArchiveSettings archiveSettings = new ArchiveSettings();
			String archive = null;
			String key = "архив";
			if (options.containsKey(key) && !options.get(key).isEmpty())
			{
				archive = options.get(key);
				NirvanaBot.parseArchiveName(archiveSettings,options.get(key));			
			}
			
			String str = "";
			key = "формат заголовка в архиве";
			Period p1 = Period.NONE;
			if (options.containsKey(key) && !options.get(key).isEmpty()) {
				str = options.get(key);
				p1 = ArchiveSettings.getHeaderPeriod(str);
				if(p1==Period.NONE) {
					log.error("В параметре \"формат заголовка в архиве\" не задан переменный параметр. Значение этого параметра не принято.");
					return;
				} else {
					archiveSettings.headerFormat = str;
				}
			}
			
			key = "параметры архива";
			if (options.containsKey(key) && !options.get(key).isEmpty()) {
				NirvanaBot.parseArchiveSettings(archiveSettings,options.get(key));
				//archiveSettings.headerFormat = options.get(key); 
			}
			
			List<String> reportTypes = null;
			key = "тип";
			if (options.containsKey(key) && !options.get(key).isEmpty()) {
				reportTypes = NirvanaBot.optionToStringArray(options, key);
				//archiveSettings.headerFormat = options.get(key); 
			}
			
			if(reportTypes.isEmpty()) {
				log.info("No reports given in 'тип' parameter");
				return;
			}
			
			key = "комментарий";
			String comment = COMMENT;
			if (options.containsKey(key) && !options.get(key).isEmpty()) {
				comment = options.get(key);
			}
			
			//int startFromYear = START_YEAR;
			key = "первый год";
			if (options.containsKey(key) && !options.get(key).isEmpty()) {
				try {
					archiveSettings.startYear = Integer.parseInt(options.get(key));
				} catch(NumberFormatException e) {
					log.warn(String.format(NirvanaBot.ERROR_PARSE_INTEGER_FORMAT_STRING, key, options.get(key)));
				}
			}
			
			//String firstArchive = null;
			key = "первый архив";
			if (options.containsKey(key) && !options.get(key).isEmpty()) {
				archiveSettings.firstArchive = options.get(key);
			}
			
			//int startFromYear2 = -1;
			key = "первый год после первого архива";
			if (options.containsKey(key) && !options.get(key).isEmpty()) {
				try {
					archiveSettings.startYear2 = Integer.parseInt(options.get(key));
				} catch(NumberFormatException e) {
					log.warn(String.format(NirvanaBot.ERROR_PARSE_INTEGER_FORMAT_STRING, key, options.get(key)));
				}
			}
			
			boolean sort = SORT_DEFAULT;
			key = "сортировать";
			if (options.containsKey(key) && !options.get(key).isEmpty()) {
				if(options.get(key).equalsIgnoreCase(YES_RU)) sort = true;
				else if(options.get(key).equalsIgnoreCase(NO_RU)) sort = false;
			}
			
			boolean cacheonly = USE_CACHE_ONLY;
			key = "только кэш";
			if (options.containsKey(key) && !options.get(key).isEmpty()) {
				if(options.get(key).equalsIgnoreCase(YES_RU)) cacheonly = true;
				else if(options.get(key).equalsIgnoreCase(NO_RU)) cacheonly = false;
			}
			
			boolean cache = USE_CACHE;
			key = "кэш";
			if (options.containsKey(key) && !options.get(key).isEmpty()) {
				if(options.get(key).equalsIgnoreCase(YES_RU)) cache = true;
				else if(options.get(key).equalsIgnoreCase(NO_RU)) cache = false;
			}
			// create db
			String name = archiveSettings.archive.substring(0,archive.indexOf("/"));
			
			//log.info("parameter processing finished, time: "+watch.toString());
			showStatus("parameter processing finished",watch);
			
			// do not load from cache if archive is single -> we will rebuild db from scratch
			ArchiveDatabase2 db = new ArchiveDatabase2(name, cache /*&& !archiveSettings.isSingle()*/, "cache");
			
			//log.info("database created, time: "+watch.toString());
			showStatus("database created",watch);
			
			if(!cacheonly) {
				try {
					getData(archiveSettings,db,cache);
				} catch (IOException e) {			
					log.fatal(e);
					return;
				}
			}
			//log.info("data loaded to database, time: "+watch.toString());
			showStatus("data loaded to database",watch);
			
			if(!db.isSorted()) {
				log.warn("db is not sorted");
				if(sort) {
					db.sort();
					db.markYears();
					//log.info("data sorted, time: "+watch.toString());
					showStatus("data sorted",watch);
				}
			}
			
			
			if(cache && !cacheonly) 
				db.save();
			
			Calendar c = Calendar.getInstance();
			int endYear = c.get(Calendar.YEAR);
			
			Statistics.portal = name;
			
			// create statistics
			try{
				for(String type:reportTypes) {
					//log.info("creating statistics of type: "+type);
					key = type;
					String destination = null;
					if (options.containsKey(key) && !options.get(key).isEmpty()) {
						destination = options.get(key);
					} else {
						log.error("Output destination for "+type+" is not defined");
						continue;
					}
					
					Map<String,String> suboptions = getSubOptions(options,type);
					if(suboptions.get("первый год")==null)
						suboptions.put("первый год",String.valueOf(archiveSettings.startYear));
					
					
					Statistics stat = null;
					if(destination.contains("%(год)")) {
						for(int year=archiveSettings.startYear;year<=endYear;year++) {
							log.info("creating report of type: "+type + " for year: "+String.valueOf(year));
							stat = StatisticsFabric.createReporter(wiki,type,year);						
							if(stat==null) {
								log.error("Report type "+type+" is not defined");
								break;
							}
							if(suboptions!=null && !suboptions.isEmpty()) stat.setOptions(suboptions);
							stat.put(db);
							String text = stat.toString();
							String article = destination.replace("%(год)", String.valueOf(year));
							//log.info("updating "+article+" : "+this.COMMENT);
							/*if(wiki.editIfChanged(article, text, this.COMMENT, true))
								log.info("done");
							else
								log.info("not changed");*/
							editIfChanged(article, text, comment, true);
						}
					} else {				
						log.info("creating report of type: "+type);
						stat = StatisticsFabric.createReporter(wiki,type);					
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
						editIfChanged(article, text, comment, true);
					}
					
				}
			} catch(IOException e) {
				log.fatal(e);
				return;			
			} catch(LoginException e) {
				log.fatal(e);
				return;
			} catch(IllegalStateException e) {
				log.fatal(e);
				return;
			} catch(Exception e) {
				log.fatal(e.toString());
				
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
		}
		
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
    		wiki.edit(title, text, summary, minor, true, -2);
    		return true;
    	} else {
    		log.debug("skip updating "+title+ " (no changes)");
    	}
    	return false;
    }
	
	private interface PurgeDatabase{
		public void purge(ArchiveDatabase2 db);
	};
	
	void getData(ArchiveSettings archiveSettings, ArchiveDatabase2 db, boolean cache) throws IOException {
		if(archiveSettings.isSingle()) {
			PurgeDatabase purge = null;
			if(!db.isEmpty()) {
				purge = new PurgeDatabase() {
					@Override
					public void purge(ArchiveDatabase2 db) {
						db.removeAllItems();
					}
				};
			}
			getDataFromArchive(archiveSettings.archive,db,purge,archiveSettings.addToTop,true/*,true*/);
			if(cache) db.save();
			db.markEnd();
		} else {
			boolean markAll = db.isEmpty();
			int startYear = archiveSettings.startYear;
			Calendar c = Calendar.getInstance();
			int endYear = c.get(Calendar.YEAR);
			int curQ = c.get(Calendar.MONTH)/3+1;
			ArchiveItem last = db.getLastFromCache();
			//boolean checkLast = false;
			if(last!=null && last.year>startYear) {
				startYear = last.year; 
				log.info("db already contains data. scanning data from " + String.valueOf(startYear)+" year");
			}
			int year_offset = 1;
			for(int year=startYear;year<=endYear;year+=year_offset) {				
				String archiveThisYear = null;
				
				if (archiveSettings.firstArchive != null && year<archiveSettings.startYear2) {
					archiveThisYear = archiveSettings.firstArchive;
					year_offset = archiveSettings.startYear2-archiveSettings.startYear;
				} else {
					archiveThisYear = archiveSettings.archive.replace(
							ArchiveSettings.Period.YEAR.template(), String.valueOf(year));
					year_offset = 1;
				}
				if(archiveSettings.archivePeriod==Period.YEAR || 
						archiveThisYear==archiveSettings.firstArchive || 
						archiveThisYear.equals(archiveSettings.firstArchive)) {
					PurgeDatabase purge = null;
					if(last!=null && last.year==year) {
						// db.removeItemsFromLastYear(); disabled because of new method
						//checkLast = true;
						purge = new PurgeDatabase(){
							@Override
							public void purge(ArchiveDatabase2 db) {
								db.removeItemsFromLastYear();
								//db.markYearBegin(year);
							}
						};
					} else {
						//checkLast = false;
						//db.markYearBegin(year);
					}		
					//db.markYearBegin(year);
					getDataFromArchive(archiveThisYear,db,purge,archiveSettings.addToTop,false/*year_offset>1*//*,checkLast*/);
					if(cache) db.save();
				} else {
					int qStart = 1;
					int qEnd = 4;
					// skip archives of old quarters, which we have loaded to db already from cache
					if(last!=null && year==last.year) {
						int lq = last.getQuarter();
						if(lq>qStart) {
							qStart = lq;
						}
					}
					// skip future quarters
					if(year==endYear) {
						qEnd = curQ; 
					}
					if(DEBUG) {qStart = 2;qEnd=2;endYear=2010;}
					//db.markYearBegin(year);
					for(int q=qStart;q<=qEnd;q++) {
						String archive = archiveThisYear.replace(
								Period.QUARTER.template(), String.valueOf(q));
						archive = archive.replace(
								Period.SEASON.template(), DateTools.russianSeasons[q-1]);
						PurgeDatabase purge = null;
						if(last!=null && year==last.year && q==last.getQuarter()) {
							//checkLast = true;
							//db.removeItemsFromLastQuarter();
							purge = new PurgeDatabase(){
								@Override
								public void purge(ArchiveDatabase2 db) {
									db.removeItemsFromLastQuarter();
								}
							};
						} else {
							//checkLast = false;
						}
						getDataFromArchive(archive,db,purge,archiveSettings.addToTop,false/*,checkLast*/);
						if(cache) db.save();
					}
				}
			}
			if(markAll) {
				db.markYears();
			} else {
				db.markYearsWhichNotMarked();
			}
		}
	}
	
	void getDataFromArchive(String archive, 
			ArchiveDatabase2 db, 
			PurgeDatabase purgeFunc, 
			boolean addToTop, 
			boolean markYear/*, boolean checkLast*/) throws IOException {
		log.info("scanning archive [["+archive+"]]");
		//int startFrom = 0;
		String lines[] = null;
		String text = null;
		try {
			text = wiki.getPageText(archive).trim();
		} catch (FileNotFoundException e) {
			log.warn("Archive "+archive + " not found (skiped)");
			return;
		}
		lines = text.split("\n"); 
		
		if(lines.length==0) {
			log.warn("archive "+archive+" is empty");
			return;
		}
		log.info(String.valueOf(lines.length)+" lines found");
		
		int i;
		int year = 0;
		if(addToTop) i = lines.length-1;
		else i = 0;
		int end;
		if(addToTop) end = -1;
		else end = lines.length;
		
		if(purgeFunc!=null) {
			String dump = null;
			try {
				dump = FileTools.readWikiFile("cache", archive+".txt");
			} catch (FileNotFoundException e) {
				// ignore
				log.info(e);
				log.info("Failed to read "+archive+".txt"+" (ignored)");
			} catch (UnsupportedEncodingException e) {
				log.error(e);
				log.info("Failed to read "+archive+".txt"+" (ignored)");
			}			
			
			if(dump!=null) {
				log.info("applying dump "+archive+".txt");
				dump = dump.trim();				
				if(addToTop && text.endsWith(dump)) {
					String lines2[] = dump.split("\n");
					i -= lines2.length;
					log.info("Skipping last "+String.valueOf(lines2.length)+" lines");
				} else if(!addToTop && text.startsWith(dump)) {
					String lines2[] = dump.split("\n");
					i += lines2.length;					
					log.info("Skipping first "+String.valueOf(lines2.length)+" lines");
				} else {
					purgeFunc.purge(db);
				}
			}
		}	
		try {
		FileTools.dump(text,"cache",archive+".txt");
		} catch (UnsupportedEncodingException e) {
			log.error(e);
		}
		text = null;
		/*
		if(lines==null) {
			try {
				lines = wiki.getPageLines(archive);
			} catch (FileNotFoundException e) {
				log.warn("Archive "+archive + " not found (skiped)");
				return;
			}
		}*/
		
		
		//ArchiveItem last = db.getLastFromCache();
//		boolean foundLast = true;
//		if(checkLast && last!=null) {
//			foundLast = false;
//		}
		//true;
		//if(DEBUG) i = 900;
		for(;i!=end;) {			
			String line = lines[i];			
			ArchiveItem item = parseLine(line);
			if(item==null) {
				log.trace("parse line: "+line+ "  NO ARTICLE");				
			} else {
				log.trace("parse line: "+line+ "  FOUND -> "+item.article);
				if(markYear && item.year!=year) {
					year = item.year;
					db.markYearBegin(year);
				}
//				if(foundLast)
					db.add(item);
//				else {
//					if(item.equals(last)) 
//						foundLast = true; 
//					else if(item.getDateAsInt()>last.getDateAsInt()) {
//						foundLast = true;
//						db.add(item);
//					} if some article was deleted and recreated, it will breake our db and a lot of duplicates will occur
//				}
			}
			if(addToTop)i--; else i++;
		}
	}
	
	ArchiveItem parseLine(String line) throws IOException {
		ArchiveItem item = null;
		Pattern p = Pattern.compile("\\[\\[(?<article>.+)\\]\\]");
		Matcher m = p.matcher(line);
		while(m.find()) {
			String article = m.group("article");
			if(	NewPages.userNamespace(article) || NewPages.categoryNamespace(article))
				continue;
			Revision r = null;
			r = wiki.getFirstRevision(article,true);
			if(r!=null) {
				/** sometimes shit happens, for instance check this page history 
				 *  http://ru.wikipedia.org/wiki/%D0%9B%D1%91%D0%B2%D0%B0_%D0%91%D0%B8-2
				 *  where 2 first revisions are blank 
				 *  (no user, no revision id, not possible to diff or something)
				 */
				if(r.getUser()==null) { 
					r = tryFindAnotherRev(article, r);
					if(r!=null) {
						item = new ArchiveItem(r);
						return item;
					}
				} else {
					item = new ArchiveItem(r);
					return item;
				}
				
			}
		}
		p = Pattern.compile("\\{\\{(?<template>.+)\\}\\}");
		m = p.matcher(line);
		if(m.find()) {
			String templateString = m.group("template");			
			String []items = templateString.split("\\|");
			if(items[0].trim().equals("u")) return null; // ignore "u" template
			Calendar c = null;			
			String article = null;
			String user = null;
			for(int i=1;i<items.length;i++) {
				String s = items[i];
				if(c==null) {
					c = DateTools.parseDate(s);
					if(c!=null) continue;
				}
				if(article==null && !NewPages.userNamespace(s)) {
					article = s;
					continue;
				} else if(user==null) {
					user = s;
				}				
			}
			if(c!=null && article!=null && user!=null) {
				item = new ArchiveItem(article,user,c);
				return item;
			} else if(article!=null) {
				Revision r = null;
				r = wiki.getFirstRevision(article,true);
				if(r!=null) {
					/** sometimes shit happens, for instance check this page history 
					 *  http://ru.wikipedia.org/wiki/%D0%9B%D1%91%D0%B2%D0%B0_%D0%91%D0%B8-2
					 *  where 2 first revisions are blank 
					 *  (no user, no revision id, not possible to diff or something)
					 */
					if(r.getUser()==null) { 
						r = tryFindAnotherRev(article, r);
						if(r!=null) {
							item = new ArchiveItem(r);
							return item;
						}
					} else {
						item = new ArchiveItem(r);
						return item;
					}
					
				}
			}
		}		
		return null;
	}
	
	Revision tryFindAnotherRev(String article, Revision r) throws IOException {
		Revision []revs = null;
		//HashMap<String,Object> info = null;
		String redirect = null;
			//r = m_wiki.getTopRevisionWithNewTitle(article,true);
			//info = m_wiki.getPageInfo(article);
		redirect = wiki.resolveRedirect(article);
		String realPage = (redirect==null)?article:redirect;
		Calendar c1 = r.getTimestamp();
		Calendar c2 = Calendar.getInstance();
		c2.set(c1.get(Calendar.YEAR)+2, 0, 1); // we give 2 years chance to find next rev
		r = null;
		revs = wiki.getPageHistory(realPage, c2, c1);
		if(revs!=null) {
			for(int i=revs.length-1;i>=0;i--) {
				Revision rev = revs[i];
				if(rev.getUser()!=null) {
					return rev;
				}
			}
		}
		return null;
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
