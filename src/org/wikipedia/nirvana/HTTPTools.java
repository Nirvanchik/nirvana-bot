/**
 *  @(#)HTTPTools.java 
 *  Copyright � 2011-2014 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author kin
 *
 */
public class HTTPTools {
	private static final String USER_AGENT = "NirvanaBot";
	private static final String DEFAULT_ENCODING = "UTF-8";
    private static final Logger log;
	// time to open a connection
    private static final int CONNECTION_CONNECT_TIMEOUT_MSEC_LONG = 120000; // 120 seconds
    // time for the read to take place. (needs to be longer, some connections are slow
    // and the data volume is large!)
    private static final int CONNECTION_READ_TIMEOUT_MSEC_LONG = 5*60*1000; // 5 min

    private static final int CONNECTION_CONNECT_TIMEOUT_MSEC_SHORT = 15*1000;
    private static final int CONNECTION_READ_TIMEOUT_MSEC_SHORT = 60*1000;

    static {
        log = LogManager.getLogger(HTTPTools.class.getName());
    }

    public static String fetch(String url) throws IOException {
    	return fetch(url, false, false, true);
    }

    public static String fetch(URL url) throws IOException {
    	return fetch(url, false, false, true);
    }
    
    public static String fetch(String url, boolean longTimeout, boolean removeEscape, boolean customUserAgent) throws IOException {
    	return fetch(new URL(url), longTimeout, removeEscape, customUserAgent);
    }
    
	public static String fetch(URL url, boolean longTimeout, boolean removeEscape, boolean customUserAgent) throws IOException
    {
		log.debug("fetching url="+url);
		log.trace("remove escape="+removeEscape);
        // connect
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        if (longTimeout) {
        	connection.setConnectTimeout(CONNECTION_CONNECT_TIMEOUT_MSEC_LONG);
        	connection.setReadTimeout(CONNECTION_READ_TIMEOUT_MSEC_LONG);
        } else {
        	connection.setConnectTimeout(CONNECTION_CONNECT_TIMEOUT_MSEC_SHORT);
        	connection.setReadTimeout(CONNECTION_READ_TIMEOUT_MSEC_SHORT);
        }
        if (customUserAgent) {
        	connection.setRequestProperty("User-Agent", USER_AGENT);
        }
        //setCookies(connection, cookies);
        connection.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(
            connection.getInputStream(), DEFAULT_ENCODING));       

        // get the text
        String line;
        StringBuilder text = new StringBuilder(100000);
        while ((line = in.readLine()) != null)
        {
        	if(removeEscape) {
        		line = line.replace("&quot;","\"");
        	}
            text.append(line);
            text.append("\n");
        }
        in.close();
        return text.toString();
    }
	
	public static void download(String url, File file) throws IOException {
		log.debug("downloading url="+url);
		// connect
        URLConnection connection = new URL(url).openConnection();
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(180000);
        //setCookies(connection, cookies);
        connection.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(
            connection.getInputStream(), "UTF-8"));       

        // get the text
        String line;
        
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        while ((line = in.readLine()) != null)
        {
        	out.append(line);
        	out.append("\n");
        }
        in.close();
        out.close();
        return;
	}
	   
	public static void download(String url, String fileName) throws IOException {
        File file = new File(fileName);
        download(url,file);
    }
	
	public static String removeEscape(String line) {
		return line.replace("&quot;","\"").replace("&#039;", "'").replace("&amp;", "&");
	}
}
