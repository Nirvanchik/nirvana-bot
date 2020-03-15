/**
 *  @(#)NumberToolsTest.java
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

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link NumberTools}.
 *
 */
public class NumberToolsTest {

    @Test
    public void testParseFileSize() {
        Assert.assertEquals(1, NumberTools.parseFileSize("1"));
        Assert.assertEquals(100, NumberTools.parseFileSize("100"));
        Assert.assertEquals(12345, NumberTools.parseFileSize("12345"));

        Assert.assertEquals(2048, NumberTools.parseFileSize("2 Kb"));
        Assert.assertEquals(2048, NumberTools.parseFileSize("2 K"));
        Assert.assertEquals(2048, NumberTools.parseFileSize("2K"));
        Assert.assertEquals(2048, NumberTools.parseFileSize("2 KB"));
        Assert.assertEquals(2048, NumberTools.parseFileSize("2 Кб"));
        Assert.assertEquals(2048, NumberTools.parseFileSize("2 КБ"));

        Assert.assertEquals(2097152, NumberTools.parseFileSize("2 Mb"));
        Assert.assertEquals(2097152, NumberTools.parseFileSize("2 M"));
        Assert.assertEquals(2097152, NumberTools.parseFileSize("2M"));
        Assert.assertEquals(2097152, NumberTools.parseFileSize("2 MB"));
        Assert.assertEquals(2097152, NumberTools.parseFileSize("2 Мб"));
        Assert.assertEquals(2097152, NumberTools.parseFileSize("2 МБ"));
    }

    @Test
    public void formatFloat1OptionalFractionDigit() {
        Assert.assertEquals("1", NumberTools.formatFloat1OptionalFractionDigit(1f));
        Assert.assertEquals("1.5", NumberTools.formatFloat1OptionalFractionDigit(1.5f));
        Assert.assertEquals("1.3", NumberTools.formatFloat1OptionalFractionDigit(1.333f));
        Assert.assertEquals("1.4", NumberTools.formatFloat1OptionalFractionDigit(1.3999f));
    }

}
