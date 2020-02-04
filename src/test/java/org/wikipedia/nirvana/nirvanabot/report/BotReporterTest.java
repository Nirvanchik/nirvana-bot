/**
 *  @(#)BotReporterTest.java
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

import org.wikipedia.nirvana.localization.LocalizedTemplate;
import org.wikipedia.nirvana.localization.TestLocalizationManager;
import org.wikipedia.nirvana.nirvanabot.NirvanaBot.BotError;
import org.wikipedia.nirvana.util.FileTools;
import org.wikipedia.nirvana.wiki.MockNirvanaWiki;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Unit-tests for {@link BotReporter}.
 *
 */
public class BotReporterTest {

    private MockNirvanaWiki mockWiki;
    private NirvanaWiki wiki;
    
    private TimeZone timeZone;
    
    public static final String TEST_DATA_DIR = "src/test/resources/test_data/reports/";
    
    static class TestBotReporter extends BotReporter {
        private LinkedList<Calendar> mockTimeQueue = new LinkedList<>();
        private String mockedWikiReport;
        private String mockedTxtReport;

        /**
         * Main constructor.
         */
        public TestBotReporter(NirvanaWiki wiki, String cacheDir, int capacity, boolean started,
                String version, String preambulaFile) {
            super(wiki, cacheDir, capacity, started, version, preambulaFile);
        }
        
        @Override
        protected Calendar getCurrentTime() {
            Assert.assertFalse("getCurrentTime() was called but there is no mocked times in buffer",
                    mockTimeQueue.isEmpty());
            return mockTimeQueue.remove();
        }
        
        public void mockTime(Calendar time) {
            mockTimeQueue.add(time);
        }
        
        public void mockWikiReport(String report) {
            mockedWikiReport = report;
        }

        public void mockTxtReport(String report) {
            mockedTxtReport = report;
        }
        
        @Override
        public String printReportWiki() {
            if (mockedWikiReport != null) {
                return mockedWikiReport;
            }
            return super.printReportWiki();
        }
        
        @Override
        public String printReportTxt() {
            if (mockedTxtReport != null) {
                return mockedTxtReport;
            }
            return super.printReportTxt();
        }

        public ReportItem mockProcessedItem(String name) {
            ReportItem item = BotReporterTest.mockProcessedItem(name);
            add(item);
            return item;
        }

        public ReportItem mockUpdatedItem(String name) {
            ReportItem item = BotReporterTest.mockUpdatedItem(name);
            add(item);
            return item;
        }
        
        public ReportItem mockErrorItem(String name, BotError error) {
            ReportItem item = BotReporterTest.mockErrorItem(name, error);
            add(item);
            return item;
        }
        
    }
    
    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();
    
    private File cacheDir;
    private File preambulaFile;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        Map<String, String> translations = new HashMap<>();
        translations.put("Бот запущен", "Bot started");
        translations.put("Бот остановлен", "Bot stopped");
        translations.put("Да", "Yes");
        translations.put("Нет", "No");
        translations.put("портал/проект", "portal/project");
        translations.put("проходов", "launches");
        translations.put("статус", "status");
        translations.put("время", "time");
        translations.put("новых статей", "new pages");
        translations.put("список обновлен", "list updated");
        translations.put("статей в архив", "pages in archive");
        translations.put("архив обновлен", "archive updated");
        translations.put("ошибок", "errors");
        translations.put("ошибка", "error");
        
        Map<String, LocalizedTemplate> localizedTemplates = new HashMap<>();
        localizedTemplates.put("Да", new LocalizedTemplate("Да", "Yes"));
        localizedTemplates.put("Нет", new LocalizedTemplate("Нет", "No"));
        TestLocalizationManager.init(translations, localizedTemplates);
        
        mockWiki = new MockNirvanaWiki("en.wikipedia.org");

        wiki = Mockito.spy(mockWiki);
        MockitoAnnotations.initMocks(this);
        
        cacheDir = tmpDir.newFolder();
        preambulaFile = tmpDir.newFile("preambula.txt");
        FileTools.writeFile("PREAMBULA", preambulaFile.getPath());
        
