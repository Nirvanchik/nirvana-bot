/**
 *  @(#)RevisionWithId.java 02.11.2013
 *  Copyright © 2013 Dmitry Trofimovich (KIN)(DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.nirvanabot.pagesfetcher;

import java.util.Calendar;

import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;

/**
 * @author kin
 *
 */
public class RevisionWithId extends Revision {
	private long id;

	public RevisionWithId(Wiki wiki, long revid, Calendar timestamp,
			String title, String summary, String user, boolean minor,
			boolean bot, boolean rvnew, int size, long id) {
		wiki.super(revid, timestamp, title, summary, user, minor, bot, rvnew, size);
		this.id = id;
	}
	
	public RevisionWithId(Wiki wiki, Revision r, long id) {
		wiki.super(r.getRevid(), r.getTimestamp(), r.getPage(), 
				r.getSummary(), r.getUser(), r.isMinor(), r.isBot(), r.isNew(), r.getSize());
		this.id = id;
	}
	
	public long getId() { return this.id; }
	public void setId(long id) { this.id = id; }
	
}
