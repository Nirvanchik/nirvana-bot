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

package org.wikipedia.nirvana.wiki;

import static org.wikipedia.nirvana.wiki.WikiUtils.findMatchingBraceOfTemplate;

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
import java.util.TreeMap;

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
        localizedTemplates.put("Новые сверху",
                new LocalizedTemplate("Новые сверху", "Add to top"));
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

    @Test
    public void testFindMatchingBraceOfTemplate() {
        Assert.assertEquals(3, findMatchingBraceOfTemplate("{{T}}", 2));
        Assert.assertEquals(13, findMatchingBraceOfTemplate("{{Templ|param}}", 2));
        Assert.assertEquals(13, findMatchingBraceOfTemplate("{{Templ|param}}", 7));
        Assert.assertEquals(17, findMatchingBraceOfTemplate("{{Templ|p1=a|p2=b}}", 7));
        Assert.assertEquals(19, findMatchingBraceOfTemplate("{{Templ\n|p1=a\n|p2=b}}", 7));
        Assert.assertEquals(23, findMatchingBraceOfTemplate("{{Templ\n|p1={{a}}\n|p2=b}}", 7));
        Assert.assertEquals(23, findMatchingBraceOfTemplate("{{Templ|p1={{a{{Xyz}}}}}}", 7));
        Assert.assertEquals(23, findMatchingBraceOfTemplate("{{Templ|p1={{{{Xyz}}d}}}}", 7));
        
        Assert.assertEquals(-1, findMatchingBraceOfTemplate("{{T}sdgsdg", 2));
        Assert.assertEquals(-1, findMatchingBraceOfTemplate("{{T{{x}}|b=c}sdgsdg", 2));
    }

    @SuppressWarnings("serial")
    @Test
    public void testParseBotTemplate() {
        TreeMap<String, String> params = new TreeMap<>();
        String wikiText =
                "Some text\n" +
                "{{Templ\n" +
                "|param1 = val1\n" +
                " |param2 = val2\n" +
                "\t|param3 = val3\n" +
                " |param4 = val4 val4 val4\n" +
                " |param5 = val5 line1\n" +
                "val5 line2\n" +
                " |param6 = val6 {{Templ2}}\n" +
                " |param7 = val7 | line1\n" +
                " val7 |line2\n" +
                " |param8 = val8\n" +
                "}} some text here";
        TreeMap<String, String> expected = new TreeMap<String, String>() {
            {
                put("param1", "val1");
                put("param2", "val2");
                put("param3", "val3");
                put("param4", "val4 val4 val4");
                put("param5", "val5 line1\nval5 line2");
                put("param6", "val6 {{Templ2}}");
                put("param7", "val7 | line1\n val7 |line2");
                put("param8", "val8");
            }
        };
        boolean success = WikiUtils.parseBotTemplate("Templ", wikiText, params);
        Assert.assertTrue(success);
        Assert.assertEquals(expected, params);
    }

    @SuppressWarnings("serial")
    @Test
    public void testParseWikiTemplate_withNewLine() {
        TreeMap<String, String> params = new TreeMap<>();
        String wikiText =
                "Some text\n" +
                "{{Templ\n" +
                "|param1 = val1\n" +
                " |param2 = val2\n" +
                "\t|param3 = val3\n" +
                " |param4 = val4 val4 val4\n" +
                " |param5 = val5 line1\n" +
                "val5 line2\n" +
                " |param6 = val6 {{Templ2}}\n" +
                " |param7 = val7 | param8\n" +
                " line2 |param9\n" +
                " |param10 = val10\n" +
                "}} some text here";
        boolean success = WikiUtils.parseWikiTemplate("Templ", wikiText, params);
        TreeMap<String, String> expected = new TreeMap<String, String>() {
            {
                put("param1", "val1");
                put("param2", "val2");
                put("param3", "val3");
                put("param4", "val4 val4 val4");
                put("param5", "val5 line1\nval5 line2");
                put("param6", "val6 {{Templ2}}");
                put("param7", "val7");
                put("param8\n line2", null);
                put("param9", null);
                put("param10", "val10");
            }
        };        
        Assert.assertTrue(success);
        Assert.assertEquals(expected, params);
    }

    @SuppressWarnings("serial")
    @Test
    public void testParseWikiTemplate_oneLine() {
        TreeMap<String, String> params = new TreeMap<>();
        String wikiText = "xyz {{Templ|param1=val1|A|x=y|z={{t2}}}}";
        boolean success = WikiUtils.parseWikiTemplate("Templ", wikiText, params);
        TreeMap<String, String> expected = new TreeMap<String, String>() {
            {
                put("param1", "val1");
                put("A", null);
                put("x", "y");
                put("z", "{{t2}}");
            }
        };
        Assert.assertTrue(success);
        Assert.assertEquals(expected, params);
    }
}
