/**
 *  @(#)LogUtilsTest.java
 *  Copyright © 2022 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.util;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link LogUtils}.
 *
 */
public class LogUtilsTest {
    Logger log;

    @Before
    public void setUp() throws Exception {
        log = Mockito.spy(LogManager.getLogger(this.getClass().getName()));
    }

    @SuppressWarnings("serial")
    @Test
    public void testLogParametersMap() {
        Map<String, String> params = new HashMap<String, String>() {{
                put("key1", "A");
                put("key2", "B");
            }
        };
        LogUtils.logParametersMap(log, params);
        verify(log).debug(eq("{} = {}"), eq("key1"), eq("A"));
        verify(log).debug(eq("{} = {}"), eq("key2"), eq("B"));
    }

    @SuppressWarnings("serial")
    @Test
    public void testLogParametersMap_withBorder() {
        Map<String, String> params = new HashMap<String, String>() {{
                put("key1", "A");
            }
        };
        LogUtils.logParametersMap(log, params, true);
        verify(log).debug(eq("----< params >----"));
        verify(log).debug(eq("{} = {}"), eq("key1"), eq("A"));
        verify(log).debug(eq("------------------"));
    }

}
