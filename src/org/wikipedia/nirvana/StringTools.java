/**
 *  @(#)StringTools.java
 *  Copyright © 2011-2014 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import java.util.ArrayList;
import java.util.List;

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
    
    public static String[] splitBottom(String str, String separatorChars, int max) {
        
        if (str == null) {
            return null;
        }
        int len = str.length();
        if (len == 0) {
            return new String[0];
        }
        List<String> list = new ArrayList<String>();
        int sizePlus1 = 1;
        int i = len-1, start = len;
        boolean match = false;
        if (separatorChars.length() == 1) {
            // Optimise 1 character case
            char sep = separatorChars.charAt(0);
            while (i >= 0) {
                if (str.charAt(i) == sep) {
                    if (match) {
                        if (sizePlus1++ == max) {
                            i = -1;
                        }
                        list.add(0,str.substring(i+1, start));
                        match = false;
                    }
                    start = i--;
                    continue;
                }
                match = true;
                i--;
            }
        } else {
            // standard case
        	/*
            while (i < len) {
                if (separatorChars.indexOf(str.charAt(i)) >= 0) {
                    if (match) {
                        if (sizePlus1++ == max) {
                            i = len;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                match = true;
                i++;
            }*/
        }
        if (match) {
        	list.add(0,str.substring(i+1, start));
        }
        return list.toArray(new String[list.size()]);
    }
    
    public static String trancateTo(String text, int len) {
    	return text.length()>len?text.substring(0,len):text;
    }
    
    public static boolean startsWithAnyChar(String text, String chars) {
    	if (chars == null || chars.isEmpty()) {
    		return false;
    	}
    	for (char ch:chars.toCharArray()) {
    		if (text.charAt(0) == ch) {
    			return true;
    		}
    	}
    	return false;
    }
    
}
