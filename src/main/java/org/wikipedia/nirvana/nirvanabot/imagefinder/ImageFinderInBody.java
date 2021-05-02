/**
 *  @(#)ImageFinderInBody.java
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
import java.util.regex.Pattern;

/**
 * Searches image in wiki page text.
 */
public class ImageFinderInBody extends BaseImageFinder {
    static final String REGEX_TO_FIND_IMAGE =
            "\\[\\[(Image|File|Файл|Изображение):(?<filename>[^\\|\\]]+)";
    static final Pattern PATTERN_TO_FIND_IMAGE = Pattern.compile(REGEX_TO_FIND_IMAGE);

    /**
     * Public constructor.
     */
    public ImageFinderInBody(NirvanaWiki wiki, NirvanaWiki commons) {
        super(wiki, commons);
    }

    @Override
    public String findImage(String wikiText) throws IOException {
        return findImageByRegex(wikiText, PATTERN_TO_FIND_IMAGE, "filename", true);
    }    
}
