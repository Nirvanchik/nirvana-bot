/**
 *  @(#)PortalModule.java 07/04/2012
 *  Copyright © 2011 - 2014 Dmitry Trofimovich (KIN)(DimaTrofimovich@gmail.com)
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

import java.io.IOException;
import java.rmi.ServerError;

import javax.security.auth.login.LoginException;

import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.ServiceError;

/**
 * @author kin
 *
 */
public interface PortalModule {
	public boolean update(NirvanaWiki wiki, ReportItem reportData, 
			String comment) throws IOException, LoginException, InterruptedException, ServiceError;	
}

