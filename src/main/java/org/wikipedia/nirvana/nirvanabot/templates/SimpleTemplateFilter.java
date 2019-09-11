/**
 *  @(#)SimpleTemplateFilter.java 08.07.2016
 *  Copyright © 2016 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.nirvanabot.templates;

import org.wikipedia.nirvana.wiki.CatScanTools.EnumerationType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Template filter which has only simple list of templates.
 * Article must have any/all/none of templates listed here to pass this filter.
 */
public class SimpleTemplateFilter implements TemplateFilter {
    protected List<String> templates;
    protected EnumerationType enumType;

    public SimpleTemplateFilter(List<String> templates, EnumerationType enumType) {
        this.templates = templates;
        this.enumType = enumType;
        if (templates != null) {
            Collections.sort(templates);
        }
    }

    @Override
    public List<String> getTemplates() {
        return templates;
    }

    @Override
    public EnumerationType listType() {
        return enumType;
    }

    @Override
    public List<TemplateFindItem> getParamFilterItems() {
        List<TemplateFindItem> list = new ArrayList<>();
        for (String template: templates) {
            list.add(new TemplateFindItem(template, "", ""));
        }
        return list;
    }

    @Override
    public boolean paramValueFiltering() {
        return false;
    }
}
