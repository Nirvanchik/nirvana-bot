/**
 *  @(#)ImageFinderInTemplatesTest.java 17.02.2017
 *  Copyright © 2017 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.nirvanabot.imagefinder;

import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.localization.TestLocalizationManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit-tests for {@link ImageFinderInTemplates}.
 */
public class ImageFinderInTemplatesTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        TestLocalizationManager.init(Localizer.DEFAULT_LOCALIZATION);
    }

    @After
    public void tearDown() throws Exception {
        TestLocalizationManager.reset();
    }

    @Test
    public void findsImageInTemplate_oneParam() {
        ImageFinderInTemplates finder = new ImageFinderInTemplates();

        String image = finder.checkImageIsImageTemplate("{{часть изображения|изобр=Cow.png}}");

        Assert.assertEquals("Cow.png", image);
    }

    @Test
    public void findsImageInTemplate_spaces() {
        ImageFinderInTemplates finder = new ImageFinderInTemplates();

        String image = finder.checkImageIsImageTemplate("{{часть изображения| изобр = Cow.png }}");

        Assert.assertEquals("Cow.png", image);
    }

    @Test
    public void findsImageInTemplate_manyParams() {
        ImageFinderInTemplates finder = new ImageFinderInTemplates();

        String image = finder.checkImageIsImageTemplate(
                "{{часть изображения|нечто=xyz|изобр=Cow.png|папа=мама}}");
        
        Assert.assertEquals("Cow.png", image);
    }

    @Test
    public void imageNotFound() {
        ImageFinderInTemplates finder = new ImageFinderInTemplates();

        String image = finder.checkImageIsImageTemplate("{{часть изображения|гобо=добо}}");

        Assert.assertEquals(null, image);
    }
    
    @Test
    public void templateNotFound() {
        ImageFinderInTemplates finder = new ImageFinderInTemplates();

        String image = finder.checkImageIsImageTemplate("Cow.png");

        Assert.assertEquals("Cow.png", image);
    }
}
