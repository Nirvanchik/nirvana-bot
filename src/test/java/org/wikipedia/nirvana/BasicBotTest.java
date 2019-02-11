/**
 *  @(#)BasicBotTest.java 26 January 2019 г.
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

package org.wikipedia.nirvana;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

/**
 * Unit tests for {@link BasicBot}.
 */
public class BasicBotTest {

    @Test
    public void testGetUserTemplateRe_findsWhenTemplateNoNs() {
        Pattern p = Pattern.compile(BasicBot.getUserTemplateRe("MyBot", null));
        String text = "Xyz.  {{User:MyBot|param1=value1}} blablabla";
        Assert.assertTrue(p.matcher(text).find());
    }

    @Test
    public void testGetUserTemplateRe_findsWhenTemplateWithEnNs() {
        Pattern p = Pattern.compile(BasicBot.getUserTemplateRe("User:MyBot", null));
        String text = "Xyz.  {{User:MyBot|param1=value1}} blablabla";
        Assert.assertTrue(p.matcher(text).find());
    }

    @Test
    public void testGetUserTemplateRe_findsLocalizedNsTitleNoNs() {
        Pattern p = Pattern.compile(BasicBot.getUserTemplateRe("MyBot", "Участник"));
        
        String text = "Xyz.  {{User:MyBot|param1=value1}} blablabla";
        Assert.assertTrue(p.matcher(text).find());
        
        text = "Xyz.  {{Участник:MyBot|param1=value1}} blablabla";
        Assert.assertTrue(p.matcher(text).find());
    }

    @Test
    public void testGetUserTemplateRe_findsLocalizedNsTitleEnNs() {
        Pattern p = Pattern.compile(BasicBot.getUserTemplateRe("User:MyBot", "Участник"));
        
        String text = "Xyz.  {{User:MyBot|param1=value1}} blablabla";
        Assert.assertTrue(p.matcher(text).find());
        
        text = "Xyz.  {{Участник:MyBot|param1=value1}} blablabla";
        Assert.assertTrue(p.matcher(text).find());
    }

    @Test
    public void testGetUserTemplateRe_findsLocalizedNsTitleLocNs() {
        Pattern p = Pattern.compile(BasicBot.getUserTemplateRe("Участник:MyBot", "Участник"));
        
        String text = "Xyz.  {{User:MyBot|param1=value1}} blablabla";
        Assert.assertTrue(p.matcher(text).find());
        
        text = "Xyz.  {{Участник:MyBot|param1=value1}} blablabla";
        Assert.assertTrue(p.matcher(text).find());
    }

}
