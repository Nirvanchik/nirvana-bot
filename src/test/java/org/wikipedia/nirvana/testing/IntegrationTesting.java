/**
 *  @(#)IntegrationTesting.java
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

package org.wikipedia.nirvana.testing;

import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.util.DateTools;
import org.wikipedia.nirvana.wiki.MockNirvanaWiki;
import org.wikipedia.nirvana.wiki.MockNirvanaWiki.EditInfoMinimal;
import org.wikipedia.nirvana.wiki.MockNirvanaWiki.MockRevision;
import org.wikipedia.nirvana.wiki.MockNirvanaWiki.TestRevision;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Utilities for integration tests.
 *
 */
public class IntegrationTesting {
    
    static List<EditInfoMinimal> parseEdits(JSONArray jsonEdits) {
        Iterator<?> it = jsonEdits.iterator();
        List<EditInfoMinimal> edits = new ArrayList<>();
        while (it.hasNext()) {
            JSONObject editInfoJson = (JSONObject) it.next();
            EditInfoMinimal edit = new EditInfoMinimal(
                    (String) editInfoJson.get("title"),
                    JsonUtils.readMultiLineString(editInfoJson, "text"),
                    (int)(long)(Long) editInfoJson.get("section"));
            edits.add(edit);
        }
        return edits;
    }

