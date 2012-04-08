/**
 *  @(#)Archive.java 07/04/2012
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


/**
 * @author kin
 *
 */
public class Archive {
	protected boolean addToTop = true;
	//private String latestItemHeader = null;
	protected int newLines = 0;
	protected String delimeter = "\n";
	
	protected static org.apache.log4j.Logger log = null;	

	public String toString() {
		return "";
	}
	
	public void add(String item) {
		
	}
	
	public int newItemsCount() { return newLines; }
	
	public Archive() {
		log = org.apache.log4j.Logger.getLogger(Archive.class.getName());
	}

}
