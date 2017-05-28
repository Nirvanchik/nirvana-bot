/**
 *  @(#)Archive.java 02/07/2012
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
 * Recommended code page for this file is CP1251 (also called Windows-1251).
 * */

package org.wikipedia.nirvana.archive;

import java.io.IOException;
import java.util.Calendar;

import javax.security.auth.login.LoginException;

import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.archive.ArchiveSettings.Enumeration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Abstract class for archive pages of different types. 
 *
 */
public abstract class Archive {
    protected final Logger log;

	protected boolean addToTop = true;
	//private String latestItemHeader = null;
	protected int newLines = 0;
	protected String delimeter = "\n";
	//protected boolean hasOL = false;
	//protected boolean globalEnumeration = false;
	//protected String archiveName = "";
	protected Enumeration enumeration = Enumeration.NONE;
	public static final String OL = "<ol>";
	public static final String OL_END = "</ol>";

	public String toString() {
		return "";
	}

    /**
     * Adds list item to this archive according to this archive rules (settings).
     *
     * @param item list item (from "New pages" wiki list).
     * @param c list item date, or null if it's not possible to find out the date.
     */
    public abstract void add(String item, Calendar c);
	
	public int newItemsCount() { return newLines; }

    public Archive() {
        log = LogManager.getLogger(Archive.class.getName());
    }

	public void update(NirvanaWiki wiki, String archiveName, boolean minor, boolean bot) throws LoginException, IOException {
		
	}
	
	public static String trimEnumerationAndWhiteSpace(String text) {
		String oldText = text.trim();
		if(oldText.startsWith(OL)) {
			oldText = oldText.substring(OL.length());		
		}
		if(oldText.endsWith(OL_END)) {
			oldText = oldText.substring(0,oldText.length()-OL_END.length());
		}
		oldText = oldText.trim();
		return oldText;
	}

}
