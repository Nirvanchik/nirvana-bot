/**
 *  @(#)DateTools.java
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

package org.wikipedia.nirvana.util;

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
import java.util.TimeZone;

import javax.annotation.Nullable;

/**
 * Utilities for printing/converting dates (localized with {@link
 * org.wikipedia.nirvana.localization.Localizer Localizer}) and standard {@link java.util.Locale}.
 * Also provides localized date strings (months, seasons, etc.).
 * 
 * Uses Gregorian calendar convention when calculating week number in year.
 * Uses European convention of what is the first day of week (Monday).
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

    public static String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS zzz";
    private static final TimeZone utc = TimeZone.getTimeZone("UTC");
    private static final SimpleDateFormat isoFormatter = new SimpleDateFormat(ISO_FORMAT);

    static {
        sLog = LogManager.getLogger(DateTools.class.getName());
        isoFormatter.setTimeZone(utc);
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

    /**
     * Public constructor.
     *
     * @param language Language of localization. This language will be used with  {@link
     *     org.wikipedia.nirvana.localization.Localizer Localizer} to localize month and season
     *     names and with java system Locale class.
     */
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

    /**
     * Initialize method.
     * Must be called once before the first usage if you want to use this class as a Singletone.
     * Call this method before the first getInstance() call.
     *
     * @param language language to localize strings.
     */
    public static void init(String language) {
        if (sInstance != null) {
            throw new IllegalArgumentException("DateTools is already initialized");
        }

        sInstance = new DateTools(language);
    }

    @VisibleForTesting
    static void resetFromTests() {
        sInstance = null;
    }

    /**
     * Returns DateTools singletone instance.
     * Before using it you must initialize it with {@link #init(String)} method. 
     */
    public static DateTools getInstance() {
        if (sInstance == null)  {
            throw new IllegalStateException("Looks like DateTools is not initialized yet.");
        }

        return sInstance;
    }

    /**
     * Returns one of 4 season names (winter, spring, summer, autumn) according to specified index.
     * Season names are localized to languages with {@link
     * org.wikipedia.nirvana.localization.Localizer Localizer}. Only Russian seasons are hardcoded,
     * other available only if you provide localization using {@link
     * org.wikipedia.nirvana.localization.LocalizationManager LocalizationManager}.
     * 
     * @param number number of the required season.
     * @return a string with the season title.
     */
    public String seasonString(int number) {
        return seasons[number];
    }

    /**
     * Returns one of 4 month names (January, February, etc.) according to specified index.
     * Month names are localized to languages with {@link
     * org.wikipedia.nirvana.localization.Localizer Localizer}. Only Russian months are hardcoded,
     * other available only if you provide localization using {@link
     * org.wikipedia.nirvana.localization.LocalizationManager LocalizationManager}.
     * 
     * @param number number of the required month.
     * @return month string.
     */
    public String monthString(int number) {
        return months[number];
    }

    /**
     * The same as {@link #monthString(int)} but the returned month title has a genitive case. 
     * 
     * @param number number of the required month.
     * @return month string.
     */
    public String monthMonatString(int number) {
        return monat[number];
    }

    /**
     * Returns the first day of week with which this class is configured (Calendar integer id,
     * Monday for Europe, Sunday for US, etc.)
     *
     * @see {@link Calendar#getFirstDayOfWeek()}. 
     */
    public int getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    /**
     * Returns minimal days count in the first week with which this class is configured (4 days
     * usually).
     *
     * @see {@link Calendar#getMinimalDaysInFirstWeek()}. 
     */
    public int getMinimalDaysInFirstWeek() {
        return minimalDaysInFirstWeek;
    }

    /**
     * Print (format) date to string in genitive format (popular format for date strings in Russia
     * and other Slavic-language countries).
     * For example: "1 января 2001".
     *
     * @param datetime date to print.
     * @return string with a date formatted in genitive case.
     */
    public String printDateDayMonthYearGenitive(Calendar datetime) {
        DateFormatSymbols symbols = new DateFormatSymbols(locale);
        symbols.setMonths(monat);
        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", symbols);
        Date date = datetime.getTime();
        sdf.setTimeZone(datetime.getTimeZone());
        return sdf.format(date);
    }

    /**
     * Print date/time to timestamp string in UTC timezone.
     *
     * @param datetime Date instance.
     * @return timestamp (UTC timezone).
     */
    public static String printTimestamp(Date datetime) {
        return isoFormatter.format(datetime).toString();
    }

    /**
     * Parse date from string to computer format (Calendar).
     * The string should have date in the next (weird) format: "yyyy-mm-dd".
     * Example: "2001-05-20T". Will be parse to a date of 20 June 2001.
     * Warning: Calendar will use current time zone of the system.
     *
     * @param dateStr date string.
     * @return Calendar instance with a parsed date or null (if not parsed).
     */
    @Nullable
    public static Calendar parseSimpleDate(String dateStr) {
        Calendar c = null;
        DateFormat formatter1; 
        Date date;
        // TODO: WTF is this T? This is simple date, not a timestamp!
        formatter1 = new SimpleDateFormat("yyyy-MM-dd'T'");
        try {
            date = (Date)formatter1.parse(dateStr);
            c = Calendar.getInstance();
            c.setTime(date);            
        } catch (ParseException e) {
            // Ignore.
        }
        return c;
    }

    // TODO: Are we sure we need this strange Frankenstein method? Refactor it. Rename it.
    /**
     * Parse date string from 3 known by this bot possible date string formats.
     * Use it on your own risk or better don't use it at all.
     * Was developed specially to parse dates or timestamps written by humans in some configs.
     *
     * @param dateStr string with date to parse.
     * @return Calendar instance or null if failed to parse.
     */
    @Nullable
    public static Calendar parseTimeStampOrSimpleDate(String dateStr) {
        Calendar c = parseDate(dateStr);
        if (c == null) {
            c = parseSimpleDate(dateStr);
        }
        return c;
    }

    // TODO: Make it clean about those strange timestamp formats and friendly with time zones.
    /***
     * This is bad, bad, very bad function to parse date from string.
     * 2 date formats are allowed:
     * 1) timestamp format ("yyyy-MM-dd'T'HH:mm:ss'Z'")
     * 2) some strange timestamp format I don't know where it is from ("EEE MMM d HH:mm:ss z yyyy")
     * The first format ignores timezone.
     * The second format reads timezone, but converts it to current timezone.
     *
     * @param dateStr string with date to parse.
     * @return Calendar instance or null if failed to parse.
     */
    @Deprecated
    @Nullable
    public static Calendar parseDate(String dateStr) {        
        Calendar c = null;
        DateFormat formatter1;
        DateFormat formatter2; 
        Date date;
        // TODO: This is BAD BAD BAD!
        // The first format reads timestamp without timezone info.
        // So, it will think that parsed date is in current timezone.
        // If the source time zone is the same as local time zone of the bot, it's fine.
        // If it's not (for example, it can be in UTC), the parsed
        // date will have wrong hour value (-1 hour for Germany, -3 hours for Minsk/Moscow, etc.)
        // The second format if fine but you must know that it still be converted to
        // local timezone of the place where bot runs.
        formatter1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter2 = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.ENGLISH);
        try {
            date = (Date)formatter1.parse(dateStr);
            c = Calendar.getInstance();
            c.setTime(date);            
        } catch (ParseException e) {            
            try {                
                date = (Date)formatter2.parse(dateStr);
                System.out.println("" + date.toString());
                c = Calendar.getInstance();
                c.setTime(date);
            } catch (ParseException e1) {
                // Ignore
            }
        }
        return c;
    }

    /**
     * Parse timestamp string in Mediawiki standard format: ("yyyy-MM-dd'T'HH:mm:ss'Z'").
     * It is expected that the time string has a time in UTC zone.
     * Returned Calendar instance will have UTC zone.
     *
     * @param timeString time string.
     * @return Calendar time in UTC zone.
     */
    @Nullable
    public static Calendar parseWikiTimestampUTC(String timeString) {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(utc);
        try {
            Calendar c = Calendar.getInstance();
            c.setTimeZone(utc);
            Date date = (Date) formatter.parse(timeString);
            c.setTime(date);
            return c;
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Parse date string which is written in genitive case (popular format for date strings in
     * Russia and other Slavic-language countries).
     * Example date string: "1 января 2001".
     *
     * @param dateStr date string.
     * @return Calendar instance with the date or null if failed to parse.
     */
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
            // Ignore
        }
        return c;
    }

    /**
     * Get week number of the specified calendar day.
     *
     * @param year a year of the date.
     * @param month month number of the calendar date (starting from 0).
     * @param day a day in a month of the date.
     * @return week number of this calendar day (starting from 1).
     */
    public int dayToWeek(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.setFirstDayOfWeek(firstDayOfWeek);
        c.setMinimalDaysInFirstWeek(minimalDaysInFirstWeek);
        c.set(year, month, day);
        int week = c.get(Calendar.WEEK_OF_YEAR);
        // TODO: this is wrong. Year has 52 weeks, not 50.
        if (month == 0 && day <= 7 && week > 50) {
            week = 0;
        }
        return week;
    }

    /**
     * Get month number of the specified week in the specified year.
     *
     * @param year a year.
     * @param week a week number (starting from 1).
     * @return month number of this week (starting from 0).
     */
    public int weekToMonth(int year, int week) {
        if (week <= 1) return 0;
        Calendar c = Calendar.getInstance();
        c.clear();
        c.setFirstDayOfWeek(firstDayOfWeek);
        c.setMinimalDaysInFirstWeek(minimalDaysInFirstWeek);
        // TODO: Fix it. 7 is wrong value. It's SATURDAY. Use string constant, not number.
        // TODO: SUNDAY is the last day of week in regions where MONDAY is the first one.
        //       But not in any world region.
        //       Use 7 - (FIRST_DAY_OF_WEEK_DEFAULT - 1).
        c.setWeekDate(year, week, 7); // sunday
        int month = c.get(Calendar.MONTH);
        int md = c.get(Calendar.DAY_OF_MONTH);
        // TODO: Don't use magic number, use minimalDaysInFirstWeek
        if (md < 4) month--;
        // TODO: This is a check for left margin. Add check for right margin too.
        if (month < 0) month = 11; // bug fix (ArrayIndexOutOfBoundsException)
        return month;
    }
}
