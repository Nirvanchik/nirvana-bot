/**
 *  @(#)PageFormatterTest.java
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

package org.wikipedia.nirvana.nirvanabot;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.wikipedia.Wiki;
import org.wikipedia.Wiki.User;
import org.wikipedia.nirvana.BasicBot;
import org.wikipedia.nirvana.archive.ArchiveSettings;
import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.localization.TestLocalizationManager;
import org.wikipedia.nirvana.nirvanabot.NirvanaBot.BotGlobalSettings;
import org.wikipedia.nirvana.util.DateTools;
import org.wikipedia.nirvana.util.FileTools;
import org.wikipedia.nirvana.util.MockDateTools;
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

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Unit-tests for {@link PageFormatter}.
 *
 */
public class PageFormatterTest {
    public static final String BOT_SETTINGS_TEMPLATE = "User:Bot1:Template";
    public static final String PORTAL_SETTINGS_PAGE = "Portal:A/New pages/Settings";
    public static final String PORTAL_SETTINGS_TEXT = "{{User:Bot1:Template|param1=value1}}";
    public static final String HEADER = "{{HEADER}}\n";
    public static final String HEADER_WITH_VARIABLE =
            "{{HEADER|bot=%(бот)|project=%(проект)|page=%(страница)|archive=%(архив)}}\n";
    public static final String FOOTER = "\n{{FOOTER}}";
    public static final String FOOTER_WITH_VARIABLE =
            "\n{{FOOTER|bot=%(бот)|project=%(проект)|page=%(страница)|archive=%(архив)}}";
    public static final List<String> ITEMS = Arrays.asList(new String[] {
            "* [[Elephant]]", "* [[Lion]]", "* [[Buffalo]]"});

    public static final List<String> ITEMS_MANY = Arrays.asList(new String[] {
            "* [[Elephant]]", "* [[Lion]]", "* [[Buffalo]]",
            "* [[Dog]]", "* [[Cat]]", "* [[Cow]]", "* [[Sheep]]"});

    public static final String PAGELIST_MANY =
            "* [[Elephant]]\n* [[Lion]]\n* [[Buffalo]]\n* [[Dog]]\n" +
            "* [[Cat]]\n* [[Cow]]\n* [[Sheep]]";

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    private File cacheDir;

    PortalParam params;
    BotGlobalSettings globalSettings;
    NirvanaWiki wiki;
    MockSystemTime mockSystemTime = new MockSystemTime();

    static class MockSystemTime extends SystemTime {
        private Calendar time;

        public void setFromTimestamp(String timestamp) {
            time = DateTools.parseWikiTimestampUTC(timestamp);
        }

        @Override
        public Calendar now() {
            return (Calendar) time.clone();
        }

        public void add1Day() {
            time.add(Calendar.DAY_OF_MONTH, 1);
        }

        public void add1Minute() {
            time.add(Calendar.MINUTE, 1);
        }
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TestLocalizationManager.reset();
        Localizer.init(Localizer.NO_LOCALIZATION);
        BotVariables.init();
        DateTools.init("ru");
        BasicBot.initLogFromTest();
        PortalConfig.initStatics();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        TestLocalizationManager.reset();
        MockDateTools.reset();
    }

    @Before
    public void setUp() throws Exception {
        params = new PortalParam();
        params.page = "Portal:A/New pages";
        params.archSettings = new ArchiveSettings();
        params.archSettings.archive = "Portal:A/New pages/Archive";

        globalSettings = new BotGlobalSettings();

        wiki = Mockito.mock(NirvanaWiki.class);
        User user = Mockito.mock(User.class);
        when(user.getUsername()).thenReturn("Bot1");
        when(wiki.getCurrentUser()).thenReturn(user);

        mockSystemTime.setFromTimestamp("2020-02-20T21:00:00Z");

        File outDirDefult = tempDir.newFolder("tmp");
        FileTools.setDefaultOut(outDirDefult.getPath());
        FileTools.setDefaultEncoding(FileTools.UTF8);
        cacheDir = tempDir.newFolder("cache");
    }

    @After
    public void tearDown() throws Exception {
        TestPortalConfig.reset();
    }

    void withHeader() {
        params.header = HEADER;
    }

    void withFooter() {
        params.footer = FOOTER;
    }

    void withMiddle() {
        params.middle = "\n{{MIDDLE}}\n";
    }

    void withHeaderFooter() {
        withHeader();
        withFooter();
    }

    void withHeaderFooterMiddle() {
        withHeader();
        withFooter();
        withMiddle();
    }

