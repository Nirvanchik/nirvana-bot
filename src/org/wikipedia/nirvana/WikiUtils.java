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
 * Recommended code page for this file is CP1251 (also called Windows-1251).
 * */

package org.wikipedia.nirvana;

import org.wikipedia.nirvana.localization.LocalizedTemplate;
import org.wikipedia.nirvana.localization.Localizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author kin
 *
 */
public class WikiUtils {
    protected static final Logger log;

	private static final Pattern COMMENT_PATTERN = Pattern.compile("<!--(.+?)-->", Pattern.DOTALL);

    static {
        log = LogManager.getLogger(WikiUtils.class.getName());
    }

	public static int findMatchingBraceOfTemplate(String text, int start) {
		int begin = start;
        int end = -1;
        int level = 1;
        for (int i = begin; i < text.length() - 1; ++i)
        {
            if (text.charAt(i) == '{' && text.charAt(i+1) == '{')
            {
                ++level;
            }
            else if (text.charAt(i) == '}' && text.charAt(i+1) == '}')
            {
                --level;
                if (level == 0)
                {
                    end = i;
                    break;
                }
            }
        }
        return end;
	}

    public static boolean parseBotTemplate(String templateRegex, String text,
            Map<String, String> parameters) {
        return parseTemplateImpl(templateRegex, text, parameters, true);
    }

    public static boolean parseWikiTemplate(String templateRegex, String text,
            Map<String, String> parameters) {
        return parseTemplateImpl(templateRegex, text, parameters, false);
    }

    @Deprecated
    public static boolean parseTemplate(String templateRegex, String text,
            Map<String, String> parameters, boolean botTemplate) {
        return parseTemplateImpl(templateRegex, text, parameters, botTemplate);
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
        String str = "(\\{\\{"+templateRegex+")(.+)$"; // GOOD
        Pattern pattern = Pattern.compile(str,Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(text);
        if (!m.find())
        {
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
                if ((botTemplate && key.equals("align") || key.equals("style"))) {
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

    private static void appendToLastVal(Map<String, String> parameters, String value, String lastKey) {
        value = removeComments(value).trim();
        String lastVal = parameters.get(lastKey) + "|" + value;
        parameters.put(lastKey, lastVal);
    }

    public static String removeNoWikiText(String text) {
	    String result = text;
	    boolean changed = false;
	    StringBuilder sb = new StringBuilder();
	    int index = text.indexOf("<nowiki>");
	    int lastNoWikiEnd = 0;
	    while (index>=0 && index < text.length()) {
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
    
    public static String removeComments(String text) {
    	return COMMENT_PATTERN.matcher(text).replaceAll("");
    }

    public static String removePreTags(String text) {
        return text.replace("<pre>", "").replace("</pre>", "");
    }

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

    public static String addTextToTemplateLastNoincludeSectionBeforeCats(
    		String categoryKey, String templateText, String insertText) {
    	String text;
    	String separator = "\n";
    	int indexOfLastNoinclude = templateText.lastIndexOf("<noinclude>");
    	if (indexOfLastNoinclude < 0) {
    		text = templateText + "<noinclude>" + insertText + "</noinclude>";
    	} else {
    		int indexOfLastNoincludeCloser = templateText.indexOf("</noinclude", indexOfLastNoinclude);
    		if (indexOfLastNoincludeCloser < 0) {
    			log.warn("Oops! The template code doesn't close <noinclude> section!");
        		if (templateText.endsWith(separator)) {
        			separator = "";
        		}
    			text = templateText + separator + insertText;
    			indexOfLastNoincludeCloser = templateText.length();
    		} else {
    			if (templateText.charAt(indexOfLastNoincludeCloser - 1) == '\n') {
    				separator = "";
    			}
    			text = templateText.substring(0, indexOfLastNoincludeCloser) +
    					separator +
    					insertText +
    					templateText.substring(indexOfLastNoincludeCloser);
    		}
    		// here we have a range of (indexOfLastNoinclude,indexOfLastNoincludeCloser)
    		int insertPlace = findCategoriesSectionInTextRange(
    				categoryKey, templateText, indexOfLastNoinclude, indexOfLastNoincludeCloser);
    		if (templateText.charAt(insertPlace - 1) == '\n') {
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
					end;
    	}
    	return text;
    }
    
    public static int findCategoriesSectionInTextRange(String categoryKey, String text, int start, int end) {
    	String categoryDetected = "[["+categoryKey+":";
    	String categoryEnDetected = "[[Category:";
    	int index = text.indexOf(categoryDetected, start);
    	if (index<0 || index>=end) {
    		index = text.indexOf(categoryEnDetected, start);
    	}
    	if (index>0 && index<end) {
    		if (text.charAt(index-1)=='\n') {
    			index--;
    		}
    		return index;
    	}
    	return end;
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

        Localizer localizer = Localizer.getInstance();
        boolean toBottom = true;
        LocalizedTemplate template = localizer.localizeTemplateStrict("Новые сверху");
        if (template != null) {
            toBottom = !containsTemplate(discussion, template.localizeName());
        }
        if (toBottom) {
            template = localizer.localizeTemplateStrict("Новые сверху 2");
            if (template != null) {
                toBottom = !containsTemplate(discussion, template.localizeName());
            }
        }
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
     * Checks if specified text contains specified template. 
     */
    public static boolean containsTemplate(String text, String template) {
        assert template != null && !template.isEmpty();
        if (text == null) return false;
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
