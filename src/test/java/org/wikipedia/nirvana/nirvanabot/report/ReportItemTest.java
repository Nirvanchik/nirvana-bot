/**
 *  @(#)ReportItemTest.java
 *  Copyright © 2020 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.nirvanabot.report;

import org.wikipedia.nirvana.localization.TestLocalizationManager;
import org.wikipedia.nirvana.nirvanabot.NirvanaBot.BotError;
import org.wikipedia.nirvana.nirvanabot.report.ReportItem.Status;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * Unit-tests for {@link ReportItem}.
 *
 */
public class ReportItemTest {

    static class TestReportItem extends ReportItem {
        private LinkedList<Long> mockTimeQueue = new LinkedList<>();
        
        public TestReportItem(String template, String name) {
            super(template, name);
        }

        @Override
        protected long getCurrentTimeMillis() {
            Assert.assertFalse(
                    "getCurrentTimeMillis() was called but there is no mocked times in buffer",
                    mockTimeQueue.isEmpty());
            return mockTimeQueue.remove();
        }

        public void mockTime(Long time) {
            mockTimeQueue.add(time);
        }
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TestLocalizationManager.initWithReportingEnglishTranslations();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        TestLocalizationManager.reset();
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    private ReportItem prepareAndRunItem() {
        return prepareAndRunItem(null);
    }

    private ReportItem prepareAndRunItem(BotError error) {
        TestReportItem item = new TestReportItem("Settings Template", "Portal A");
        item.mockTime(1000L);
        item.mockTime(71000L);
        item.start();
        item.processed();
        if (error == null) {
            item.updated();
            item.updated = true;
            item.archived = true;
            item.newPagesFound = 5;
            item.pagesArchived = 5;
        } else {
            item.error(error);
        }
        item.end();
        return item;
    }

    /**
     * Test method for {@link ReportItem#toStringTxt()}.
     */
    @Test
    public void testToStringTxt() {
        ReportItem item = prepareAndRunItem();

        String reportLine = item.toStringTxt();

        Assert.assertTrue(String.format("String \"%s\" does not match expected re", reportLine),
                Pattern.matches(
                "Portal A\\s+UPDATED\\s+00:01:10\\s+5\\s+yes\\s+5\\s+yes\\s+0\\s+NONE\\s*",
                reportLine));
        
    }

    /**
     * Test method for {@link ReportItem#toStringWiki(int)}.
     */
    @Test
    public void testToStringWiki() {
        ReportItem item = prepareAndRunItem();

        String reportLine = item.toStringWiki(1);

        Assert.assertEquals(
                "|-\n|1 ||align='left'| [[Portal A]] || 1 || {{Yes|UPDATED}} || 00:01:10 " +
                "|| 5 || {{Yes}} || 5 || {{Yes}} || 0 || -",
                reportLine);
    }

    @Test
    public void testToStringWiki_error() {
        ReportItem item = prepareAndRunItem(BotError.IO_ERROR);

        String reportLine = item.toStringWiki(1);

        Assert.assertEquals(
                "|-\n|1 ||align='left'| [[Portal A]] || 1 || {{No|ERROR}} || 00:01:10 " +
                "|| 0 || No || 0 || No || 0 || {{No|IO_ERROR}}",
                reportLine);
    }

    @Test
    public void testGetHeaderWiki() {
        // Check that separator count in the header is matching one in normal report line.
        ReportItem item = prepareAndRunItem();

        String headerLine = ReportItem.getHeaderWiki();
        System.out.println("headerLine: " + headerLine);
        String reportLine = item.toStringWiki(1);
        System.out.println("reportLine: " + reportLine);

        Assert.assertEquals(
                StringUtils.countMatches(reportLine, "||"),
                StringUtils.countMatches(headerLine, "!!"));
    }

    /**
     * Test method for {@link ReportItem#theSameSource(ReportItem)}.
     */
    @Test
    public void testTheSameSource() {
        ReportItem item1 = new ReportItem("Settings Template", "Portal A");

        Assert.assertTrue(item1.theSameSource(new ReportItem("Settings Template", "Portal A")));
        
        Assert.assertFalse(item1.theSameSource(new ReportItem("Settings Template", "Portal B")));

        Assert.assertFalse(item1.theSameSource(new ReportItem("Settings Template 2", "Portal A")));
    }

    /**
     * Test method for {@link ReportItem#merge(ReportItem)}.
     */
    @Test
    public void testMerge() {
        ReportItem item1 = prepareAndRunItem();
        ReportItem item2 = prepareAndRunItem();
        item1.merge(item2);

        Assert.assertEquals(70000, item1.timeDiff);
        Assert.assertEquals(2, item1.times);
        Assert.assertEquals(Status.UPDATED, item1.status);
        Assert.assertEquals(BotError.NONE, item1.error);

        item1 = prepareAndRunItem();
        item2 = prepareAndRunItem(BotError.IO_ERROR);
        item1.merge(item2);

        Assert.assertEquals(70000, item1.timeDiff);
        Assert.assertEquals(2, item1.times);
        Assert.assertEquals(Status.ERROR, item1.status);
        Assert.assertEquals(BotError.IO_ERROR, item1.error);
    }

    /**
     * Test method for {@link ReportItem#toString()}.
     */
    @Test
    public void testToString() {
        ReportItem item = prepareAndRunItem();
        // Should not crash
        System.out.println(item.toString());
    }

}
