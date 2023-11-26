/**
 *  @(#)WikiService.java
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

import org.wikipedia.nirvana.wiki.NirvanaWiki;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.security.auth.login.FailedLoginException;

/**
 * Checks availability of Wiki site (HTTP API is checked).
 *
 */
public class WikiService extends InternetService {
    NirvanaWiki wiki;
    boolean reloginRequired;

    /**
     * Constructs checker object with specific wiki site.
     */
    public WikiService(NirvanaWiki wiki) {
        this(wiki.getDomain(), wiki);
    }
    
    /**
     * Constructs checker object with a custom name and specific wiki site.
     */
    public WikiService(String name, NirvanaWiki wiki) {
        super(name);
        this.wiki = wiki;
    }

    /**
     * Set flag that this Wiki client should relogin to continue working.
     * Call this after network disconnects or long time inactivity if you don't know if you are
     * going to interact with this Wiki client.
     *
     * @param reloginRequired relogin status value.
     */
    public void setNeedsRelogin(boolean reloginRequired) {
        this.reloginRequired = reloginRequired;
    }

    /**
     * Call this if you are going to edit wiki and you fear that there was an inactivity for a long
     * time.
     */
    public void reloginIfNeed() throws FailedLoginException, IOException {
        if (reloginRequired) {
            wiki.relogin();
            reloginRequired = false;
        }
    }

    /**
     * Relogin wiki client immediately.
     */
    public void relogin() throws FailedLoginException, IOException {
        wiki.relogin();
        reloginRequired = false;
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

    @Override
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
