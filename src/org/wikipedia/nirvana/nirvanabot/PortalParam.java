/**
 *  @(#)PortalParam.java 23/08/2012
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

package org.wikipedia.nirvana.nirvanabot;

import java.util.List;

/**
 * @author kin
 *
 */
public class PortalParam {
	public String lang;
	public List<String> categories; 
	public List<String> categoriesToIgnore;
	public List<String> usersToIgnore;
	public String page;
	public String archive;
	public ArchiveSettings archSettings;
	public int ns;
	public int depth;
	public int hours;
	public int maxItems;
	public String format;
	public String delimeter;
	public String header;
	public String footer;
	public boolean minor;
	public boolean bot;
	Deleted deletedFlag;
	public String imageSearchTags;
	public enum Deleted{
		DONT_TOUCH,
		REMOVE,
		MARK
	}
	public PortalParam() {
		lang = "ru";
		minor = true;
		bot = true;
		deletedFlag = Deleted.DONT_TOUCH;
		imageSearchTags = null;
	}
}
