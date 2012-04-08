/**
 *  @(#)ArchiveNoHeaders.java 07/04/2012
 *  Copyright © 2011 - 2012 Dmitry Trofimovich (KIN)
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

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

public class ArchiveNoHeaders extends Archive {
	private ArrayList<String> items;
	public String toString() {
		if(addToTop)
			return StringUtils.join(items, delimeter)+delimeter;// перенос строки нужен для склейки
		else
			return StringUtils.join(items, delimeter); // для склейки нужно отсутствие переноса 
	}
	
	public ArchiveNoHeaders(boolean addToTop, String delimeter) {
		this.addToTop = addToTop;
		this.delimeter = delimeter;
		items = new ArrayList<String>();
	}
	
	public void add(String item) {
		this.newLines++;
		if(this.addToTop) {
			items.add(0, item);
		} else {
			items.add(item);
		}
	}

}
