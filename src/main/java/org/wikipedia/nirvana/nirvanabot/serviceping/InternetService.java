/**
 *  @(#)InternetService.java 12.03.2016
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

import org.wikipedia.nirvana.util.HTTPTools;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author kin
 *
 */
public class InternetService extends DoubleCheckService {
	private final URL url1;
	private final URL url2;
	private static final Pattern HTML_SITE_PATTERN = 
			Pattern.compile("^.*<html.*>.*<body.*>.*<\\/body>.*<\\/html>.*$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			//Pattern.compile("^.*<html.*>.*<\\/html>.*$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	public InternetService() {
		this("Internet connection");
	}
	
	public InternetService(String name) {
		super(name);
		try {
	        url1 = new URL("https://ya.ru");
			url2 = new URL("http://www.bbc.com/");
        } catch (MalformedURLException e) {	        
	        e.printStackTrace();
	        throw new RuntimeException(e);
        }
	}

	/* (non-Javadoc)
	 * @see org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService#isAvailable()
	 */
	@Override
	protected boolean checkAvailable() {
		if (!checkConnection(url1) && !checkConnection(url2)) {
			return false;
		}
		return true;
	}
	
	protected boolean checkConnection(URL url) {
		try {
			final URLConnection conn = url.openConnection();                                                                                                                                                                                  
	        conn.connect();
        } catch (IOException e) {
        	setLastError("Failed to open connection to url: " + url.toString());
	        return false;
        }                                                                                                                                                                                                                   
        return true;          
	}

    @Override
    protected boolean checkWorking() throws InterruptedException {	    
	    return checkHtmlSiteWorking(url1) || checkHtmlSiteWorking(url2);
    }
    
    private boolean checkHtmlSiteWorking(URL url) {
    	String result = null;
    	try {
	        result = HTTPTools.fetch(url);	        
        } catch (IOException e) {
        	setLastError("IOException when fetching url: " + url.toString()+" " + e.toString());
	        return false;
        }
    	if (result == null || result.isEmpty()) {
    		setLastError("Fetch result is empty from url: "+url.toString());
    		return false;
    	}
    	Matcher m = HTML_SITE_PATTERN.matcher(result);
    	if (!m.matches()) {
    		setLastError(String.format("The site %1$s doesn't match to HTML site pattern", url.toString()));
    		return false;
    	}
    	return true;
    }
}
