/**
 *  @(#)DiscussionPagesSettings.java 29.02.2016
 *  Copyright © 2016 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.nirvanabot;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kin
 *
 */
public class DiscussionPagesSettings {
    protected static org.apache.log4j.Logger log = null;
    public static final DiscussionPagesSettings EMPTY = new DiscussionPagesSettings();

    private static void initLog() {
        if (log == null) {
            log = org.apache.log4j.Logger.getLogger(DiscussionPagesSettings.class);
        }
    }


	List<DiscussionPageTemplate> templates;

	public static class DiscussionPageTemplate {
		//private static final String STANDART_LINK_FORMAT = "";
		private static final String LINK_FORMAT = "[[%1$s|%2$s]]";
		private static final String LINK_FORMAT_WITH_FRAGMENT = "[[%1$s#%2$s|%3$s]]";
		String template;
		String linkFormatString;
		String linkText;
		String prefix;
		//String regex;
		public boolean hasLink() {
			return prefix!=null && linkFormatString != null;
		}
		public String formatLinkForPage(String link, String page, String fragment) {
			if (linkFormatString == null) {
				log.error("What's the fuck you doing nigga????");
				return "";
			}
			if (linkFormatString.contains("#")) {
				int hash = linkFormatString.indexOf("#");
				String right = linkFormatString.substring(hash + 1);
				String fragmentString = right.replace("%(название)", page);
				if (needsToSearchFragment()) {
					if (fragment == null) {
						return String.format(LINK_FORMAT, link, linkText);
					}	
					fragmentString = fragmentString.replace("%(текст_с_названием)", fragment);
				}
				return String.format(LINK_FORMAT_WITH_FRAGMENT, link, fragmentString, linkText);
			} else {
				return String.format(LINK_FORMAT, link, linkText);
			}
		}
		
		public boolean needsToSearchFragment() {
			return linkFormatString != null && linkFormatString.contains("%(текст_с_названием)");
		}
		
		DiscussionPageTemplate(String template) {
			this.template = template;
			linkFormatString = null;
			linkText = null;
			prefix = null;
		}
		
		public static DiscussionPageTemplate fromSettingString(String str) {
			
			DiscussionPageTemplate newTempl = null;
			String parts[] = str.split("=");
			if (parts.length != 2) {
				return null;
			}		
			if (parts[0].trim().isEmpty()) {
				return null;
			}
			newTempl = new DiscussionPageTemplate(parts[0].trim());
			parts[1] = parts[1].trim();
			if (!parts[1].isEmpty()) {
				int replacementStart = parts[1].indexOf("%(");
				if (replacementStart < 0) {
					return null;
				}
				newTempl.prefix = parts[1].substring(0, replacementStart);
				int comma = parts[1].indexOf(",");
				if (comma >= 0) {
					newTempl.linkText = parts[1].substring(comma).trim();
					parts[1] = parts[1].substring(0,comma).trim();
				} else {
					newTempl.linkText = newTempl.template.toLowerCase();
				}
				if (parts[1].isEmpty()) {
					return null;
				}
				newTempl.linkFormatString = parts[1];
			}
			return newTempl;
		}
		
		public static DiscussionPageTemplate fromPrefix(String prefix) {
			DiscussionPageTemplate newTempl = null;
			newTempl = new DiscussionPageTemplate(null);
			newTempl.prefix = prefix;
			newTempl.linkText = "обсуждение";
			newTempl.linkFormatString = prefix + "%(дата)";
			return newTempl;
		}
	}
	
	DiscussionPagesSettings() {
		templates = new ArrayList<>();
	}

	public static DiscussionPagesSettings parseFromSettingsString(String text) {
        initLog();
		if (text == null) {
			return null;
		}
		text = text.trim();
		DiscussionPagesSettings settings = new DiscussionPagesSettings();
		String lines[] = text.split("\n");
		for (String line:lines) {
			line = line.trim();
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("#")) {
				continue;
			}
			DiscussionPageTemplate template = DiscussionPageTemplate.fromSettingString(line);
			if (template == null) {
				log.error("Failed to parse DiscussionPage settings line: "+line);
			} else {
				settings.templates.add(template);
			}
		}
		return settings;
	}
}
