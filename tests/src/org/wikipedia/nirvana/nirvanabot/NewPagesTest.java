/**
 *  @(#)NewPagesTest.java 12.03.2017
 *  Copyright � 2017 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.nirvanabot;

import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.localization.TestLocalizationManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Unit tests for {@link NewPages}.
 */
public class NewPagesTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        TestLocalizationManager.init(Localizer.DEFAULT_LOCALIZATION);
    }

    @After
    public void tearDown() throws Exception {
        TestLocalizationManager.reset();
        NewPages.resetFromTests();
    }

    @Test
    public void markDeleted_marksAnonimous() {
        NewPages.initStatics();
        String item = "* {{����� ������|���������|05 ���� 2008|195.39.211.231}}";
        String expected = "* {{����� ������|���������|05 ���� 2008|195.39.211.231|��}}";
        String result = NewPages.markDeleted(item);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void markDeleted_marksNormal() {
        NewPages.initStatics();
        String item = "* {{����� ������|��������|29 ������� 2008|Alekseeva Anna}}";
        String expected = "* {{����� ������|��������|29 ������� 2008|Alekseeva Anna|��}}";
        String result = NewPages.markDeleted(item);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void markDeleted_marksNormalWithPipe() {
        NewPages.initStatics();
        String item = "* {{����� ������|��������|29 ������� 2008|Alekseeva Anna|}}";
        String expected = "* {{����� ������|��������|29 ������� 2008|Alekseeva Anna|��}}";
        String result = NewPages.markDeleted(item);
        Assert.assertEquals(expected, result);
    }
    
    @Test
    public void markDeleted_marksSimple() {
        NewPages.initStatics();
        String item = "* [[��������]]";
        String expected = 
                "* [[��������]]<span style=\"color:#966963\"> � '''������ �������'''</span>";
        String result = NewPages.markDeleted(item);
        Assert.assertEquals(expected, result);
    }
    
    @Test
    public void unmarkDeleted_unmarksNormal() {
        NewPages.initStatics();
        String item = "* {{����� ������|��������|29 ������� 2008|Alekseeva Anna|��}}";
        String expected = "* {{����� ������|��������|29 ������� 2008|Alekseeva Anna}}";
        String result = NewPages.unmarkDeleted(item);
        Assert.assertEquals(expected, result);
    }
    
    @Test
    public void unmarkDeleted_unmarksSimple() {
        NewPages.initStatics();
        String item = "* [[��������]]<span style=\"color:#966963\"> � '''������ �������'''</span>";
        String expected = "* [[��������]]";
        String result = NewPages.unmarkDeleted(item);
        Assert.assertEquals(expected, result);
    }
}
