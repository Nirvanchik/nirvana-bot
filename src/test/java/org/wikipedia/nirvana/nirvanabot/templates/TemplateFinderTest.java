/**
 *  @(#)TemplateFinderTest.java
 *  Copyright © 2024 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.nirvanabot.templates;

import static org.mockito.Mockito.when;

import org.wikipedia.Wiki;
import org.wikipedia.nirvana.wiki.NirvanaWiki;
import org.wikipedia.nirvana.wiki.WikiBooster;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

/**
 * Unit tests for {@link TemplateFinder}.
 *
 */
public class TemplateFinderTest {
    @Mock
    NirvanaWiki wiki;

    @Mock
    WikiBooster booster;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(wiki.namespaceIdentifier(Wiki.TEMPLATE_NAMESPACE)).thenReturn("Template");
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link 
     * org.wikipedia.nirvana.nirvanabot.templates.TemplateFinder#find(java.lang.String)}.
     */
    @Test
    public void testFind_noTemplate() throws Exception {
        TemplateFindItem findItem = new TemplateFindItem("Money", "", "");
        TemplateFinder finder = new TemplateFinder(
                Collections.singletonList(findItem), wiki, booster);

        when(booster.hasTemplate(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

        Assert.assertFalse(finder.find("Test article"));        
    }

    /**
     * Test method for {@link 
     * org.wikipedia.nirvana.nirvanabot.templates.TemplateFinder#find(java.lang.String)}.
     */
    @Test
    public void testFind_simple() throws Exception {
        TemplateFindItem findItem = new TemplateFindItem("Money", "", "");
        TemplateFinder finder = new TemplateFinder(
                Collections.singletonList(findItem), wiki, booster);
        
        when(booster.hasTemplate(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        
        Assert.assertTrue(finder.find("Test article"));
    }

    /**
     * Test method for {@link 
     * org.wikipedia.nirvana.nirvanabot.templates.TemplateFinder#find(java.lang.String)}.
     */
    @Test
    public void testFind_withParam() throws Exception {
        TemplateFindItem findItem = new TemplateFindItem("Money", "type", "");
        TemplateFinder finder = new TemplateFinder(
                Collections.singletonList(findItem), wiki, booster);
        
        when(booster.hasTemplate(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        
        when(booster.getPageText(Mockito.eq("Test1"))).thenReturn("Some dummy text");
        when(booster.getPageText(Mockito.eq("Test2"))).thenReturn("Some dummy text\n{{Money}}");
        when(booster.getPageText(Mockito.eq("Test3"))).thenReturn("Some text\n{{Money|a|b|c=1}}");
        when(booster.getPageText(Mockito.eq("Test4"))).thenReturn(
                "Some {{cat|type=good|1}}.\n"
                + "dummy text\\n{{Money|go=cs|type=coin|music=kino}}\\nOther text");
        
        Assert.assertFalse(finder.find("Test1"));
        Assert.assertFalse(finder.find("Test2"));
        Assert.assertFalse(finder.find("Test3"));
        Assert.assertTrue(finder.find("Test4"));        
    }
    
    /**
     * Test method for {@link 
     * org.wikipedia.nirvana.nirvanabot.templates.TemplateFinder#find(java.lang.String)}.
     */
    @Test
    public void testFind_withParamAndValue() throws Exception {
        TemplateFindItem findItem = new TemplateFindItem("Money", "type", "coin");
        TemplateFinder finder = new TemplateFinder(
                Collections.singletonList(findItem), wiki, booster);
        
        when(booster.hasTemplate(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        
        when(booster.getPageText(Mockito.eq("Test1"))).thenReturn("Some dummy text");
        when(booster.getPageText(Mockito.eq("Test2"))).thenReturn("Some dummy text\n{{Money}}");
        when(booster.getPageText(Mockito.eq("Test3"))).thenReturn("Some text\n{{Money|a|b|c=1}}");
        when(booster.getPageText(Mockito.eq("Test4"))).thenReturn(
                "Dummy text\\n{{Money|go=cs|type=paper|music=kino}}\\nOther text");
        when(booster.getPageText(Mockito.eq("Test5"))).thenReturn(
                "Dummy text\\n{{Money|go=cs|type=coin|music=kino}}\\nOther text");
        
        Assert.assertFalse(finder.find("Test1"));
        Assert.assertFalse(finder.find("Test2"));
        Assert.assertFalse(finder.find("Test3"));
        Assert.assertFalse(finder.find("Test4"));
        Assert.assertTrue(finder.find("Test5"));
    }
}
