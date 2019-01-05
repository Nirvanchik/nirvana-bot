/**
 *  @(#)NirvanaWikiTest.java 06.02.2017
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

package org.wikipedia.nirvana;

import org.wikipedia.Wiki;
import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.localization.TestLocalizationManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

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

        Assert.assertTrue(wiki.isRedirect("#REDIRECT [[Военно-воздушные силы]]\n"));
        Assert.assertFalse(wiki.isRedirect("#перенаправление [[Военно-воздушные силы]]\n"));
        Assert.assertFalse(wiki.isRedirect("#ПЕРЕНАПРАВЛЕНИЕ [[Военно-воздушные силы]]\n"));
        Assert.assertFalse(wiki.isRedirect("Some article text\n"));
    }

    @Test
    public void isRedirectLocalized_defaultLocale() {
        TestLocalizationManager.init(Localizer.DEFAULT_LOCALIZATION);
        NirvanaWiki wiki = new NirvanaWiki("test.xyz", "/w", "https://", "ru");

        Assert.assertTrue(wiki.isRedirect("#REDIRECT [[Военно-воздушные силы]]\n"));
        Assert.assertTrue(wiki.isRedirect("#перенаправление [[Военно-воздушные силы]]\n"));
        Assert.assertTrue(wiki.isRedirect("#ПЕРЕНАПРАВЛЕНИЕ [[Военно-воздушные силы]]\n"));
        Assert.assertFalse(wiki.isRedirect("Some article text\n"));
    }

    @Test
    public void isRedirectLocalized_hasTranslation() {
        Map<String, String> translations = new HashMap<>();
        translations.put("перенаправление", "перанакираванне");
        TestLocalizationManager.init(translations);
        NirvanaWiki wiki = new NirvanaWiki("test.xyz", "/w", "https://", "be");

        Assert.assertTrue(wiki.isRedirect("#REDIRECT [[Военно-воздушные силы]]\n"));
        Assert.assertFalse(wiki.isRedirect("#перенаправление [[Военно-воздушные силы]]\n"));
        Assert.assertFalse(wiki.isRedirect("#ПЕРЕНАПРАВЛЕНИЕ [[Военно-воздушные силы]]\n"));
        Assert.assertTrue(wiki.isRedirect("#перанакираванне [[Военно-воздушные силы]]\n"));
        Assert.assertTrue(wiki.isRedirect("#ПЕРАНАКИРАВАННЕ [[Военно-воздушные силы]]\n"));
        Assert.assertFalse(wiki.isRedirect("Some article text\n"));
    }

    @Test
    public void isRedirectLocalized_noTranslation() {
        Map<String, String> translations = new HashMap<>();
        TestLocalizationManager.init(translations);
        NirvanaWiki wiki = new NirvanaWiki("test.xyz", "/w", "https://", "be");

        Assert.assertTrue(wiki.isRedirect("#REDIRECT [[Военно-воздушные силы]]\n"));
        Assert.assertFalse(wiki.isRedirect("#перенаправление [[Военно-воздушные силы]]\n"));
        Assert.assertFalse(wiki.isRedirect("#ПЕРЕНАПРАВЛЕНИЕ [[Военно-воздушные силы]]\n"));
        Assert.assertFalse(wiki.isRedirect("#перанакираванне [[Военно-воздушные силы]]\n"));
        Assert.assertFalse(wiki.isRedirect("#ПЕРАНАКИРАВАННЕ [[Военно-воздушные силы]]\n"));
        Assert.assertFalse(wiki.isRedirect("Some article text\n"));
    }

    @Test
    public void addTopicToDiscussionPage_addsTopicToBottom() throws LoginException, IOException {
        TestLocalizationManager.init(Localizer.DEFAULT_LOCALIZATION);
        MockNirvanaWiki wiki = new MockNirvanaWiki("test.xyz", "/w", "https://", "ru");
        wiki.login("AlphaBot", "123456");
        String discussion = 
                "= Some topic =\n" +
                "Hello. This is it. Vasya Pupkin, 27 April 2017.\n";
        wiki.mockPageText("Project talk:A", discussion);
        String message =
                "= Warning =\n" +
                "Here god comes.";
        wiki.addTopicToDiscussionPage("Project talk:A", message, false, false, "Add message");

        String expected = 
                "= Some topic =\n" +
                "Hello. This is it. Vasya Pupkin, 27 April 2017.\n" +
                "\n" +
                "= Warning =\n" +
                "Here god comes.";
        wiki.validateEdit("Project talk:A", expected);
    }

    @Test
    public void addTopicToDiscussionPage_duplicates() throws LoginException, IOException {
        TestLocalizationManager.init(Localizer.DEFAULT_LOCALIZATION);
        MockNirvanaWiki wiki = new MockNirvanaWiki("test.xyz", "/w", "https://", "ru");
        wiki.login("AlphaBot", "123456");
        String discussion = 
                "= Some topic =\n" +
                "Hello. This is it. Vasya Pupkin, 27 April 2017.\n" +
                "\n" +
                "= Warning =\n" +
                "Here god comes." +
                "\n" +
                "= Other topic =\n" +
                "Other thing.\n";
        wiki.mockPageText("Project talk:A", discussion);
        String message =
                "= Warning =\n" +
                "Here god comes.";
        wiki.addTopicToDiscussionPage("Project talk:A", message, false, false, "Add message");

        String expected = 
                "= Some topic =\n" +
                "Hello. This is it. Vasya Pupkin, 27 April 2017.\n" +
                "\n" +
                "= Warning =\n" +
                "Here god comes." +
                "\n" +
                "= Other topic =\n" +
                "Other thing.\n" +
                "\n" +
                "= Warning =\n" +
                "Here god comes.";
        wiki.validateEdit("Project talk:A", expected);
    }

    @Test
    public void addTopicToDiscussionPage_doesNotDuplicate() throws LoginException, IOException {
        TestLocalizationManager.init(Localizer.DEFAULT_LOCALIZATION);
        MockNirvanaWiki wiki = new MockNirvanaWiki("test.xyz", "/w", "https://", "ru");
        wiki.login("AlphaBot", "123456");
        String discussion = 
                "= Some topic =\n" +
                "Hello. This is it. Vasya Pupkin, 27 April 2017.\n" +
                "\n" +
                "= Warning =\n" +
                "Here god comes." +
                "\n" +
                "= Other topic =\n" +
                "Other thing.\n";
        wiki.mockPageText("Project talk:A", discussion);
        String message =
                "= Warning =\n" +
                "Here god comes.";
        wiki.addTopicToDiscussionPage("Project talk:A", message, false, true, "Add message");
        
        wiki.validateNoEdits();
    }

    @Test
    public void addTopicToDiscussionPage_addsSignature() throws LoginException, IOException {
        TestLocalizationManager.init(Localizer.DEFAULT_LOCALIZATION);
        MockNirvanaWiki wiki = new MockNirvanaWiki("test.xyz", "/w", "https://", "ru");
        wiki.login("AlphaBot", "123456");
        String discussion = 
                "= Some topic =\n" +
                "Hello. This is it. Vasya Pupkin, 27 April 2017.\n";
        wiki.mockPageText("Project talk:A", discussion);
        String message =
                "= Warning =\n" +
                "Here god comes.";
        wiki.addTopicToDiscussionPage("Project talk:A", message, true, false, "Add message");
        
        String expected = 
                "= Some topic =\n" +
                "Hello. This is it. Vasya Pupkin, 27 April 2017.\n" +
                "\n" +
                "= Warning =\n" +
                "Here god comes. ~~~~\n";
        wiki.validateEdit("Project talk:A", expected);
    }

    @Test
    public void getPageLines() throws LoginException, IOException {
        TestLocalizationManager.init(Localizer.DEFAULT_LOCALIZATION);
        MockNirvanaWiki wiki = new MockNirvanaWiki("test.xyz", "/w", "https://", "ru");
        wiki.login("AlphaBot", "123456");

        String text =
                "Demo - this is a demo page.\n" +
                "Here goes some dummy text.\n" +
                "\n" +
                "== Some topic ==\n" +
                "* Dummy item";

        wiki.mockPageText("P", text);
        wiki.mockNamespace("P", Wiki.MAIN_NAMESPACE);

        List<String> lines = wiki.getPageLines("P");

        List<String> expected = Arrays.asList(
                "Demo - this is a demo page.",
                "Here goes some dummy text.",
                "",
                "== Some topic ==",
                "* Dummy item");
        Assert.assertEquals(expected, lines);
    }
}
