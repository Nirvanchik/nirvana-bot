/**
 *  @(#)MockNirvanaBot.java
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

package org.wikipedia.nirvana.nirvanabot;

import static org.mockito.Mockito.when;

import org.wikipedia.nirvana.base.BotFatalError;
import org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService.Status;
import org.wikipedia.nirvana.nirvanabot.serviceping.WikiService;
import org.wikipedia.nirvana.testing.IntegrationTestHelper;
import org.wikipedia.nirvana.testing.IntegrationTesting;
import org.wikipedia.nirvana.testing.JsonUtils;
import org.wikipedia.nirvana.testing.TestError;
import org.wikipedia.nirvana.wiki.CatScanTools;
import org.wikipedia.nirvana.wiki.MockCatScanTools;
import org.wikipedia.nirvana.wiki.MockNirvanaWiki;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;

/**
 * Patches {@link NirvanaBot} to make it work with mocked wiki and mocked services in tests.
 * All mocked data are read from specified json config.
 *
 * @see {@link org.wikipedia.nirvana.nirvanabot.NirvanaBotTest}
 */
public class MockNirvanaBot extends NirvanaBot {
    /**
     * Logger to use in early working methods (before {@link
     * org.wikipedia.nirvana.base.BasicBot#initLog} which is called from
     * {@link org.wikipedia.nirvana.base.BasicBot#startWithConfig}).
     */
    private Logger tmpLog = Logger.getLogger("MockNirvanaBot");
    WikiService mockWikiService;

    TestHelper testHelper = new TestHelper();

    public static class ExpectedQuery {
        public final List<String> contains;

        public ExpectedQuery(List<String> contains) {
            this.contains = contains;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof String) {
                return containsAll((String) obj);
            }
            return super.equals(obj);
        }

        @Override
        public String toString() {
            StringBuilder temp = new StringBuilder("ExpectedQuery [contains: ");
            temp.append(StringUtils.join(contains, ", "));
            temp.append("]");
            return temp.toString();
        }

        private boolean containsAll(String str) {
            for (String thing: contains) {
                if (!str.contains(thing)) return false;
            }
            return true;
        }
    }
    
    private static class TestHelper extends IntegrationTestHelper {

        JSONObject commonsWikiJson = null;

        List<ExpectedQuery> expectedQueries = null;
        
        public MockNirvanaWiki createCommonsWiki() {
            MockNirvanaWiki wiki = new MockNirvanaWiki("commons");
            if (commonsWikiJson != null) {
                IntegrationTesting.readWiki(wiki, commonsWikiJson);
            }
            return wiki;
        }

        @Override
        protected void readJson(JSONObject jsonObject) {
            super.readJson(jsonObject);

            commonsWikiJson = (JSONObject) jsonObject.get("commons_wiki");

            JSONArray jsonResponces = (JSONArray) jsonObject.get("wiki_tools");
            if (jsonResponces != null) {
                List<String> wikiToolsResponces = JsonUtils
                        .parseMultilineStringArray(jsonResponces);
                MockCatScanTools.mockResponses(wikiToolsResponces);
            }

            JSONArray jsonQueries = (JSONArray) jsonObject.get("expected_tools_queries");
            if (jsonQueries != null) {
                expectedQueries = parseQueries(jsonQueries); 
            }
        }

        private List<ExpectedQuery> parseQueries(JSONArray jsonQueries) {
            Iterator<?> it = jsonQueries.iterator();
            List<ExpectedQuery> queries = new ArrayList<>();
            while (it.hasNext()) {
                JSONObject queryJson = (JSONObject) it.next();
                ExpectedQuery query = new ExpectedQuery(
                        JsonUtils.readStringList(queryJson, "contains"));
                queries.add(query);
            }
            return queries;
        }
        
        /**
         * Checks if required http queries were made in test run or just validate
         * that they have required strings for example. 
         */
        public void validateQueries() {
            if (expectedQueries != null) {
                Assert.assertEquals(expectedQueries, MockCatScanTools.getQueries());
            }
        }
    }

    /**
     * Constructs mocked NirvanaBot.
     *
     * @param flags global bot run flags, see {@link org.wikipedia.nirvana.base.BasicBot}
     * @param testConfigPath path to js config from which to read mock data
     * @throws TestError in the case when test json data are corrupt or unavailable
     */
    public MockNirvanaBot(int flags, @Nullable String testConfigPath) throws TestError {
        super(flags);
        mockWikiService = Mockito.mock(WikiService.class);
        try {
            when(mockWikiService.isOk()).thenReturn(Status.OK);
        } catch (InterruptedException e) {
            // Ignored
        }
        if (testConfigPath != null) {
            try {
                testHelper.readTestConfig(testConfigPath);
            } catch (IOException | ParseException e) {
                tmpLog.severe(e.getMessage());
                throw new TestError(e);
            }
        }
    }

    @Override
    public String getConfig(String[] args) {
        String name = args[0];
        return name;
    }

    @Override
    protected NirvanaWiki createWiki(String domain, String path, String protocol,
            String language) {
        return testHelper.createWiki(domain, path, protocol, language, this.debugMode);
    }

    @Override
    protected NirvanaWiki createCommonsWiki() {
        return testHelper.createCommonsWiki();
    }

    @Override
    protected ServiceManager createServiceManager() throws BotFatalError {
        ServiceManager manager = Mockito.mock(ServiceManager.class);
        try {
            when(manager.checkServices()).thenReturn(true);
            when(manager.getMainWikiService()).thenReturn(mockWikiService);
            when(manager.getActiveService()).thenReturn(CatScanTools.Service.PETSCAN);
        } catch (InterruptedException e) {
            // Ignored, is not going to come here
        }
        return manager;
    }

    @Override
    protected SystemTime initSystemTime() {
        return testHelper.getSystemTime();
    }

    @Override
    protected void go() throws InterruptedException, BotFatalError {
        super.go();
    }

    /**
     * Checks if edits to wikis are equal to those provided in test js config.
     */
    public void validateEdits() {
        testHelper.validateEdits();
    }

    /**
     * Checks if required http queries were made in test run or just validate
     * that they have required strings for example. 
     */
    public void validateQueries() {
        testHelper.validateQueries();
    }
}
