/**
 *  @(#)ArchiveItem.java
 *  Copyright © 2012 - 2014 Dmitry Trofimovich (KIN)
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

package org.wikipedia.nirvana.statistics;

import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.util.XmlTools;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.Calendar;

@JsonIgnoreProperties({"getDateAsInt","dateAsInt","getQuarter","quarter"})
public class ArchiveItem {
	
	public String article;
	public String user;
	public int year;
	public int month;
	public int day;
	public int size;
	
	public ArchiveItem(String article, String user, int year, int month, int day) {
		this.article = article;
		this.user = user;
		this.year = year;
		this.month = month;
		this.day = day;
	}

	public ArchiveItem() {
		// default constructor
	}

	public ArchiveItem(Revision r) {
        this(r, r.getSize());
	}

	public ArchiveItem(Revision r, int size) {
        this.article = XmlTools.removeEscape(r.getPage());
        this.user = XmlTools.removeEscape(r.getUser());
        OffsetDateTime datetime = r.getTimestamp();
        this.year = datetime.getYear();
        this.month = datetime.getMonthValue();
        this.day = datetime.getDayOfMonth();
		this.size = size;
	}

	public ArchiveItem(String article, String user, Calendar c) {
		this.article = article;
		this.user = user;
		this.year = c.get(Calendar.YEAR);
		this.month = c.get(Calendar.MONTH);
		this.day = c.get(Calendar.DAY_OF_MONTH);
	}

	//@JsonAnySetter
	//@JsonIgnoreProperties
	public int getDateAsInt() {
		return (year<<16 | month<<8 | day);
	}
	
	public int getQuarter() {
		return month/3+1;
	}

}
