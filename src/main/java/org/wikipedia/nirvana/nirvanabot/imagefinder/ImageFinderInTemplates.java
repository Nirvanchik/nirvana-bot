/**
 *  @(#)ImageFinderInTemplates.java
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
 * This file is encoded with UTF-8.
 * */

package org.wikipedia.nirvana.nirvanabot.imagefinder;

import org.wikipedia.nirvana.localization.LocalizedTemplate;
import org.wikipedia.nirvana.localization.Localizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks that image that was specified as template parameter (f.e. "|image=Image1.png") in card
 * description is not a bare image but image folded into template (which trancates a section f.e.).
 *
 */
public class ImageFinderInTemplates {
    private final String templateStart;
    private final Pattern imageFindRe; 

    /**
     * Constructs object for the current localization set in Localizer.
     */
    public ImageFinderInTemplates() {
        Localizer localizer = Localizer.getInstance();
        LocalizedTemplate template = localizer.localizeTemplateStrict("часть изображения");
        if (template != null) {
            templateStart = "{{" + template.localizeName();
            imageFindRe = Pattern.compile("\\|\\s*" +
                    template.localizeParam("изобр") + "\\s*=\\s*(?<image>[^\\|\\}]+)");
        } else {
            templateStart = null;
            imageFindRe = null;
        }
    }

    /**
     * @return image name or null if template found but image not specified as parameter.
     */
    public String checkImageIsImageTemplate(String image) {
        if (image.contains("{{") || image.contains("}}")) {
            if (templateStart == null) return null;
            if (image.contains(templateStart)) {
                // {{часть изображения|изобр=UC1755381 Subularia aquatica var. americana.jpg
                // |позиция=center|ширина=280|общая=800|верх=470|низ=400|лево=60|рамка=нет
                // |помехи=да}}
                Matcher m1 = imageFindRe.matcher(image);
                if (m1.find()) {
                    image = m1.group("image").trim();
                    return image;
                }
            }
            return null;
        }
        return image;
    }
}
