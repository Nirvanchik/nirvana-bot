/**
 *  @(#)ServicePinger.java 12.03.2016
 *  Copyright © 2016 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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
 * @author kin
 *
 */
public class ServicePinger {
    protected static final Logger sLog;
    protected final Logger log;

	static final long RECHECK_DELAY_1 = 10L*60L*1000L;
	static final long RECHECK_DELAY_2 = 60L*60L*1000L;
	
	static final long RECHECK_1_TIMEOUT = 60L*60L*1000L;  // 1 hour
	static final long TIMEOUT = 10L*60L*60L*1000L;  // 10 hours
	
	static final String SERVICE_STATUS_STRING = "%1$-25s -> %2$s";
	private static final String SERVICE_STATUS_STRING_DETAILED = "%1$s: %2$s";
	
    private final List<OnlineService> services;
	private OnlineService lastFuckedService = null;
	
	private long timeout = TIMEOUT;

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

    public ServicePinger(OnlineService... servicesList) {
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

    public void addService(OnlineService service) {
        log.debug("Add service to ServicePinger: " + service.toString());
        services.add(service);
    }

    public void removeService(OnlineService service) {
        for (OnlineService s: services) {
            if (s == service) {
                services.remove(s);
                return;
            }
        }
    }

    public void setTimeout(long timeout) {
    	this.timeout = timeout;
    }

    public void tryRecoverReplacedServices() throws InterruptedException {
    	for (OnlineService service: services) {
   			service.recover();
    	}
    }

    public long tryToSolveProblems() throws InterruptedException, ServiceWaitTimeoutException,
            BotFatalError {
    	long start = currentTimeMillis();
   		tryToSolveProblemsByWaiting();
    	long end = currentTimeMillis();
    	return end - start;
    }

    private void tryToSolveProblemsByWaiting() throws InterruptedException,
            ServiceWaitTimeoutException, BotFatalError {
    	long firstFailTime = currentTimeMillis();
    	long currentDelay = RECHECK_DELAY_1;
    	boolean ok = false;
    	while (!ok) {
            long downtime = currentTimeMillis() - firstFailTime;
            if (downtime > RECHECK_1_TIMEOUT) {
    			currentDelay = RECHECK_DELAY_2;
    		}
            if (downtime > timeout) {
                if (timeout > 0 ) {
                    log.warn("Stop waiting after more then "+ timeout +" ms passed");
                }
    			throw new ServiceWaitTimeoutException(
    					String.format(Locale.ENGLISH,
    							"Required services are not available after waiting more than %d ms",
    							timeout));
    		}
    		log.info(String.format(Locale.ENGLISH, "Sleep for %d ms before next status check...", currentDelay));
    		sleep(currentDelay);    	
    		ok = isOk();    		
    	}
        if (afterDowntimeCallback != null) {
            afterDowntimeCallback.afterDowntime(currentTimeMillis() - firstFailTime);
        }
    }
    
	protected long currentTimeMillis() {
    	return System.currentTimeMillis();
    }
    
    protected void sleep(long millis) throws InterruptedException {
    	Thread.sleep(millis);
    }
    
    public static class ServiceWaitTimeoutException extends TimeoutException {

        private static final long serialVersionUID = -3303561839370965807L;

        public ServiceWaitTimeoutException(String message) {
	        super(message);
        }
    	
    }
    
    public boolean isOk() throws InterruptedException {
    	log.info("Checking available services...");
    	lastFuckedService = null;
    	for (OnlineService service: services) {
    		service.resetCache();
    	}
    	boolean online = true;
    	for (OnlineService service: services) {
    		OnlineService.Status status = service.isOk();
    		if (status != OnlineService.Status.OK) {
    			online = false;
    			if (lastFuckedService == null) {
    				lastFuckedService = service;
    			}
    		}
    		log.info(String.format(Locale.ENGLISH, SERVICE_STATUS_STRING, service.getName(), status));
    	}/*
    	if (online) {
    		lastFailTime = null;
    		currentDelay = RECHECK_DELAY_1;
    	}*/
    	if (!online) {
    		for (OnlineService service: services) {
    			logDetailedStatusIfFailed(service);
    		}
    	}
    	return online;
    }
    
    public OnlineService getLastFuckedService() {
    	return lastFuckedService;
    }
    
    static void logDetailedStatusIfFailed(OnlineService service) throws InterruptedException {
    	if (service.isOk() != Status.OK) {
            sLog.warn(String.format(Locale.ENGLISH, SERVICE_STATUS_STRING_DETAILED, service.getName(),
                    service.getLastError()));
    	}
    }
}
