/**
 *  @(#)StatisticsMonth.java 20/10/2012
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

/**
 * @author kin
 *
 */
public class StatisticsMonth extends Statistics {
	int year = 0;

	/**
	 * @param type
	 * @throws FileNotFoundException
	 * @throws BadAttributeValueExpException
	 */
	public StatisticsMonth(String type) throws FileNotFoundException,
			BadAttributeValueExpException {
		super(type);
		year = 0;
	}
	
	/**
	 * @param type
	 * @throws FileNotFoundException
	 * @throws BadAttributeValueExpException
	 */
	public StatisticsMonth(String type, int year) throws FileNotFoundException,
			BadAttributeValueExpException {
		super(type);
		this.year = year;
	}
	
	public void put(ArchiveDatabase2 db) throws IllegalStateException {
		// 1) find out period of the first week
		ListIterator<ArchiveItem> it = null;
		if(this.year==0) it = db.getIterator();
		else it = db.getYearIterator(year);
		HashMap<String,Integer> userstat = new HashMap<String,Integer>(100);
		//HashMap<Integer,Integer> articlestat = new HashMap<Integer,Integer>(7);
		int articlestat[] = new int[31];
		Arrays.fill(articlestat, 0);
		
		int total = 0;
		int curMonth = 0;
		int y = 0;
		ArchiveItem item = null;
		if(it.hasNext()) {
			item = it.next();
			if(this.year!=0 && item.year!=this.year) 
				throw new IllegalStateException("year processing: "+this.year+", item's year: "+item.year+", item: "+item.article);
			y = item.year;			
			curMonth = item.month;
			userstat.put(item.user, 1);
			articlestat[item.day-1] = 1;
			total = 1;
		}
		
		while(it.hasNext()) {
			item = it.next();
			if(this.year!=0 && item.year!=this.year) 
				throw new IllegalStateException("year processing: "+this.year+", item's year: "+item.year+", item: "+item.article);
			//boolean ny = (this.year!=0 && item.year!=y);
			if(item.month!=curMonth) {
				// calculate stat and add to list 
				this.addStat(y, curMonth, total, articlestat, userstat);
				curMonth = item.month;
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
			articlestat[item.day-1]++;
			total++;			
		}
		// last stat
		if(item!=null)
			this.addStat(y, curMonth, total, articlestat, userstat);
		
		calcTotal();
	}
	
	
	void addStat(int year, int month, int total, int articlestat[],Map<String,Integer> userstat) {
		StatItem stat = new StatItem();
		stat.number = month+1;	
		stat.month = month;
		stat.year = year;
		stat.articles = total;
		
		int min = 1000000;
		int max = 0;
		int sum = 0;
		Calendar c = Calendar.getInstance();
		c.set(year, month, 1);
		int mdays = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		for(int i=0;i<mdays;i++) {
			int n =articlestat[i];
			sum +=n;
			if(n<min) min = n;
			if(n>max) max = n;
		}
		stat.articlesDayMid = ((float)sum/(float)mdays);
		stat.articlesDayMax = max;
		stat.articlesDayMin = min;
		String user = "nobody";
		int userc = -1;
		for (Map.Entry<String, Integer> entry : userstat.entrySet()) {
			if(entry.getValue()>userc) {
				user = entry.getKey();
				userc = entry.getValue();
			}
		}			
		stat.user = user;
		stat.userArticles = userc;
		if(!items.isEmpty()) {
			StatItem prev = this.items.get(items.size()-1);
			stat.progress = stat.articles - prev.articles;
			//stat.progress = (int) ((prev.articles - stat.articles)/(prev.articles==0?1:(prev.articles*0.1)));
		}
		this.items.add(stat);
		addUserStat(userstat);
	}

}
