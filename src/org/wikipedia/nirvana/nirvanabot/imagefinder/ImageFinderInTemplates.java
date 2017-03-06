/**
 *  @(#)ImageFinderInTemplates.java 17.02.2017
 *  Copyright © 2017 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks that image that was specified as template parameter (f.e. "|image=Image1.png") in card
 * description is not a bare image but image folded into template (which trancates a section f.e.).
 *
 */
public class ImageFinderInTemplates {
    private static final Pattern IMAGE_PART_TEMPLATE_IMAGE_FIND_REGEX = 
            Pattern.compile("\\|\\s*изобр\\s*=\\s*(?<image>[^\\|\\}]+)");

    /**
     * @return image name or null if template found but image not specified as parameter.
     */
    public String checkImageIsImageTemplate(String image) {
        if (image.contains("{{часть изображения")) {
            // {{часть изображения|изобр=UC1755381 Subularia aquatica var. americana.jpg
            // |позиция=center|ширина=280|общая=800|верх=470|низ=400|лево=60|рамка=нет|помехи=да}}
            Matcher m1 = IMAGE_PART_TEMPLATE_IMAGE_FIND_REGEX.matcher(image);
            if (m1.find()) {
                image = m1.group("image").trim();
            } else {
                image = null;
            }
        } else if (image.contains("{{") || image.contains("}}")) {
            image = null;
        }
        return image;
    }
}
