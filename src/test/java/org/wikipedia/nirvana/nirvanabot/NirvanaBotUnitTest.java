/**
 *  @(#)NirvanaBotUnitTest.java
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

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.wikipedia.Wiki;
import org.wikipedia.nirvana.base.BasicBot;
import org.wikipedia.nirvana.base.BotFatalError;
import org.wikipedia.nirvana.base.BotTemplateParser;
import org.wikipedia.nirvana.error.ArchiveUpdateFailure;
import org.wikipedia.nirvana.error.DangerousEditException;
import org.wikipedia.nirvana.error.InvalidLineFormatException;
import org.wikipedia.nirvana.error.ServiceError;
import org.wikipedia.nirvana.localization.TestLocalizationManager;
import org.wikipedia.nirvana.nirvanabot.NirvanaBot.NewPagesData;
import org.wikipedia.nirvana.nirvanabot.report.BotReporter;
import org.wikipedia.nirvana.nirvanabot.serviceping.CatscanService;
import org.wikipedia.nirvana.nirvanabot.serviceping.InternetService;
import org.wikipedia.nirvana.nirvanabot.serviceping.NetworkInterface;
import org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService.Status;
import org.wikipedia.nirvana.nirvanabot.serviceping.ServicePinger;
import org.wikipedia.nirvana.nirvanabot.serviceping.TestServiceManager;
import org.wikipedia.nirvana.nirvanabot.serviceping.WikiService;
import org.wikipedia.nirvana.testing.MockSystemTime;
import org.wikipedia.nirvana.testing.TestError;
import org.wikipedia.nirvana.util.DateTools;
import org.wikipedia.nirvana.util.MockDateTools;
import org.wikipedia.nirvana.util.SystemTime;
import org.wikipedia.nirvana.wiki.CatScanTools;
import org.wikipedia.nirvana.wiki.MockCatScanTools;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;

/**
 * Unit-tests for {@link NirvanaBot}.
 * 
 * Here we testing secondary code like: error handling, reporting, utilities, retries, etc.
 * 
 * The primary code (portal pages updating) is tested in {@link NirvanaBotTest}.
 *
 */
public class NirvanaBotUnitTest {

    private static class MockWikiService extends WikiService {

        public MockWikiService(String name, NirvanaWiki wiki) {
            super(name, wiki);
        }

        @Override
        public Status isOk() throws InterruptedException {
            return Status.OK;
        }
    }

    /**
     * {@link NirvanaBot} wrapper class with most of things mocked.
     *
     * Note: ServiceManager and ServicePinger are real.
     *
     */
    private static class TestNirvanaBot extends NirvanaBot {
        public static final String NEW_PAGES_SETTINGS_MINIMAL = 
                "{{User:TestBot:Template1\n" +
                "|тип = Новые статьи\n" +
                "|категории = Собаки\n" +
                "|страница = Portal:Portal 1/New pages\n" +
                "}}\n";
        public static final String NEW_PAGES_SETTINGS_NOTYPE = 
                "{{User:TestBot:Template1\n" +
                "|категории = Собаки\n" +
                "|страница = Portal:Portal 1/New pages\n" +
                "}}\n";
        public final NirvanaWiki mainWiki;
        public final NirvanaWiki commonsWiki;

        public final NetworkInterface mockServiceNetwork;
        public final InternetService mockServiceInternet;
        public final CatscanService mockCatscanService;
        public final WikiService mockWikiService;
        public final ServicePinger servicePinger;

        public final BotReporter mockReporter;

        public final String [] mockPortalTaskSettingsPages;

        // Note: for mocking catscan we use CatScanTools.resetFromTest() which disables network.
        public final CatScanTools.Service catscan = CatScanTools.Service.PETSCAN;

        public long timeInMillis = 0;

        public final NewPages mockNewPages;
        
        @Nullable
        public Consumer<NewPagesData> createPortalModuleHook;

