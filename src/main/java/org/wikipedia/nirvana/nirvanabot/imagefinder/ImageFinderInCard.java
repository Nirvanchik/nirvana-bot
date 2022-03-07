/**
 *  @(#)ImageFinderInCard.java
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

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Searches image in wiki article card.
 * Card - is a special template placed at the top of the article text and that holds the most
 * common (numeric and textual) data about article subject. Often it has also an image.
 */
public class ImageFinderInCard extends BaseImageFinder {
    private Map<String,List<String>> customKeys;
    private List<String> defaultKeys;
    private ImageFinderInTemplates finderInTemplates;

    /**
     * Public constructor.
     */
    public ImageFinderInCard(NirvanaWiki wiki, NirvanaWiki commons, String param) {
        super(wiki, commons);
        customKeys = new HashMap<String,List<String>>();
        defaultKeys = new ArrayList<String>();
        String [] items = param.split(";");
        for (String item: items) {
            item = item.trim();
            if (item.isEmpty()) {
                continue;
            }
            int pos = item.indexOf(":");
            if (pos < 0) {
                defaultKeys = parseKeys(item);
            } else if (pos == 0) {
                defaultKeys = parseKeys(item.substring(1));
            } else {
                String template = item.substring(0,pos);
                customKeys.put(template, parseKeys(item.substring(pos + 1)));
            }
        }
        log.debug(customKeys.toString());
        log.debug(defaultKeys.toString());
        finderInTemplates = new ImageFinderInTemplates();
    }
    
    private List<String> parseKeys(String keysList) {
        ArrayList<String> list = new ArrayList<String>();
        String [] keys = keysList.split(",");
        for (String key: keys) {
            list.add(key.trim());
        }
        return list;
    }

    @Override
    public String findImage(String wikiText) throws IOException {
        for (Map.Entry<String, List<String>> entry:customKeys.entrySet()) {
            String template = entry.getKey();
            if (wikiText.contains("{{" + template) && entry.getValue().size() > 0) {
                String regexToFindImage = "\\|\\s*(" +
                        StringUtils.join(entry.getValue(), "|") +
                        ")\\s*=\\s*(?<filename>.+?)\\s*\n";
                //log.debug("regex = "+regexToFindImage);
                String image = checkImage(wikiText, regexToFindImage);
                if (image != null) {
                    return image;
                }
            }
        }        
        if (defaultKeys.size() > 0) {
            final String defaultKeysRe = StringUtils.join(defaultKeys, "|");
            String regexToFindImage =
                    "\\|\\s*(" + defaultKeysRe + ")\\s*=\\s*(?<filename>.+?)\\s*\n";
            return checkImage(wikiText, regexToFindImage);
        }
        return null;
    }

    private String findImageByRegexSimple(String text, Pattern pattern) throws IOException {
        return findImageByRegex(text, pattern, DEFAULT_REGEX_IMAGE_TAG, false);
    }

    // TODO: Stop using dummy regex here and rewrite this shit code.
    //     RE like "|image=(.+?)\n" does not work for complex code where image is enveloped into
    //     multiline template. It also badly works for one-liner templates.
    //     So to parse this it is a bad idea to use simple regex.
    //     We should parse template code into key/value pairs and iterate through it.
    //     In the iterated value we can check if image is template and should be parsed again.
    private String checkImage(String wikiText, String regexToFindImage) throws IOException {
        Matcher m = Pattern.compile(
                regexToFindImage,
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE
                ).matcher(wikiText);
        while (m.find()) {
            String image = StringUtils.strip(m.group(DEFAULT_REGEX_IMAGE_TAG).trim());
            log.debug("Image: {}", image);
            // TODO(Nirvanchik): This way we can miss images. For example we found 1 image,
            // it happened to be non-existing and we return with null and don't check what
            // it is further in the article text
            // This should be made with using some kind of iterator rather then just return value
            if (image.isEmpty()) {
                continue;
            }
            if (image.contains("{{")) {
                image = finderInTemplates.checkImageIsImageTemplate(image);
            } else if (image.contains("[[")) {
                image = findImageByRegexSimple(image, ImageFinderInBody.PATTERN_TO_FIND_IMAGE);
            }
            if (image == null || image.isEmpty()) {
                continue;
            }
            if (image.contains("|")) {
                // One-liner template case
                // Example: {{персона|изображение = Jirina-sedlackova.jpg|other param = 100500}}
                image = StringUtils.strip(image.substring(0, image.indexOf('|')));
            }
            if (image.isEmpty()) {
                continue;
            }
            // TODO: May be rewrite this with a better fix or better RE?
            // TODO: Cover this in tests.
            // TODO: Add right trim.
            // Workaround against [#73]
            if (image.contains("}}")) {
                // One-liner template case
                // Example: {{персона|изображение = Jirina-sedlackova.jpg}}
                image = StringUtils.strip(image.substring(0, image.indexOf("}}")));
            }
            if (image.contains(">") || image.contains("<")) {
                continue;
            }
            if (checkImageExists(image)) {
                return image;
            }            
        }
        return null;
    }

}
