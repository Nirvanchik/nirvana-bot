/**
 *  @(#)DoubleCheckService.java
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

/**
 * Abstract service with double check.
 *
 */
public abstract class DoubleCheckService extends BasicService {

    /**
     * Construct service instance with specified name.
     */
    public DoubleCheckService(String name) {
        super(name);
    }

    protected abstract boolean checkAvailable();

    protected abstract boolean checkWorking() throws InterruptedException;

    @Override
    protected boolean checkOk() throws InterruptedException {
        if (!checkAvailable()) {
            return false;
        }
        return checkWorking();
    }
}
