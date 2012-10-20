/**
 *  @(#)DateTools.java 02/07/2012
 *  Copyright © 2011 - 2012 Dmitry Trofimovich (KIN)
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

package org.wikipedia.nirvana;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author kin
 *
 */
public class DateTools {
	/** Настройки недели по ГОСТ ИСО 8601-2001 п. 2.17 неделя календарная.
	 * Период времени, состоящий из семи дней внутри календарного года.
	 * Неделя начинается с понедельника и идентифицируется своим порядковым номером в году.
	 * Первой календарной неделей года считают первую неделю, содержащую первый четверг текущего года.
	 * В григорианском календаре - это неделя, содержащая 4 января.
	 */
	public static final int MINIMAL_DAYS_IN_FIRST_WEEK_DEFAULT = 4;	
	public static int FIRST_DAY_OF_WEEK_DEFAULT = Calendar.MONDAY; 
	
	public static int MINIMAL_DAYS_IN_FIRST_WEEK = MINIMAL_DAYS_IN_FIRST_WEEK_DEFAULT;
	public static int FIRST_DAY_OF_WEEK = FIRST_DAY_OF_WEEK_DEFAULT;
	
	public static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DateTools.class.getName());;
	
	/**
	 * 
	 */
	
	public static final String[] russianMonat = 
		{
		   "января", 
		   "февраля", 
		   "марта", 
		   "апреля", 
		   "мая", 
		   "июня", 
		   "июля", 
		   "августа", 
		   "сентября", 
		   "октября", 
		   "ноября", 
		   "декабря"
		};
	
	public static final String[] russianMonths = 
		{
		   "январь", 
		   "февраль", 
		   "март", 
		   "апрель", 
		   "май", 
		   "июнь", 
		   "июль", 
		   "август", 
		   "сентябрь", 
		   "октябрь", 
		   "ноябрь", 
		   "декабрь"
		};
	
	public static final String[] russianSeasons = 
		{
		   "зима", 
		   "весна", 
		   "лето", 
		   "осень", 
		};
	

	public DateTools() {
		
	}

	public static String printDateDayMonthYearGenitiveRussian(Calendar c) {
		
		DateFormatSymbols russSymbol = new DateFormatSymbols(Locale.getDefault());
		russSymbol.setMonths(russianMonat);
		SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", russSymbol);
		Date date = c.getTime();
		return sdf.format(date);
	}
	
	public static Calendar parseDate(String dateStr) {		
		Calendar c = null;
		DateFormat formatter1,formatter2; 
		Date date;		 
		formatter1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		formatter2 = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy",Locale.ENGLISH);
		try {
			date = (Date)formatter1.parse(dateStr);
			c = Calendar.getInstance();
    		c.setTime(date);		    
		} catch (ParseException e) {			
			try {				
				date = (Date)formatter2.parse(dateStr);
				c = Calendar.getInstance();
	    		c.setTime(date);
			}catch (ParseException e1) {
			}
		}
		return c;		 
	}
	public static int dayToWeek(int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, day);
		c.setFirstDayOfWeek(DateTools.FIRST_DAY_OF_WEEK);
		c.setMinimalDaysInFirstWeek(DateTools.MINIMAL_DAYS_IN_FIRST_WEEK);		
		int week = c.get(Calendar.WEEK_OF_YEAR);
		if(month==0 && day<=7 && week>50) {
			week = 0;
		}
		return week;
	}
	
	public static int weekToMonth(int year, int week) {
		if(week<=1) return 0;
		Calendar c = Calendar.getInstance();
		c.setFirstDayOfWeek(DateTools.FIRST_DAY_OF_WEEK);
		c.setMinimalDaysInFirstWeek(DateTools.MINIMAL_DAYS_IN_FIRST_WEEK);
		c.setWeekDate(year, week, 7); //sunday
		int month = c.get(Calendar.MONTH);
		int md = c.get(Calendar.DAY_OF_MONTH);
		if(md<4) month--;
		if(month<0) month = 11; // bug fix (ArrayIndexOutOfBoundsException)
			//System.out.println("month < 0 week:"+week+" md = "+md+" year="+year);
		return month;
	}
}
