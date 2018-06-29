/**
 *  @(#)TemplateFindItem.java 23.08.2015
 *  Copyright © 2015 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.apache.commons.lang3.StringUtils;
import org.wikipedia.nirvana.StringTools;
import org.wikipedia.nirvana.nirvanabot.BadFormatException;

/**
 * @author kin
 *
 */
public class TemplateFindItem implements Comparable<TemplateFindItem>{
    public final String template;
    public final String param;
    public final String value;

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

	public static TemplateFindItem parseTemplateFindData(String templateFindData) throws BadFormatException {
        int slashes = StringTools.howMany(templateFindData, '/'); 
        if (slashes == 2) {
			String parts[] = StringUtils.splitPreserveAllTokens(templateFindData, "/", 3);
			parts = StringUtils.stripAll(parts);
            if (parts[0].isEmpty()) {
				throw new BadFormatException();
			}
			return new TemplateFindItem(parts[0], parts[1], parts[2]);
        } else if (slashes == 1) {
            String parts[] = StringUtils.splitPreserveAllTokens(templateFindData, "/", 3);
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
}
