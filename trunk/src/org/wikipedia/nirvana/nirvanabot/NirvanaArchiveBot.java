/**
 *  @(#)NirvanaArchiveBot.java 0.01 02/07/2012
 *  Copyright � 2012 Dmitry Trofimovich (KIN)
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import org.wikipedia.nirvana.DateTools;
import org.wikipedia.nirvana.NirvanaBasicBot;
import org.wikipedia.nirvana.nirvanabot.ArchiveSettings.Period;

/**
 * @author kin
 *
 */
public class NirvanaArchiveBot extends NirvanaBasicBot{
	private static String TASK_LIST_FILE = "task.txt";
	public static final String delimeter = "\n";
	
	//public static String COMMENT = "������������ ���������� � ��������� � ������";
	
	public static final String INFO = 
		"NirvanaArchiveBot v1.01 Updates archives of new articles lists at http://ru.wikipedia.org\n" +
		"Copyright (C) 2011-2012 Dmitry Trofimovich (KIN)\n" +
		"This program comes with ABSOLUTELY NO WARRANTY; for details type `'.\n" +
		"This is free software, and you are welcome to redistribute it\n"+
		"under certain conditions; type `' for details.\n"+
		"";
	
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
		// �������� ������:
		// 1) ����� ������� ����� ��������
		// 2) ������ ���������
		// 3) ������ ������� ���������
		// 4) ���������, ������/�����
		// 5) ����������,����� ������
		// ~4) ������� ������ ���������?
		// ~5) �����
		// ~6) ������
		
		File taskFile = new File(TASK_LIST_FILE);
		String task = null;
		try {
			//FileInputStream in = new FileInputStream(taskFile);
			//String line;
	        StringBuilder sb = new StringBuilder(10000);
	        FileReader fr = null;
	        fr = new FileReader(taskFile);
	       // BufferedReader br = new BufferedReader(fr);
	        
	        //fr.close();
	        char buf[] = new char[1000];
	       // fr.read(buf);
	        //log.info(new String(buf));
	        int end = 0;
	        while ((end=fr.read(buf))>0)
	        {	        	
	            sb.append(buf,0,end);	           
	        }
	        //sb.append(buf);
			fr.close();
			task = sb.toString();
		} catch (FileNotFoundException e) {
			log.error(e.toString());
			return;
		} catch (IOException e) {
			log.error(e.toString());
			e.printStackTrace();
			return;
		}
		/*
		try {
			FileTools.dump(task, "dump", "task.txt");
		} catch (IOException e) {			
			e.printStackTrace();
		}*/
		Map<String, String> options = new HashMap<String, String>();
		if(!parseTemplate(task,options)) {
			log.error("incorrect settings");
			return;
		}
		
		ArchiveSettings archiveSettings = new ArchiveSettings();
		String archive = null;
		String key = "�����";
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
		key = "������ ��������� � ������";
		Period p1 = Period.NONE;
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			str = options.get(key);
			p1 = ArchiveSettings.getHeaderPeriod(str);
			if(p1==Period.NONE) {
				log.error("� ��������� \"������ ��������� � ������\" �� ����� ���������� ��������. �������� ����� ��������� �� �������.");
				return;
			} else {
				archiveSettings.headerFormat = str;
			}
		}
		
		key = "������ ������������ � ������";
		Period p2 = Period.NONE;
		if (options.containsKey(key) && !options.get(key).isEmpty()) {
			str = options.get(key); 
			p2 = ArchiveSettings.getHeaderPeriod(str);
			if(p2==Period.NONE) {
				log.error("� ��������� \"������ ������������ � ������\" �� ����� ���������� ��������. �������� ����� ��������� �� �������.");
				return;
			} else {
				archiveSettings.headerHeaderFormat = archiveSettings.headerFormat;
				archiveSettings.headerFormat = str;
			}			
		}
		
		if(p1!=Period.NONE && p2!=Period.NONE && p1==p2) {
			log.error("�������� \"������ ��������� � ������\" � �������� \"������ ������������ � ������\" ����� ���������� ������ ���������� "+p1.template());
			return;
		}
		
		//data.errors.addAll(validateArchiveFormat(archiveSettings));
		
		/*
		if(archiveSettings.headerFormat==null && archiveSettings.headerHeaderFormat==null) {
			archiveSettings.headerFormat = ArchiveSettings.DEFAULT_HEADER_FORMAT;
		} */
		
		
		
		key = "��������� ������";
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
		
		/*
		String prefix = "";
		if (options.containsKey("�������"))
		{
			prefix = options.get("�������");
		}*/
		
		//boolean markEdits = true;
		boolean bot = true;
		boolean minor = true;
		if (options.containsKey("�������� ������") && !options.get("�������� ������").isEmpty()) {
			String mark = options.get("�������� ������").toLowerCase();
			if(mark.equalsIgnoreCase("���")) {
				//markEdits = false;
				bot = false;
				minor = false;
			} else {				
				if(!mark.contains("���") && mark.contains("�����")) {
					bot = false;
				} else if(mark.contains("���") && !mark.contains("�����")) {
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
			int start_year = 2008;
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
		
		Archive thisArchive = ArchiveFactory.createArchive(archiveSettings, wiki, archive, delimeter,true);
    	
		
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
					item.compareToIgnoreCase(Archive.OL)!=0 &&
					item.compareToIgnoreCase(Archive.OL_END)!=0 &&
					!p.matcher(item).matches()) {
				Calendar c = NewPages.getNewPagesItemDate(wiki,item);
	    		if(c==null) {
	    			thisArchive.add(item);
	    			continue;
	    		} 
	    		if(archiveSettings.withoutHeaders()) {
	    			if(!archiveSettings.hasHtmlEnumeration()) {
	    				((ArchiveSimple)thisArchive).add(item);
	    			} else {
	    				((ArchiveWithEnumeration)thisArchive).add(item);
	    			}
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
		wiki.edit(archive, thisArchive.toString(), COMMENT, minor, bot);
	}
	
	private static boolean parseTemplate(String text, Map<String, String> parameters)
    {
		String lines[] = text.split("\r|\n");
		log.debug("Archive settings");
		for(String line: lines) {			
			if(line.trim().isEmpty()) continue;
			log.debug(line);
			int index = line.indexOf("=");
			if(index<0) return false;
			parameters.put(line.substring(0,index).trim(), line.substring(index+1).trim());
		}
		return true;
    }
	//protected void
	
	protected void loadCustomProperties() {
		TASK_LIST_FILE = properties.getProperty("task-list-file",TASK_LIST_FILE);
		log.info("task list file: "+TASK_LIST_FILE);
		//COMMENT = properties.getProperty("update-comment", COMMENT);
	}
}
