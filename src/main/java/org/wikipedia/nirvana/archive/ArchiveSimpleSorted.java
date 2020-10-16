/**
 *  @(#)ArchiveSimpleSorted.java 0.01 20/10/2012
 *  Copyright © 2012 Dmitry Trofimovich (KIN)(DimaTrofimovich@gmail.com)
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

import java.io.IOException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.StringUtils;

import org.wikipedia.nirvana.nirvanabot.NewPages;
import org.wikipedia.nirvana.wiki.NirvanaWiki;


/**
 * @author kin
 *
 */
public class ArchiveSimpleSorted extends ArchiveSimple {
	NirvanaWiki wiki;
	SortedMap<Calendar,String> itemsSorted;

	/**
	 * 
	 */
	public ArchiveSimpleSorted(NirvanaWiki wiki, String lines[], boolean addToTop, String delimeter) {
		super(addToTop, delimeter);
		this.wiki = wiki;
		Comparator<Calendar> compA = new Comparator<Calendar>(){
			@Override
			public int compare(Calendar a, Calendar b) {				
				return a.compareTo(b);
			}
		};
		Comparator<Calendar> compB = new Comparator<Calendar>(){
			@Override
			public int compare(Calendar a, Calendar b) {				
				return b.compareTo(a);
			}
		};
		if(addToTop)
			itemsSorted = new TreeMap<Calendar, String>(compB); // descending
		else
			itemsSorted = new TreeMap<Calendar, String>(compA); // ascending
		for(String line:lines) {
            Calendar c = NewPages.getNewPagesItemDate(wiki, line);
            add(line, c);
		}
	}

    @Override
    public void add(String item, Calendar c) {
		if(c==null) {
            super.add(item, c);
		} else {
			this.itemsSorted.put(c, item);
		}
	}

	public String toString() {
		Iterator<Calendar> it = itemsSorted.keySet().iterator();
		StringBuffer sb = new StringBuffer(10000);		
		while(it.hasNext()) {
			sb.append(itemsSorted.get(it.next()));
			sb.append(delimeter);
		}
		if(addToTop) {
			sb.append(StringUtils.join(items, delimeter));
			return sb.toString();
		} else {
			return StringUtils.join(items, delimeter)+delimeter+sb.toString();
		}		
	}
	
	public void update(NirvanaWiki wiki,String archiveName, boolean minor, boolean bot) throws LoginException, IOException {
		throw new java.lang.UnsupportedOperationException("update is not supported, use toString() instead");
	}


}
