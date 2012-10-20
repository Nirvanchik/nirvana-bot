/**
 *  @(#)StatisticsFabric.java 20/10/2012
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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;

/**
 * @author kin
 *
 */
@SuppressWarnings("rawtypes")
public class StatisticsFabric {
	private static Map<String,Class> reporterClass;
	private static Map<String,Statistics> reporters;
	private static MultiKeyMap reportersYear;
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(StatisticsFabric.class.getName());
	
	/**
	 * 
	 */
	public StatisticsFabric() {
		
	}
	
	/*public void register(String type, Class<Statistics> cl) {
		
	}*/
	
	static {
		reporterClass = new HashMap<String,Class>(5);
		reporterClass.put("по неделям", 	StatisticsWeek.class);
		reporterClass.put("по месяцам", 	StatisticsMonth.class);
		reporterClass.put("по годам", 		StatisticsYear.class);
		reporterClass.put("рейтинг за год", Rating.class);
		reporterClass.put("общий рейтинг", 	RatingTotal.class);
		reporters = new HashMap<String,Statistics>(5);
		reportersYear = new MultiKeyMap();
	}
	
	@SuppressWarnings("unchecked")
	static Statistics createReporter(String type) {
		Statistics ob = null;
		ob = reporters.get(type);
		if(ob==null) {
			Class cl = reporterClass.get(type);
			if(cl==null) return null;			
			try {
				Class params[] = new Class[1];
				params[0] = String.class;
				ob = (Statistics) cl.getDeclaredConstructor(params).newInstance(type);
				reporters.put(type, ob);
			} catch (Exception e) {
				log.error(e);
			}
		} 
		// NULL return value will tell about error			
		return ob;
	}
	
	@SuppressWarnings("unchecked")
	static Statistics createReporter(String type, int year) {
		Statistics ob = null;
		MultiKey multiKey = new MultiKey(type, new Integer(year));
		ob = (Statistics) reportersYear.get(multiKey);
		if(ob==null) {
			Class cl = reporterClass.get(type);
			if(cl==null) return null;
			try {
				Class params[] = new Class[]{String.class,int.class};
				Constructor c = cl.getDeclaredConstructor(params); 
				ob = (Statistics) c.newInstance(type,year);
				reportersYear.put(multiKey,ob);
			} catch (Exception e) {
				log.error(e.toString());
				e.printStackTrace();
			}
		}
		// NULL return value will tell about error			
		return ob;
	}
	
	static Statistics getReporter(String type) {
		return reporters.get(type);
	}
	
	static Statistics getReporter(String type, int year) {
		Statistics ob = null;
		MultiKey multiKey = new MultiKey(type, new Integer(year));
		ob = (Statistics) reportersYear.get(multiKey);		
		return ob;
	}
	
	static Statistics getReporterWithUserData(Statistics caller) {
		Statistics ob = getReporter("рейтинг за год");
		if(ob==caller) ob = null;
		if(ob==null) {
			ob = getReporter("по месяцам");
			if(ob==caller) ob = null;
		}
		if(ob==null) {
			ob = getReporter("по неделям");
			if(ob==caller) ob = null;
		}
		return ob;
	}
	static Statistics getReporterWithUserData(Statistics caller, int year) {
		Statistics ob = getReporter("рейтинг за год",year);
		if(ob==caller) ob = null;
		if(ob==null) {
			ob = getReporter("по месяцам",year);
			if(ob==caller) ob = null;
		}
		if(ob==null) {
			ob = getReporter("по неделям",year);
			if(ob==caller) ob = null;
		}
		return ob;
	}
	
	static void purge() {
		reporters.clear();
		reportersYear.clear();
	}

}
