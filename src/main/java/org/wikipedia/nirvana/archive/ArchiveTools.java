/**
 *  @(#)ArchiveTools.java
 *  Copyright © 2020 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.archive;

import org.wikipedia.Wiki;
import org.wikipedia.nirvana.util.FileTools;
import org.wikipedia.nirvana.wiki.WikiFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Utility to sort a list of wiki page titles according to creation date (ascending or descending).
 *
 */
public class ArchiveTools {
    public static final int PAGE_COUNT_PER_REQUEST = 20;
    private static Wiki wiki;
    private static final Logger log = Logger.getLogger("ArchiveTools");
    
    /**
     * Data class to keep wiki page title and its id.
     */
    public static class Page {
        String name;
        long id;

        public Page(String name, long id) {
            this.name = name;
            this.id = id;
        }
    }
    
    @SuppressWarnings("rawtypes")
    static List<String> sortPageListByCreationDate(List<String> pages, Wiki wiki, boolean asc) {
        int i = 0;
        ArrayList<Page> pagesWithInfo = new ArrayList<Page>(100000);
        while (i < pages.size()) {
            log.info("processing line: " + i + " of " + pages.size());
            // TODO: Remove this batches code. Wiki now uses POST and can handle large requests.
            int len = PAGE_COUNT_PER_REQUEST;
            if (i + len > pages.size()) {
                len = pages.size() - i;
            }
            List<String> part = pages.subList(i, i + len);
            Map [] results;
            try {
                results = wiki.getPageInfo(part.toArray(new String[0]));
            } catch (IOException e) {
                throw new RuntimeException("Failed to get page info", e);
            }
            for (Map info: results) {
                long pageId = (Long) info.get("pageid");
                String pageName = (String) info.get("displaytitle");
                pagesWithInfo.add(new Page(pageName,pageId));
            }
            i += len;
        }
        return pagesWithInfo.stream()
                .sorted(new Comparator<Page>() {

                    @Override
                    public int compare(Page p1, Page p2) {
                        return (int)(asc ? p1.id - p2.id : p2.id - p1.id);
                    }
                })
                .map(info -> info.name)
                .collect(Collectors.toList());
    }

    /**
     * Main method of tool. 
     */
    static void mainImpl(WikiFactory wikiFactory, String [] args) {
        if (args.length == 0) {
            log.info("Please provide some arguments (file list from which to read pages, "
                    + "'-asc' argument to sort ascending)");
            return;
        }
        boolean asc = false;
        ArrayList<String> files = new ArrayList<String>();
        for (String arg:args) {
            if (arg.startsWith("-")) {
                if (arg.equals("-asc")) {
                    asc = true;
                }
            } else {
                files.add(arg);
            }
        }
        if (files.size() < 2) {
            log.severe("Please provide input and output file name");
            return;
        }
        if (files.size() > 2) {
            log.warning("Ignore args: " + files.subList(2, files.size()).toString());
        }
        String inFile = files.get(0);
        String outFile = files.get(1);

        log.log(Level.INFO, "processing file: " + inFile);
        List<String> pages;
        try {
            pages = FileTools.readFileToList(inFile, FileTools.UTF8, true);
        } catch (FileNotFoundException e1) {
            log.severe("File not found: " + inFile);
            return;
        } catch (IOException e1) {
            log.severe("Failed to read file: " + inFile);
            return;
        }
        wiki = wikiFactory.createInstance("ru.wikipedia.org");
        wiki.setMaxLag( 15 );
        try {
            pages = sortPageListByCreationDate(pages, wiki, asc);

            StringBuilder bld = new StringBuilder();
            for (String p: pages) {
                bld.append(p);
                bld.append(System.lineSeparator());
            }
            String text = bld.toString();
            try {
                FileTools.writeFile(text, outFile, FileTools.UTF8);
            } catch (IOException e) {
                log.severe(String.format("Failed to save file \"%s\" : %s ", outFile,
                        e.getMessage()));
                return;
            }
        } finally {
            wiki.logout();
        }
    }

    /**
     * Entry point of tool. 
     */
    public static void main(String [] args) {
        mainImpl(WikiFactory.ORIGINAL, args);
    }
}
