/**
 *  @(#)RatingTotal.java 20/10/2012
 *  Copyright © 2012 - 2014 Dmitry Trofimovich (KIN)
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
 * This file is encoded with UTF-8.
 * */
package org.wikipedia.nirvana.statistics;

import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import javax.management.BadAttributeValueExpException;

import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.nirvanabot.NirvanaBot;

/**
 * @author kin
 *
 */
public class RatingTotal extends Rating {
	private static final int DEFAULT_SIZE = 20;
	private static int DEFAULT_START_YEAR = 2008;
	int size = DEFAULT_SIZE;
	int startYear = DEFAULT_START_YEAR;
	int endYear = 0;
	Map<Integer,Map<String,Integer>> userstatByYear;
	String userItemTemplate;
	String headerItemTemplate;
	
	public static void setDefaultStartYear(int year) {
		DEFAULT_START_YEAR = year;
	}
	
	public static int getDefaultStartYear() {
		return DEFAULT_START_YEAR;
	}
	
	protected class StatItemRT extends StatItem {
		public Map<Integer,Integer> userArticlesByYear;
		StatItemRT() {
			super();
			this.userArticlesByYear = new HashMap<Integer,Integer>(5);
		}
		StatItemRT(StatItem item) {
			super();
			this.user = item.user;
			this.number = item.number;
			this.userArticles = item.userArticles;
			this.userArticlesByYear = new HashMap<Integer,Integer>(5);
			this.progress = item.progress;
		}
		public String toString() {
			String str = super.toString();
			StringBuffer sb = new StringBuffer("");
			for(int year = startYear;year<=endYear;year++)
				sb.append(userItemTemplate.replace(
                        "%(статей участника за год)", 
						String.valueOf(this.userArticlesByYear.get(year))));
            str = str.replace("%(статьи участника по годам)", sb.toString());
			return str;
		}
	};

	/**
	 * @param type
	 * @throws FileNotFoundException
	 * @throws BadAttributeValueExpException
	 */
    public RatingTotal(NirvanaWiki wiki, String cacheDir, String type)
            throws FileNotFoundException, BadAttributeValueExpException {
        super(wiki, cacheDir, type);
		startYear = DEFAULT_START_YEAR;
		endYear = Calendar.getInstance().get(Calendar.YEAR);
		userstatByYear = new HashMap<Integer,Map<String,Integer>>(5);
	}
	
	public void setOptions(Map<String,String> options) {
		super.setOptions(options);
        String key = "первый год";
		if(options.containsKey(key) && !options.get(key).isEmpty()) {
			try {
				this.startYear = Integer.parseInt(options.get(key));
			} catch(NumberFormatException e) {
                log.warn(String.format(NirvanaBot.ERROR_PARSE_INTEGER_FORMAT_STRING_EN, key,
                        options.get(key)));
			}					
		}
	}
	
	public void put(ArchiveDatabase2 db) throws IllegalStateException {
		if (!this.filterBySize) {
    		Statistics totalReporter = StatisticsFabric.getReporterWithUserData(this);
    		Map<Integer,Statistics> reporterByYear = new HashMap<Integer,Statistics>(5);
    		int years = endYear - startYear + 1;
    		for(int year = startYear;year<=endYear;year++) {
    			Statistics rep = StatisticsFabric.getReporterWithUserData(this,year);
    			if(rep!=null) reporterByYear.put(year, rep);
    		}
    		
    		if(totalReporter!=null) {
    			this.totalUserStat = totalReporter.getUserStat();
    		}
    		
    		if(reporterByYear.size()==years) {
    			for(int year = startYear;year<=endYear;year++) {
    				Statistics rep = reporterByYear.get(year);				 
    				userstatByYear.put(year, rep.getUserStat());
    			}
    		}
		}
		
		if (userstatByYear.isEmpty()) {
			putFromDb(db); 
		} else if(totalUserStat.isEmpty()) {
			super.putFromDb(db); 
		} 
		
		analyze(); // here user rating is prepared
		//analyze2(); // here user rating is extended with articles by year
		
	}
	
	public void putFromDb(ArchiveDatabase2 db) throws IllegalStateException {
		totalUserStat.clear();
		userstatByYear.clear();
		ListIterator<ArchiveItem> it = null;
		it = db.getIterator();
		int y = 0;
		ArchiveItem item = null;
		Map<String,Integer> userstat = new HashMap<String,Integer>(100);
		if(it.hasNext()) {
			item = it.next();
			y = item.year;
			if (!(this.filterBySize && item.size<this.minSize)) {
				this.totalUserStat.put(item.user, 1);
				userstat.put(item.user, 1);
			}
		}
		while(it.hasNext()) {
			item = it.next();
			if(item.year!=y) {
				this.userstatByYear.put(y, userstat);
				y = item.year;
				userstat = new HashMap<String,Integer>(100);				
			}
			if (this.filterBySize && item.size<this.minSize)
				continue;
			Integer n = totalUserStat.get(item.user);
			if (n==null) totalUserStat.put(item.user, 1);
			else 		 totalUserStat.put(item.user, n+1);	
			
			n = userstat.get(item.user);
			if (n==null) userstat.put(item.user, 1);
			else 		 userstat.put(item.user, n+1);	
		}
		this.userstatByYear.put(y, userstat);
	}
	
	/**
     *  Задача здесь подменить два набора items + userstatByYear
     *  одним набором items, в котором лежат StatItemRT вместо StatItem
     *  StatItemRT - содержат статистику по годам
	 */
	protected void additionalProcessing() {
		for(int i = 0;i<this.items.size();i++) {
			StatItem item = items.get(i);
			StatItemRT newitem = new StatItemRT(item);
			for(int year = startYear;year<=endYear;year++) {
				Map<String,Integer> userstat = this.userstatByYear.get(year); 
				Integer n = 0;
				if(userstat!=null ) n = userstat.get(item.user);
				if(n==null) n = 0;
				newitem.userArticlesByYear.put(year, n);
			}
			items.set(i, newitem);
		}
	}
	
	protected void merge(int srcIndex, int destIndex) {
		StatItemRT src = (StatItemRT)items.get(srcIndex);
		StatItemRT dest = (StatItemRT)items.get(destIndex);
		dest.userArticles += src.userArticles;		
		for(int y = this.startYear;y<=endYear;y++) {
			int a = dest.userArticlesByYear.get(y);
			int b = src.userArticlesByYear.get(y);
			dest.userArticlesByYear.put(y, a+b);
		}
	}
	
	protected void getSettingsFromIni(Map<String, String> options) throws BadAttributeValueExpException {
		super.getSettingsFromIni(options);
        headerItemTemplate = options.get("годы");
        userItemTemplate = options.get("статьи участника по годам");
		if(userItemTemplate==null||headerItemTemplate==null) {
			log.error("incorrect settings for statistics");
			throw new BadAttributeValueExpException(this.type);
		}
	}
	public String toString() {
		StringBuffer sb = new StringBuffer();		
		for(int year=startYear;year<=endYear;year++)
            sb.append(this.headerItemTemplate.replace("%(год)", String.valueOf(year)));
		header = header.replace("%(годы)", sb.toString());
		return super.toString();
	}
}
