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
        return prepareAndRunItemImpl(null, false, false, false, true);
    }

    private ReportItem prepareAndRunItem(boolean finish) {
        return prepareAndRunItemImpl(null, false, false, false, finish);
    }

    private ReportItem prepareAndRunItem(BotError error) {
        return prepareAndRunItemImpl(error, true, false, false, true);
    }

    private ReportItem prepareAndRunItem(BotError error, boolean finish) {
        return prepareAndRunItemImpl(error, true, false, false, finish);
    }

    private ReportItem prepareAndRunItemWithUpdateError() {
        return prepareAndRunItemImpl(BotError.IO_ERROR, false, true, false, true);
    }

    private ReportItem prepareAndRunItemWithUpdateArchiveError() {
        return prepareAndRunItemImpl(BotError.IO_ERROR, false, false, true, true);
    }

    private ReportItem prepareAndRunItemImpl(BotError error, boolean getDataError,
            boolean updateNewPagesError, boolean updateArchiveError, boolean finish) {
        TestReportItem item = new TestReportItem("Settings Template", "Portal A");
        item.mockTime(1000L);
        item.mockTime(71000L);
        item.start();
        item.processed(1);
        if (!getDataError) {
            item.willUpdateNewPages();
            if (!updateNewPagesError) {
                item.newPagesUpdated(5);
                item.willUpdateArchive();
                if (!updateArchiveError) {
                    item.archiveUpdated(5);
                } else {
                    item.archiveUpdateError();
                }
            } else {
                item.newPagesUpdateError();
            }
        }
        if (error == null) {
            item.updated();
        } else {
            item.error(error);
        }
        if (finish) {
            item.end();
        }
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
                "Portal A\\s+1\\s+UPDATED\\s+00:01:10\\s+5\\s+Yes\\s+Yes\\(5\\)\\s+0\\s+NONE\\s*",
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
                "|-\n|1 ||align='left'| [[Portal A]] || 1 || 1 || {{Yes|UPDATED}} || 00:01:10 " +
                "|| 5 || {{Yes|Yes}} || {{Yes|Yes}} ( 5) || 0 || -",
                reportLine);
    }

    @Test
    public void testToStringWiki_errorGettingData() {
        ReportItem item = prepareAndRunItem(BotError.IO_ERROR);

        String reportLine = item.toStringWiki(1);

        Assert.assertEquals(
                "|-\n|1 ||align='left'| [[Portal A]] || 1 || 1 || {{No|ERROR}} || 00:01:10 " +
                "|| 0 || N/A || N/A || 0 || {{No|IO_ERROR}}",
                reportLine);
    }

    @Test
    public void testToStringWiki_errorUpdating() {
        ReportItem item = prepareAndRunItemWithUpdateError();

        String reportLine = item.toStringWiki(1);

        Assert.assertEquals(
                "|-\n|1 ||align='left'| [[Portal A]] || 1 || 1 || {{No|ERROR}} || 00:01:10 " +
                "|| 0 || {{No|Error}} || N/A || 0 || {{No|IO_ERROR}}",
                reportLine);
    }

    @Test
    public void testToStringWiki_errorUpdatingArchive() {
        ReportItem item = prepareAndRunItemWithUpdateArchiveError();

        String reportLine = item.toStringWiki(1);

        Assert.assertEquals(
                "|-\n|1 ||align='left'| [[Portal A]] || 1 || 1 || {{No|ERROR}} || 00:01:10 " +
                "|| 5 || {{Yes|Yes}} || {{No|Error}} || 0 || {{No|IO_ERROR}}",
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
        Assert.assertEquals(10, item1.newPagesFound);
        Assert.assertEquals(10, item1.pagesArchived);
        Assert.assertEquals(Status.UPDATED, item1.status);
        Assert.assertEquals(BotError.NONE, item1.error);

        item1 = prepareAndRunItem();
        item2 = prepareAndRunItem(BotError.IO_ERROR);
        item1.merge(item2);

        Assert.assertEquals(70000, item1.timeDiff);
        Assert.assertEquals(2, item1.times);
        Assert.assertEquals(5, item1.newPagesFound);
        Assert.assertEquals(5, item1.pagesArchived);
        Assert.assertEquals(Status.ERROR, item1.status);
        Assert.assertEquals(BotError.IO_ERROR, item1.error);

        item1 = prepareAndRunItem();
        item2 = prepareAndRunItem(BotError.IO_ERROR, false);
        item2.restart();
        item2.processed(2);
        item2.willUpdateNewPages();
        item2.newPagesUpdated(5);
        item2.willUpdateArchive();
        item2.archiveUpdated(5);
        item2.updated();
        item2.end();

        item1.merge(item2);

        Assert.assertEquals(70000, item1.timeDiff);
        Assert.assertEquals(2, item1.times);
        Assert.assertEquals(3, item1.tries);
        Assert.assertEquals(10, item1.newPagesFound);
        Assert.assertEquals(10, item1.pagesArchived);
        Assert.assertEquals(Status.UPDATED, item1.status);
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

    @Test
    public void testTryCount() {
        ReportItem item = prepareAndRunItem(false);
        item.processed(2);
        item.processed(3);
        item.end();
        Assert.assertEquals(3, item.tries);
    }
}
