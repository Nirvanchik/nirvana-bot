/**
 *  @(#)ArchiveSimple.java 02/07/2012
 *  Copyright © 2011 - 2012 Dmitry Trofimovich (KIN)(DimaTrofimovich@gmail.com)
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
import java.util.ArrayList;
import java.util.Calendar;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.StringUtils;
import org.wikipedia.nirvana.NirvanaWiki;

public class ArchiveSimple extends Archive {
	protected ArrayList<String> items;
	public String toString() {
		if(addToTop)
            // Для склейки нужен перенос строки
            return StringUtils.join(items, delimeter) + delimeter;
		else
            // Для склейки нужно отсутствие переноса
            return StringUtils.join(items, delimeter);
	}
	
	public ArchiveSimple(boolean addToTop, String delimeter) {
		log.debug("ArchiveSimple created");
		this.addToTop = addToTop;
		this.delimeter = delimeter;
		items = new ArrayList<String>();
	}

    @Override
    public void add(String item, Calendar c) {
		this.newLines++;
		if(this.addToTop) {
			items.add(0, item);
		} else {
			items.add(item);
		}
	}
	public void update(NirvanaWiki wiki, String archiveName, boolean minor, boolean bot) throws LoginException, IOException {
		if(addToTop) {
			wiki.prependOrCreate(archiveName, toString(), 
                    "+" + newItemsCount() + " статей", minor, bot);
		} else {
			wiki.appendOrCreate(archiveName, toString(), 
                    "+" + newItemsCount() + " статей", minor, bot);
		}
	}
}
