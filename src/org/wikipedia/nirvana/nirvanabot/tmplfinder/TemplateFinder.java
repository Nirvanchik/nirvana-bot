/**
 *  @(#)TemplateFinder.java 23.08.2015
 *  Copyright � 2015 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.nirvanabot.tmplfinder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.wikipedia.Wiki;
import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.WikiUtils;

/**
 * @author kin
 *
 */
public class TemplateFinder {
	TemplateFindItem findData[];
	NirvanaWiki wiki;
	
	public TemplateFinder(TemplateFindItem findData[], NirvanaWiki wiki) {
		this.findData = findData;
		this.wiki = wiki;
	}
	
    public boolean find(String article) throws IOException {
    	String templates[] = wiki.getTemplates(article, Wiki.TEMPLATE_NAMESPACE);
    	for (int i = 0; i<templates.length; i++) {
    		templates[i] = StringUtils.removeStart(templates[i], wiki.namespaceIdentifier(Wiki.TEMPLATE_NAMESPACE) + ":");
    	}
    	if (templates.length == 0) {
    		return false;
    	}
    	String text = wiki.getPageText(article);
    	for(TemplateFindItem findItem: findData) {
    		if (findItem(findItem, text, templates)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private boolean findItem(TemplateFindItem item, String text, String templates[]) {
    	String templatesToSearch[];
    	if (item.template.isEmpty()) {
    		templatesToSearch = templates;
    	} else {    		
    		if (ArrayUtils.contains(templates, item.template)) {
    			templatesToSearch = new String[]{item.template};
    		} else {
    			return false;
    		}
    	}
    	return findItemInTemplates(item, text, templatesToSearch);
    }

    private boolean findItemInTemplates(TemplateFindItem item, String text, String templates[]) {
	    for (String template:templates) {
	    	if (findItemInTemplate(item, text, template)) {
	    		return true;
	    	}
	    }
	    return false;
    }
    
    private boolean findItemInTemplate(TemplateFindItem item, String text, String template) {
	    // 1) ����� ����� � ���������� ��� �������, ������� � ���� ������� ��������
    	HashMap<String, String> params = new HashMap<>();
    	if (!WikiUtils.parseTemplate(template, text, params, false)) {
    		return false;
    	}    	
    	// 2) �������� �� ������� � �������� ������ ���� � ��������
    	if (item.param.isEmpty()) {
    		if (item.value.isEmpty()) {
    			return true;
    		} 
    		for (String value: params.values()) {
        		if (value.contains(item.value)) {
        			return true;
        		}
        	}
    	} else { 
			String value = params.get(item.param);
			if (value != null && (item.value.isEmpty() || value.contains(item.value))) {
				return true;
			}
    	}
	    return false;
    }    
    
}