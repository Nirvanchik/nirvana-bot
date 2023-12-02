/**
 *  @(#)ServicePinger.java
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

import org.wikipedia.nirvana.nirvanabot.BotFatalError;
import org.wikipedia.nirvana.nirvanabot.SystemTime;
import org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;

/**
 * The main class of this package.
 * Holds all services instances implementing {@link OnlineService} interface.
 * Helps in managing with services availability.
 *
 * Provides methods that allow to:
 * 1) check if all services are OK;
 * 2) wait when services come alive;
 * 3) try recover some services that allow this.
 *
 */
public class ServicePinger {
    protected static final Logger sLog;
    protected final Logger log;

    static final long RECHECK_DELAY_1 = 10 * 60 * 1000L;
    static final long RECHECK_DELAY_2 = 60 * 60 * 1000L;
    
    static final long RECHECK_1_TIMEOUT = 60 * 60 * 1000L;  // 1 hour
    static final long TIMEOUT = 10 * 60 * 60 * 1000L;  // 10 hours
    
    static final String SERVICE_STATUS_STRING = "%1$-25s -> %2$s";
    private static final String SERVICE_STATUS_STRING_DETAILED = "%1$s: %2$s";
    private static final String TIMEOUT_MSG =
            "Required services are not available after waiting more than %d ms";
    
    private final List<OnlineService> services;
    private OnlineService lastFailedService = null;
    
    private long timeout = TIMEOUT;
    private final SystemTime systemTime;

    @Nullable
    private AfterDowntimeCallback afterDowntimeCallback;

    static {
        sLog = LogManager.getLogger(ServicePinger.class.getName());
    }

    /**
     * Callback interface.
     * External user can implement it and add some action to call
     * when services come back online after been down.
     */
    public interface AfterDowntimeCallback {
        /**
         * Called when services come back online.
         * @param downtime down time in milliseconds.
         */
        void afterDowntime(long downtime) throws BotFatalError;
    }

    /**
     * Constructs service pinger with provided list of services objects.
     */
    public ServicePinger(SystemTime systemTime, OnlineService... servicesList) {
        this.systemTime = systemTime;
        services = new ArrayList<>(Arrays.asList(servicesList));
        log = LogManager.getLogger(getClass().getName());
        log.debug("Build ServicePinger of services: " + StringUtils.join(services, ", "));
    }

    /**
     * Sets callback action that will be called after downtime when services
     * come online.
     *
     * @param callback Callback instance.
     */
    public void setAfterDowntimeCallback(AfterDowntimeCallback callback) {
        afterDowntimeCallback = callback;
    }

    /**
     * Add service to monitor.
     */
    public void addService(OnlineService service) {
        log.debug("Add service to ServicePinger: " + service.toString());
        services.add(service);
    }

    /**
     * Remove service from monitoring.
     */
    public void removeService(OnlineService service) {
        services.remove(service);
    }

    /**
     * Set timeout value in milliseconds.
     * This is the max time to wait by {@link #waitServicesOk()} method
     * when services come back online.
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Try to recover broken services.
     * Call this method in any free moments when services are not used.
     */
    public void tryRecoverServices() throws InterruptedException {
        for (OnlineService service: services) {
            service.recover();
        }
    }

    /**
     * Wait when services come back online.
     *
     * @return time in milliseconds that we have waited.
     */
    public long waitServicesOk() throws InterruptedException, ServiceWaitTimeoutException,
            BotFatalError {
        long start = currentTimeMillis();
        long firstFailTime = start;
        long currentDelay = RECHECK_DELAY_1;
        boolean ok = false;
        while (!ok) {
            long downtime = currentTimeMillis() - firstFailTime;
            if (downtime > RECHECK_1_TIMEOUT) {
                currentDelay = RECHECK_DELAY_2;
            }
            if (downtime > timeout) {
                if (timeout > 0 ) {
                    log.warn("Stop waiting after more then " + timeout + " ms passed");
                }
                throw new ServiceWaitTimeoutException(
                        String.format(Locale.ENGLISH, TIMEOUT_MSG, timeout));
            }
            log.info(String.format(Locale.ENGLISH, "Sleep for %d ms before next status check...",
                    currentDelay));
            sleep(currentDelay);
            ok = isOk();
        }
        if (afterDowntimeCallback != null) {
            afterDowntimeCallback.afterDowntime(currentTimeMillis() - firstFailTime);
        }
        return currentTimeMillis() - start;
    }
    
    protected long currentTimeMillis() {
        return systemTime.currentTimeMillis();
    }
    
    protected void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }
    
    /**
     * Exception that is raised if allowed time passed and services didn't come online.
     *
     */
    public static class ServiceWaitTimeoutException extends TimeoutException {

        private static final long serialVersionUID = -3303561839370965807L;

        public ServiceWaitTimeoutException(String message) {
            super(message);
        }
        
    }

    /**
     * Check if all services are online.
     */
    public boolean isOk() throws InterruptedException {
        log.info("Checking available services...");
        lastFailedService = null;
        for (OnlineService service: services) {
            service.resetCache();
        }
        boolean online = true;
        for (OnlineService service: services) {
            OnlineService.Status status = service.isOk();
            if (status != OnlineService.Status.OK) {
                online = false;
                if (lastFailedService == null) {
                    lastFailedService = service;
                }
            }
            log.info(String.format(Locale.ENGLISH, SERVICE_STATUS_STRING, service.getName(),
                    status));
        }
        if (!online) {
            for (OnlineService service: services) {
                logDetailedStatusIfFailed(service);
            }
        }
        return online;
    }

    /**
     * Get service that was last failing.
     */
    public OnlineService getLastFailedService() {
        return lastFailedService;
    }
    
    static void logDetailedStatusIfFailed(OnlineService service) throws InterruptedException {
        if (service.isOk() != Status.OK) {
            sLog.warn(String.format(Locale.ENGLISH, SERVICE_STATUS_STRING_DETAILED,
                    service.getName(), service.getLastError()));
        }
    }
}
