/**
 *  @(#)Statistics.java 20/10/2012
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.BadAttributeValueExpException;

import org.wikipedia.nirvana.DateTools;
import org.wikipedia.nirvana.FileTools;
import org.wikipedia.nirvana.NirvanaBasicBot;


/**
 * @author kin
 *
 */
public class Statistics {	
	public static final String DELIMETER = "\n";
	public static final String DEFAULT_INI_FOLDER = "statistics";
	public static final String DEFAULT_CACHE_FOLDER = "cache";
	public static String cacheFolder = DEFAULT_CACHE_FOLDER;
	public static String iniFolder = DEFAULT_INI_FOLDER;
	public static String portal = "";
	String header;
	String footer;
	String totalTemplate;
	String itemTemplate;
	//String ini;
	String type;
	protected List<StatItem> items;
	protected Map<String,Integer> totalUserStat;
	protected TotalItem total;
	protected org.apache.log4j.Logger log = null;
	
	protected class StatItem {
		public int number;
		public int year;
		public int month;
		public String title;
		public int progress;
		public int articles;
		public int articlesDayMin;
		public int articlesDayMax;
		public float articlesDayMid;
		public String user;
		public int userArticles;
		public String toString() {
			String progress = "{{нет изменений}}";
			if(this.progress>0) progress = "{{рост}}";
			else if(this.progress<0) progress = "{{падение}}";
			//if(//this.a)
			String mid="0";
			if(this.articlesDayMid>=2.0)
				mid = String.valueOf(Math.round(this.articlesDayMid));
			else
				mid = new DecimalFormat("#.#").format(this.articlesDayMid);
			log.debug("item to String "+number +" year "+year+" month "+month);
			String str = itemTemplate.replace("%(номер)", String.valueOf(this.number)).
					replace("%(месяц)",DateTools.russianMonths[this.month]).
					replace("%(год)",String.valueOf(this.year)).
					replace("%(статей)",String.valueOf(this.articles)).
					replace("%(прогресс)",progress).
					replace("%(MID в день)",mid).
					replace("%(MIN в день)",String.valueOf(this.articlesDayMin)).
					replace("%(MAX в день)",String.valueOf(this.articlesDayMax)).
					replace("%(участник)",this.user).
					replace("%(статей участника)",String.valueOf(this.userArticles));
			return str;
		}
		StatItem () {
			number = 0;
			year = 0;
			month = 0;
			title = "";
			progress = 0;
			articles = 0;
			articlesDayMin = 0;
			articlesDayMax = 0;
			articlesDayMid = 0;
			user = "";
			userArticles = 0;
		}
	};
	protected class TotalItem {
		public int articles;
		public int articlesMid;
		public int articlesDayMin;
		public int articlesDayMax;
		public float articlesDayMid;
		public String user;
		public int userArticles;
		String toString(String template) {
			String mid="0";
			if(this.articlesDayMid>=2.0)
				mid = String.valueOf(Math.round(this.articlesDayMid));
			else
				mid = new DecimalFormat("#.#").format(this.articlesDayMid);
			String str = totalTemplate.replace("%(статей)",String.valueOf(this.articles)).
					replace("%(MID в день)",mid).
					replace("%(MIN в день)",String.valueOf(this.articlesDayMin)).
					replace("%(MAX в день)",String.valueOf(this.articlesDayMax)).
					replace("%(участник)",this.user).
					replace("%(статей участника)",String.valueOf(this.userArticles));
			return str; 
		}
		TotalItem () {
			articles = 0;
			articlesMid = 0;
			articlesDayMin = 0;
			articlesDayMax = 0;
			articlesDayMid = 0;
			user = "";
			userArticles = 0;
		}
	};
	
	public void createTotal() {
		total = new TotalItem();
	}
	
	Statistics(String type) throws FileNotFoundException, BadAttributeValueExpException {
		this.type = type;
		init();
	}
	
	public void setOptions(Map<String,String> options) {
		// no default options
	}
	
