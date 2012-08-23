/**
 *  @(#)ArchiveWithHeadersWithItemsCount.java 02/07/2012
 *  Copyright © 2012 Dmitry Trofimovich (KIN)
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
package org.wikipedia.nirvana.nirvanabot;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.wikipedia.nirvana.nirvanabot.ArchiveSettings.Enumeration;
import org.wikipedia.nirvana.nirvanabot.ArchiveSettings.Period;

/**
 * @author kin
 *
 */
public class ArchiveWithHeadersWithItemsCount extends ArchiveWithHeaders {
	public static final String template = "%(количество)";
	//protected String headerFormat;
	public HeaderFormatItem patternOfHeader[] = null;
	//protected Matcher matcher;
	/**
	 * @param text
	 * @param addToTop
	 * @param delimeter
	 * @param enumeration
	 */
	public ArchiveWithHeadersWithItemsCount(String text, boolean addToTop,
			String delimeter, Enumeration enumeration, String headerFormat) {		
		//this.headerFormat = headerFormat;
		super(addToTop, delimeter, enumeration);		
		initPattern(headerFormat);
		init(text);		
	}
	
	/**
	 * @param lines
	 * @param addToTop
	 * @param delimeter
	 * @param enumeration
	 */
	public ArchiveWithHeadersWithItemsCount(String[] lines, boolean addToTop,
			String delimeter, Enumeration enumeration, String headerFormat) {
		super(addToTop, delimeter, enumeration);	
		log.debug("ArchiveWithHeadersWithItemsCount created");
		initPattern(headerFormat);
		init(lines);		
	}
	
	private void initPattern(String headerFormat) {
		ArrayList<HeaderFormatItem> pattern = new ArrayList<HeaderFormatItem>();
		int first = 0;
		//int index = 0;
		boolean wasPeriod = false;
		int pos = 0;
		while(pos<headerFormat.length()) {
			wasPeriod = false;
			for(Period p : Period.values()) {				
				if(p==Period.NONE) continue;
				String param = p.template();
				if(headerFormat.regionMatches(pos, param, 0, param.length())) {
					if(first<pos) {
						pattern.add(new HeaderFormatItem(headerFormat.substring(first,pos)));						
					}
					pattern.add(new HeaderFormatItem(p));
					pos+=param.length();
					first=pos;
					wasPeriod = true;
				}
			
			}
			if(headerFormat.regionMatches(pos, template, 0, template.length())) {
				if(first<pos) {
					pattern.add(new HeaderFormatItem(headerFormat.substring(first,pos)));						
				}
				pattern.add(new HeaderFormatItem(template));
				pos+=template.length();
				first=pos;
				wasPeriod = true;
			}
			//if(first!=pos || pos==0) pos++;
			if(!wasPeriod) pos++;
			
		}
		if(first<pos) {
			pattern.add(new HeaderFormatItem(headerFormat.substring(first,pos)));
		}
		this.patternOfHeader = pattern.toArray(new HeaderFormatItem[0]);
	}
	
	public class IntAndString {
		public int val=-1;
		public String str = null;
	}
	
	public String headerVariableToConstant(String header,IntAndString data) {
		String str = header;
		int pos = 0;
//		if(this.patternOfHeader==null) 
//			initPattern(headerFormat);
		for(HeaderFormatItem item : patternOfHeader) {
			if(item.period==Period.NONE) {
				if(item.string.equals(template)) {
					int start = pos;
					while(Character.isDigit(str.charAt(pos)))
						pos++;
					
					str = header.substring(0, start) + template + str.substring(pos);
					if(data!=null) {
						data.val = Integer.parseInt(header.substring(start, pos));
						data.str = str;
					}
					return str;
				} else if(item.string.length()>0){
					pos+=item.string.length();
				} else {
					return str;
				}
			} else {
				if(item.period.isNumeric()) {					
					while(Character.isDigit(str.charAt(pos)))
						pos++;
				} else {
					while(Character.isAlphabetic(str.charAt(pos)))
						pos++;
				}
				
			}
		}
		return str;
	}
	
	public class HeaderFormatItem{		
		public Period period = Period.NONE;
		public String string="";
		//String template = "";
		HeaderFormatItem(String str) {
			string = str;
		}
		HeaderFormatItem (Period p) {
			period = p;
		}
		/*HeaderFormatItem (Period p) {
			period = p;
		}*/
	}
	
	
	protected class SectionWithItemsCount extends Section {
		int oldCount = -1;
		SectionWithItemsCount(Enumeration enumeration, String header,
				String headerText, String hHeader, String hHeaderText,
				boolean old) {
			super(enumeration, header, headerText, hHeader, hHeaderText, old);			
		}
		SectionWithItemsCount(Enumeration enumeration, String header,
				String headerText, String hHeader, String hHeaderText,
				boolean old, int count) {
			super(enumeration, header, headerText, hHeader, hHeaderText, old);
			this.oldCount = count;
		}

		SectionWithItemsCount(Enumeration enumeration, String header,
				String headerText, boolean old) {
			super(enumeration, header, headerText, old);			
		}
		SectionWithItemsCount(Enumeration enumeration, String header,
				String headerText, boolean old, int count) {
			super(enumeration, header, headerText, old);
			this.oldCount = count;
		}

		SectionWithItemsCount(Enumeration enumeration, boolean old) {
			super(enumeration, old);
			
		}
		
		public String toString() {
			if(!this.headerText.isEmpty()) {
				if(this.trancated) {
					this.headerText = this.headerText.replace(
							template, String.valueOf(this.oldCount));
				} else {
					this.headerText = this.headerText.replace(
						template, String.valueOf(this.getSize()));
				}
			}
			return super.toString();
		}
		
	};
	
	
	
	protected Section createSection(Enumeration enumeration, boolean old) {
		return new SectionWithItemsCount(enumeration,old);		
	}
	
	protected Section createSection(Enumeration enumeration,
			String header,	String headerText,	boolean old) {
		if(old) {
			IntAndString data = new IntAndString();
			String ht = headerVariableToConstant(headerText,data);
			String h = StringUtils.strip(ht, "=").trim();
			return new SectionWithItemsCount(enumeration,h,ht,old,data.val);	
		}
		return new SectionWithItemsCount(enumeration,header,headerText,old);		
	}
	
	protected Section createSection(Enumeration enumeration,
			String header,String headerText,
			String sHeader, String sHeaderText,
			boolean old) {
		if(old) {
			IntAndString data = new IntAndString();
			String ht = headerVariableToConstant(headerText,data);
			String h = StringUtils.strip(ht, "=").trim();
			return new SectionWithItemsCount(enumeration,h,ht,sHeader,sHeaderText,old,data.val);	
		}
		return new SectionWithItemsCount(enumeration,header,headerText,sHeader,sHeaderText,old);		
	}

}
