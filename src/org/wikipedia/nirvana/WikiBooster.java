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

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is a thing that increases bot performance greatly. When you know what pages texts you're
 * going to download or what pages templates you're going to get you and so on you can use this
 * booster. It will prefetch all required data in a short time and then return them one by one
 * when you ask them. The effect is achieved by fetching more data in every request. So, you 
 * don't waste thousands of HTTP GET/POST requests to download what you want, you use tens or
 * hundreds instead. Each HTTP GET is expensive, it takes time, it adds a load to network. 
 */
public class WikiBooster {
    private static final Logger log = Logger.getLogger(WikiBooster.class.getName());
    private final NirvanaWiki wiki;
    private final List<String> pages;
    private final Set<String> pagesSet;
    private Map<String, List<String>> templatesCache = null;
    private Map<String, String> pageTextCache = null;
    private int templatesNs = -1;
    
    /**
     * Constructor taking a wiki instance and page list as an array.
     * If you need a booster for another set of pages, please create a new instance for that.
     * 
     * @param wiki {@link org.wikipedia.nirvana.NirvanaWiki NirvanaWiki} instance.
     * @param pages an array of pages.
     */
    public WikiBooster(NirvanaWiki wiki, String[] pages) {
        this.wiki = wiki;
        this.pages = new ArrayList<>(Arrays.asList(pages));
        pagesSet = new HashSet<>(this.pages);
    }

    /**
     * Constructor taking a wiki instance and page list.
     * If you need a booster for another set of pages, please create a new instance for that.
     * 
     * @param wiki {@link org.wikipedia.nirvana.NirvanaWiki NirvanaWiki} instance.
     * @param pages list of pages.
     */
    public WikiBooster(NirvanaWiki wiki, List<String> pages) {
        this.wiki = wiki;
        this.pages = pages;
        pagesSet = new HashSet<>(pages);
    }

    /**
     * Constructs <code>WikiBooster</code> taking a wiki instance and list of revisions.
     * Page list will be taken from those revisions.
     * If you need a booster for another set of pages, please create a new instance for that.
     * 
     * @param wiki {@link org.wikipedia.nirvana.NirvanaWiki NirvanaWiki} instance.
     * @param revs list of revisions of class {@link org.wikipedia.Wiki.Revision Revision}.
     */
    public static WikiBooster create(NirvanaWiki wiki, List<Revision> revs) {
        List<String> pages = new ArrayList<>(revs.size());
        for (int i = 0; i < revs.size(); i++) {
            pages.add(revs.get(i).getPage());
        }
        return new WikiBooster(wiki, pages);
    }

    /**
     * Gets the list of templates used on a particular page that are in a particular namespace(s).
     *
     * @param title the title of the page.
     * @param ns a list of namespaces to filter by, empty = all namespaces.
     * @return the list of templates used on that page in that namespace.
     * @see org.wikipedia.Wiki#getTemplates(String, int...)
     */
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
            boolean usePostOld = wiki.isUsingPost();
            // Actually, POST here is slower than GET, so I leave it commented
            // wiki.setUsePost(true);
            log.debug("Request templates for " + pages.size() + " pages.");
            String[][] pagesTemplates =
                    wiki.getPagesTemplates(pages.toArray(new String[pages.size()]), ns);
            wiki.setUsePost(usePostOld);
            templatesCache = new HashMap<>();
            for (int i = 0; i < pages.size(); i++) {
                templatesCache.put(pages.get(i), Arrays.asList(pagesTemplates[i]));
            }
        }
        return templatesCache.get(title);
    }

    /**
     * Gets the raw wikicode for a page.
     *
     * @param title the title of the page.
     * @return the raw wikicode of a page.
     * @see org.wikipedia.Wiki#getPageText(String)
     */
    public String getPageText(String title) throws IOException {
        if (!pagesSet.contains(title)) {
            throw new RuntimeException("The booster is not prepared for page: " + title);
        }
        if (pageTextCache == null) {
            log.debug("Request texts for " + pages.size() + " pages.");
            String[] texts = wiki.getPagesTexts(pages.toArray(new String[pages.size()]));
            pageTextCache = new HashMap<>();
            for (int i = 0; i < pages.size(); i++) {
                pageTextCache.put(pages.get(i), texts[i]);
            }
        }
        return pageTextCache.get(title);
    }

    /**
     * Use it when you are sure that you will not use the specified page.
     * Good for performance. Why fetch anything for it if will not use it?
     * Please use this method carefully.
     *
     * @param title the title of the page that should be removed from processing
     */
    public void removePage(String title) {
        pages.remove(title);
        pagesSet.remove(title);
    }
}
