/**
 *  @(#)WikiFactory.java
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

package org.wikipedia.nirvana.wiki;

import org.wikipedia.Wiki;

/**
 * Factory to create Wiki instance.
 *
 */
public abstract class WikiFactory {
    /**
     * Creates Wiki class instance for the specified domain.
     *
     * @param domain the wiki domain name e.g. en.wikipedia.org (defaults to
     *     en.wikipedia.org)
     * @return the created wiki
     */
    public abstract Wiki createInstance(String domain);
    
    /**
     * Creates instance of default Wiki class.
     */
    public static WikiFactory ORIGINAL = new WikiFactory() {

        @Override
        public Wiki createInstance(String domain) {            
            return Wiki.createInstance(domain);
        }
        
    };
    
    /**
     * Creates instance of NirvanaWiki class (extended version of Wiki).
     */
    public static WikiFactory NIRVANA = new WikiFactory() {
        
        @Override
        public Wiki createInstance(String domain) {
            return new NirvanaWiki(domain);
        }
    };
}
