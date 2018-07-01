/**
 *  @(#)TemplateFilter.java 07.07.2016
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

import org.wikipedia.nirvana.WikiTools.EnumerationType;

import java.util.List;

/**
 * Interface for templates filter. Template filter is a set of templates or some special rules
 * which allow smart selection of articles based on checking transcluded templates and their
 * parameters.
 */
public interface TemplateFilter {

    /**
     * @return list of templates
     */
    public List<String> getTemplates();

    /**
     * @return list processing method (AND, OR, NONE OF)
     */
    public EnumerationType listType();

    /**
     * @return list of {@link TemplateFindItem} - info about required template parameters and
     *         values to search in article.
     */
    public List<TemplateFindItem> getParamFilterItems();

    /**
     * @return <code>true</code> if this filter requires filtering by searching params/values in
     *            transcluded template code.
     */
    public boolean paramValueFiltering();
}
