/**
 *  @(#)BadFormatException.java
 *  Copyright © 2023 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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
 * Exception that can be raised by simple parser utilities when parsing some user-modified settings
 * when that settings have unexpected format. Bot settings parser cannot recognize that setting
 * string and raises this error. The calling bot code must decide how to handle this error (ignore, 
 * abort execution, notify user, etc).
 *
 */
public class BadFormatException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs exception with empty message.
     */
    public BadFormatException() {
    }

    /**
     * Constructs a new exception with the specified detail message.
     */
    public BadFormatException(String message) {
        super(message);
    }

}
