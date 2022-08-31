/**
 *  @(#)BasicBotTest.java
 *  Copyright © 2019 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.base;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.wikipedia.nirvana.nirvanabot.BotFatalError;
import org.wikipedia.nirvana.nirvanabot.BotSettingsError;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.security.auth.login.FailedLoginException;

/**
 * Unit tests for {@link BasicBot}.
 */
public class BasicBotTest {
    public static final String BOT_CONFIG_DEFAULT = "test_data/bot_config_ru_default.xml";
    public static final String BOT_CONFIG_NO_ACCOUNT = "test_data/bot_config_ru_no_account.xml";
    public static final String BOT_CONFIG_EXT_ACCOUNT_CONF = 
            "test_data/bot_config_ru_ext_account_conf.xml";
    public static final String BOT_CONFIG_WITH_LOGGING_CONF =
            "test_data/bot_config_ru_log4j_conf.xml";

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();
    
    private static class TestBot extends BasicBot {
        public BasicBot mocked = Mockito.mock(BasicBot.class);
        public Map<String, String> interceptLaunchParams;
        public NirvanaWiki mockWiki = Mockito.mock(NirvanaWiki.class);

        public TestBot(int flags) {
            super(flags);
        }

        @Override
        protected void go() throws InterruptedException, BotFatalError {
            mocked.go();
        }
        
        @Override
        protected boolean loadCustomProperties(Map<String, String> launchParams)
                throws BotSettingsError {
            mocked.loadCustomProperties(launchParams);
            interceptLaunchParams = launchParams;
            return true;
        }
        
        @Override
        protected void configureWikiBeforeLogin() {
            super.configureWikiBeforeLogin();
            mocked.configureWikiBeforeLogin();
        }

        @Override
        protected void onInterrupted(InterruptedException exception) {
            mocked.onInterrupted(exception);
        }
        
        protected NirvanaWiki createWiki(String domain, String path, String protocol,
                String language) {
            return mockWiki;
        }
    }
    
    @Test
    public void testRun() throws Exception {
        TestBot bot = new TestBot(BasicBot.FLAG_DEFAULT_LOG);
        
        File configFileResource = new File(this.getClass().getClassLoader()
                .getResource(BOT_CONFIG_DEFAULT)
                .getFile());

        int exitCode = bot.run(new String[] {configFileResource.getPath()});

        assertBotRunSuccessfully(bot, exitCode);

        verify(bot.getWiki()).login("TestBot", "no password".toCharArray());
    }

    @Test
    public void testCleanupAfterCrash() throws Exception {
        TestBot bot = new TestBot(BasicBot.FLAG_DEFAULT_LOG);

        File configFileResource = new File(this.getClass().getClassLoader()
                .getResource(BOT_CONFIG_DEFAULT)
                .getFile());

        doThrow(new BotFatalError("bot crashed")).when(bot.mocked).go();

        int exitCode = bot.run(new String[] {configFileResource.getPath()});

        Assert.assertEquals(1, exitCode);

        verify(bot.mockWiki).logout();
    }

    @SuppressWarnings("serial")
    @Test
    public void testRun_withCustomArgs() throws Exception {
        TestBot bot = new TestBot(BasicBot.FLAG_DEFAULT_LOG);
        
        File configFileResource = new File(this.getClass().getClassLoader()
                .getResource(BOT_CONFIG_DEFAULT)
                .getFile());

        int exitCode = bot.run(new String[] {configFileResource.getPath(), "someArg",
                "-key1=A", "-key2=B"});
        
        Map<String, String> expected = new HashMap<String, String>() {{
                put("key1", "A");
                put("key2", "B");
            }
        };

        Assert.assertEquals(0, exitCode);
        Assert.assertEquals(expected, bot.interceptLaunchParams);
    }
    
    @Test
    public void testRun_noAccount() throws Exception {
        TestBot bot = new TestBot(BasicBot.FLAG_DEFAULT_LOG);
        
        File configFileResource = new File(this.getClass().getClassLoader()
                .getResource(BOT_CONFIG_NO_ACCOUNT)
                .getFile());

        int exitCode = bot.run(new String[] {configFileResource.getPath()});

        assertBotCrashedEarly(bot, exitCode);
    }

    @Test
    public void testRun_externalAccountConfig() throws Exception {
        TestBot bot = new TestBot(BasicBot.FLAG_DEFAULT_LOG);
        
        File configFileResource = new File(this.getClass().getClassLoader()
                .getResource(BOT_CONFIG_EXT_ACCOUNT_CONF)
                .getFile());

        int exitCode = bot.run(new String[] {configFileResource.getPath()});

        assertBotRunSuccessfully(bot, exitCode);

        verify(bot.getWiki()).login("TestBot2", "no password 2".toCharArray());
    }
    
