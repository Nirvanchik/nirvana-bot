/**
 *  @(#)BotFatalError.java
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

package org.wikipedia.nirvana.base;

/**
 * Checked exception that any bot can raise when unrecoverable error happens and there is no other
 * ways except stopping bot execution immediately.
 * 
 * For using in {@link BasicBot#go} method.
 *
 */
public class BotFatalError extends Exception {

    private static final long serialVersionUID = -1859267728311575459L;

    /**
     * Constructs a new exception with the specified detail message. Thecause is not initialized,
     * and may subsequently be initialized bya call to Throwable.initCause(java.lang.Throwable).
     *
     * @param message the detail message. The detail message is saved forlater retrieval by the
     *     Throwable.getMessage() method.
     */
    public BotFatalError(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause and a detailmessage of 
     * (cause==null ? null : cause.toString()) (whichtypically contains the class and detail
     * message of cause). This constructor is useful for exceptions that are little more
     * than wrappers for other throwables. 
     *
     * @param cause  the cause (which is saved for later retrieval by the Throwable.getCause()
     *     method). (A null value ispermitted, and indicates that the cause is nonexistent or
     *     unknown.)
     */
    public BotFatalError(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail message andcause.
     * Note that the detail message associated with cause is not automatically incorporated inthis
     * exception's detail message.
     *
     * @param message the detail message (which is saved for later retrievalby the
     *     Throwable.getMessage() method).
     * @param cause the cause (which is saved for later retrieval by the Throwable.getCause()
     *     method). A null value ispermitted, and indicates that the cause is nonexistent orunknown.
     */
    public BotFatalError(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified detail message,cause, suppression enabled or
     * disabled, and writable stacktrace enabled or disabled.
     *
     * @param message the detail message
     * @param cause the cause. (A null value is permitted,and indicates that the cause is
     *     nonexistent or unknown.)
     * @param enableSuppression whether or not suppression is enabled or disabled
     * @param writableStackTrace whether or not the stack trace should be writableSince:
     */
    public BotFatalError(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
