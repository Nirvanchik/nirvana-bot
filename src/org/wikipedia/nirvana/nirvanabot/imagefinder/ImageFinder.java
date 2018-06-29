/**
 *  @(#)ImageFinder.java 23/08/2012
 *  Copyright © 2013 - 2014 Dmitry Trofimovich (KIN)(DimaTrofimovich@gmail.com)
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wikipedia.nirvana.NirvanaWiki;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author kin
 *
 */
public abstract class ImageFinder {
    protected static final Logger log;

    static {
        log = LogManager.getLogger(ImageFinder.class.getName());
    }

    public ImageFinder() {
    }

	public abstract String findImage(NirvanaWiki wiki, NirvanaWiki commons, String article) throws IOException;
	public static final String DEFAULT_REGEX_IMAGE_TAG = "filename";

    public static String findImageByRegex(NirvanaWiki wiki, NirvanaWiki commons,
            ImageFinderInTemplates finderInTemplates, String article, Pattern pattern, String tag)
                    throws IOException {
        return findImageByRegex(wiki, commons, finderInTemplates, article, pattern, tag, true, true);
	}

	public static String findImageByRegexSimple(String text, Pattern pattern) throws IOException {
        return findImageByRegex(null, null, null, text, pattern, DEFAULT_REGEX_IMAGE_TAG, false,
                false);
	}

    public static String findImageByRegex(NirvanaWiki wiki, NirvanaWiki commons,
            ImageFinderInTemplates finderInTemplates, String article, Pattern pattern, String tag,
            boolean checkImageTemplates, boolean checkExists) throws IOException {
		if (article == null || article.isEmpty()) {
			return null;
		}
		Matcher m = pattern.matcher(article);
        while(m.find()) {
        	String image = m.group(tag).trim();
            // иногда вставляют не просто изображение а шаблон {{часть изображения}}
            if (checkImageTemplates && finderInTemplates != null) {
                image = finderInTemplates.checkImageIsImageTemplate(image);
        	}
    		
        	log.debug("image: "+image);

        	if(image!=null && !image.isEmpty() && !image.contains(">") && !image.contains("<")) {
                // некоторые товарищи умудряются вставлять изображение с |
                // например: Equisetum.palustre.jpg|Equisetum.palustre
                // в итоге выпрыгивает IOException
        		if(image.contains("|")) {
        			image = image.substring(0, image.indexOf('|'));
        		}
        		if (checkExists && !checkImageExists(wiki, commons, image)) {
        			continue;
        		}
        		return image;
        	}
        }
		return null;
	}

	protected static boolean checkImageExists(NirvanaWiki wiki, NirvanaWiki commons, String image) throws IOException {
        // здесь мы обрабатываем случаи когда попадается картинка с URL-кодами
        // например Haloragis_erecta_2007-06-02_%28plant%29.jpg
		image = java.net.URLDecoder.decode(image, "UTF-8");
		if (wiki.exists("File:"+image)) {
			return true;
		}
		if (commons.exists("File:"+image)) {
			return true;
		}
		return false;
	}
}
