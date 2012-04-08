/**
 *  @(#)ArchiveWithHeaders.java 07/04/2012
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

/**
 * WARNING: This file may contain Russian characters.
 * Recommended code page for this file is CP1251 (also called Windows-1251).
 * */
package org.wikipedia.nirvana.nirvanabot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.wikipedia.nirvana.FileTools;
import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.StringTools;

public class ArchiveWithHeaders extends Archive{
	//private String archiveFullText;
	private String archivePartialText;
	//private String archiveBottom;
	private List<ItemsWithHeader> parts;	
	public static int HOW_MANY_ITEMS_TO_PARSE = 100;
	
	private String latestItemHeaderHeader = null;
	private static class ItemsWithHeader {
		private String header;
		private String headerText;
		private String headerHeader;
		private String headerHeaderText;
		private ArrayList<String> items;
		ItemsWithHeader() {
			header = null;
			headerText = "";
			headerHeader = null;
			headerHeaderText = "";
			items = new ArrayList<String>();
		}
		ItemsWithHeader(String header,String headerText) {
			this.header = header;
			this.headerText = headerText;
			headerHeader = null;
			headerHeaderText = "";
			items = new ArrayList<String>();
		}
		ItemsWithHeader(String header,String headerText,String hHeader, String hHeaderText) {
			this.header = header;
			this.headerText = headerText;
			headerHeader = hHeader;
			headerHeaderText = hHeaderText;
			items = new ArrayList<String>();
		}
		public String getHeader() {
			return header;
		}
		public String getHeaderText() {
			return headerText;
		}
		public String getHeaderHeader() {
			return headerHeader;
		}
		public void clearHeaderHeader() {
			this.headerHeader = null;
			this.headerHeaderText = "";
		}
		public String getText(String delimeter) {
			StringBuffer buf = new StringBuffer();
			if(!this.headerHeaderText.isEmpty()) {
				buf.append(headerHeaderText);
				buf.append(delimeter);
			}
			if(!this.headerText.isEmpty()) {
				buf.append(headerText);
				buf.append(delimeter);
			}
			String str = StringUtils.join(items, delimeter);
			if(!str.isEmpty()) {
				buf.append(str);
				buf.append(delimeter);
			}
			return buf.toString();
		}
		public void addItemToEnd(String item) {
			items.add(item);
		}
		public void addItemToBegin(String item) {
			items.add(0, item);
		}
		
		public void pushHeader(String h, String hText) {
			if(headerHeader!=null)
				return;
			if(this.header==null){
				this.header = h;
				this.headerText = hText;
			} else {
				this.headerHeader = this.header;
				this.headerHeaderText = this.headerText;
				this.header = h;
				this.headerText = hText;
			}
		}
		public void pushHeaderHeader(String h, String hText) {
			if(headerHeader!=null)
				return;
			this.headerHeader = h;
			this.headerHeaderText = hText;
		}
		public boolean isEmpty() {
			return (this.items.size()==0);
		}
		public String getItem(int index) {
			return this.items.get(index);
		}
		@SuppressWarnings("unused")
		public String getFirst() {
			return this.items.get(0);
		}
		@SuppressWarnings("unused")
		public String getLast() {
			return this.items.get(items.size()-1);
		}
		public int getSize() {
			return this.items.size();
		}
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if(!addToTop) {
			buf.append(this.archivePartialText);
			//buf.ap
		}
		for(ItemsWithHeader part:parts) {
			buf.append(part.getText(this.delimeter));
		}
		if(addToTop) {
			buf.append(this.archivePartialText);
		}
		return buf.toString();
	}
	
	public void initLatestItemHH(String hh) {
		this.latestItemHeaderHeader = hh;
	}
	
