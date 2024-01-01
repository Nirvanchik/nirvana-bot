/**
 *  @(#)DangerEditException.java 6 сент. 2019 г.
 *  Copyright © 2019 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.error;

/**
 * Exception to throw when bot detects that the next edit may be dangerous (wrong edit).
 * Bot cancels this update and throws this exception instead.
 */
public class DangerousEditException extends Exception {

    private static final long serialVersionUID = 1L;
    private static final String DANGEROUS_EDIT_MSG_FMT =
            "Prevent edit that looks dangerous. Old page %s had %d items, " +
            "and bot wants to update it to %d items.";

    /**
     * Public constructor.
     *
     * @param was How many items was in old text.
     * @param wouldbe How many items bot generated for new text.
     */
    public DangerousEditException(String page, int was, int wouldbe) {
        super(String.format(DANGEROUS_EDIT_MSG_FMT, page, was, wouldbe));
    }

    /**
     * Public constructor.
     */
    public DangerousEditException(String arg) {
        super(arg);
    }

}
