/**
 *  Copyright © 2022 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
 *  @author kmorozov
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

package org.wikipedia.nirvana.parser.parser;

import org.wikipedia.Wiki;
import org.wikipedia.nirvana.error.ServiceError;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.RevisionWithId;
import org.wikipedia.nirvana.parser.format.TabFormatDescriptor;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

// TODO: Support parsing of 1-namespace lists and many-namespaces lists.
// 1-namespace parser doesn't parse namespace position. It requires additional argument "namespace".
// many-namespaces parser parses namespace position.
// TODO: Get rid of RevisionWithId class here which is nirvanabot specific. Create internal class 
//     for this matter.
// TODO: Get rid of service instance from here. This is just parser. It should be free from knowing
//     any services.
// TODO: Get rid of NirvanaWiki instance here. This is a simple parser. It should not depend on
//     Wiki heavy class. Finally, allowed dependencies here: org.wikipedia.nirvana.util and
//     org.wikipedia.nirvana.parser. So this code can be moved to separate common module.
/**
 * Default Catscan response parser class implementation (works with TSV format only).
 */
public class DefaultPageListParser implements PageListParser {

    private final Logger log;

    // We will not check every line if output has thousands lines for better performance
    protected static final int LINES_TO_CHECK = 25;

    private final TabFormatDescriptor descriptor;
    private final Pattern lineRulePattern;
    private final CatScanTools.Service service;
    private final NirvanaWiki wiki;
    private final int namespace;

    /**
     * Constructs instance using specified descriptor object.
     */
    public DefaultPageListParser(CatScanTools.Service service, NirvanaWiki wiki,
            TabFormatDescriptor descriptor, int namespace) {
        this.descriptor = descriptor;
        this.namespace = namespace;
        this.service = service;
        this.wiki = wiki;

        lineRulePattern = Pattern.compile(descriptor.lineRule);
        log = LogManager.getLogger(this.getClass().getName());
    }

    public DefaultPageListParser(CatScanTools.Service service, NirvanaWiki wiki, int namespace) {
        this(service, wiki, (TabFormatDescriptor) service.getFormat().getFormatDescriptor(),
                namespace);
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

    @Override
    public Collection<Wiki.Revision> parsePagesList(String rawPageList)
            throws IOException, ServiceError {
        if (rawPageList.startsWith("ERROR : MYSQL error")) {
            log.error("Invalid service output: " + StringTools.trancateTo(rawPageList, 100));
            throw new ServiceError("Invalid output of service" + service.getName());
        }
        if (!rawPageList.contains("\n")) {
            log.warn("Service output looks bad - no new lines. See first 300 chars: {}",
                    StringTools.trancateTo(rawPageList, 300));
        }

        Map<String, Wiki.Revision> pageInfoMap = new TreeMap<>();

        String namespaceIdentifier = namespace == 0 ? "" : wiki.namespaceIdentifier(namespace);

        try (StringReader sr = new StringReader(rawPageList);
                BufferedReader b = new BufferedReader(sr)) {
            for (int j = 0; j < descriptor.skipLines; j++) b.readLine();
            String line;
            int j = 0;
            while ((line = b.readLine()) != null) {
                j++;
                if (line.isEmpty()) continue;
                if (j < LINES_TO_CHECK && !lineRulePattern.matcher(line).matches()) {
                    log.error("Invalid service output line: " + line);
                    FileTools.dump(rawPageList, "last_service_out_with_error.txt");
                    throw new ServiceError("Invalid output of service: " + service.getName());
                }
                String[] groups = line.split("\t");
                int thisNs = 0; // articles by default
                if (!service.filteredByNamespace) {
                    throw new IllegalStateException(
                        CatScanTools.ERR_SERVICE_FILTER_BY_NAMESPACE_DISABLED);
                }
                // то что мы ищем совпадает с тем, что нашли
                if (service.filteredByNamespace) {
                    String title = groups[descriptor.titlePos].replace('_', ' ');
                    if (!service.hasSuffix && namespace != 0) {
                        title = namespaceIdentifier + ":" + title;
                        log.debug("Namespace is not 0, add suffix!");
                    }

                    pageInfoMap.computeIfAbsent(title, _title -> {
                        long id = 0;
                        if (descriptor.idPos >= 0) {
                            try {
                                id = Long.parseLong(groups[descriptor.idPos]);
                            } catch (NumberFormatException e) {
                                log.error(e.toString());
                            }
                        }
                        log.debug("Add page to list: {}", _title);

                        return new RevisionWithId(wiki, 0, OffsetDateTime.now(), _title, "",
                                "", false, false, true, 0, id);
                    });
                }
            }
        }

        return pageInfoMap.values();
    }
}
