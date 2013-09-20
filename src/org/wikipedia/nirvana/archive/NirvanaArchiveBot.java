/**
 *  @(#)NirvanaArchiveBot.java 1.2 20/10/2012
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
package org.wikipedia.nirvana.archive;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import org.wikipedia.nirvana.DateTools;
import org.wikipedia.nirvana.FileTools;
import org.wikipedia.nirvana.NirvanaBasicBot;
import org.wikipedia.nirvana.archive.ArchiveSettings.Period;
import org.wikipedia.nirvana.nirvanabot.NewPages;
import org.wikipedia.nirvana.nirvanabot.NirvanaBot;

/**
 * @author kin
 *
 */
public class NirvanaArchiveBot extends NirvanaBasicBot{
	public static final String delimeter = "\n";	

	private static String TASK_LIST_FILE = "task.txt";
	//public static String COMMENT = "Проставление заголовков и нумерации в архиве";
	
	public static final String INFO = 
		"NirvanaArchiveBot v1.3 Updates archives of new articles lists at http://ru.wikipedia.org\n" +
		"Copyright (C) 2011-2012 Dmitry Trofimovich (KIN)\n" +		
		"\n";
	
	public void showInfo() {
		System.out.print(INFO);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NirvanaBasicBot bot = new NirvanaArchiveBot();
		bot.showInfo();
		//bot.showLicense();
		System.out.print("-----------------------------------------------------------------\n");
		String configFile = bot.getConfig(args);		
		System.out.println("applying config file: "+configFile);
		bot.startWithConfig(configFile);
	}
	
	

	protected void go() {
		//log.info("Bot");
		// исходные данные:
		// 1) архив который нужно обновить
		// 2) формат заголовка
		// 3) формат второго заголовка
		// 4) нумерация, сверху/снизу
		// 5) ботоправка,малая правка
		// ~4) удалять старые заголовки?
		// ~5) шапка
		// ~6) подвал
		
		
		String task = FileTools.readFile(TASK_LIST_FILE);
		
		if(task==null)
			return;
		
		/*
		try {
			FileTools.dump(task, "dump", "task.txt");
		} catch (IOException e) {			
			e.printStackTrace();
		}*/
		Map<String, String> options = new HashMap<String, String>();
		if(!textOptionsToMap(task,options)) {
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
			//archiveSettings.archive = options.get(key);
			//archiveSettings.archivePeriod = ArchiveSettings.getHeaderPeriod(archive);
			/*if(archiveSettings.archivePeriod.degree()<Period.MONTH.degree()) {
				
			}*/
			//data.errors.addAll(parseArchiveName(archiveSettings,options.get(key)));
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
		
		key = "формат подзаголовка в архиве";
		Period p2 = Period.NONE;
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			str = options.get(key); 
			p2 = ArchiveSettings.getHeaderPeriod(str);
			if(p2==Period.NONE) {
				log.error("В параметре \"формат подзаголовка в архиве\" не задан переменный параметр. Значение этого параметра не принято.");
				return;
			} else {
				archiveSettings.headerHeaderFormat = archiveSettings.headerFormat;
				archiveSettings.headerFormat = str;
			}			
		}
		
		if(p1!=Period.NONE && p2!=Period.NONE && p1==p2) {
			log.error("Параметр \"формат заголовка в архиве\" и параметр \"формат подзаголовка в архиве\" имеют одинаковый период повторения "+p1.template());
			return;
		}
		
		//data.errors.addAll(validateArchiveFormat(archiveSettings));
		
		/*
		if(archiveSettings.headerFormat==null && archiveSettings.headerHeaderFormat==null) {
			archiveSettings.headerFormat = ArchiveSettings.DEFAULT_HEADER_FORMAT;
		} */
		
		
		
		key = "параметры архива";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			ArrayList<String> errors = NirvanaBot.parseArchiveSettings(archiveSettings,options.get(key));
			if(errors.size()>0) {
				Object err_list[] = errors.toArray();
				for(Object ob:err_list) {
					log.error((String)(ob));
				}
				return;
			}
			//archiveSettings.headerFormat = options.get(key); 
		}
		