    private void mockNewPagesTopRevision(boolean exists) throws IOException {
        Wiki.Revision r = null;
        if (exists) {
            OffsetDateTime datetime = OffsetDateTime.ofInstant(
                    Instant.ofEpochMilli(1583614545000L), ZoneId.systemDefault());
            r = wiki.new Revision(1, datetime, "page", "edit settings", "User1",
                    false, false, false, 1000);
        }
        when(wiki.getTopRevision(eq("Portal:A/New pages"))).thenReturn(r);
    }

    private PageFormatter create() {
        return new PageFormatter(params,
                BOT_SETTINGS_TEMPLATE, PORTAL_SETTINGS_PAGE, PORTAL_SETTINGS_TEXT,
                globalSettings, wiki, mockSystemTime, cacheDir.getPath());
    }

    /**
     * Test method for {@link PageFormatter#substPlaceholdersIfNeed()}.
     */
    @Test
    public void testSubstPlaceholders() {
        params.header = HEADER_WITH_VARIABLE;
        params.footer = FOOTER_WITH_VARIABLE;
        PageFormatter formatter = create();
        formatter.substPlaceholdersIfNeed();
        String result = formatter.formatPage(ITEMS);
        String expected = "" +
                "{{HEADER|bot=Bot1|project=A|page=Portal:A/New pages" +
                "|archive=Portal:A/New pages/Archive}}\n" +
                "* [[Elephant]]\n* [[Lion]]\n* [[Buffalo]]\n" +
                "{{FOOTER|bot=Bot1|project=A|page=Portal:A/New pages" +
                "|archive=Portal:A/New pages/Archive}}";
        Assert.assertEquals(expected, result);
    }

    /**
     * Test method for {@link PageFormatter#getHeaderFooterChanges()}.
     */
    @Test
    public void testGetHeaderFooterChanges_noPage() throws IOException {
        withHeaderFooter();
        PageFormatter formatter = create();
        mockNewPagesTopRevision(false);
        formatter.getHeaderFooterChanges();
        Assert.assertEquals(HEADER, formatter.headerLastUsed);
        Assert.assertEquals(FOOTER, formatter.footerLastUsed);
    }

    /**
     * Test method for {@link PageFormatter#getHeaderFooterChanges()}.
     */
    @Test
    public void testGetHeaderFooterChanges_noLastChangesOfSettings() throws IOException {
        withHeaderFooter();
        PageFormatter formatter = create();
        mockNewPagesTopRevision(true);
        when(wiki.getPageHistory(anyString(), anyObject(), anyObject(), anyBoolean()))
                .thenReturn(new Wiki.Revision[] {});
        formatter.getHeaderFooterChanges();
        Assert.assertEquals(HEADER, formatter.headerLastUsed);
        Assert.assertEquals(FOOTER, formatter.footerLastUsed);
    }

    /**
     * Test method for {@link PageFormatter#portalSettingsChangedAccordingToCache()}.
     */
    @Test
    public void doNotGoToWikiWhenSettingsNotChangedAccordingToCache() throws IOException {
        withHeaderFooter();
        PageFormatter formatter = create();
        mockNewPagesTopRevision(true);
        when(wiki.getPageHistory(anyString(), anyObject(), anyObject(), anyBoolean()))
                .thenReturn(new Wiki.Revision[] {});

        formatter.getHeaderFooterChanges();
        mockSystemTime.add1Minute();
        formatter.notifyNewPagesUpdated();

        Assert.assertEquals(HEADER, formatter.headerLastUsed);
        Assert.assertEquals(FOOTER, formatter.footerLastUsed);

        mockSystemTime.add1Day();
        formatter.getHeaderFooterChanges();
        Mockito.verify(wiki, times(1)).getTopRevision(anyString());
        Mockito.verify(wiki, times(1)).getPageHistory(anyString(), anyObject(), anyObject(),
                anyBoolean());
    }

    /**
     * Test method for {@link PageFormatter#getHeaderFooterChanges()}.
     */
    @Test
    public void testGetHeaderFooterChanges_settingsCreatedRecently() throws IOException {
        withHeaderFooter();
        PageFormatter formatter = create();
        mockNewPagesTopRevision(true);
        Wiki.Revision revSettings = Mockito.mock(Wiki.Revision.class);
        when(revSettings.getPrevious()).thenReturn(null);
        when(wiki.getPageHistory(anyString(), anyObject(), anyObject(), anyBoolean()))
                .thenReturn(new Wiki.Revision[] {revSettings});
        formatter.getHeaderFooterChanges();
        Assert.assertEquals(HEADER, formatter.headerLastUsed);
        Assert.assertEquals(FOOTER, formatter.footerLastUsed);
    }

