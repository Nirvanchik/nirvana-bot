/**
 *  @(#)ArchiveFactory.java
 *  Copyright © 2020 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Creates required instance of {@link Archive} subclass.
 * Depending on settings {@link ArchiveSettings} it will select the most suitable variant of
 * Archive container.
 */
public class ArchiveFactory {

    protected static final Logger log;

    static {
        log = LogManager.getLogger(ArchiveFactory.class.getName());
    }

    /**
     * Creates a new Archive. This archive container will be empty.
     * 
     */
    public static Archive createArchive(ArchiveSettings archiveSettings, NirvanaWiki wiki,
            String name, String delimeter) throws IOException {
        return createArchive(archiveSettings, wiki, name, delimeter, false);
    }

    // TODO: This is too difficult. Split this factory to 2 methods or 2 factory classes.
    // TODO: Make another constructor instead of "empty" flag
    /**
     * Creates a new or existing Archive. This archive container will be empty or filled with
     * an old archived items from Wiki text depending on the provided flags.
     * 
     * @param archiveSettings Instance of {@link ArchiveSettings}. Archive settings.
     * @param wiki Instance of {@link NirvanaWiki} class. Will be used to get old archive page.
     * @param name Wiki page of this archive
     * @param delimeter A string that separates archive items (usually "\n").
     * @param empty if <code>true</code> the archive container will be filled with items from
     *     Wiki page.
     * @return instance of archive containter.
     */
    public static Archive createArchive(ArchiveSettings archiveSettings, NirvanaWiki wiki,
            String name, String delimeter, boolean empty) throws IOException {
        Archive archive = null;
        log.debug("Creating archive: {}", name);
        if (archiveSettings.removeDuplicates) {
            String [] lines = new String[0];
            if (!empty) {
                lines = wiki.getPageLinesArray(name);
            }
            // It is allways empty : "lines" ignored
            archive = new ArchiveUnique(wiki,lines,archiveSettings.addToTop,delimeter);
        } else if (archiveSettings.withoutHeaders()) {
            if (!archiveSettings.hasHtmlEnumeration()) {
                if (archiveSettings.sorted) {
                    String [] lines = new String[0];
                    if (!empty) {
                        lines = wiki.getPageLinesArray(name);
                    }
                    // It is allways non-emplty, it's filled with items
                    archive = new ArchiveSimpleSorted(wiki, lines, archiveSettings.addToTop,
                            delimeter);
                } else {    
                    // It is allways empty
                    archive = new ArchiveSimple(archiveSettings.addToTop, delimeter);
                }
            } else {
                String text = "";
                if (!empty) {
                    text = wiki.getPageText(name);
                    if (text == null) {
                        text = "";
                    }
                }
                // It is allways empty
                archive = new ArchiveWithEnumeration(text, archiveSettings.addToTop, delimeter);
            }            
        } else {
            String [] lines = new String[0];
            if (!empty) {
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
