/**
 *  @(#)CatscanServiceTest.java
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

import org.wikipedia.nirvana.wiki.CatScanTools;
import org.wikipedia.nirvana.wiki.MockCatScanTools;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

/**
 * Unit tests for {@link CatscanService}.
 *
 */
public class CatscanServiceTest {
    private CatScanTools.Service testedService;
    
    private static final String RESPONSE1 =
            "number    title    pageid    namespace    length    touched\n" + 
            "1    Accelerated_mobile_pages    7196068        13488    20230916114038\n" + 
            "10    HTML    9482        29915    20231130075656\n" + 
            "11    HTML_Application    1222097        9686    20220509203835\n" + 
            "12    HTML_Tidy    4674216        5155    20220509203840\n" + 
            "13    HTML-атрибут    10384773        5869    20231121235901\n" + 
            "14    HTML-цвета    242919        28214    20231029090440\n" + 
            "15    HTML5    1556482        53627    20231114104937\n" + 
            "";
    
    private static final String RESPONSE2 =
            "number    title    pageid    namespace    length    touched\n" + 
            "1    Элементы_HTML    72131        36837    20231120205212\n" + 
            "2    Фрейм_(HTML)    682191        4949    20231023070250\n" + 
            "3    Форма_(HTML)    4246158        8990    20230406084430\n" + 
            "30    HTMLHelp    238478        8304    20230921175201\n" + 
            "31    HTML5_video    3430851        63074    20231113212743\n" + 
            "32    HTML5_audio    5688755        6343    20220509203639\n" + 
            "33    HTML5_Shiv    5189205        1695    20221130040404\n" + 
            "34    HTML5    1556482        53627    20231114104937\n" + 
            "";
    
    private static final String RESPONSE_TIMEOUT = "@java.net.SocketTimeoutException";

    @Before
    public void setUp() throws Exception {
        testedService = CatScanTools.Service.PETSCAN;
        MockCatScanTools.testsNeverSleep();
        MockCatScanTools.reset();
    }

    @After
    public void tearDown() throws Exception {
        MockCatScanTools.reset();
    }

    @Test
    public void testCheckWorking() throws InterruptedException {
        CatscanService serviceChecker = new CatscanService(testedService);
        MockCatScanTools.mockResponses(Arrays.asList(RESPONSE1, RESPONSE2));
        Assert.assertTrue(serviceChecker.checkWorking());
    }
    
    @Test
    public void timeout() throws InterruptedException {
        CatscanService serviceChecker = new CatscanService(testedService);
        MockCatScanTools.mockResponses(Arrays.asList(RESPONSE_TIMEOUT, RESPONSE_TIMEOUT));

        Assert.assertFalse(serviceChecker.checkWorking());
        Assert.assertEquals("Exception when calling loadPagesForCatWithService(): "
                + "java.net.SocketTimeoutException", serviceChecker.getLastError());
    }
    
    @Test
    public void emptyResponse() throws InterruptedException {
        CatscanService serviceChecker = new CatscanService(testedService);
        MockCatScanTools.mockResponses(Arrays.asList("", ""));

        Assert.assertFalse(serviceChecker.checkWorking());
        Assert.assertEquals("loadPagesForCatWithService() returned empty result",
                serviceChecker.getLastError());
    }

    @Test
    public void invalidResponse() throws InterruptedException {
        CatscanService serviceChecker = new CatscanService(testedService);
        MockCatScanTools.mockResponses(Arrays.asList("123", "456"));

        Assert.assertFalse(serviceChecker.checkWorking());
        Assert.assertEquals("loadPagesForCatWithService() returned corrupted result",
                serviceChecker.getLastError());
    }
    
    @Test
    public void invalidResponseForAnotherType() throws InterruptedException {
        CatscanService serviceChecker = new CatscanService(testedService);
        MockCatScanTools.mockResponses(Arrays.asList(RESPONSE1, "456"));

        Assert.assertFalse(serviceChecker.checkWorking());
        Assert.assertEquals("loadNewPagesForCatWithService() returned corrupted result",
                serviceChecker.getLastError());
    }

}
