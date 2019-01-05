/**
 *  @(#)SpamArchiveSearch.java 0.01 06/07/2011
 *  Copyright (C) 2011 - 2013 MER-C
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.

 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.wikipedia.tools;

import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;
import org.wikipedia.Wiki;

/**
 *  A crude replacement for Eagle's spam archive search tool.
 *  @author MER-C
 *  @version 0.01
 */
public class SpamArchiveSearch
{
    /**
     *  Main for testing/offline stuff. 
     *  @param args command line arguments (ignored)
     *  @throws IOException if a network error occurs
     */
    public static void main(String[] args) throws IOException
    {
        String query = JOptionPane.showInputDialog(null, "Enter query string");
        if (query == null)
            System.exit(0);
        StringBuilder buffer = new StringBuilder(10000);
        ArrayList<String[]> results = archiveSearch(query);
        buffer.append("<h2>Searching for \"");
        buffer.append(query);
        buffer.append("\".</h2>\n<ul>\n");
        for (String[] result : results)
        {
            buffer.append("<li><a href=\"//");
            buffer.append(result[0].contains("Talk:Spam blacklist") ? "meta.wikimedia" : "en.wikipedia");
            buffer.append(".org/wiki/");
            buffer.append(result[0]);
            buffer.append("\">");
            buffer.append(result[0]);
            buffer.append("</a>\n");
        }
        buffer.append("</ul>\n<p>");
        buffer.append(results.size());
        buffer.append(" results.\n");
        System.out.println(buffer.toString());
    }

    /**
     *  Searches the following spam-related discussion archives for the given 
     *  query string:
     * 
     *  <ul>
     *  <li><a href="//meta.wikimedia.org/wiki/WM:SBL">Global spam blacklist</a>
     *  <li><a href="//en.wikipedia.org/wiki/WP:SBL">en.wikipedia spam blacklist</a>
     *  <li><a href="//en.wikipedia.org/wiki/MediaWiki_talk:Spam-whitelist">en.wikipedia spam whitelist</a>
     *  <li><a href="//en.wikipedia.org/wiki/WT:WPSPAM">en.wikipedia WikiProject Spam</a>
     *  <li><a href="//en.wikipedia.org/wiki/WP:RSN">en.wikipedia reliable sources noticeboard</a>
     *  <li><a href="//en.wikipedia.org/wiki/WP:ELN">en.wikipedia external links noticeboard</a>
     *  </ul>
     *  
     *  Domain names need to be enclosed in quotes.
     * 
     *  @param query a query string
     *  @return the spam archive search results for that query
     *  @throws IOException if a network error occurs
     */
    public static ArrayList<String[]> archiveSearch(String query) throws IOException
    {
        Wiki enWiki = new Wiki("en.wikipedia.org");
        Wiki meta = new Wiki("meta.wikimedia.org");
        enWiki.setMaxLag(-1);
        meta.setMaxLag(-1);
        
        // there's some silly api bugs
        ArrayList<String[]> results = new ArrayList<>(20);
        results.addAll(Arrays.asList(meta.search(query + " \"spam blacklist\"", Wiki.TALK_NAMESPACE)));
        results.addAll(Arrays.asList(enWiki.search(query + " \"spam blacklist\"", Wiki.MEDIAWIKI_TALK_NAMESPACE)));
        results.addAll(Arrays.asList(enWiki.search(query + " \"spam whitelist\"", Wiki.MEDIAWIKI_TALK_NAMESPACE)));
        results.addAll(Arrays.asList(enWiki.search(query + " \"wikiproject spam\"", Wiki.PROJECT_TALK_NAMESPACE)));
        results.addAll(Arrays.asList(enWiki.search(query + " \"reliable sources noticeboard\"", Wiki.PROJECT_NAMESPACE)));
        results.addAll(Arrays.asList(enWiki.search(query + " \"external links noticeboard\"", Wiki.PROJECT_NAMESPACE)));

        return results;
    }
}