/**
 *  @(#)NirvanaBotTest.java 03.04.2016
 *  Copyright © 2016 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.wikipedia.nirvana.FileTools;
import org.wikipedia.nirvana.MockWikiTools;
import org.wikipedia.nirvana.NirvanaBasicBot;
import org.wikipedia.nirvana.nirvanabot.MockNirvanaBot.TestError;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;

/**
 * This is gonna be the main unit-acceptance-test for almost all main bot logic code.
 * The idea is to mock real wiki data in mocked {@link org.wikipedia.Wiki} class, mock responses in
 * {@link org.wikipedia.nirvana.WikiTools} class and simulate conditions of real bot running.
 * So, we run the bot and check if his edits in our mocked {@link 
 * org.wikipedia.nirvana.MockNirvanaWiki MockNirvanaWiki} are correct.
 */
public class NirvanaBotTest {
    public static final String TEST_DATA_PATH = "tests/test_data/";
    public static final String BOT_CONFIG_DEFAULT = "bot_config_ru_default.xml";
    public static final String BOT_CONFIG_DEFAULT_PATH = TEST_DATA_PATH + BOT_CONFIG_DEFAULT;

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
        MockWikiTools.reset();
    }

    // Ensure test infrastructure is not broken
    @Test
    public void testDataFound() {
        String str = FileTools.readFileSilently("tests/test_data/test.xml");
        Assert.assertNotNull(str);
    }

    /**
     * Test case 001.
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "новые статьи"
     * 2) All portal settings are default
     * 3) No archive
     * BOT SETTINGS:
     * 1) All bot settings are default
     * WIKI state:
     * 1) New pages page doesn't exist
     */
    @Test
    public void newPages_updateWithDefaultValues_firstTime() throws TestError {
        String config = "001_new_pages_update_with_default_vals_first_time.js";
        MockNirvanaBot bot =
                new MockNirvanaBot(NirvanaBasicBot.FLAG_CONSOLE_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 002.
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "новые статьи"
     * 2) шаблоны с параметром = ћузыкальный коллектив/язык/–усский €зык,
     *    ћузыкальный коллектив/язык/русский €зык
     * 3) No archive
     * BOT SETTINGS:
     * 1) All bot settings are default
     * WIKI state:
     * 1) New pages page doesn't exist
     */
    @Test
    public void newPages_templateWithParams1() throws TestError {
        String config = "002_new_pages_template_with_params_1.js";
        MockNirvanaBot bot =
                new MockNirvanaBot(NirvanaBasicBot.FLAG_CONSOLE_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 003.
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "статьи с шаблонами"
     * 2) шаблоны = ’ороша€ стать€, »збранна€ стать€, —тать€ года
     * 3) No archive
     * BOT SETTINGS:
     * 1) All bot settings are default
     * WIKI state:
     * 1) New pages page doesn't exist
     */
    @Test
    public void pages_templates1() throws TestError {
        String config = "003_pages_templates_1.js";
        MockNirvanaBot bot =
                new MockNirvanaBot(NirvanaBasicBot.FLAG_CONSOLE_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }
}
