/**
 *  @(#)TemplateFindItem.java
 *  Copyright © 2024 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.wikipedia.nirvana.error.BadFormatException;
import org.wikipedia.nirvana.util.StringTools;

import org.apache.commons.lang3.StringUtils;

/**
 * Template item to search in wiki page text.
 * Consists of template name, template parameter and its value.
 * For example, wiki page could have:
 *    {{dog
 *      | size = large
 *      | height = 50 sm
 *    }}
 * Here "dog, size, large" could be an example to search.
 *
 */
public class TemplateFindItem implements Comparable<TemplateFindItem> {
    public final String template;
    public final String param;
    public final String value;

    /**
     * Constructs template item to search.
     *
     * @param template template name.
     * @param param template parameter.
     * @param value template parameter value.
     */
    public TemplateFindItem(String template, String param, String value) {
        this.template = StringUtils.capitalize(template);
        if (param != null && !param.isEmpty()) {
            param = param.toLowerCase();
        }
        this.param = param;
        this.value = value;
        if (template == null || template.isEmpty() || param == null || value == null) {
            throw new RuntimeException("Bad format");
        }
    }

    /**
     * Get {@link TemplateFindItem} from settings string at bot wiki settings page.
     */
    public static TemplateFindItem parseTemplateFindData(String templateFindData)
            throws BadFormatException {
        int slashes = StringTools.howMany(templateFindData, '/'); 
        if (slashes == 2) {
            String[] parts = StringUtils.splitPreserveAllTokens(templateFindData, "/", 3);
            parts = StringUtils.stripAll(parts);
            if (parts[0].isEmpty()) {
                throw new BadFormatException();
            }
            return new TemplateFindItem(parts[0], parts[1], parts[2]);
        } else if (slashes == 1) {
            String[] parts = StringUtils.splitPreserveAllTokens(templateFindData, "/", 3);
            parts = StringUtils.stripAll(parts);
            if (parts[0].isEmpty()) {
                throw new BadFormatException();
            }
            return new TemplateFindItem(parts[0], parts[1], "");
        } else if (slashes == 0) {
            if (templateFindData.trim().isEmpty()) {
                throw new BadFormatException();
            }
            return new TemplateFindItem(templateFindData.trim(), "", "");
        } else {
            throw new BadFormatException();
        }
    }

    /**
     * @return True if search item is simple. By "simple" we mean when we just search template
     *     without parameter specification.
     */
    public boolean isSimple() {
        return param.isEmpty() && value.isEmpty();
    }

    @Override
    public int compareTo(TemplateFindItem ob) {
        if (this.isSimple() && ob.isSimple()) {
            return 0;
        } else if (!this.isSimple() && ob.isSimple()) {
            return 1;
        } else if (this.isSimple() && !ob.isSimple()) {
            return -1;
        } else {
            return 0;
        }
    }
    
    @Override
    public boolean equals(Object ob) {
        if (this == ob) return true;
        if (ob instanceof TemplateFindItem) {   
            TemplateFindItem other = (TemplateFindItem) ob;
            return this.template.equals(other.template)
                && this.param.equals(other.param)
                && this.value.equals(other.value);
        }
        return false;
    }
    
    @Override
    public String toString() {
        return String.format("TemplateFindItem[%s/%s/%s]", this.template, this.param, this.value);
    }
}