    /**
     * Test method for {@link PageFormatter#getHeaderFooterChanges()}.
     */
    @Test
    public void testGetHeaderFooterChanges_settingsUpdated() throws IOException {
        withHeaderFooter();
        PageFormatter formatter = create();
        mockNewPagesTopRevision(true);
        Wiki.Revision revSettingsNew = Mockito.mock(Wiki.Revision.class);
        Wiki.Revision revSettingsOld = Mockito.mock(Wiki.Revision.class);
        String oldSettings =
                "{{User:Bot1:Template\n" +
                "|шапка={{OLD HEADER}}\\n\n" +
                "|подвал=\\n{{OLD FOOTER}}\n" +
                "}}\n";
        when(revSettingsOld.getText()).thenReturn(oldSettings);
        when(revSettingsNew.getPrevious()).thenReturn(revSettingsOld);
        when(wiki.getPageHistory(anyString(), anyObject(), anyObject(), anyBoolean()))
                .thenReturn(new Wiki.Revision[] {revSettingsNew});
        when(wiki.namespaceIdentifier(eq(Wiki.USER_NAMESPACE))).thenReturn("User");
        formatter.getHeaderFooterChanges();
        Assert.assertEquals("{{OLD HEADER}}\n", formatter.headerLastUsed);
        Assert.assertEquals("\n{{OLD FOOTER}}", formatter.footerLastUsed);
    }

    /**
     * Test method for {@link PageFormatter#stripBotsAllowString(java.lang.String)}.
     */
    @Test
    public void testStripBotsAllowString() {
        PageFormatter formatter = create();
        String oldText = "\n{{Bots|allow=Bot1}}\n* [[Apple]]\n* [[Potato]]";
        oldText = formatter.stripBotsAllowString(oldText);
        String expected = "\n\n* [[Apple]]\n* [[Potato]]";
        Assert.assertEquals(expected, oldText);
        
        String result = formatter.formatPage(ITEMS);
        expected = "{{Bots|allow=Bot1}}\n* [[Elephant]]\n* [[Lion]]\n* [[Buffalo]]";
        Assert.assertEquals(expected, result);
    }

    /**
     * Test method for {@link PageFormatter#formatPage(java.util.List)}.
     */
    @Test
    public void formatPage() {
        PageFormatter formatter = create();
        String result = formatter.formatPage(ITEMS);
        String expected = "* [[Elephant]]\n* [[Lion]]\n* [[Buffalo]]";
        Assert.assertEquals(expected, result);
    }

    /**
     * Test method for {@link PageFormatter#formatPage(java.util.List)}.
     */
    @Test
    public void formatPageWith2Columns() {
        withMiddle();
        PageFormatter formatter = create();
        String result = formatter.formatPage(ITEMS_MANY);
        String expected =
                "* [[Elephant]]\n* [[Lion]]\n* [[Buffalo]]\n* [[Dog]]\n" + 
                "{{MIDDLE}}\n" + 
                "* [[Cat]]\n* [[Cow]]\n* [[Sheep]]";
        Assert.assertEquals(expected, result);
    }

    /**
     * Test method for {@link PageFormatter#formatPage(java.util.List)}.
     */
    @Test
    public void formatPageWith2Columns1Item_withHeaderFooterMiddle() {
        withHeaderFooterMiddle();
        PageFormatter formatter = create();
        String result = formatter.formatPage(Arrays.asList(new String[] {"* [[Dog]]"}));
        String expected = "{{HEADER}}\n* [[Dog]]\n{{MIDDLE}}\n\n{{FOOTER}}";
        Assert.assertEquals(expected, result);
    }

    /**
     * Test method for {@link PageFormatter#stripDecoration(java.lang.String)}.
     */
    @Test
    public void testStripDecoration_all() {
        withHeaderFooterMiddle();
        PageFormatter formatter = create();
        String text =
                HEADER +
                "* [[Elephant]]\n* [[Lion]]\n* [[Buffalo]]\n* [[Dog]]\n" +
                "{{MIDDLE}}\n" +
                "* [[Cat]]\n* [[Cow]]\n* [[Sheep]]\n" +
                "{{FOOTER}}";
        String result = formatter.stripDecoration(text);
        Assert.assertEquals(PAGELIST_MANY, result);
    }

    @Test
    public void testStripDecoration_headerFooter() {
        withHeaderFooter();
        PageFormatter formatter = create();
        String text =
                HEADER +
                "* [[Elephant]]\n* [[Lion]]\n* [[Buffalo]]\n* [[Dog]]\n" +
                "* [[Cat]]\n* [[Cow]]\n* [[Sheep]]\n" +
                "{{FOOTER}}";
        String result = formatter.stripDecoration(text);
        Assert.assertEquals(PAGELIST_MANY, result);
    }

    @Test
    public void testStripDecoration_header() {
        withHeader();
        PageFormatter formatter = create();
        String text =
                HEADER +
                "* [[Elephant]]\n* [[Lion]]\n* [[Buffalo]]\n* [[Dog]]\n" +
                "* [[Cat]]\n* [[Cow]]\n* [[Sheep]]";
        String result = formatter.stripDecoration(text);
        Assert.assertEquals(PAGELIST_MANY, result);
    }

