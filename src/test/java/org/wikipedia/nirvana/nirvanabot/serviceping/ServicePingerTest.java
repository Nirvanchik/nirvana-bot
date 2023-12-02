/**
 *  @(#)ServicePingerTest.java
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.wikipedia.nirvana.nirvanabot.serviceping.ServicePinger.RECHECK_1_TIMEOUT;
import static org.wikipedia.nirvana.nirvanabot.serviceping.ServicePinger.RECHECK_DELAY_1;
import static org.wikipedia.nirvana.nirvanabot.serviceping.ServicePinger.RECHECK_DELAY_2;
import static org.wikipedia.nirvana.nirvanabot.serviceping.ServicePinger.TIMEOUT;

import org.wikipedia.nirvana.nirvanabot.serviceping.ServicePinger.AfterDowntimeCallback;
import org.wikipedia.nirvana.nirvanabot.serviceping.ServicePinger.ServiceWaitTimeoutException;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

/**
 * Unit tests for {@link ServicePinger}.
 *
 */
public class ServicePingerTest {

    private static class TestService extends BasicService {
        protected boolean checkOkCalled = false;
        
        boolean checkOkReturnVal = true;

        public TestService(String name) {
            super(name);
        }
        
        @Override
        protected boolean checkOk() {
            checkOkCalled = true;
            return checkOkReturnVal;
        }
        
        public void resetFromTest() {
            checkOkCalled = false;
        }
        
    }

    private static class TestServicesManager extends MockServicePinger {
        private TestService failingService = null;
        private long timeToRecover = -1;

        public TestServicesManager(OnlineService... servicesList) {
            super(servicesList);
        }
        
        public void setFailingServiceAndTimeToRecover(TestService failingService,
                long timeToRecover) {
            this.failingService = failingService;
            this.timeToRecover = timeToRecover;
        }

        @Override
        protected void sleep(long millis) throws InterruptedException {
            super.sleep(millis);
            if (failingService != null && timeToRecover != -1 && currentTime >= timeToRecover) {
                failingService.checkOkReturnVal = true;
            }
        }

    }

    @Test
    public void ok_whenAllServicesOk() throws InterruptedException {
        TestService service1 = new TestService("service1");
        TestService service2 = new TestService("service2");
        TestService service3 = new TestService("service3");
        ServicePinger manager = new ServicePinger(service1, service2, service3);
        Assert.assertTrue(manager.isOk());
    }

    @Test
    public void fail_whenAnyServiceFails() throws InterruptedException {
        TestService service1 = new TestService("service1");
        TestService service2 = new TestService("service2");
        TestService service3 = new TestService("service3");
        ServicePinger manager = new ServicePinger(service1, service2, service3);
        service1.checkOkReturnVal = false;
        Assert.assertFalse(manager.isOk());
        service1.checkOkReturnVal = true;
        service2.checkOkReturnVal = false;
        Assert.assertFalse(manager.isOk());
    }

    @Test
    public void failsAndReturnsLastFailedService() throws InterruptedException {
        TestService service1 = new TestService("service1");
        TestService service2 = new TestService("service2");
        TestService service3 = new TestService("service3");
        ServicePinger manager = new ServicePinger(service1, service2, service3);
        service2.checkOkReturnVal = false;
        Assert.assertFalse(manager.isOk());
        OnlineService service = manager.getLastFailedService();
        Assert.assertEquals(service2, service);
    }
    
    @Test
    public void eachServicesCheckIsReal() throws InterruptedException {
        TestService service1 = new TestService("service1");
        TestService service2 = new TestService("service2");
        TestService service3 = new TestService("service3");
        ServicePinger manager = new ServicePinger(service1, service2, service3);
        manager.isOk();
        service1.resetFromTest();
        service2.resetFromTest();
        service3.resetFromTest();
        Assert.assertTrue(manager.isOk());
        Assert.assertTrue(service1.checkOkCalled && service2.checkOkCalled &&
                service3.checkOkCalled);
    }

    @Test
    public void resolveProblemsByWaiting_shortTime() throws Exception {
        TestService service1 = new TestService("service1");
        TestService service2 = new TestService("service2");
        TestService service3 = new TestService("service3");
        TestServicesManager manager = new TestServicesManager(service1, service2, service3);
        service2.checkOkReturnVal = false;
        manager.setFailingServiceAndTimeToRecover(service2, RECHECK_1_TIMEOUT / 2);
        Assert.assertFalse(manager.isOk());
        long waitTime = manager.waitServicesOk();
        Assert.assertTrue(waitTime > RECHECK_DELAY_1 && waitTime < RECHECK_1_TIMEOUT);
        List<Long> times = manager.getTimeTicks();
        Assert.assertTrue(times.size() > 0);
        for (Long time:times) {
            Assert.assertTrue(time > 0 && time < RECHECK_1_TIMEOUT);
        }
    }

    @Test
    public void resolveProblemsByWaiting_longTime() throws Exception {
        TestService service1 = new TestService("service1");
        TestService service2 = new TestService("service2");
        TestService service3 = new TestService("service3");
        TestServicesManager manager = new TestServicesManager(service1, service2, service3);
        service2.checkOkReturnVal = false;
        manager.setFailingServiceAndTimeToRecover(service2, TIMEOUT / 2);
        Assert.assertFalse(manager.isOk());
        long waitTime = manager.waitServicesOk();
        Assert.assertTrue(
                waitTime > RECHECK_DELAY_1 &&
                waitTime > RECHECK_DELAY_2 &&
                waitTime > RECHECK_1_TIMEOUT &&
                waitTime < TIMEOUT);
        List<Long> times = manager.getTimeTicks();
        Assert.assertTrue(times.size() > 0);
        long max = 0;
        boolean timeEncreased = false;
        for (Long time:times) {
            Assert.assertTrue(time > 0 && time < TIMEOUT);
            Assert.assertTrue(time >= max);
            if (time > max) {
                if (max != 0) {
                    timeEncreased = true;
                }
                max = time;
            }
        }
        Assert.assertTrue(timeEncreased);
    }

    @Test(expected = ServiceWaitTimeoutException.class)
    public void resolveProblemsByWaiting_timeout() throws Exception {
        TestService service1 = new TestService("service1");
        TestService service2 = new TestService("service2");
        TestService service3 = new TestService("service3");
        TestServicesManager manager = new TestServicesManager(service1, service2, service3);
        service2.checkOkReturnVal = false;
        manager.setFailingServiceAndTimeToRecover(service2, TIMEOUT * 2);
        Assert.assertFalse(manager.isOk());
        manager.waitServicesOk();
    }

    @Test
    public void callCallbackAfterDowntime() throws Exception {
        TestService service1 = new TestService("service1");
        TestService service2 = new TestService("service2");
        TestService service3 = new TestService("service3");
        TestServicesManager manager = new TestServicesManager(service1, service2, service3);
        AfterDowntimeCallback callback = mock(AfterDowntimeCallback.class);
        manager.setAfterDowntimeCallback(callback);
        service2.checkOkReturnVal = false;
        manager.setFailingServiceAndTimeToRecover(service2, RECHECK_1_TIMEOUT / 2);
        Assume.assumeFalse(manager.isOk());

        manager.waitServicesOk();

        verify(callback, times(1)).afterDowntime(Mockito.anyLong());
    }

}
