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
     * Creates archive container and fill it with an archived items from existring Wiki page.
     *
     * @param archiveSettings Instance of {@link ArchiveSettings}. Archive settings.
     * @param wiki Instance of {@link NirvanaWiki} class. Will be used to get old archive page.
     * @param name Wiki page name of this archive
     * @return instance of archive containter.
     */
    public static Archive createArchiveAndRead(ArchiveSettings archiveSettings, NirvanaWiki wiki,
            String name) throws IOException {
        Archive archive = createArchive(archiveSettings, wiki, name);
        archive.read(wiki, name);
        return archive;
    }

    /**
     * Creates empty Archive.
     * 
     * @param archiveSettings Instance of {@link ArchiveSettings}. Archive settings.
     * @param wiki Instance of {@link NirvanaWiki} class. Will be used to get old archive page.
     * @param name Wiki page of this archive
     * @return instance of archive containter.
     */
    public static Archive createArchive(ArchiveSettings archiveSettings, NirvanaWiki wiki,
            String name) throws IOException {
        log.debug("Creating archive: {}", name);
        if (!archiveSettings.hasHeaders()) {
            if (!archiveSettings.hasHtmlEnumeration()) {
                return new ArchiveSimple(archiveSettings.addToTop);
            } else {
                return new ArchiveWithEnumeration(archiveSettings.addToTop);
            }
        } else {
            return new ArchiveWithHeaders(archiveSettings.parseCount,
                    archiveSettings.addToTop, archiveSettings.enumeration,
                    archiveSettings.headerFormat, archiveSettings.superHeaderFormat);

        }
    }
}