        public TestNirvanaBot(int flags) throws TestError, InterruptedException, IOException,
                LoginException, ServiceError, BotFatalError, InvalidLineFormatException,
                DangerousEditException, ArchiveUpdateFailure {
            super(flags);

            // WARNING: This mock is heavy and slow: 500..600 millis.
            mainWiki = Mockito.mock(NirvanaWiki.class);
            wiki = mainWiki;
            wiki.login("login", "password");
            commonsWiki = Mockito.mock(NirvanaWiki.class);
            when(mainWiki.namespaceIdentifier(eq(Wiki.USER_NAMESPACE))).thenReturn("User");
            mockPortalTaskSettingsPages = new String [] {
                "Portal:Portal 1/New pages/settings",
                "Portal:Portal 2/New pages/settings",
                "Portal:Portal 3/New pages/settings"
            };
            when(mainWiki
                    .whatTranscludesHere(eq("User:TestBot:Template1"), eq(Wiki.ALL_NAMESPACES)))
                    .thenReturn(mockPortalTaskSettingsPages);
            for (String settingsPage: mockPortalTaskSettingsPages) {
                when(mainWiki.getPageText(eq(settingsPage))).thenReturn(
                        NEW_PAGES_SETTINGS_MINIMAL);
            }
            when(mainWiki.allowEditsByCurrentBot(Mockito.anyString())).thenReturn(true);

            mockServiceNetwork = Mockito.mock(NetworkInterface.class);
            when(mockServiceNetwork.getName()).thenReturn("Network");
            when(mockServiceNetwork.isOk()).thenReturn(Status.OK);

            mockServiceInternet = Mockito.mock(InternetService.class);
            when(mockServiceInternet.getName()).thenReturn("Internet");
            when(mockServiceInternet.isOk()).thenReturn(Status.OK);

            mockWikiService = new MockWikiService("Wiki", mainWiki);

            mockCatscanService = Mockito.mock(CatscanService.class);
            when(mockCatscanService.getName()).thenReturn("Catscan");

            mockReporter = Mockito.mock(BotReporter.class);

            servicePinger = new ServicePinger(
                    new MockSystemTime(),  // Use SystemTime object of bot
                    mockServiceNetwork,
                    mockServiceInternet,
                    mockWikiService,
                    mockCatscanService);

            mockNewPages = Mockito.mock(NewPages.class);

            properties.setProperty("new-pages-template", "User:TestBot:Template1");
        }
        
        protected SystemTime initSystemTime() {
            return new SystemTime() {
                public Calendar now() {
                    Calendar cal = DateTools.parseWikiTimestampUTC("2020-01-24T19:20:40Z");
                    return cal;
                }
            };
        }
        
        public void setProperty(String key, String value) {
            properties.setProperty(key, value);
        }

        @Override
        protected NirvanaWiki createWiki(String domain, String path, String protocol,
                String language) {
            return mainWiki;
        }

        @Override
        protected NirvanaWiki createCommonsWiki() {
            return commonsWiki;
        }

        @Override
        protected ServiceManager createServiceManager() throws BotFatalError {
            ServiceManager manager = new TestServiceManager(mockServiceInternet, mockWikiService,
                    mockCatscanService, servicePinger, catscan);
            return manager;
        }

        @Override
        protected BotReporter createReporter(String cacheDir) {
            return mockReporter;
        }

        @Override
        protected boolean createPortalModule(BotTemplateParser botTemplateParser, 
                Map<String, String> options, NewPagesData data) {
            boolean retValue = super.createPortalModule(botTemplateParser, options, data);
            if (createPortalModuleHook != null) {
                createPortalModuleHook.accept(data);
            }
            data.portalModule = mockNewPages;
            return retValue;
        }

        public void enableStatusReport() {
            enableStatus = true;
        }

        public void enableDetailedWikiReport() {
            enableReport = true;
            REPORT_FORMAT = "wiki";
        }

