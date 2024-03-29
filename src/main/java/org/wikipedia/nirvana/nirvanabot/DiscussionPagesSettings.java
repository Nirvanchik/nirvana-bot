/**
 *  @(#)DiscussionPagesSettings.java
 *  Copyright © 2023 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.nirvanabot;

import org.wikipedia.nirvana.localization.Localizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps global settings of discussion pages.
 * Example of setting: "К удалению = Википедия:К удалению/%(дата)#%(название), к удалению"
 * Using this settings, it can generate wiki link to page discussion.
 * See {@link 
 * DiscussionPagesSettings.DiscussionPageTemplate#formatLinkForPage(String, String, String)}.
 *
 * This helps to not hardcode strings like "Википедия:К удалению" in bot code.
 * This is configured in bot config uniquely for every wiki site.
 */
public class DiscussionPagesSettings {
    protected static Logger log = null;
    public static final DiscussionPagesSettings EMPTY = new DiscussionPagesSettings();

    private static void initLog() {
        if (log == null) {
            log = LogManager.getLogger(DiscussionPagesSettings.class);
        }
    }

    List<DiscussionPageTemplate> templates;

    public static class DiscussionPageTemplate {
        private static final String LINK_FORMAT = "[[%1$s|%2$s]]";
        private static final String LINK_FORMAT_WITH_FRAGMENT = "[[%1$s#%2$s|%3$s]]";
        String template;
        String linkFormatString;
        String linkText;
        String prefix;

        DiscussionPageTemplate(String template) {
            this.template = template;
            linkFormatString = null;
            linkText = null;
            prefix = null;
        }

        public boolean hasLink() {
            return prefix != null && linkFormatString != null;
        }

        /**
         * Generate discussion wiki link for page.
         */
        public String formatLinkForPage(String link, String page, String fragment) {
            if (linkFormatString == null) {
                log.error("What's the fuck you doing nigga????");
                return "";
            }
            if (linkFormatString.contains("#")) {
                int hash = linkFormatString.indexOf("#");
                String right = linkFormatString.substring(hash + 1);
                String fragmentString = right.replace(BotVariables.TITLE, page);
                if (needsToSearchFragment()) {
                    if (fragment == null) {
                        return String.format(LINK_FORMAT, link, linkText);
                    }    
                    fragmentString = fragmentString.replace(BotVariables.TEXT_WITH_TITLE,
                            fragment);
                }
                return String.format(LINK_FORMAT_WITH_FRAGMENT, link, fragmentString, linkText);
            } else {
                return String.format(LINK_FORMAT, link, linkText);
            }
        }

        public boolean needsToSearchFragment() {
            return linkFormatString != null &&
                    linkFormatString.contains(BotVariables.TEXT_WITH_TITLE);
        }

        /**
         * Generate {@link DiscussionPageTemplate} from settings line.
         */
        public static DiscussionPageTemplate fromSettingString(String str) {

            DiscussionPageTemplate newTempl = null;
            String[] parts = str.split("=");
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
                    newTempl.linkText = parts[1].substring(comma + 1).trim();
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

        /**
         * Generate {@link DiscussionPageTemplate} from custom prefix.
         * Used for legacy portal sections that used "префикс" parameter to generate
         * discussion link.
         */
        public static DiscussionPageTemplate fromPrefix(String prefix) {
            DiscussionPageTemplate newTempl = null;
            newTempl = new DiscussionPageTemplate(null);
            newTempl.prefix = prefix;
            newTempl.linkText = Localizer.getInstance().localize("обсуждение");
            newTempl.linkFormatString = prefix + BotVariables.DATE;
            return newTempl;
        }
    }

    DiscussionPagesSettings() {
        templates = new ArrayList<>();
    }

    /**
     * Parse settings from text.
     * Rules: see example:
     *     К удалению = Википедия:К удалению/%(дата)#%(название), к удалению
     *  К переименованию = \
     *  Википедия:К переименованию/%(дата)#%(текст_с_названием), к переименованию
     */
    public static DiscussionPagesSettings parseFromSettingsString(String text) {
        initLog();
        if (text == null) {
            return null;
        }
        text = text.trim();
        DiscussionPagesSettings settings = new DiscussionPagesSettings();
        String[] lines = text.split("\n");
        for (String line:lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("#")) {
                continue;
            }
            DiscussionPageTemplate template = DiscussionPageTemplate.fromSettingString(line);
            if (template == null) {
                log.error("Failed to parse DiscussionPage settings line: {}", line);
            } else {
                settings.templates.add(template);
            }
        }
        return settings;
    }
}
