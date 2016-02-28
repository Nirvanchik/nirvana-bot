/**
 *  @(#)WikiUtils.java 25.08.2015
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

package org.wikipedia.nirvana;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author kin
 *
 */
public class WikiUtils {
	protected static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WikiUtils.class.getName());

	public static boolean parseTemplate(String templateRegex, String text, Map<String, String> parameters, boolean splitByNewLine) {
        String str = "(\\{\\{"+templateRegex+")(.+)$"; // GOOD
        log.debug("pattern = "+str);
        Pattern pattern = Pattern.compile(str,Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(text);
        if (!m.find())
        {
        	//log.error("portal settings parse error (doesn't match pattern)");
            return false;
        }
        
        log.debug("group count = "+m.groupCount());
        
        int level = 1;
        int begin = m.end(1);
        int end = -1;
        for (int i = begin; i < text.length() - 1; ++i)
        {
            if (text.charAt(i) == '{' && text.charAt(i+1) == '{')
            {
                ++level;
            }
            else if (text.charAt(i) == '}' && text.charAt(i+1) == '}')
            {
                --level;
                if (level == 0)
                {
                    end = i;
                    break;
                }
            }
        }

        if (end == -1)
        {
            return false;
        }
        Pattern commentPattern = Pattern.compile("<!--(.+?)-->");
        
        String parameterString = text.substring(begin, end);
        String splitParam = "\\|";
        if (splitByNewLine) {
        	splitParam = "\\n\\s*\\|";
        }
        String[] ps = parameterString.split(splitParam);
        
        String lastKey = "";
        String lastVal = "";
        for(int i =0;i<ps.length;i++) {
        	String p = ps[i];
        	//log.debug("checking string: "+p);
        	boolean newStringToLastVal = false;
        	int count = StringTools.howMany(p, '=');
        	if(count==0 && i==0) continue;
        	if(!lastVal.isEmpty() && lastVal.endsWith("{")) { // {| означает начало таблицы
        		newStringToLastVal = true;        		
        	} else if(count>0) {
        		int eq = p.indexOf('=');
        		String first = p.substring(0,eq); 
        		String last = p.substring(eq+1);
        		String key = first.trim().toLowerCase();
        		if(key.equals("align") || key.equals("style")) {
        			newStringToLastVal = true;
        		} else {
	        		Matcher mComment = commentPattern.matcher(last);
	            	String value = mComment.replaceAll("").trim();
	        		parameters.put(key, value);
	                lastKey = key;        	
	                lastVal = value;
        		}
        	} else {
        		if (!lastKey.isEmpty())
                {
        			newStringToLastVal = true;                	
                }
        	}  
        	if(newStringToLastVal) {
        		Matcher mkey = commentPattern.matcher(p);
                String value = mkey.replaceAll("").trim();
                //parameters[lastKey] = parameters[lastKey] + "|" + value;
                lastVal = parameters.get(lastKey)+"|"+value;
                parameters.put(lastKey, lastVal);
        	}
        }
        return true;
	}
}
