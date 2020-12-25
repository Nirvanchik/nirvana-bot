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

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Calendar;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;

/**
 * Extended version of simple archive that has additional enumeration with html code.
 *
 */
public class ArchiveWithEnumeration extends ArchiveSimple {

    String oldText;

    /**
     * Constructs archive class using specified archive text and archive settings.
     *
     * @param text text with archived new page items.
     * @param addToTop flag where to add new page items. <code>true</code> to add at top, 
     *     <code>false</code> to add at bottom.
     * @param delimeter separator character or string inserted between new page items.
     */
    public ArchiveWithEnumeration(String text, boolean addToTop, String delimeter) {
        super(addToTop, delimeter);
        log.debug("ArchiveWithEnumeration created");
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

    // TODO: Use StringBuilder
    @Override
    public String toString() {
        if (addToTop) {
            if (oldText.isEmpty()) {
                return OL + delimeter + StringUtils.join(items, delimeter) + delimeter + OL_END;
            } else {
                return OL + delimeter + StringUtils.join(items, delimeter) + delimeter + oldText +
                        delimeter + OL_END;
            }
        } else {
            if (oldText.isEmpty()) {
                return OL + delimeter + StringUtils.join(items, delimeter) + delimeter + OL_END;
            } else {
                return OL + delimeter + oldText + delimeter + StringUtils.join(items, delimeter) +
                        delimeter + OL_END;
            }
        }
    }

    @Override
    public void update(NirvanaWiki wiki,String archiveName, boolean minor, boolean bot)
            throws LoginException, IOException {
        wiki.edit(archiveName, toString(), updateSummary(), minor, bot);
    }
}
