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

import org.wikipedia.nirvana.BasicBot;
import org.wikipedia.nirvana.localization.TestLocalizationManager;
import org.wikipedia.nirvana.nirvanabot.MockNirvanaBot.TestError;
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
    public static final String BOT_CONFIG_BE = "bot_config_be.xml";
    public static final String BOT_CONFIG_DEFAULT_PATH = TEST_DATA_PATH + BOT_CONFIG_DEFAULT;
    public static final String BOT_CONFIG_BE_PATH = TEST_DATA_PATH + BOT_CONFIG_BE;

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
        MockNirvanaBot bot =
                new MockNirvanaBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{BOT_CONFIG_DEFAULT_PATH});
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
        String config = "015_new_pages_with_images_everywhere.js";
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
     * Similar tests: 027, 026.
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
}
