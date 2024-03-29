/**
 *  @(#)ImageFinder.java
 *  Copyright © 2013 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.nirvanabot.imagefinder;

import java.io.IOException;

/**
 * Interface for image search engines that implement different search logic.
 *
 */
public interface ImageFinder {

    /**
     * Finds first available image in wiki page text.
     *
     * @param wikiText Wiki page text
     * @return image title
     */
    public String findImage(String wikiText) throws IOException;
}
