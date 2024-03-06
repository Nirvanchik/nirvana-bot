/**
 *  @(#)TemplateFindItemTest.java
 *  Copyright © 2024 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.nirvanabot.templates;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link TemplateFindItem}.
 *
 */
public class TemplateFindItemTest {

    /**
     * Test method for {@link 
     * TemplateFindItem#parseTemplateFindData(java.lang.String)}.
     */
    @Test
    public void testParseTemplateFindData() throws Exception {
        TemplateFindItem item = TemplateFindItem.parseTemplateFindData("Money/type/coin");
        Assert.assertEquals(new TemplateFindItem("Money", "type", "coin"), item);
        Assert.assertNotEquals(new TemplateFindItem("Money", "type", "A"), item);

        item = TemplateFindItem.parseTemplateFindData("Money / type / coin");
        Assert.assertEquals(new TemplateFindItem("Money", "type", "coin"), item);

        item = TemplateFindItem.parseTemplateFindData("Money/type/");
        Assert.assertEquals(new TemplateFindItem("Money", "type", ""), item);

        item = TemplateFindItem.parseTemplateFindData("Money/type");
        Assert.assertEquals(new TemplateFindItem("Money", "type", ""), item);

        item = TemplateFindItem.parseTemplateFindData("Money//");
        Assert.assertEquals(new TemplateFindItem("Money", "", ""), item);

        item = TemplateFindItem.parseTemplateFindData("Money/");
        Assert.assertEquals(new TemplateFindItem("Money", "", ""), item);

        item = TemplateFindItem.parseTemplateFindData("Money");
        Assert.assertEquals(new TemplateFindItem("Money", "", ""), item);
    }

}
