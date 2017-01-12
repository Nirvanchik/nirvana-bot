/**
 *  @(#)StatisticsWeek.java 20/10/2012
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import javax.management.BadAttributeValueExpException;

import org.wikipedia.nirvana.DateTools;
import org.wikipedia.nirvana.NirvanaWiki;

/**
 * @author kin
 *
 */
public class StatisticsWeek extends Statistics {
	int year;
	public static final boolean HIDE_WEEK_0 = true;
	
		
	/**
	 * @param type
	 * @throws FileNotFoundException
	 * @throws BadAttributeValueExpException
	 */
    public StatisticsWeek(NirvanaWiki wiki, String cacheDir, String type, int year)
            throws FileNotFoundException, BadAttributeValueExpException {
        super(wiki, cacheDir, type);
		this.year = year;
	}
	
	/**
	 * @param type
	 * @throws FileNotFoundException
	 * @throws BadAttributeValueExpException
	 */
    public StatisticsWeek(NirvanaWiki wiki, String cacheDir, String type)
            throws FileNotFoundException, BadAttributeValueExpException {
        super(wiki, cacheDir, type);
		this.year = 0;
	}
	
		
	public void put(ArchiveDatabase2 db) throws IllegalStateException {
		Calendar c = Calendar.getInstance();
		c.setFirstDayOfWeek(DateTools.FIRST_DAY_OF_WEEK);
		c.setMinimalDaysInFirstWeek(DateTools.MINIMAL_DAYS_IN_FIRST_WEEK);	
		
		
		// 1) find out period of the first week
		ListIterator<ArchiveItem> it = null;
		if(this.year==0) it = db.getIterator();
		else it = db.getYearIterator(year);
		HashMap<String,Integer> userstat = new HashMap<String,Integer>(100);
		//HashMap<Integer,Integer> articlestat = new HashMap<Integer,Integer>(7);
		int articlestat[] = new int[7];
		Arrays.fill(articlestat, 0);

		
		int total = 0;
		int curWeek = 0;
		int y = 0;
		ArchiveItem item = null;
		if(it.hasNext()) item = it.next();
		//else return;
		if(item!=null && this.year!=0 && item.year!=this.year) 
			throw new IllegalStateException("year processing: "+this.year+", item's year: "+item.year+", item: "+item.article);
		if(item!=null) {		
					//DateTools.dayToWeek(item.year, item.month, item.day);
			y = item.year;			
			c.set(item.year, item.month, item.day);
			curWeek = c.get(Calendar.WEEK_OF_YEAR);
			if(item.month==0 && item.day<=7 && curWeek>50) {
				curWeek = 0;
			}
			int wday = c.get(Calendar.DAY_OF_WEEK)-1;  
			userstat.put(item.user, 1);
			articlestat[wday] = 1;
			total = 1;
		}
		
		while(it.hasNext()) {
			item = it.next();
			if(this.year!=0 && item.year!=this.year) 
				throw new IllegalStateException("year processing: "+this.year+", item's year: "+item.year+", item: "+item.article);
			c.set(item.year, item.month, item.day);
			//int week = DateTools.dayToWeek(item.year, item.month, item.day);
			int week = c.get(Calendar.WEEK_OF_YEAR);
			
			if(item.month==0 && item.day<=7 && week>50) {
				week = 0;
			}
			//boolean ny = (this.year!=0 && item.year!=y);
			if(week!=curWeek) {
				// calculate stat and add to list 
				this.addStat(y, curWeek, total, articlestat, userstat);
				curWeek = week;
				if(y!=item.year)
					y = item.year;
				// zero stat
				total = 0;
				Arrays.fill(articlestat, 0);
				userstat.clear();
			} 
			// add item to current stat
			
			Integer n = userstat.get(item.user);
			if(n==null) userstat.put(item.user, 1);
			else userstat.put(item.user, n+1);
			int wday = c.get(Calendar.DAY_OF_WEEK)-1; 
			articlestat[wday]++;
			total++;			
		}
		// last stat
		if(item!=null)
			this.addStat(y, curWeek, total, articlestat, userstat);
		
		calcTotal();
	}
	
	
	void addStat(int year, int week, int total, int articlestat[],Map<String,Integer> userstat) {
		StatItem weekStat = new StatItem();
		weekStat.number = week;			
		weekStat.month = DateTools.weekToMonth(year,week);
		weekStat.year = year;
		weekStat.articles = total;
		
		int min = 1000000;
		int max = 0;
		int sum = 0;
		for(int n:articlestat) {
			sum +=n;
			if(n<min) min = n;
			if(n>max) max = n;
		}
		weekStat.articlesDayMid = (float) (sum/7.0);
		weekStat.articlesDayMax = max;
		weekStat.articlesDayMin = min;
		String user = "nobody";
		int userc = -1;
		for (Map.Entry<String, Integer> entry : userstat.entrySet()) {
			if(entry.getValue()>userc) {
				user = entry.getKey();
				userc = entry.getValue();
			}
		}			
		weekStat.user = user;
		weekStat.userArticles = userc;
		if(!items.isEmpty()) {
			StatItem prev = this.items.get(items.size()-1);
			weekStat.progress = weekStat.articles - prev.articles;
			//weekStat.progress = (int) ((prev.articles - weekStat.articles)/(prev.articles==0?1:(prev.articles*0.1)));
		}
		this.items.add(weekStat);
		addUserStat(userstat);
	}
	
	
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(customHeader);
		sb.append(header);
		sb.append(DELIMETER);
		for(int i=0;i<items.size();i++) {
			StatItem item = items.get(i);
			if(i==0 && HIDE_WEEK_0) continue;
			sb.append(item.toString());
			sb.append(DELIMETER);
		}		
		if(this.totalTemplate!=null && !totalTemplate.isEmpty()) {
			sb.append(total.toString(totalTemplate));
			sb.append(DELIMETER);
		}
		sb.append(footer);
		sb.append(customFooter);
		return sb.toString();
	}
	

}
