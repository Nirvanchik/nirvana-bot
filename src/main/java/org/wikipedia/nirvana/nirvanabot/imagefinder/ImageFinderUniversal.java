/**
 *  @(#)ImageFinderUniversal.java
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

import org.wikipedia.nirvana.wiki.NirvanaWiki;

import java.io.IOException;

/**
 * Searches image in all places of wiki page - in page text and in article card.
 */
public class ImageFinderUniversal implements ImageFinder {
    ImageFinderInCard finder1;
    ImageFinderInBody finder2;

    /**
     * Public constructor.
     */
    public ImageFinderUniversal(NirvanaWiki wiki, NirvanaWiki commons, String param) {
        super();
        finder1 = new ImageFinderInCard(wiki, commons, param);
        finder2 = new ImageFinderInBody(wiki, commons);
    }

    @Override
    public String findImage(String wikiText) throws IOException {
        String image = finder1.findImage(wikiText);
        if (image != null) {
            return image;
        }
        image = finder2.findImage(wikiText);
        return image;
    }
}
