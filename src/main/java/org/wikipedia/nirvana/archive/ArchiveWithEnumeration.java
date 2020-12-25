/**
 *  @(#)ArchiveWithEnumeration.java
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

import static org.wikipedia.nirvana.archive.EnumerationUtils.OL;
import static org.wikipedia.nirvana.archive.EnumerationUtils.OL_END;

import org.wikipedia.nirvana.wiki.NirvanaWiki;

import java.io.IOException;
import java.util.Calendar;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;

/**
 * Extended version of simple archive that has additional enumeration with html code.
 *
 */
public class ArchiveWithEnumeration extends ArchiveSimple {

    String oldText = "";

    /**
     * Constructs archive class using specified archive text and archive settings.
     *
     * @param addToTop flag where to add new page items. <code>true</code> to add at top, 
     *     <code>false</code> to add at bottom.
     */
    public ArchiveWithEnumeration(boolean addToTop) {
        super(addToTop);
        log.debug("ArchiveWithEnumeration created");
    }

    @Override
    public void read(NirvanaWiki wiki, String archivePage) throws IOException {
        String text = wiki.getPageText(archivePage);
        if (text == null) {
            text = "";
        }
        oldText = EnumerationUtils.trimEnumerationAndWhitespace(text);
    }

    @Override
    public void add(String item, @Nullable Calendar creationDate) {
        String str = item;
        if (str.startsWith("*") || str.startsWith("#")) {
            str = "<li>" + str.substring(1);
        } else {
            str = "<li> " + str;
        }
        super.add(str, creationDate);
    }

    @Override
    public String toString() {
        String old = oldText;
        if (!old.isEmpty()) {
            old += "\n";
        }
        StringBuilder buf = new StringBuilder();
        buf.append(OL).append("\n");
        if (!addToTop) {
            buf.append(old);
        }
        for (String item: items) {
            buf.append(item).append("\n");
        }
        if (addToTop) {
            buf.append(old);
        }
        buf.append(OL_END);
        return buf.toString();
    }

    @Override
    public void update(NirvanaWiki wiki,String archiveName, boolean minor, boolean bot)
            throws LoginException, IOException {
        wiki.edit(archiveName, toString(), updateSummary(), minor, bot);
    }
}
