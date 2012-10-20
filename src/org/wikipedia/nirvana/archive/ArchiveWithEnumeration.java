/**
 *  @(#)ArchiveWithEnumeration.java 02/07/2012
 *  Copyright © 2012 Dmitry Trofimovich (KIN)
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

package org.wikipedia.nirvana.archive;

import java.io.IOException;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.StringUtils;
import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.archive.ArchiveSettings.Enumeration;

/**
 * @author kin
 *
 */
public class ArchiveWithEnumeration extends ArchiveSimple {
	String oldText;
	/**
	 * @param addToTop
	 * @param delimeter
	 */
	public ArchiveWithEnumeration(String text, boolean addToTop, String delimeter) {
		super(addToTop, delimeter);
		log.debug("ArchiveWithEnumeration created");
		this.enumeration = Enumeration.HTML_GLOBAL;
		oldText = trimEnumerationAndWhiteSpace(text);
	}
	public ArchiveWithEnumeration(String lines[], boolean addToTop, String delimeter) {
		super(addToTop, delimeter);
		this.enumeration = Enumeration.HTML_GLOBAL;
		int i = 0;
		while(i<lines.length && lines[i].isEmpty()) i++;
		if(lines[i].compareToIgnoreCase(OL)==0) i++;
		int j = lines.length-1;
		while(j>=0 && lines[j].isEmpty()) j--;
		if(lines[j].compareToIgnoreCase(OL_END)==0) j--;
		oldText = StringUtils.join(lines,delimeter,i,j+1);
	}
	public void add(String item) {
		String str = item;
		if(str.startsWith("*") || str.startsWith("#")) {
			str = "<li>"+str.substring(1);
		} else {
			str = "<li> " + str;
		}
		super.add(str);
	}
	
	public String toString() {
		if(addToTop) {
			if(oldText.isEmpty()) 
				return OL+delimeter+StringUtils.join(items, delimeter)+delimeter+OL_END;
			else
				return OL+delimeter+StringUtils.join(items, delimeter)+delimeter+oldText+delimeter+OL_END;
		}
		else {
			if(oldText.isEmpty())
				return OL+delimeter+StringUtils.join(items, delimeter)+delimeter+OL_END;
			else
				return OL+delimeter+oldText+delimeter+StringUtils.join(items, delimeter)+delimeter+OL_END;
		}
	}
	
	public void update(NirvanaWiki wiki,String archiveName, boolean minor, boolean bot) throws LoginException, IOException {
		wiki.edit(archiveName, toString(), 
				"+"+newItemsCount()+" статей", minor, bot);
	}

}
