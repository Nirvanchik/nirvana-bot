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

import static org.wikipedia.nirvana.wiki.WikiUtils.addTextToTemplateLastNoincludeSectionBeforeCats;
import static org.wikipedia.nirvana.wiki.WikiUtils.findCategoriesSectionInTextRange;
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
    public void addTextToDiscussion_addsToBottomByDefault() {
        String discussion = "Old text is here\n";
        String topic = "New topic\n";
        String expected =
                "Old text is here\n" +
                "\n" +
                "New topic\n";
        Assert.assertEquals(expected, WikiUtils.addTextToDiscussion(topic, discussion));
    }
    
    @Test
    public void addTextToDiscussion_addsToBottomWhenNoNewLine() {
        String discussion = "Old text is here";
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
    public void addTextToDiscussion_addsToTopWhenTemplate2SpecifiedLocalized() {
        TestLocalizationManager.reset();
        Map<String, LocalizedTemplate> localizedTemplates = new HashMap<>();
        localizedTemplates.put("Новые сверху 2",
                new LocalizedTemplate("Новые сверху 2", "Add to top 2"));
        TestLocalizationManager.init(null, localizedTemplates);

        String discussion =
                "Old text is here\n" +
                "{{Add to top 2}}\n" +
                "Some other text";
        String topic = "New topic\n";
        String expected =
                "New topic\n" +
                "\n" +
                "Old text is here\n" +
                "{{Add to top 2}}\n" +
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
        Assert.assertEquals(null,
                WikiUtils.getPortalFromSettingsSubPage("Portal:A1"));
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
                "|val8 line2 \n" +  // should be merged to param8
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
                put("param8", "val8|val8 line2");
            }
        };
        boolean success = WikiUtils.parseBotTemplate("Templ", wikiText, params);
        Assert.assertTrue(success);
        Assert.assertEquals(expected, params);
    }
    
    @Test
    public void testParseBotTemplate_corruptBracketsNotMatch() {
        TreeMap<String, String> params = new TreeMap<>();
        String wikiText =
                "Some text\n" +
                "{{Templ\n" +
                "|param1 = val1\n" +
                "|param2 = val2\n" +
                "} some text here";
        boolean success = WikiUtils.parseBotTemplate("Templ", wikiText, params);
        Assert.assertFalse(success);
    }
    
    @Test
    public void testParseBotTemplate_templateNotFound() {
        TreeMap<String, String> params = new TreeMap<>();
        String wikiText =
                "Some text\n" +
                "{{Templ\n" +
                "|param1 = val1\n" +
                "|param2 = val2\n" +
                "} some text here";
        boolean success = WikiUtils.parseBotTemplate("Templ5", wikiText, params);
        Assert.assertFalse(success);
    }

    @SuppressWarnings("serial")
    @Test
    public void testParseBotTemplate_respectHtmlCodeInParams() {
        TreeMap<String, String> params = new TreeMap<>();
        String wikiText =
                "Some text\n" +
                "{{Templ\n" +
                "|подвал = \\n{|align=\"center\" style=\"margin-top:1em;\"\\n\n" +
                "|style=\"padding:0 1em 0 0\"|[[Image:Fil.svg|Арх]] [[Портал:Евр/Арх|Арх]]\\n" +
                "|[[Image:Rob.svg]] [[Портал:Евр/Нов/Параметры|Пар]]\\n|}\n" + 
                "|архив = Портал:Европейский союз/Новые статьи/Архив\n" +
                "}} some text here";
        TreeMap<String, String> expected = new TreeMap<String, String>() {
            {
                put("подвал", "\\n{|align=\"center\" style=\"margin-top:1em;\"\\n" +
                        "|style=\"padding:0 1em 0 0\"|[[Image:Fil.svg|Арх]] " +
                        "[[Портал:Евр/Арх|Арх]]\\n" +
                        "|[[Image:Rob.svg]] [[Портал:Евр/Нов/Параметры|Пар]]\\n|}");
                put("архив", "Портал:Европейский союз/Новые статьи/Архив");
            }
        };
        boolean success = WikiUtils.parseBotTemplate("Templ", wikiText, params);
        Assert.assertTrue(success);
        // These comparisons are for Eclipse nice visual diff.
        Assert.assertEquals(expected.get("подвал"), params.get("подвал"));
        Assert.assertEquals(expected.get("архив"), params.get("архив"));
        // Eclipse shows maps diff badly (cannot unfold it).
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
    public void testParseWikiTemplate_commentMustBeRemoved() {
        TreeMap<String, String> params = new TreeMap<>();
        String wikiText =
                "Some text\n" +
                "{{Templ\n" +
                "|param1 = val1 <!-- Dummy comment-->val1_end\n" +
                " |param2 = val2\n" +
                "}} some text here";
        boolean success = WikiUtils.parseWikiTemplate("Templ", wikiText, params);
        TreeMap<String, String> expected = new TreeMap<String, String>() {
            {
                put("param1", "val1 val1_end");
                put("param2", "val2");
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

    @Test
    public void testParseWikiTemplate_notFound() {
        TreeMap<String, String> params = new TreeMap<>();
        String wikiText = "xyz {{empl|param1=val1}}";
        boolean success = WikiUtils.parseWikiTemplate("Templ", wikiText, params);
        Assert.assertFalse(success);
    }

    @Test
    public void testParseWikiTemplate_brokenTemplate() {
        TreeMap<String, String> params = new TreeMap<>();
        String wikiText = "xyz {{Templ|param1=val1{{Some Text}}.";
        boolean success = WikiUtils.parseWikiTemplate("Templ", wikiText, params);
        Assert.assertFalse(success);
    }

    @Test
    public void removeNoWikiText() {
        Assert.assertEquals("Some text.",
                WikiUtils.removeNoWikiText("Some text."));
        Assert.assertEquals("Some text other text.",
                WikiUtils.removeNoWikiText("Some text<nowiki></nowiki> other text."));
        Assert.assertEquals("Some text other text.",
                WikiUtils.removeNoWikiText("Some text<nowiki>inside nowiki</nowiki> other text."));
        Assert.assertEquals("Some text",
                WikiUtils.removeNoWikiText("Some text<nowiki>inside nowiki other text."));
        Assert.assertEquals("Some text\n\n other text.",
                WikiUtils.removeNoWikiText(
                        "Some text\n<nowiki>inside nowiki</nowiki>\n other text."));
        Assert.assertEquals("Some text other text. More text.",
                WikiUtils.removeNoWikiText(
                        "Some text<nowiki>nowiki 1</nowiki> other text." +
                        "<nowiki>nowiki 2</nowiki> More text."));
    }

    @Test
    public void removeComments() {
        Assert.assertEquals("Some text other text.",
                WikiUtils.removeComments("Some text<!-- bla bla bla--> other text."));
        Assert.assertEquals("Some text\n other text.",
                WikiUtils.removeComments("Some text\n<!-- bla \nbla \nbla--> other text."));
        Assert.assertEquals("Some text other text. More text.",
                WikiUtils.removeComments(
                        "Some text<!-- comment 1--> other text." +
                        "<!-- comment 2 --> More text."));
    }

    @Test
    public void removePreTags() {
        Assert.assertEquals("Some text other text.",
                WikiUtils.removePreTags("Some text<pre></pre> other text."));
        Assert.assertEquals("Some text bla bla bla other text.",
                WikiUtils.removePreTags("Some text <pre>bla bla bla</pre> other text."));
        Assert.assertEquals("Some text\n bla \nbla \nbla other text.",
                WikiUtils.removePreTags("Some text\n<pre> bla \nbla \nbla</pre> other text."));
        Assert.assertEquals("Some text bla bla bla other text. bla bla bla More text.",
                WikiUtils.removePreTags(
                        "Some text <pre>bla bla bla</pre> other text. " +
                        "<pre>bla bla bla</pre> More text."));
        Assert.assertEquals("Some text other text.",
                WikiUtils.removePreTags("Some text<pre> other text."));
        Assert.assertEquals("Some text other text.",
                WikiUtils.removePreTags("Some text</pre> other text."));
    }

    @Test
    public void addPreTags() {
        Assert.assertEquals("<pre>\nSome text.</pre>\n",
                WikiUtils.addPreTags("Some text."));
        Assert.assertEquals("<pre>\nSome text.\nOther text.\n</pre>\n",
                WikiUtils.addPreTags("Some text.\nOther text.\n"));
    }

    @Test
    public void testFindCategoriesSectionInTextRange() {
        String wikiText = "";
        Assert.assertEquals(0,
                findCategoriesSectionInTextRange(null, wikiText, 0, wikiText.length()));
        wikiText = "Blablabla\n\nxoxo";
        Assert.assertEquals(15,
                findCategoriesSectionInTextRange(null, wikiText, 0, wikiText.length()));
        Assert.assertEquals(5,
                findCategoriesSectionInTextRange(null, wikiText, 0, 5));
        wikiText = "Blabla\n" +
                "[[Category:Godo]]\n" +
                "xoxo";
        Assert.assertEquals(7,
                findCategoriesSectionInTextRange(null, wikiText, 0, wikiText.length()));
        Assert.assertEquals(7,
                findCategoriesSectionInTextRange("Категория", wikiText, 0, wikiText.length()));
        Assert.assertEquals(5,
                findCategoriesSectionInTextRange(null, wikiText, 0, 5));
        Assert.assertEquals(5,
                findCategoriesSectionInTextRange("Категория", wikiText, 0, 5));
        wikiText = "Blabla\n" +
                "[[Категория:Капуста]]\n" +
                "xoxo";
        Assert.assertEquals(7,
                findCategoriesSectionInTextRange("Категория", wikiText, 0, wikiText.length()));
        Assert.assertEquals(5,
                findCategoriesSectionInTextRange("Категория", wikiText, 0, 5));
        wikiText = "Blabla\n" +
                "[[Категория:Капуста]]\n" +
                "[[Категория:Баклажан]]\n" +
                "xoxo";
        Assert.assertEquals(7,
                findCategoriesSectionInTextRange("Категория", wikiText, 0, wikiText.length()));
        wikiText = "Blabla\n" +
                "[[Категория:Капуста]]\n" +
                "[[Category:Godo]]\n" +
                "xoxo";
        Assert.assertEquals(7,
                findCategoriesSectionInTextRange("Категория", wikiText, 0, wikiText.length()));
        wikiText = "Blabla\n" +
                "[[Category:Godo]]\n" +
                "[[Категория:Капуста]]\n" +
                "xoxo";
        Assert.assertEquals(7,
                findCategoriesSectionInTextRange("Категория", wikiText, 0, wikiText.length()));
    }

    @Test
    public void testAddTextToTemplateLastNoincludeSectionBeforeCats() {
        String text = "";
        String expe = "<noinclude>Abc</noinclude>";
        String result = addTextToTemplateLastNoincludeSectionBeforeCats(null, text, "Abc");
        Assert.assertEquals(expe, result);

        text = "Mama go home.";
        expe = "Mama go home.<noinclude>Abc</noinclude>";
        result = addTextToTemplateLastNoincludeSectionBeforeCats(null, text, "Abc");
        Assert.assertEquals(expe, result);

        text = "Mama go home.<noinclude>Some text.</noinclude>";
        expe = "Mama go home.<noinclude>Some text.\nAbc\n</noinclude>";
        result = addTextToTemplateLastNoincludeSectionBeforeCats(null, text, "Abc");
        Assert.assertEquals(expe, result);

        text = "Mama go home.<noinclude>Some text.</noinclude>...\n" +
                "<noinclude>Another text.</noinclude>";
        expe = "Mama go home.<noinclude>Some text.</noinclude>...\n" +
                "<noinclude>Another text.\n" +
                "Abc\n" +
                "</noinclude>";
        result = addTextToTemplateLastNoincludeSectionBeforeCats(null, text, "Abc");
        Assert.assertEquals(expe, result);

        text = "Mama go home.<noinclude>Some text.</noinclude>...\n" +
                "<noinclude>Another text.\n" +
                "</noinclude>";
        expe = "Mama go home.<noinclude>Some text.</noinclude>...\n" +
                "<noinclude>Another text.\n" +
                "Abc\n" +
                "</noinclude>";
        result = addTextToTemplateLastNoincludeSectionBeforeCats(null, text, "Abc");
        Assert.assertEquals(expe, result);

        text = "Mama go home.<noinclude>Some text.</noinclude>...\n" +
                "<noinclude>Another text." +
                "[[Category:Home]]" +
                "</noinclude>";
        expe = "Mama go home.<noinclude>Some text.</noinclude>...\n" +
                "<noinclude>Another text.\n" +
                "Abc\n" +
                "[[Category:Home]]" +
                "</noinclude>";
        result = addTextToTemplateLastNoincludeSectionBeforeCats(null, text, "Abc");
        Assert.assertEquals(expe, result);

        text = "Mama go home.<noinclude>Some text.</noinclude>...\n" +
                "<noinclude>Another text.\n" +
                "[[Category:Home]]" +
                "</noinclude>";
        expe = "Mama go home.<noinclude>Some text.</noinclude>...\n" +
                "<noinclude>Another text.\n" +
                "Abc\n" +
                "[[Category:Home]]" +
                "</noinclude>";
        result = addTextToTemplateLastNoincludeSectionBeforeCats(null, text, "Abc");
        Assert.assertEquals(expe, result);

        text = "Mama go home.<noinclude>Some text.</noinclude>...\n" +
                "<noinclude>Another text.\n" +
                "\n" +
                "[[Category:Home]]" +
                "</noinclude>";
        expe = "Mama go home.<noinclude>Some text.</noinclude>...\n" +
                "<noinclude>Another text.\n" +
                "Abc\n" +
                "[[Category:Home]]" +
                "</noinclude>";
        result = addTextToTemplateLastNoincludeSectionBeforeCats(null, text, "Abc");
        Assert.assertEquals(expe, result);

        // Bad case. We must not crash here, just do our job.
        text = "Mama go home.<noinclude>Some text.</noinclude>...\n" +
                "<noinclude>Another text.\n" +
                "\n" +
                "[[Category:Home]]";
        expe = "Mama go home.<noinclude>Some text.</noinclude>...\n" +
                "<noinclude>Another text.\n" +
                "Abc\n" +
                "[[Category:Home]]";
        result = addTextToTemplateLastNoincludeSectionBeforeCats(null, text, "Abc");
        Assert.assertEquals(expe, result);
    }
}
