/**
 *  @(#)ArchiveFactory.java 02/07/2012
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

import org.wikipedia.nirvana.nirvanabot.BotVariables;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * @author kin
 *
 */
public class ArchiveFactory {

    protected static final Logger log;

    static {
        log = LogManager.getLogger(ArchiveFactory.class.getName());
    }

	/**
	 * @throws IOException 
	 * 
	 */
	public static Archive createArchive(ArchiveSettings archiveSettings, NirvanaWiki wiki, String name, String delimeter) throws IOException {
		return createArchive(archiveSettings,wiki,name,delimeter,false);
	}

    // TODO: Make another constructor instead of "empty" flag
	public static Archive createArchive(ArchiveSettings archiveSettings, NirvanaWiki wiki, String name, String delimeter, boolean empty) throws IOException {
		Archive archive = null;
		log.debug("creating archive: "+name);
		if(archiveSettings.removeDuplicates) {
			String lines[] = new String[0];
			if(!empty) {
                lines = wiki.getPageLinesArray(name);
			}
			archive = new ArchiveUnique(wiki,lines,archiveSettings.addToTop,delimeter);
		} else if(archiveSettings.withoutHeaders()) {
			if(!archiveSettings.hasHtmlEnumeration()) {
				if(archiveSettings.sorted) {
					String lines[] = new String[0];
					if(!empty) {
                        lines = wiki.getPageLinesArray(name);
					}
					archive = new ArchiveSimpleSorted(wiki,lines,archiveSettings.addToTop,delimeter);
				}
				else	
					archive = new ArchiveSimple(archiveSettings.addToTop,delimeter);
			} else {
				String text = "";
				if(!empty) {
                    text = wiki.getPageText(name);
                    if (text == null) {
                        text = "";
                    }
				}
				archive = new ArchiveWithEnumeration(text,archiveSettings.addToTop,delimeter);
			}			
		} else {
			String lines[] = new String[0];
			if(!empty) {
                lines = wiki.getPageLinesArray(name);
			}

            archive = new ArchiveWithHeaders(lines, archiveSettings.parseCount,
                    archiveSettings.addToTop, delimeter, archiveSettings.enumeration,
                    archiveSettings.headerFormat, archiveSettings.superHeaderFormat);

            ((ArchiveWithHeaders) archive).initLatestItemHeaderHeader(wiki, archiveSettings);
		}
		return archive;
	}
}
