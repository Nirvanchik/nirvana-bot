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
import org.wikipedia.nirvana.MockDateTools;
import org.wikipedia.nirvana.MockWikiTools;
import org.wikipedia.nirvana.NirvanaBasicBot;
import org.wikipedia.nirvana.WikiTools.Service;
import org.wikipedia.nirvana.WikiTools.ServiceFeatures;
import org.wikipedia.nirvana.localization.TestLocalizationManager;
import org.wikipedia.nirvana.nirvanabot.MockNirvanaBot.TestError;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

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
        TestLocalizationManager.reset();
        MockDateTools.reset();
    }

    // Ensure test infrastructure is not broken
    @Test
    public void testDataFound() {
        File f = new File(TEST_DATA_PATH);
        Assert.assertTrue(f.exists() && f.isDirectory());
        String str = FileTools.readFileSilently(TEST_DATA_PATH + "test.xml");
        Assert.assertNotNull("test.xml not found. Something is wrong with test_data!!!", str);
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
                new MockNirvanaBot(NirvanaBasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 002.
     * Info: smoke
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "новые статьи"
     * 2) шаблоны с параметром = Музыкальный коллектив/Язык/Русский язык,
     *    Музыкальный коллектив/Язык/русский язык
     */
    @Test
    public void newPages_templateWithParams1() throws TestError {
        String config = "002_new_pages_template_with_params_1.js";
        MockNirvanaBot bot =
                new MockNirvanaBot(NirvanaBasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 003.
     * Info: smoke
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "статьи с шаблонами"
     * 2) шаблоны = Хорошая статья, Избранная статья, Статья года
     */
    @Test
    public void pages_templates1() throws TestError {
        String config = "003_pages_templates_1.js";
        MockNirvanaBot bot =
                new MockNirvanaBot(NirvanaBasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 004.
     * Similar tests: 002
     * Info: smoke
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "статьи с шаблонами"
     * 2) шаблоны с параметром = Музыкальный коллектив/Язык/Русский язык,
     *    Музыкальный коллектив/Язык/русский язык
     */
    @Test
    public void pages_templateWithParams1() throws TestError {
        String config = "004_pages_template_with_params_1.js";
        MockNirvanaBot bot =
                new MockNirvanaBot(NirvanaBasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 005.
     * Similar tests: 002
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "новые статьи"
     * 2) шаблоны с параметром = Просто шаблон, Музыкальный коллектив/Язык/Русский язык,
     *    Музыкальный коллектив/Язык/русский язык
     */
    @Test
    public void newPages_templateWithParams2() throws TestError {
        String config = "005_new_pages_template_with_params_2.js";
        MockNirvanaBot bot =
                new MockNirvanaBot(NirvanaBasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 006.
     * Similar tests: 004, 005
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "статьи с шаблонами"
     * 2) шаблоны с параметром = Просто шаблон, rq/notability/,
     *    Музыкальный коллектив/Язык/Русский язык,
     *    Музыкальный коллектив/Язык/русский язык
     */
    @Test
    public void pages_templateWithParams2() throws TestError {
        String config = "006_pages_template_with_params_2.js";
        MockNirvanaBot bot =
                new MockNirvanaBot(NirvanaBasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 007.
     * Similar tests: 003, 008, 009
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "статьи с шаблонами"
     * 2) шаблоны = Хорошая статья, Избранная статья, Статья года
     */
    @Test
    public void pages_templates_any() throws TestError {
        String config = "007_pages_templates_any.js";
        MockNirvanaBot bot =
                new MockNirvanaBot(NirvanaBasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 008.
     * Similar tests: 003, 007, 009
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "статьи с шаблонами"
     * 2) шаблоны = Хорошая статья; Избранная статья; Статья года
     */
    @Test
    public void pages_templates_all() throws TestError {
        String config = "008_pages_templates_all.js";
        MockNirvanaBot bot =
                new MockNirvanaBot(NirvanaBasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 009.
     * Similar tests: 003, 007, 008
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "статьи с шаблонами"
     * 2) шаблоны = ! Хорошая статья, Избранная статья, Статья года
     */
    @Test
    public void pages_templates_none() throws TestError {
        String config = "009_pages_templates_none.js";
        MockNirvanaBot bot =
                new MockNirvanaBot(NirvanaBasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 010.
     * Similar tests: 003.
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "новые статьи"
     * 2) шаблоны = Хорошая статья, Избранная статья, Статья года
     */
    @Test
    public void newPages_templates1() throws TestError {
        String config = "010_new_pages_templates_1.js";
        MockNirvanaBot bot =
                new MockNirvanaBot(NirvanaBasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 011.
     * Similar tests: 003, 010.
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "новые статьи"
     * 2) шаблоны = Хорошая статья, Избранная статья, Статья года
     * SERVICE SETTINGS:
     * 1) No support of ServiceFeatures.NEWPAGES_WITH_TEMPLATE
     */
    @Test
    public void newPages_templates2() throws TestError {
        String config = "011_new_pages_templates_2.js";
        // TODO(Nirvanchik): mock it with Mockito.
        // Not possible to mock with Mockito currently.
        // Service spyService = Mockito.spy(Service.PETSCAN);
        // Mockito.when(spyService.supportsFeature(ServiceFeatures.NEWPAGES_WITH_TEMPLATE))
        //         .thenReturn(false);
        Service.setTestFeatures(ServiceFeatures.NEWPAGES | ServiceFeatures.FAST_MODE);
        MockNirvanaBot bot =
                new MockNirvanaBot(NirvanaBasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 012.
     * Similar tests: 005, 011.
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "новые статьи"
     * 2) шаблоны с параметром = Просто шаблон, Музыкальный коллектив/Язык/Русский язык,
     *    Музыкальный коллектив/Язык/русский язык
     * SERVICE SETTINGS:
     * 1) No support of ServiceFeatures.NEWPAGES_WITH_TEMPLATE
     */
    @Test
    public void newPages_templateWithParams3() throws TestError {
        String config = "012_new_pages_template_with_params_3.js";
        // TODO(Nirvanchik): mock it with Mockito.
        // Not possible to mock with Mockito currently.
        // Service spyService = Mockito.spy(Service.PETSCAN);
        // Mockito.when(spyService.supportsFeature(ServiceFeatures.NEWPAGES_WITH_TEMPLATE))
        //         .thenReturn(false);
        Service.setTestFeatures(ServiceFeatures.NEWPAGES | ServiceFeatures.FAST_MODE);
        MockNirvanaBot bot =
                new MockNirvanaBot(NirvanaBasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 013.
     * Similar tests: 014.
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "новые статьи с изображениями в карточке"
     * SERVICE SETTINGS:
     */
    @Test
    public void newPages_imagesInCard() throws TestError {
        String config = "013_new_pages_with_images_in_card.js";
        // TODO(Nirvanchik): mock it with Mockito.
        // Not possible to mock with Mockito currently.
        // Service spyService = Mockito.spy(Service.PETSCAN);
        // Mockito.when(spyService.supportsFeature(ServiceFeatures.NEWPAGES_WITH_TEMPLATE))
        //         .thenReturn(false);
        Service.setTestFeatures(ServiceFeatures.NEWPAGES | ServiceFeatures.FAST_MODE);
        MockNirvanaBot bot =
                new MockNirvanaBot(NirvanaBasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 014.
     * Similar tests: 013.
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "новые статьи с изображениями в тексте"
     * SERVICE SETTINGS:
     */
    @Test
    public void newPages_imagesInText() throws TestError {
        String config = "014_new_pages_with_images_in_text.js";
        // TODO(Nirvanchik): mock it with Mockito.
        // Not possible to mock with Mockito currently.
        // Service spyService = Mockito.spy(Service.PETSCAN);
        // Mockito.when(spyService.supportsFeature(ServiceFeatures.NEWPAGES_WITH_TEMPLATE))
        //         .thenReturn(false);
        Service.setTestFeatures(ServiceFeatures.NEWPAGES | ServiceFeatures.FAST_MODE);
        MockNirvanaBot bot =
                new MockNirvanaBot(NirvanaBasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 015.
     * Similar tests: 013, 014.
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "новые статьи с изображениями"
     * SERVICE SETTINGS:
     */
    @Test
    public void newPages_images() throws TestError {
        String config = "015_new_pages_with_images_everywhere.js";
        // TODO(Nirvanchik): mock it with Mockito.
        // Not possible to mock with Mockito currently.
        // Service spyService = Mockito.spy(Service.PETSCAN);
        // Mockito.when(spyService.supportsFeature(ServiceFeatures.NEWPAGES_WITH_TEMPLATE))
        //         .thenReturn(false);
        Service.setTestFeatures(ServiceFeatures.NEWPAGES | ServiceFeatures.FAST_MODE);
        MockNirvanaBot bot =
                new MockNirvanaBot(NirvanaBasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 016.
     * Similar tests: 013.
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "новые статьи с изображениями в карточке"
     * SERVICE SETTINGS:
     */
    @Test
    public void newPages_imagesInCardInTemplate() throws TestError {
        String config = "016_new_pages_with_images_in_template_in_card.js";
        // TODO(Nirvanchik): mock it with Mockito.
        // Not possible to mock with Mockito currently.
        // Service spyService = Mockito.spy(Service.PETSCAN);
        // Mockito.when(spyService.supportsFeature(ServiceFeatures.NEWPAGES_WITH_TEMPLATE))
        //         .thenReturn(false);
        Service.setTestFeatures(ServiceFeatures.NEWPAGES | ServiceFeatures.FAST_MODE);
        MockNirvanaBot bot =
                new MockNirvanaBot(NirvanaBasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }
}
