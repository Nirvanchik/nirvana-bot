/**
 *  @(#)CatScanToolsTest.java
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

package org.wikipedia.nirvana.wiki;

import org.wikipedia.nirvana.wiki.CatScanTools.EnumerationType;
import org.wikipedia.nirvana.wiki.CatScanTools.Service;
import org.wikipedia.nirvana.wiki.CatScanTools.ServiceFeatures;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;


/**
 * Unit-tests for {@link CatScanTools}. CatScanTools cannot be unit-tested as it depends on external
 * web-services. Here we unit-test the next aspects:
 * 1) Simple utility code, interfaces, expectations of API.
 * 2) CatScanTools mocking for other unit-tests.
 * 3) Minimal requirements of all services and their "enabled" status.
 */
public class CatScanToolsTest {
    private CatScanTools.Service testedService;

    @Before
    public void setUp() throws Exception {
        testedService = CatScanTools.Service.PETSCAN;
    }

    @After
    public void tearDown() throws Exception {
        CatScanTools.resetFromTest();
    }

    @Test
    public void testAtLeastOneServiceIsOn() {
        boolean isOn = false;
        for (Service service: Service.values()) {
            if (!service.down) {
                isOn = true;
                break;
            }
        }
        Assert.assertTrue("At leas one service must be enabled", isOn);
    }

    @Test
    public void testAllServicesHaveFeatures() {
        for (Service service: Service.values()) {
            if (!service.down && service.features == 0) {
                Assert.fail(String.format("Service %s is enabled and has no features!",
                        service.name));
            }
        }
    }

    @Test
    public void testMockFeatures() {
        CatScanTools.Service.setTestFeatures(0b1000000);

        Assert.assertTrue(testedService.supportsFeature(0b1000000));
        Assert.assertFalse(testedService.supportsFeature(0b1));
    }

    @Test
    public void testMockQuery() throws Exception {
        CatScanTools.mockResponces(Arrays.asList("responce1", "responce2"));

        Assert.assertEquals("responce1",
                CatScanTools.loadNewPagesForCatWithService(testedService, "cat", "en", 5, 500, 0));
        Assert.assertEquals("responce2",
                CatScanTools.loadNewPagesForCatWithService(testedService, "dog", "en", 5, 300, 0));

        List<String> queries = CatScanTools.getQueries();
        Assert.assertEquals(2, queries.size());
        Assert.assertTrue(queries.get(0).contains("cat") && queries.get(0).contains("500"));
        Assert.assertTrue(queries.get(1).contains("dog") && queries.get(1).contains("300"));
    }

    @Test (expected = IllegalArgumentException.class)
    public void testFailWhenUnsupported() throws Exception {
        CatScanTools.mockResponces(Arrays.asList("responce1", "responce2"));
        CatScanTools.Service.setTestFeatures(0);

        Assert.assertEquals("responce1",
                CatScanTools.loadNewPagesForCatWithService(testedService, "cat", "en", 5, 500, 0));
    }

    @Test
    public void testAllQuieriesCanBeMocked() throws Exception {
        CatScanTools.mockResponces(Arrays.asList("responce1", "responce2", "responce3", "responce4",
                "responce5", "responce6", "responce7", "responce8", "responce9", "responce10"));
        String cat = "cat";
        List<String> cats = Arrays.asList("dogs");
        List<String> ignore = Arrays.asList("poodles");
        List<String> templates = Arrays.asList("test");
        EnumerationType et = EnumerationType.OR;

        Assert.assertEquals("responce1", CatScanTools.loadPagesForCatWithService(testedService,
                cat, "en", 5, 0));
        Assert.assertEquals("responce2", CatScanTools.loadPagesForCatListAndIgnoreWithService(
                testedService, cats, ignore, "en", 5, 0));
        Assert.assertEquals("responce3", CatScanTools.loadNewPagesForCatWithService(testedService,
                cat, "en", 5, 500, 0));
        Assert.assertEquals("responce4", CatScanTools.loadNewPagesForCatListAndIgnoreWithService(
                testedService, cats, ignore, "en", 5, 500, 0));
        Assert.assertEquals("responce5", CatScanTools.loadPagesWithTemplatesForCatWithService(
                testedService, cat, "en", 5, templates, et, 0));
        Assert.assertEquals("responce6",
                CatScanTools.loadPagesWithTemplatesForCatListAndIgnoreWithService(testedService,
                        cats, ignore, "en", 5, templates, et, 0));
        Assert.assertEquals("responce7", CatScanTools.loadNewPagesWithTemplatesForCatWithService(
                testedService, cat, "en", 5, 500, templates, et, 0));
        Assert.assertEquals("responce8",
                CatScanTools.loadNewPagesWithTemplatesForCatListAndIgnoreWithService(testedService,
                        cats, ignore, "en", 5, 500, templates, et, 0));
    }

    @Test
    public void testGetServiceByName() {
        Assert.assertEquals(testedService,
                CatScanTools.Service.getServiceByName(testedService.name)); 
        Assert.assertEquals(null, CatScanTools.Service.getServiceByName("dummy"));
        Assert.assertEquals(testedService,
                CatScanTools.Service.getServiceByName("dummy", testedService));
    }

    @Test
    public void testHasService() {
        Assert.assertTrue(CatScanTools.Service.hasService(testedService.name)); 
        Assert.assertFalse(null, CatScanTools.Service.hasService("dummy"));
    }

    @Test
    public void testHasTemplateQueryGeneration() throws Exception {
        CatScanTools.mockResponces(Arrays.asList("responce1", "responce2", "responce3",
                "responce4", "responce5", "responce6"));
        String cat = "cat";
        List<String> cats = Arrays.asList("dogs");
        List<String> ignore = Arrays.asList("poodles");
        List<String> templates = Arrays.asList("test");

        CatScanTools.loadPagesWithTemplatesForCatWithService(testedService, cat, "en", 5, templates,
                EnumerationType.OR, 0);
        CatScanTools.loadPagesWithTemplatesForCatWithService(testedService, cat, "en", 5, templates,
                EnumerationType.AND, 0);
        CatScanTools.loadPagesWithTemplatesForCatWithService(testedService, cat, "en", 5, templates,
                EnumerationType.NONE, 0);

        Assert.assertNotEquals(CatScanTools.getQueries().get(0), CatScanTools.getQueries().get(1));
        Assert.assertNotEquals(CatScanTools.getQueries().get(1), CatScanTools.getQueries().get(2));

        CatScanTools.loadPagesWithTemplatesForCatListAndIgnoreWithService(testedService, cats,
                ignore, "en", 5, templates, EnumerationType.OR, 0);
        CatScanTools.loadNewPagesWithTemplatesForCatWithService(testedService, cat, "en", 5, 500,
                templates, EnumerationType.AND, 0);
        CatScanTools.loadNewPagesWithTemplatesForCatListAndIgnoreWithService(testedService, cats,
                ignore, "en", 5, 500, templates, EnumerationType.NONE, 0);    

        Assert.assertNotEquals(CatScanTools.getQueries().get(3), CatScanTools.getQueries().get(4));
        Assert.assertNotEquals(CatScanTools.getQueries().get(4), CatScanTools.getQueries().get(5));
    }

    @Test
    public void testGetDefaultServiceForFeature() {
        Assert.assertNotNull(
                CatScanTools.Service.getDefaultServiceForFeature(ServiceFeatures.NEWPAGES, null));
        Assert.assertNotNull(
                CatScanTools.Service.getDefaultServiceForFeature(ServiceFeatures.PAGES, null));
    }
    
}
