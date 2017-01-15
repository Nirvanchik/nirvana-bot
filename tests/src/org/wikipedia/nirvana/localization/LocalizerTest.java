/**
 *  @(#)LocalizerTest.java 15.01.2017
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

package org.wikipedia.nirvana.localization;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

/**
 * Unit-tests for {@link Localizer}.
 */
public class LocalizerTest {

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
    }

    @Test
    public void doesNotTranslateIfNoTranslation() {
        Localizer localizer = new Localizer();
        Map<String, String> translations = new HashMap<>();
        translations.put("apple", "Apfel");
        localizer.addTranslations(translations);
        localizer.setInitialized();
        Assert.assertEquals("word", localizer.localize("word"));
    }

    @Test
    public void translatesWord() {
        Localizer localizer = new Localizer();
        Map<String, String> translations = new HashMap<>();
        translations.put("word", "Wort");
        localizer.addTranslations(translations);
        localizer.setInitialized();
        Assert.assertEquals("Wort", localizer.localize("word"));
    }

    @Test
    public void translatesManyWords() {
        Localizer localizer = new Localizer();
        Map<String, String> translations = new HashMap<>();
        translations.put("word", "Wort");
        translations.put("apple", "Apfel");
        localizer.addTranslations(translations);
        localizer.setInitialized();
        Assert.assertEquals("Wort", localizer.localize("word"));
        Assert.assertEquals("Apfel", localizer.localize("apple"));
    }
    
    @Test
    public void collectsNewTerms() {
        Localizer localizer = new Localizer();
        Map<String, String> translations = new HashMap<>();
        translations.put("word", "Wort");
        localizer.addTranslations(translations);
        localizer.setInitialized();
        localizer.localize("apple");
        translations = localizer.getTranslations();
        Assert.assertTrue(translations.containsKey("apple") && translations.get("apple") == null);
    }    

    @Test(expected = Throwable.class)
    public void whenNotInitialized() {
        Localizer localizer = new Localizer();
        Map<String, String> translations = new HashMap<>();
        translations.put("word", "Wort");
        localizer.addTranslations(translations);
        localizer.localize("apple");
    }
}