	protected void init() throws FileNotFoundException, BadAttributeValueExpException {
		log = org.apache.log4j.Logger.getLogger(this.getClass().getName());		
		items = new ArrayList<StatItem>(100);
		totalUserStat = new HashMap<String,Integer>(200);
		createTotal();
		String ini = iniFolder+"\\"+type+".ini";
		String file = FileTools.readFile(ini);
		if(file==null) {
			log.error("file "+ini +" not found");
			throw new FileNotFoundException(ini);
		}
		Map<String, String> options = new HashMap<String, String>();
		if(!NirvanaBasicBot.textOptionsToMap(file,options)) {
			log.error("incorrect settings for statistics");
			throw new BadAttributeValueExpException(file);
		}
		getSettingsFromIni(options);
	}
	protected void getSettingsFromIni(Map<String, String> options) throws BadAttributeValueExpException {
		
		header = options.get("шапка");
		itemTemplate = options.get("формат элемента");
		footer = options.get("подвал");
		totalTemplate = options.get("итого");		
		if(header==null||itemTemplate==null||footer==null) {
			log.error("incorrect settings for statistics");
			throw new BadAttributeValueExpException(this.type);
		}
		header = header.replace("\\n", "\n");
		itemTemplate = itemTemplate.replace("\\n", "\n");
		footer = footer.replace("\\n", "\n");
		if(totalTemplate!=null) totalTemplate = totalTemplate.replace("\\n", "\n");
	}
	public void put(ArchiveDatabase2 db) {}
	
	protected void calcTotal() {
		if(this.items.isEmpty()) {
			return;
		}
		this.total.articlesDayMin = 1000000;
		float midday = 0;
		//Map<String, Integer> userstat = new HashMap<String,Integer>(10); this is for old total user stat		
		for(StatItem stat:this.items) {
			if(stat.articlesDayMin<total.articlesDayMin)
				total.articlesDayMin = stat.articlesDayMin;
			if(stat.articlesDayMax>total.articlesDayMax)
				total.articlesDayMax = stat.articlesDayMax;
			total.articles+= stat.articles;
			midday+=stat.articlesDayMid;
			/* old version of total user stat
			Integer n = userstat.get(stat.user);
			if(n==null) userstat.put(stat.user, stat.userArticles);
			else userstat.put(stat.user, n+stat.userArticles);
			*/
		}
		total.articlesDayMid = midday/this.items.size();
		total.articlesMid = total.articles/this.items.size();		
		total.userArticles = 0;
		/* old version of total user stat
		for (Map.Entry<String, Integer> entry : userstat.entrySet()) {
			if(entry.getValue()>total.userArticles) {
				total.user = entry.getKey();
				total.userArticles = entry.getValue();
			}
		}*/		
		// new version of total user stat
		String user = "nobody";
		int n = -1;
		for (Map.Entry<String, Integer> entry : this.totalUserStat.entrySet()) {
			if(entry.getValue()>n) {
				user = entry.getKey();
				n = entry.getValue();
			}
		}
		this.total.user = user;
		this.total.userArticles = n;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(header);
		sb.append(DELIMETER);
		for(StatItem item:items) {
			sb.append(item.toString());
			sb.append(DELIMETER);
		}		
		if(this.totalTemplate!=null && !totalTemplate.isEmpty()) {
			sb.append(total.toString(totalTemplate));
			sb.append(DELIMETER);
		}
		sb.append(footer);
		return sb.toString();
	}
	
	protected void addUserStat(Map<String,Integer> userstat) {
		for (Map.Entry<String, Integer> entry : userstat.entrySet()) {
			if(entry.getValue()>total.userArticles) {
				String user = entry.getKey();
				int a = entry.getValue();
				Integer n = this.totalUserStat.get(user);
				if(n==null) this.totalUserStat.put(user, a);
				else this.totalUserStat.put(user, n+a);
			}
		}
	}
	
	public Map<String,Integer> getUserStat() {return this.totalUserStat;}
}
