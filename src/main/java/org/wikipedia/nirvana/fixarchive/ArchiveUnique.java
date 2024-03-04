/**
 *  @(#)ArchiveUnique.java
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

package org.wikipedia.nirvana.fixarchive;

import org.wikipedia.nirvana.archive.ArchiveSimple;
import org.wikipedia.nirvana.nirvanabot.NewPageItemParser;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

import javax.security.auth.login.LoginException;

/**
 * Archive which is used to remove duplicated items from existing archive page.
 *
 */
public class ArchiveUnique extends ArchiveSimple {
    NirvanaWiki wiki;

    protected HashMap<String,Integer> uniqueItemIndexes;
    private final NewPageItemParser newPageItemParser;

    @Override
    public String toString() {
        while (items.remove(null));
        if (addToTop) {
            Collections.reverse(items);
            return super.toString();
        } else {
            return super.toString();  // Для склейки нужно отсутствие переноса.
        }
    }

    /**
     * Constructs archive object using existing archive items, wiki, and some settings.
     *
     * @param wiki NirvanaWiki instance. Required to call getFirstRevision() in order to find page
     *     creation date if it is not available in new page item.
     * @param lines contents of existing wiki archive (new page items).
     * @param addToTop flag where to add new page items. <code>true</code> to add at top, 
     *     <code>false</code> to add at bottom.
     */
    public ArchiveUnique(NirvanaWiki wiki, String [] lines, boolean addToTop) {
        super(addToTop);
        log.debug("ArchiveUnique created");
        this.wiki = wiki;
        uniqueItemIndexes = new HashMap<String,Integer>();
        newPageItemParser = new NewPageItemParser();
    }

    @Override
    public void add(String item, Calendar creationDate) {
        String title = newPageItemParser.getNewPagesItemArticle(item);
        if (title != null) {
            String origTitle = null;
            try {
                origTitle = wiki.resolveRedirect(title);
            } catch (IOException e) {
                throw new RuntimeException("Failed to resolve redirect", e);
            }
            if (origTitle != null) {
                title = origTitle;
            }
            if (this.uniqueItemIndexes.containsKey(title)) {
                // skip
            } else {                
                items.add(item);
                uniqueItemIndexes.put(title, items.size() - 1);
            }
        } else {
            items.add(item);
        }
    }

    @Override
    public void update(NirvanaWiki wiki,String archiveName, boolean minor, boolean bot)
            throws LoginException, IOException {
        throw new UnsupportedOperationException("update is not supported, use toString() instead");
    }
}
