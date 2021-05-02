/**
 *  @(#)BaseImageFinder.java
 *  Copyright © 2021 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * Implements the most common image searching logic using specified search pattern and wiki text.
 *
 */
public abstract class BaseImageFinder implements ImageFinder {
    public static final String DEFAULT_REGEX_IMAGE_TAG = "filename";
    protected final Logger log;

    @Nullable
    protected NirvanaWiki wiki;
    @Nullable
    protected NirvanaWiki commons;

    /**
     * Public constructor.
     */
    public BaseImageFinder(NirvanaWiki wiki, NirvanaWiki commons) {
        super();
        this.wiki = wiki;
        this.commons = commons;
        log = LogManager.getLogger(this.getClass().getName());
    }

    protected String findImageByRegex(String wikiText, Pattern pattern, String tag,
            boolean checkExists) throws IOException {
        if (wikiText == null) {
            throw new NullPointerException("wikiText argument must not be null");
        }
        if (wikiText.isEmpty()) {
            return null;
        }
        Matcher m = pattern.matcher(wikiText);
        while (m.find()) {
            String image = m.group(tag).trim();
            
            log.debug("image: {}", image);

            if (image != null && !image.isEmpty() && !image.contains(">") && !image.contains("<")) {
                // некоторые товарищи умудряются вставлять изображение с |
                // например: Equisetum.palustre.jpg|Equisetum.palustre
                // в итоге выпрыгивает IOException
                if (image.contains("|")) {
                    image = image.substring(0, image.indexOf('|'));
                }
                if (checkExists && !checkImageExists(image)) {
                    continue;
                }
                return image;
            }
        }
        return null;
    }

    /**
     * Checks that image exists in this wiki or in the Commons wiki.
     * Image with free licenses are usually placed in Commons.
     */
    protected boolean checkImageExists(String image) throws IOException {
        if (wiki == null) {
            throw new NullPointerException("wiki argument must not be null");
        }
        if (commons == null) {
            throw new NullPointerException("commons argument must not be null");
        }
        // здесь мы обрабатываем случаи когда попадается картинка с URL-кодами
        // например Haloragis_erecta_2007-06-02_%28plant%29.jpg
        image = java.net.URLDecoder.decode(image, "UTF-8");
        if (wiki.exists("File:" + image)) {
            return true;
        }
        if (commons.exists("File:" + image)) {
            return true;
        }
        return false;
    }
}
