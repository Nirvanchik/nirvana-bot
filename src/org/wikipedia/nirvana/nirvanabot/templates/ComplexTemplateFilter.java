/**
 *  @(#)ComplexTemplateFilter.java 08.07.2016
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
 * Recommended code page for this file is CP1251 (also called Windows-1251).
 * */

package org.wikipedia.nirvana.nirvanabot.templates;

import org.wikipedia.nirvana.WikiTools.EnumerationType;

import java.util.ArrayList;
import java.util.List;

/**
 * Complex template filter.
 * We have here requirements for each template: param/value.
 * Article must have template with the corresponding param or param/value pair to pass this filter.
 */
public class ComplexTemplateFilter extends SimpleTemplateFilter {
    List<TemplateFindItem> findItems;

    /**
     * Creates an instance having list of {@link TemplateFindItem} and {@link
     * org.wikipedia.nirvana.WikiTools.EnumerationType EnumerationType}. 
     */
    public ComplexTemplateFilter(List<TemplateFindItem> items, EnumerationType enumType) {        
        super(null, enumType);
        findItems = items;
        templates = new ArrayList<>();
        for (TemplateFindItem item: items) {
            if (!templates.contains(item.template)) {
                templates.add(item.template);
            }
        }
    }

    @Override
    public List<TemplateFindItem> getParamFilterItems() {
        return findItems;
    }

    @Override
    public boolean paramValueFiltering() {
        return findItems.size() > 0;
    }
}
