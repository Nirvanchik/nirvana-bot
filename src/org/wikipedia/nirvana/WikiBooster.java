/**
 *  @(#)WikiBooster.java 17.07.2016
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

package org.wikipedia.nirvana;

import org.wikipedia.Wiki.Revision;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author kin
 *
 */
public class WikiBooster {
    private final NirvanaWiki wiki;
    private final String pages[];
    private final Set<String> pagesSet;
    private Map<String, List<String>> templatesCache = null;
    private Map<String, String> pageTextCache = null;
    private int templatesNs = -1;
    
    public WikiBooster(NirvanaWiki wiki, String pages[]) {
        this.wiki = wiki;
        this.pages = pages;
        pagesSet = new HashSet<>(Arrays.asList(pages));
    }

    public WikiBooster(NirvanaWiki wiki, List<String> pages) {
        this.wiki = wiki;
        this.pages = pages.toArray(new String[pages.size()]);
        pagesSet = new HashSet<>(pages);
    }
    
    public static WikiBooster create(NirvanaWiki wiki, List<Revision> revs) {
        String pages[] = new String[revs.size()];
        for (int i=0; i < revs.size(); i++) {
            pages[i] = revs.get(i).getPage();
        }
        return new WikiBooster(wiki, pages);
    }
    
    public List<String> getTemplates(String title, int ns) throws IOException {
        if (!pagesSet.contains(title)) {
            throw new RuntimeException("The booster is not prepared for page: " + title);
        }
        if (templatesNs != -1 && templatesNs != ns) {
            throw new RuntimeException(
                    String.format("Unexpected namespace: %d. The booster was used with %d",
                            ns, templatesNs));
        }
        templatesNs = ns;
        if (templatesCache == null) {
            String[][] pagesTemplates = wiki.getPagesTemplates(pages, ns);
            templatesCache = new HashMap<>();
            for (int i=0; i<pages.length; i++) {
                templatesCache.put(pages[i], Arrays.asList(pagesTemplates[i]));
            }
        }
        return templatesCache.get(title);
    }

    public String getPageText(String title) throws IOException {
        if (!pagesSet.contains(title)) {
            throw new RuntimeException("The booster is not prepared for page: " + title);
        }
        if (pageTextCache == null) {
            pageTextCache = new HashMap<>();
        }
        String text = pageTextCache.get(title);
        if (text == null) {
            text = wiki.getPageText(title);
            pageTextCache.put(title, text);
        }
        return text;
    }
}
