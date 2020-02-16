/**
 *  @(#)MockServicePinger.java
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

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ServicePinger} for tests.
 *
 * Just a wrapper that replaces real time ticks with fake ones.
 *
 */
public class MockServicePinger extends ServicePinger {
    protected long currentTime;
    private List<Long> waitTimeTicks = new ArrayList<>();

    /**
     * Public constructor.
     */
    public MockServicePinger(OnlineService... servicesList) {
        super(servicesList);
    }

    @Override
    protected long currentTimeMillis() {
        return currentTime;
    }

    @Override
    protected void sleep(long millis) throws InterruptedException {
        currentTime += millis;
        waitTimeTicks.add(millis);
    }

    @Override
    public void tryRecoverReplacedServices() throws InterruptedException {
        super.tryRecoverReplacedServices();
    }

    @Override
    public long tryToSolveProblems() throws InterruptedException, ServiceWaitTimeoutException,
            BotFatalError {
        currentTime = 0;
        waitTimeTicks.clear();
        return super.tryToSolveProblems();
    }

    public List<Long> getTimeTicks() {
        return waitTimeTicks;
    }
}
