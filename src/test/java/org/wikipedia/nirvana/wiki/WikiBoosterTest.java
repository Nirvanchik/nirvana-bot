/**
 *  @(#)WikiBoosterTest.java
 *  Copyright © 2019 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.wiki;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;

import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.localization.TestLocalizationManager;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

/**
 * Unit-tests for {@link WikiBooster}.
 *
 */
public class WikiBoosterTest {
    private MockNirvanaWiki mockWiki;
    private NirvanaWiki wiki;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TestLocalizationManager.init(Localizer.NO_LOCALIZATION);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        TestLocalizationManager.reset();
    }

    @Before
    public void setUp() throws Exception {
        mockWiki = new MockNirvanaWiki("en.wikipedia.org");
        mockWiki.mockPageText("page1", "text1");
        mockWiki.mockPageTemplates("page1", Arrays.asList("TA", "P1T1", "P1T2"));
        
        mockWiki.mockPageText("page2", "text2");
        mockWiki.mockPageTemplates("page2", Arrays.asList("TA", "P2T1", "P2T2"));
        
        mockWiki.mockPageText("page3", "text3");
        mockWiki.mockPageTemplates("page3", Arrays.asList("TB", "P3T1", "P3T2"));
        
        mockWiki.mockPageText("page4", "text4");
        mockWiki.mockPageTemplates("page4", Arrays.asList("TB", "P4T1", "P4T2"));

        mockWiki.mockPageText("page5", "text5");
        mockWiki.mockPageTemplates("page5", Arrays.asList("TB", "P5T1", "P5T2"));
        
        wiki = Mockito.spy(mockWiki);
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link
     * org.wikipedia.nirvana.wiki.WikiBooster#getTemplates(java.lang.String, int)}.
     */
    @Test
    public void testGetTemplates() throws Exception {
        String [] pages = {"page1", "page2", "page3"};
        String [] templates = {"TA", "P1T1"};
        WikiBooster booster = new WikiBooster(wiki, Arrays.asList(pages), Arrays.asList(templates));
        
        Assert.assertEquals(booster.getTemplates("page1", 1), Arrays.asList("TA", "P1T1", "P1T2"));
        
        String [] page1 = {"page1"};
        Mockito.verify(wiki, times(0)).getPagesTemplates(aryEq(page1), anyInt());
        Mockito.verify(wiki, times(0)).getTemplates(eq("page1"), anyInt());
        Mockito.verify(wiki).getPagesTemplates(aryEq(pages), anyInt());
        
        Assert.assertEquals(booster.getTemplates("page1", 1), Arrays.asList("TA", "P1T1", "P1T2"));
        Assert.assertEquals(booster.getTemplates("page2", 1), Arrays.asList("TA", "P2T1", "P2T2"));
        Assert.assertEquals(booster.getTemplates("page3", 1), Arrays.asList("TB", "P3T1", "P3T2"));
        
        Mockito.verifyNoMoreInteractions(wiki);        
    }
    
    @Test(expected = IllegalStateException.class)
    public void testGetTemplates_namespaceMustNotChange() throws Exception {
        String [] pages = {"page1", "page2", "page3"};
        String [] templates = {"TA", "P1T1"};
        WikiBooster booster = new WikiBooster(wiki, Arrays.asList(pages), Arrays.asList(templates));
        
        Assert.assertEquals(booster.getTemplates("page1", 1), Arrays.asList("TA", "P1T1", "P1T2"));
        
        booster.getTemplates("page2", 2);        
    }
    
    @Test (expected = IllegalStateException.class)
    public void testGetTemplate_badlyPrepared1() throws Exception {
        String [] pages = {"page1", "page2", "page3"};
        String [] templates = {"TA", "P1T1"};
        WikiBooster booster = new WikiBooster(wiki, Arrays.asList(pages), Arrays.asList(templates));
        booster.getTemplates("page5", 1);
    }

    /**
     * Test method for {@link org.wikipedia.nirvana.wiki.WikiBooster#getPageText(java.lang.String)}.
     */
    @Test
    public void testGetPageText() throws Exception {
        String [] pages = {"page1", "page2", "page3"};
        WikiBooster booster = new WikiBooster(wiki, Arrays.asList(pages));
        
        Assert.assertEquals("text1", booster.getPageText("page1"));
        
        Mockito.verify(wiki, times(0)).getPageText(anyString());
        Mockito.verify(wiki).getPageText(aryEq(pages));

        Assert.assertEquals("text1", booster.getPageText("page1"));
        Assert.assertEquals("text2", booster.getPageText("page2"));
        Assert.assertEquals("text3", booster.getPageText("page3"));
        
        Mockito.verifyNoMoreInteractions(wiki);
    }
    
    @Test (expected = IllegalStateException.class)
    public void testGetPageText_badlyPrepared() throws Exception {
        String [] pages = {"page1", "page2", "page3"};
        WikiBooster booster = new WikiBooster(wiki, Arrays.asList(pages));
        booster.getPageText("page5");
    }

    @Test
    public void testRemovePage() throws Exception {
        String [] pages = {"page1", "page2", "page3"};
        WikiBooster booster = new WikiBooster(wiki, Arrays.asList(pages));
        Assume.assumeThat(booster.getPageText("page1"), CoreMatchers.is("text1"));

        booster.removePage("page1");
        
        boolean raised = false;
        try {
            booster.getPageText("page1");
        } catch (IllegalStateException e) {
            raised = true;
        }
        Assert.assertTrue("Exception not raised!", raised);
        Assert.assertEquals("text2", booster.getPageText("page2"));
    }

    /**
     * Test method for {@link org.wikipedia.nirvana.wiki.WikiBooster#hasTemplate(java.lang.String,
     * java.lang.String)}.
     */
    @Test
    public void testHasTemplate() throws Exception {
        String [] pages = {"page1", "page2", "page3"};
        String [] templates = {"TA", "P1T1"};
        WikiBooster booster = new WikiBooster(wiki, Arrays.asList(pages), Arrays.asList(templates));
        
        Assert.assertTrue(booster.hasTemplate("page1", "TA"));
        
        Mockito.verify(wiki, times(0)).hasTemplate(any(), anyString());
        Mockito.verify(wiki, times(0)).pageHasTemplate(any(), anyString());
        Mockito.verify(wiki).hasTemplates(aryEq(pages), aryEq(templates));
        
        Assert.assertTrue(booster.hasTemplate("page1", "TA"));
        Assert.assertTrue(booster.hasTemplate("page1", "P1T1"));
        Assert.assertTrue(booster.hasTemplate("page2", "TA"));
        Assert.assertFalse(booster.hasTemplate("page2", "P1T1"));
        Assert.assertFalse(booster.hasTemplate("page3", "TA"));
        Assert.assertFalse(booster.hasTemplate("page3", "P1T1"));
        
        Mockito.verifyNoMoreInteractions(wiki);
    }
    
    @Test (expected = IllegalStateException.class)
    public void testHasTemplate_badlyPrepared1() throws Exception {
        String [] pages = {"page1", "page2", "page3"};
        String [] templates = {"TA", "P1T1"};
        WikiBooster booster = new WikiBooster(wiki, Arrays.asList(pages), Arrays.asList(templates));
        booster.hasTemplate("page5", "TA");
    }

    @Test (expected = IllegalStateException.class)
    public void testHasTemplate_badlyPrepared2() throws Exception {
        String [] pages = {"page1", "page2", "page3"};
        String [] templates = {"TA", "P1T1"};
        WikiBooster booster = new WikiBooster(wiki, Arrays.asList(pages), Arrays.asList(templates));
        booster.hasTemplate("page1", "TTT");
    }
    
    @Test (expected = IllegalStateException.class)
    public void testHasTemplate_badlyPrepared3() throws Exception {
        String [] pages = {"page1", "page2", "page3"};
        WikiBooster booster = new WikiBooster(wiki, Arrays.asList(pages));
        booster.hasTemplate("page1", "T");
    }

    @Test
    public void testRevListConstructor() throws Exception {
        ArrayList<Revision> revs = new ArrayList<>();
        revs.add(wiki.new Revision(5, Calendar.getInstance(Locale.ENGLISH), "page1",
                "Create page1", "user 1", false, false, true, 1000));
        revs.add(wiki.new Revision(5, Calendar.getInstance(Locale.ENGLISH), "page2",
                "Create page2", "user 2", false, false, true, 1000));
        WikiBooster booster = WikiBooster.create(wiki, revs, null);
        booster.getPageText("page1");
        booster.getPageText("page2");
    }
}
