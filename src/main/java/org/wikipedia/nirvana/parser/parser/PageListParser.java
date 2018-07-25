package org.wikipedia.nirvana.parser.parser;

import org.wikipedia.Wiki;
import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.ServiceError;
import org.wikipedia.nirvana.WikiTools;

import java.io.IOException;
import java.util.List;

/**
 * @author kmorozov
 */
public interface PageListParser {

    List<Wiki.Revision> parsePagesList(String rawPageList) throws IOException, ServiceError;
}
