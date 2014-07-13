/**
 *  @(#)ImageFinderInBody.java 23/08/2012
 *  Copyright © 2014 Dmitry Trofimovich (KIN)(DimaTrofimovich@gmail.com)
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
public class ImageFinderInBody extends ImageFinder {
	private final String regexToFindImage = "\\[\\[(Image|File|Файл|Изображение):(?<filename>.+?)(\\||\\])";

	/**
	 * 
	 */
	public ImageFinderInBody() {
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see org.wikipedia.nirvana.nirvanabot.ImageFinder#findImage(org.wikipedia.nirvana.NirvanaWiki, java.lang.String)
	 *//*
	@Override
	public String findImage(NirvanaWiki wiki, String article) throws IOException {
		Pattern p = Pattern.compile(this.regexToFindImage);
        Matcher m = p.matcher(article);
        while(m.find()) {
        	String image = m.group("filename").trim();
        	if(image!=null && !image.isEmpty()) {
        		String str = null;
        		try {
					str = wiki.getPageText(image);
				} catch (FileNotFoundException e) {
					// nothing
				}
        		if(str!=null) {
        			return image;
        		}
        	}
        }
		return null;
	}*/
	public String findImage(NirvanaWiki wiki, NirvanaWiki commons, String article) throws IOException {
		return findImageByRegex(wiki, commons, article,regexToFindImage,"filename");
	}
	

}
