/**
 *  @(#)ServiceManager.java
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

package org.wikipedia.nirvana.nirvanabot;

import static org.wikipedia.nirvana.nirvanabot.NirvanaBot.SERVICE_AUTO;

import org.wikipedia.nirvana.annotation.VisibleForTesting;
import org.wikipedia.nirvana.nirvanabot.serviceping.CatscanService;
import org.wikipedia.nirvana.nirvanabot.serviceping.InternetService;
import org.wikipedia.nirvana.nirvanabot.serviceping.NetworkInterface;
import org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService;
import org.wikipedia.nirvana.nirvanabot.serviceping.ServiceGroup;
import org.wikipedia.nirvana.nirvanabot.serviceping.ServiceGroup.Listener;
import org.wikipedia.nirvana.nirvanabot.serviceping.ServicePinger;
import org.wikipedia.nirvana.nirvanabot.serviceping.ServicePinger.AfterDowntimeCallback;
import org.wikipedia.nirvana.nirvanabot.serviceping.ServicePinger.ServiceWaitTimeoutException;
import org.wikipedia.nirvana.nirvanabot.serviceping.WikiService;
import org.wikipedia.nirvana.wiki.CatScanTools;
import org.wikipedia.nirvana.wiki.CatScanTools.Service;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * @author kin
 *
 */
public class ServiceManager {
    protected static final Logger log;

	private final ServicePinger servicePinger;
    @Nullable
    private ServiceGroup<CatscanService> pageListFetchServiceGroup = null;
    protected CatScanTools.Service activeService = null;
    private WikiService mainWiki = null;
    private InternetService internet = null;
    protected OnlineService catscan = null;

    static {
        log = LogManager.getLogger(ServiceManager.class.getName());
    }

	public ServiceManager(String defaultServiceName, String selectedServiceName, NirvanaWiki wiki, NirvanaWiki commons) throws BotFatalError {
        this(wiki, commons);
        updateCatScan(defaultServiceName, selectedServiceName);
    }

    public ServiceManager(NirvanaWiki wiki, NirvanaWiki commons) {
		NetworkInterface serviceNetwork = new NetworkInterface();
		InternetService serviceInternet = new InternetService();
		serviceInternet.dependsOn(serviceNetwork);
        internet = serviceInternet; 
		WikiService serviceWiki = new WikiService(wiki);
        mainWiki = serviceWiki;
		serviceWiki.dependsOn(serviceInternet);
		WikiService serviceCommons = new WikiService(commons);
		serviceCommons.dependsOn(serviceInternet);
        servicePinger = new ServicePinger(
                 new SystemTime(),
                 serviceNetwork,
                 serviceInternet,
                 serviceWiki,
                 serviceCommons);
    }

    @VisibleForTesting
    protected ServiceManager(InternetService internet, WikiService mainWiki,
            ServicePinger servicePinger) {
        this.internet = internet; 
        this.mainWiki = mainWiki;
        this.servicePinger = servicePinger;
    }

    public void updateCatScan(String defaultServiceName, String selectedServiceName) throws BotFatalError {
        CatScanTools.Service defaultService =
                CatScanTools.Service.getServiceByName(defaultServiceName);
        assert defaultService != null;

        if (selectedServiceName.equalsIgnoreCase(SERVICE_AUTO)) {
            activeService = defaultService;
        } else {
            activeService =
                    CatScanTools.Service.getServiceByName(selectedServiceName, defaultService);
        }
        log.info("Selected service is: " + activeService.getName());

        Map<Service, CatscanService> servicesMap = new HashMap<>();
        ArrayList<CatscanService> services = new ArrayList<>();
        for (Service s: Service.values()) {
            if (!s.down) {
                CatscanService service = new CatscanService(s);
                service.dependsOn(internet);
                servicesMap.put(s, service);
                services.add(service);
            }
        }
        CatscanService serviceCatscanDefault = servicesMap.get(activeService);
        if (serviceCatscanDefault == null) {
            log.error("We have a problem, Watson! HOUHOU!");
            throw new BotFatalError("Unexpected state: we can't find this service");
        }

        if (catscan != null) {
            servicePinger.removeService(catscan);
        }
		if (selectedServiceName.equalsIgnoreCase(SERVICE_AUTO)) {
            log.info("Create service group");

			pageListFetchServiceGroup =
                    new ServiceGroup<CatscanService>(
                            new SystemTime(),
                            services.toArray(new CatscanService[0]))
                    .setDefault(serviceCatscanDefault);

			pageListFetchServiceGroup.setListener(new Listener<CatscanService>() {

				@Override
                public void onActiveServiceChanged(CatscanService activeService) {
					ServiceManager.this.activeService = activeService.getService();	                
                    log.info("Active service changed to: " + ServiceManager.this.activeService.name());
                }
			});
            catscan = pageListFetchServiceGroup;
		} else {
			pageListFetchServiceGroup = null;
            catscan = serviceCatscanDefault;
		}
        servicePinger.addService(catscan);
    }

	public void setTimeout(long timeout) {
		servicePinger.setTimeout(timeout);
	}

    /**
     * Sets callback action that will be called after downtime when services
     * come online.
     *
     * @param callback Callback instance.
     */
	public void setAfterDowntimeCallback(AfterDowntimeCallback callback) {
		servicePinger.setAfterDowntimeCallback(callback);
	}

    public CatScanTools.Service getActiveService() {
		return activeService;
	}

    public WikiService getMainWikiService() {
        return mainWiki;
    }

	public void timeToFixProblems() throws InterruptedException {
		if (pageListFetchServiceGroup != null) {
            servicePinger.tryRecoverServices();
			activeService = pageListFetchServiceGroup.getActiveService().getService();
		}
	}

    public boolean checkServices() throws InterruptedException, BotFatalError {
		try {
	        if (!servicePinger.isOk()) {
                servicePinger.waitServicesOk();
	        }
        } catch (ServiceWaitTimeoutException e) {
	        log.error(e);
            mainWiki.setNeedsRelogin(true);
	        return false;
        }
		return true;
	}
}
