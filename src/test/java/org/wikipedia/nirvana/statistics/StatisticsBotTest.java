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

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * Integration tests for {@link StatisticsBot}.
 *
 */
public class StatisticsBotTest {

    public static final String TEST_DATA_PATH = "src/test/resources/test_data/stat_bot/";
    public static final String BOT_CONFIG_DEFAULT = "bot_config_ru_default.xml";
    public static final String BOT_CONFIG_WITH_CACHE = "bot_config_ru_with_cache.xml";
    public static final String BOT_CONFIG_DEFAULT_PATH = TEST_DATA_PATH + BOT_CONFIG_DEFAULT;
    public static final String BOT_CONFIG_WITH_CACHE_PATH = TEST_DATA_PATH + BOT_CONFIG_WITH_CACHE;
    
    MockStatisticsBot bot;

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
        if (bot != null) {
            FileUtils.deleteDirectory(new File(bot.getCacheDir()));
        }
    }

    protected void run(String config) throws TestError {
        run(config, BOT_CONFIG_DEFAULT_PATH);
    }

    protected void run(String config, String botConfig) throws TestError {
        bot = new MockStatisticsBot(BasicBot.FLAG_DEFAULT_LOG, TEST_DATA_PATH + config);
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
        run("003_statistics_year_archive_year.js");
    }
    
    @Test
    public void testStatisticsYear_archive_single() throws TestError {
        run("004_statistics_year_archive_single.js");
    }

    @Test
    public void testStatisticsYear_archive_parse_template_items() throws TestError {
        run("005_statistics_year_archive_parse_template_items.js");
    }
    
    @Test
    public void testStatisticsYear_archive_quarter() throws TestError {
        run("006_statistics_year_archive_quarter.js");
    }
    
    @Test
    public void testStatisticsYear_archive_single_load_from_cache() throws TestError {
        run("007_statistics_year_archive_single_load_from_cache_p1.js",
                BOT_CONFIG_WITH_CACHE_PATH);
        run("007_statistics_year_archive_single_load_from_cache_p2.js",
                BOT_CONFIG_WITH_CACHE_PATH);
    }

    @Test
    public void testStatisticsYear_archive_single_load_from_cache_from_bottom() throws TestError {
        run("008_statistics_year_archive_single_load_from_cache_fbot_p1.js",
                BOT_CONFIG_WITH_CACHE_PATH);
        run("008_statistics_year_archive_single_load_from_cache_fbot_p2.js",
                BOT_CONFIG_WITH_CACHE_PATH);
    }
    
    @Test
    public void testStatisticsYear_first_revision_has_no_author() throws TestError {
        run("009_statistics_year_first_revision_has_no_author.js");
    }

    @Test
    public void testStatisticsYear_archive_single_drop_cache() throws TestError {
        run("010_statistics_year_archive_single_drop_cache_p1.js",
                BOT_CONFIG_WITH_CACHE_PATH);
        run("010_statistics_year_archive_single_drop_cache_p2.js",
                BOT_CONFIG_WITH_CACHE_PATH);
    }

    @Test
    public void testStatisticsYear_archive_quarter_load_from_cache() throws TestError {
        run("011_statistics_year_archive_quarter_load_from_cache_p1.js",
                BOT_CONFIG_WITH_CACHE_PATH);
        run("011_statistics_year_archive_quarter_load_from_cache_p2.js",
                BOT_CONFIG_WITH_CACHE_PATH);
    }

    @Test
    public void testStatisticsYear_archive_year_load_from_cache() throws TestError {
        run("012_statistics_year_archive_year_load_from_cache_p1.js",
                BOT_CONFIG_WITH_CACHE_PATH);
        run("012_statistics_year_archive_year_load_from_cache_p2.js",
                BOT_CONFIG_WITH_CACHE_PATH);
    }

    @Test
    public void testStatisticsYear_archive_quarter_drop_cache() throws TestError {
        run("013_statistics_year_archive_quarter_drop_cache_p1.js",
                BOT_CONFIG_WITH_CACHE_PATH);
        run("013_statistics_year_archive_quarter_drop_cache_p2.js",
                BOT_CONFIG_WITH_CACHE_PATH);
    }

    @Test
    public void testStatisticsYear_archive_year_drop_cache() throws TestError {
        run("014_statistics_year_archive_year_drop_cache_p1.js",
                BOT_CONFIG_WITH_CACHE_PATH);
        run("014_statistics_year_archive_year_drop_cache_p2.js",
                BOT_CONFIG_WITH_CACHE_PATH);
    }

    @Test
    public void testRating_total() throws TestError {
        run("015_rating_total_archive_single.js");
    }

    @Test
    public void testRating_by_year() throws TestError {
        run("016_rating_year_archive_single.js");
    }

    @Test
    public void testStatisticsYear_ratingYear() throws TestError {
        run("017_statistics_year_rating_year.js");
    }

    @Test
    public void testRating_total_user_redirects_to_talk_page() throws TestError {
        run("018_rating_total_user_redirects_to_talk_page.js");
    }

    @Ignore // FIXME
    @Test
    public void testRating_renamed_user() throws TestError {
        run("019_rating_total_renamed_user.js");
    }

    @Test
    public void testRating_renamed_user_2() throws TestError {
        run("020_rating_total_renamed_user_2.js");
    }

    @Test
    public void testRating_total_size_filter() throws TestError {
        run("021_rating_total_size_filter.js");
    }
}
