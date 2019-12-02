/**
 *  @(#)HTTPTools.java 
 *  Copyright © 2014 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.util;

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
 * Simple HttpURLConnection wrapper for GET requests.
 * Allows customizing user-agent and network timeouts.  
 * Used for REST API requests, texts downloading.
 *
 * Additionally, has utility method for HTML entity processing.
 */
public class HttpTools {
    private static final String USER_AGENT = "NirvanaBot";
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final Logger log;
    // time to open a connection
    private static final int CONNECTION_CONNECT_TIMEOUT_MSEC_LONG = 120000; // 120 seconds
    // time for the read to take place. (needs to be longer, some connections are slow
    // and the data volume is large!)
    private static final int CONNECTION_READ_TIMEOUT_MSEC_LONG = 5 * 60 * 1000; // 5 min

    private static final int CONNECTION_CONNECT_TIMEOUT_MSEC_SHORT = 15 * 1000;
    private static final int CONNECTION_READ_TIMEOUT_MSEC_SHORT = 60 * 1000;

    static {
        log = LogManager.getLogger(HttpTools.class.getName());
    }

    /**
     * Fetch text from the specified url to a string.
     *
     * @param url Url to fetch.
     * @return a string with fetched text.
     */
    public static String fetch(String url) throws IOException {
        return fetch(url, false, false, true);
    }

    /**
     * Fetch text from the specified url to a string.
     *
     * @param url Url to fetch.
     * @return a string with fetched text.
     */
    public static String fetch(URL url) throws IOException {
        return fetch(url, false, false, true);
    }

    /**
     * Fetch text from the specified url to a string.
     *
     * @param url Url to fetch.
     * @param longTimeout if <code>true</code> long timeout values will be used.
     * @param removeEscape whether to unescape HTML escaped characters. 
     * @param customUserAgent whether to use internal userAgent. 
     * @return a string with fetched text.
     */
    public static String fetch(String url, boolean longTimeout, boolean removeEscape,
            boolean customUserAgent) throws IOException {
        return fetch(new URL(url), longTimeout, removeEscape, customUserAgent);
    }

    // TODO: Remove "removeEscape" out of this class to respect "single responsibility" principle.
    // This class does not know what it is fetching and it is not its level to do that.
    /**
     * Fetch text from the specified url to a string.
     *
     * @param url Url to fetch.
     * @param longTimeout if <code>true</code> long timeout values will be used.
     * @param removeEscape whether to unescape HTML escaped characters. 
     * @param customUserAgent whether to use internal userAgent. 
     * @return a string with fetched text.
     */
    public static String fetch(URL url, boolean longTimeout, boolean removeEscape,
            boolean customUserAgent) throws IOException {
        log.debug("fetching url: {}", url);
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
        StringBuilder text = new StringBuilder(100000);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
            connection.getInputStream(), DEFAULT_ENCODING))) {       

            String line;
            while ((line = in.readLine()) != null) {
                if (removeEscape) {
                    line = line.replace("&quot;","\"");
                }
                text.append(line);
                text.append("\n");
            }
        }
        return text.toString();
    }

    /**
     * Download text from the specified url and save it to a file.
     *
     * @param url Url to download from.
     * @param file File to save text.
     */
    public static void download(String url, File file) throws IOException {
        log.debug("downloading url: {}", url);
        // connect
        URLConnection connection = new URL(url).openConnection();
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(180000);
        //setCookies(connection, cookies);
        connection.connect();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                connection.getInputStream(), "UTF-8"))) {

            String line;
            try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
                while ((line = in.readLine()) != null) {
                    out.append(line);
                    out.append("\n");
                }
            }
        }
        return;
    }

    /**
     * Download text from the specified url and save it to a file.
     *
     * @param url Url to download from.
     * @param fileName File path to save text.
     */
    public static void download(String url, String fileName) throws IOException {
        File file = new File(fileName);
        download(url, file);
    }
}
