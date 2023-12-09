/**
 *  @(#)InternetServiceTest.java
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

package org.wikipedia.nirvana.nirvanabot.serviceping;

import org.wikipedia.nirvana.util.MockHttpTools;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.SocketTimeoutException;
import java.util.Arrays;

/**
 * Unit tests for {@link InternetService}.
 *
 */
public class InternetServiceTest {
    private static final String RESPONSE = "<html><body><p>Hello!</p></body></html>";
    
    private static class TestInternetService extends InternetService {
        
    }
    
    @Before
    public void setUp() throws Exception {
        MockHttpTools.reset();
    }

    @After
    public void tearDown() throws Exception {
        MockHttpTools.reset();
    }

    @Test
    public void checkWorking() throws InterruptedException {
        InternetService checker = new TestInternetService();
        MockHttpTools.mockResponces(Arrays.asList(RESPONSE, RESPONSE));
        Assert.assertTrue(checker.checkWorking());
    }
    
    @Test
    public void checkWorkingUseFallback() throws InterruptedException {
        InternetService checker = new TestInternetService();
        MockHttpTools.mockResponces(Arrays.asList(new SocketTimeoutException("No response"),
                RESPONSE));
        Assert.assertTrue(checker.checkWorking());
    }

    
    @Test
    public void timeout() throws InterruptedException {
        InternetService checker = new TestInternetService();
        MockHttpTools.mockResponces(Arrays.asList(new SocketTimeoutException("No response"),
                new SocketTimeoutException("No response")));
        Assert.assertFalse(checker.checkWorking());
    }
    
    @Test
    public void invalidResponse() throws InterruptedException {
        InternetService checker = new TestInternetService();
        MockHttpTools.mockResponces(Arrays.asList("", ""));
        Assert.assertFalse(checker.checkWorking());
    }

}
