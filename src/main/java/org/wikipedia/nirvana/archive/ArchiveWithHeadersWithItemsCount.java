/**
 *  @(#)ArchiveWithHeadersWithItemsCount.java
 *  Copyright © 2012-2014 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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
package org.wikipedia.nirvana.archive;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.wikipedia.nirvana.archive.ArchiveSettings.Enumeration;
import org.wikipedia.nirvana.archive.ArchiveSettings.Period;
import org.wikipedia.nirvana.nirvanabot.BotVariables;
import org.wikipedia.nirvana.nirvanabot.NirvanaBot;

/**
 * Archive that has "count" (or "%(количество)") placeholder in its headers.
 * "count" is a count of articles in the next header. 
 */
public class ArchiveWithHeadersWithItemsCount extends ArchiveWithHeaders {
    private List<HeaderFormatItem> patternOfHeader = null;
    private List<HeaderFormatItem> patternOfSuperHeader = null;

	public class HeaderFormatItem {		
		public Period period = Period.NONE;
		public String string = "";
		HeaderFormatItem(String str) {
			string = str;
		}
		HeaderFormatItem(Period p) {
			period = p;
		}
	}

	/**
     * Constructor to use when archive is available as bare string.
	 */
	public ArchiveWithHeadersWithItemsCount(String text, int parseCount, boolean addToTop,
            String delimeter, Enumeration enumeration, String headerFormat,
            String superHeaderFormat) {
        super(parseCount, addToTop, delimeter, enumeration, headerFormat, superHeaderFormat);
        log.debug("ArchiveWithHeadersWithItemsCount created");
        patternOfHeader = parsePattern(headerFormat);
        patternOfSuperHeader = parsePattern(superHeaderFormat);
		init(text);		
	}

	/**
     * Constructor to use when archive is available as string array. 
	 */
	public ArchiveWithHeadersWithItemsCount(String[] lines, int parseCount, boolean addToTop,
            String delimeter, Enumeration enumeration, String headerFormat,
            String superHeaderFormat) {
        super(parseCount, addToTop, delimeter, enumeration, headerFormat, superHeaderFormat);
        log.debug("ArchiveWithHeadersWithItemsCount created");
        patternOfHeader = parsePattern(headerFormat);
        patternOfSuperHeader = parsePattern(superHeaderFormat);
		init(lines);		
	}

    private List<HeaderFormatItem> parsePattern(String headerFormat) {
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
            if (headerFormat.regionMatches(pos, BotVariables.COUNT, 0,
                    BotVariables.COUNT.length())) {
				if(first<pos) {
					pattern.add(new HeaderFormatItem(headerFormat.substring(first,pos)));						
				}
                pattern.add(new HeaderFormatItem(BotVariables.COUNT));
                pos += BotVariables.COUNT.length();
				first=pos;
				wasPeriod = true;
			}
			//if(first!=pos || pos==0) pos++;
			if(!wasPeriod) pos++;
			
		}
		if(first<pos) {
			pattern.add(new HeaderFormatItem(headerFormat.substring(first,pos)));
		}
        return pattern;
	}

	public class IntAndString {
		public int val=-1;
		public String str = null;
	}

    public String headerVariableToConstant(String header, List<HeaderFormatItem> headerPattern,
            IntAndString data) {
		String str = header;
		int pos = 0;
        for (HeaderFormatItem item : headerPattern) {
			if(item.period==Period.NONE) {
                if (item.string.equals(BotVariables.COUNT)) {
					int start = pos;
					while(Character.isDigit(str.charAt(pos)))
						pos++;

                    str = header.substring(0, start) + BotVariables.COUNT + str.substring(pos);
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
	
	
	protected class SectionWithItemsCount extends Section {
		int oldCount = -1;
		SectionWithItemsCount(Enumeration enumeration, String header,
                String headerText, String superHeader, String superHeaderText,
				boolean old) {
            super(enumeration, header, headerText, superHeader, superHeaderText, old);
		}
		SectionWithItemsCount(Enumeration enumeration, String header,
                String headerText, String superHeader, String superHeaderText,
				boolean old, int count) {
            super(enumeration, header, headerText, superHeader, superHeaderText, old);
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
                this.headerText = this.headerText.replace(
                        BotVariables.COUNT, String.valueOf(getSize()));
			}
            if (!superHeaderText.isEmpty()) {
                int size = getSuperSectionSize(superHeader);
                log.debug("Calculated size for section {} was: {}", superHeader, size);
                superHeaderText = superHeaderText.replace(BotVariables.COUNT, String.valueOf(size));
            }
			return super.toString();
		}

        @Override
        public int getSize() {
            if (trancated) return oldCount;
            else return super.getSize();
        }

	};

    private int getSuperSectionSize(String superHeaderName) {
        int start = -1;
        int size = 0;
        start = findSuperHeader(superHeaderName);
        if (start < 0) {
            log.error("This is not possible! I can't find this header..");
            if (NirvanaBot.DEBUG_BUILD) {
                assert false : "This is not possible! I can't find this header..";
            }
            return 0;
        }
        int end = findLastSectionInSuperSection(start);
        log.debug("Super-section range is: {}:{}.", start, end);
        for (int i = start; i <= end; i++) {
            size += parts.get(i).getSize();
        }
        return size;
    }

	protected Section createSection(Enumeration enumeration, boolean old) {
		return new SectionWithItemsCount(enumeration,old);		
	}
	
	protected Section createSection(Enumeration enumeration,
			String header,	String headerText,	boolean old) {
		if(old) {
			IntAndString data = new IntAndString();
            String ht = headerVariableToConstant(headerText, patternOfHeader, data);
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
            String ht = headerVariableToConstant(headerText, patternOfHeader, data);
			String h = StringUtils.strip(ht, "=").trim();
            String sht = headerVariableToConstant(headerText, patternOfSuperHeader, data);
            String sh = StringUtils.strip(sht, "=").trim();
            return new SectionWithItemsCount(enumeration, h, ht, sh, sht, old, data.val);
		}
		return new SectionWithItemsCount(enumeration,header,headerText,sHeader,sHeaderText,old);		
	}
}