    @Test
    public void testLogging_notConfigured() throws Exception {
        TestBot bot = new TestBot(BasicBot.NO_FLAGS);
        
        File configFileResource = new File(this.getClass().getClassLoader()
                .getResource(BOT_CONFIG_DEFAULT)
                .getFile());

        int exitCode = bot.run(new String[] {configFileResource.getPath()});

        assertBotCrashedEarly(bot, exitCode);
    }

    @Test
    public void testWikiLoginFailed() throws Exception {
        TestBot bot = new TestBot(BasicBot.FLAG_DEFAULT_LOG);
        
        File configFileResource = new File(this.getClass().getClassLoader()
                .getResource(BOT_CONFIG_DEFAULT)
                .getFile());

        doThrow(new FailedLoginException("Login Failed")).when(bot.mockWiki)
                .login(anyString(), anyString());
        doThrow(new FailedLoginException("Login Failed")).when(bot.mockWiki)
                .login(anyString(), (char[]) any());

        int exitCode = bot.run(new String[] {configFileResource.getPath()});

        assertBotCrashed(bot, exitCode);
    }

    @Test
    public void testLogging_Configured() throws Exception {
        TestBot bot = new TestBot(BasicBot.NO_FLAGS);
        
        File configFileResource = new File(this.getClass().getClassLoader()
                .getResource(BOT_CONFIG_WITH_LOGGING_CONF)
                .getFile());

        int exitCode = bot.run(new String[] {configFileResource.getPath()});

        assertBotRunSuccessfully(bot, exitCode);
    }
    
    @Test
    public void showLicense( ) {
        TestBot bot = new TestBot(BasicBot.FLAG_DEFAULT_LOG);
        bot.showLicense();
    }

    
    @Test
    public void testGetUserTemplateRe_findsWhenTemplateNoNs() {
        Pattern p = Pattern.compile(BasicBot.getUserTemplateRe("MyBot", null));
        String text = "Xyz.  {{User:MyBot|param1=value1}} blablabla";
        Assert.assertTrue(p.matcher(text).find());
    }

    @Test
    public void testGetUserTemplateRe_findsWhenTemplateWithEnNs() {
        Pattern p = Pattern.compile(BasicBot.getUserTemplateRe("User:MyBot", null));
        String text = "Xyz.  {{User:MyBot|param1=value1}} blablabla";
        Assert.assertTrue(p.matcher(text).find());
    }

    @Test
    public void testGetUserTemplateRe_findsLocalizedNsTitleNoNs() {
        Pattern p = Pattern.compile(BasicBot.getUserTemplateRe("MyBot", "Участник"));
        
        String text = "Xyz.  {{User:MyBot|param1=value1}} blablabla";
        Assert.assertTrue(p.matcher(text).find());
        
        text = "Xyz.  {{Участник:MyBot|param1=value1}} blablabla";
        Assert.assertTrue(p.matcher(text).find());
    }

    @Test
    public void testGetUserTemplateRe_findsLocalizedNsTitleEnNs() {
        Pattern p = Pattern.compile(BasicBot.getUserTemplateRe("User:MyBot", "Участник"));
        
        String text = "Xyz.  {{User:MyBot|param1=value1}} blablabla";
        Assert.assertTrue(p.matcher(text).find());
        
        text = "Xyz.  {{Участник:MyBot|param1=value1}} blablabla";
        Assert.assertTrue(p.matcher(text).find());
    }

    @Test
    public void testGetUserTemplateRe_findsLocalizedNsTitleLocNs() {
        Pattern p = Pattern.compile(BasicBot.getUserTemplateRe("Участник:MyBot", "Участник"));
        
        String text = "Xyz.  {{User:MyBot|param1=value1}} blablabla";
        Assert.assertTrue(p.matcher(text).find());
        
        text = "Xyz.  {{Участник:MyBot|param1=value1}} blablabla";
        Assert.assertTrue(p.matcher(text).find());
    }
    
    private void assertBotCrashedEarly(TestBot bot, int exitCode) throws Exception {
        assertBotCrashed(bot, exitCode, true);
    }
    
    private void assertBotCrashed(TestBot bot, int exitCode) throws Exception {
        assertBotCrashed(bot, exitCode, false);
    }

    @SuppressWarnings("unchecked")
    private void assertBotCrashed(TestBot bot, int exitCode, boolean early) throws Exception {
        Assert.assertEquals(1, exitCode);
        
        if (early) {
            verify(bot.mocked, never()).configureWikiBeforeLogin();
            verify(bot.mocked, never()).loadCustomProperties(Mockito.anyMap());
        }
        verify(bot.mocked, never()).go();

        if (early) {
            Assert.assertNull(bot.getWiki());
        }
    }

    @SuppressWarnings("unchecked")
    private void assertBotRunSuccessfully(TestBot bot, int exitCode) throws Exception {
        Assert.assertEquals(0, exitCode);
        
        verify(bot.mocked).configureWikiBeforeLogin();
        verify(bot.mocked).loadCustomProperties(Mockito.anyMap());
        verify(bot.mocked).go();

        Assert.assertTrue(new File(bot.outDir).isDirectory());
    }
}
