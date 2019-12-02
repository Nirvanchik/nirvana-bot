/**
 *  @(#)XmlToolsTest.java
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
 * Tests for {@link XmlTools}.
 *
 */
public class XmlToolsTest {

    /**
     * Test method for {@link org.wikipedia.nirvana.util.XmlTools#removeEscape(java.lang.String)}.
     */
    @Test
    public void testRemoveEscape() {
        Assert.assertEquals("Some title with \"word\"",
                XmlTools.removeEscape("Some title with &quot;word&quot;"));
        Assert.assertEquals("With quoted '@'",
                XmlTools.removeEscape("With quoted &#039;@&#039;"));
        Assert.assertEquals("With &this",
                XmlTools.removeEscape("With &amp;this"));
    }
}
