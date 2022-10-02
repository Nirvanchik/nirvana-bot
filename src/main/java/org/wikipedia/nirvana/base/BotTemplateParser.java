/**
 *  @(#)BotParamsTemplate.java
 *  Copyright © 2022 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.base;

import org.wikipedia.nirvana.util.StringTools;
import org.wikipedia.nirvana.wiki.WikiUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * We keep bot settings in a wiki page inside of Wiki template which we call here "bot template".
 * So, each wiki project can have their own unique settings for bot. Project members can modify
 * bot settings at any moment.
 *
 * This class helps to parse this template params (key/value pairs) into
 * <code>map&lt;String, String&gt;</code>.
 */
public class BotTemplateParser {
    private Logger log = LogManager.getLogger(BotTemplateParser.class.getName());

    private final String template;
    // TODO: Prepare and cache Pattern instance instead of String RE.
    private final String templateRe;

    /**
     * Constructs bot template parser class.
     *
     * @param template Title of template which is used to provide bot parameters.
     *                 Normally template is expected to be in User namespace. Namespace name
     *                 can be omitted in title but the function will always search template
     *                 of the User namespace.
     * @param userNamespaceLoc Localized name of user namespace.
     */
    public BotTemplateParser(String template, @Nullable String userNamespaceLoc) {
        this.template = template; 
        templateRe = getUserTemplateRe(template, userNamespaceLoc);
    }

    /**
     * Parse bot template from wiki text. "Bot template" is a wiki template which is used to
     * provide configuration parameters for wiki bot. This function will search the first
     * occurrence of bot template only.
     *
     * @param text Wiki text in which bot parameters should be parsed.
     * @param parameters Key-value map where bot parameters will be stored.
     * @return true if bot template found and parsed successfully.
     */
    public boolean tryParseTemplate(String text, Map<String, String> parameters) {
        log.debug("Bot settings parsing started for template: {}", template);
        log.debug("Text (truncated to 100): {}", StringTools.trancateTo(text, 100));
        if (!WikiUtils.parseBotTemplate(templateRe, text, parameters)) {
            // TODO (Nirvanchik): return error code with error details.
            log.error("Failed to parse bot settings");
            return false;
        }
        // TODO: Remove it out of here - ugly workaround.
        parameters.put("BotTemplate", template);
        return true;
    }

    static String getUserTemplateRe(String template,
                                    @Nullable String userNamespaceLoc) {
        String userPrefix = "";
        String templateName;
        String userEnPrefix = "User";
        if (template.startsWith(userEnPrefix)) {
            templateName = template.substring(userEnPrefix.length() + ":".length());
        } else if (userNamespaceLoc != null && template.startsWith(userNamespaceLoc + ":")) {
            templateName = template.substring(userNamespaceLoc.length() + ":".length());
        } else {
            templateName = template;
        }
        userPrefix = userEnPrefix + ":";
        if (userNamespaceLoc != null) {
            userPrefix = String.format("(%s:|%s:)", userEnPrefix, userNamespaceLoc);
        }
        // Escape "(" and ")" in template name.
        templateName = templateName.replace("(", "\\(").replace(")", "\\)");
        // We don't start from ^ because we allow any text before template
        return userPrefix + templateName;
    }
}
