/**
 *  @(#)PortalConfig.java 12.03.2017
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
 * Recommended code page for this file is CP1251 (also called Windows-1251).
 * */

package org.wikipedia.nirvana.nirvanabot;

import org.wikipedia.nirvana.StringTools;
import org.wikipedia.nirvana.localization.Localizer;

import java.util.Map;

/**
 * Keeps all bot configuration strings localized with {@link
 * org.wikipedia.nirvana.localization.Localizer Localizer}.
 * Make sure you call {@link #initStatics()} before usage.
 * You will get {@link java.lang.NullPointerException} if you use these strings before
 * initialization.
 */
public class PortalConfig {
    public static String KEY_HEADER;
    public static String KEY_FOOTER;
    public static String KEY_MIDDLE;
    public static String KEY_TYPE;
    public static String KEY_CATEGORY;
    public static String KEY_CATEGORIES;
    public static String KEY_PAGE;
    public static String KEY_IGNORE;
    public static String KEY_IGNORE_AUTHORS;
    public static String KEY_SERVICE;
    public static String KEY_DEFAULT_SERVICE;
    public static String KEY_ARCHIVE;
    public static String KEY_ARCHIVE_HEADER_FORMAT;
    public static String KEY_ARCHIVE_SUBHEADER_FORMAT;
    public static String KEY_ARCHIVE_PARAMS;
    public static String KEY_PREFIX;
    public static String KEY_MARK_EDITS;
    public static String KEY_FAST_MODE;
    public static String KEY_NAMESPACE;
    public static String KEY_UPDATES_PER_DAY;
    public static String KEY_TEMPLATES_WITH_PARAM;
    public static String KEY_TEMPLATES;
    public static String KEY_FORMAT;
    public static String KEY_DEPTH;
    public static String KEY_HOURS;
    public static String KEY_MAX_ITEMS;
    public static String KEY_DELETED_PAGES;
    public static String KEY_RENAMED_PAGES;
    public static String KEY_SEPARATOR;
    public static String KEY_IMAGE_SEARCH;
    public static String KEY_FAIR_USE_IMAGE_TEMPLATES;

    public static String LIST_TYPE_DISCUSSED_PAGES;
    public static String LIST_TYPE_DISCUSSED_PAGES2;
    @Deprecated
    public static String LIST_TYPE_DISCUSSED_PAGES_OLD;
    public static String LIST_TYPE_NEW_PAGES;
    @Deprecated
    public static String LIST_TYPE_NEW_PAGES_OLD;
    public static String LIST_TYPE_NEW_PAGES_7_DAYS;
    @Deprecated
    public static String LIST_TYPE_NEW_PAGES_7_DAYS_OLD;
    public static String LIST_TYPE_NEW_PAGES_WITH_IMAGES;
    @Deprecated
    public static String LIST_TYPE_NEW_PAGES_WITH_IMAGES_OLD;
    public static String LIST_TYPE_NEW_PAGES_WITH_IMAGES_IN_CARD;
    @Deprecated
    public static String LIST_TYPE_NEW_PAGES_WITH_IMAGES_IN_CARD_OLD;
    public static String LIST_TYPE_NEW_PAGES_WITH_IMAGES_IN_TEXT;
    @Deprecated
    public static String LIST_TYPE_NEW_PAGES_WITH_IMAGES_IN_TEXT_OLD;
    public static String LIST_TYPE_PAGES;
    @Deprecated
    public static String LIST_TYPE_PAGES_OLD;
    public static String LIST_TYPE_PAGES_WITH_TEMPLATES;
    public static String LIST_TYPE_WATCHLIST;

    public static String STR_ABOVE;
    public static String STR_AUTO;
    public static String STR_BELOW;
    public static String STR_BOT;
    public static String STR_DEFAULT;
    public static String STR_DELETE;
    public static String STR_ENUMERATE_WITH_HASH;
    public static String STR_ENUMERATE_WITH_HASH2;
    public static String STR_ENUMERATE_WITH_HTML;
    public static String STR_ENUMERATE_WITH_HTML2;
    public static String STR_ENUMERATE_WITH_HTML_GLOBAL;
    public static String STR_ENUMERATE_WITH_HTML_GLOBAL2;
    public static String STR_LEAVE;
    public static String STR_NEW_TITLE;
    public static String STR_MARK;
    public static String STR_NO;
    public static String STR_OLD_TITLE;
    public static String STR_REMOVE_DUPLICATES;
    public static String STR_SMALL;
    public static String STR_SORT;
    public static String STR_TOSORT;
    public static String STR_YES;
    

