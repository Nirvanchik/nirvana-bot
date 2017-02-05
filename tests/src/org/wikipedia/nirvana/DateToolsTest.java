/**
 *  @(#)DateToolsTest.java 03.02.2017
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
 * Recommended code page for this file is CP1251 (also called Windows-1251).
 * */

package org.wikipedia.nirvana;

import org.wikipedia.nirvana.localization.TestLocalizationManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import junit.framework.Assert;

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
}