        timeZone = TimeZone.getTimeZone("Europe/London");

    }

    @After
    public void tearDown() throws Exception {
        TestLocalizationManager.reset();
    }

    private static ReportItem newReportItem(String portal) {
        return new ReportItem("Template 1", portal);
    }

    private static ReportItem mockProcessedItem(String name) {
        ReportItem item = newReportItem(name);
        item.processed();
        return item;
    }
    
    private static ReportItem mockProcessedItem(List<ReportItem> list, String name) {
        ReportItem item = mockProcessedItem(name);
        list.add(item);
        return item;
    }

    private static ReportItem mockUpdatedItem(String name) {
        ReportItem item = newReportItem(name);
        item.processed();
        item.updated();
        return item;
    }

    private static ReportItem mockUpdatedItem(List<ReportItem> list, String name) {
        ReportItem item = mockUpdatedItem(name);
        list.add(item);
        return item;
    }
    
    private static ReportItem mockErrorItem(String name, BotError error) {
        ReportItem item = newReportItem(name);
        item.processed();
        item.error(error);
        return item;
    }

    private static ReportItem mockErrorItem(List<ReportItem> list, String name, BotError error) {
        ReportItem item = mockErrorItem(name, error);
        list.add(item);
        return item;
    }
    
    private void mockStartAndEndTime(TestBotReporter reporter) {
        Calendar time1 = Calendar.getInstance(timeZone, Locale.ENGLISH);
        time1.set(2020, 1, 1, 5, 10, 0);
        reporter.mockTime(time1);
        Calendar time2 = (Calendar) time1.clone();
        time2.add(Calendar.MINUTE, 10);        
        reporter.mockTime(time2);
    }

    /**
     * Test method for {@link BotReporter#report()}.
     */
    @Test
    public void testReport() {
        // Nothing to test
    }

    /**
     * Test method for {@link BotReporter#updateStartStatus(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testUpdateStartStatus() throws Exception {
        TestBotReporter reporter = new TestBotReporter(wiki, cacheDir.getPath(), 10, false,
                "1.0", preambulaFile.getPath());
        Calendar time1 = Calendar.getInstance(timeZone, Locale.ENGLISH);
        time1.set(2020, 1, 1, 5, 10, 0);
        reporter.mockTime(time1);
        reporter.botStarted(true);
        reporter.updateStartStatus("Status page", "StatusTemplate");
        String expected = "{{StatusTemplate|status=1|starttime=2020-02-01 05:10:00|version=1.0}}"; 
        mockWiki.validateEdit("Status page", expected);
    }

    /**
     * Test method for {@link BotReporter#updateEndStatus(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testUpdateEndStatus() throws Exception {
        TestBotReporter reporter = new TestBotReporter(wiki, cacheDir.getPath(), 10, false,
                "1.0", preambulaFile.getPath());

        mockStartAndEndTime(reporter);
    
        reporter.botStarted(true);
        reporter.portalChecked();  // 1
        
        reporter.portalChecked();  // 2
        reporter.portalProcessed();
        
        reporter.portalChecked();  // 3
        reporter.portalProcessed();

        reporter.portalChecked();  // 4
        reporter.portalProcessed();
        reporter.portalUpdated();
    
        reporter.portalChecked();  // 5
        reporter.portalProcessed();
        reporter.portalUpdated();

        reporter.portalChecked();  // 6
        reporter.portalProcessed();
        reporter.portalError();
        
        reporter.addToTotal(6);
        
        reporter.botFinished(true);
        reporter.updateEndStatus("Status page", "StatusTemplate");
        
        String extectedStatusWikiText =
                "{{StatusTemplate|status=0" +
                "|starttime=2020-02-01 05:10:00|endtime=2020-02-01 05:20:00|time=0 h 10 m 0 s" +
                "|total=6|checked=6|processed=5" +
                "|updated=2|errors=1|version=1.0}}";
        mockWiki.validateEdit("Status page", extectedStatusWikiText);
    }

    /**
     * Test method for {@link BotReporter#updateStatus()}.
     */
    @Test
    public void testUpdateStatus() {
        // Nothing to test
    }

    @Test
    public void testPrintReportWiki() throws IOException {
        TestBotReporter reporter = new TestBotReporter(wiki, cacheDir.getPath(), 10, false,
                "1.0", preambulaFile.getPath());
        mockStartAndEndTime(reporter);
        
        reporter.botStarted(true);
        reporter.mockUpdatedItem("Portal 1");
        reporter.mockProcessedItem("Portal 2");
        reporter.mockErrorItem("Portal 3", BotError.IO_ERROR);
        reporter.botFinished(true);
        String report = reporter.printReportWiki();
        String expected = FileTools.readWikiFileFromPath(TEST_DATA_DIR + "wiki_report.txt").trim();
        Assert.assertEquals(expected, report);
    }
    
    @Test
    public void testPrintReportTxt() throws IOException {
        TestBotReporter reporter = new TestBotReporter(wiki, cacheDir.getPath(), 10, false,
                "1.0", preambulaFile.getPath());
        mockStartAndEndTime(reporter);
        
        reporter.botStarted(true);
        reporter.mockUpdatedItem("Portal 1");
        reporter.mockProcessedItem("Portal 2");
        reporter.mockErrorItem("Portal 3", BotError.IO_ERROR);
        reporter.botFinished(true);
        String report = reporter.printReportTxt();
        String expected = FileTools.readFile(TEST_DATA_DIR + "txt_report.txt");
        Assert.assertEquals(expected, report);
    }

    /**
     * Test method for {@link BotReporter#reportTxt(java.lang.String)}.
     */
    @Test
    public void testReportTxt() throws IOException {
        TestBotReporter reporter = new TestBotReporter(wiki, cacheDir.getPath(), 10, false,
                "1.0", preambulaFile.getPath());
        mockStartAndEndTime(reporter);
        
        reporter.botStarted(true);
        reporter.botFinished(true);
        
        reporter.mockTxtReport("TEXT REPORT");
        
        File reportFile = tmpDir.newFile("report.txt");
        reporter.reportTxt(reportFile.getPath());
        
    }

    /**
     * Test method for {@link BotReporter#reportWiki(java.lang.String, boolean)}.
     */
    @Test
    public void testReportWiki() {
        // Not testing this specially. This method is going to be refactored.
    }

    /**
     * Test method for {@link BotReporter#merge(java.util.List)}.
     */
    @Test
    public void testMerge() {
        TestBotReporter reporter1 = new TestBotReporter(wiki, cacheDir.getPath(), 10, false,
                "1.0", preambulaFile.getPath());
        
        List<ReportItem> list2 = new ArrayList<>();
        
        reporter1.mockUpdatedItem("Portal 1");
        reporter1.mockProcessedItem("Portal 2");
        reporter1.mockUpdatedItem("Portal 3");
        reporter1.mockErrorItem("Portal 4", BotError.IO_ERROR);

        mockUpdatedItem(list2, "Portal 2");
        mockErrorItem(list2, "Portal 3", BotError.IO_ERROR);
        mockUpdatedItem(list2, "Portal 4");
        mockUpdatedItem(list2, "Portal 5");
        
        reporter1.merge(list2);
        
        ReportItem item1 = mockUpdatedItem("Portal 1");
        ReportItem item2 = mockUpdatedItem("Portal 2");
        item2.times = 2;
        ReportItem item3 = mockErrorItem("Portal 3", BotError.IO_ERROR);
        item3.times = 2;
        ReportItem item4 = mockErrorItem("Portal 4", BotError.IO_ERROR);
        item4.times = 2;
        ReportItem item5 = mockUpdatedItem("Portal 5");
        Assert.assertArrayEquals(
                new ReportItem[] {item1, item2, item3, item4, item5},
                reporter1.reportItems.toArray(new ReportItem[] {}));
    }

    /**
     * Test method for {@link BotReporter#doReportWiki(java.lang.String)}.
     */
    @Test
    public void testDoReportWiki() {
        TestBotReporter reporter = new TestBotReporter(wiki, cacheDir.getPath(), 10, false,
                "1.0", preambulaFile.getPath());
        mockStartAndEndTime(reporter);
        
        reporter.botStarted(true);
        reporter.botFinished(true);
        
        reporter.mockWikiReport("WIKI TEXT");
        
        reporter.doReportWiki("Some page");
        
        mockWiki.validateEdit("Some page", "WIKI TEXT");
    }

    /**
     * Test method for {@link BotReporter#printTimeDiff(java.util.Calendar, java.util.Calendar)}.
     */
    @Test
    public void testPrintTimeDiff() {
        Calendar time1 = Calendar.getInstance(timeZone, Locale.ENGLISH);
        time1.set(2020, 1, 1, 5, 10, 0);
        Calendar time2 = Calendar.getInstance(timeZone, Locale.ENGLISH);
        time2.set(2020, 1, 1, 7, 15, 30);
        String timeDiff = BotReporter.printTimeDiff(time1, time2);
        Assert.assertEquals("2 h 5 m 30 s", timeDiff);
    }

    /**
     * Test method for {@link BotReporter#printTimeDiff(java.util.Calendar, java.util.Calendar)}.
     */
    @Test
    public void testPrintTimeDiff_beyondMidnight() {
        Calendar time1 = Calendar.getInstance(timeZone, Locale.ENGLISH);
        time1.set(2020, 1, 1, 22, 55, 30);
        Calendar time2 = Calendar.getInstance(timeZone, Locale.ENGLISH);
        time2.set(2020, 1, 2, 1, 15, 30);
        String timeDiff = BotReporter.printTimeDiff(time1, time2);
        Assert.assertEquals("2 h 20 m 0 s", timeDiff);
    }
    
    /**
     * Test method for {@link BotReporter#save(java.io.File)} and
     * {@link BotReporter#load(java.io.File)}.
     */
    @Test
    public void testSaveAndLoad() throws IOException {
        TestBotReporter reporter1 = new TestBotReporter(wiki, cacheDir.getPath(), 10, false,
                "1.0", preambulaFile.getPath());
        TestBotReporter reporter2 = new TestBotReporter(wiki, cacheDir.getPath(), 10, false,
                "1.1", preambulaFile.getPath());
        
        ReportItem item1 = reporter1.mockProcessedItem("Portal 1");
        ReportItem item2 = reporter1.mockUpdatedItem("Portal 2");
        ReportItem item3 = reporter1.mockErrorItem("Portal 3", BotError.IO_ERROR);
        
        File reportFile = tmpDir.newFile("report.json");
        reporter1.save(reportFile);
        Assert.assertTrue(reportFile.exists());
        
        List<ReportItem> report2 = reporter2.load(reportFile);
        
        Assert.assertArrayEquals(
                new ReportItem[]{item1, item2, item3},
                report2.toArray(new ReportItem[] {}));
    }

    @Test
    public void testLogStatus() {
        TestBotReporter reporter = new TestBotReporter(wiki, cacheDir.getPath(), 10, false,
                "1.0", preambulaFile.getPath());
        reporter.logStatus();
    }

}
