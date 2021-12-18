/**
 *  @(#)ServiceError.java 
 *  Copyright © 2014 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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
 * Exception that raised when some third party service returns unexpected output that looks like 
 * the service made a mistake. Instead of using corrupt data we fail current update procedure,
 * and retry it is allowed by bot logic.
 *
 */
public class ServiceError extends Exception {

    /**
     * Serial ID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public ServiceError() {
    }

    /**
     * Constructor with text message.
     *
     * @param message the detail message
     */
    public ServiceError(String message) {
        super(message);
    }

    /**
     * Constructor with exception.
     *
     * @param cause exception, that caused this error
     */
    public ServiceError(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with text message and exception.
     *
     * @param message the detail message
     * @param cause exception, that caused this error
     */
    public ServiceError(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified detail message,cause, suppression enabled or
     * disabled, and writable stacktrace enabled or disabled.
     *
     * @param message the detail message
     * @param cause exception, that caused this error
     * @param enableSuppression whether or not suppression is enabledor disabled
     * @param writableStackTrace whether or not the stack trace shouldbe writable
     */
    public ServiceError(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);        
    }
}