	void parseTop(String text) {
		String [] oldItems;
		oldItems = text.trim().split(delimeter);
		int first,last;
		first = 0;
		last = oldItems.length>HOW_MANY_ITEMS_TO_PARSE?HOW_MANY_ITEMS_TO_PARSE-1:oldItems.length-1;
		if(last<oldItems.length-1) {
			archivePartialText = StringUtils.join(oldItems,delimeter,last+1,oldItems.length);
		}
		Pattern p = Pattern.compile("^==(=?)(\\s*)(?<headername>[^=]+)(\\s*)(=?)==$");
		ItemsWithHeader part = null;//new ItemsWithHeader();
		for(int i=first;i<=last;i++) {
			String item = oldItems[i];
			if(item.isEmpty()) continue;
            Matcher m = p.matcher(item);
            if(m.matches()) {
//            	if(part!=null) {
//            		parts.add(part);
//            	}
            	if(part!=null && part.getHeaderHeader()==null && part.isEmpty() &&
            			( StringTools.howMany(part.getHeaderText(), '=', true)<
            			  StringTools.howMany(item, '=', true)        )            ) {
            		part.pushHeader(m.group("headername").trim(),item);         
            	} else {
            		part = new ItemsWithHeader(m.group("headername").trim(),item);
            		parts.add(part);
            	}
            } else {
            	if(part==null) {
            		part = new ItemsWithHeader();
            		parts.add(part);
            	}
            	part.addItemToEnd(item);
            }           
		}
	}
	void parseBottom(String text) {
		String [] oldItems;
		oldItems = text.trim().split(delimeter);
		int first,last;
		last = oldItems.length-1;
		first = oldItems.length>HOW_MANY_ITEMS_TO_PARSE?oldItems.length-HOW_MANY_ITEMS_TO_PARSE:0;
		if(first>0) {
			archivePartialText = StringUtils.join(oldItems,delimeter,0,first)+delimeter;
		}
		try {
			FileTools.dump(archivePartialText, "dump", "partial.txt");
		} catch (IOException e) {}
		Pattern p = Pattern.compile("^==(=?)(\\s*)(?<headername>[^=]+)(\\s*)(=?)==$");
		ItemsWithHeader part = null;
		boolean wasHeader = false;
		for(int i = last;i>=first;i--) {
			String item = oldItems[i];
			if(item.isEmpty()) continue;
            Matcher m = p.matcher(item);
            if(part==null) {
        		part = new ItemsWithHeader();
        		parts.add(0,part);
        	}
            if(m.matches()) {    
            	wasHeader = true;
            	if(part.getHeader()!=null && part.getHeaderHeader()==null &&
            			( StringTools.howMany(part.getHeaderText(), '=', true)>
                  			  StringTools.howMany(item, '=', true)        )     ) {
            		part.pushHeaderHeader(m.group("headername").trim(),item);
            	} else if(part.getHeader()!=null) {
            		part = new ItemsWithHeader(m.group("headername").trim(),item);
            		parts.add(0,part);
            	} else {
            		part.pushHeader(m.group("headername").trim(),item);
            	}
            } else {
            	if(wasHeader) {
            		part = new ItemsWithHeader();
            		parts.add(0,part);
            	}
            	wasHeader = false;
            	part.addItemToBegin(item);
            }
		}
		
	}
	
	public ArchiveWithHeaders(String text, boolean addToTop, String delimeter) {
		this.addToTop = addToTop;
		this.delimeter = delimeter;
		archivePartialText = "";	
		parts = new ArrayList<ItemsWithHeader>();
		
		if(addToTop) {
			parseTop(text);
		} else {
			parseBottom(text);
		}		
	}

	public void add(String item) {
		newLines++;
		if(parts.isEmpty()) {
			ItemsWithHeader section = new ItemsWithHeader();
			section.addItemToBegin(item);
			parts.add(section);
			return;
		}
		if(addToTop) {
			parts.get(0).addItemToBegin(item);
		} else {
			parts.get(parts.size()-1).addItemToEnd(item);
		}
	}
	
	//public int findHeader()
	
	public void add(String item, String header) {
		newLines++;
		ItemsWithHeader part= null;
		String headerStripped = StringUtils.strip(header, "=").trim();
		if(addToTop) {
			boolean found = false;
			for(int i=0;i<parts.size();i++) {
				part = parts.get(i);
				if(part.getHeader()!=null && part.getHeader().compareTo(headerStripped)==0) {
					found = true;
					break;
				}
			}
			if(found) {
				part.addItemToBegin(item);
			} else {
				part = new ItemsWithHeader(headerStripped,header);
				parts.add(0, part);
				part.addItemToBegin(item);
			}
		} else {
			boolean found = false;
			for(int i=parts.size()-1;i>=0;i--) {
				part = parts.get(i);
				if(part.getHeader()!=null && part.getHeader().compareTo(headerStripped)==0) {
					found = true;
					break;
				}
			}
			if(found) {
				part.addItemToEnd(item);
			} else {
				part = new ItemsWithHeader(headerStripped,header);
				parts.add(part);
				part.addItemToEnd(item);
			}
		}
	}
	
