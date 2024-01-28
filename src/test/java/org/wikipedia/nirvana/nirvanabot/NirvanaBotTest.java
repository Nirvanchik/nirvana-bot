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
 * This file is encoded with UTF-8.
 * */

package org.wikipedia.nirvana.nirvanabot;

import org.wikipedia.nirvana.base.BasicBot;
import org.wikipedia.nirvana.localization.TestLocalizationManager;
import org.wikipedia.nirvana.testing.TestError;
import org.wikipedia.nirvana.util.FileTools;
import org.wikipedia.nirvana.util.MockDateTools;
import org.wikipedia.nirvana.wiki.CatScanTools.Service;
import org.wikipedia.nirvana.wiki.CatScanTools.ServiceFeatures;
import org.wikipedia.nirvana.wiki.MockCatScanTools;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * This is gonna be the main unit-acceptance-test for almost all main bot logic code.
 * The idea is to mock real wiki data in mocked {@link org.wikipedia.Wiki} class, mock responses in
 * {@link org.wikipedia.nirvana.wiki.CatScanTools} class and simulate conditions of real bot
 * running.
 * So, we run the bot and check if his edits in our mocked {@link 
 * org.wikipedia.nirvana.wiki.MockNirvanaWiki MockNirvanaWiki} are correct.
 */