		key = "первый год";
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			try {
				archiveSettings.startYear = Integer.parseInt(options.get(key));
			} catch(NumberFormatException e) {
				log.warn(String.format(NirvanaBot.ERROR_PARSE_INTEGER_FORMAT_STRING, key, options.get(key)));
			}
		}

		
		/*
		String prefix = "";
		if (options.containsKey("префикс"))
		{
			prefix = options.get("префикс");
		}*/
		
		//boolean markEdits = true;
		boolean bot = true;
		boolean minor = true;
		if (options.containsKey("помечать правки") && !options.get("помечать правки").isEmpty()) {
			String mark = options.get("помечать правки").toLowerCase();
			if(mark.equalsIgnoreCase("нет")) {
				//markEdits = false;
				bot = false;
				minor = false;
			} else {				
				if(!mark.contains("бот") && mark.contains("малая")) {
					bot = false;
				} else if(mark.contains("бот") && !mark.contains("малая")) {
					minor = false;
				}				
			}
		}
		log.info("archive settings OK");
		
		updateAllArchives(archive, archiveSettings, bot, minor);
		
	}
	
	private void updateAllArchives(String archive, ArchiveSettings archiveSettings, boolean bot, boolean minor) {
		//ArrayList<String> archive_list = new ArrayList<String>();
		if(archiveSettings.archivePeriod==Period.NONE) {
			//archive_list.add(archive);
			try {
				updateArchive(archive, archiveSettings, bot, minor);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (LoginException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			int start_year = archiveSettings.startYear;
			Calendar c = Calendar.getInstance();
			int end_year = c.get(Calendar.YEAR);
			if(!archiveSettings.archive.contains(Period.YEAR.template())) {
				start_year = end_year;
			}
			for(int year=start_year;year<=end_year;year++) {
				int start_quarter = 0;
				int end_quarter = 3;
				if(!archiveSettings.archive.contains(Period.QUARTER.template()) && 
						!archiveSettings.archive.contains(Period.SEASON.template())) {
					end_quarter = 0;
				}
				for(int q=start_quarter;q<=end_quarter;q++) {
					String page = archive.replace(Period.YEAR.template(), String.valueOf(year)).
							replace(Period.SEASON.template(),DateTools.russianSeasons[q]).
							replace(Period.QUARTER.template(),String.valueOf(q+1));
					try {
						updateArchive(page, archiveSettings, bot, minor);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (LoginException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	private void updateArchive(String archive, ArchiveSettings archiveSettings, boolean bot, boolean minor) throws IOException, LoginException {
		log.info("updating archive: "+archive);
		String lines[]= null;
		try {
			lines = this.wiki.getPageLines(archive);
		} catch (FileNotFoundException e) {
			log.info("archive not found");
			return;
		} catch (IOException e) {
			log.error("Failed to get archive page"+e.toString());
			e.printStackTrace();
			return;
		}
		
		Archive thisArchive = ArchiveFactory.createArchive(archiveSettings, wiki, archive, delimeter, true);
    	
		
		int delta = 0;
		int start,end;
		if(archiveSettings.addToTop) {
			delta = -1;
			start = lines.length-1;
			end = -1;
		} else {
			delta = 1;
			start = 0;
			end = lines.length;
		}
		Pattern p = Pattern.compile("^==(=?)(\\s*)(?<headername>[^=]+)(\\s*)(=?)==$");
		
		for(int i=start;i!=end;i+=delta) {
			String item = lines[i];			
			if(!item.isEmpty() &&
					archiveSettings.removeDuplicates ||
					(	item.compareToIgnoreCase(Archive.OL)!=0 &&
					 	item.compareToIgnoreCase(Archive.OL_END)!=0 &&
					 	!p.matcher(item).matches())	) {
				if(archiveSettings.removeDuplicates) {
					((ArchiveUnique)thisArchive).add(item);
				} else if(archiveSettings.withoutHeaders()) {
	    			if(!archiveSettings.hasHtmlEnumeration()) {
	    				if(archiveSettings.sorted)
	    					((ArchiveSimpleSorted)thisArchive).add(item);
	    				else
	    					((ArchiveSimple)thisArchive).add(item);
	    			} else {
	    				((ArchiveWithEnumeration)thisArchive).add(item);
	    			}
	    		} else {
	    			Calendar c = NewPages.getNewPagesItemDate(wiki,item);
		    		if(c==null) {
		    			thisArchive.add(item);
		    			//continue;
		    		} else {
			    		String thisHeader = archiveSettings.getHeaderForDate(c);
			    		String superHeader = archiveSettings.getHeaderHeaderForDate(c);
			    		if(superHeader==null) {
			    			((ArchiveWithHeaders)thisArchive).add(item, thisHeader);
			    		} else {
			    			((ArchiveWithHeaders)thisArchive).add(item, thisHeader,superHeader);
			    		}
		    		}
	    		}
			}
		}
		wiki.edit(archive, thisArchive.toString(), COMMENT, minor, bot);
	}
	
	//protected void
	
	protected void loadCustomProperties() {
		TASK_LIST_FILE = properties.getProperty("task-list-file",TASK_LIST_FILE);
		log.info("task list file: "+TASK_LIST_FILE);
		//COMMENT = properties.getProperty("update-comment", COMMENT);
	}
}
