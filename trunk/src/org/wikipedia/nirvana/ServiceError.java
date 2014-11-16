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
 * Recommended code page for this file is CP1251 (also called Windows-1251).
 * */

package org.wikipedia.nirvana;

/**
 * @author kin
 *
 */
public class ServiceError extends Exception {

	/**
     * Serial ID 
     */
    private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 */
	public ServiceError() {
	}

	/**
	 * Constructor with text message
	 * @param message text message
	 */
	public ServiceError(String message) {
		super(message);
	}

	/**
	 * Constructor with exception
	 * @param cause exception, that caused this error
	 */
	public ServiceError(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with text message and exception
	 * @param message
	 * @param cause
	 */
	public ServiceError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Customized constructor 
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public ServiceError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);		
	}

}
