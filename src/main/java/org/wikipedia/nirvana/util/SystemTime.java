/**
 *  @(#)TimeManager.java
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

package org.wikipedia.nirvana.util;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;

/**
 * Mediator object for working with system time.
 * Useful for moking date/time (Calendar) objects in tests. 
 *
 */
public class SystemTime {

    /**
     * Default constructor. 
     */
    public SystemTime() {
        // do nothing
    }

    /**
     * Get current time.
     */
    @Deprecated
    public Calendar now() {
        return Calendar.getInstance();
    }

    /**
     * Returns current time if {@link OffsetDateTime} format.
     */
    public OffsetDateTime nowOdt() {
        return OffsetDateTime.now(); 
    }

    /**
     * Returns the current time in milliseconds.
     */
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Causes the currently executing thread to sleep (temporarily cease execution)
     * for the specified number of milliseconds.
     */
    public void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }
}
