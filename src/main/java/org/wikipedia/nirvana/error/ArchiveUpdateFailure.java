/**
 *  @(#)ArchiveUpdateFailure.java
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

package org.wikipedia.nirvana.error;

/**
 * Exception that raised when archive update fails.
 * This failure should be handled separately, "retry logic" is disabled for it and
 * this wrapper exception used to catch this fail and skip retrying task.
 *
 */
public class ArchiveUpdateFailure extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor with Throwable cause argument.
     */
    public ArchiveUpdateFailure(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with Throwable cause and String message arguments.
     */
    public ArchiveUpdateFailure(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified detail message, cause,
     * suppression enabled or disabled, and writable stack trace enabled or disabled.
     *
     * @param message the detail message.
     * @param cause the cause (which is saved for later retrieval by the getCause() method). 
     * @param enableSuppression whether or not suppression is enabled or disabled.
     * @param writableStackTrace whether or not the stack trace should be writable.
     */
    public ArchiveUpdateFailure(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
