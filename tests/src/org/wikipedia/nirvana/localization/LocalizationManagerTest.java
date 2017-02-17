/**
 *  @(#)LocalizationManagerTest.java 15.01.2017
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

package org.wikipedia.nirvana.localization;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.FileTools;
import org.wikipedia.nirvana.NirvanaBasicBot;
import org.wikipedia.nirvana.NirvanaWiki;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;

import junit.framework.Assert;

/**
 * Unit-tests for {@link LocalizationManager}.
 */
public class LocalizationManagerTest {
    private static final String OUT_DIR = "out/test";
    private static final String CACHE_DIR = "out/test/cache";
    private static final String TRANSLATIONS_DIR = "out/test/translations";
    private static final String LANG = "test";
    private static final String CUSTOM_TRANSLATION_FILE = "custom_translation.ini";
    private static final String CUSTOM_TRANSLATION_PATH = TRANSLATIONS_DIR + "/"
            + CUSTOM_TRANSLATION_FILE;
    private static final String WIKI_TRANSLATIONS = "Project:X/WikiTranslationPage";

    private File translationFile;
    private File customTranslationFile;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        File f = new File(TRANSLATIONS_DIR);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        customTranslationFile = new File(CUSTOM_TRANSLATION_PATH);
        translationFile = new File(TRANSLATIONS_DIR + "/" + LANG + ".ini");
    }

    @After
    public void tearDown() throws Exception {
        Localizer.resetFromTests();
        translationFile.delete();
        customTranslationFile.delete();
        for (File f : new File(CACHE_DIR).listFiles()) {
            f.delete();
        }
    }

    private void writeFile(File file, String text) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        OutputStreamWriter or = new OutputStreamWriter(out, "UTF-8");
        or.write(text);
        or.close();
    }

    private NirvanaWiki makeWikiWithTranslation(String translation) throws IOException {
        return makeWikiWithTranslation(translation, true);
    }

    private NirvanaWiki makeWikiWithTranslation(String translation, boolean old)
            throws IOException {
        NirvanaWiki wiki = mock(NirvanaWiki.class);
        when(wiki.getPageText(eq(WIKI_TRANSLATIONS))).thenReturn(translation);
        when(wiki.exists(eq(WIKI_TRANSLATIONS))).thenReturn(true);
        Calendar c = Calendar.getInstance();
        if (old) {
            c.add(Calendar.DAY_OF_MONTH, -1);
        } else {
            c.add(Calendar.DAY_OF_MONTH, 1);
        }
        Revision r = wiki.new Revision(0, c, WIKI_TRANSLATIONS, "edit", "Bot", false, false, false,
                0);
        when(wiki.getTopRevision(eq(WIKI_TRANSLATIONS))).thenReturn(r);
        return wiki;
    }

    private NirvanaWiki makeWikiWithNoTranslation() throws IOException {
        NirvanaWiki wiki = mock(NirvanaWiki.class);
        when(wiki.getPageText(eq(WIKI_TRANSLATIONS))).thenThrow(new FileNotFoundException());
        when(wiki.exists(eq(WIKI_TRANSLATIONS))).thenReturn(false);
        return wiki;
    }

    @Test
    public void loadsTranslationsFromLanguageSelectedFile() throws Exception {
        writeFile(translationFile, "apple = Apfel");
        LocalizationManager localizationManager = new LocalizationManager(OUT_DIR, CACHE_DIR,
                TRANSLATIONS_DIR, LANG);
        localizationManager.loadDefault();
        Assert.assertEquals("Apfel", Localizer.getInstance().localize("apple"));
    }

    @Test
    public void loadsTranslationsFromFile_hasKeyNoTranslation() throws Exception {
        writeFile(translationFile, "apple = ");
        LocalizationManager localizationManager = new LocalizationManager(OUT_DIR, CACHE_DIR,
                TRANSLATIONS_DIR, LANG);
        localizationManager.loadDefault();
        Assert.assertEquals("apple", Localizer.getInstance().localize("apple"));
    }

    @Test
    public void loadsTranslationsFromFile_noTranslation() throws Exception {
        writeFile(translationFile, "");
        LocalizationManager localizationManager = new LocalizationManager(OUT_DIR, CACHE_DIR,
                TRANSLATIONS_DIR, LANG);
        localizationManager.loadDefault();
        Assert.assertEquals("apple", Localizer.getInstance().localize("apple"));
    }

    @Test(expected = Throwable.class)
    public void raisesIfTranslationMissing() throws Exception {
        LocalizationManager localizationManager = new LocalizationManager(OUT_DIR, CACHE_DIR,
                TRANSLATIONS_DIR, LANG);
        localizationManager.loadDefault();
    }

    @Test
    public void loadsTranslationsFromCustomFile() throws Exception {
        writeFile(customTranslationFile, "apple = Apfel");
        LocalizationManager localizationManager = new LocalizationManager(OUT_DIR, CACHE_DIR,
                TRANSLATIONS_DIR, LANG, CUSTOM_TRANSLATION_PATH);
        localizationManager.loadDefault();
        Assert.assertEquals("Apfel", Localizer.getInstance().localize("apple"));
    }

    @Test(expected = Throwable.class)
    public void raisesIfCustomTranslationMissing() throws Exception {
        writeFile(translationFile, "apple = Apfel");
        LocalizationManager localizationManager = new LocalizationManager(OUT_DIR, CACHE_DIR,
                TRANSLATIONS_DIR, LANG, CUSTOM_TRANSLATION_PATH);
        localizationManager.loadDefault();
    }

    @Test
    public void loadsTranslationsFromWiki() throws Exception {
        writeFile(translationFile, "apple = Apfel");
        NirvanaWiki wiki = makeWikiWithTranslation("<pre>\nparrot = Birne\n</pre>\n");

        LocalizationManager localizationManager = new LocalizationManager(OUT_DIR, CACHE_DIR,
                TRANSLATIONS_DIR, LANG);
        localizationManager.load(wiki, WIKI_TRANSLATIONS);

        verify(wiki, Mockito.atLeastOnce()).getPageText(eq(WIKI_TRANSLATIONS));
        Assert.assertEquals("Apfel", Localizer.getInstance().localize("apple"));
        Assert.assertEquals("Birne", Localizer.getInstance().localize("parrot"));
    }

    @Test
    public void loadsTranslationsFromWiki_hasManyTranslations() throws Exception {
        writeFile(translationFile, "");
        NirvanaWiki wiki = makeWikiWithTranslation(
                "<pre>\nparrot = Birne\napple = Apfel\n</pre>\n");

        LocalizationManager localizationManager = new LocalizationManager(OUT_DIR, CACHE_DIR,
                TRANSLATIONS_DIR, LANG);
        localizationManager.load(wiki, WIKI_TRANSLATIONS);

        Assert.assertEquals("Apfel", Localizer.getInstance().localize("apple"));
        Assert.assertEquals("Birne", Localizer.getInstance().localize("parrot"));
    }

    @Test
    public void loadsTranslationsFromWiki_noWikiPage() throws Exception {
        writeFile(translationFile, "apple = Apfel");
        NirvanaWiki wiki = makeWikiWithNoTranslation();

        LocalizationManager localizationManager = new LocalizationManager(OUT_DIR, CACHE_DIR,
                TRANSLATIONS_DIR, LANG);
        localizationManager.load(wiki, WIKI_TRANSLATIONS);

        Assert.assertEquals("Apfel", Localizer.getInstance().localize("apple"));
    }

    @Test
    public void loadsTranslationsFromWiki_noTranslation() throws Exception {
        writeFile(translationFile, "");
        NirvanaWiki wiki = makeWikiWithTranslation("<pre>\n</pre>\n");

        LocalizationManager localizationManager = new LocalizationManager(OUT_DIR, CACHE_DIR,
                TRANSLATIONS_DIR, LANG);
        localizationManager.load(wiki, WIKI_TRANSLATIONS);

        Assert.assertEquals("parrot", Localizer.getInstance().localize("parrot"));
    }

    @Test
    public void loadsTranslationsFromWiki_hasKeyNoTranslation() throws Exception {
        writeFile(translationFile, "");
        NirvanaWiki wiki = makeWikiWithTranslation("<pre>\nparrot = \n</pre>\n");

        LocalizationManager localizationManager = new LocalizationManager(OUT_DIR, CACHE_DIR,
                TRANSLATIONS_DIR, LANG);
        localizationManager.load(wiki, WIKI_TRANSLATIONS);

        Assert.assertEquals("parrot", Localizer.getInstance().localize("parrot"));
    }

    @Test
    public void loadsTranslationsFromWiki_usesCache() throws Exception {
        writeFile(translationFile, "");

        NirvanaWiki wiki = makeWikiWithTranslation("<pre>\napple = Apfel\n</pre>\n");
        FileTools.dump("<pre>\napple = Apfel</pre>\n", CACHE_DIR, WIKI_TRANSLATIONS);

        LocalizationManager localizationManager = new LocalizationManager(OUT_DIR, CACHE_DIR,
                TRANSLATIONS_DIR, LANG);
        localizationManager.load(wiki, WIKI_TRANSLATIONS);

        verify(wiki, never()).getPageText(eq(WIKI_TRANSLATIONS));
        Assert.assertEquals("Apfel", Localizer.getInstance().localize("apple"));
    }

    @Test
    public void loadsTranslationsFromWiki_cacheIsDirty() throws Exception {
        writeFile(translationFile, "");

        NirvanaWiki wiki = makeWikiWithTranslation(
                "<pre>\napple = Apfel\nparrot = Birne\n</pre>\n", false);
        FileTools.dump("<pre>\napple = Apfel</pre>\n", CACHE_DIR, WIKI_TRANSLATIONS);

        LocalizationManager localizationManager = new LocalizationManager(OUT_DIR, CACHE_DIR,
                TRANSLATIONS_DIR, LANG);
        localizationManager.load(wiki, WIKI_TRANSLATIONS);

        verify(wiki, atLeastOnce()).getPageText(eq(WIKI_TRANSLATIONS));
        Assert.assertEquals("Birne", Localizer.getInstance().localize("parrot"));
    }

    @Test
    public void skipLocalizationForDefaultLang_default() throws Exception {
        Assume.assumeTrue(!NirvanaBasicBot.DEBUG_BUILD);

        LocalizationManager localizationManager = new LocalizationManager(OUT_DIR, CACHE_DIR,
                TRANSLATIONS_DIR, LocalizationManager.DEFAULT_LANG);
        localizationManager.loadDefault();

        Assert.assertTrue(Localizer.getInstance().getTranslations().isEmpty());
    }

    @Test
    public void skipLocalizationForDefaultLang_wiki() throws Exception {
        Assume.assumeTrue(!NirvanaBasicBot.DEBUG_BUILD);
        NirvanaWiki wiki = makeWikiWithTranslation("<pre>\napple = Apfel\n</pre>\n");

        LocalizationManager localizationManager = new LocalizationManager(OUT_DIR, CACHE_DIR,
                TRANSLATIONS_DIR, LocalizationManager.DEFAULT_LANG);
        localizationManager.load(wiki, WIKI_TRANSLATIONS);

        Mockito.verifyNoMoreInteractions(wiki);
        Assert.assertTrue(Localizer.getInstance().getTranslations().isEmpty());
    }

    @Test
    public void refreshWikiTranslations_defaultLang() throws Exception {
        Assume.assumeTrue(!NirvanaBasicBot.DEBUG_BUILD);
        NirvanaWiki wiki = makeWikiWithTranslation("<pre>\napple = Apfel\n</pre>\n");

        LocalizationManager localizationManager = new LocalizationManager(OUT_DIR, CACHE_DIR,
                TRANSLATIONS_DIR, LocalizationManager.DEFAULT_LANG);
        localizationManager.load(wiki, WIKI_TRANSLATIONS);
        localizationManager.refreshWikiTranslations(wiki, WIKI_TRANSLATIONS);

        Mockito.verifyNoMoreInteractions(wiki);
    }

    @Test
    public void refreshWikiTranslations_updateWiki() throws Exception {
        writeFile(translationFile, "");
        NirvanaWiki wiki = makeWikiWithTranslation(
                "<pre>\n���������� ����� ������ ��� ��������=\napple = Apfel\n</pre>\n");

        LocalizationManager localizationManager = new LocalizationManager(OUT_DIR, CACHE_DIR,
                TRANSLATIONS_DIR, LANG);
        localizationManager.load(wiki, WIKI_TRANSLATIONS);
        Localizer.getInstance().localize("coconut");
        localizationManager.refreshWikiTranslations(wiki, WIKI_TRANSLATIONS);

        verify(wiki, atLeastOnce()).edit(eq(WIKI_TRANSLATIONS),
                eq("<pre>\n���������� ����� ������ ��� ��������=\n" +
                        "apple = Apfel\ncoconut = \n</pre>\n"),
                Mockito.anyString());
    }
    
    @Test
    public void refreshWikiTranslations_createWiki() throws Exception {
        writeFile(translationFile, "");
        NirvanaWiki wiki = makeWikiWithNoTranslation();

        LocalizationManager localizationManager = new LocalizationManager(OUT_DIR, CACHE_DIR,
                TRANSLATIONS_DIR, LANG);
        localizationManager.load(wiki, WIKI_TRANSLATIONS);
        Localizer.getInstance().localize("coconut");
        localizationManager.refreshWikiTranslations(wiki, WIKI_TRANSLATIONS);

        verify(wiki, atLeastOnce()).edit(eq(WIKI_TRANSLATIONS),
                eq("<pre>\n�������� �������� ����������� = \ncoconut = \n</pre>\n"),
                Mockito.anyString());
    }
}