/**
 *  @(#)WikiUtils.java 25.08.2015
 *  Copyright © 2015 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.wikipedia.nirvana.localization.LocalizedTemplate;
import org.wikipedia.nirvana.localization.Localizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * Utility static methods for processing Wiki pages.
 * Examples:
 * - parse wiki template on wiki page;
 * - remove comments;
 * - add text to Talk Page;
 * - search/remove some tags/sections/blocks on wiki page.
 */
public class WikiUtils {
    protected static final Logger log;

    private static final Pattern COMMENT_PATTERN = Pattern.compile("<!--(.+?)-->", Pattern.DOTALL);

    static {
        log = LogManager.getLogger(WikiUtils.class.getName());
    }

    /**
     * Finds matching double brace in wiki page with template.
     *
     * Used to find template ending in wiki code.
     * The code handles nested templates but doesn't handle
     * escaped blocks or nowiki/code sections.
     *
     * @param text Any wiki text with template.
     * @param start Start index where to start search. Must point after opening bracket ("{{").
     * @return Index where matching brace detected.
     */
    public static int findMatchingBraceOfTemplate(String text, int start) {
        int begin = start;
        int end = -1;
        int level = 1;
        for (int i = begin; i < text.length() - 1; ++i) {
            if (text.charAt(i) == '{' && text.charAt(i + 1) == '{') {
                ++level;
                ++i;
            } else if (text.charAt(i) == '}' && text.charAt(i + 1) == '}') {
                --level;
                if (level == 0) {
                    end = i;
                    break;
                }
                ++i;
            }
        }
        return end;
    }

    /**
     * Parse bot template from wiki page (the first one if multiple found).
     * Bot template has a special format:
     * - template parameters can be multiline;
     * - pipe symbol ("|") is considered as a regular text if no "=" symbol found before next pipe
     *   symbol;
     * - template parameters are always specified on new line
     *
     * @param templateRegex Regex to find template beginning in wiki text.
     * @param text Any wiki text where to parse template from.
     * @param parameters A key/value map where parameters will be stored.
     * @return true if template found and parsed successfully, false if not found.
     */
    public static boolean parseBotTemplate(String templateRegex, String text,
            Map<String, String> parameters) {
        return parseTemplateImpl(templateRegex, text, parameters, true);
    }

    /**
     * Parse wiki template from wiki page (the first one if multiple found).
     *
     * @param templateRegex Regex to find template beginning in wiki text.
     * @param text Any wiki text where to parse template from.
     * @param parameters A key/value map where parameters will be stored.
     * @return true if template found and parsed successfully, false if not found.
     */
    public static boolean parseWikiTemplate(String templateRegex, String text,
            Map<String, String> parameters) {
        return parseTemplateImpl(templateRegex, text, parameters, false);
    }

