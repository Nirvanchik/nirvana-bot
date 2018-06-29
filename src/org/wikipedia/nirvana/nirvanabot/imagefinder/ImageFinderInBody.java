/**
 *  @(#)ImageFinderInBody.java 23/08/2012
 *  Copyright © 2013 Dmitry Trofimovich (KIN)(DimaTrofimovich@gmail.com)
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
import java.util.regex.Pattern;

import org.wikipedia.nirvana.NirvanaWiki;

/**
 * @author kin
 *
 */
public class ImageFinderInBody extends ImageFinder {
    static final String REGEX_TO_FIND_IMAGE =
            "\\[\\[(Image|File|Файл|Изображение):(?<filename>[^\\|\\]]+)";
	static final Pattern PATTERN_TO_FIND_IMAGE = Pattern.compile(REGEX_TO_FIND_IMAGE);

	/**
	 * 
	 */
	public ImageFinderInBody() {
		// nothing to do
	}

	@Override
	public String findImage(NirvanaWiki wiki, NirvanaWiki commons, String article) throws IOException {
        return findImageByRegex(wiki, commons, null, article, PATTERN_TO_FIND_IMAGE, "filename");
	}
	

}
