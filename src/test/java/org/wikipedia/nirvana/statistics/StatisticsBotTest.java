/**
 *  @(#)StatisticsBotTest.java
 *  Copyright © 2023 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.statistics;

import org.wikipedia.nirvana.base.BasicBot;
import org.wikipedia.nirvana.localization.TestLocalizationManager;
import org.wikipedia.nirvana.nirvanabot.TestPortalConfig;
import org.wikipedia.nirvana.testing.TestError;
import org.wikipedia.nirvana.util.FileTools;
import org.wikipedia.nirvana.util.MockDateTools;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

/**
 * Integration tests for {@link StatisticsBot}.
 *
 */
public class StatisticsBotTest {

    public static final String TEST_DATA_PATH = "src/test/resources/test_data/stat_bot/";
    public static final String BOT_CONFIG_DEFAULT = "bot_config_ru_default.xml";
    public static final String BOT_CONFIG_DEFAULT_PATH = TEST_DATA_PATH + BOT_CONFIG_DEFAULT;

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
        TestLocalizationManager.reset();
        MockDateTools.reset();
        TestPortalConfig.reset();
    }

    protected void run(String config) throws TestError {
        run(config, BOT_CONFIG_DEFAULT_PATH);
    }

    protected void run(String config, String botConfig) throws TestError {
        MockStatisticsBot bot =
                new MockStatisticsBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
        bot.run(new String[]{botConfig});
        bot.validateEdits();
    }

    // Ensure test infrastructure is not broken
    @Test
    public void testDataFound() {
        File f = new File(TEST_DATA_PATH);
        Assert.assertTrue(f.exists() && f.isDirectory());
        String str = FileTools.readFileSilently(TEST_DATA_PATH + BOT_CONFIG_DEFAULT);
        Assert.assertNotNull(BOT_CONFIG_DEFAULT + "not found. Something is wrong with test_data!!!",
                str);
    }
    
    @Test
    public void testStatisticsWeek() throws TestError {
        run("001_statistics_week.js");
    }

    @Test
    public void testStatisticsMonth() throws TestError {
        run("002_statistics_month.js");
    }

    @Test
    public void testStatisticsYear() throws TestError {
        run("003_statistics_year.js");
    }
    
    @Test
    public void testStatisticsYear_archive_single() throws TestError {
        run("004_statistics_year_archive_single.js");
    }

    @Test
    public void testStatisticsYear_archive_parse_template_items() throws TestError {
        run("005_statistics_year_archive_parse_template_items.js");
    }
}
