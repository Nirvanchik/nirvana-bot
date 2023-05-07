/**
 *  @(#)ArchiveItem.java
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

package org.wikipedia.nirvana.statistics;

import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.util.XmlTools;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.Calendar;

// TODO: Migrate to java.time dates.
@JsonIgnoreProperties({"getDateAsInt", "dateAsInt", "getQuarter", "quarter"})
public class ArchiveItem {
    
    public String article;
    public String user;
    public int year;
    public int month;
    public int day;
    public int size;
    
    /**
     * Constructs archive item using all item properties.
     * Size will be set to 0.
     *
     */
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

    public ArchiveItem(Revision revision) {
        this(revision, revision.getSize());
    }

    /**
     * Constructs archive item using data from {@link Revision} object and size.
     */
    public ArchiveItem(Revision revision, int size) {
        this.article = XmlTools.removeEscape(revision.getPage());
        this.user = XmlTools.removeEscape(revision.getUser());
        OffsetDateTime datetime = revision.getTimestamp();
        this.year = datetime.getYear();
        // We are still working with Calendar dates, where months are indexed from 0.
        this.month = datetime.getMonthValue() - 1;
        if (this.month < 0) throw new IllegalStateException("Negative month value");
        this.day = datetime.getDayOfMonth();
        this.size = size;
    }

    /**
     * Constructs archive item. Creation date parameters are taken from Calendar instance.
     *
     */
    public ArchiveItem(String article, String user, Calendar cal) {
        this.article = article;
        this.user = user;
        this.year = cal.get(Calendar.YEAR);
        this.month = cal.get(Calendar.MONTH);
        this.day = cal.get(Calendar.DAY_OF_MONTH);
    }

    public int getDateAsInt() {
        return (year << 16 | month << 8 | day);
    }
    
    public int getQuarter() {
        return month / 3 + 1;
    }
}
