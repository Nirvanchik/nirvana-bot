/**
 *  @(#)ServiceManager.java 20.03.2016
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

import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.WikiTools;
import org.wikipedia.nirvana.WikiTools.Service;
import org.wikipedia.nirvana.nirvanabot.serviceping.CatscanService;
import org.wikipedia.nirvana.nirvanabot.serviceping.InternetService;
import org.wikipedia.nirvana.nirvanabot.serviceping.NetworkInterface;
import org.wikipedia.nirvana.nirvanabot.serviceping.ServiceGroup;
import org.wikipedia.nirvana.nirvanabot.serviceping.ServiceGroup.Listener;
import org.wikipedia.nirvana.nirvanabot.serviceping.ServicePinger;
import org.wikipedia.nirvana.nirvanabot.serviceping.WikiService;
import org.wikipedia.nirvana.nirvanabot.serviceping.ServicePinger.ServiceWaitTimeoutException;

/**
 * @author kin
 *
 */
public class ServiceManager {
	protected static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ServiceManager.class.getName());
	private final ServicePinger servicePinger;
	private final ServiceGroup<CatscanService> pageListFetchServiceGroup;
	private WikiTools.Service activeService = null;
	
	public ServiceManager(String defaultServiceName, String selectedServiceName, NirvanaWiki wiki, NirvanaWiki commons) throws BotFatalError {
		WikiTools.Service defaultService = WikiTools.Service.CATSCAN2;
		defaultService = WikiTools.Service.getServiceByName(defaultServiceName, defaultService);

		if (selectedServiceName.equalsIgnoreCase(SERVICE_AUTO)) {
			activeService = defaultService;
		} else {
			activeService = WikiTools.Service.getServiceByName(selectedServiceName, defaultService);
		}
		
		NetworkInterface serviceNetwork = new NetworkInterface();
		InternetService serviceInternet = new InternetService();
		serviceInternet.dependsOn(serviceNetwork);
		WikiService serviceWiki = new WikiService(wiki);
		serviceWiki.dependsOn(serviceInternet);
		WikiService serviceCommons = new WikiService(commons);
		serviceCommons.dependsOn(serviceInternet);
		CatscanService serviceCatscan2 = new CatscanService(Service.CATSCAN2);
		serviceCatscan2.dependsOn(serviceInternet);		
		CatscanService serviceCatscan3 = new CatscanService(Service.CATSCAN3);
		serviceCatscan3.dependsOn(serviceInternet);
		CatscanService serviceCatscanDefault = serviceCatscan2; 
		switch (defaultService) {
			case CATSCAN2: serviceCatscanDefault = serviceCatscan2; break;
			case CATSCAN3: serviceCatscanDefault = serviceCatscan3; break;
			default:
				log.fatal("Unexpected default service: "+defaultService);
				throw new BotFatalError("Unexpected default service: "+defaultService);
		}
		
		if (selectedServiceName.equalsIgnoreCase(SERVICE_AUTO)) {
			pageListFetchServiceGroup =
					new ServiceGroup<CatscanService>(
							serviceCatscanDefault, serviceCatscan2, serviceCatscan3);
			
			pageListFetchServiceGroup.setListener(new Listener<CatscanService>() {

				@Override
                public void onActiveServiceChanged(CatscanService activeService) {
					ServiceManager.this.activeService = activeService.getService();	                
                }
			});
		
			servicePinger = new ServicePinger(
		 			serviceNetwork, 
		 			serviceInternet,
		 			serviceWiki,
		 			serviceCommons,
		 			pageListFetchServiceGroup);
		} else {
			pageListFetchServiceGroup = null;
			servicePinger = new ServicePinger(
		 			serviceNetwork, 
		 			serviceInternet,
		 			serviceWiki,
		 			serviceCommons,
		 			serviceCatscanDefault);
		}
	}
	
	
	public void setTimeout(long timeout) {
		servicePinger.setTimeout(timeout);
	}
	
	public WikiTools.Service getActiveService() {
		return activeService;
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