    private static boolean initialized;
    
    Map<String, String> options;
    
    /**
     * Call this before the first use of this class.
     */
    public static void initStatics() {
        if (initialized) return;
        Localizer localizer = Localizer.getInstance();
        KEY_HEADER = localizer.localize("шапка");
        KEY_FOOTER = localizer.localize("подвал");
        KEY_MIDDLE = localizer.localize("середина");
        KEY_TYPE = localizer.localize("тип");
        KEY_CATEGORY = localizer.localize("категория");
        KEY_CATEGORIES = localizer.localize("категории");
        KEY_PAGE = localizer.localize("страница");
        KEY_IGNORE = localizer.localize("игнорировать");
        KEY_IGNORE_AUTHORS = localizer.localize("игнорировать авторов");
        KEY_SERVICE = localizer.localize("сервис");
        KEY_DEFAULT_SERVICE = localizer.localize("сервис по умолчанию");
        KEY_ARCHIVE = localizer.localize("архив");
        KEY_ARCHIVE_HEADER_FORMAT = localizer.localize("формат заголовка в архиве");
        KEY_ARCHIVE_SUBHEADER_FORMAT = localizer.localize("формат подзаголовка в архиве");
        KEY_ARCHIVE_PARAMS = localizer.localize("параметры архива");
        KEY_PREFIX = localizer.localize("префикс");
        KEY_MARK_EDITS = localizer.localize("помечать правки");
        KEY_FAST_MODE = localizer.localize("быстрый режим");
        KEY_NAMESPACE = localizer.localize("пространство имён");
        KEY_UPDATES_PER_DAY = localizer.localize("частота обновлений");
        KEY_TEMPLATES = localizer.localize("шаблоны");
        KEY_TEMPLATES_WITH_PARAM = localizer.localize("шаблоны с параметром");
        KEY_FORMAT = localizer.localize("формат элемента");
        KEY_DEPTH = localizer.localize("глубина");
        KEY_HOURS = localizer.localize("часов");
        KEY_MAX_ITEMS = localizer.localize("элементов");
        KEY_DELETED_PAGES = localizer.localize("удаленные статьи");
        KEY_RENAMED_PAGES = localizer.localize("переименованные статьи");
        KEY_SEPARATOR = localizer.localize("разделитель");
        KEY_IMAGE_SEARCH = localizer.localize("поиск картинки");
        KEY_FAIR_USE_IMAGE_TEMPLATES = localizer.localize("шаблоны запрещенных картинок");

        LIST_TYPE_NEW_PAGES = localizer.localize("новые статьи");
        LIST_TYPE_NEW_PAGES_7_DAYS = localizer.localize("новые статьи по дням");
        LIST_TYPE_NEW_PAGES_7_DAYS_OLD = localizer.localize("списки новых статей по дням");
        LIST_TYPE_NEW_PAGES_OLD = localizer.localize("список новых статей");
        LIST_TYPE_NEW_PAGES_WITH_IMAGES = localizer.localize("новые статьи с изображениями");
        LIST_TYPE_NEW_PAGES_WITH_IMAGES_OLD =
                localizer.localize("список новых статей с изображениями");
        LIST_TYPE_NEW_PAGES_WITH_IMAGES_IN_CARD =
                localizer.localize("новые статьи с изображениями в карточке");
        LIST_TYPE_NEW_PAGES_WITH_IMAGES_IN_CARD_OLD =
                localizer.localize("список новых статей с изображениями в карточке");
        LIST_TYPE_NEW_PAGES_WITH_IMAGES_IN_TEXT =
                localizer.localize("новые статьи с изображениями в тексте");
        LIST_TYPE_NEW_PAGES_WITH_IMAGES_IN_TEXT_OLD =
                localizer.localize("список новых статей с изображениями в тексте");
        LIST_TYPE_PAGES = localizer.localize("статьи");
        LIST_TYPE_PAGES_WITH_TEMPLATES = localizer.localize("статьи с шаблонами");
        LIST_TYPE_WATCHLIST = localizer.localize("список наблюдения");
        LIST_TYPE_PAGES_OLD =
                localizer.localize("список страниц с заданными категориями и шаблонами");
        LIST_TYPE_DISCUSSED_PAGES = localizer.localize("обсуждаемые статьи");
        LIST_TYPE_DISCUSSED_PAGES2 = localizer.localize("статьи с шаблонами и обсуждением");
        LIST_TYPE_DISCUSSED_PAGES_OLD = localizer.localize(
                "список страниц с заданными категориями, шаблонами и обсуждением");

        STR_ABOVE = localizer.localize("сверху");
        STR_AUTO = localizer.localize("авто");
        STR_BELOW = localizer.localize("снизу");
        STR_BOT = localizer.localize("бот");
        STR_DEFAULT = localizer.localize("по умолчанию");
        STR_DELETE = localizer.localize("удалять");
        STR_ENUMERATE_WITH_HASH = localizer.localize("нумерация решетками");
        STR_ENUMERATE_WITH_HASH2 = localizer.localize("нумерация решётками");
        STR_ENUMERATE_WITH_HTML = localizer.localize("нумерация кодом html");
        STR_ENUMERATE_WITH_HTML2 = localizer.localize("нумерация кодом HTML");
        STR_ENUMERATE_WITH_HTML_GLOBAL = localizer.localize("глобальная нумерация кодом html");
        STR_ENUMERATE_WITH_HTML_GLOBAL2 = localizer.localize("глобальная нумерация кодом HTML");
        STR_NEW_TITLE = localizer.localize("новое название");
        STR_LEAVE = localizer.localize("оставлять");
        STR_MARK = localizer.localize("помечать");
        STR_NO = localizer.localize("нет");
        STR_OLD_TITLE = localizer.localize("старое название");
        STR_REMOVE_DUPLICATES = localizer.localize("удалить дубликаты");
        STR_SMALL = localizer.localize("малая");
        STR_SORT = localizer.localize("сортировка");
        STR_TOSORT = localizer.localize("сортировать");
        STR_YES = localizer.localize("да");
        
        initialized = true;
    }

