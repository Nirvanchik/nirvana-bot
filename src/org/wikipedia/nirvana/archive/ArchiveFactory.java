/**
 *  @(#)ArchiveFactory.java 02/07/2012
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

import java.io.FileNotFoundException;
import java.io.IOException;
import org.wikipedia.nirvana.NirvanaWiki;

/**
 * @author kin
 *
 */
public class ArchiveFactory {

	protected static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ArchiveFactory.class.getName());
	/**
	 * @throws IOException 
	 * 
	 */
	public static Archive createArchive(ArchiveSettings archiveSettings, NirvanaWiki wiki, String name, String delimeter) throws IOException {
		return createArchive(archiveSettings,wiki,name,delimeter,false);
	}
	public static Archive createArchive(ArchiveSettings archiveSettings, NirvanaWiki wiki, String name, String delimeter, boolean empty) throws IOException {
		Archive archive = null;
		log.debug("creating archive: "+name);
		if(archiveSettings.removeDuplicates) {
			String lines[] = new String[0];
			if(!empty) {
				try{
					lines = wiki.getPageLines(name);
				} catch(FileNotFoundException e) {
					//log.info("archive "+arname+" is empty");
				}
			}
			archive = new ArchiveUnique(wiki,lines,archiveSettings.addToTop,delimeter);
		} else if(archiveSettings.withoutHeaders()) {
			if(!archiveSettings.hasHtmlEnumeration()) {
				if(archiveSettings.sorted) {
					String lines[] = new String[0];
					if(!empty) {
						try{
							lines = wiki.getPageLines(name);
						} catch(FileNotFoundException e) {
							//log.info("archive "+arname+" is empty");
						}
					}
					archive = new ArchiveSimpleSorted(wiki,lines,archiveSettings.addToTop,delimeter);
				}
				else	
					archive = new ArchiveSimple(archiveSettings.addToTop,delimeter);
			} else {
				String text = "";
				if(!empty) {
					try{
						text = wiki.getPageText(name);
					} catch(FileNotFoundException e) {
						//log.info("archive "+arname+" is empty");
					}
				}
				archive = new ArchiveWithEnumeration(text,archiveSettings.addToTop,delimeter);
			}			
		} else {
			String lines[] = new String[0];
			if(!empty) {
				try{
					lines = wiki.getPageLines(name);
				} catch(FileNotFoundException e) {
					//log.info("archive "+arname+" is empty");
				}
			}
			if(archiveSettings.headerFormat.contains(ArchiveWithHeadersWithItemsCount.template) /*||
					(archiveSettings.headerHeaderFormat!=null &&
					archiveSettings.headerHeaderFormat.contains(ArchiveWithHeadersWithItemsCount.template))*/	) {
				
				archive = new ArchiveWithHeadersWithItemsCount(lines,
						archiveSettings.parseCount,
						archiveSettings.addToTop,delimeter,
						archiveSettings.enumeration,archiveSettings.headerFormat);
			} else {
				
				archive = new ArchiveWithHeaders(lines,
						archiveSettings.parseCount,
						archiveSettings.addToTop,delimeter,
						archiveSettings.enumeration);
			}
			((ArchiveWithHeaders)archive).initLatestItemHeaderHeader(wiki,archiveSettings);
		}
		return archive;
	}

}
