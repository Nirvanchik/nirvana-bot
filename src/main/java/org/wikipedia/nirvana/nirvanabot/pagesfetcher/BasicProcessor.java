/**
 *  @(#)BasicProcessor.java 10.12.2014
 *  Copyright © 2014 Dmitry Trofimovich (KIN)(DimaTrofimovich@gmail.com)
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
import org.wikipedia.nirvana.error.ServiceError;
import org.wikipedia.nirvana.util.FileTools;
import org.wikipedia.nirvana.util.StringTools;
import org.wikipedia.nirvana.wiki.CatScanTools;
import org.wikipedia.nirvana.wiki.CatScanTools.NamespaceFormat;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author kin
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

    // Regex to check validity of line
    protected String LINE_RULE;
    // We will not check every line if output has thousands lines for better performance
    protected final int LINES_TO_CHECK = 25;

    /**
	 * 
	 */
    public BasicProcessor(CatScanTools.Service service, List<String> cats, List<String> ignore, 
			String lang, int depth, int namespace, PageListFetcher fetcher) {
		this.service = service;
		this.categories = cats;
		this.categoriesToIgnore = ignore;
		this.language = lang;
		this.depth = depth;
		this.namespace = namespace;
		this.LINE_RULE = "^.+$";
		this.fetcher = fetcher;
        if (service.lineRule != null) {
            this.LINE_RULE = service.lineRule;
		}
        log = LogManager.getLogger(this.getClass().getName());
	}

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

	public void parsePageList(NirvanaWiki wiki, HashSet<String> pages, ArrayList<Revision> pageInfoList, HashSet<String> ignore, String pageList) throws IOException, ServiceError {
        if (pageList.startsWith("ERROR : MYSQL error")) {
            log.error("Invalid service output. See first 300 chars: {}",
                    StringTools.trancateTo(pageList, 300));
        	throw new ServiceError("Invalid output of service: "+service.getName());
        }
        if (!pageList.contains("\n")) {
            log.warn("Service output looks bad - no new lines. See first 300 chars: {}",
                    StringTools.trancateTo(pageList, 300));
        }
		String line;
		String namespaceIdentifier = "";
		if (namespace!=0) {
			namespaceIdentifier = wiki.namespaceIdentifier(namespace);
		}
		StringReader sr = new StringReader(pageList);
		BufferedReader b = new BufferedReader(sr);
        for (int j = 0; j < service.skipLines; j++) {
            b.readLine();
        }
		Pattern p = Pattern.compile(LINE_RULE);

		int j = 0;
        while ((line = b.readLine()) != null)
        {
        	j++;
        	if (line.isEmpty()) continue;
        	if (j<LINES_TO_CHECK && !p.matcher(line).matches()) {
        		log.error("Invalid service output line: "+line);
                FileTools.dump(pageList, "last_service_out_wit_error.txt");
        		throw new ServiceError("Invalid output of service: "+service.getName());
        	}
            // TODO: Move this low level TSV parsing out of here.
            String[] groups = line.split("\t");
            if (!service.filteredByNamespace) {
                throw new IllegalStateException(
                        CatScanTools.ERR_SERVICE_FILTER_BY_NAMESPACE_DISABLED);
            }
            if (service.filteredByNamespace) {
                String title = groups[service.titlePos].replace('_', ' ');
                if (ignore != null && ignore.contains(title)) {
                    log.debug("Ignore page: {}", title);
                    continue;
                }
                if (!service.hasSuffix && namespace != 0)
                {	                	
                    title = namespaceIdentifier + ":" + title;	                	
                    log.debug("Namespace is not 0, add suffix!");
                }	                
                
                if (!pages.contains(title))
                {
                	long id = 0;
                    if (service.idPos >= 0) {
                		try {
                            id = Long.parseLong(groups[service.idPos]);
		                } catch(NumberFormatException e) {
		                	log.error(e.toString());
		                	continue;
		                }
                	}
                    // TODO: This code looks ugly. Rework it.
                    // We don't know real revision information and create some fake RevisionWithId
                    // object where only title is only known and other fields are fake and
                    // dangerous to use because wrong and unknown.
                    RevisionWithId page = new RevisionWithId(wiki, 0, OffsetDateTime.now(), title,
                            "", "", false, false, true, 0, id);
                    pages.add(title);
                    log.debug("Add page to list: {}", title);
                    pageInfoList.add(page);
                }
            }
        }//while
	}

}
