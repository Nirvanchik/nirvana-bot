/**
 *  @(#)ServiceManager.java 01.04.2016
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

package org.wikipedia.nirvana.nirvanabot;

import static org.wikipedia.nirvana.nirvanabot.NirvanaBot.SERVICE_AUTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.WikiTools;
import org.wikipedia.nirvana.WikiTools.Service;
import org.wikipedia.nirvana.nirvanabot.serviceping.CatscanService;
import org.wikipedia.nirvana.nirvanabot.serviceping.InternetService;
import org.wikipedia.nirvana.nirvanabot.serviceping.NetworkInterface;
import org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService;
import org.wikipedia.nirvana.nirvanabot.serviceping.ServiceGroup;
import org.wikipedia.nirvana.nirvanabot.serviceping.ServiceGroup.Listener;
import org.wikipedia.nirvana.nirvanabot.serviceping.ServicePinger;
import org.wikipedia.nirvana.nirvanabot.serviceping.ServicePinger.ServiceWaitTimeoutException;
import org.wikipedia.nirvana.nirvanabot.serviceping.WikiService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author kin
 *
 */
public class ServiceManager {
    protected static final Logger log;

	private final ServicePinger servicePinger;
    private ServiceGroup<CatscanService> pageListFetchServiceGroup = null;
	private WikiTools.Service activeService = null;
    private WikiService mainWiki = null;
    private InternetService internet = null;
    private OnlineService catscan = null;

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
                 serviceNetwork,
                 serviceInternet,
                 serviceWiki,
                 serviceCommons);
    }

    public void updateCatScan(String defaultServiceName, String selectedServiceName) throws BotFatalError {
        WikiTools.Service defaultService = WikiTools.Service.getServiceByName(defaultServiceName);
        assert defaultService != null;

        if (selectedServiceName.equalsIgnoreCase(SERVICE_AUTO)) {
            activeService = defaultService;
        } else {
            activeService = WikiTools.Service.getServiceByName(selectedServiceName, defaultService);
        }
        log.info("Selected service is: " + activeService.getName());

        Map<Service, CatscanService> servicesMap = new HashMap<>();
        ArrayList<CatscanService> services = new ArrayList<>();
        for (Service s: Service.values()) {
            if (!s.DOWN) {
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
                            serviceCatscanDefault, services.toArray(new CatscanService[0]));

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

	public WikiTools.Service getActiveService() {
		return activeService;
	}

    public WikiService getMainWikiService() {
        return mainWiki;
    }

	public void timeToFixProblems() throws InterruptedException {
		if (pageListFetchServiceGroup != null) {
			servicePinger.tryRecoverReplacedServices();
			activeService = pageListFetchServiceGroup.getActiveService().getService();
		}
	}
	
	public boolean checkServices() throws InterruptedException {
		try {
	        if (!servicePinger.isOk()) {
	        	servicePinger.tryToSolveProblems();
	        }
        } catch (ServiceWaitTimeoutException e) {
	        log.error(e);
	        return false;
        }
		return true;
	}
}
