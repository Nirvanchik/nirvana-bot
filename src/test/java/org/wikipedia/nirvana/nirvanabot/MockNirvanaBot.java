/**
 *  @(#)MockNirvanaBot.java 03.04.2016
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

import static org.mockito.Mockito.when;

import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService.Status;
import org.wikipedia.nirvana.nirvanabot.serviceping.WikiService;
import org.wikipedia.nirvana.util.FileTools;
import org.wikipedia.nirvana.wiki.CatScanTools;
import org.wikipedia.nirvana.wiki.MockCatScanTools;
import org.wikipedia.nirvana.wiki.MockNirvanaWiki;
import org.wikipedia.nirvana.wiki.MockNirvanaWiki.EditInfoMinimal;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.mockito.Mockito;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

/**
 * Patches {@link NirvanaBot} to make it work with mocked wiki and mocked services in tests.
 * All mocked data are read from specified json config.
 *
 * @see {@link org.wikipedia.nirvana.nirvanabot.NirvanaBotTest}
 */
public class MockNirvanaBot extends NirvanaBot {
    WikiService mockWikiService;
    MockNirvanaWiki mainWiki;

    JSONObject mainWikiJson = null;
    JSONObject commonsWikiJson = null;

    List<EditInfoMinimal> expectedEdits = null;
    List<ExpectedQuery> expectedQueries = null;

    public static class TestError extends Exception {
        private static final long serialVersionUID = 1L;
        public final Exception original;

        public TestError(Exception error) {
            original = error;
        }
    }

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

    /**
     * Constructs mocked NirvanaBot.
     *
     * @param flags global bot run flags, see {@link org.wikipedia.nirvana.BasicBot}
     * @param testConfigPath path to js config from which to read mock data
     * @throws TestError in the case when test json data are corrupt or unavailable
     */
    public MockNirvanaBot(int flags, String testConfigPath) throws TestError {
        super(flags);
        mockWikiService = Mockito.mock(WikiService.class);
        try {
            when(mockWikiService.isOk()).thenReturn(Status.OK);
        } catch (InterruptedException e) {
            // Ignored
        }
        if (testConfigPath != null) {
            try {
                initializeFromTestConfig(testConfigPath);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
                throw new TestError(e);
            }
        }
    }

    void initializeFromTestConfig(String testConfigPath) throws IOException, ParseException {
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(testConfigPath), FileTools.UTF8);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
            mainWikiJson = (JSONObject) jsonObject.get("wiki");
            commonsWikiJson = (JSONObject) jsonObject.get("commons_wiki");

            JSONArray jsonResponces = (JSONArray) jsonObject.get("wiki_tools");
            if (jsonResponces != null) {
                List<String> wikiToolsResponces = parseMultilineStringArray(jsonResponces);
                MockCatScanTools.mockResponses(wikiToolsResponces);
            }

            JSONArray jsonEdits = (JSONArray) jsonObject.get("expected_edits");
            if (jsonEdits != null) {
                expectedEdits = parseEdits(jsonEdits); 
            }

