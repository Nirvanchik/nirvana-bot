/**
 *  @(#)RevisionWithId.java
 *  Copyright © 2022 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;

import java.time.OffsetDateTime;

/**
 * Extended version of {@class Wiki.Revision} class that can keep page id.
 * This page id is later used to sort pages by id which is similar to sorting it by creation date. 
 *
 */
public class RevisionWithId extends Revision {
    private long id;

    /**
     * Constructs RevisionWithId object using specified parameters for Revision class and id.
     */
    public RevisionWithId(Wiki wiki, long revid, OffsetDateTime timestamp,
            String title, String summary, String user, boolean minor,
            boolean bot, boolean rvnew, int size, long id) {
        wiki.super(revid, timestamp, title, summary, user, minor, bot, rvnew, size);
        this.id = id;
    }

    /**
     * Constructs RevisionWithId object by cloning existing Revision object.
     */
    public RevisionWithId(Wiki wiki, Revision rev, long id) {
        wiki.super(rev.getRevid(), rev.getTimestamp(), rev.getPage(), 
                rev.getSummary(), rev.getUser(), rev.isMinor(), rev.isBot(), rev.isNew(),
                rev.getSize());
        this.id = id;
    }

    /**
     * @return page id.
     */
    public long getId() { return this.id; }
    
    /**
     * Sets page id.
     *
     * @param id page id
     */
    public void setId(long id) { this.id = id; }

}
