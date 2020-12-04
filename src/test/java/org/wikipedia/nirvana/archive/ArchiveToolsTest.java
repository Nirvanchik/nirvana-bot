/**
 *  @(#)ArchiveToolsTest.java
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
import org.wikipedia.nirvana.wiki.MockNirvanaWiki;
import org.wikipedia.nirvana.wiki.NirvanaWiki;
import org.wikipedia.nirvana.wiki.WikiFactory;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link ArchiveTools}.
 *
 */
public class ArchiveToolsTest {
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    public static class TestWikiFactory extends WikiFactory {
        private WikiFactoryDelegate delegate;
        
        public TestWikiFactory(WikiFactoryDelegate delegate) {
            this.delegate = delegate;
        }
        
        public static interface WikiFactoryDelegate {
            void initWiki(MockNirvanaWiki wiki);
        }
        
        @Override
        public Wiki createInstance(String domain) {
            MockNirvanaWiki wiki = new MockNirvanaWiki(domain);
            delegate.initWiki(wiki);
            return wiki;
        }
    }

    @SuppressWarnings({ "rawtypes", "serial", "unchecked" })
    private Map mockPageInfo(Long pageId, String displayTitle) {
        return new HashMap() {{
                    put("pageid", pageId);
                    put("displaytitle", displayTitle);
            }
        };
    }

    /**
     * Test method for {@link ArchiveTools#sortPageListByCreationDate()}.
     */
    @Test
    public void testSortPageList() {
        List<String> pageList = Arrays.asList(new String []{"page A", "page Z", "page X"});
        MockNirvanaWiki wiki = new MockNirvanaWiki("ru.wikipedia.org");
        
        wiki.mockPageInfo("page A", mockPageInfo(1L, "Page A"));
        wiki.mockPageInfo("page X", mockPageInfo(25L, "Page X"));
        wiki.mockPageInfo("page Z", mockPageInfo(27L, "Page ZZZ"));
        List<String> sorted = ArchiveTools.sortPageListByCreationDate(pageList, wiki, true);
        Assert.assertArrayEquals(new String[] {"Page A", "Page X", "Page ZZZ"},
                sorted.toArray());
    }

    /**
     * Test method for {@link ArchiveTools#main(java.lang.String[])}.
     */
    @Test
    public void testMain() throws IOException {
        TestWikiFactory.WikiFactoryDelegate delegate = new TestWikiFactory.WikiFactoryDelegate() {

            @Override
            public void initWiki(MockNirvanaWiki wiki) {
                wiki.mockPageInfo("page A", mockPageInfo(1L, "Page A"));
                wiki.mockPageInfo("page X", mockPageInfo(25L, "Page X"));
                wiki.mockPageInfo("page Z", mockPageInfo(27L, "Page ZZZ"));                
            }
            
        };
        TestWikiFactory wikiFactory = new TestWikiFactory(delegate);
        File inputFile = new File(folder.getRoot(), "input.txt");
        File outFile = new File(folder.getRoot(), "output.txt");
        FileTools.writeFile("page Z\npage A\npage X", inputFile);
        ArchiveTools.mainImpl(wikiFactory,
                new String[] {inputFile.getPath(), outFile.getPath(), "-asc"});
        String out = FileTools.readFile(outFile);
        Assert.assertEquals("Page A\r\nPage X\r\nPage ZZZ\r\n", out);

        ArchiveTools.mainImpl(wikiFactory,
                new String[] {inputFile.getPath(), outFile.getPath(), "-desc"});
        out = FileTools.readFile(outFile);
        Assert.assertEquals("Page ZZZ\r\nPage X\r\nPage A\r\n", out);
    }
}
