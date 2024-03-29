/**
 *  @(#)TemplateFinder.java 23.08.2015
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

package org.wikipedia.nirvana.nirvanabot.templates;

import org.wikipedia.Wiki;
import org.wikipedia.nirvana.wiki.NirvanaWiki;
import org.wikipedia.nirvana.wiki.WikiBooster;
import org.wikipedia.nirvana.wiki.WikiUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Utility class that can make difficult template searches in wiki page text.
 * It can search externally provided search items (TemplateFindItem), which is a set of 3 elements
 * (template, key, value):
 * 1) template name
 * 2) template variable
 * 3) template variable value
 * 
 * WikiBooster here is used for speed optimizations
 * (merge many similar API calls into one heavy call, and then parse and take required data). 
 * This reduces HTTP traffic very much.
 */
public class TemplateFinder {
    private final List<TemplateFindItem> findItems;
    private final WikiBooster wikiBooster;
    private final String templatePrefix;

    /**
     * Constructs instance of class with predefined items to search, Wiki and WikiBooster.
     */
    public TemplateFinder(List<TemplateFindItem> findItems, NirvanaWiki wiki,
            WikiBooster wikiBooster) throws IOException {
        this.findItems = findItems;
        this.wikiBooster = wikiBooster;
        templatePrefix = wiki.namespaceIdentifier(Wiki.TEMPLATE_NAMESPACE) + ":";
    }

    /**
     * Search specified template items in article, which title is provided by user.
     */
    public boolean find(String article) throws IOException {
        for (TemplateFindItem findItem: findItems) {
            if (findItem(findItem, article)) {
                return true;
            }
        }
        return false;
    }

    private boolean findItem(TemplateFindItem item, String article)
            throws IOException {
        if (!wikiBooster.hasTemplate(article, templatePrefix + item.template)) {
            return false;
        }
        if (item.isSimple()) {
            return true;
        }
        String text = wikiBooster.getPageText(article);
        return findItemInTemplate(item, text, item.template);
    }

    private boolean findItemInTemplate(TemplateFindItem item, String text, String template) {
        // 1) Нужно найти и распарсить код шаблона, занести в мапу словарь значений
        HashMap<String, String> params = new HashMap<>();
        if (!WikiUtils.parseWikiTemplate(template, text, params)) {
            return false;
        }
        assert !item.isSimple();
        // 2) Пройтись по словарю и отыскать нужный ключ и значение
        if (item.value.isEmpty()) {
            // We don't care value, just check that param exists
            return params.containsKey(item.param);
        }
        String value = params.get(item.param);
        return (value != null && value.contains(item.value));
    }    
}
