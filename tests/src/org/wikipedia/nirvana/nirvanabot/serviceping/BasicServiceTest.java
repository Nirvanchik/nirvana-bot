/**
 *  @(#)BasicServiceTest.java 13.03.2016
 *  Copyright © 2016 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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
 * Recommended code page for this file is CP1251 (also called Windows-1251).
 * */

package org.wikipedia.nirvana.nirvanabot.serviceping;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService.Status;

/**
 * @author kin
 *
 */
public class BasicServiceTest {
	
	private static class TestService extends BasicService {
		private boolean checkOkCalled = false;
		
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

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void callsCheckOk_firstCall() throws InterruptedException {
		TestService service = new TestService("service");
		service.isOk();
		assertTrue(service.checkOkCalled);
	}

	@Test
	public void callsCheckOk_afterReset() throws InterruptedException {
		TestService service = new TestService("service");
		service.isOk();

		service.resetFromTest();
		service.resetCache();
		service.isOk();
		assertTrue(service.checkOkCalled);
	}

	@Test
	public void caching() throws InterruptedException {
		TestService service = new TestService("service");
		service.checkOkReturnVal = true;
		service.isOk();

		service.resetFromTest();
		assertEquals(Status.OK, service.isOk());
		assertFalse(service.checkOkCalled);

		service = new TestService("service");
		service.checkOkReturnVal = false;
		service.isOk();

		service.resetFromTest();
		assertEquals(Status.FAIL, service.isOk());
		assertFalse(service.checkOkCalled);
	}
	
	@Test
	public void dependency_ok() throws InterruptedException {
		TestService parent = new TestService("service");
		TestService service = new TestService("service");
		service.dependsOn(parent);
		parent.checkOkReturnVal = true;
		service.checkOkReturnVal = true;
		assertEquals(Status.OK, service.isOk());
		assertTrue(parent.checkOkCalled);
	}
	
	@Test
	public void dependency_fail() throws InterruptedException {
		TestService parent = new TestService("service");
		TestService service = new TestService("service");
		service.dependsOn(parent);
		parent.checkOkReturnVal = false;
		service.checkOkReturnVal = true;
		assertEquals(Status.UNKNOWN, service.isOk());
		assertTrue(parent.checkOkCalled);
	}
	
	@Test
	public void lastError() {
		TestService service = new TestService("service");
		service.setLastError("error1");
		assertEquals("error1", service.getLastError());
		
		service.resetCache();
		assertEquals("", service.getLastError());
	}

}
