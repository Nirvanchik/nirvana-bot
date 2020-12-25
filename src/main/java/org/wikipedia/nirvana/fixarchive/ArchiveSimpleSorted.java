/**
 *  @(#)ArchiveSimpleSorted.java
 *   Copyright © 2020 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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
 **/

package org.wikipedia.nirvana.fixarchive;

import org.wikipedia.nirvana.archive.ArchiveSimple;
import org.wikipedia.nirvana.nirvanabot.NewPages;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;


/**
 * Archive which is created in order to sort existing archive items by creation date.
 *
 */
public class ArchiveSimpleSorted extends ArchiveSimple {
    NirvanaWiki wiki;
    SortedMap<Calendar,String> itemsSorted;

    // TODO: Do not do this in constructor!
    /**
     * Constructor.
     *
     * @param wiki NirvanaWiki instance. Required to call getFirstRevision() in order to find page
     *     creation date if it is not available in new page item.
     * @param lines contents of existing wiki archive (new page items).
     * @param addToTop flag where to add new page items. <code>true</code> to add at top, 
     *     <code>false</code> to add at bottom.
     */
    public ArchiveSimpleSorted(NirvanaWiki wiki, String [] lines, boolean addToTop) {
        super(addToTop);
        this.wiki = wiki;
        Comparator<Calendar> compA = new Comparator<Calendar>() {
            @Override
            public int compare(Calendar left, Calendar right) {                
                return left.compareTo(right);
            }
        };
        Comparator<Calendar> compB = new Comparator<Calendar>() {
            @Override
            public int compare(Calendar left, Calendar right) {                
                return right.compareTo(left);
            }
        };
        if (addToTop) {
            itemsSorted = new TreeMap<Calendar, String>(compB); // descending
        } else {
            itemsSorted = new TreeMap<Calendar, String>(compA); // ascending
        }
        for (String line:lines) {
            Calendar c = NewPages.getNewPagesItemDate(wiki, line);
            add(line, c);
        }
    }

    // TODO: This code is very strange and unclear
    @Override
    public void add(String item, @Nullable Calendar creationDate) {
        if (creationDate == null) {
            super.add(item, creationDate);
        } else {
            this.itemsSorted.put(creationDate, item);
        }
    }

    // TODO: This code is very strange and unclear
    /**
     *  Prints all archive contents to string.
     */
    @Override
    public String toString() {
        Iterator<Calendar> it = itemsSorted.keySet().iterator();
        StringBuffer sb = new StringBuffer(10000);        
        while (it.hasNext()) {
            sb.append(itemsSorted.get(it.next()));
            sb.append("\n");
        }
        if (addToTop) {
            sb.append(StringUtils.join(items, "\n"));
            return sb.toString();
        } else {
            return StringUtils.join(items, "\n") + "\n" + sb.toString();
        }        
    }
    
    @Override
    public void update(NirvanaWiki wiki, String archiveName, boolean minor, boolean bot)
            throws LoginException, IOException {
        throw new UnsupportedOperationException("update is not supported, use toString() instead");
    }
}
