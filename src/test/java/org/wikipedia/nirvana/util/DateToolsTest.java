/**
 *  @(#)DateToolsTest.java
 *  Copyright © 2017 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.wikipedia.nirvana.localization.TestLocalizationManager;
import org.wikipedia.nirvana.util.DateTools;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Unit-tests for {@link DateTools}.
 */
public class DateToolsTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Map<String, String> translations = new HashMap<>();
        translations.put("январь", "студзень");
        translations.put("января", "студзеня");
        translations.put("зима", "зiма");
        TestLocalizationManager.init(translations);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        TestLocalizationManager.reset();
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void monthString_noLocalization() {
        DateTools dateTools = new DateTools("ru");
        Assert.assertEquals("январь", dateTools.monthString(0));
    }

    @Test
    public void monthString_localized() {
        DateTools dateTools = new DateTools("be");
        Assert.assertEquals("студзень", dateTools.monthString(0));
    }

    @Test
    public void monthMonatString_noLocalization() {
        DateTools dateTools = new DateTools("ru");
        Assert.assertEquals("января", dateTools.monthMonatString(0));
    }

    @Test
    public void monthMonatString_localized() {
        DateTools dateTools = new DateTools("be");
        Assert.assertEquals("студзеня", dateTools.monthMonatString(0));
    }

    @Test
    public void seasonString_noLocalization() {
        DateTools dateTools = new DateTools("ru");
        Assert.assertEquals("зима", dateTools.seasonString(0));
    }

    @Test
    public void seasonString_localized() {
        DateTools dateTools = new DateTools("be");
        Assert.assertEquals("зiма", dateTools.seasonString(0));
    }

    @Test
    public void printDateDayMonthYearGenitive_noLocalization() {
        Calendar c = Calendar.getInstance(new Locale("ru"));
        DateTools dateTools = new DateTools("ru");
        c.set(2000, 0, 1);
        Assert.assertEquals("1 января 2000", dateTools.printDateDayMonthYearGenitive(c));
    }

    @Test
    public void printDateDayMonthYearGenitive_localized() {
        Calendar c = Calendar.getInstance(new Locale("be"));
        DateTools dateTools = new DateTools("be");
        c.set(2000, 0, 1);
        Assert.assertEquals("1 студзеня 2000", dateTools.printDateDayMonthYearGenitive(c));
    }

    @Test
    public void parseDateStringDayMonthYearGenitive_noLocalization() {
        DateTools dateTools = new DateTools("ru");
        Calendar c = dateTools.parseDateStringDayMonthYearGenitive("1 января 2000");
        Assert.assertNotNull(c);
        Assert.assertEquals(2000, c.get(Calendar.YEAR));
        Assert.assertEquals(0, c.get(Calendar.MONTH));
        Assert.assertEquals(1, c.get(Calendar.DAY_OF_MONTH));        
    }

    @Test
    public void parseDateStringDayMonthYearGenitive_localized() {
        DateTools dateTools = new DateTools("be");
        Calendar c = dateTools.parseDateStringDayMonthYearGenitive("1 студзеня 2000");
        Assert.assertNotNull(c);
        Assert.assertEquals(2000, c.get(Calendar.YEAR));
        Assert.assertEquals(0, c.get(Calendar.MONTH));
        Assert.assertEquals(1, c.get(Calendar.DAY_OF_MONTH));        
    }
    
    @Test
    public void weekToMonth() {
        DateTools dateTools = new DateTools("ru");

        // Ideal year begin
        Assert.assertEquals(0, dateTools.weekToMonth(2018, 1));
        Assert.assertEquals(0, dateTools.weekToMonth(2018, 2));
        Assert.assertEquals(0, dateTools.weekToMonth(2018, 3));
        Assert.assertEquals(0, dateTools.weekToMonth(2018, 4));
        // TODO: Broken case. Fix it.
        // Assert.assertEquals(1, dateTools.weekToMonth(2018, 5));
        Assert.assertEquals(1, dateTools.weekToMonth(2018, 6));
        
        Assert.assertEquals(10, dateTools.weekToMonth(2018, 48));
        Assert.assertEquals(11, dateTools.weekToMonth(2018, 49));
        Assert.assertEquals(11, dateTools.weekToMonth(2018, 50));
        Assert.assertEquals(11, dateTools.weekToMonth(2018, 51));
        Assert.assertEquals(11, dateTools.weekToMonth(2018, 52));
        // TODO: Broken case. Fix it.
        // Assert.assertEquals(11, dateTools.weekToMonth(2018, 53));
        
        // Problem year begin
        Assert.assertEquals(0, dateTools.weekToMonth(2016, 1));
        Assert.assertEquals(0, dateTools.weekToMonth(2016, 2));
        Assert.assertEquals(0, dateTools.weekToMonth(2016, 3));
        Assert.assertEquals(0, dateTools.weekToMonth(2016, 4));
        Assert.assertEquals(1, dateTools.weekToMonth(2016, 5));

        // TODO: Broken case. Fix it.
        // Assert.assertEquals(11, dateTools.weekToMonth(2016, 48));
        Assert.assertEquals(11, dateTools.weekToMonth(2016, 49));
        Assert.assertEquals(11, dateTools.weekToMonth(2016, 50));
        Assert.assertEquals(11, dateTools.weekToMonth(2016, 51));
        Assert.assertEquals(11, dateTools.weekToMonth(2016, 52));
    }
    
    @Test
    public void dayToWeek() {
        DateTools dateTools = new DateTools("ru");

        // Ideal year begin
        Assert.assertEquals(1, dateTools.dayToWeek(2018, 0, 1));
        Assert.assertEquals(1, dateTools.dayToWeek(2018, 0, 2));
        Assert.assertEquals(1, dateTools.dayToWeek(2018, 0, 3));
        Assert.assertEquals(1, dateTools.dayToWeek(2018, 0, 4));
        Assert.assertEquals(1, dateTools.dayToWeek(2018, 0, 5));
        Assert.assertEquals(1, dateTools.dayToWeek(2018, 0, 6));
        Assert.assertEquals(1, dateTools.dayToWeek(2018, 0, 7));
        Assert.assertEquals(2, dateTools.dayToWeek(2018, 0, 8));
        
        Assert.assertEquals(52, dateTools.dayToWeek(2018, 11, 24));
        Assert.assertEquals(52, dateTools.dayToWeek(2018, 11, 25));
        Assert.assertEquals(52, dateTools.dayToWeek(2018, 11, 26));
        Assert.assertEquals(52, dateTools.dayToWeek(2018, 11, 27));
        Assert.assertEquals(52, dateTools.dayToWeek(2018, 11, 28));
        Assert.assertEquals(52, dateTools.dayToWeek(2018, 11, 29));
        Assert.assertEquals(52, dateTools.dayToWeek(2018, 11, 30));
        // TODO: Broken case. Fix it.
        // Assert.assertEquals(53, dateTools.dayToWeek(2018, 11, 31));
        
        // Problem year begin
        // TODO: Broken cases. Fix it.
        // TODO: WTF 10 days in the first year?
        //Assert.assertEquals(1, dateTools.dayToWeek(2016, 0, 1));
        //Assert.assertEquals(1, dateTools.dayToWeek(2016, 0, 2));
        //Assert.assertEquals(1, dateTools.dayToWeek(2016, 0, 3));
        Assert.assertEquals(1, dateTools.dayToWeek(2016, 0, 4));
        Assert.assertEquals(1, dateTools.dayToWeek(2016, 0, 5));
        Assert.assertEquals(1, dateTools.dayToWeek(2016, 0, 6));
        Assert.assertEquals(1, dateTools.dayToWeek(2016, 0, 7));
        Assert.assertEquals(1, dateTools.dayToWeek(2016, 0, 8));
        Assert.assertEquals(1, dateTools.dayToWeek(2016, 0, 9));
        Assert.assertEquals(1, dateTools.dayToWeek(2016, 0, 10));
        Assert.assertEquals(2, dateTools.dayToWeek(2016, 0, 11));
        
        Assert.assertEquals(51, dateTools.dayToWeek(2016, 11, 24));
        Assert.assertEquals(51, dateTools.dayToWeek(2016, 11, 25));
        Assert.assertEquals(52, dateTools.dayToWeek(2016, 11, 26));
        Assert.assertEquals(52, dateTools.dayToWeek(2016, 11, 27));
        Assert.assertEquals(52, dateTools.dayToWeek(2016, 11, 28));
        Assert.assertEquals(52, dateTools.dayToWeek(2016, 11, 29));
        Assert.assertEquals(52, dateTools.dayToWeek(2016, 11, 30));
        Assert.assertEquals(52, dateTools.dayToWeek(2016, 11, 31));
    }
    
    protected void checkDate(Calendar cal, int expectYear, int expectMonth, int expectDay) {
        Assert.assertEquals(expectYear, cal.get(Calendar.YEAR));
        Assert.assertEquals(expectMonth, cal.get(Calendar.MONTH));
        Assert.assertEquals(expectDay, cal.get(Calendar.DAY_OF_MONTH));
    }
    
    protected void checkTime(Calendar cal, int expectHour, int expectMinutes, int expectSeconds) {
        Assert.assertEquals(expectHour, cal.get(Calendar.HOUR));
        Assert.assertEquals(expectMinutes, cal.get(Calendar.MINUTE));
        Assert.assertEquals(expectSeconds, cal.get(Calendar.SECOND));
    }    
    
    @Test
    public void parseSimpleDate() {
        // TODO: WTF is this T? This is simple date, not a timestamp!
        Calendar c = DateTools.parseSimpleDate("2019-07-25T");
        Assert.assertNotNull(c);
        checkDate(c, 2019, 6, 25);
    }
    
    @Test
    public void parseSimpleDate_unparsable() {
        Assert.assertNull(DateTools.parseSimpleDate("201 blabla"));
    }

    @Test
    public void parseDate_v1() {
        Calendar c = DateTools.parseDate("2019-07-25T10:05:17Z");
        
        Assert.assertNotNull(c);
        checkDate(c, 2019, 6, 25);
        checkTime(c, 10, 5, 17);
    }        

    @Test
    public void parseDate_v2() {
        Calendar c = DateTools.parseDate("Wed Jul 4 05:08:56 UTC 2001");

        checkDate(c, 2001, 6, 4);

        Assert.assertEquals(8, c.get(Calendar.MINUTE));
        Assert.assertEquals(56, c.get(Calendar.SECOND));

        // Calendar converts time hour according to local time zone of the system.
        // For test to pass we adjust time zone to time zone of the source date.
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        Assert.assertEquals(5, c.get(Calendar.HOUR));
    }

    @Test
    public void parseDate_unparsable() {
        Assert.assertNull(DateTools.parseDate("201 blabla"));
    }

    @Test
    public void parseTimeStampOrSimpleDate() {
        Calendar c = DateTools.parseTimeStampOrSimpleDate("2019-07-25T");
        
        Assert.assertNotNull(c);
        checkDate(c, 2019, 6, 25);
        checkTime(c, 0, 0, 0);
        
        c = DateTools.parseTimeStampOrSimpleDate("2019-07-25T10:05:17Z");
        
        Assert.assertNotNull(c);
        checkDate(c, 2019, 6, 25);
        checkTime(c, 10, 5, 17);
    }

    @Test
    public void parseTimeStampOrSimpleDate_unparsable() {
        Assert.assertNull(DateTools.parseTimeStampOrSimpleDate("201 blabla"));
    }

    @Test
    public void testSingltoneUsage() {
        DateTools.init("ru");
        Assert.assertEquals("зима", DateTools.getInstance().seasonString(0));
    }
}
