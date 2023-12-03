/**
 *  @(#)ServiceGroupTest.java
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

import org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService.Status;
import org.wikipedia.nirvana.testing.MockSystemTime;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit tests for {@link ServiceGroup}.
 *
 */
public class ServiceGroupTest {
    MockSystemTime testTime = new MockSystemTime();

    private static class TestService extends BasicService {

        boolean checkOkReturnVal = true;

        public TestService(String name) {
            super(name);
        }

        @Override
        protected boolean checkOk() {
            if (!checkOkReturnVal) {
                setLastError("broken");
            }
            return checkOkReturnVal;
        }

        public void setOk() {
            checkOkReturnVal = true;
        }

        public void setBroken() {
            checkOkReturnVal = false;
        }

    }

    @Before
    public void setup() {
        testTime = new MockSystemTime();
    }

    @Test
    public void okIfActiveServiceOk() throws InterruptedException {
        TestService service1 = new TestService("A");
        TestService service2 = new TestService("B");
        ServiceGroup<TestService> sg = new ServiceGroup<TestService>(testTime, service1, service2);
        Assert.assertEquals(sg.isOk(), Status.OK);
        Assert.assertTrue(testTime.getTimeTicks().isEmpty());
         
        service2.setBroken();
        Assert.assertEquals(sg.isOk(), Status.OK);
    }

    @Test
    public void switchToFallback() throws InterruptedException {
        TestService service1 = new TestService("A");
        TestService service2 = new TestService("B");
        service1.setBroken();
        ServiceGroup<TestService> sg = new ServiceGroup<TestService>(testTime, service1, service2);
        Assert.assertEquals(sg.isOk(), Status.OK);
        Assert.assertTrue(testTime.getTimeTicks().isEmpty());
        Assert.assertEquals("B", sg.getActiveService().getName());         
    }

    @Test
    public void callCallback() throws InterruptedException {
        TestService service1 = new TestService("A");
        TestService service2 = new TestService("B");
        service1.setBroken();
        ServiceGroup<TestService> sg = new ServiceGroup<TestService>(testTime, service1, service2);
        @SuppressWarnings("unchecked")
        ServiceGroup.Listener<TestService> callback = mock(ServiceGroup.Listener.class);
        sg.setListener(callback);
        sg.isOk();
        Assert.assertEquals("B", sg.getActiveService().getName());
        verify(callback, times(1)).onActiveServiceChanged(Mockito.any());
    }

    @Test
    public void switchToMainIfFallbackBroken() throws InterruptedException {
        TestService service1 = new TestService("A");
        TestService service2 = new TestService("B");
        service1.setBroken();
        ServiceGroup<TestService> sg = new ServiceGroup<TestService>(testTime, service1, service2);
        sg.isOk();
        Assert.assertEquals("B", sg.getActiveService().getName());

        service2.setBroken();
        sg.resetCache();
        sg.isOk();
        Assert.assertEquals("A", sg.getActiveService().getName());
    }

    @Test
    public void recover() throws InterruptedException {
        TestService service1 = new TestService("A");
        TestService service2 = new TestService("B");
        service1.setBroken();
        ServiceGroup<TestService> sg = new ServiceGroup<TestService>(testTime, service1, service2);
        sg.isOk();
        Assume.assumeTrue(sg.getActiveService().getName().equals("B"));

        service1.setOk();
        testTime.sleep(15 * 60 * 1000L);
        sg.recover();
        Assert.assertEquals("A", sg.getActiveService().getName());
    }

    @Test
    public void recover_dontCheckTooMuch() throws InterruptedException {
        TestService service1 = Mockito.spy(new TestService("A"));
        TestService service2 = new TestService("B");
        service1.setBroken();
        ServiceGroup<TestService> sg = new ServiceGroup<TestService>(testTime, service1, service2);
        sg.isOk();
        Assume.assumeTrue(sg.getActiveService().getName().equals("B"));

        testTime.sleep(1 * 60 * 1000L);
        sg.recover();
        testTime.sleep(1 * 60 * 1000L);
        sg.recover();
        testTime.sleep(1 * 60 * 1000L);
        sg.recover();
        testTime.sleep(10 * 60 * 1000L);
        sg.recover();

        verify(service1, times(2)).checkOk();
    }
}
