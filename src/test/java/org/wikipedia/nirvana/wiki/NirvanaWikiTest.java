/**
 *  @(#)NirvanaWikiTest.java
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

package org.wikipedia.nirvana.wiki;

import org.wikipedia.Wiki;
import org.wikipedia.Wiki.User;
import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.localization.TestLocalizationManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Unit-tests for {@link NirvanaWiki}.
 *
 */
public class NirvanaWikiTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File tempDir;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        tempDir = folder.newFolder("tmp");
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
    public void addTopicToDiscussionPage_addsTopicToBottom() throws Exception {
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
    public void addTopicToDiscussionPage_duplicates() throws Exception {
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
    public void addTopicToDiscussionPage_doesNotDuplicate() throws Exception {
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
    public void addTopicToDiscussionPage_addsSignature() throws Exception {
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
    public void addTopicToDiscussionPage_respectNobots() throws Exception {
        TestLocalizationManager.init(Localizer.DEFAULT_LOCALIZATION);
        MockNirvanaWiki wiki = new MockNirvanaWiki("test.xyz", "/w", "https://", "ru");
        wiki.login("AlphaBot", "123456");
        String discussion =
                "{{Nobots}}" +
                "= Some topic =\n" +
                "Hello. This is it. Vasya Pupkin, 27 April 2017.\n";
        wiki.mockPageText("Project talk:A", discussion);
        String message =
                "= Warning =\n" +
                "Here god comes.";
        wiki.addTopicToDiscussionPage("Project talk:A", message, true, false, "Add message");
        
        wiki.validateNoEdits();
    }

    @Test
    public void getPageLines() throws Exception {
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

    @Test
    public void loginAndRelogin() throws Exception {
        MockNirvanaWiki wiki = new MockNirvanaWiki("test.xyz");
        wiki.allowLogin(true);
        wiki.mockFetchSequential("logintoken=\"ABC1\"");
        wiki.mockFetchSequential("result=\"Success\" lgusername=\"Bob\"");
        wiki.mockFetchSequential("logintoken=\"ABC2\"");
        wiki.mockFetchSequential("result=\"Success\" lgusername=\"Bob\"");
        String [] userrights = {"god"};
        Map<String, Object> userinfo = new HashMap<>();
        userinfo.put("rights", userrights);
        wiki.mockUserInfo("Bob", userinfo);

        wiki.login("Bob", "12345");
        User user1 = wiki.getCurrentUser();
        Assert.assertEquals(user1.getUsername(), "Bob");

        wiki.relogin();
        User user2 = wiki.getCurrentUser();
        Assert.assertEquals(user1.getUsername(), "Bob");

        Assert.assertTrue(user1 != user2);
    }

    @Test
    public void testPrefixedName() throws Exception {
        MockNirvanaWiki wiki = new MockNirvanaWiki("test.xyz");
        wiki.mockNamespaceIdentifier(5, "Project");
        Assert.assertEquals("Project:Abc", wiki.prefixedName(5, "Abc"));
    }

    @Test
    public void allowBots() {
        Assert.assertTrue(NirvanaWiki.allowBots("Any text", "Dog"));
        Assert.assertTrue(NirvanaWiki.allowBots("Any text {{bots|allow=Dog}} hoho", "Dog"));
        Assert.assertTrue(NirvanaWiki.allowBots("Any text {{Bots|allow=Dog}} hoho", "Dog"));
        Assert.assertTrue(NirvanaWiki.allowBots("Any text {{Bots|x=y|allow=Dog}} hoho", "Dog"));
        Assert.assertTrue(NirvanaWiki.allowBots("Any text {{Bots| allow= Dog }} hoho", "Dog"));
        Assert.assertTrue(NirvanaWiki.allowBots("Any text {{Bots|allow=Bear,Dog,Tiger}}", "Dog"));
        Assert.assertTrue(NirvanaWiki.allowBots("Any text {{Bots|allow=Bear, Dog, Tiger}}", "Dog"));
        Assert.assertTrue(NirvanaWiki.allowBots("Any text {{Bots|allow=all}}", "Dog"));
        Assert.assertTrue(NirvanaWiki.allowBots("Any text {{Bots|deny=none}}", "Dog"));
        Assert.assertTrue(NirvanaWiki.allowBots("Any text {{Bots|deny=Tiger}}", "Dog"));
        Assert.assertTrue(NirvanaWiki.allowBots("Any text {{Bots|deny=Tiger, BadDog}}", "Dog"));
        Assert.assertTrue(NirvanaWiki.allowBots("Any text {{Bots|deny=Tiger, DogDog }}", "Dog"));

        Assert.assertFalse(NirvanaWiki.allowBots("Any text {{nobots}}.", "Dog"));
        Assert.assertFalse(NirvanaWiki.allowBots("Any text {{Nobots}}.", "Dog"));
        Assert.assertFalse(NirvanaWiki.allowBots("Any text {{Bots|allow=none}}.", "Dog"));
        Assert.assertFalse(NirvanaWiki.allowBots("Any text {{bots|allow=none}}.", "Dog"));
        Assert.assertFalse(NirvanaWiki.allowBots("Any text {{bots|deny=all}}.", "Dog"));
        Assert.assertFalse(NirvanaWiki.allowBots("Any text {{bots|deny=all}}.", "Dog"));
        Assert.assertFalse(NirvanaWiki.allowBots("Any text {{bots|optout=all}}.", "Dog"));
        Assert.assertFalse(NirvanaWiki.allowBots("Any text {{bots|deny=Dog}}.", "Dog"));
        Assert.assertFalse(NirvanaWiki.allowBots("Any text {{bots|x=y|deny=Dog}}.", "Dog"));
        Assert.assertFalse(NirvanaWiki.allowBots("Any text {{bots|deny= Dog}}.", "Dog"));
        Assert.assertFalse(NirvanaWiki.allowBots("Any text {{bots|allow=Tiger}}.", "Dog"));
        Assert.assertFalse(NirvanaWiki.allowBots("Any text {{bots|allow=Tiger,BadDog}}.", "Dog"));
        Assert.assertFalse(NirvanaWiki.allowBots("Any text {{bots|allow=Tiger,DogDog}}.", "Dog"));
    }

    @Test
    public void allowEditsByCurrentBot() throws Exception {
        MockNirvanaWiki wiki = new MockNirvanaWiki("test.xyz");
        wiki.login("Dog", "12345");
        Assert.assertTrue(wiki.allowEditsByCurrentBot("Any text {{Bots|allow=Dog}}"));
        Assert.assertFalse(wiki.allowEditsByCurrentBot("Any text {{Bots|allow=Cat}}"));
        Assert.assertFalse(wiki.allowEditsByCurrentBot("Any text {{Bots|deny=Dog}}"));
    }

    @Test
    public void getAllowBotsString() {
        Assert.assertEquals("{{Bots|x=y|allow=Dog}}",
                NirvanaWiki.getAllowBotsString("Any text {{Bots|x=y|allow=Dog}} hoho"));
        Assert.assertEquals("{{bots|allow=none}}",
                NirvanaWiki.getAllowBotsString("Any text {{bots|allow=none}}."));
        Assert.assertEquals("{{Bots|deny=Tiger, BadDog}}",
                NirvanaWiki.getAllowBotsString("Any text {{Bots|deny=Tiger, BadDog}}"));
        Assert.assertEquals("{{nobots}}",
                NirvanaWiki.getAllowBotsString("Any text {{nobots}}."));
        Assert.assertEquals("{{Nobots}}",
                NirvanaWiki.getAllowBotsString("Any text {{Nobots}}."));
    }

    @Test
    public void editInDebugMode() throws Exception {
        MockNirvanaWiki wiki = new MockNirvanaWiki("test.xyz");
        wiki.allowEdits(true);
        wiki.setDumpMode(tempDir.getAbsolutePath());
        wiki.mockPageText("Dog", "I am a big dog.");
        wiki.edit("Dog", "Hou hou", "haha");
        File oldDump = new File(tempDir, "Dog.old.txt");
        Assert.assertEquals("I am a big dog.",
                new String(Files.readAllBytes(Paths.get(oldDump.getAbsolutePath())), "UTF-8"));
        File newDump = new File(tempDir, "Dog.new.txt");
        Assert.assertEquals("Hou hou",
                new String(Files.readAllBytes(Paths.get(newDump.getAbsolutePath())), "UTF-8"));
    }

    @Test
    public void testLogWrapper() {
        MockNirvanaWiki wiki = new MockNirvanaWiki("test.xyz") {
            private static final long serialVersionUID = 1L;
            
            void logAllLevels() {
                log(Level.INFO, "debug", "info message");
                log(Level.CONFIG, "debug", "config message");
                log(Level.FINE, "debug", "fine message");
                log(Level.FINER, "debug", "finer message");
                log(Level.FINEST, "debug", "finest message");
                log(Level.WARNING, "debug", "warning message");
                log(Level.SEVERE, "debug", "severe message");
                logurl("http://some.url", "debug");
            }

            @Override
            public void debug() {
                logAllLevels();
                logDomain = true;
                logAllLevels();
            }
        };
        wiki.debug();
    }

    @Test
    public void testLogWrapperWithException() {
        MockNirvanaWiki wiki = new MockNirvanaWiki("test.xyz") {
            private static final long serialVersionUID = 1L;

            @Override
            public void debug() {
                Exception ex = new Exception("bug");
                log(Level.WARNING, "debug", "warning message", ex);
                log(Level.SEVERE, "debug", "severe message", ex);
                logDomain = true;
                log(Level.SEVERE, "debug", "severe message", ex);
            }
        };
        wiki.debug();
    }

    @Test
    public void editIfChanged() throws Exception {
        MockNirvanaWiki wiki = new MockNirvanaWiki("test.xyz");

        wiki.mockPageText("Bob", null);
        wiki.editIfChanged("Bob", "dummy text", "dummy summary", false, true);
        wiki.validateEdit("Bob", "dummy text");
        wiki.resetEdits();

        wiki.mockPageText("Bob", "gogo");
        wiki.editIfChanged("Bob", "dummy text", "dummy summary", false, true);
        wiki.validateEdit("Bob", "dummy text");
        wiki.resetEdits();

        wiki.mockPageText("Bob", "good boy");
        wiki.editIfChanged("Bob", "good boy\n", "dummy summary", false, true);
        wiki.validateEdit("Bob", "good boy\n");
        wiki.resetEdits();

        wiki.mockPageText("Bob", "good boy");
        wiki.editIfChanged("Bob", "goof boy", "dummy summary", false, true);
        wiki.validateEdit("Bob", "goof boy");
        wiki.resetEdits();

        wiki.mockPageText("Bob", "bad boy");
        wiki.editIfChanged("Bob", "bad boy", "dummy summary", false, true);
        wiki.validateNoEdits();
        wiki.resetEdits();
    }

    @Test (expected = FileNotFoundException.class)
    public void prepend_pageDoesNotExist() throws Exception {
        MockNirvanaWiki wiki = new MockNirvanaWiki("test.xyz");
        wiki.mockPageText("Bob", null);
        wiki.prepend("Bob", "dummy text", false, true);
    }

    @Test
    public void prepend() throws Exception {
        MockNirvanaWiki wiki = new MockNirvanaWiki("test.xyz");        
        wiki.mockPageText("Bob", "I like dogs.");

        wiki.prepend("Bob", "I like cats.\n", false, true);

        wiki.validateEdit("Bob", "I like cats.\nI like dogs.");
    }

    @Test
    public void prependOrCreate() throws Exception {
        MockNirvanaWiki wiki = new MockNirvanaWiki("test.xyz");

        wiki.mockPageText("Bob", null);
        wiki.prependOrCreate("Bob", "dummy text", false, true);
        wiki.validateEdit("Bob", "dummy text");

        wiki.resetEdits();

        wiki.mockPageText("Bob", "I like dogs.");
        wiki.prependOrCreate("Bob", "I like cats.\n", false, true);
        wiki.validateEdit("Bob", "I like cats.\nI like dogs.");
    }

    @Test (expected = FileNotFoundException.class)
    public void append_pageDoesNotExist() throws Exception {
        MockNirvanaWiki wiki = new MockNirvanaWiki("test.xyz");
        wiki.mockPageText("Bob", null);
        wiki.append("Bob", "dummy text", "dummy comment", false, true);
    }

    @Test
    public void append() throws Exception {
        MockNirvanaWiki wiki = new MockNirvanaWiki("test.xyz");        
        wiki.mockPageText("Bob", "I like dogs.");

        wiki.append("Bob", "I like cats.", "dummy comment", false, true);

        wiki.validateEdit("Bob", "I like dogs.I like cats.");
    }

    @Test
    public void appendOrCreate() throws Exception {
        MockNirvanaWiki wiki = new MockNirvanaWiki("test.xyz");

        wiki.mockPageText("Bob", null);
        wiki.appendOrCreate("Bob", "dummy text", "dummy comment", false, true);
        wiki.validateEdit("Bob", "dummy text");

        wiki.resetEdits();

        wiki.mockPageText("Bob", "I like dogs.");
        wiki.appendOrCreate("Bob", "I like cats.", "dummy comment", false, true);
        wiki.validateEdit("Bob", "I like dogs.I like cats.");
    }
}
