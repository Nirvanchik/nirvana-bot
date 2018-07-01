/**
 *  @(#)CatscanService.java 12.03.2016
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

import org.wikipedia.nirvana.WikiTools;
import org.wikipedia.nirvana.WikiTools.ServiceFeatures;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * @author kin
 *
 */
public class CatscanService extends InternetService {
	WikiTools.Service service;

	/**
	 * @param priority
	 */
	public CatscanService(WikiTools.Service service) {
		this(service.getName(), service);
	}

	/**
	 * @param name
	 * @param priority
	 */
	public CatscanService(String name, WikiTools.Service service) {
		super(name);
		this.service = service;
	}
	
	public WikiTools.Service getService() {
		return service;
	}
	
	@Override
	protected boolean checkAvailable() {
		URL url;
        try {
	        url = new URL(WikiTools.HTTP + "://" + service.DOMAIN + service.PATH);
        } catch (MalformedURLException e) {
	        throw new RuntimeException(e);
        }
		return checkConnection(url);
	}
	
	@Override
    protected boolean checkWorking() throws InterruptedException {	    
        if (service.supportsFeature(ServiceFeatures.PAGES)) {
			try {
				Date date = new Date();
				log.debug("time: "+date.toString());
				WikiTools.setFastMode(true);
	            String result = WikiTools.loadPagesForCatWithService(service, "HTML", "ru", 1, 0);
	            date = new Date();
	            log.debug("time: "+date.toString());
	            if (result == null || result.isEmpty()) {
	            	setLastError("loadPagesForCatWithService() returned empty result");
	            	return false;
	            }
	            if (!result.contains("HTML5")) {
	            	setLastError("loadPagesForCatWithService() returned corrupted result ");
	            	return false;
	            }
            } catch (IOException e) {
            	setLastError("Exception when calling loadPagesForCatWithService(): " + e.toString());
	            return false;
            } finally {
            	WikiTools.setFastMode(false);
            }
        } 
        if (service.supportsFeature(ServiceFeatures.NEWPAGES)) {
				try {
					//System.currentTimeMillis();
					Date date = new Date();
					log.debug("time: "+date.toString());
					WikiTools.setFastMode(true);
		            String result = WikiTools.loadNewPagesForCatWithService(service, "HTML", "ru", 0, 50*12*31*24, 0);
		            date = new Date();
		            log.debug("time: "+date.toString());
		            if (result == null || result.isEmpty()) {
		            	setLastError("loadNewPagesForCatWithService() returned empty result");
		            	return false;
		            }
		            if (!result.contains("HTML5")) {
		            	setLastError("loadNewPagesForCatWithService() returned corrupted result ");
		            	return false;
		            }
	            } catch (IOException | InterruptedException e) {
	            	setLastError("Exception when calling loadNewPagesForCatWithService(): " + e.toString());
		            return false;
	            } finally {
	            	WikiTools.setFastMode(false);
	            }
		} /*else {
			setLastError("This is an unexpected service which doesn't support required things");
			return false;
		}*/
		return true;
    }
}
