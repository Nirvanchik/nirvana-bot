/**
 *  @(#)HttpToolsTest.java
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

package org.wikipedia.nirvana.util;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;


/**
 * Tests for {@link HttpTools}.
 *
 */
public class HttpToolsTest {
    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
    private void withSimpleGet() {
        withSimpleGet("Some content");
    }

    private void withSimpleGet(String response) {
        stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text")
                    .withBody(response)));
    }

    @Test
    public void testFetchString() throws IOException {
        withSimpleGet();
         
        String response = HttpTools.fetch("http://localhost:8089/test");
         
        Assert.assertEquals("Some content" + "\n", response);
        verify(getRequestedFor(urlMatching("/test")));
    }

    @Test
    public void testFetchUrl() throws IOException {
        withSimpleGet();
         
        String response = HttpTools.fetch(new URL("http://localhost:8089/test"));
         
        Assert.assertEquals("Some content" + "\n", response);
        verify(getRequestedFor(urlMatching("/test")));
    }
    
    @Test
    public void testFetch_withUseragent() throws IOException {
        withSimpleGet();
     
        String response = HttpTools.fetch("http://localhost:8089/test", false, true);
         
        Assert.assertEquals("Some content" + "\n", response);
        verify(getRequestedFor(urlMatching("/test"))
                .withHeader("User-Agent", matching("NirvanaBot")));
    }

    @Test
    public void testFetch_withUnescape() throws IOException {
        withSimpleGet("Some &quot;content&quot;");
     
        String response = HttpTools.fetch("http://localhost:8089/test", false, true, false);
         
        Assert.assertEquals("Some \"content\"" + "\n", response);
    }

    @Test
    public void testDownload() throws IOException {
        withSimpleGet();

        File resultFile = tmpDir.newFile("test.txt");
        HttpTools.download("http://localhost:8089/test", resultFile);
         
        verify(getRequestedFor(urlMatching("/test")));
        
        Assert.assertTrue(resultFile.exists());
        String result = FileTools.readFile(resultFile.getPath());        
        Assert.assertEquals("Some content" + "\n", result);
    }

    @Test
    public void testDownloadUrl() throws IOException {
        withSimpleGet();

        File resultFile = tmpDir.newFile("test.txt");
        HttpTools.download("http://localhost:8089/test", resultFile.getPath());
         
        verify(getRequestedFor(urlMatching("/test")));
        
        Assert.assertTrue(resultFile.exists());
        String result = FileTools.readFile(resultFile.getPath());        
        Assert.assertEquals("Some content" + "\n", result);
    }

}
