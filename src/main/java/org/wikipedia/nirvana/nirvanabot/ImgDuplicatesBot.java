package org.wikipedia.nirvana.nirvanabot;

import org.apache.logging.log4j.util.Strings;
import org.wikipedia.Wiki;
import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.ServiceError;
import org.wikipedia.nirvana.WikiTools;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.FetcherFactory;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.PageListFetcher;
import org.wikipedia.nirvana.nirvanabot.templates.SimpleTemplateFilter;
import org.wikipedia.nirvana.nirvanabot.templates.TemplateFilter;
import org.wikipedia.nirvana.parser.format.TabFormatDescriptor;
import org.wikipedia.nirvana.parser.parser.DefaultPageListParser;
import org.wikipedia.nirvana.parser.parser.PageListParser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author kmorozov
 */
public class ImgDuplicatesBot extends NirvanaBot {

    private String[] INFOBOX_TEMPLATES;

    /**
     * Constructor with flags
     *
     * @param flags
     */
    public ImgDuplicatesBot(int flags) {
        super(flags);
    }

    @Override
    protected boolean loadCustomProperties(Map<String, String> launch_params) {
        INFOBOX_TEMPLATES = properties.getProperty("infobox-templates").split(" ");

        return true;
    }

    @Override
    protected void go() throws InterruptedException, BotFatalError {
        if (INFOBOX_TEMPLATES == null || INFOBOX_TEMPLATES.length == 0)
            throw new BotFatalError("No templates to check.");

        TemplateFilter templateFilter = new SimpleTemplateFilter(Arrays.asList(INFOBOX_TEMPLATES), WikiTools.EnumerationType.OR);
        PageListFetcher fetcher = new FetcherFactory.PagesWithTemplatesFetcher(templateFilter);

        WikiTools.Service service = WikiTools.Service.PETSCAN;
        NirvanaWiki wiki = createCommonsWiki();

        try {
            String rawPageList = fetcher.loadNewPagesForCat(service, "", "ru", 0, 0);

            if (!Strings.isEmpty(rawPageList)) {
                PageListParser parser = new DefaultPageListParser((TabFormatDescriptor) service.getFormat().getFormatDescriptor());
                try {
                    List<Wiki.Revision> pages = parser.parsePagesList(service, wiki, rawPageList);

                    if (pages != null && pages.size() > 0) {

                    }
                } catch (ServiceError serviceError) {
                    serviceError.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
