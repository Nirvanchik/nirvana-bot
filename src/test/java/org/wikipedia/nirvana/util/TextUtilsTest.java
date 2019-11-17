/**
 *  @(#)TestUtilsTest.java
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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests for {@link TextUtils}.
 *
 */
public class TextUtilsTest {

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

    @Test
    public void testTextOptionsToMap_smoke() {
        String text =
                "option1=a\n" +
                "option2=b\n";
        Map<String, String> options = new HashMap<>();
        
        boolean retVal = TextUtils.textOptionsToMap(text, options);
        
        Map<String, String> expected = new HashMap<String, String>() {
            {
                put("option1", "a");
                put("option2", "b");
            }
        };
        Assert.assertEquals(expected, options);
        Assert.assertTrue(retVal);
    }
    
    @Test
    public void testTextOptionsToMap_okWithEmptyLines() {
        String text =
                "option1=a\n" +
                "\n\n\n" +
                "option2=b\n";
        Map<String, String> options = new HashMap<>();
        
        TextUtils.textOptionsToMap(text, options);
        
        Map<String, String> expected = new HashMap<String, String>() {
            {
                put("option1", "a");
                put("option2", "b");
            }
        };
        Assert.assertEquals(expected, options);
    }

    @Test
    public void testTextOptionsToMap_ignoreComments() {
        String text =
                "// Some comment where = symbol should be ignored\n" +
                "option1=a\n" +
                "  // Comment that starts with spaces.\n" +
                "option2=b\n" +
                "# Bash/Python style comments.\n";
        Map<String, String> options = new HashMap<>();
        
        TextUtils.textOptionsToMap(text, options);
        
        Map<String, String> expected = new HashMap<String, String>() {
            {
                put("option1", "a");
                put("option2", "b");
            }
        };
        Assert.assertEquals(expected, options);
    }
    
    @Test
    public void testTextOptionsToMap_noComments() {
        String text =
                "// comment1\n" +
                "option1=a\n" +
                "  // comment2 = smthng.\n" +
                "option2=b\n" +
                "# comment3\n";
        Map<String, String> options = new HashMap<>();
        
        TextUtils.textOptionsToMap(text, options, false, new String[] {});
        
        Map<String, String> expected = new HashMap<String, String>() {
            {
                put("// comment1", "");
                put("option1", "a");
                put("// comment2", "smthng.");
                put("option2", "b");
                put("# comment3", "");
            }
        };
        Assert.assertEquals(expected, options);
    }

    @Test
    public void testTextOptionsToMap_emptyIsEmpty() {
        String text =
                "option1=a\n" +
                "option2=\n" +
                "option3\n";
        Map<String, String> options = new HashMap<>();
        
        boolean retVal = TextUtils.textOptionsToMap(text, options, false);
        
        Map<String, String> expected = new HashMap<String, String>() {
            {
                put("option1", "a");
                put("option2", "");
                put("option3", "");
            }
        };
        Assert.assertEquals(expected, options);
        Assert.assertFalse(retVal);
    }

    @Test
    public void testTextOptionsToMap_emptyIsNull() {
        String text =
                "option1=a\n" +
                "option2=\n" +
                "option3\n";
        Map<String, String> options = new HashMap<>();
        
        boolean retVal = TextUtils.textOptionsToMap(text, options, true);
        
        Map<String, String> expected = new HashMap<String, String>() {
            {
                put("option1", "a");
                put("option2", null);
                put("option3", null);
            }
        };
        Assert.assertEquals(expected, options);
        Assert.assertFalse(retVal);
    }
    
}
