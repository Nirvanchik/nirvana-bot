/**
 *  @(#)DateTools.java 14.12.2014
 *  Copyright © 2011 - 2014 Dmitry Trofimovich (KIN)
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

package org.wikipedia.nirvana;

import org.wikipedia.nirvana.annotation.VisibleForTesting;
import org.wikipedia.nirvana.localization.Localizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utilities for printing/converting dates (localized with {@link
 * org.wikipedia.nirvana.localization.Localizer Localizer}).
 * Also provides localized date strings (months, seasons, etc.).
 */
public class DateTools {
    private static final String DEFAULT_LANGUAGE = "ru";

    /**
     * Настройки недели по ГОСТ ИСО 8601-2001 п. 2.17 неделя календарная.
     * Период времени, состоящий из семи дней внутри календарного года.
     * Неделя начинается с понедельника и идентифицируется своим порядковым номером в году.
     * Первой календарной неделей года считают первую неделю, содержащую первый четверг текущего
     * года. В григорианском календаре - это неделя, содержащая 4 января.
     */
	public static final int MINIMAL_DAYS_IN_FIRST_WEEK_DEFAULT = 4;	
    public static final int FIRST_DAY_OF_WEEK_DEFAULT = Calendar.MONDAY; 

    public static final Logger sLog;

    private static DateTools sInstance;

    private final Localizer localizer;
    private final int minimalDaysInFirstWeek = MINIMAL_DAYS_IN_FIRST_WEEK_DEFAULT;
    private final int firstDayOfWeek = FIRST_DAY_OF_WEEK_DEFAULT;
    private final Locale locale;
    private final String[] monat;
    private final String[] months;
    private final String[] seasons;

    static {
        sLog = LogManager.getLogger(DateTools.class.getName());
    }

    private static final String[] MONAT_RU = {
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

    public static final String[] MONTHS_RU = {
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

    public static final String[] SEASONS_RU = {
           "зима",
           "весна",
           "лето",
           "осень",
    };

    public DateTools(String language) {
        locale = new Locale(language);
        localizer = Localizer.getInstance();
        if (DEFAULT_LANGUAGE.equals(language)) {
            monat = MONAT_RU;
            months = MONTHS_RU;
            seasons = SEASONS_RU;
            return;
        }
        monat = localizeArray(MONAT_RU);
        months = localizeArray(MONTHS_RU);
        seasons = localizeArray(SEASONS_RU);
    }

    private String[] localizeArray(String[] array) {
        String[] localized = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            localized[i] = localizer.localize(array[i]);
        }
        return localized;
    }

    public static void init(String language) {
        assert sInstance == null;

        sInstance = new DateTools(language);
    }

    @VisibleForTesting
    static void resetFromTests() {
        sInstance = null;
    }

    public static DateTools getInstance() {
        assert sInstance != null : "Looks like DateTools is not initialized yet.";

        return sInstance;
    }

    public String seasonString(int number) {
        return seasons[number];
    }

    public String monthString(int number) {
        return months[number];
    }

    public String monthMonatString(int number) {
        return monat[number];
    }

    public int getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    public int getMinimalDaysInFirstWeek() {
        return minimalDaysInFirstWeek;
    }

    public String printDateDayMonthYearGenitive(Calendar c) {
        DateFormatSymbols symbols = new DateFormatSymbols(locale);
        symbols.setMonths(monat);
        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", symbols);
		Date date = c.getTime();
		return sdf.format(date);
	}
	
	public static Calendar parseSimpleDate(String dateStr) {
		Calendar c = null;
		DateFormat formatter1; 
		Date date;		 
		formatter1 = new SimpleDateFormat("yyyy-MM-dd'T'");
		try {
			date = (Date)formatter1.parse(dateStr);
			c = Calendar.getInstance();
    		c.setTime(date);		    
		} catch (ParseException e) {
			
		}
		return c;
	}
	
	public static Calendar parseTimeStampOrSimpleDate(String dateStr) {
		Calendar c = parseDate(dateStr);
		if (c == null) {
			c = parseSimpleDate(dateStr);
		}
		return c;
	}
	
	public static Calendar parseDate(String dateStr) {		
		Calendar c = null;
		DateFormat formatter1,formatter2; 
		Date date;		 
		formatter1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		formatter2 = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.ENGLISH);
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
	
    public Calendar parseDateStringDayMonthYearGenitive(String dateStr) {
		Calendar c = null;
		DateFormatSymbols russSymbol = new DateFormatSymbols(locale);
        russSymbol.setMonths(monat);
		DateFormat formatter = new SimpleDateFormat("d MMMM yyyy", russSymbol);
		Date date;
		try {
			date = (Date)formatter.parse(dateStr);
			c = Calendar.getInstance();
    		c.setTime(date);		    
		} catch (ParseException e) {		
		}
		return c;
	}

    public int dayToWeek(int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, day);
        c.setFirstDayOfWeek(firstDayOfWeek);
        c.setMinimalDaysInFirstWeek(minimalDaysInFirstWeek);
		int week = c.get(Calendar.WEEK_OF_YEAR);
		if(month==0 && day<=7 && week>50) {
			week = 0;
		}
		return week;
	}

    public int weekToMonth(int year, int week) {
		if(week<=1) return 0;
		Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(firstDayOfWeek);
        c.setMinimalDaysInFirstWeek(minimalDaysInFirstWeek);
		c.setWeekDate(year, week, 7); //sunday
		int month = c.get(Calendar.MONTH);
		int md = c.get(Calendar.DAY_OF_MONTH);
		if(md<4) month--;
		if(month<0) month = 11; // bug fix (ArrayIndexOutOfBoundsException)
			//System.out.println("month < 0 week:"+week+" md = "+md+" year="+year);
		return month;
	}
}
