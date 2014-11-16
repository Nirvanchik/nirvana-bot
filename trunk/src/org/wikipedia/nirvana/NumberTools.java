/**
 *  @(#)NumberTools.java 12.10.2014
 *  Copyright ฉ 2014 Dmitry Trofimovich (KIN, Nirvanchik) (DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana;

import org.apache.commons.lang3.StringUtils;

/**
 * @author kin
 *
 */
public class NumberTools {
	public static final int K = 1024;
	public static final int M = K*K;
	public static int parseFileSize(String s) throws NumberFormatException {
		int size = 0;		
		int multi = 1;
		if (StringUtils.endsWithIgnoreCase(s, "K") || StringUtils.endsWithIgnoreCase(s, "ส")) {
			multi = K;
			s = s.substring(0, s.length()-1).trim();
		} else if (StringUtils.endsWithIgnoreCase(s, "KB") || StringUtils.endsWithIgnoreCase(s, "สม")) {
			multi = K;
			s = s.substring(0, s.length()-2).trim();
		} else if (StringUtils.endsWithIgnoreCase(s, "M") || StringUtils.endsWithIgnoreCase(s, "ฬ")) {
			multi = M;
			s = s.substring(0, s.length()-1).trim();
		} else if (StringUtils.endsWithIgnoreCase(s, "MB") || StringUtils.endsWithIgnoreCase(s, "ฬม")) {
			multi = M;
			s = s.substring(0, s.length()-2).trim();
		}
		size = Integer.parseInt(s);		
		return (size*multi);
	}
}