        public void enableDetailedTextReport() {
            enableReport = true;
            REPORT_FORMAT = "txt";
        }
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TestLocalizationManager.reset();
        MockCatScanTools.reset();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        NirvanaBot.UPDATE_PAUSE = 0;
    }

    @After
    public void tearDown() throws Exception {
        MockCatScanTools.reset();
        TestLocalizationManager.reset();
        MockDateTools.reset();
        TestPortalConfig.reset();
    }

    @Test
    public void testUpdateStatusReport() throws Exception {
        TestNirvanaBot bot = new TestNirvanaBot(BasicBot.FLAG_DEFAULT_LOG);
        bot.enableStatusReport();
        when(bot.mockCatscanService.isOk()).thenReturn(Status.OK);
        when(bot.mockNewPages.update(anyObject(), anyObject(), anyString()))
                .thenReturn(true);

        bot.initLog();
        bot.loadCustomProperties(Collections.emptyMap());
        bot.go();

        verify(bot.mockReporter).updateStartStatus(anyString(), anyString());
        verify(bot.mockReporter).updateEndStatus(anyString(), anyString());
    }

    @Test
    public void testReloginCalledAfterFatalStopBeforeStatusReport() throws Exception {
        TestNirvanaBot bot = new TestNirvanaBot(BasicBot.FLAG_DEFAULT_LOG);
        bot.enableStatusReport();
        when(bot.mockCatscanService.isOk()).thenReturn(Status.OK).thenReturn(Status.FAIL);
        when(bot.mockNewPages.update(anyObject(), anyObject(), anyString()))
                .thenThrow(new IOException("Fake exception from test"));

        bot.initLog();
        bot.loadCustomProperties(Collections.emptyMap());
        bot.go();

        verify(bot.mockReporter).updateStartStatus(anyString(), anyString());
        verify(bot.mockReporter).updateEndStatus(anyString(), anyString());
        verify(bot.mainWiki).relogin();
    }

    @Test
    public void testReloginCalledAfterFatalStopBeforeDetailedReport() throws Exception {
        TestNirvanaBot bot = new TestNirvanaBot(BasicBot.FLAG_DEFAULT_LOG);
        bot.enableDetailedWikiReport();
        when(bot.mockCatscanService.isOk()).thenReturn(Status.OK).thenReturn(Status.FAIL);
        when(bot.mockNewPages.update(anyObject(), anyObject(), anyString()))
                .thenThrow(new IOException("Fake exception from test"));

        bot.initLog();
        bot.loadCustomProperties(Collections.emptyMap());
        bot.go();

        verify(bot.mockReporter).reportWiki(anyString(), eq(true), anyObject());
        verify(bot.mainWiki).relogin();
    }

    @Test
    public void testReloginCalledAfterFailureAndLongWaiting() throws Exception {
        TestNirvanaBot bot = new TestNirvanaBot(BasicBot.FLAG_DEFAULT_LOG);
        when(bot.mockCatscanService.isOk())
                .thenReturn(Status.OK)
                .thenReturn(Status.FAIL)
                .thenReturn(Status.FAIL)
                .thenReturn(Status.FAIL)
                .thenReturn(Status.OK);
        when(bot.mockNewPages.update(anyObject(), anyObject(), anyString()))
                .thenThrow(new IOException("Fake exception from test"))
                .thenReturn(true);

        bot.initLog();
        bot.loadCustomProperties(Collections.emptyMap());
        bot.go();

        verify(bot.mainWiki).relogin();
    }

