/**
 *  @(#)NetworkInterface.java
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

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Service that represents local network connection, no matter wired or wireless.
 *
 */
public class NetworkInterface extends BasicService {

    /**
     * Default constructor.
     */
    public NetworkInterface() {
        super("Network interface");
    }

    @Override
    protected boolean checkOk() {
        try {
            if ("127.0.0.1".equals(InetAddress.getLocalHost().getHostAddress().toString())) {
                setLastError("This host has a local address : 127.0.0.1");
                return false;
            }
        } catch (UnknownHostException e) {
            setLastError("Got exception when checking host address: " + e.toString());
            return false;
        }
        return true;
    }
}
