/**
 *  @(#)NamespaceUtilsTest.java
 *  Copyright © 2020 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.localization.TestLocalizationManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link NamespaceUtils}.
 *
 */
public class NamespaceUtilsTest {

    @Before
    public void setUp() throws Exception {
        TestLocalizationManager.init(Localizer.DEFAULT_LOCALIZATION);
    }

    @After
    public void tearDown() throws Exception {
        TestLocalizationManager.reset();
    }

    @Test
    public void testUserNamespace() {
        Assert.assertTrue(NamespaceUtils.userNamespace("Участник:Гоша"));
        Assert.assertTrue(NamespaceUtils.userNamespace("Обсуждение участника:Гоша"));
        Assert.assertTrue(NamespaceUtils.userNamespace("Обсуждение_участника:Гоша"));
        Assert.assertTrue(NamespaceUtils.userNamespace("User:Sam"));
        Assert.assertTrue(NamespaceUtils.userNamespace("User_talk:Sam"));
        Assert.assertFalse(NamespaceUtils.userNamespace("Федор"));
        Assert.assertFalse(NamespaceUtils.userNamespace("Проект:Петруха"));
    }

    @Test
    public void testCategoryNamespace() {
        Assert.assertTrue(NamespaceUtils.categoryNamespace("Категория:Гоша"));
        Assert.assertTrue(NamespaceUtils.categoryNamespace("Category:Sam"));
        Assert.assertFalse(NamespaceUtils.categoryNamespace("Федор"));
        Assert.assertFalse(NamespaceUtils.categoryNamespace("Участник:Гоша"));
        Assert.assertFalse(NamespaceUtils.categoryNamespace("Обсуждение_участника:Гоша"));
        Assert.assertFalse(NamespaceUtils.categoryNamespace("User:Sam"));        
        Assert.assertFalse(NamespaceUtils.categoryNamespace("Проект:Петруха"));
    }

}
