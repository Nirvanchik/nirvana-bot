/**
 *  @(#)WikiService.java 12.03.2016
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.wikipedia.nirvana.NirvanaWiki;

/**
 * @author kin
 *
 */
public class WikiService extends InternetService {
	NirvanaWiki wiki;

	/**
	 * @param name
	 * @param priority
	 */
	public WikiService(NirvanaWiki wiki) {
		this(wiki.getDomain(), wiki);
	}
	
	public WikiService(String name, NirvanaWiki wiki) {
		super(name);
		this.wiki = wiki;
	}
	
	@Override
	protected boolean checkAvailable() {
		URL url;
        try {
	        url = new URL(wiki.getProtocol() + wiki.getDomain());
        } catch (MalformedURLException e) {
	        throw new RuntimeException(e);
        }
		return checkConnection(url);
	}
	
	protected boolean checkWorking() {
		try {
	        Map<String, Object> info = wiki.getSiteInfo();
	        if (info == null) {
	        	setLastError("The method getSiteInfo returned null result");
	        	return false;
	        }
	        if (info.get("version") == null) {
	        	setLastError("No \"version\" field in result -> API result is corrupted");
	        	return false;
	        }
        } catch (IOException e) {
        	setLastError("Exception when calling getSiteInfo(): " + e.toString());
	        return false;
        }
		return true;
	}

}
