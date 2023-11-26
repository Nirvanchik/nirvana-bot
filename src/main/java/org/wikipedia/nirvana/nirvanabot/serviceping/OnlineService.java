/**
 *  @(#)OnlineService.java
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

package org.wikipedia.nirvana.nirvanabot.serviceping;

/**
 * Basic interface that represents any online service.
 * "Online" means that service is available via network (Internet) connection
 * and is third-party, or network/internet connection itself.
 * This abstraction is used to deal with services that can be broken when bot starts
 * or while bot is working.
 */
public interface OnlineService {
    /**
     * Availability status of service
     * (if service is available and working).
     */
    enum Status {
        OK,
        FAIL,
        UNKNOWN
    }

    /**
     * @return human readable service name. Used for logging and ui.
     */
    String getName();

    /**
     * Check service availability and return its status.
     * This status may be cached to speed up subsequent calls.
     *
     * @return service availability status
     */
    Status isOk() throws InterruptedException;

    /**
     * Try recover this service if possible.
     * This method can make some actions to make service working again
     * (may be use alternative domain name or anything else).
     */
    void recover() throws InterruptedException;

    /**
     * Drop any cached state of the service.
     */
    void resetCache();

    /**
     * Return error that happened when service availability was last checked
     * (for use when status was FAIL or UNKNOWN). 
     */
    String getLastError();
}
