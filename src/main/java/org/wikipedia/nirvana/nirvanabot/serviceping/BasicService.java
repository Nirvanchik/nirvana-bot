/**
 *  @(#)BasicService.java
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 * Basic implementation of online service.
 *
 */
public abstract class BasicService implements OnlineService {
    protected final Logger log;

    BasicService dependantOn = null;
    private String name;
    private Status cachedStatus = null;
    private String lastError = null;

    /**
     * Constructs instance of service with specified name.
     */
    public BasicService(String name) {
        this.name = name;
        log = LogManager.getLogger(getClass().getName());
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return getName();
    }
    
    /**
     * Check service availability.
     * Must always do a real check, not using caching. 
     */
    protected abstract boolean checkOk() throws InterruptedException;
    
    private boolean checkParentIsOk() throws InterruptedException {
        return (dependantOn == null || dependantOn.isOk() == Status.OK);
    }

    /**
     * Set dependency of this service on another service.
     * This will optimize isOk() detection logic.
     */
    public BasicService dependsOn(BasicService service) {
        dependantOn = service;
        return this;
    }

    @Override
    public void resetCache() {
        cachedStatus = null;
        lastError = null;
    }

    @Override
    public void recover() throws InterruptedException {
        // do nothing if service is not recoverable
    }
    
    @Override
    public Status isOk() throws InterruptedException {
        if (cachedStatus != null) {
            return cachedStatus;
        }
        if (!checkParentIsOk()) {
            cachedStatus = Status.UNKNOWN;
            return cachedStatus;
        }
        if (checkOk()) {
            cachedStatus = Status.OK;
        } else {
            cachedStatus = Status.FAIL;
        }
        return cachedStatus;
    }

    @Override
    public String getLastError() {
        if (lastError == null) {
            return "";
        }
        return lastError;
    }

    /**
     * Set last error that happened during checking availability.
     */
    void setLastError(String error) {
        lastError = error;
    }
}