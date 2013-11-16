/**
 *  @(#)ImageFinderUniversal.java 23/08/2012
 *  Copyright © 2012 Dmitry Trofimovich (KIN)
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

package org.wikipedia.nirvana.nirvanabot.imagefinder;

import java.io.IOException;

import org.wikipedia.nirvana.NirvanaWiki;

/**
 * @author kin
 *
 */
public class ImageFinderUniversal extends ImageFinder {
	ImageFinderInCard finder1;
	ImageFinderInBody finder2;

	/**
	 * 
	 */
	public ImageFinderUniversal(String param) {
		finder1 = new ImageFinderInCard(param);
		finder2 = new ImageFinderInBody();
	}

	/* (non-Javadoc)
	 * @see org.wikipedia.nirvana.nirvanabot.ImageFinder#findImage(org.wikipedia.nirvana.NirvanaWiki, java.lang.String)
	 */
	@Override
	public String findImage(NirvanaWiki wiki, NirvanaWiki commons, String article)
			throws IOException {
		String image = finder1.findImage(wiki, commons, article);
		if(image!=null)
			return image;
		image = finder2.findImage(wiki, commons, article);
		return image;
	}

}
