/**
 *  @(#)BotSettingsError.java
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

package org.wikipedia.nirvana.nirvanabot;

/**
 * Error to throw when found error in settings.
 *
 */
public class BotSettingsError extends BotFatalError {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public BotSettingsError() {
    }

    /**
     * Constructs exception object with custom message.
     */
    public BotSettingsError(String message) {
        super(message);
    }

    /**
     * Constructs exception object with stack trace object (Throwable).
     */
    public BotSettingsError(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs exception object with custom message and stack trace object.
     */
    public BotSettingsError(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * One more complicated constructor I don't know what for.
     */
    public BotSettingsError(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