	public void add(String item, String header, String headerHeader) {
		newLines++;
		String headerHeaderStripped = StringUtils.strip(headerHeader, "=").trim();
		String headerStripped = StringUtils.strip(header, "=").trim();
		ItemsWithHeader part = null;
		int hhIndex = -1;
		hhIndex = findHeaderHeader(headerHeaderStripped);
		// если заголовок верхнего уровня не найден,
		// значит его нужно создать
		if(hhIndex<0) {
			if(addToTop) {		
				part = new ItemsWithHeader(headerStripped,header,headerHeaderStripped,headerHeader);
				parts.add(0, part);
				part.addItemToBegin(item);
				return;
			} else  {
				boolean createNewSection = true;
				if(parts.size()!=0 && parts.get(0).getHeaderHeader()==null) {					
					// нужно проверить, последние хедэры наши(относятся к заголовку) или нет					
					if(latestItemHeaderHeader!=null && 
							latestItemHeaderHeader.compareTo(headerHeader)==0) {
						createNewSection = false;						
					} 
				} 
				if(createNewSection) {
					part = new ItemsWithHeader(headerStripped,header,headerHeaderStripped,headerHeader);
					parts.add(part);
					part.addItemToEnd(item);
					return;
				} else {					
					hhIndex = 0;
				}					
			}
		} else {
			
		}
		
		// 1 найти хидер?
		int hIndex = findHeaderInSuperSection(headerStripped,hhIndex);
		if(hIndex<0) {
			// создать подсекцию
			if(addToTop) {		
				part = parts.get(hhIndex);
				part.clearHeaderHeader();
				part = new ItemsWithHeader(headerStripped,header,headerHeaderStripped,headerHeader);
				parts.add(hhIndex, part);
				part.addItemToBegin(item);				
			} else {
				part = new ItemsWithHeader(headerStripped,header);
				parts.add(findLastSectionInSuperSection(hhIndex)+1,part);
				part.addItemToEnd(item);	
			}
		} else {
			part = parts.get(hIndex);
			if(addToTop) {	
				part.addItemToBegin(item);
			} else {
				part.addItemToEnd(item);
			}				
		}
	}
	
	
	
	void initLatestItemHeaderHeader(NirvanaWiki wiki, ArchiveSettings aSettings) {
		latestItemHeaderHeader = null;
		if(!addToTop) {
			if(this.parts.size()>0) {
				ItemsWithHeader items = parts.get(0);
				if(items.getHeaderHeader()==null) {
					for(int i=items.getSize()-1;i>=0;i++) {
						Calendar c = NewPages.getNewPagesItemDate(wiki, items.getItem(i));
						if(c!=null) {
							latestItemHeaderHeader = aSettings.getHeaderHeaderForDate(c);
							log.debug("latestItemHeaderHeader : "+latestItemHeaderHeader+" for date: "+c.toString());
							return;
						}
					}
				}
			}
		}
	}
	
	
	public int findHeaderHeader(String hh) {
		int index = -1;
		ItemsWithHeader part = null;
		if(addToTop) {
			for(int i=0;i<parts.size();i++) {
				part = parts.get(i);
				if(part.getHeaderHeader()!=null && part.getHeaderHeader().compareTo(hh)==0) {
					index = i;
					break;
				}
			}
		} else {
			for(int i=parts.size()-1;i>=0;i--) {
				part = parts.get(i);
				if(part.getHeaderHeader()!=null && part.getHeaderHeader().compareTo(hh)==0) {
					index = i;
					break;
				}
			}
		}
		return index;
	}
	
	
	public int findHeaderInSuperSection(String header, int startFrom) {
		//int index = -1;
		ItemsWithHeader part = parts.get(startFrom);
		String hh = part.getHeaderHeader();
		for(int i = startFrom;i<this.parts.size();i++) {
			part = parts.get(i);
			if(hh==null && part.getHeaderHeader()!=null) return -1;
			else if (hh!=null && part.getHeaderHeader()!=null && part.getHeaderHeader().compareTo(hh)!=0) return -1;
			if(part.getHeader()!=null && part.getHeader().compareTo(header)==0) return i;
		}		
		return -1;
	}
	public int findLastSectionInSuperSection(int startFrom) {
		ItemsWithHeader part = parts.get(startFrom);
		String hh = part.getHeaderHeader();
		int i = 0;
		for(i = startFrom;i<this.parts.size();i++) {
			part = parts.get(i);
			if(hh==null && part.getHeaderHeader()!=null) break;
			else if (hh!=null && part.getHeaderHeader()!=null && part.getHeaderHeader().compareTo(hh)!=0) break;			
		}		
		return (i-1);
	}
	
}
