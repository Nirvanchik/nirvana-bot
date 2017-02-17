/**
 *  @(#)NirvanaWikiTest.java 06.02.2017
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

package org.wikipedia.nirvana;

import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.localization.TestLocalizationManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit-tests for {@link NirvanaWiki}.
 *
 */
public class NirvanaWikiTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        TestLocalizationManager.reset();
    }

    @Test
    public void isRedirect() {
        TestLocalizationManager.init(Localizer.NO_LOCALIZATION);
        NirvanaWiki wiki = new NirvanaWiki("test.xyz", "/w", "https://", "en");

        Assert.assertTrue(wiki.isRedirect("#REDIRECT [[������-��������� ����]]\n"));
        Assert.assertFalse(wiki.isRedirect("#��������������� [[������-��������� ����]]\n"));
        Assert.assertFalse(wiki.isRedirect("#��������������� [[������-��������� ����]]\n"));
        Assert.assertFalse(wiki.isRedirect("Some article text\n"));
    }

    @Test
    public void isRedirectLocalized_defaultLocale() {
        TestLocalizationManager.init(Localizer.DEFAULT_LOCALIZATION);
        NirvanaWiki wiki = new NirvanaWiki("test.xyz", "/w", "https://", "ru");

        Assert.assertTrue(wiki.isRedirect("#REDIRECT [[������-��������� ����]]\n"));
        Assert.assertTrue(wiki.isRedirect("#��������������� [[������-��������� ����]]\n"));
        Assert.assertTrue(wiki.isRedirect("#��������������� [[������-��������� ����]]\n"));
        Assert.assertFalse(wiki.isRedirect("Some article text\n"));
    }

    @Test
    public void isRedirectLocalized_hasTranslation() {
        Map<String, String> translations = new HashMap<>();
        translations.put("���������������", "���������������");
        TestLocalizationManager.init(translations);
        NirvanaWiki wiki = new NirvanaWiki("test.xyz", "/w", "https://", "be");

        Assert.assertTrue(wiki.isRedirect("#REDIRECT [[������-��������� ����]]\n"));
        Assert.assertFalse(wiki.isRedirect("#��������������� [[������-��������� ����]]\n"));
        Assert.assertFalse(wiki.isRedirect("#��������������� [[������-��������� ����]]\n"));
        Assert.assertTrue(wiki.isRedirect("#��������������� [[������-��������� ����]]\n"));
        Assert.assertTrue(wiki.isRedirect("#��������������� [[������-��������� ����]]\n"));
        Assert.assertFalse(wiki.isRedirect("Some article text\n"));
    }

    @Test
    public void isRedirectLocalized_noTranslation() {
        Map<String, String> translations = new HashMap<>();
        TestLocalizationManager.init(translations);
        NirvanaWiki wiki = new NirvanaWiki("test.xyz", "/w", "https://", "be");

        Assert.assertTrue(wiki.isRedirect("#REDIRECT [[������-��������� ����]]\n"));
        Assert.assertFalse(wiki.isRedirect("#��������������� [[������-��������� ����]]\n"));
        Assert.assertFalse(wiki.isRedirect("#��������������� [[������-��������� ����]]\n"));
        Assert.assertFalse(wiki.isRedirect("#��������������� [[������-��������� ����]]\n"));
        Assert.assertFalse(wiki.isRedirect("#��������������� [[������-��������� ����]]\n"));
        Assert.assertFalse(wiki.isRedirect("Some article text\n"));
    }
}