    /**
     * Reads wiki mocks configuration from provided json object and sets them up 
     * to provided {@link MockNirvanaWiki} object.
     */
    public static void readWiki(MockNirvanaWiki wiki, JSONObject wikiJson) {
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
                wiki.mockNamespaceIdentifier((Long) nsIdJson.get("number"),
                        (String) nsIdJson.get("id"));
            }
        }
        JSONArray nsJsonList = (JSONArray) wikiJson.get("namespace");
        if (nsJsonList != null) {
            Iterator<?> it = nsJsonList.iterator();
            while (it.hasNext()) {
                JSONObject nsJson = (JSONObject) it.next();
                wiki.mockNamespace((String) nsJson.get("title"),
                        ((Long) nsJson.get("id")).intValue());
            }
        }
        JSONArray pageTextJsonList = (JSONArray) wikiJson.get("pageText");
        if (pageTextJsonList != null) {
            Iterator<?> it = pageTextJsonList.iterator();
            while (it.hasNext()) {
                JSONObject pageTextJson = (JSONObject) it.next();
                wiki.mockPageText((String) pageTextJson.get("title"),
                        JsonUtils.readMultiLineString(pageTextJson, "text"));
            }
        }

        JSONArray revTextJsonList = (JSONArray) wikiJson.get("revisionText");
        if (revTextJsonList != null) {
            Iterator<?> it = revTextJsonList.iterator();
            while (it.hasNext()) {
                JSONObject revTextJson = (JSONObject) it.next();
                wiki.mockRevisionText((Long) revTextJson.get("revid"),
                        JsonUtils.readMultiLineString(revTextJson, "text"));
            }
        }

        JSONArray firstRevJsonList = (JSONArray) wikiJson.get("firstRevision");
        if (firstRevJsonList != null) {
            for (MockRevision r: parseRevisionList(wiki, firstRevJsonList)) {
                wiki.mockFirstRevision(r.title, r.revision);
                if (!r.title.equals(r.currentTitle)) {
                    wiki.mockFirstRevision(r.currentTitle, r.revision);
                }
            }
        }

        JSONArray topRevJsonList = (JSONArray) wikiJson.get("topRevision");
        if (topRevJsonList != null) {
            for (MockRevision r: parseRevisionList(wiki, topRevJsonList)) {
                wiki.mockTopRevision(r.title, r.revision);
                if (!r.title.equals(r.currentTitle)) {
                    wiki.mockTopRevision(r.currentTitle, r.revision);
                }
            }
        }

        JSONArray revJsonList = (JSONArray) wikiJson.get("revision");
        if (revJsonList != null) {
            for (MockRevision r: parseRevisionList(wiki, revJsonList)) {
                if (r.revision != null) {
                    wiki.mockRevision(r.revision.getRevid(), r.revision);
                }
            }
        }

        JSONArray pageTemplatesJsonList = (JSONArray) wikiJson.get("templates");
        if (pageTemplatesJsonList != null) {
            Iterator<?> it = pageTemplatesJsonList.iterator();
            while (it.hasNext()) {
                JSONObject pageTemplatesJson = (JSONObject) it.next();
                wiki.mockPageTemplates((String) pageTemplatesJson.get("title"),
                        JsonUtils.readStringList(pageTemplatesJson, "templates"));
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
                        JsonUtils.readStringList(whatLinksHereJson, "links"));
            }
        }

        JSONArray pageHistoryJsonList = (JSONArray) wikiJson.get("pageHistory");
        if (pageHistoryJsonList != null) {
            Iterator<?> it = pageHistoryJsonList.iterator();
            while (it.hasNext()) {
                JSONObject pageHistoryItem = (JSONObject) it.next();
                MockRevision [] mockRevs = parseRevisionList(
                        wiki, (JSONArray) pageHistoryItem.get("revisions"));
                Revision [] revs = Arrays.stream(mockRevs)
                        .map(r -> r.revision)
                        .filter(Objects::nonNull)
                        .toArray(Revision[]::new);
                wiki.mockPageHistory((String) pageHistoryItem.get("title"),
                        (Long) pageHistoryItem.get("start_time"),
                        (Long) pageHistoryItem.get("end_time"),
                        revs);
            }
        }
        
        JSONArray resolveRedirectJsonList = (JSONArray) wikiJson.get("resolveRedirect");
        if (resolveRedirectJsonList != null) {
            Iterator<?> it = resolveRedirectJsonList.iterator();
            while (it.hasNext()) {
                JSONObject resolveRedirectJson = (JSONObject) it.next();
                wiki.mockResolveRedirect((String) resolveRedirectJson.get("title"),
                        (String) resolveRedirectJson.get("resolvedTitle"));
            }
        }
    }
    
    private static MockRevision [] parseRevisionList(MockNirvanaWiki wiki,
            JSONArray revisionsJson) {
        MockRevision [] revisions = new MockRevision[revisionsJson.size()];
        Iterator<?> it = revisionsJson.iterator();
        for (int i = 0; it.hasNext(); i++) {
            revisions[i] = parseRevision(wiki, (JSONObject) it.next());
        }
        return revisions;
    }

    @SuppressWarnings("unchecked")  // Json parsing
    private static MockRevision parseRevision(MockNirvanaWiki wiki, JSONObject revisionJson) {
        String title = (String) revisionJson.get("title");
        TestRevision r = null;
        if (revisionJson.containsKey("revid")) {
            long revid = (Long) revisionJson.get("revid");
            Object timestampField = revisionJson.get("timestamp");
            Assert.assertNotNull("'timestamp' item must not be null", timestampField);
            Calendar timestamp = readTimestamp(timestampField);
            OffsetDateTime datetime = OffsetDateTime.ofInstant(timestamp.toInstant(),
                    ZoneId.systemDefault());
            String summary = (String) revisionJson.get("summary");
            String user = (String) revisionJson.get("user");
            boolean minor = (Boolean) revisionJson.get("minor");
            boolean bot = (Boolean) revisionJson.getOrDefault("bot", false);
            boolean rvnew = (Boolean) revisionJson.getOrDefault("rvnew", false);
            int size = (int)(long)(Long) revisionJson.get("size");
            r = wiki.new TestRevision(
                    revid, datetime, title, summary, user, minor, bot, rvnew, size);
            if (revisionJson.containsKey("previous")) {
                r.setPrevious((Long) revisionJson.get("previous")); 
            }
        }
        String currentTitle = (String) revisionJson.getOrDefault("current_title", title);
        MockRevision mockRev = wiki.new MockRevision(title, currentTitle, r);
        return mockRev;
    }

    // TODO: Migrate to Java8 dates
    /**
     * Reads timestamp from Json object.
     * Json object can be one of:
     * 1) integer or long value (unix time in milliseconds)
     * 2) string value (wiki timestamp with format like yyyy-MM-dd'T'HH:mm:ss'Z)
     */
    public static Calendar readTimestamp(Object field) {
        if (field instanceof Long || field instanceof Integer) {
            long timestamp = field instanceof Long ? (Long) field : (Integer) field;
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(timestamp);
            // May be first set time zone, and then time in millis?
            c.setTimeZone(TimeZone.getTimeZone("UTC"));
            return c;
        } else if (field instanceof String) {
            Calendar c = DateTools.parseWikiTimestampUTC((String) field);
            Assert.assertNotNull("Failed to parse timestamp string: " + (String) field, c);
            return c;
        } else {
            throw new IllegalStateException(
                    "Unexpected type of timestamp item: " + field.getClass());
        }
    }
}