    /**
     * Constructs config object for map with key-value data.
     */
    public PortalConfig(Map<String, String> options) {
        this.options = options;
    }

    /**
     * Check if provided key is set in config. Empty value considered as "no key" response. 
     */
    public boolean hasKey(String key) {
        return options.containsKey(key) && !options.get(key).isEmpty();
    }
    
    /**
     * Get value from config for the specified key.
     */
    public String get(String key) {
        return options.get(key);
    }

    /**
     * Get value from config for the specified key. Return default value if that key not found.
     */
    public String get(String key, String defaultValue) {
        if (!hasKey(key)) return defaultValue;
        return options.get(key);
    }

    /**
     * Get unescaped value from config for the specified key.
     */
    public String getUnescaped(String key) {
        return options.get(key).replace("\\\"", "").replace("\\n", "\n");
    }

    /**
     * Get unescaped value from config for the specified key or default value if that key not
     * found.
     */
    public String getUnescaped(String key, String defaultValue) {
        if (!hasKey(key)) return defaultValue;
        return options.get(key).replace("\\\"", "").replace("\\n", "\n");
    }

    /**
     * Get value from config for the specified key.
     * Remove double quotes if value is double-quoted.
     */
    public String getUnquoted(String key) {
        return StringTools.trimDoubleQuoteIfFound(options.get(key));
    }

    /**
     * Get value from config for the specified key. Return default value if that key not found.
     * Remove double quotes if value is double-quoted.
     */
    public String getUnquoted(String key, String defaultValue) {
        String str = defaultValue;
        if (hasKey(key)) {
            str = get(key);
        }
        return StringTools.trimDoubleQuoteIfFound(str);
    }

}
