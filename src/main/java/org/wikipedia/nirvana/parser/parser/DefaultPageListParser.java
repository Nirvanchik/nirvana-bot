package org.wikipedia.nirvana.parser.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wikipedia.Wiki;
import org.wikipedia.nirvana.FileTools;
import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.ServiceError;
import org.wikipedia.nirvana.StringTools;
import org.wikipedia.nirvana.WikiTools;
import org.wikipedia.nirvana.nirvanabot.NirvanaBot;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.RevisionWithId;
import org.wikipedia.nirvana.parser.format.TabFormatDescriptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author kmorozov
 */
public class DefaultPageListParser implements PageListParser {

    private final Logger log = LogManager.getLogger(this.getClass().getName());

    // We will not check every line if output has thousands lines for better performance
    protected final int LINES_TO_CHECK = 25;

    private final TabFormatDescriptor descriptor;
    private final Pattern lineRulePattern;
    private final WikiTools.Service service;
    private final NirvanaWiki wiki;
    private final int namespace;

    public DefaultPageListParser(WikiTools.Service service, NirvanaWiki wiki, TabFormatDescriptor descriptor, int namespace) {
        this.descriptor = descriptor;
        this.namespace = namespace;
        this.service = service;
        this.wiki = wiki;

        lineRulePattern = Pattern.compile(descriptor.getLineRule());
    }

    public DefaultPageListParser(WikiTools.Service service, NirvanaWiki wiki) {
        this(service, wiki, (TabFormatDescriptor) service.getFormat().getFormatDescriptor(), NirvanaBot.DEFAULT_NAMESPACE);
    }

    @Override
    public Collection<Wiki.Revision> parsePagesList(String rawPageList) throws IOException, ServiceError {
        if (rawPageList.startsWith("ERROR : MYSQL error")) {
            log.error("Invalid service output: " + StringTools.trancateTo(rawPageList, 100));
            throw new ServiceError("Invalid output of service" + service.getName());
        }

        Map<String, Wiki.Revision> pageInfoMap = new HashMap<>();

        String namespaceIdentifier = namespace == 0 ? "" : wiki.namespaceIdentifier(namespace);

        try (StringReader sr = new StringReader(rawPageList); BufferedReader b = new BufferedReader(sr)) {
            for (int j = 0; j < descriptor.getSkipLines(); j++) b.readLine();
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
                int thisNS = 0; // articles by default
                if (!service.filteredByNamespace) {
                    try {
                        thisNS = Integer.valueOf(groups[descriptor.getNamespacePos()]);
                    } catch (NumberFormatException e) {
                        log.warn("invalid namespace detected", e);
                    }
                }
                // то что мы ищем совпадает с тем, что нашли
                if (service.filteredByNamespace || thisNS == namespace) {
                    String title = groups[descriptor.getTitlePos()].replace('_', ' ');
                    if (!service.hasSuffix && namespace != 0) {
                        title = namespaceIdentifier + ":" + title;
                        log.debug("Namespace is not 0, add suffix!");
                    }

                    pageInfoMap.computeIfAbsent(title, _title -> {
                        long revId = 0;
                        if (descriptor.getRevidPos() >= 0) {
                            try {
                                revId = Long.valueOf(groups[descriptor.getRevidPos()]);
                            } catch (NumberFormatException e) {
                                log.error(e.toString());
                            }
                        }
                        long id = 0;
                        if (descriptor.getIdPos() >= 0) {
                            try {
                                id = Long.valueOf(groups[descriptor.getIdPos()]);
                            } catch (NumberFormatException e) {
                                log.error(e.toString());
                            }
                        }
                        log.debug("adding page to list:" + _title);

                        return new RevisionWithId(wiki, revId, Calendar.getInstance(), _title, "", "", false, false, true, 0, id);
                    });
                } else if (thisNS == Wiki.USER_NAMESPACE && namespace != Wiki.USER_NAMESPACE) {
                    // Здесь мы обрабатываем случаи, когда статьи сначала проходят через личное
                    // пространство а потом переименовываются в основное пространство
                    //String title = groups[TITLE_POS].replace('_', ' ');
                    long revId = 0;
                    if (descriptor.getRevidPos() >= 0) {
                        try {
                            revId = Long.valueOf(groups[descriptor.getRevidPos()]);
                        } catch (NumberFormatException e) {
                            log.error(e.toString());
                            continue;
                        }
                    }
                    Wiki.Revision r = wiki.getRevision(revId);
                    String title = r.getPage();

                    // Случай когда мы ищем категории, шаблоны и т.д. чтобы отсеять обычные статьи
                    int n = wiki.namespace(title);
                    if (n != namespace) {
                        continue;
                    }

                    if (!pageInfoMap.containsKey(title)) {
                        pageInfoMap.put(title, r);
                        log.debug("adding page to list:" + title);
                    }
                }
            }
        }

        return pageInfoMap.values();
    }
}