    @Test
    public void testStripDecoration_headerPageEmptySpace() {
        withHeader();
        PageFormatter formatter = create();
        String text =
                "\t  \n{{HEADER}}\n" +
                "* [[Elephant]]\n* [[Lion]]\n* [[Buffalo]]\n* [[Dog]]\n" +
                "* [[Cat]]\n* [[Cow]]\n* [[Sheep]]";
        String result = formatter.stripDecoration(text);
        Assert.assertEquals(PAGELIST_MANY, result);
    }

    @Test
    public void testStripDecoration_headerEmptySpacePageEmptySpace() {
        params.header = "\n{{HEADER}}\n";
        PageFormatter formatter = create();
        String text =
                "\t  \n\n{{HEADER}}\n" +
                "* [[Elephant]]\n* [[Lion]]\n* [[Buffalo]]\n* [[Dog]]\n" +
                "* [[Cat]]\n* [[Cow]]\n* [[Sheep]]";
        String result = formatter.stripDecoration(text);
        Assert.assertEquals(PAGELIST_MANY, result);
    }

    @Test
    public void testStripDecoration_headerWithVariable() {
        params.header = HEADER_WITH_VARIABLE;
        PageFormatter formatter = create();
        String text =
                "{{HEADER|bot=Bot1|project=A|page=Portal:A/New pages" +
                "|archive=Portal:A/New pages/Archive}}\n" +
                "* [[Elephant]]\n* [[Lion]]\n* [[Buffalo]]\n* [[Dog]]\n" +
                "* [[Cat]]\n* [[Cow]]\n* [[Sheep]]";
        String result = formatter.stripDecoration(text);
        Assert.assertEquals(PAGELIST_MANY, result);
    }

    @Test
    public void testStripDecoration_headerWithUpdatedVariable() {
        params.header = "{{HEADER|bot=%(бот)|project=%(проект)|date=%(дата)}}\n";
        PageFormatter formatter = create();
        String text =
                "{{HEADER|bot=Bot1|project=A|date=13 ноября 2014}}\n" +
                "* [[Elephant]]\n* [[Lion]]\n* [[Buffalo]]\n* [[Dog]]\n" +
                "* [[Cat]]\n* [[Cow]]\n* [[Sheep]]";
        String result = formatter.stripDecoration(text);
        Assert.assertEquals(PAGELIST_MANY, result);
    }

    @Test
    public void testStripDecoration_footer() {
        withFooter();
        PageFormatter formatter = create();
        String text =
                "* [[Elephant]]\n* [[Lion]]\n* [[Buffalo]]\n* [[Dog]]\n" +
                "* [[Cat]]\n* [[Cow]]\n* [[Sheep]]\n" +
                "{{FOOTER}}";
        String result = formatter.stripDecoration(text);
        Assert.assertEquals(PAGELIST_MANY, result);
    }

    @Test
    public void testStripDecoration_footerPageEmptySpace() {
        withFooter();
        PageFormatter formatter = create();
        String text =
                "* [[Elephant]]\n* [[Lion]]\n* [[Buffalo]]\n* [[Dog]]\n" +
                "* [[Cat]]\n* [[Cow]]\n* [[Sheep]]\n" +
                "{{FOOTER}}\n\t  ";
        String result = formatter.stripDecoration(text);
        Assert.assertEquals(PAGELIST_MANY, result);
    }

    @Test
    public void testStripDecoration_footerEmptySpacePageEmptySpace() {
        params.footer = "\n{{FOOTER}}\n";
        PageFormatter formatter = create();
        String text =
                "* [[Elephant]]\n* [[Lion]]\n* [[Buffalo]]\n* [[Dog]]\n" +
                "* [[Cat]]\n* [[Cow]]\n* [[Sheep]]\n" +
                "{{FOOTER}}\n\t  \n";
        String result = formatter.stripDecoration(text);
        Assert.assertEquals(PAGELIST_MANY, result);
    }

    @Test
    public void testStripDecoration_footerWithVariable() {
        params.footer = FOOTER_WITH_VARIABLE;
        PageFormatter formatter = create();
        String text =
                "* [[Elephant]]\n* [[Lion]]\n* [[Buffalo]]\n* [[Dog]]\n" +
                "* [[Cat]]\n* [[Cow]]\n* [[Sheep]]\n" +
                "{{FOOTER|bot=Bot1|project=A|page=Portal:A/New pages" +
                "|archive=Portal:A/New pages/Archive}}";
        String result = formatter.stripDecoration(text);
        Assert.assertEquals(PAGELIST_MANY, result);
    }
}
