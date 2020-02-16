/**
 *  @(#)TestServiceManager.java
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

package org.wikipedia.nirvana.nirvanabot.serviceping;

import org.wikipedia.nirvana.nirvanabot.BotFatalError;
import org.wikipedia.nirvana.nirvanabot.ServiceManager;
import org.wikipedia.nirvana.wiki.CatScanTools;

/**
 * {@link ServiceManager} replacement for some tests that require interacting with real
 * ServiceManager and may be ServicePinger code but mocked {@link
 * org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService} placeholders.
 *
 */
public class TestServiceManager extends ServiceManager {

    /**
     * Constructor for testing.
     */
    public TestServiceManager(InternetService internet, WikiService mainWiki,
            CatscanService catscanService, ServicePinger servicePinger,
            CatScanTools.Service activeService) {
        super(internet, mainWiki, servicePinger);
        catscan = catscanService;
        this.activeService = activeService;
    }

    @Override
    public void updateCatScan(String defaultServiceName, String selectedServiceName)
            throws BotFatalError {
        log.info("updateCatScan() called with args: {}, {}", defaultServiceName,
                selectedServiceName);
    }

}
