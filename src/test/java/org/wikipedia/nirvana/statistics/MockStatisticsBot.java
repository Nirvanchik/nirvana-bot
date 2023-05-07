/**
 *  @(#)MockStatisticsBot.java
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

package org.wikipedia.nirvana.statistics;

import org.wikipedia.nirvana.base.BasicBot;
import org.wikipedia.nirvana.nirvanabot.SystemTime;
import org.wikipedia.nirvana.testing.IntegrationTestHelper;
import org.wikipedia.nirvana.testing.TestError;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.Nullable;

/**
 * Wrapper class for unit testing.
 *
 */
public class MockStatisticsBot extends StatisticsBot {
    private Logger tmpLog = Logger.getLogger("MockNirvanaBot");

    private IntegrationTestHelper testHelper = new IntegrationTestHelper();

    /**
     * Constructs mocked StatisticsBot.
     *
     * @param flags global bot run flags, see {@link org.wikipedia.nirvana.base.BasicBot}
     * @param testConfigPath path to js config from which to read mock data
     * @throws TestError in the case when test json data are corrupt or unavailable
     */
    public MockStatisticsBot(int flags, @Nullable String testConfigPath) throws TestError {
        super(BasicBot.FLAG_DEFAULT_LOG);
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

    /**
     * Checks if edits to wikis are equal to those provided in test js config.
     */
    public void validateEdits() {
        testHelper.validateEdits();
    }
    
    @Override
    protected SystemTime initSystemTime() {
        return testHelper.getSystemTime();
    }

}