    private static boolean parseTemplateImpl(String templateRegex, String text,
            Map<String, String> parameters, boolean botTemplate) {
        // Bot template looks like:
        // {{Template
        //   | param1 = val1
        //   | param2 = val2
        //   | param3 = Val3: very long
        //     multiline srting
        //   | param4 = Val4: In the middle you can also find | symbol but it isn't param separator
        //   | it is strange, but this is treated as a continuation of Val4
        //   | param5 =
        // }}
        // Wiki template looks like:
        // {{Template
        //   | param1
        //   | param2 = value2
        // }}
        // or
        // {{Template | param1 | param2 = value2}}
        boolean splitByNewLine = botTemplate;
        String str = "(\\{\\{" + templateRegex + ")(.+)$"; // GOOD
        Pattern pattern = Pattern.compile(str, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(text);
        if (!m.find()) {
            return false;
        }

        int begin = m.end(1);
        int end = findMatchingBraceOfTemplate(text, begin);
        if (end == -1) {
            return false;
        }        
        
        String parameterString = text.substring(begin, end);
        String splitParam = "\\|";
        if (splitByNewLine) {
            splitParam = "\\n\\s*\\|";
        }
        String[] parts = parameterString.split(splitParam);
        String lastKey = "";
        for (int i = 1; i < parts.length; i++) {
            String p = parts[i];
            String[] eqParts = p.split("=", 2);
            if (eqParts.length > 1) {
                String key = eqParts[0].trim().toLowerCase();
                // TODO: Do we still need this ugly workaround?
                if (botTemplate && (key.equals("align") || key.equals("style"))) {
                    appendToLastVal(parameters, lastKey, p);
                } else {
                    String value = removeComments(eqParts[1]).trim();
                    parameters.put(key, value);
                    if (botTemplate) {
                        lastKey = key;
                    }
                }
            } else {
                if (botTemplate) {
                    if (!lastKey.isEmpty()) {
                        appendToLastVal(parameters, lastKey, p);
                    }
                } else {
                    String key = p.trim();
                    if (!key.isEmpty()) {
                        parameters.put(key, null);
                    }
                }
            }  
        }
        return true;
    }

    private static void appendToLastVal(Map<String, String> parameters, String lastKey,
            String value) {
        value = removeComments(value).trim();
        String lastVal = parameters.get(lastKey) + "|" + value;
        parameters.put(lastKey, lastVal);
    }

    /**
     * Removes all &lt;nowiki&gt; tagged sections from wiki text.
     *
     * If nested "nowiki" tags exist in text the result will be unknown.
     * Use on your own risk.
     *
     * @param text Any wiki text.
     * @return Updated text with "nowiki" tagged sections deleted.
     */
    public static String removeNoWikiText(String text) {
        String result = text;
        boolean changed = false;
        StringBuilder sb = new StringBuilder();
        int index = text.indexOf("<nowiki>");
        int lastNoWikiEnd = 0;
        while (index >= 0 && index < text.length()) {
            changed = true;
            sb.append(text.substring(lastNoWikiEnd, index));
            
            int end = text.indexOf("</nowiki>", index);
            if (end < 0) {
                lastNoWikiEnd = text.length();
                break;
            } 
            lastNoWikiEnd = end + "</nowiki>".length();
            
            index = text.indexOf("<nowiki>", lastNoWikiEnd);            
        } 
        if (changed) {
            if (lastNoWikiEnd < text.length()) {
                sb.append(text.substring(lastNoWikiEnd, text.length()));
            }
            result = sb.toString();
        }
        return result;
    }
    
    /**
     * Removes html comments from text.
     *
     * @param text Any wiki or html text.
     * @return Updated text with html comments deleted.
     */
    public static String removeComments(String text) {
        return COMMENT_PATTERN.matcher(text).replaceAll("");
    }

    /**
     * Removes &lt;pre&gt; tags from wiki text.
     *
     * All &lt;pre&gt; and &lt;/pre&gt; tags tags will be deleted from text.
     *
     * @param text where pre tags will be removed
     * @return Updated wiki text with "pre" tags removed.
     */
    public static String removePreTags(String text) {
        return text.replace("<pre>", "").replace("</pre>", "");
    }

    /**
     * Adds &lt;pre&gt; tags to some wiki text.
     *
     * @param text any text (wiki code).
     * @return the same text inside of &lt;pre&gt; tags.
     */
    public static String addPreTags(String text) {
        return "<pre>\n" + text + "</pre>\n";
    }

    /**
     * Removes categories from page text (usually specified at the bottom of a page).
     */
    public static String removeCategories(String text) {
        Localizer localizer = Localizer.getInstance();
        String categoryKey = localizer.localize("Категория:");
        String re = "\\[\\[(Category:|" + categoryKey + ")[^\\]]+\\]\\]";
        Pattern p = Pattern.compile(re);
        return p.matcher(text).replaceAll("");
    }

    /**
     * Adds text to last &lt;noinclude&gt; section before categories section in template code.
     *
     * If &lt;noinclude&gt; section is absent it will be created.
     *
     * @param categoryKey Localized name of Category namespace.
     * @param templateText Template wiki code.
     * @param insertText Text to insert in template.
     * @return Updated template code with inserted text.
     */
    public static String addTextToTemplateLastNoincludeSectionBeforeCats(
            @Nullable String categoryKey, String templateText, String insertText) {
        String text;
        String separator = "\n";
        String separator2 = "\n";
        int indexOfLastNoinclude = templateText.lastIndexOf("<noinclude>");
        if (indexOfLastNoinclude < 0) {
            text = templateText + "<noinclude>" + insertText + "</noinclude>";
        } else {
            int indexOfLastNoincludeCloser = templateText.indexOf("</noinclude",
                    indexOfLastNoinclude);
            if (indexOfLastNoincludeCloser < 0) {
                log.warn("Oops! The template code doesn't close <noinclude> section!");
                indexOfLastNoincludeCloser = templateText.length();
            }
            // here we have a range of (indexOfLastNoinclude,indexOfLastNoincludeCloser)
            int insertPlace = findCategoriesSectionInTextRange(
                    categoryKey, templateText, indexOfLastNoinclude, indexOfLastNoincludeCloser);
            if (insertPlace > 0 && templateText.charAt(insertPlace - 1) == '\n') {
                insertPlace--;
                separator2 = "";
            }
            if (insertPlace > 0 && templateText.charAt(insertPlace - 1) == '\n') {
                separator = "";
            }
            String start = templateText;
            String end = "";
            if (insertPlace < templateText.length()) {
                start = templateText.substring(0, insertPlace);
                end = templateText.substring(insertPlace);
            }
            text = start +
                    separator +
                    insertText +
                    separator2 +
                    end;
        }
        return text;
    }

    /**
     * Finds the beginning of "Category" section in wiki page fragment.
     *
     * Category section beginning is described as the first English category record
     * ([[Category:<>]]) or similar record localized to language ([[Категория:<>]]).
     * Returns index of the first category record found or "end" value if not found.
     *
     * @param categoryKey Localized name of "Category" namespace. If null, only English word will
     *                    be searched.
     * @param text Wiki page text
     * @param start Where to start search (wiki page fragment begin).
     * @param end Where to finish search (wiki page fragment end). Not inclusive.
     * @return begin of "Category" section or -1 if not found.
     */
    public static int findCategoriesSectionInTextRange(@Nullable String categoryKey, String text,
            int start, int end) {
        int index1 = end;
        if (categoryKey != null) {
            index1 = text.indexOf("[[" + categoryKey + ":", start);
        }
        if (index1 < 0 || index1 > end) index1 = end;
        int index2 = text.indexOf("[[Category:", start);
        if (index2 < 0 || index2 > end) index2 = end;        
        return Math.min(index1, index2);
    }

    /**
     * Adds text to talk page.
     * Respects "newer at top" or "newer at bottom" rule specified via templates.
     * Doesn't respect {{Nobots}} mark, it should be checked somewhere else. 
     */
    public static String addTextToDiscussion(String text, String discussion) {
        if (discussion == null) {
            discussion = "";
        }
        if (discussion.isEmpty()) return text;

        boolean toBottom = discussionRequiresAddToBottom(discussion);
        StringBuilder result = new StringBuilder();
        if (toBottom) {
            result.append(discussion);
            if (!discussion.endsWith("\n")) {
                result.append('\n');
            }
            result.append('\n').append(text);
        } else {
            result.append(text).append('\n').append(discussion);
        }
        return result.toString();
    }

    /**
     * Find out where talk page requires to add new topics: top or bottom. 
     *
     * @param discussion Talk page text.
     * @return true if can add to bottom or false otherwise.
     */
    public static boolean discussionRequiresAddToBottom(String discussion) {
        Localizer localizer = Localizer.getInstance();
        LocalizedTemplate template = localizer.localizeTemplateStrict("Новые сверху");
        if (template != null) {
            if (containsTemplate(discussion, template.localizeName())) {
                return false;
            }
        }
        template = localizer.localizeTemplateStrict("Новые сверху 2");
        if (template != null) {
            if (containsTemplate(discussion, template.localizeName())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if specified text contains specified template. 
     */
    public static boolean containsTemplate(String text, String template) {
        assert text != null && template != null && !template.isEmpty();
        if (text.isEmpty()) return false;
        return text.contains("{{" + StringUtils.capitalize(template)) ||
                text.contains("{{" + StringUtils.uncapitalize(template));
    }

    /**
     * Extracts portal title from portal section update settings page.
     * For example for title "Project:Project A/New pages/Settings"
     * it will give  "Project:Project A".
     */
    public static String getPortalFromSettingsSubPage(String subPage) {
        int lastSlash = subPage.lastIndexOf('/');
        if (lastSlash < 0 ) return null;
        int secondLastSlash = subPage.lastIndexOf('/', lastSlash - 1);
        return subPage.substring(0, secondLastSlash >= 0 ? secondLastSlash : lastSlash);
    }
}
