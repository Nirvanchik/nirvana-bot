/**
 *  @(#)NewPagesTest.java 12.03.2017
 *  Copyright © 2017 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.nirvanabot;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.localization.TestLocalizationManager;
import org.wikipedia.nirvana.nirvanabot.NirvanaBot.BotGlobalSettings;
import org.wikipedia.nirvana.nirvanabot.report.ReportItem;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;


/**
 * Unit tests for {@link NewPages}.
 */
public class NewPagesTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        TestLocalizationManager.init(Localizer.DEFAULT_LOCALIZATION);
    }

    @After
    public void tearDown() throws Exception {
        TestLocalizationManager.reset();
        NewPages.resetFromTests();
    }
   
    private PortalParam makeTestParam() {
        PortalParam param = new PortalParam();
        return param;
    }

    // TODO: Finish this test when it is possible to mock PageListFetcher and PageListProcessor.
    @Ignore()
    @Test(expected = DangerousEditException.class)
    public void cancelEditWhenDangerous() throws Exception {
        NirvanaWiki wiki = Mockito.mock(NirvanaWiki.class);
        String oldText = "* item 1\n* item 2\n* item3";
        when(wiki.getPageText(anyString())).thenReturn(oldText);
        when(wiki.getTopRevision(anyString())).thenReturn(null);
        ReportItem reportData = Mockito.mock(ReportItem.class);
        PortalParam param = this.makeTestParam();
        SystemTime systemTime = new SystemTime();
        PageFormatter pageFormatter = new PageFormatter(param, "User:Bot1:Settings",
                "Portal:PortalA/New Pages", "", new BotGlobalSettings(), wiki, systemTime, "");
        NewPages newPages = new NewPages(param, pageFormatter, systemTime);
        //newPages.update(wiki, reportData, "comment");
    }
}
