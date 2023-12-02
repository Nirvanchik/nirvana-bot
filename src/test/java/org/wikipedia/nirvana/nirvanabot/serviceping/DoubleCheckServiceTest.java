/**
 *  @(#)DoubleCheckServiceTest.java
 *  Copyright © 2023 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService.Status;

import org.junit.Test;

/**
 * Unit tests for {@link DoubleCheckService}.
 *
 */
public class DoubleCheckServiceTest {

    private static class TestService extends DoubleCheckService {
        private boolean checkAvailableCalled = false;
        private boolean checkWoringCalled = false;

        boolean checkAvailableReturnVal = true;
        boolean checkWorkingReturnVal = true;

        public TestService(String name) {
            super(name);
        }

        @Override
        protected boolean checkAvailable() {
            checkAvailableCalled = true;
            return checkAvailableReturnVal;
        }

        @Override
        protected boolean checkWorking() {
            checkWoringCalled = true;
            return checkWorkingReturnVal;
        }
    }

    @Test
    public void bothChecksAreCalled() throws InterruptedException {
        TestService service = new TestService("service");
        service.isOk();
        assertTrue(service.checkAvailableCalled);
        assertTrue(service.checkWoringCalled);
    }

    @Test
    public void isWorkingDependsOnIsAvailable() throws InterruptedException {
        TestService service = new TestService("service");
        service.checkAvailableReturnVal = false;
        service.checkWorkingReturnVal = true;
        assertFalse(Status.OK == service.isOk());
    }

}
