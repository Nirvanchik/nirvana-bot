/**
 *  @(#)ServiceGroup.java 19.03.2016
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
 * Recommended code page for this file is CP1251 (also called Windows-1251).
 * */

package org.wikipedia.nirvana.nirvanabot.serviceping;

import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

public class ServiceGroup<T extends BasicService> extends BasicService {

	private static final long RECHECK_DELAY_1 = 10L*60L*1000L;
	private static final long RECHECK_DELAY_2 = 60L*60L*1000L;
	private static final long RECHECK_1_TIMEOUT = 60L*60L*1000L;
	private long currentDelay = RECHECK_DELAY_1;
	private long firstFailTime = 0;
	private long lastFailTime = 0;
	
	final T services[];
	final T defaultService;

	private T activeService;
	
	private Listener<T> listener = null;
	
	public interface Listener<T extends BasicService> {
		void onActiveServiceChanged(T activeService);
	}
	
    @SafeVarargs
    public ServiceGroup(String name, T defaultChecker, T... allCheckers) {
        super(name);
        services = Arrays.copyOf(allCheckers, allCheckers.length);
        activeService = defaultService = defaultChecker;
    }
    
    @SafeVarargs
    public ServiceGroup(T defaultChecker, T... allCheckers) {
        this("group of services: " + StringUtils.join(allCheckers, ", ") + ".",
        		defaultChecker,
        		allCheckers);
    }
    
    public void setListener(Listener<T> l) {
    	this.listener = l;
    }
    
    public void notifyActiveServiceChanged() {
    	if (listener != null) {
    		listener.onActiveServiceChanged(activeService);
    	}
    }
	
	public T getActiveService() {
		return activeService;
	}
	
	@Override
	public String getName() {
		return super.getName();
	}
	
	@Override
	protected boolean checkOk() throws InterruptedException {
		if (activeService.isOk() == Status.OK) {
			return true;
		}
		for (T service: services) {
			Status status = service.isOk();
			log.info(String.format(Locale.ENGLISH, ServicePinger.SERVICE_STATUS_STRING, service.getName(), status));
            ServicePinger.logDetailedStatusIfFailed(service);
			if (status == Status.OK) {
				if (activeService != service) {
					log.info(String.format("%1$s is replaced by %2$s",
							activeService.getName(),
							service.getName()));
					activeService = service;
		    		firstFailTime = System.currentTimeMillis();
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
	public boolean isReplacable() {
		return true;
	}

	@Override
    public void resetCache() {
	    super.resetCache();
	    for(T service:services) {
	    	service.resetCache();
	    }
	    //activeService = null; Here we should use recheck delay or use it in
    }
	
	/* (non-Javadoc)
     * @see org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService#recover()
     */
    @Override
    public void recover() throws InterruptedException {
    	if (activeService == defaultService) {
    		return;
    	}
    	assert firstFailTime != 0;
    	assert lastFailTime != 0;
    	long time = System.currentTimeMillis();
    	if (time - firstFailTime > RECHECK_1_TIMEOUT) {
    		currentDelay = RECHECK_DELAY_2;
    	}
    	if (time - lastFailTime < currentDelay) {
    		return;
    	}
    	defaultService.resetCache();
    	log.info(String.format("Checking %1$s in order to switch back to it...", defaultService.getName()));
    	Status status = defaultService.isOk();
    	log.info(String.format(Locale.ENGLISH, ServicePinger.SERVICE_STATUS_STRING, defaultService.getName(), status));
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