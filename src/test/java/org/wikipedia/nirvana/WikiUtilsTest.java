/**
 *  @(#)WikiUtilsTest.java 13.04.2017
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

package org.wikipedia.nirvana;

import org.wikipedia.nirvana.localization.LocalizedTemplate;
import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.localization.TestLocalizationManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit-tests for {@link WikiUtils}.
 */
public class WikiUtilsTest {

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
    public void containsTemplate() {
        Assert.assertFalse(WikiUtils.containsTemplate(null, "t"));
        Assert.assertFalse(WikiUtils.containsTemplate("", "t"));
        Assert.assertTrue(WikiUtils.containsTemplate("sometext{{templ}}othertext", "templ"));
        Assert.assertTrue(WikiUtils.containsTemplate("sometext {{templ}} othertext", "templ"));
        Assert.assertTrue(WikiUtils.containsTemplate("sometext{{templ|params}}othertex", "templ"));
        Assert.assertTrue(WikiUtils.containsTemplate("sometext\n{{templ}}\nothertext", "templ"));
        Assert.assertTrue(WikiUtils.containsTemplate("sometext{{Templ}}othertext", "templ"));
        Assert.assertTrue(WikiUtils.containsTemplate("sometext{{Templ}}othertext", "Templ"));
        Assert.assertTrue(WikiUtils.containsTemplate("sometext{{templ}}othertext", "Templ"));
        Assert.assertTrue(WikiUtils.containsTemplate("текст{{шаблон}}текст", "шаблон"));
        Assert.assertTrue(WikiUtils.containsTemplate("текст{{шаблон|параметр}}текст", "шаблон"));
        Assert.assertTrue(WikiUtils.containsTemplate("текст{{Шаблон}}текст", "шаблон"));
        Assert.assertTrue(WikiUtils.containsTemplate("текст{{Шаблон}}текст", "Шаблон"));
        Assert.assertTrue(WikiUtils.containsTemplate("текст{{шаблон}}текст", "Шаблон"));
    }

    @Test
    public void addTextToDiscussion_addsToNullDiscussion() {
        Assert.assertEquals("new text", WikiUtils.addTextToDiscussion("new text", null));
    }

    @Test
    public void addTextToDiscussion_addsToEmptyDiscussion() {
        Assert.assertEquals("new text", WikiUtils.addTextToDiscussion("new text", ""));
    }

    @Test
    public void addTextToDiscussion_addsToBottomWhenNotSpecified() {
        String discussion = "Old text is here\n";
        String topic = "New topic\n";
        String expected =
                "Old text is here\n" +
                "\n" +
                "New topic\n";
        Assert.assertEquals(expected, WikiUtils.addTextToDiscussion(topic, discussion));
    }

    @Test
    public void addTextToDiscussion_addsToTopWhenTemplate1Specified() {
        String discussion =
                "Old text is here\n" +
                "{{Новые сверху}}\n" +
                "Some other text";
        String topic = "New topic\n";
        String expected =
                "New topic\n" +
                "\n" +
                "Old text is here\n" +
                "{{Новые сверху}}\n" +
                "Some other text";
        Assert.assertEquals(expected, WikiUtils.addTextToDiscussion(topic, discussion));
    }

    @Test
    public void addTextToDiscussion_addsToTopWhenTemplate1SpecifiedLocalized() {
        TestLocalizationManager.reset();
        Map<String, LocalizedTemplate> localizedTemplates = new HashMap<>();
        localizedTemplates.put("Новые сверху", new LocalizedTemplate("Add to top", ""));
        TestLocalizationManager.init(null, localizedTemplates);

        String discussion =
                "Old text is here\n" +
                "{{Add to top}}\n" +
                "Some other text";
        String topic = "New topic\n";
        String expected =
                "New topic\n" +
                "\n" +
                "Old text is here\n" +
                "{{Add to top}}\n" +
                "Some other text";
        Assert.assertEquals(expected, WikiUtils.addTextToDiscussion(topic, discussion));
    }

    @Test
    public void addTextToDiscussion_addsToTopWhenTemplate2Specified() {
        String discussion =
                "Old text is here\n" +
                "{{Новые сверху 2}}\n" +
                "Some other text";
        String topic = "New topic\n";
        String expected =
                "New topic\n" +
                "\n" +
                "Old text is here\n" +
                "{{Новые сверху 2}}\n" +
                "Some other text";
        Assert.assertEquals(expected, WikiUtils.addTextToDiscussion(topic, discussion));
    }

    @Test
    public void getPortalFromSettingsSubPage() {
        Assert.assertEquals("Portal:A1/Project B", 
                WikiUtils.getPortalFromSettingsSubPage("Portal:A1/Project B/New Pages/Settings"));
        Assert.assertEquals("Portal:A1",
                WikiUtils.getPortalFromSettingsSubPage("Portal:A1/New Pages/Settings"));
        Assert.assertEquals("Portal:A1",
                WikiUtils.getPortalFromSettingsSubPage("Portal:A1/Settings"));
    }

    @Test
    public void removeCategories() {
        Assert.assertEquals("Ho\n\nBo\n",
                WikiUtils.removeCategories("Ho\n[[Category:Cat]]\nBo\n"));
        Assert.assertEquals("Ho\n\n\nBo\n",
                WikiUtils.removeCategories("Ho\n[[Category:Cat1]]\n[[Category:Cat2]]\nBo\n"));
        Assert.assertEquals("Ho\n\nBo\n",
                WikiUtils.removeCategories("Ho\n[[Категория:Кат]]\nBo\n"));
        Assert.assertEquals("Ho\n\n\nBo\n",
                WikiUtils.removeCategories("Ho\n[[Категория:Кат1]]\n[[Категория:Кат2]]\nBo\n"));
    }

}
