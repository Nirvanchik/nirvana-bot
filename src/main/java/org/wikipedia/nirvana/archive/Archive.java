/**
 *  @(#)Archive.java
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

import org.wikipedia.nirvana.archive.ArchiveSettings.Enumeration;
import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Calendar;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;


// TODO: Add another base class for archives. Split updatable and fixer archives.
/**
 * Abstract class for archive pages of different types.
 * One Archive class represents one Wiki page with an archived list of new pages. 
 *
 */
public abstract class Archive {
    protected final Logger log;

    protected boolean addToTop = true;
    protected int newLines = 0;
    protected String delimeter = "\n";
    protected Enumeration enumeration = Enumeration.NONE;
    public static final String OL = "<ol>";
    public static final String OL_END = "</ol>";

    /**
     * Prints all archive contents to string.
     */
    public String toString() {
        return "";
    }

    /**
     * Adds list item to this archive according to this archive rules (settings).
     *
     * @param item new page list item (from "New pages" wiki list).
     * @param creationDate new page creation date (if available).
     */
    public abstract void add(String item, @Nullable Calendar creationDate);

    /**
     * @return How many items were added to this archive.
     */
    public int newItemsCount() {
        return newLines;
    }

    /**
     * Default constructor.
     */
    public Archive() {
        log = LogManager.getLogger(Archive.class.getName());
    }

    /**
     * Updates wiki page with a new archived items.
     * NOTE: not all archive classes support this method.
     *
     * @param wiki instance of {@link org.wikipedia.nirvana.wiki.NirvanaWiki}.
     * @param archiveName Title of Wiki page where this archive exists.  
     * @param minor whether the edit should be marked as minor. See
     *     [[Help:Minor edit]]. Overrides {@link #isMarkMinor()}.
     * @param bot whether to mark the edit as a bot edit.
     */
    public abstract void update(NirvanaWiki wiki, String archiveName, boolean minor, boolean bot)
            throws LoginException, IOException;

    /**
     * @return summary string used when updating archive wiki page.
     */
    protected String updateSummary() {
        Localizer localizer = Localizer.getInstance();
        return "+" + newItemsCount() + " " + localizer.localize("статей");
    }

    // TODO: Move it to utils
    /**
     * Removes html enumeration tags from text (if text starts/ends with them).
     *
     * @param text text to process
     * @return updated text (with enumerations tags stripped).
     */
    public static String trimEnumerationAndWhiteSpace(String text) {
        String oldText = text.trim();
        if (oldText.startsWith(OL)) {
            oldText = oldText.substring(OL.length());        
        }
        if (oldText.endsWith(OL_END)) {
            oldText = oldText.substring(0,oldText.length() - OL_END.length());
        }
        oldText = oldText.trim();
        return oldText;
    }

}
