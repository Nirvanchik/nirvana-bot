/**
 *  @(#)Pages.java
 *  Copyright © 2011-2014 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.base.BotFatalError;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.FetcherFactory;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.PageListFetcher;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.PageListProcessor;
import org.wikipedia.nirvana.util.SystemTime;
import org.wikipedia.nirvana.wiki.CatScanTools;
import org.wikipedia.nirvana.wiki.CatScanTools.Service;
import org.wikipedia.nirvana.wiki.CatScanTools.ServiceFeatures;

import java.util.List;

/**
 * Portal page that generates list of articles selected by categories only,
 * or additionally by having some templates.
 *
 */
public class Pages extends NewPages {

    /**
     * Constructs class instance.
     */
    public Pages(PortalParam param, PageFormatter pageFormatter, SystemTime systemTime) {
        super(param, pageFormatter, systemTime);

        if (this.archiveSettings.archive != null) {
            // TODO: Handle this in the apropriate place. This class should not care about it.
            this.archiveSettings.archive = null;
        }
        getRevisionMethod = GetRevisionMethod.GET_FIRST_REV_IF_NEED;
        UPDATE_FROM_OLD = false;
        UPDATE_ARCHIVE = false;
        supportAuthor = false;
    }

    @Override
    protected PageListProcessor createPageListProcessor() throws BotFatalError {
        PageListFetcher fetcher;
        needsCustomTemlateFiltering = false;
        if (templateFilter == null) {
            fetcher = createSimpleFetcher();
        } else {
            CatScanTools.Service service = this.service;
            if (!service.supportsFeature(CatScanTools.ServiceFeatures.PAGES_WITH_TEMPLATE)) {
                service = CatScanTools.Service.getDefaultServiceForFeature(
                        CatScanTools.ServiceFeatures.PAGES_WITH_TEMPLATE, this.service);
            }
            if (service.supportsFeature(CatScanTools.ServiceFeatures.PAGES_WITH_TEMPLATE)) {
                fetcher = new FetcherFactory.PagesWithTemplatesFetcher(templateFilter);
            } else {
                needsCustomTemlateFiltering = true;
                fetcher = createSimpleFetcher();
            }
        }
        return createPageListProcessorWithFetcher(service, fetcher, categories, categoriesToIgnore);
    }
    
    private PageListFetcher createSimpleFetcher() throws BotFatalError {
        Service service = this.service;
        if (!service.supportsFeature(ServiceFeatures.PAGES)) {
            service = Service.getDefaultServiceForFeature(
                    ServiceFeatures.PAGES, this.service);
        }
        if (!service.supportsFeature(ServiceFeatures.PAGES)) {
            throw new BotFatalError("No service available for this project page list.");
        }
        return new FetcherFactory.PagesFetcher();
    }

    @Override
    public void sortPages(List<Revision> pageInfoList, boolean byRevision) {
        // no sort (sorted by default)
        // In CATSCAN2 sort is disabled so we have to sort actually until it's fixed on catscan2
        sortPagesByName(pageInfoList);
    }    
    
}
