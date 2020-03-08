/**
 *  @(#)PageFormatter.java
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

package org.wikipedia.nirvana.nirvanabot;

import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.BasicBot;
import org.wikipedia.nirvana.archive.ArchiveSettings;
import org.wikipedia.nirvana.nirvanabot.NirvanaBot.BotGlobalSettings;
import org.wikipedia.nirvana.util.DateTools;
import org.wikipedia.nirvana.util.StringTools;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Does page old content processing. Prepares page content final representation. This includes:
 * - remove old page header & footer;
 * - read {{Bots}} template and preserve it in the resulting page;
 * - append new header & footer (update them if they were changed);
 * - replace placeholders in pages & footer content if required.
 */
public class PageFormatter {
    protected final Logger log;
    // Main params of this class
    @Nonnull
    protected String delimeter;
    @Nonnull
    protected String header;
    @Nonnull
    protected String footer;
    @Nonnull
    protected String middle;
    protected String headerLastUsed;
    protected String footerLastUsed;
    protected String middleLastUsed;
    
    // Params required for SUBST
    protected final String pageName;
    protected final ArchiveSettings archiveSettings;
    protected final String botSettingsTemplate;
    protected final String portalSettingsPage;
    
    // Data sources
    protected final BotGlobalSettings globalSettings;
    protected final NirvanaWiki wiki;
    protected final SystemTime systemTime;
    protected final DateTools dateTools;
    
    @Nullable
    protected String botsAllowString;

    /**
     * Constructs page formatter object.
     */
    public PageFormatter(PortalParam params, String botSettingsTemplate, String portalSettingsPage,
            BotGlobalSettings globalSettings, NirvanaWiki wiki, SystemTime systemTime) {
        this.delimeter = params.delimeter;
        this.header = params.header == null ? "" : params.header;
        this.footer = params.footer == null ? "" : params.footer;
        this.middle = params.middle;
        headerLastUsed = this.header;
        footerLastUsed = this.footer;
        middleLastUsed = this.middle;

        this.pageName = params.page;
        this.archiveSettings = params.archSettings;
        this.botSettingsTemplate = botSettingsTemplate;
        this.portalSettingsPage = portalSettingsPage;

        this.globalSettings = globalSettings;
        this.wiki = wiki;
        this.systemTime = systemTime;
        this.dateTools = DateTools.getInstance();
        
        log = LogManager.getLogger(this.getClass().getName());
        substPlaceholdersIfNeed();
    }

    /**
     * Replaces placeholders in the header/footer/middle with real values.
     */
    public void substPlaceholdersIfNeed() {
        header = substAllParams(header);
        footer = substAllParams(footer);
    }

    /**
     * Prepares header/footer/middle values intended for subtraction from old page content.
     * The main point here is to refresh that values with older header/footer/middle content in the
     * case when user have changed them in portal settings after the last time when the portal page
     * was updated.
     */
    public void getHeaderFooterChanges()
            throws IOException {
        Revision revNewPages = wiki.getTopRevision(pageName);
        if (revNewPages == null) {
            // New pages page is not yet created, this is the first update.
            return;
        }

        // TODO: WTF we put now() as an earliest date?
        Revision []revs = wiki.getPageHistory(portalSettingsPage, Calendar.getInstance(),
                revNewPages.getTimestamp());
        if (revs.length == 0) {
            // No changes made in the time period after the last new pages update.
            return;
        }

        Revision r = revs[revs.length - 1].getPrevious();

        if (r == null) {
            // Settings was recently created, no old edits.
            return;
        }
        // get last used header/footer
        log.info("portal params were changed after last use");
        
        String settingsText = r.getText();
        Map<String, String> options = new HashMap<String,String>();
        String userNamespace = wiki.namespaceIdentifier(Wiki.USER_NAMESPACE);
        if (BasicBot.tryParseTemplate(botSettingsTemplate, userNamespace, settingsText, options)) {
            PortalConfig portalConfig = new PortalConfig(options);
            headerLastUsed = portalConfig.getUnescaped(PortalConfig.KEY_HEADER,
                    globalSettings.getDefaultHeader());
            footerLastUsed = portalConfig.getUnescaped(PortalConfig.KEY_FOOTER,
                    globalSettings.getDefaultFooter());
            middleLastUsed = portalConfig.getUnescaped(PortalConfig.KEY_MIDDLE,
                    globalSettings.getDefaultMiddle());
        }
    }

    protected String stripBotsAllowString(String text) {
        botsAllowString = NirvanaWiki.getAllowBotsString(text);
        if (botsAllowString != null) {
            log.debug("Bots allow string: {}", botsAllowString);
            int pos = text.indexOf(botsAllowString);
            text = text.substring(0, pos) + text.substring(pos + botsAllowString.length());
        }
        return text;
    }

    /**
     * Format resulting page, joining items with separator and adding header, footer, middle and
     * bots allow string.
     */
    public String formatPage(List<String> items) {
        if (middle.isEmpty()) {
            return formatPage(StringUtils.join(items.toArray(), delimeter));
        } else if (items.size() > 1) {
            int m = (items.size() + 1) / 2; // 2->1, 3->2, 4->2
            return formatPageWith2Columns(
                    StringUtils.join(items.subList(0, m), delimeter),
                    StringUtils.join(items.subList(m, items.size()), delimeter));
        } else {
            return formatPageWithContentBeforeMiddle(
                    StringUtils.join(items, delimeter));
        }
    }

