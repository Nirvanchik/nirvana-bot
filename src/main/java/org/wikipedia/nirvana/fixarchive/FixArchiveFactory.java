/**
 *  @(#)FixArchiveFactory.java
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

package org.wikipedia.nirvana.fixarchive;

import org.wikipedia.nirvana.archive.Archive;
import org.wikipedia.nirvana.archive.ArchiveFactory;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import java.io.IOException;

/**
 * Archive factory used by FixArchiveBot.
 * Creates empty Archive container, configured with archive settings and used to reformat existing
 * archive (sort, remove duplicates, add/remove headers, add/remove HTML enumeration markers, etc.).
 */
public class FixArchiveFactory extends ArchiveFactory {
    /**
     * Creates a new Archive. This archive container will be empty.
     * 
     * @param archiveSettings Instance of {@link ArchiveProcessingSettings}. Archive settings.
     * @param wiki Instance of {@link NirvanaWiki} class. Will be used to get old archive page.
     * @param name Wiki page of this archive
     * @return instance of archive containter.
     */
    public static Archive createArchive(ArchiveProcessingSettings archiveSettings, NirvanaWiki wiki,
            String name) throws IOException {
        Archive archive = null;
        log.debug("Creating archive: {}", name);
        
        if (archiveSettings.removeDuplicates) {
            String [] lines = new String[0];
            archive = new ArchiveUnique(wiki, lines, archiveSettings.addToTop);
        } else if (archiveSettings.sorted) {
            if (archiveSettings.hasHeaders()) {
                throw new IllegalArgumentException("Sort command is not supported for archives "
                        + "with headers");
            }
            if (archiveSettings.hasHtmlEnumeration()) {
                throw new IllegalArgumentException("Sort command is not supported for archives "
                        + "with html enumeration");
            }
            String [] lines = new String[0];
            archive = new ArchiveSimpleSorted(wiki, lines, archiveSettings.addToTop);
        } else {
            archive = ArchiveFactory.createArchive(archiveSettings, wiki, name);
        }
        return archive;
    }
}