public class NirvanaBotTest {
    public static final String TEST_DATA_PATH = "src/test/resources/test_data/";
    public static final String BOT_CONFIG_DEFAULT = "bot_config_ru_default.xml";
    public static final String BOT_CONFIG_RETRIES = "bot_config_ru_with_retries.xml";
    public static final String BOT_CONFIG_RU_WITH_DISUSSION_TEMPLATES =
            "bot_config_ru_discussion_templates.xml";
    public static final String BOT_CONFIG_RU_TESTARCH = "bot_config_ru_testarch.xml";
    public static final String BOT_CONFIG_RU_ERR_NOTIF = "bot_config_ru_error_notif.xml";
    public static final String BOT_CONFIG_BE = "bot_config_be.xml";
    public static final String BOT_CONFIG_DEFAULT_PATH = TEST_DATA_PATH + BOT_CONFIG_DEFAULT;
    public static final String BOT_CONFIG_BE_PATH = TEST_DATA_PATH + BOT_CONFIG_BE;
    public static final String BOT_CONFIG_RU_TESTARCH_PATH =
            TEST_DATA_PATH + BOT_CONFIG_RU_TESTARCH;
    public static final String BOT_CONFIG_RU_ERR_NOTIF_PATH =
            TEST_DATA_PATH + BOT_CONFIG_RU_ERR_NOTIF; 

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TestLocalizationManager.reset();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        MockCatScanTools.reset();
        TestLocalizationManager.reset();
        MockDateTools.reset();
        TestPortalConfig.reset();
    }

    protected void run(String config) throws TestError {
        run(config, BOT_CONFIG_DEFAULT_PATH);
    }

    protected void run(String config, String botConfig) throws TestError {
        MockNirvanaBot bot =
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{botConfig});
        bot.validateQueries();
        bot.validateEdits();
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
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 017.
     * Similar tests: 001
     * Conditions:
     * Same as 001
     * BOT SETTINGS:
     * 1) language = be
     */
    @Test
    public void newPages_updateWithDefaultValues_firstTimeBe() throws TestError {
        String config = "017_new_pages_update_with_default_vals_first_time_be.js";
        MockNirvanaBot bot =
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_BE_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 018.
     * Similar tests: 001
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "новые статьи"
     * 2) All portal settings are default
     * 3) No archive
     * 4) Invalid placeholders in format string
     * BOT SETTINGS:
     * 1) All bot settings are default
     * WIKI state:
     * 1) New pages page doesn't exist
     */
    @Test
    public void newPages_updateWithBadPlaceholders() throws TestError {
        String config = "018_new_pages_update_with_bad_placeholders.js";
        MockNirvanaBot bot =
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 019.
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "новые статьи"
     * 2) разделитель = "\n"
     */
    @Test
    public void newPages_updateWithDelimiter() throws TestError {
        String config = "019_new_pages_update_with_delimiter.js";
        MockNirvanaBot bot =
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 029.
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "новые статьи"
     * 2) разделитель = "\n"
     * 3) категория = Собаки
     */
    @Test
    public void newPages_updateWith1CategoryOldWay() throws TestError {
        String config = "020_new_pages_update_with_1_category_old.js";
        MockNirvanaBot bot =
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
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
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
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
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
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
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
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
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
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
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
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
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
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
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
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
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
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
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
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
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
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
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
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
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
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
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
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
        run("015_new_pages_with_images_everywhere.js");
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
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 021.
     * Similar tests: .
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "обсуждаемые статьи"
     * SERVICE SETTINGS:
     */
    @Test
    public void discussedPages_smoke() throws TestError {
        String config = "021_pages_templates_with_discussion.js";
        MockNirvanaBot bot =
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{TEST_DATA_PATH + BOT_CONFIG_RU_WITH_DISUSSION_TEMPLATES});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 022.
     * Similar tests: 023.
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "новые статьи"
     * SERVICE SETTINGS:
     */
    @Test
    public void retry_ok() throws TestError {
        String config = "022_new_pages_update_retry_default.js";
        MockNirvanaBot bot =
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{TEST_DATA_PATH + BOT_CONFIG_RETRIES});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 023.
     * Similar tests: 022.
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "новые статьи"
     * SERVICE SETTINGS:
     */
    @Test
    public void retry_fails() throws TestError {
        String config = "023_new_pages_update_retry_fails.js";
        MockNirvanaBot bot =
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{TEST_DATA_PATH + BOT_CONFIG_RETRIES});
        bot.validateQueries();
        bot.validateEdits();
    }

    /**
     * Test case 024.
     * Similar tests: 022, 023.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     *   tryCount = 3
     */
    @Test
    public void retry_custom() throws TestError {
        run("024_new_pages_update_retry_custom.js");
    }

    /**
     * Test case 025.
     * Summary: header must be added at the top of the page (there was no header before update).
     * Similar tests: .
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     *   header = === Новые статьи на тему Собаки ===
     */
    @Test
    public void new_pages_add_header() throws TestError {
        run("025_new_pages_add_header.js");
    }

    /**
     * Test case 026.
     * Summary: header must be added at the top of the page (there was the same header in the page
     * before update).
     * Similar tests: 025, 027.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     *   header = === Новые статьи на тему Собаки ===
     */
    @Test
    public void new_pages_update_header() throws TestError {
        run("026_new_pages_update_header.js");
    }

    /**
     * Test case 027.
     * Summary: header must be added at the top of the page
     * Scenario:
     *   - There was the same header in the page and in bot settings
     *   - User edited portal page and added some whitespace before header
     *   - Bot updates portal page and correctly leaves header (but it's ok to remove whitespace)
     *
     * Similar tests: 026.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     *   header = === Новые статьи на тему Собаки ===
     */
    @Test
    public void new_pages_update_header_remove_whitespace() throws TestError {
        run("027_new_pages_update_header_remove_whitespace.js");
    }

    /**
     * Test case 028.
     * Summary: header must be added at the top of the page
     * Scenario:
     *   - There was the same header in the page and in bot settings, and that header has some
     *   whitespace at the beginning;
     *   - User edited portal page and added some whitespace before header;
     *   - Bot updates portal page and correctly leaves header (but it's ok to remove whitespace).
     *
     * Similar tests: 027, 026, 045.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     *   header = \n === Новые статьи на тему Собаки === \n
     */
    @Test
    public void new_pages_update_header_with_whitespace_remove_whitespace() throws TestError {
        run("028_new_pages_update_header_with_whitespace_remove_whitespace.js");
    }

    /**
     * Test case 029.
     * Summary: header must be removed from the top of the page 
     * Scenario:
     *   - There was a header in the page (in settings and in portal page).
     *   - User removed header in portal settings
     *   - Bot updates portal page without header and removes old header from portal page.
     *
     * Similar tests: 026.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     *   header = 
     */
    @Test
    public void new_pages_remove_header() throws TestError {
        run("029_new_pages_remove_header.js");
    }

    /**
     * Test case 030.
     * Summary: header must be removed from the top of the page 
     * Scenario:
     *   - There was a header in the page (in settings and in portal page).
     *   - User edited header in portal settings
     *   - Bot updates portal page with a new header and removes old header from portal page.
     *
     * Similar tests: 029.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     *   header = === КРАСИВЫЙ ЗАГОЛОВОК ===\n
     */
    @Test
    public void new_pages_update_new_header() throws TestError {
        run("030_new_pages_update_new_header.js");
    }

    /**
     * Test case 031.
     * Summary: header & footer must be added at the top & bottom of the page (there was no header
     * and footer before update).
     *
     * Similar tests: 025.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     *   header = {{НАЧАЛО БЛОКА}}\n
     *   footer = \n{{КОНЕЦ БЛОКА}}
     */
    @Test
    public void new_pages_add_headerfooter() throws TestError {
        run("031_new_pages_add_headerfooter.js");
    }

    /**
     * Test case 032.
     * Summary: header & footer must be added at the top & bottom of the page (there was the same
     * header & the same footer in the page before update).
     *
     * Similar tests: 026, 031.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     *   header = {{НАЧАЛО БЛОКА}}\n
     *   footer = \n{{КОНЕЦ БЛОКА}}
     */
    @Test
    public void new_pages_update_headerfooter() throws TestError {
        run("032_new_pages_update_headerfooter.js");
    }

    /**
     * Test case 033.
     * Summary: header & footer must be removed from the top & bottom of the page 
     * Scenario:
     *   - There was a header & footer in the page (in settings and in portal page).
     *   - User removed header & footer in portal settings
     *   - Bot updates portal page without header and without footer and removes old header & footer
     *   from portal page.
     *
     * Similar tests: 029, 032.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     *   header =
     *   footer = 
     */
    @Test
    public void new_pages_remove_headerfooter() throws TestError {
        run("033_new_pages_remove_headerfooter.js");
    }

    /**
     * Test case 034.
     * Summary: header & footer must be updated at the top & bottom of the page 
     * Scenario:
     *   - There was a header & footer in the page (in settings and in portal page).
     *   - User edited header & footer in portal settings
     *   - Bot updates portal page with updated header and without footer and removes old header &
     *   footer from portal page.
     *
     * Similar tests: 029, 032.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     *   header =
     *   footer = 
     */
    @Test
    public void new_pages_update_new_headerfooter() throws TestError {
        run("034_new_pages_update_new_headerfooter.js");
    }

    /**
     * Test case 035.
     * Summary: bots template must be preserved
     * 
     * Similar tests: 025, 026.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     *   header = {{НАЧАЛО БЛОКА}}\n
     */
    @Test
    public void new_pages_update_header_preserve_bots_template() throws TestError {
        run("035_new_pages_update_header_preserve_bots_template.js");
    }

    /**
     * Test case 036.
     * Summary: header must be added at the top of the page (there was no header before update).
     *
     * Similar tests: 003, 025.
     *
     * PORTAL SETTINGS:
     *   type = "статьи с шаблонами"
     *   header = {{НАЧАЛО БЛОКА}}\n
     */
    @Test
    public void pages_add_header() throws TestError {
        run("036_pages_add_header.js");
    }

    /**
     * Test case 037.
     * Summary: header must be added at the top of the page (there was the same header in the page
     * before update).
     *
     * Similar tests: 003, 026, 037.
     *
     * PORTAL SETTINGS:
     *   type = "статьи с шаблонами"
     *   header = {{НАЧАЛО БЛОКА}}\n
     */
    @Test
    public void pages_update_header() throws TestError {
        run("037_pages_update_header.js");
    }

    /**
     * Test case 038.
     * Summary: new pages week should be updated correctly including header that has a date
     * variable. Also, we should check here update comment with correct new pages count
     * calculation.
     *
     * Similar tests: 026.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи по дням"
     *   header = {{НАЧАЛО БЛОКА}}\n
     *
     * Timezone: UTC 
     * Sorry, I was lazy to convert all 61 timestamps in this test-case by adding +3 hours.
     * I am working at UTC+3, the bot is working at UTC+1 or UTC+2. People that watch the reports
     * work in many different timezones.
     */
    @Test
    public void new_pages_week_update_header() throws TestError {
        run("038_new_pages_week_update_header.js");
        // TODO: Validate edit comment
    }

    /**
     * Test case 039.
     * Summary: bot must remove renamed pages with older title from page list.
     * Similar tests: .
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_process_old_handle_renamed() throws TestError {
        run("039_new_pages_process_old_handle_renamed.js");
    }

    /**
     * Test case 040.
     * Summary: bot must remove deleted pages from page list.
     * Similar tests: 039.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_process_old_remove_deleted() throws TestError {
        run("040_new_pages_process_old_remove_deleted.js");
    }

    /**
     * Test case 041.
     * Summary: bot must remove deleted category pages and not delete existing category pages.
     * Similar tests: 039, 040.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_process_old_category_namespace() throws TestError {
        run("041_new_pages_process_old_category_namespace.js");
    }

    /**
     * Test case 042.
     * Summary: when bot updates new pages it must also update archive page with removed items.
     * Similar tests: .
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_archive_update() throws TestError {
        run("042_new_pages_archive_update.js");
    }

    /**
     * Test case 043.
     * Summary: when bot updates new pages it must also update archive page with removed items.
     *     User specified in settings that archive should be updated at bottom and it must follow
     *     this.
     * Similar tests: 42.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_archive_update_bottom() throws TestError {
        run("043_new_pages_archive_update_bottom.js");
    }

    /**
     * Test case 044.
     * Summary: when bot updates new pages it must also update archive page with removed items. If
     *     archive does not exist it must create it.
     * Similar tests: 42.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_archive_create() throws TestError {
        run("044_new_pages_archive_create.js");
    }

    /**
     * Test case 045.
     * Summary: header must be added at the top of the page
     * Scenario:
     *   - There was the same header in the page and in bot settings, and that header has some
     *   whitespace at the beginning;
     *   - Bot updates portal page and correctly leaves header.
     *
     * Similar tests: 027, 028.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     *   header = \n === Новые статьи на тему Собаки === \n
     */
    @Test
    public void new_pages_update_header_with_whitespace() throws TestError {
        run("045_new_pages_update_header_with_whitespace.js");
    }

    /**
     * Test case 046.
     * Summary: footer must be added at the bottom of the page (there was no footer before update).
     * Similar tests: 025.
     */
    @Test
    public void new_pages_add_footer() throws TestError {
        run("046_new_pages_add_footer.js");
    }

    /**
     * Test case 047.
     * Summary: footer must be added at the bottom of the page (there was the same footer in the
     * page before update).
     * Similar tests: 026, 046.
     */
    @Test
    public void new_pages_update_footer() throws TestError {
        run("047_new_pages_update_footer.js");
    }

    /**
     * Test case 048.
     * Summary: footer must be added at the bottom of the page
     * Scenario:
     *   - There was the same footer in the page and in bot settings, and that footer has some
     *   whitespace at the beginning;
     *   - Bot updates portal page and correctly leaves footer.
     *   - empty lines at the end of footer must be trancated as MediaWiki itself trancates trailing
     *   new lines.
     *
     * Similar tests: 047, 045.
     */
    @Test
    public void new_pages_update_footer_with_trailing_whitespace() throws TestError {
        run("048_new_pages_update_footer_with_trailing_whitespace.js");
    }

    /**
     * Test case 049.
     * Summary: when bot updates new pages it must also update archive page with removed items.
     * Similar tests: 042.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_archive_update_with_headers() throws TestError {
        run("049_new_pages_archive_update_with_headers.js");
    }

    /**
     * Test case 050.
     * Summary: when bot updates new pages it must also update archive page with removed items.
     * Similar tests: 042, 049.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_archive_update_with_headers_no_header() throws TestError {
        run("050_new_pages_archive_update_with_headers_no_header.js");
    }

    /**
     * Test case 051.
     * Summary: when bot updates new pages it must also update archive page with removed items.
     * Similar tests: 042, 049.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_archive_update_with_headers_next_header() throws TestError {
        run("051_new_pages_archive_update_with_headers_next_header.js");
    }

    /**
     * Test case 052.
     * Summary: when bot updates new pages it must also update archive page with removed items.
     * Similar tests: 042.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_archive_update_many_pages() throws TestError {
        run("052_new_pages_archive_update_many_pages.js");
    }

    /**
     * Test case 053.
     * Summary: when bot updates new pages it must also update archive page with removed items.
     * Similar tests: 043, 049.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_archive_update_with_headers_bottom() throws TestError {
        run("053_new_pages_archive_update_with_headers_bottom.js");
    }

    /**
     * Test case 054.
     * Summary: when bot updates new pages it must also update archive page with removed items.
     * Similar tests: 043, 049, 053.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_archive_update_with_headers_bottom_no_header() throws TestError {
        run("054_new_pages_archive_update_with_headers_bottom_no_header.js");
    }

    /**
     * Test case 055.
     * Summary: when bot updates new pages it must also update archive page with removed items.
     * Similar tests: 042, 049, 051.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_archive_update_with_headers_bottom_next_header() throws TestError {
        run("055_new_pages_archive_update_with_headers_bottom_next_header.js");
    }

    /**
     * Test case 056.
     * Summary: when bot updates new pages it must also update archive page with removed items.
     * Similar tests: 049.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_archive_update_with_2headers() throws TestError {
        run("056_new_pages_archive_update_with_2headers.js");
    }

    /**
     * Test case 057.
     * Summary: when bot updates new pages it must also update archive page with removed items.
     * Similar tests: 049, 050, 056.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_archive_update_with_2headers_no_header() throws TestError {
        run("057_new_pages_archive_update_with_2headers_no_header.js");
    }

    /**
     * Test case 058.
     * Summary: when bot updates new pages it must also update archive page with removed items.
     * Similar tests: 049, 053, 056.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_archive_update_with_2headers_bottom() throws TestError {
        run("058_new_pages_archive_update_with_2headers_bottom.js");
    }

    /**
     * Test case 059.
     * Summary: when bot updates new pages it must also update archive page with removed items.
     * Similar tests: 049, 054, 056, 058.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_archive_update_with_2headers_bottom_no_header() throws TestError {
        run("059_new_pages_archive_update_with_2headers_bottom_no_header.js");
    }

    /**
     * Test case 060.
     * Summary: when bot updates new pages it must also update archive page with removed items.
     * Similar tests: 055, 056, 058.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_archive_update_with_2headers_bottom_next_header() throws TestError {
        run("060_new_pages_archive_update_with_2headers_bottom_next_header.js");
    }

    /**
     * Test case 061.
     * Summary: when bot updates new pages it must also update archive page with removed items.
     * Similar tests: 049, 056.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_archive_update_with_2headers_next_header() throws TestError {
        run("061_new_pages_archive_update_with_2headers_next_header.js");
    }

    
    /**
     * Test case 062.
     * Summary: when bot updates new pages it must also update archive page with removed items.
     * Similar tests: 049, 056.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_archive_update_with_2headers_fastload() throws TestError {
        run("062_new_pages_archive_update_with_2headers_fastload.js",
                BOT_CONFIG_RU_TESTARCH_PATH);
    }

    /**
     * Test case 062.
     * Summary: when bot updates new pages it must also update archive page with removed items.
     * Similar tests: 049, 056.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_archive_update_with_2headers_fastload_bottom() throws TestError {
        run("063_new_pages_archive_update_with_2headers_fastload_bottom.js",
                BOT_CONFIG_RU_TESTARCH_PATH);
    }

    // TODO: Broken (invalid header order)
    /**
     * Test case 058.
     * Summary: when bot updates new pages it must also update archive page with removed items.
     * Similar tests: 049, 053, 056.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_archive_update_with_2headers_bottom_skipped_header() throws TestError {
        run("076_new_pages_archive_update_with_2headers_bottom_skipped_header.js");
    }

    /**
     * Test case 077.
     * Summary: .
     * Similar tests: 064.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_archive_update_with_2headers_hash_enum_add() throws TestError {
        run("077_new_pages_archive_update_with_2headers_hash_enum_add.js");
    }

    /**
     * Test case 078.
     * Summary: .
     * Similar tests: 064, 077.
     *
     * PORTAL SETTINGS:
     *   type = "новые статьи"
     */
    @Test
    public void new_pages_archive_update_with_2headers_hash_enum_update() throws TestError {
        run("078_new_pages_archive_update_with_2headers_hash_enum_update.js");
    }

    /**
     * Test case 080.
     * Summary: New pages with images should find pages with images. Image should be checked for
     * existance. If image doesn't exist in main wiki it should exist in Commons.
     * Similar tests: 014.
     */
    @Test
    public void newPages_images_from_commons() throws TestError {
        run("080_new_pages_with_images_from_commons.js");
    }
    
    /**
     * Test case 081.
     * Summary: New pages must support "slow mode" ("fast mode = off"). In slow mode we request
     * data from Catscan for every category in the category list. We merge results and remove
     * duplications.
     *
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "новые статьи"
     *
     * BOT SETTINGS:
     * 1) быстрый режим = "нет"
     * 2) категории -> has 2 categories
     *
     * WIKI state:
     * 1) New pages page doesn't exist
     *
     * Similar tests: 001.
     */
    @Test
    public void newPages_slow_mode() throws TestError {
        run("081_new_pages_slow_mode.js");
    }

    /**
     * Test case 082.
     * Summary: New pages must support "slow mode" ("fast mode = off"). In slow mode we request
     * data from Catscan for every category in the category list. We merge results and remove
     * duplications. Also, we request ignored categories one by one and remove ignored pages from
     * results.
     *
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "новые статьи"
     *
     * BOT SETTINGS:
     * 1) быстрый режим = "нет"
     * 2) категории -> has 2 categories
     * 2) игнорировать -> has 2 categories
     *
     * WIKI state:
     * 1) New pages page doesn't exist
     *
     * Similar tests: 081.
     */
    @Test
    public void newPages_slow_mode_ignored_categories() throws TestError {
        run("082_new_pages_slow_mode_ignored_categories.js");
    }

    /**
     * Test case 083.
     * Summary: New pages must support "slow mode" ("fast mode = off"). In slow mode we request
     * data from Catscan for every category in the category list. We merge results and remove
     * duplications. For any category user may specify customized depth. We must not loose it.
     *
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "новые статьи"
     *
     * BOT SETTINGS:
     * 1) быстрый режим = "нет"
     * 2) категории -> has 2 categories; one category has customized depth
     *
     * WIKI state:
     * 1) New pages page doesn't exist
     *
     * Similar tests: 081.
     */
    @Test
    public void newPages_custom_category_depth() throws TestError {
        run("083_new_pages_custom_category_depth.js");
    }

    /**
     * Test case 084.
     * Summary: User can make mistakes in bot settings. Send notification to portal discussion
     * page in this case.
     *
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "новые статьи"
     *
     * BOT SETTINGS:
     * 1) тип, сервис имеют невалидные значения
     *
     * Similar tests: none.
     */
    @Test
    public void newPages_error_notification() throws TestError {
        run("084_new_pages_error_notification.js", BOT_CONFIG_RU_ERR_NOTIF_PATH);
    }

    /**
     * Test case 085.
     * Summary: User can make mistakes in bot settings. Send notification to portal discussion
     * page in this case.
     *
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "новые статьи"
     *
     * BOT SETTINGS:
     * 1) параметр "категории" не задан (опечатка)
     *
     * Similar tests: 084.
     */
    @Test
    public void newPages_error_notification_2() throws TestError {
        run("085_new_pages_error_notification_2.js", BOT_CONFIG_RU_ERR_NOTIF_PATH);
    }

    /**
     * Test case 086.
     * Summary: User can make mistakes in bot settings. Send notification to portal discussion
     * page in this case.
     *
     * Conditions:
     * PORTAL SETTINGS:
     * 1) type = "новые статьи"
     *
     * BOT SETTINGS:
     * 1) в поле "формат" неизвестные плейсхолдеры
     *
     * Similar tests: 085.
     */
    @Test
    public void newPages_error_notification_3() throws TestError {
        run("086_new_pages_error_notification_3.js", BOT_CONFIG_RU_ERR_NOTIF_PATH);
    }

}
