/**
 *  @(#)ServiceGroup.java
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

import org.wikipedia.nirvana.nirvanabot.SystemTime;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Locale;

/**
 * Template class that can be used as a facade delegating execution to one of many services.
 * If the main service in the group is broken, another one will be selected as active one.
 * This turns N unstable services into 1 stable.
 */
public class ServiceGroup<T extends BasicService> extends BasicService {

    private static final long RECHECK_DELAY_1 = 10 * 60 * 1000L;
    private static final long RECHECK_DELAY_2 = 60 * 60 * 1000L;
    private static final long RECHECK_1_TIMEOUT = 60 * 60 * 1000L;
    private long currentDelay = RECHECK_DELAY_1;
    private long firstFailTime = 0;
    private long lastFailTime = 0;

    final T[] services;
    private T defaultService;

    private T activeService;

    private Listener<T> listener = null;
    private SystemTime systemTime;

    /**
     * Listener interface for important changes in Service Group.
     */
    public interface Listener<T extends BasicService> {
        /**
         * Called when active service is switched.
         */
        void onActiveServiceChanged(T activeService);
    }

    /**
     * Constructs service group with specific name, main service and
     * varied number of additional services.
     */
    @SafeVarargs
    public ServiceGroup(String name, SystemTime systemTime, T... allCheckers) {
        super(name);
        services = Arrays.copyOf(allCheckers, allCheckers.length);
        activeService = defaultService = services[0];
        this.systemTime = systemTime;
    }

    /**
     * Constructs service group with main service and
     * varied number of additional services.
     */
    @SafeVarargs
    public ServiceGroup(SystemTime systemTime, T... allCheckers) {
        this("group of services: " + StringUtils.join(allCheckers, ", ") + ".",
                systemTime,
                allCheckers);
    }
    
    public ServiceGroup<T> setDefault(T defaultService) {
        this.defaultService = defaultService;
        return this;
    }

    /**
     * Set listener of events of this service group.
     */
    public void setListener(Listener<T> listener) {
        this.listener = listener;
    }

    protected void notifyActiveServiceChanged() {
        if (listener != null) {
            listener.onActiveServiceChanged(activeService);
        }
    }

    /**
     * Get service which is currently active.
     */
    public T getActiveService() {
        return activeService;
    }

    @Override
    protected boolean checkOk() throws InterruptedException {
        if (activeService.isOk() == Status.OK) {
            return true;
        }
        for (T service: services) {
            Status status = service.isOk();
            log.info(String.format(Locale.ENGLISH, ServicePinger.SERVICE_STATUS_STRING,
                    service.getName(), status));
            ServicePinger.logDetailedStatusIfFailed(service);
            if (status == Status.OK) {
                if (activeService != service) {
                    log.info(String.format("%1$s is replaced by %2$s",
                            activeService.getName(),
                            service.getName()));
                    activeService = service;
                    firstFailTime = systemTime.currentTimeMillis();
                    lastFailTime = firstFailTime;
                    currentDelay = RECHECK_DELAY_1;
                    notifyActiveServiceChanged();
                }
                return true;
            }
        }
        if (activeService != defaultService) {
            log.info(String.format("Set default service back: %1$s ", defaultService.getName()));
            activeService = defaultService;
            notifyActiveServiceChanged();
        }
        return false;
    }

    @Override
    public void resetCache() {
        super.resetCache();
        for (T service: services) {
            service.resetCache();
        }
    }

    @Override
    public void recover() throws InterruptedException {
        if (activeService == defaultService) {
            return;
        }
        assert firstFailTime != 0;
        assert lastFailTime != 0;
        long time = systemTime.currentTimeMillis();
        if (time - firstFailTime > RECHECK_1_TIMEOUT) {
            currentDelay = RECHECK_DELAY_2;
        }
        if (time - lastFailTime < currentDelay) {
            return;
        }
        defaultService.resetCache();
        log.info(String.format("Checking %1$s in order to switch back to it...",
                defaultService.getName()));
        Status status = defaultService.isOk();
        log.info(String.format(Locale.ENGLISH, ServicePinger.SERVICE_STATUS_STRING,
                defaultService.getName(), status));
        ServicePinger.logDetailedStatusIfFailed(defaultService);
        if (status == Status.OK) {
            activeService = defaultService;
            log.info(String.format("Switched back to %1$s successfully", defaultService.getName()));
            notifyActiveServiceChanged();
            return;
        }
        lastFailTime = time;
    }

    @Override
    public String getLastError() {
        return activeService.getLastError();
    }
}