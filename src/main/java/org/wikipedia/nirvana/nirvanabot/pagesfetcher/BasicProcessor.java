/**
 *  @(#)BasicProcessor.java
 *  Copyright © 2022 Dmitry Trofimovich (KIN)(DimaTrofimovich@gmail.com)
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

import org.wikipedia.nirvana.error.ServiceError;
import org.wikipedia.nirvana.wiki.CatScanTools;
import org.wikipedia.nirvana.wiki.CatScanTools.NamespaceFormat;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

// TODO: Dummy class. Remove it out of here?
/**
 * Basic class for page list processors.
 * I don't know what is its purpose.
 *
 */
public abstract class BasicProcessor implements PageListProcessor {
    protected final Logger log;

    protected CatScanTools.Service service;
    protected List<String> categories;
    protected List<String> categoriesToIgnore;
    protected String language;
    protected int depth;
    protected int namespace;
    protected PageListFetcher fetcher;

    /**
     * Constructs processor class.
     */
    public BasicProcessor(CatScanTools.Service service, List<String> cats, List<String> ignore, 
            String lang, int depth, int namespace, PageListFetcher fetcher) {
        this.service = service;
        this.categories = cats;
        this.categoriesToIgnore = ignore;
        this.language = lang;
        this.depth = depth;
        this.namespace = namespace;
        this.fetcher = fetcher;
        log = LogManager.getLogger(this.getClass().getName());
    }

    /**
     * Returns integer representation of wiki namespace.
     * When we parse "namespace" item from service response we can get in one of the next formats:
     * - integer, localized string, canonical (English) string. So here we read this raw string
     * and convert to the required for us format.
     * 
     * @param wiki {@class NirvanaWiki} instance.
     * @param namespaceString namespace string returned by the service.
     * @return integer value of namespace
     */
    protected int getNamespaceId(NirvanaWiki wiki, String namespaceString)
            throws IOException, ServiceError {
        int id;
        if (service.namespaceFormat == NamespaceFormat.NUMBER) {
            try {
                id = Integer.parseInt(namespaceString);
            } catch (NumberFormatException e) {
                throw new ServiceError("Service returned non-numeric namespace id", e);
            }
        } else if (service.namespaceFormat.isString) {
            id = wiki.namespaceId(namespaceString);
        } else {
            throw new IllegalStateException("Unexpected namespace format: " +
                    service.namespaceFormat.toString());
        }
        return id;
    }

}
