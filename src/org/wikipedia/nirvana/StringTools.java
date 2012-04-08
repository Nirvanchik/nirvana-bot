/**
 *  @(#)StringTools.java 07/04/2012
 *  Copyright © 2011 - 2012 Dmitry Trofimovich (KIN)
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

package org.wikipedia.nirvana;

/**
 * @author kin
 *
 */
public class StringTools {

	/**
	 * 
	 */
	
	public static int howMany(String s, char c) {
		return howMany(s,c,false);
	}
	
	public static int howMany(String s, char c, boolean continuous) {
		int n = 0;
		for(char x : s.toCharArray()) {
			if(x==c) {
				n++;
			} else if(continuous)
				break;
		}
		return n;
	}
	
    public static String trimLeft(String s) {
        return s.replaceAll("^\\s+", "");
    }
     
    public static String trimRight(String s) {
        return s.replaceAll("\\s+$", "");
    } 
    
}
