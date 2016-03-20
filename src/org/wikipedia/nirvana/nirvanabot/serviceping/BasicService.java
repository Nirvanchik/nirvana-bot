/**
 *  @(#)BasicService.java 12.03.2016
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



/**
 * @author kin
 *
 */
public class BasicService implements OnlineService {
	protected static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BasicService.class.getName());
	BasicService dependantOn = null;
	private String name;
	@Deprecated
	private int priority = 0;
	private Status cachedStatus = null;
	private String lastError = null;

	/**
	 * 
	 */
	@Deprecated
	public BasicService(String name, int priority) {
		this.name = name;
		this.priority = priority;
	}

	public BasicService(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService#getName()
	 */
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	protected boolean checkOk() throws InterruptedException {		
		return true;
	}
	
	public boolean checkParentWorking() throws InterruptedException {
		return (dependantOn == null || dependantOn.isOk() == Status.OK);
	}

	/* (non-Javadoc)
	 * @see org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService#isReplacable()
	 */
	@Override
	public boolean isReplacable() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService#getReplacement()
	 */
	@Override
	public boolean replace() throws InterruptedException {
		return false;
	}

	/* (non-Javadoc)
     * @see org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService#getPriority()
     */
    @Override
    public int getPriority() {
	    return priority;
    }
    
    public BasicService dependsOn(BasicService service) {
    	dependantOn = service;
    	return this;
    }

	/* (non-Javadoc)
     * @see org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService#resetCache()
     */
    @Override
    public void resetCache() {
	    cachedStatus = null;
	    lastError = null;
    }

	/* (non-Javadoc)
     * @see org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService#isReplaced()
     */
    @Override
    public boolean isReplaced() {
	    return false;
    }

	/* (non-Javadoc)
     * @see org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService#recover()
     */
    @Override
    public void recover() throws InterruptedException {
    }
    
    @Override
    public Status isOk() throws InterruptedException {
/*    	Status status = isAvailable();
    	if (status != Status.OK) {
    		return status;
    	}
    	status = isWorking();
    	if (status == Status.UNKNOWN) {
    		return Status.FAIL;
    	}
    	return status;*/
    	if (cachedStatus != null) {
			return cachedStatus;
		}
		if (!checkParentWorking()) {
			cachedStatus = Status.UNKNOWN;
			return cachedStatus;
		}
		if (checkOk()) {
			cachedStatus = Status.OK;
		} else {
			cachedStatus = Status.FAIL;
		}
		return cachedStatus;
    }

	/* (non-Javadoc)
     * @see org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService#getDetailedStatus()
     */
    /*
    @Override
    public String getDetailedStatus() {
    	if (cachedStatus != null) {
    		return cachedStatus.toString();
    	}
	    return "";
    }*/

	/* (non-Javadoc)
     * @see org.wikipedia.nirvana.nirvanabot.serviceping.OnlineService#getLastError()
     */
    @Override
    public String getLastError() {
    	if (lastError == null) {
    		return "";
    	}
	    return lastError;
    }
    
    public void setLastError(String error) {
    	lastError = error;
    }
}