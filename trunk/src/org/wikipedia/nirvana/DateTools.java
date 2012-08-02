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

	/**
	 * 
	 */
	
	public static final String[] russianMonat = 
		{
		   "€нвар€", 
		   "феврал€", 
		   "марта", 
		   "апрел€", 
		   "ма€", 
		   "июн€", 
		   "июл€", 
		   "августа", 
		   "сент€бр€", 
		   "окт€бр€", 
		   "но€бр€", 
		   "декабр€"
		};
	
	public static final String[] russianMonths = 
		{
		   "€нварь", 
		   "февраль", 
		   "март", 
		   "апрель", 
		   "май", 
		   "июнь", 
		   "июль", 
		   "август", 
		   "сент€брь", 
		   "окт€брь", 
		   "но€брь", 
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
}
