/**
 *  @(#)ImageFinder.java 23/08/2012
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wikipedia.nirvana.NirvanaWiki;

/**
 * @author kin
 *
 */
public abstract class ImageFinder {
	public abstract String findImage(NirvanaWiki wiki, NirvanaWiki commons, String article) throws IOException;
	protected static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ImageFinder.class.getName());
	
	public static String findImageByRegex(NirvanaWiki wiki, NirvanaWiki commons, String article, String regex, String tag) throws IOException {
		Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(article);
        while(m.find()) {
        	String image = m.group(tag).trim();
        	log.debug("image: "+image);
        	if(image!=null && !image.isEmpty() && !image.contains(">") && !image.contains("<")) {
        		// некоторые товарищи умудряются вставлять изображение с | например: Equisetum.palustre.jpg|Equisetum.palustre
        		// в итоге выпрыгивает IOException
        		if(image.contains("|")) {
        			image = image.substring(0, image.indexOf('|'));
        		}
        		// здесь мы обрабатываем случаи когда попадается картинка с URL-кодами
        		// например Haloragis_erecta_2007-06-02_%28plant%29.jpg
        		image = java.net.URLDecoder.decode(image, "UTF-8");
        		String str = null;
        		try {
					str = wiki.getPageText("File:"+image);
				} catch (FileNotFoundException e) {
					//log.debug("image not found");
				}
        		if(str!=null) {
        			return image;
        		}
        		try {
					str = commons.getPageText("File:"+image);
				} catch (FileNotFoundException e) {
					//log.debug("image not found");
				}
        		if(str!=null) {
        			return image;
        		}
        	}
        }
		return null;
	}
	
	ImageFinder() {
		//log = org.apache.log4j.Logger.getLogger(ImageFinder.class.getName());
	}
}
