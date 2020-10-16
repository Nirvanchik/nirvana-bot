/**
 *  @(#)ArchiveWithHeaders.java 02/07/2012
 *  Copyright © 2011 - 2012 Dmitry Trofimovich (KIN)(DimaTrofimovich@gmail.com)
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.StringUtils;

import org.wikipedia.nirvana.archive.ArchiveSettings.Enumeration;
import org.wikipedia.nirvana.nirvanabot.NewPages;
import org.wikipedia.nirvana.util.StringTools;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

public class ArchiveWithHeaders extends Archive{
	//private String archiveFullText;
	private String archivePartialText = "";

    protected List<Section> parts;
	public static int HOW_MANY_ITEMS_TO_PARSE_DEFAULT = 100;	
	protected int HOW_MANY_ITEMS_TO_PARSE = HOW_MANY_ITEMS_TO_PARSE_DEFAULT;	
	private String latestItemHeaderHeader = null;

    /**
     * @see ArchiveSettings#headerFormat
     */
    public String headerFormat;

    /**
     * @see ArchiveSettings#superHeaderFormat
     */
    public String superHeaderFormat;

	protected Section createSection(Enumeration enumeration, boolean old) {
		return new Section(enumeration,old);		
	}

	protected Section createSection(Enumeration enumeration,
			String header,	String headerText,	boolean old) {
		return new Section(enumeration,header,headerText,old);		
	}

    protected Section createSection(Enumeration enumeration, String header, String headerText,
            String superHeader, String superHeaderText, boolean old) {
        return new Section(enumeration, header, headerText, superHeader, superHeaderText, old);
	}

	protected class Section {
		private Enumeration enumeration = Enumeration.NONE;
        /**
         * Single header, or low-level header.
         */
		protected String header;
		protected String headerText;
        /**
         * Top-level header.
         */
        protected String superHeader;
        protected String superHeaderText;
		private ArrayList<String> items;
		private boolean hasOL = false;
		private boolean old;
		protected boolean trancated;// = true;
		Section(Enumeration enumeration,boolean old) {
			log.debug("section created, no header");
			this.enumeration = enumeration;
			header = null;
			headerText = "";
            superHeader = null;
            superHeaderText = "";
			items = new ArrayList<String>();
			this.old = old;
			trancated = old;			 
		}
		Section(Enumeration enumeration,String header,String headerText,boolean old) {
			log.debug("section created, "+headerText);
			this.enumeration = enumeration;
			this.header = header;
			this.headerText = headerText;
            superHeader = null;
            superHeaderText = "";
			items = new ArrayList<String>();
			this.old = old;
			trancated = old;	
		}

        Section(Enumeration enumeration, String header, String headerText, String superHeader,
                String superHeaderText, boolean old) {
			log.debug("section created, "+headerText);
            log.debug("super header: " + superHeaderText);
			this.enumeration = enumeration;
			this.header = header;
			this.headerText = headerText;
            this.superHeader = superHeader;
            this.superHeaderText = superHeaderText;
			items = new ArrayList<String>();
			this.old = old;
			trancated = old;	
		}
		public String getHeader() {
			return header;
		}
		public String getHeaderText() {
			return headerText;
		}
		public String getSuperHeader() {
			return superHeader;
		}

        public void clearSuperHeader() {
            this.superHeader = null;
            this.superHeaderText = "";
		}

		public String toString() {
			StringBuffer buf = new StringBuffer();
            if(!this.superHeaderText.isEmpty()) {
                buf.append(superHeaderText);
				buf.append(delimeter);
			}
			if(!this.headerText.isEmpty()) {
				buf.append(headerText);
				buf.append(delimeter);
			}
			if(enumeration==Enumeration.HTML && (!trancated || hasOL)) {
				buf.append(OL);
				buf.append(delimeter);
			}
			if(!trancated && !this.hasOL && this.old && (enumeration==Enumeration.HTML ||
					enumeration==Enumeration.HTML_GLOBAL		)) {
				for(int i=0;i<items.size();i++) {
					String item = items.get(i);
					if(!item.startsWith("<li>")) {
						if(item.startsWith("#") || item.startsWith("*")) {
							item = "<li>" + item.substring(1);
						} else {
							item = "<li> "+item;
						}
						items.set(i, item);
					}
				}
			}
			String str = StringUtils.join(items, delimeter);
			if(!str.isEmpty()) {
				buf.append(str);
				buf.append(delimeter);
			}
			if(enumeration==Enumeration.HTML && (!trancated || hasOL)) {
				buf.append(OL_END);
				buf.append(delimeter);
			}
			return buf.toString();
		}
		public String enumItem(String item) {
			String str = item;
			//log.debug("enumItem() item: "+item);
			//log.debug("enumItem() enumeration: "+enumeration + " hasOL: "+hasOL + " trancated: "+ trancated);
			if(enumeration==Enumeration.HTML_GLOBAL ||
					(enumeration==Enumeration.HTML && 
						(this.hasOL || !this.trancated))) {
				if(item.startsWith("#") || item.startsWith("*")) {
					str = "<li>"+ item.substring(1);
				} else {
					str = "<li> "+ item;
				}
			} else if(enumeration==Enumeration.HASH) {
				if(str.startsWith("#")) {
					// do nothing
				} else if(str.startsWith("*")) {
					str = "#" + str.substring(1);
				} else {
					str = "# " + str;
				}
			}
			return str;
		}
		public void addOldItemToEnd(String item) {			
			items.add(item);
		}
		public void addOldItemToBegin(String item) {
			items.add(0, item);
		}
		
		public void addItemToEnd(String item) {			
			items.add(enumItem(item));
		}
		public void addItemToBegin(String item) {
			items.add(0, enumItem(item));
		}

		public void pushHeader(String h, String hText) {
            if (superHeader != null) return;
			if(this.header==null){
				this.header = h;
				this.headerText = hText;
			} else {
                this.superHeader = this.header;
                this.superHeaderText = this.headerText;
				this.header = h;
				this.headerText = hText;
			}
		}

        public void pushSuperHeader(String h, String hText) {
            if (superHeader != null) return;
            this.superHeader = h;
            this.superHeaderText = hText;
		}
		public boolean isEmpty() {
			return (this.items.size()==0);
		}
		public String getItem(int index) {
			return this.items.get(index);
		}
		public String getFirst() {
			return this.items.get(0);
		}
		public String getLast() {
			return this.items.get(items.size()-1);
		}

        /**
         * @return items count in this section loaded by bot. It may not count all items when bot
         *         reads section partially.
         */
		public int getSize() {
			return this.items.size();
		}
	}

	public String toString() {
		//StringBuilder b;
		StringBuilder buf = new StringBuilder();
		if(this.enumeration == Enumeration.HTML_GLOBAL) {
			buf.append(OL);
			buf.append(delimeter);
		}
		if(!addToTop) {
			buf.append(this.archivePartialText);
			//buf.ap
		}
		
		for(Section part:parts) {			
			buf.append(part.toString());
		}
		
		if(addToTop && !archivePartialText.isEmpty()) {
			buf.append(this.archivePartialText);			
			if(!archivePartialText.endsWith(delimeter)) 
				buf.append(delimeter);
		}
		
		if(this.enumeration == Enumeration.HTML_GLOBAL) {			
			buf.append(OL_END);
		}
		
		return buf.toString();
	}
	
	public void initLatestItemHH(String hh) {
		this.latestItemHeaderHeader = hh;
	}
	
	
	
	void parseTop(String lines[]) {
		int i = 0;
		while(i<lines.length && lines[i].isEmpty()) i++;
		if(i< lines.length && lines[i].compareToIgnoreCase(OL)==0) i++;
		else if (i< lines.length && lines[i].startsWith(OL)) {
			lines[i] = lines[i].substring(OL.length());
		}
		int j = lines.length-1;
		while(j>=0 && lines[j].isEmpty()) j--;
		if(j>=0 && lines[j].compareToIgnoreCase(OL_END)==0) j--;
		else if(j>=0 && lines[j].endsWith(OL_END)) {
			lines[j]=lines[j].substring(0,lines[j].length()-OL.length());			
		}
		int first=0, last;
		last = (j-i)>HOW_MANY_ITEMS_TO_PARSE?HOW_MANY_ITEMS_TO_PARSE-1:j;
		if(last<j) {
			archivePartialText = StringUtils.join(lines,delimeter,last+1,j+1);
		}
		parseTopLines(lines,first,last);
	}
	
	void parseTop(String text) {
		String [] oldItems;
		oldItems = StringUtils.split(text.trim(),delimeter,HOW_MANY_ITEMS_TO_PARSE);
		int first=0,last;		
		if(oldItems.length==HOW_MANY_ITEMS_TO_PARSE) {
			archivePartialText = oldItems[oldItems.length-1];
			last = oldItems.length-2;
		} else {
			last = oldItems.length-1;
		}
		parseTopLines(oldItems,first,last);
	}
	
	void parseTopLines(String oldItems[],int first, int last) {	
		
		Pattern p = Pattern.compile("^==(=?)(\\s*)(?<headername>[^=]+)(\\s*)(=?)==$");
		Section part = null;//new Section();
		boolean trancated = !this.archivePartialText.isEmpty();
		for(int i=first;i<=last;i++) {
			String item = oldItems[i];
			if(item.isEmpty()) continue;
            Matcher m = p.matcher(item);
            if(m.matches()) {
                if (part != null && part.getSuperHeader() == null && part.isEmpty() &&
                        (StringTools.countPaddedCharsAtLeft(part.getHeaderText(), '=') <
                                StringTools.countPaddedCharsAtLeft(item, '='))) {
            		part.pushHeader(m.group("headername").trim(),item);         
            	} else {
            		if(part!=null) {
            			part.trancated = false;
            		}
            		part = createSection(enumeration,m.group("headername").trim(),item,true);
            		part.trancated = trancated;
            		parts.add(part);
            	}
            } else {
            	if(part==null) {
            		part = createSection(enumeration,true);
            		part.trancated = trancated;
            		parts.add(part);            	
	            }
            	if(item.compareToIgnoreCase("<ol>")==0) {
            		part.hasOL = true;
            	} else if(item.compareToIgnoreCase("</ol>")==0) {
            		part.trancated = false;
            	}
            	else {/*
	            	if(part==null) {
	            		part = new Section();
	            		parts.add(part);
	            	}*/
	            	part.addOldItemToEnd(item);
            	}
            }           
		}
	}
	void parseBottom(String lines[]) {	
		int i = 0;
		while(i<lines.length && lines[i].isEmpty()) i++;
		if(i<lines.length && lines[i].compareToIgnoreCase(OL)==0) i++;
		int j = lines.length-1;
		while(j>=0 && lines[j].isEmpty()) j--;
		if(j>=0 && lines[j].compareToIgnoreCase(OL_END)==0) j--;
		
		int first,last;
		last = j;
		first = (j-i)>HOW_MANY_ITEMS_TO_PARSE?j+1-HOW_MANY_ITEMS_TO_PARSE:0;
		if(first>0) {
			archivePartialText = StringUtils.join(lines,delimeter,0,first)+delimeter;
		}
		parseBottomLines(lines,first,last);
	}
	void parseBottom(String text) {
		String [] oldItems;
		oldItems = StringTools.splitBottom(text.trim(), delimeter, HOW_MANY_ITEMS_TO_PARSE);
		int first,last;
		last = oldItems.length-1;		
        if (oldItems.length > HOW_MANY_ITEMS_TO_PARSE) {
			archivePartialText = oldItems[0];
			first = 1; 
		} else {
			first = 0;
		}
		parseBottomLines(oldItems,first,last);
	}
	void parseBottomLines(String oldItems[],int first, int last) {
		
		Pattern p = Pattern.compile("^==(=?)(\\s*)(?<headername>[^=]+)(\\s*)(=?)==$");
		Section part = null;
		boolean wasHeader = false;
		boolean trancated = !this.archivePartialText.isEmpty();
		for(int i = last;i>=first;i--) {
			String item = oldItems[i];
			if(item.isEmpty()) continue;
            Matcher m = p.matcher(item);
            if(part==null) {
        		part = createSection(enumeration,true);
        		part.trancated = trancated;
        		parts.add(0,part);
        	}
            if(m.matches()) {    
            	wasHeader = true;
            	part.trancated = false;
            	if(part.getHeader()==null) {
            		part.pushHeader(m.group("headername").trim(),item);
                } else if(part.getHeader() != null && part.getSuperHeader() == null &&
                        (StringTools.countPaddedCharsAtLeft(part.getHeaderText(), '=') >
                                  StringTools.countPaddedCharsAtLeft(item, '='))) {
                    part.pushSuperHeader(m.group("headername").trim(),item);
            	} else {
            		part = createSection(enumeration,m.group("headername").trim(),item,true);
            		part.trancated = trancated;
            		parts.add(0,part);
            	} 
            } else {
            	if(item.equalsIgnoreCase(OL)) {
            		part.trancated = false;
            	} else if(item.equalsIgnoreCase(OL_END)) {
            		if(wasHeader) {
                		part = createSection(enumeration,true);
                		part.trancated = trancated;
                		parts.add(0,part);
                	}
            		part.hasOL = true;
            	} else {
	            	if(wasHeader) {
	            		part = createSection(enumeration,true);
	            		part.trancated = trancated;
	            		parts.add(0,part);
	            	}
	            	//wasHeader = false;
	            	part.addOldItemToBegin(item);
            	}
            	wasHeader = false;
            }
		}
		
	}
	
	public ArchiveWithHeaders(String text, int parseCount, boolean addToTop, String delimeter,
            Enumeration enumeration, String headerFormat, String superHeaderFormat) {
		log.debug("ArchiveWithHeaders created, enumeration: "+enumeration.toString()+ " top:"+addToTop);
		this.addToTop = addToTop;
		this.delimeter = delimeter;
		archivePartialText = "";	
		this.enumeration = enumeration;		
        this.headerFormat = headerFormat;
        this.superHeaderFormat = superHeaderFormat;
		parts = new ArrayList<Section>();
		if(parseCount>=0)
			HOW_MANY_ITEMS_TO_PARSE = parseCount;
		
		init(text);
	}
	
	public void init(String text) {
		String oldText = trimEnumerationAndWhiteSpace(text);
		if(addToTop) {
			parseTop(oldText);
		} else {
			parseBottom(oldText);
		}		
	}

	public ArchiveWithHeaders(int parseCount, boolean addToTop, String delimeter,
            Enumeration enumeration, String headerFormat, String superHeaderFormat) {
		log.debug("ArchiveWithHeaders created, enumeration: "+enumeration.toString()+ " top:"+addToTop);
		this.addToTop = addToTop;
		this.delimeter = delimeter;
		archivePartialText = "";	
		this.enumeration = enumeration;
        this.headerFormat = headerFormat;
        this.superHeaderFormat = superHeaderFormat;
		if(parseCount>=0)
			this.HOW_MANY_ITEMS_TO_PARSE = parseCount;
		parts = new ArrayList<Section>();		
	}
	
	public ArchiveWithHeaders(String lines[], int parseCount, boolean addToTop, String delimeter,
            Enumeration enumeration, String headerFormat, String superHeaderFormat) {
		log.debug("ArchiveWithHeaders created, enumeration: "+enumeration.toString()+ " top:"+addToTop);
		this.addToTop = addToTop;
		this.delimeter = delimeter;
		archivePartialText = "";	
		this.enumeration = enumeration;
        this.headerFormat = headerFormat;
        this.superHeaderFormat = superHeaderFormat;
		if(parseCount>=0)
			this.HOW_MANY_ITEMS_TO_PARSE = parseCount;
		parts = new ArrayList<Section>();
		
		init(lines);
	}
	
	public void init(String lines[]) {
		if(addToTop) {
			parseTop(lines);
		} else {
			parseBottom(lines);
		}		
	}

    @Override
    public void add(String item, Calendar c) {
        if (c == null) {
            addNoDate(item);
        } else {
            String thisHeader = ArchiveSettings.getHeaderForDate(c, headerFormat);
            String superHeader = ArchiveSettings.getHeaderForDate(c, superHeaderFormat);
            if (superHeader==null) {
                addWithHeader(item, thisHeader);
            } else {
                addWithHeaderAndSuperHeader(item, thisHeader, superHeader);
            }
        }
    }

    protected void addNoDate(String item) {
		newLines++;
		if(parts.isEmpty()) {
			Section section = createSection(enumeration, false);
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

    protected void addWithHeader(String item, String header) {
		newLines++;
		Section part= null;
        String headerName = StringUtils.strip(header, "=").trim();
		if(addToTop) {
            part = findFirstSectionByName(headerName);
            if (part == null) {
                part = createSection(enumeration, headerName, header, false);
                parts.add(0, part);
			}
            part.addItemToBegin(item);
		} else {
            part = findLastSectionByName(headerName);
            if(part == null) {
                part = createSection(enumeration, headerName, header, false);
				parts.add(part);
			}
            part.addItemToEnd(item);
		}
	}

    @Nullable
    Section findFirstSectionByName(String name) {
        for (int i = 0; i < parts.size(); i++) {
            Section section = parts.get(i);
            if (name.equals(section.getHeader())) {
                return section;
            }
        }
        return null;
    }

    @Nullable
    Section findLastSectionByName(String name) {
        for (int i = parts.size() - 1; i >= 0; i--) {
            Section section = parts.get(i);
            if (name.equals(section.getHeader())) {
                return section;
            }
        }
        return null;
    }

    public void addWithHeaderAndSuperHeader(String item, String header, String superHeader) {
		newLines++;
        String superHeaderName = StringUtils.strip(superHeader, "=").trim();
		String headerStripped = StringUtils.strip(header, "=").trim();
		Section part = null;
		int hhIndex = -1;
		hhIndex = findSuperHeader(superHeaderName);
        // если заголовок верхнего уровня не найден,
        // значит его нужно создать
		if(hhIndex<0) {
			if(addToTop) {		
                part = createSection(enumeration, headerStripped, header, superHeaderName,
                        superHeader, false);
				parts.add(0, part);
				part.addItemToBegin(item);
				return;
			} else  {
				boolean createNewSection = true;
                if (parts.size() != 0 && parts.get(0).getSuperHeader() == null) {
                    // нужно проверить, последние хедэры наши(относятся к заголовку) или нет
					if(latestItemHeaderHeader!=null && 
                            latestItemHeaderHeader.compareTo(superHeader) == 0) {
						createNewSection = false;						
					} 
				} 
				if(createNewSection) {
                    part = createSection(enumeration, headerStripped, header, superHeaderName,
                            superHeader, false);
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
                part.clearSuperHeader();
                part = createSection(enumeration, headerStripped, header, superHeaderName,
                        superHeader, false);
				parts.add(hhIndex, part);
				part.addItemToBegin(item);				
			} else {
				part = createSection(enumeration, headerStripped,header,false);
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
				Section items = parts.get(0);
                if (items.getSuperHeader() == null) {
					for(int i=items.getSize()-1;i>=0;i--) {
						Calendar c = NewPages.getNewPagesItemDate(wiki, items.getItem(i));
						if(c!=null) {
                            latestItemHeaderHeader = aSettings.getSuperHeaderForDate(c);
							log.debug("latestItemHeaderHeader : "+latestItemHeaderHeader+" for date: "+c.toString());
							return;
						}
					}
				}
			}
		}
	}

	public int findSuperHeader(String hh) {
		int index = -1;
		Section part = null;
		if(addToTop) {
			for(int i=0;i<parts.size();i++) {
				part = parts.get(i);
                if (part.getSuperHeader() != null && part.getSuperHeader().equals(hh)) {
					index = i;
					break;
				}
			}
		} else {
			for(int i=parts.size()-1;i>=0;i--) {
				part = parts.get(i);
                if (part.getSuperHeader() != null && part.getSuperHeader().equals(hh)) {
					index = i;
					break;
				}
			}
		}
		return index;
	}

	public int findHeaderInSuperSection(String header, int startFrom) {
		Section part = parts.get(startFrom);
        String hh = part.getSuperHeader();
		for(int i = startFrom;i<this.parts.size();i++) {
			part = parts.get(i);
            if (part.getSuperHeader() != null) {
                if (hh == null) return -1;
                else if (!part.getSuperHeader().equals(hh)) return -1;
            }
            if (part.getHeader() != null && part.getHeader().equals(header)) return i;
		}		
		return -1;
	}

	public int findLastSectionInSuperSection(int startFrom) {
		Section part = parts.get(startFrom);
        String hh = part.getSuperHeader();
		int i = 0;
		for(i = startFrom;i<this.parts.size();i++) {
			part = parts.get(i);
            if (part.getSuperHeader() != null) {
                if (hh == null) break;
                else if (!part.getSuperHeader().equals(hh)) break;
            }
		}		
		return (i-1);
	}

	public void update(NirvanaWiki wiki, String archiveName, boolean minor, boolean bot) throws LoginException, IOException {
		String text = this.toString();
		if(!text.isEmpty())
            wiki.edit(archiveName, text, "+" + newItemsCount() + " статей", minor, bot);
	}
}
