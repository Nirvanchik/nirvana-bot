/**
 *  @(#)ImageFinderInCard.java 23/08/2012
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
 * Recommended code page for this file is CP1251 (also called Windows-1251).
 * */

package org.wikipedia.nirvana.nirvanabot.imagefinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.wikipedia.nirvana.NirvanaWiki;

/**
 * @author kin
 *
 */
public class ImageFinderInCard extends ImageFinder {
	private Map<String,List<String>> customKeys;
	private List<String> defaultKeys;

	/**
	 * 
	 */
	public ImageFinderInCard(String param) {
		customKeys = new HashMap<String,List<String>>();
		defaultKeys = new ArrayList<String>();
		String items[] = param.split(";");
		for(String item:items) {
			item = item.trim();
			if(item.isEmpty()) 
				continue;
			int pos = item.indexOf(":");
			if(pos<0) {
				defaultKeys = parseKeys(item);
			} else if(pos==0) {
				defaultKeys = parseKeys(item.substring(1));
			} else {
				String template = item.substring(0,pos);
				customKeys.put(template, parseKeys(item.substring(pos+1)));
			}
		}
		log.debug(customKeys.toString());
		log.debug(defaultKeys.toString());
	}
	
	private List<String> parseKeys(String keysList) {
		ArrayList<String> list = new ArrayList<String>();
		String keys[] = keysList.split(",");
		for(String key:keys) {
			list.add(key.trim());
		}
		return list;
	}

	/* (non-Javadoc)
	 * @see org.wikipedia.nirvana.nirvanabot.ImageFinder#findImage(org.wikipedia.nirvana.NirvanaWiki, java.lang.String)
	 */
	@Override
	public String findImage(NirvanaWiki wiki, NirvanaWiki commons, String article)
			throws IOException {
		for(Map.Entry<String, List<String>> entry:customKeys.entrySet()) {
			String template = entry.getKey();
			if(article.contains("{{"+template) && entry.getValue().size()>0) {
				String regexToFindImage = "\\| *("+
						StringUtils.join(entry.getValue(),"|")+
						") *= *(?<filename>.+?) *\n";
				//log.debug("regex = "+regexToFindImage);
				String image = checkImage(wiki,commons,article,regexToFindImage);
				if (image != null) {
					return image;
				}
			}
		}		
		if(defaultKeys.size()>0) {
			String regexToFindImage = "\\| *("+
					StringUtils.join(defaultKeys,"|")+
					") *= *(?<filename>.+?) *\n";
			return checkImage(wiki,commons,article,regexToFindImage);
		}
		return null;
	}

	private String checkImage(NirvanaWiki wiki, NirvanaWiki commons, String article, String regexToFindImage) throws IOException {
		Matcher m = Pattern.compile(regexToFindImage).matcher(article);
		while(m.find()) {
        	String image = m.group(DEFAULT_REGEX_IMAGE_TAG).trim();
        	log.debug("image: "+image);
/*        	String image = findImageByRegexSimple(article,Pattern.compile(regexToFindImage));
    		// TODO(Nirvanchik): This way we can miss images. For example we found 1 image,
    		// it happened to be non-existing and we return with null and don't check what
    		// it is further in the article text
    		// This should be made with using some kind of iterator rather then just return value
  */
        	if (image == null || image.isEmpty()) {
    			continue;
    		}
    		if (image.contains("{{")) {
    			image = checkImageIsImageTemplate(image);
    		} else if (image.contains("[[")) {
    			image = findImageByRegexSimple(image, ImageFinderInBody.PATTERN_TO_FIND_IMAGE);
    		} 
    		if(image!=null && image.contains("|")) {
       			image = image.substring(0, image.indexOf('|'));
    		}
    		if (image == null || image.isEmpty() || image.contains(">") || image.contains("<")) {
    			continue;
    		}
    		if (checkImageExists(wiki, commons, image)) {
    			return image;
    		}    		
		}
		return null;
	}

}