            JSONArray jsonQueries = (JSONArray) jsonObject.get("expected_tools_queries");
            if (jsonQueries != null) {
                expectedQueries = parseQueries(jsonQueries); 
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private List<ExpectedQuery> parseQueries(JSONArray jsonQueries) {
        Iterator<?> it = jsonQueries.iterator();
        List<ExpectedQuery> queries = new ArrayList<>();
        while (it.hasNext()) {
            JSONObject queryJson = (JSONObject) it.next();
            ExpectedQuery query = new ExpectedQuery(readStringList(queryJson, "contains"));
            queries.add(query);
        }
        return queries;
    }

    private List<EditInfoMinimal> parseEdits(JSONArray jsonEdits) {
        Iterator<?> it = jsonEdits.iterator();
        List<EditInfoMinimal> edits = new ArrayList<>();
        while (it.hasNext()) {
            JSONObject editInfoJson = (JSONObject) it.next();
            EditInfoMinimal edit = new EditInfoMinimal(
                    (String) editInfoJson.get("title"),
                    readMultiLineString(editInfoJson, "text"),
                    (int)(long)(Long) editInfoJson.get("section"));
            edits.add(edit);
        }
        return edits;
    }

    private List<String> parseMultilineStringArray(JSONArray jsonResponces) {
        List<String> list = new ArrayList<String>();
        Iterator<?> it = jsonResponces.iterator();
        while (it.hasNext()) {
            String val = parseMultiLineString(it.next());
            list.add(val);
        }
        return list;
    }

    private void readWiki(MockNirvanaWiki wiki, JSONObject wikiJson) {
        JSONArray transcludesJsonList = (JSONArray) wikiJson.get("whatTranscludesHere");
        if (transcludesJsonList != null) {
            Iterator<?> it = transcludesJsonList.iterator();
            while (it.hasNext()) {
                JSONObject transcludesJson = (JSONObject) it.next();
                wiki.mockWhatTranscludesHere((String)transcludesJson.get("title"),
                        ((String)transcludesJson.get("list")).split(","));
            }
        }
        JSONArray nsIdJsonList = (JSONArray) wikiJson.get("namespaceIdentifier");
        if (nsIdJsonList != null) {
            Iterator<?> it = nsIdJsonList.iterator();
            while (it.hasNext()) {
                JSONObject nsIdJson = (JSONObject) it.next();
                //System.out.println("ns num type: "+ nsIdJson.get("number").getClass());
                wiki.mockNamespaceIdentifier((Long) nsIdJson.get("number"),
                        (String) nsIdJson.get("id"));
            }
        }
        JSONArray pageTextJsonList = (JSONArray) wikiJson.get("pageText");
        if (pageTextJsonList != null) {
            Iterator<?> it = pageTextJsonList.iterator();
            while (it.hasNext()) {
                JSONObject pageTextJson = (JSONObject) it.next();
                wiki.mockPageText((String) pageTextJson.get("title"),
                        readMultiLineString(pageTextJson, "text"));
            }
        }

        JSONArray firstRevJsonList = (JSONArray) wikiJson.get("firstRevision");
        if (firstRevJsonList != null) {
            Iterator<?> it = firstRevJsonList.iterator();
            while (it.hasNext()) {
                JSONObject revisionJson = (JSONObject) it.next();
                wiki.mockFirstRevision((String) revisionJson.get("title"),
                        parseRevision(wiki, revisionJson));
            }
        }

        JSONArray pageTemplatesJsonList = (JSONArray) wikiJson.get("templates");
        if (pageTemplatesJsonList != null) {
            Iterator<?> it = pageTemplatesJsonList.iterator();
            while (it.hasNext()) {
                JSONObject pageTemplatesJson = (JSONObject) it.next();
                wiki.mockPageTemplates((String) pageTemplatesJson.get("title"),
                        readStringList(pageTemplatesJson, "templates"));
            }
        }

        JSONArray existsJsonList = (JSONArray) wikiJson.get("exists");
        if (existsJsonList != null) {
            Iterator<?> it = existsJsonList.iterator();
            while (it.hasNext()) {
                String title = (String) it.next();
                wiki.mockExists(title, true);
            }
        }

        JSONArray whatLinksHereJsonList = (JSONArray) wikiJson.get("whatLinksHere");
        if (whatLinksHereJsonList != null) {
            Iterator<?> it = whatLinksHereJsonList.iterator();
            while (it.hasNext()) {
                JSONObject whatLinksHereJson = (JSONObject) it.next();
                wiki.mockWhatLinksHere((String) whatLinksHereJson.get("title"),
                        readStringList(whatLinksHereJson, "links"));
            }
        }
    }

    private Revision parseRevision(MockNirvanaWiki wiki, JSONObject revisionJson) {
        long revid = (Long) revisionJson.get("revid");
        long timestamp = (Long) revisionJson.get("timestamp");
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        String title = (String) revisionJson.get("title");
        String summary = (String) revisionJson.get("summary");
        String user = (String) revisionJson.get("user");
        boolean minor = (Boolean) revisionJson.get("minor");
        boolean bot = (Boolean) revisionJson.get("bot");
        boolean rvnew = (Boolean) revisionJson.get("rvnew");
        int size = (int)(long)(Long) revisionJson.get("size");
        Revision r = wiki.new Revision(revid, c, title, summary, user, minor, bot, rvnew, size);
        return r;
    }

    private String readMultiLineString(JSONObject object, String name) {
        return parseMultiLineString(object.get(name));
    }

    private String parseMultiLineString(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof String) {
            return (String) object;
        } else {
            JSONArray list = (JSONArray) object;
            StringBuilder b = new StringBuilder();
            Iterator<?> it = list.iterator();
            while (it.hasNext()) {
                String str = (String) it.next();
                b.append(str).append("\n");
            }
            // Remove last "\n"
            if (b.length() > 0) {
                b.setLength(b.length() - 1);
            }
            return b.toString();
        }
    }

    private List<String> readStringList(JSONObject jsonObject, String name) {
        JSONArray list = (JSONArray) jsonObject.get(name);
        Iterator<?> it = list.iterator();
        List<String> result = new ArrayList<String>();
        while (it.hasNext()) {
            result.add((String) it.next());
        }
        return result;
    }

    @Override
    public String getConfig(String[] args) {
        String name = args[0];
        return name;
    }

    @Override
    protected NirvanaWiki createWiki(String domain, String path, String protocol,
            String language) {
        MockNirvanaWiki wiki = new MockNirvanaWiki(domain, path, protocol, language);
        wiki.allowEdits(this.DEBUG_MODE);
        mainWiki = wiki;  // We need it to check edits when test finishes
        if (mainWikiJson != null) {
            readWiki(wiki, mainWikiJson);
        }
        return wiki;
    }

    @Override
    protected NirvanaWiki createCommonsWiki() {
        MockNirvanaWiki wiki = new MockNirvanaWiki("commons");
        if (commonsWikiJson != null) {
            readWiki(wiki, commonsWikiJson);
        }
        return wiki;
    }

    @Override
    protected ServiceManager createServiceManager() throws BotFatalError {
        ServiceManager manager = Mockito.mock(ServiceManager.class);
        try {
            when(manager.checkServices()).thenReturn(true);
            when(manager.getMainWikiService()).thenReturn(mockWikiService);
            when(manager.getActiveService()).thenReturn(CatScanTools.Service.PETSCAN_OLD);
        } catch (InterruptedException e) {
            // Ignored, is not going to come here
        }
        return manager;
    }

    @Override
    protected void go() throws InterruptedException, BotFatalError {
        super.go();
    }

    /**
     * Checks if edits to wikis are equal to those provided in test js config.
     */
    public void validateEdits() {
        if (expectedEdits != null) {
            Assert.assertEquals(expectedEdits, mainWiki.getEdits());
        }
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
