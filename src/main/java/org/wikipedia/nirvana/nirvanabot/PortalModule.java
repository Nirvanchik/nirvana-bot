/**
 *  @(#)PortalModule.java 16/04/2017
 *  Copyright © 2011 - 2017 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.wikipedia.nirvana.ServiceError;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import java.io.IOException;

import javax.security.auth.login.LoginException;

/**
 * Basic interface for portal module updaters.
 */
public interface PortalModule {
    public boolean update(NirvanaWiki wiki, ReportItem reportData, String comment)
            throws IOException, LoginException, InterruptedException, ServiceError, BotFatalError,
            InvalidLineFormatException, DangerousEditException;
}
