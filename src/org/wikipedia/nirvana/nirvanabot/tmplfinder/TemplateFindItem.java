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
 * Recommended code page for this file is CP1251 (also called Windows-1251).
 * */

package org.wikipedia.nirvana.nirvanabot.tmplfinder;

import org.apache.commons.lang3.StringUtils;
import org.wikipedia.nirvana.StringTools;
import org.wikipedia.nirvana.nirvanabot.BadFormatException;

/**
 * @author kin
 *
 */
public class TemplateFindItem {
	public String template;
	public String param;
	public String value;
	
//	public TemplateFindItem(String templateFindData) {
//		String parts[] = templateFindData.
//	}
	
	public TemplateFindItem(String template, String param, String value) {
		this.template = template;
		this.param = param;
		if (this.param != null && !this.param.isEmpty()) {
			this.param = this.param.toLowerCase();
		}
		this.value = value;
	}
	
	public static TemplateFindItem parseTemplateFindData(String templateFindData) throws BadFormatException {		
		if (StringTools.howMany(templateFindData, '/') == 2) {
			String parts[] = StringUtils.splitPreserveAllTokens(templateFindData, "/", 3);
			parts = StringUtils.stripAll(parts);
			if (parts[0].isEmpty() && parts[1].isEmpty() && parts[2].isEmpty()) {
				throw new BadFormatException();
			}
			return new TemplateFindItem(parts[0], parts[1], parts[2]);
		} else {
			throw new BadFormatException();
		}
	}
}
