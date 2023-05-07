/**
 *  @(#)IntegrationTestHelper.java
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

import org.wikipedia.nirvana.nirvanabot.SystemTime;
import org.wikipedia.nirvana.util.DateTools;
import org.wikipedia.nirvana.util.FileTools;
import org.wikipedia.nirvana.wiki.MockNirvanaWiki;
import org.wikipedia.nirvana.wiki.MockNirvanaWiki.EditInfoMinimal;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

/**
 * Testing helper used for complicated integration tests.
 * Used mostly to mock any interactions with Wiki/NirvanaWiki class.
 *
 */
public class IntegrationTestHelper {
    protected Logger tmpLog = Logger.getLogger("IntegrationTestHelper");

    protected MockNirvanaWiki mainWiki;
    protected List<EditInfoMinimal> expectedEdits = null;

    protected JSONObject mainWikiJson = null;
    protected SystemTime mockSystemTime = new SystemTime();

    /**
     * Creates {@link NirvanaWiki} object configured to use testing mocks. 
     */
    public NirvanaWiki createWiki(String domain, String path, String protocol,
            String language, boolean allowEdits) {
        MockNirvanaWiki wiki = new MockNirvanaWiki(domain, path, protocol, language);
        wiki.allowEdits(allowEdits);
        mainWiki = wiki;  // We need it to check edits when test finishes
        if (mainWikiJson != null) {
            IntegrationTesting.readWiki(wiki, mainWikiJson);
        }
        return wiki;
    }

    /**
     * Get {@link SystemTime instance}, optionally configured to freezed time for testing.
     */
    public SystemTime getSystemTime() {
        return mockSystemTime;
    }


    /**
     * Reads testing config from json file with specified path.
     */
    public void readTestConfig(String testConfigPath) throws IOException, ParseException {
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(testConfigPath), FileTools.UTF8);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
            readJson(jsonObject);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
    
    protected void readJson(JSONObject jsonObject) {
        mainWikiJson = (JSONObject) jsonObject.get("wiki");

        JSONArray jsonEdits = (JSONArray) jsonObject.get("expected_edits");
        if (jsonEdits != null) {
            expectedEdits = IntegrationTesting.parseEdits(jsonEdits); 
        }

        if (jsonObject.containsKey("system_time")) {
            final Calendar time = IntegrationTesting
                    .readTimestamp(jsonObject.get("system_time"));
            Assert.assertNotNull(time);
            tmpLog.info("[MOCK] Set current time to " +
                    DateTools.printTimestamp(time.getTime()));
            mockSystemTime = new SystemTime() {
                public Calendar now() {
                    return (Calendar) time.clone();
                }
            };
        }
    }

    /**
     * Checks if edits to wikis are equal to those provided in test js config.
     */
    public void validateEdits() {
        if (expectedEdits != null) {
            Assert.assertEquals(expectedEdits, mainWiki.getEdits());
        }
    }

}
