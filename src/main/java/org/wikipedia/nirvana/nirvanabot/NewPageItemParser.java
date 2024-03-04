/**
 *  @(#)NewPagesParser.java
 *  Copyright © 2024 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.util.DateTools;
import org.wikipedia.nirvana.wiki.NamespaceUtils;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse utilities for new pages lists.
 * This parser can extract new page item date or title, and will fallback to API request
 * if date is not found in the item.
 *
 */
public class NewPageItemParser {
    public static final String PATTERN_NEW_PAGES_LINE_WIKIREF_ARTICLE =
            "\\[\\[(?<article>[^\\]|]+)(|[^\\]]+)?\\]\\]";
    public static final String PATTERN_NEW_PAGES_LINE_TEMPLATE_ITEM =
            "\\{\\{(?<template>.+)\\}\\}";

    private static String NEW_PAGES_LISTS_CATEGORY;

    protected final Logger log;

    /**
     * Constructs parser instance.
     */
    public NewPageItemParser() {
        Localizer localizer = Localizer.getInstance();
        NEW_PAGES_LISTS_CATEGORY =
                localizer.localize("Категория:Википедия:Списки новых статей по темам");
        log = LogManager.getLogger(this.getClass().getName());
    }

    // TODO протестировать эту функцию на другие пространства имён!!!
    /**
     * Parse wiki article name from text (one line from new pages list).
     *
     * Line is a string coming from new pages list generated by this bot.
     * Items can have different format.
     * Examples:
     * - [[Apple]]
     * - [[Apple]] (created by [[User:John]])
     * - {{Новая статья|Ранчо Техас|2023-12-17T16:04:13Z|Archivero}}
     *
     * @return article name or null if failed to parse anything
     */
    public String getNewPagesItemArticle(String item) {
        Pattern p = Pattern.compile(PATTERN_NEW_PAGES_LINE_WIKIREF_ARTICLE);
        Matcher m = p.matcher(item);
        while (m.find()) {
            String article = m.group("article");
            if (article.startsWith(":")) {
                // Special case when title starts from ":". This : is not included in title
                article = article.substring(1);
            }
            if (article.contains(NEW_PAGES_LISTS_CATEGORY)) {
                continue;
            }
            if (!NamespaceUtils.userNamespace(article)) {
                return article;
            }
        }
        p = Pattern.compile(PATTERN_NEW_PAGES_LINE_TEMPLATE_ITEM);
        m = p.matcher(item);
        if (m.find()) {
            String templateString = m.group("template");
            String []items = templateString.split("\\|");
            // 2012-02-26T16:10:36Z
            //p = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z");
            for (int i = 1; i < items.length; i++) {
                String s = items[i].trim();
                String article = s;
                if (!NamespaceUtils.userNamespace(article)) {
                    return article;
                }
            }
        }
        return null;
    }
    
    // TODO: Migrate to Java8 dates.
    /**
     * Parse wiki article creation date from text (one line from new pages list).
     *
     * Line is a string coming from new pages list generated by this bot.
     * Items can have different format.
     * Examples:
     * - [[Apple]]
     * - [[Apple]] (created by [[User:John]])
     * - {{Новая статья|Ранчо Техас|2023-12-17T16:04:13Z|Archivero}}
     * 
     * If article date is missing in text but article name presents, the date is detected with
     * a wiki Api request.
     *
     * @return article creation date or null if failed to parse anything
     */
    public Calendar getNewPagesItemDate(NirvanaWiki wiki, String item) {
        Calendar c = null;
        //NewPagesItem itemData = null;
        Pattern p = Pattern.compile(PATTERN_NEW_PAGES_LINE_WIKIREF_ARTICLE);
        Matcher m = p.matcher(item);
        boolean foundBrackets = false;
        while (m.find()) {
            String article = m.group("article");
            if (article.startsWith(":")) {
                // special case when title starts from : this : is not included in title
                article = article.substring(1);
            }                
            if (article.contains(NEW_PAGES_LISTS_CATEGORY)) {
                // foundBrackets should stay false for this case
                continue;
            }
            Revision r = null;
            try {
                r = wiki.getFirstRevision(article,true);
            } catch (IOException e) {                
                log.warn(String.format("Article %s not found", article));
            }
            if (r != null) {
                return GregorianCalendar.from(
                        r.getTimestamp().atZoneSameInstant(ZoneId.systemDefault()));
            }
            foundBrackets = true;
        }
        if (foundBrackets) return null;
        // если не нашли по [[]] скобочкам, продолжаем искать по {{}}
        p = Pattern.compile(PATTERN_NEW_PAGES_LINE_TEMPLATE_ITEM);
        m = p.matcher(item);
        if (m.find()) {
            String templateString = m.group("template");            
            String []items = templateString.split("\\|");
            // 2012-02-26T16:10:36Z
            //p = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z");
            for (String s:items) {
                c = DateTools.parseDate(s);
                if (c != null) return c;
            }
            for (String s: items) {
                Revision r = null;
                try {
                    r = wiki.getFirstRevision(s, true);
                } catch (IOException e) {
                    log.warn(String.format("Page %s not found", s));
                }
                if (r != null) {
                    return GregorianCalendar.from(
                            r.getTimestamp().atZoneSameInstant(ZoneId.systemDefault()));
                }
            }
        }
        return null;
    }

}
