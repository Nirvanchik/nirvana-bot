/**
 *  @(#)PortalParam.java 14.12.2014
 *  Copyright © 2012 - 2014 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.wikipedia.nirvana.WikiTools;
import org.wikipedia.nirvana.archive.ArchiveSettings;

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
	public WikiTools.Service service;
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
	public String middle;
	public boolean minor;
	public boolean bot;
	public boolean fastMode;
	Deleted deletedFlag;
	public int renamedFlag;
	public String imageSearchTags;
	public int updatesPerDay;
	public List<String> templates;
	public WikiTools.EnumerationType templatesEnumType;
	public String prefix;
	
	public static final int MAX_CAT_GROUPS = 20;
	
	public List<List<String>> categoryGroups;
	public List<List<String>> categoryToIgnoreGroups;
	
	public enum Deleted{
		DONT_TOUCH,
		REMOVE,
		MARK
	}
	
	public static final int RENAMED_OLD = 1;
	public static final int RENAMED_NEW = 2;
	//public static final int RENAMED_MARK = 4;
	
	public PortalParam() {
		lang = "ru";
		minor = true;
		bot = true;
		deletedFlag = Deleted.DONT_TOUCH;
		imageSearchTags = null;
		updatesPerDay = 1;
	}
}
