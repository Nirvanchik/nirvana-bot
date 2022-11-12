package org.wikipedia.nirvana.parser.parser;

import org.wikipedia.Wiki;
import org.wikipedia.nirvana.error.ServiceError;

import java.io.IOException;
import java.util.Collection;

/**
 * @author kmorozov
 */
public interface PageListParser {

    Collection<Wiki.Revision> parsePagesList(String rawPageList) throws IOException, ServiceError;
}