    /**
     * Format resulting page adding header, footer, and bots allow string.
     */
    public String formatPage(String content) {
        StringBuilder result = new StringBuilder();
        if (botsAllowString != null) {
            result.append(botsAllowString).append("\n");
        }
        result.append(header).append(content).append(footer);
        return result.toString();
    }

    /**
     * Format resulting page, adding header, footer, middle and bots allow string.
     * Used for pages split into 2 columns.
     */
    public String formatPageWith2Columns(String content1, String content2) {
        StringBuilder result = new StringBuilder();
        if (botsAllowString != null) {
            result.append(botsAllowString).append("\n");
        }
        result.append(header).append(content1).append(middle).append(content2).append(footer);
        return result.toString();
    }

    /**
     * Format resulting page, adding header, footer, middle and bots allow string.
     * Used for pages split into 2 columns but having content for only one column.
     */
    public String formatPageWithContentBeforeMiddle(String content) {
        StringBuilder result = new StringBuilder();
        if (botsAllowString != null) {
            result.append(botsAllowString).append("\n");
        }
        result.append(header).append(content).append(middle).append(footer);
        return result.toString();
    }

    /**
     * Strips header, footer, middle from page content.
     */
    public String stripDecoration(String text) {
        String result = text;
        result = trimRight(result, substConstantParams(footerLastUsed));
        if (headerLastUsed.contains(BotVariables.DATE)) {
            // TODO: Optimize it. NewPagesWeek calls this 7 times on each update.
            result = trimLeftWithUpdatedVariable(result, headerLastUsed);
        } else {
            result = trimLeft(result, substConstantParams(headerLastUsed));
        }
        result = trimMiddle(result, middleLastUsed);
        return result;
    }
    
    protected String trimRight(String text, String right) {
        String textTrimmed = text;
        if (!right.isEmpty() && textTrimmed.endsWith(right)) {                
            textTrimmed = text.substring(0, textTrimmed.length() - right.length());
            textTrimmed = StringTools.trimRight(textTrimmed);
        } else {
            textTrimmed = StringTools.trimRight(textTrimmed);
            if (!right.isEmpty() && textTrimmed.endsWith(right)) {                
                textTrimmed = text.substring(0, textTrimmed.length() - right.length());
                textTrimmed = StringTools.trimRight(textTrimmed);
            }
        } 
        return textTrimmed;
    }

    protected String trimLeftWithUpdatedVariable(String text, String left) {
        String textTrimmed = text;
        if (!left.isEmpty()) {
            Pattern p = Pattern.compile("^\\s*" + makePatternString(left));
            Matcher m = p.matcher(textTrimmed);
            if (m.find()) {
                textTrimmed = textTrimmed.substring(m.group().length());
            }            
        }
        return textTrimmed;
    }
    
    protected String trimLeft(String text, String left) {
        String textTrimmed = text;
        if (!left.isEmpty() && textTrimmed.startsWith(left)) {
            textTrimmed = text.substring(left.length());
            textTrimmed = StringTools.trimLeft(textTrimmed);
        } else {
            textTrimmed = StringTools.trimLeft(textTrimmed);
            if (!left.isEmpty() && textTrimmed.startsWith(left)) {
                textTrimmed = textTrimmed.substring(left.length());
                textTrimmed = StringTools.trimLeft(textTrimmed);
            }
        }
        return textTrimmed;
    }
    
    protected String trimMiddle(String text, String middle) {
        String textTrimmed = text;
        if (!middle.isEmpty()) {
            if (textTrimmed.contains(middle)) {
                textTrimmed = textTrimmed.replace(middle, delimeter);
            } else if (!middle.trim().isEmpty() && textTrimmed.contains(middle.trim())) {
                textTrimmed = textTrimmed.replace(middle.trim(), delimeter);
            }
        }
        return textTrimmed;
    }
    
    protected String makePatternString(String src) {
        String pattern = src;
        pattern = substParams(src, true);
        pattern = pattern.replace("{", "\\{")
                .replace("}", "\\}")
                .replace("|", "\\|")
                .replace(BotVariables.DATE, "\\d{1,2}\\s[\\p{InCyrillic}\\w]+\\s\\d{4}");
        return pattern;
    }

    protected String getCurrentUser() {
        Wiki.User user = wiki.getCurrentUser();
        return user.getUsername();
    }

    private String getPortalName() {
        String str = pageName;
        int colon = str.indexOf(':');
        if (colon >= 0) {
            str = str.substring(colon + 1);
        }
        int slash = str.indexOf('/');
        if (slash > 0) {
            str = str.substring(0, slash);
        }
        return str;
    }

    protected String substAllParams(String item) {
        return substParams(item, false);
    }

    protected String substConstantParams(String item) {
        return substParams(item, true);
    }

    protected String substParams(String item, boolean constantOnly) {
        if (item.isEmpty()) return item;
        if (!item.contains(BotVariables.PLACEHOLDER_KEY)) return item;

        String portal = getPortalName();
        Calendar c = systemTime.now();
        String date = dateTools.printDateDayMonthYearGenitive(c);
        String str = item
                .replace(BotVariables.BOT, getCurrentUser())
                .replace(BotVariables.PROJECT, portal)
                .replace(BotVariables.PORTAL, portal)
                .replace(BotVariables.PAGE, pageName);
        if (archiveSettings != null) {
            String archive = archiveSettings.getArchiveForDate(c);
            if (archive != null) {
                str = str.replace(BotVariables.ARCHIVE, archive);
            }
        }
        if (!constantOnly) {
            str = str.replace(BotVariables.DATE, date);
        }
        return str;
    }
}