    @Test
    public void testReadOverriddenProperties() throws Exception {
        TestNirvanaBot bot = new TestNirvanaBot(BasicBot.FLAG_DEFAULT_LOG);
        bot.enableStatusReport();
        when(bot.mockCatscanService.isOk()).thenReturn(Status.OK);
        when(bot.mockNewPages.update(anyObject(), anyObject(), anyString()))
                .thenReturn(true);

        bot.setProperty("overriden-properties-page", "User:TestBot:GlobalSettings");

        String globalSettings = "{{{User:TestBot:Template1 (параметры по умолчанию)\n" + 
                "|тип = список новых статей\n" + 
                "|элементов = 19\n" + 
                "|часов = 349\n" + 
                "|глубина = 5\n" + 
                "|формат элемента = * [[%(название)]] - (создал (%автор))\n" + 
                "|пространство имён = 10\n" + 
                "|шапка = ШАПКА\n" + 
                "|подвал = \\n<noinclude>\n" + 
                "[[Категория:Википедия:Списки новых статей по темам|{{PAGENAME}}]]\n" + 
                "</noinclude>\n" + 
                "|разделитель = \",\"\n" + 
                "|поиск картинки = image file,Фото,портрет,Изображение\n" + 
                "|шаблоны запрещенных картинок = Disputed-fairuse, ОДИ, Несвободный файл/ОДИ\n" + 
                "|сервис = auto\n" + 
                "|сервис по умолчанию = petscan\n" + 
                "|быстрый режим = да\n" + 
                "|попытки = 4\n" + 
                "|попытки catscan = 5\n" + 
                "}}";
        when(bot.mainWiki.getPageText(eq("User:TestBot:GlobalSettings")))
                .thenReturn(globalSettings);
        when(bot.mainWiki
                .whatTranscludesHere(eq("User:TestBot:Template1"), eq(Wiki.ALL_NAMESPACES)))
                .thenReturn(new String[] {"Portal:Portal 1/New pages/settings"});
        when(bot.mainWiki.getPageText(eq("Portal:Portal 1/New pages/settings")))
                .thenReturn(TestNirvanaBot.NEW_PAGES_SETTINGS_NOTYPE);
        bot.createPortalModuleHook = new Consumer<NirvanaBot.NewPagesData>() {

            @Override
            public void accept(NewPagesData data) {
                PortalParam param = data.param;
                Assert.assertEquals("список новых статей", data.type);
                Assert.assertEquals(19, param.maxItems);
                Assert.assertEquals(349, param.hours);
                Assert.assertEquals(5, param.depth);
                Assert.assertEquals("* [[%(название)]] - (создал (%автор))", param.format);
                Assert.assertEquals(10, param.ns);
                Assert.assertEquals("ШАПКА", param.header);
                Assert.assertEquals("\n<noinclude>\n"
                        + "[[Категория:Википедия:Списки новых статей по темам|{{PAGENAME}}]]\n"
                        + "</noinclude>", param.footer);
                Assert.assertEquals(",", param.delimeter);
                Assert.assertEquals("image file,Фото,портрет,Изображение", param.imageSearchTags);
                Assert.assertEquals("Disputed-fairuse, ОДИ, Несвободный файл/ОДИ",
                        param.fairUseImageTemplates);
                Assert.assertNotNull(param.service);
                Assert.assertTrue(param.service instanceof CatScanTools.Service);
                Assert.assertEquals(4, param.tryCount);
                Assert.assertEquals(5, param.catscanTryCount);
                Assert.assertEquals(true, param.fastMode);
            }
        };

        bot.initLog();
        bot.loadCustomProperties(Collections.emptyMap());
        bot.go();

    }

    @Test
    public void testTextReport() throws Exception {
        TestNirvanaBot bot = new TestNirvanaBot(BasicBot.FLAG_DEFAULT_LOG);
        bot.enableDetailedTextReport();
        when(bot.mockCatscanService.isOk()).thenReturn(Status.OK);
        when(bot.mockNewPages.update(anyObject(), anyObject(), anyString()))
                .thenReturn(true);

        bot.setProperty("statistics-file", "report_%(date)_%(time)_%(launch_number).txt");

        bot.initLog();
        bot.loadCustomProperties(Collections.emptyMap());
        bot.go();

        verify(bot.mockReporter).reportTxt(bot.getOutDir() + "/report_2020-01-24_19-20-40_1.txt");
    }
}
