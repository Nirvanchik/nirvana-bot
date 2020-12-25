/**
 *  @(#)ArchiveSimple.java
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

package org.wikipedia.nirvana.archive;

import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;

/**
 * Usual archive page that keeps new page items as simple list.
 */
public class ArchiveSimple extends Archive {
    protected ArrayList<String> items;

    /**
     *  Prints all archive contents to string.
     */
    @Override
    public String toString() {
        if (addToTop) {
            // Для склейки нужен перенос строки
            return StringUtils.join(items, "\n") + "\n";
        } else {
            // Для склейки нужно отсутствие переноса
            return StringUtils.join(items, "\n");
        }
    }

    /**
     * Default constructor.
     *
     * @param addToTop flag where to add new page items. <code>true</code> to add at top, 
     *     <code>false</code> to add at bottom.
     */
    public ArchiveSimple(boolean addToTop) {
        log.debug("ArchiveSimple created");
        this.addToTop = addToTop;
        items = new ArrayList<String>();
    }

    @Override
    public void add(String item, @Nullable Calendar creationDate) {
        if (this.addToTop) {
            items.add(0, item);
        } else {
            items.add(item);
        }
    }

    @Override
    public int newItemsCount() {
        return items.size();
    }

    @Override
    public void update(NirvanaWiki wiki, String archiveName, boolean minor, boolean bot)
            throws LoginException, IOException {
        if (addToTop) {
            wiki.prependOrCreate(archiveName, toString(), updateSummary(), minor, bot);
        } else {
            wiki.appendOrCreate(archiveName, toString(), updateSummary(), minor, bot);
        }
    }

    @Override
    public void read(NirvanaWiki wiki, String archivePage) throws IOException {
        // Do nothing. Old text is not required for this archive type.        
    }
}
