/**
 *  @(#)BotUtilsTest.java
 *  Copyright © 2018 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.wikipedia.nirvana.util.OptionsUtils;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;

/**
 * Unit tests for {@link OptionsUtils}.
 */
public class OptionsUtilsTest {

    @Test
    public void validateIntegerSetting() throws IOException {
        Properties props = new Properties();
        props.load(new StringReader("a: 10\nx: abc"));

        Assert.assertEquals(10, OptionsUtils.validateIntegerSetting(props, "a", 5, false));
        Assert.assertEquals(5, OptionsUtils.validateIntegerSetting(props, "b", 5, false));
        Assert.assertEquals(5, OptionsUtils.validateIntegerSetting(props, "x", 5, false));

        Assert.assertEquals(10, OptionsUtils.validateIntegerSetting(props, "a", 5, true));
        Assert.assertEquals(5, OptionsUtils.validateIntegerSetting(props, "b", 5, true));
        Assert.assertEquals(5, OptionsUtils.validateIntegerSetting(props, "x", 5, true));
    }
    
    @Test
    public void optionToList() {
        Assert.assertEquals(Arrays.asList(new String[]{"A", "B", "C"}),
                OptionsUtils.optionToList("A, B, C"));
        Assert.assertEquals(Arrays.asList(new String[]{"A", "B", "C"}),
                OptionsUtils.optionToList("A,B,C"));
        Assert.assertEquals(Arrays.asList(new String[]{"Apple", "Boom", "Cordowa"}),
                OptionsUtils.optionToList("Apple,Boom,Cordowa"));
        Assert.assertEquals(Arrays.asList(new String[]{"Big Boss", "Go to hell", "My home"}),
                OptionsUtils.optionToList("Big Boss, Go to hell, My home"));
    }
    
    @Test
    public void optionToSet() {
        Assert.assertEquals(
                new HashSet<>(Arrays.asList(new String[]{"A", "B", "C"})),
                OptionsUtils.optionToSet("A, B, C"));
        Assert.assertEquals(
                new HashSet<>(Arrays.asList(new String[]{"A", "B", "C"})),
                OptionsUtils.optionToSet("A,B,C"));
        Assert.assertEquals(
                new HashSet<>(Arrays.asList(new String[]{"Apple", "Boom", "Cordowa"})),
                OptionsUtils.optionToSet("Apple,Boom,Cordowa"));
        Assert.assertEquals(
                new HashSet<>(Arrays.asList(new String[]{"Big Boss", "Go to hell", "My home"})),
                OptionsUtils.optionToSet("Big Boss, Go to hell, My home"));
    }
    
    @Test
    public void optionToList_withDQuotes() {
        Assert.assertEquals(Arrays.asList(new String[]{"Big Boss", "Go to hell", "My home"}),
                OptionsUtils.optionToList("Big Boss, Go to hell, My home", false));

        Assert.assertEquals(Arrays.asList(new String[]{"Big Boss", "Go to hell", "My home"}),
                OptionsUtils.optionToList("Big Boss, Go to hell, My home", true));

        Assert.assertEquals(Arrays.asList(new String[]{"Big Boss", "Go to hell", "My home"}),
                OptionsUtils.optionToList("\"Big Boss\", \"Go to hell\", \"My home\"", true));

        Assert.assertEquals(
                Arrays.asList(new String[]{
                        "Boss", "Big Boss", "Chip, Dale, Monty, Gadget, Zipper"}),
                OptionsUtils.optionToList(
                        "\"Boss\", \"Big Boss\", \"Chip, Dale, Monty, Gadget, Zipper\"", true));
    }
    
    @Test
    public void optionToSet_withDQuotes() {
        Assert.assertEquals(new HashSet<>(
                Arrays.asList(new String[]{
                        "Big Boss", "Chip, Dale, Monty, Gadget, Zipper"})),
                OptionsUtils.optionToSet(
                        "\"Big Boss\", \"Chip, Dale, Monty, Gadget, Zipper\"", true, ","));
    }

    @Test
    public void optionToList_customSeparator() {
        Assert.assertEquals(Arrays.asList(new String[]{"A", "B", "C"}),
                OptionsUtils.optionToList("A B C", false, " ")); 
        Assert.assertEquals(Arrays.asList(new String[]{"A", "B", "C"}),
                OptionsUtils.optionToList("A, B, C", false, ",")); 
        Assert.assertEquals(Arrays.asList(new String[]{"A, B, C"}),
                OptionsUtils.optionToList("A, B, C", false, ";")); 
    }

    @Test
    public void optionToSet_customSeparator() {
        Assert.assertEquals(new HashSet<>(Arrays.asList(new String[]{"A", "B", "C"})),
                OptionsUtils.optionToSet("A B C", false, " ")); 
        Assert.assertEquals(new HashSet<>(Arrays.asList(new String[]{"A", "B", "C"})),
                OptionsUtils.optionToSet("A, B, C", false, ",")); 
        Assert.assertEquals(new HashSet<>(Arrays.asList(new String[]{"A, B, C"})),
                OptionsUtils.optionToSet("A, B, C", false, ";")); 
    }
}
