/**
 *  @(#)ProcessorCombinator.java
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

import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.error.ServiceError;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Processor that runs a group of other processors and merges results into one list. 
 *
 */
public class ProcessorCombinator implements PageListProcessor {
    protected final Logger log;

    private List<PageListProcessor> fetchers;

    /**
     * Creates processor instance using provided list of other processors.
     *
     * @param fetchers list of processors to use.
     */
    public ProcessorCombinator(List<PageListProcessor> fetchers) {
        this.fetchers = fetchers;
        log = LogManager.getLogger(this.getClass().getName());
    }

    @Override
    public ArrayList<Revision> getNewPages(NirvanaWiki wiki)
            throws IOException, InterruptedException, ServiceError {
        ArrayList<Revision> list;
        log.debug("getting results from main fetcher");
        list = fetchers.get(0).getNewPages(wiki);
        for (int i = 1; i < fetchers.size(); i++) {
            log.debug("getting results from another fetcher: {}", i);
            list.addAll(fetchers.get(i).getNewPages(wiki));
        }
        return list;
    }

    @Override
    public boolean mayHaveDuplicates() {        
        return fetchers.size() > 1;
    }

}
