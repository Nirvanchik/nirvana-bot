/**
 *  @(#)ArchiveWithHeaders.java
 *  Copyright © 2020 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import static org.wikipedia.nirvana.archive.EnumerationUtils.OL;
import static org.wikipedia.nirvana.archive.EnumerationUtils.OL_END;

import org.wikipedia.nirvana.archive.ArchiveSettings.Enumeration;
import org.wikipedia.nirvana.nirvanabot.NewPages;
import org.wikipedia.nirvana.util.StringTools;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;

// TODO: Algorithm is bad. 
// New pages items searcher can search older or newer items in random order.
// Example 1. New pages list has pages [A, B, C, D, E] (from older to newer). Someone adds to 
// settings "ignore category" which removes E from search results. After some update the list will
// be: [E, B, C, D, F] where E is newer that B, C, D. So, E will be archived and create section E'.
// Then B, C, D will be archived and the bot will think that whey are newer than last section (E')
// but they are not.
/**
 * Archive page that has headers.
 * Currently supports one or two levels of headers.
 *
 */
public class ArchiveWithHeaders extends Archive {
    public static int HOW_MANY_ITEMS_TO_PARSE_DEFAULT = 100;

    private final Enumeration enumeration;
    private int newLines = 0;

    /**
     * Old text of the archive (partial) which was not parsed for better performance.
     */
    private String archivePartialText = "";

    protected List<Section> parts;

    protected int numberOfItemsToParse = HOW_MANY_ITEMS_TO_PARSE_DEFAULT;

    /**
     * Super header of the latest old item in the archive. When we parse old archive from bottom
     * in optimized mode (skip most of archive text, take only N last items) we may miss super
     * header line in the parsed block. In order to find out what super header current section is
     * we guess it by parsing last item, taking its date and calculating hypothetical super header
     * of this section.
     */
    private String latestItemHeaderHeader = null;
    private String latestItemHeader = null;

    /**
     * Format string to generate header.
     *
     * @see ArchiveSettings#headerFormat
     */
    public String headerFormat;

    /**
     * Format string to generate top-level header.
     *
     * @see ArchiveSettings#superHeaderFormat
     */
    public String superHeaderFormat;

    protected Section createSection(Enumeration enumeration, boolean old) {
        return new Section(enumeration,old);        
    }

    protected Section createSection(Enumeration enumeration,
            String header,    String headerText,    boolean old) {
        return new Section(enumeration,header,headerText,old);        
    }

    protected Section createSection(Enumeration enumeration, String header, String headerText,
            String superHeader, String superHeaderText, boolean old) {
        return new Section(enumeration, header, headerText, superHeader, superHeaderText, old);
    }

    /**
     * Contains one section including section header (or section headers), archived items in this
     * section. 
     */
    protected class Section {
        private Enumeration enumeration = Enumeration.NONE;
        /**
         * Single header, or secondary header.
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
        // The only porpose of this flag - for toString() method, for archive with custom HTML
        // enumeration, for the case when new pages settings switches classic '*'/'#' enumeration
        // to HTML enumeration for archive and old archive still has '*'/'#' enumerated items that
        // should be converted to new way.
        private boolean old;
        // TODO: Bad and complicated logic. Instead of this. If some old section appears to be not
        // HTML enumerated and current settings require HTML enumeration, read and parse all archive
        // and add HTML enumeration everywhere.
        /**
         * True means that archive was not fully parsed. Some part of the archive was skipped
         * and some section can be not fully parsed (trancated). Used by HTML-enumeration logic.
         * Old section which was trancated and which was not HTML-enumerated cannot be
         * HTML-enumerated.
         */
        protected boolean trancated;

        /**
         * Constructs new or old empty section.
         * 
         * @param enumeration enumeration type of the header.
         * @param old old flag (true for old sections).
         */
        Section(Enumeration enumeration, boolean old) {
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

        /**
         * Constructs new or old section with a known header.
         *
         * @param enumeration enumeration type of the header.
         * @param header section header.
         * @param headerText full text of header line (includes formatting code).
         * @param old old flag (true for old sections).
         */
        Section(Enumeration enumeration, String header, String headerText, boolean old) {
            log.debug("section created: {}", headerText);
            this.enumeration = enumeration;
            this.header = header;
            this.headerText = headerText;
            superHeader = null;
            superHeaderText = "";
            items = new ArrayList<String>();
            this.old = old;
            trancated = old;    
        }

        /**
         * Constructs new section with two headers.
         * 
         * @param enumeration enumeration type of the header.
         * @param header section header.
         * @param headerText full text of header line (includes formatting code).
         * @param superHeader top-level section header.
         * @param superHeaderText full text of top-level header line (includes formatting code).
         * @param old old flag (true for old sections).
         */
        Section(Enumeration enumeration, String header, String headerText, String superHeader,
                String superHeaderText, boolean old) {
            log.debug("section created: {}", headerText);
            log.debug("super header: {}", superHeaderText);
            this.enumeration = enumeration;
            this.header = header;
            this.headerText = headerText;
            this.superHeader = superHeader;
            this.superHeaderText = superHeaderText;
            items = new ArrayList<String>();
            this.old = old;
            trancated = old;    
        }

        /**
         * Return header of this section.
         */
        public String getHeader() {
            return header;
        }

        /**
         * @return Full header text including formatting.
         */
        public String getHeaderText() {
            return headerText;
        }

        /**
         * @return top-level header.
         */
        public String getSuperHeader() {
            return superHeader;
        }

        /**
         * Erase top-level header.
         */
        public void clearSuperHeader() {
            this.superHeader = null;
            this.superHeaderText = "";
        }

        /**
         * Prints all section contents to string.
         */
        public String toString() {
            StringBuffer buf = new StringBuffer();
            if (!this.superHeaderText.isEmpty()) {
                buf.append(superHeaderText);
                buf.append("\n");
            }
            if (!this.headerText.isEmpty()) {
                buf.append(headerText);
                buf.append("\n");
            }
            if (enumeration == Enumeration.HTML && (!trancated || hasOL)) {
                buf.append(OL);
                buf.append("\n");
            }
            // TODO: Merge this code with enumItem()?
            // TODO: May be do this convertion separately and globally after parsing?
            // Convert usual enumeration symbols (#,*) in the old items if this section is old
            // and enum
            if (!trancated && !this.hasOL && this.old && (enumeration == Enumeration.HTML ||
                    enumeration == Enumeration.HTML_GLOBAL)) {
                for (int i = 0; i < items.size(); i++) {
                    String item = items.get(i);
                    if (!item.startsWith("<li>")) {
                        if (item.startsWith("#") || item.startsWith("*")) {
                            item = "<li>" + item.substring(1);
                        } else {
                            item = "<li> " + item;
                        }
                        items.set(i, item);
                    }
                }
            }
            for (String item: items) {
                buf.append(item).append("\n");
            }
            if (enumeration == Enumeration.HTML && (!trancated || hasOL)) {
                buf.append(OL_END);
                buf.append("\n");
            }
            return buf.toString();
        }

        /**
         * Add enumeration html code to the archived item.
         *
         * @param item new page item.
         * @return item with enumeration code at the beginning.
         */
        public String enumItem(String item) {
            String str = item;
            if (enumeration == Enumeration.HTML_GLOBAL ||
                    (enumeration == Enumeration.HTML && 
                        (this.hasOL || !this.trancated))) {
                if (item.startsWith("#") || item.startsWith("*")) {
                    str = "<li>" + item.substring(1);
                } else {
                    str = "<li> " + item;
                }
            } else if (enumeration == Enumeration.HASH) {
                if (str.startsWith("#")) {
                    // do nothing
                } else if (str.startsWith("*")) {
                    str = "#" + str.substring(1);
                } else {
                    str = "# " + str;
                }
            }
            return str;
        }

        void addOldItemToEnd(String item) {            
            items.add(item);
        }

        void addOldItemToBegin(String item) {
            items.add(0, item);
        }

        void addItemToEnd(String item) {            
            items.add(enumItem(item));
        }

        void addItemToBegin(String item) {
            items.add(0, enumItem(item));
        }

        /**
         * Sets header or top-level header for this section.
         * Should be used when parsing old archive items.
         * If section has no header, it will set header.
         * If section already has header, and has no super-header it will promote header to
         * super-header and set new header as its normal header.
         * If section has both header and super header behavior is undefined (should never happen).
         * 
         * @param header header title.
         * @param headerFullString full header text with formatting.
         */
        public void pushHeader(String header, String headerFullString) {
            // TODO: What? Add assert instead.
            if (superHeader != null) return;
            if (this.header == null) {
                this.header = header;
                this.headerText = headerFullString;
            } else {
                this.superHeader = this.header;
                this.superHeaderText = this.headerText;
                this.header = header;
                this.headerText = headerFullString;
            }
        }

        /**
         * Sets top-level header for this section.
         * Should be used when parsing old archive items.
         * If section has no top-level header, it will set header.
         * If section already has top-level header it will the behavior is undefined
         * (should never happen).
         * 
         * @param header header title.
         * @param headerFullString full header text with formatting.
         */
        public void pushSuperHeader(String header, String headerFullString) {
            // TODO: What? Add assert instead.
            if (superHeader != null) return;
            this.superHeader = header;
            this.superHeaderText = headerFullString;
        }
        
        /**
         * Returns true if this section contains no items.
         */
        public boolean isEmpty() {
            return items.isEmpty();
        }

        /**
         * Returns archived item in section specified by index.
         */
        public String getItem(int index) {
            return this.items.get(index);
        }

        /**
         * @return items count in this section loaded by bot. It may not count all items when bot
         *         reads section partially.
         */
        public int getSize() {
            return this.items.size();
        }
    } // class Section

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (this.enumeration == Enumeration.HTML_GLOBAL) {
            buf.append(OL);
            buf.append("\n");
        }
        if (!addToTop) {
            buf.append(this.archivePartialText);
        }
        
        for (Section part:parts) {            
            buf.append(part.toString());
        }
        
        if (addToTop && !archivePartialText.isEmpty()) {
            buf.append(this.archivePartialText);            
            if (!archivePartialText.endsWith("\n")) { 
                buf.append("\n");
            }
        }
        
        if (this.enumeration == Enumeration.HTML_GLOBAL) {            
            buf.append(OL_END);
        }
        
        return buf.toString();
    }
    
    void parseTop(String[] lines) {
        int i = 0;
        while (i < lines.length && lines[i].isEmpty()) i++;
        if (i < lines.length && lines[i].compareToIgnoreCase(OL) == 0) i++;
        else if (i < lines.length && lines[i].startsWith(OL)) {
            lines[i] = lines[i].substring(OL.length());
        }
        int j = lines.length - 1;
        while (j >= 0 && lines[j].isEmpty()) j--;
        if (j >= 0 && lines[j].compareToIgnoreCase(OL_END) == 0) j--;
        else if (j >= 0 && lines[j].endsWith(OL_END)) {
            lines[j] = lines[j].substring(0, lines[j].length() - OL.length());
        }
        int first = 0;
        int last = (j - i) > numberOfItemsToParse ? numberOfItemsToParse - 1 : j;
        if (last < j) {
            archivePartialText = StringUtils.join(lines, "\n", last + 1, j + 1);
        }
        parseTopLines(lines, first, last);
    }

    void parseTop(String text) {
        parseTop(text.split("\n"));
    }

    void parseTopLines(String[] oldItems, int first, int last) {    
        
        Pattern p = Pattern.compile("^==(=?)(\\s*)(?<headername>[^=]+)(\\s*)(=?)==$");
        Section part = null;
        boolean trancated = !this.archivePartialText.isEmpty();
        for (int i = first; i <= last; i++) {
            String item = oldItems[i];
            if (item.isEmpty()) continue;
            Matcher m = p.matcher(item);
            if (m.matches()) {
                if (part != null && part.getSuperHeader() == null && part.isEmpty() &&
                        (StringTools.countPaddedCharsAtLeft(part.getHeaderText(), '=') <
                                StringTools.countPaddedCharsAtLeft(item, '='))) {
                    part.pushHeader(m.group("headername").trim(),item);         
                } else {
                    if (part != null) {
                        part.trancated = false;
                    }
                    part = createSection(enumeration,m.group("headername").trim(),item,true);
                    part.trancated = trancated;
                    parts.add(part);
                }
            } else {
                if (part == null) {
                    part = createSection(enumeration, true);
                    part.trancated = trancated;
                    parts.add(part);                
                }
                if (item.compareToIgnoreCase("<ol>") == 0) {
                    part.hasOL = true;
                } else if (item.compareToIgnoreCase("</ol>") == 0) {
                    part.trancated = false;
                } else {
                    part.addOldItemToEnd(item);
                }
            }           
        }
    }

    void parseBottom(String[] lines) {    
        int i = 0;
        while (i < lines.length && lines[i].isEmpty()) i++;
        if (i < lines.length && lines[i].compareToIgnoreCase(OL) == 0) i++;
        int j = lines.length - 1;
        while (j >= 0 && lines[j].isEmpty()) j--;
        if (j >= 0 && lines[j].compareToIgnoreCase(OL_END) == 0) j--;
        
        int first;
        int last;
        last = j;
        first = (j - i) > numberOfItemsToParse ? j + 1 - numberOfItemsToParse : 0;
        if (first > 0) {
            archivePartialText = StringUtils.join(lines, "\n", 0, first) + "\n";
        }
        parseBottomLines(lines, first, last);
    }

    void parseBottom(String text) {
        parseBottom(text.split("\n"));
    }

    void parseBottomLines(String [] oldItems, int first, int last) {
        
        Pattern p = Pattern.compile("^==(=?)(\\s*)(?<headername>[^=]+)(\\s*)(=?)==$");
        Section part = null;
        boolean wasHeader = false;
        boolean trancated = !this.archivePartialText.isEmpty();
        for (int i = last; i >= first; i--) {
            String item = oldItems[i];
            if (item.isEmpty()) continue;
            Matcher m = p.matcher(item);
            if (part == null) {
                part = createSection(enumeration,true);
                part.trancated = trancated;
                parts.add(0,part);
            }
            if (m.matches()) {    
                wasHeader = true;
                part.trancated = false;
                if (part.getHeader() == null) {
                    part.pushHeader(m.group("headername").trim(),item);
                } else if (part.getHeader() != null && part.getSuperHeader() == null &&
                        (StringTools.countPaddedCharsAtLeft(part.getHeaderText(), '=') >
                                  StringTools.countPaddedCharsAtLeft(item, '='))) {
                    part.pushSuperHeader(m.group("headername").trim(),item);
                } else {
                    part = createSection(enumeration,m.group("headername").trim(),item,true);
                    part.trancated = trancated;
                    parts.add(0,part);
                } 
            } else {
                if (item.equalsIgnoreCase(OL)) {
                    part.trancated = false;
                } else if (item.equalsIgnoreCase(OL_END)) {
                    if (wasHeader) {
                        part = createSection(enumeration,true);
                        part.trancated = trancated;
                        parts.add(0,part);
                    }
                    part.hasOL = true;
                } else {
                    if (wasHeader) {
                        part = createSection(enumeration,true);
                        part.trancated = trancated;
                        parts.add(0,part);
                    }
                    part.addOldItemToBegin(item);
                }
                wasHeader = false;
            }
        }        
    }

    /**
     * Constructs ArchiveWithHeaders object with the specified archive text and required archive
     * options.
     * 
     * @param parseCount how many old items to parse. If 0 all old items will be parsed.
     * @param addToTop flag where to add new page items. <code>true</code> to add at top, 
     *     <code>false</code> to add at bottom.
     * @param enumeration enumeration type used for this archive.
     * @param headerFormat format string to generated header
     * @param superHeaderFormat format string to generated top-level header
     */
    public ArchiveWithHeaders(int parseCount, boolean addToTop,
            Enumeration enumeration, String headerFormat, String superHeaderFormat) {
        log.debug("ArchiveWithHeaders created, enumeration: {} top: {}", enumeration, addToTop);
        this.addToTop = addToTop;
        archivePartialText = "";    
        this.enumeration = enumeration;        
        this.headerFormat = headerFormat;
        this.superHeaderFormat = superHeaderFormat;
        parts = new ArrayList<Section>();
        if (parseCount >= 0) {
            numberOfItemsToParse = parseCount;
        }        
    }

    @Override
    public void read(NirvanaWiki wiki, String archivePage) throws IOException {
        String text = wiki.getPageText(archivePage);
        if (text == null) {
            text = "";
        }
        String oldText = EnumerationUtils.trimEnumerationAndWhitespace(text);
        if (addToTop) {
            parseTop(oldText);
        } else {
            parseBottom(oldText);
        }
        initHeadersOfBrokenSection(wiki);
    }

    @Override
    public void add(String item, @Nullable Calendar creationDate) {
        newLines++;
        if (creationDate == null) {
            addNoDate(item);
        } else {
            String thisHeader = ArchiveSettings.getHeaderForDate(creationDate, headerFormat);
            String superHeader = ArchiveSettings.getHeaderForDate(creationDate, superHeaderFormat);
            if (superHeader == null) {
                addWithHeader(item, thisHeader);
            } else {
                addWithHeaderAndSuperHeader(item, thisHeader, superHeader);
            }
        }
    }

    @Override
    public int newItemsCount() {
        return newLines;
    }

    /**
     * Add archived item without date.
     *
     * @param item archived item text.
     */
    protected void addNoDate(String item) {
        if (parts.isEmpty()) {
            Section section = createSection(enumeration, false);
            section.addItemToBegin(item);
            parts.add(section);
            return;
        }
        if (addToTop) {
            parts.get(0).addItemToBegin(item);
        } else {
            parts.get(parts.size() - 1).addItemToEnd(item);
        }
    }

    private void addWithHeader(String item, String header) {
        Section part = null;
        String headerName = StringUtils.strip(header, "=").trim();
        if (addToTop) {
            part = findFirstSectionByName(headerName);
            if (part == null) {
                part = createSection(enumeration, headerName, header, false);
                parts.add(0, part);
            }
            part.addItemToBegin(item);
        } else {
            part = findLastSectionByName(headerName);
            if (part == null) {
                part = createSection(enumeration, headerName, header, false);
                parts.add(part);
            }
            part.addItemToEnd(item);
        }
    }

    @Nullable
    private Section findFirstSectionByName(String name) {
        for (int i = 0; i < parts.size(); i++) {
            Section section = parts.get(i);
            if (name.equals(section.getHeader())) {
                return section;
            }
        }
        return null;
    }

    @Nullable
    private Section findLastSectionByName(String name) {
        for (int i = parts.size() - 1; i >= 0; i--) {
            Section section = parts.get(i);
            if (name.equals(section.getHeader())) {
                return section;
            }
        }
        return null;
    }

    private void addWithHeaderAndSuperHeader(String item, String header, String superHeader) {
        String superHeaderName = StringUtils.strip(superHeader, "=").trim();
        String headerStripped = StringUtils.strip(header, "=").trim();
        Section part = null;
        int hhIndex = -1;
        hhIndex = findSuperHeader(superHeaderName);
        // если заголовок верхнего уровня не найден,
        // значит его нужно создать
        if (hhIndex < 0) {
            if (addToTop) {
                part = createSection(enumeration, headerStripped, header, superHeaderName,
                        superHeader, false);
                parts.add(0, part);
                part.addItemToBegin(item);
                return;
            } else {
                boolean createNewSection = true;
                if (parts.size() != 0 && parts.get(0).getSuperHeader() == null) {
                    // нужно проверить, последние хедэры наши(относятся к заголовку) или нет
                    if (latestItemHeaderHeader != null && 
                            latestItemHeaderHeader.compareTo(superHeader) == 0) {
                        createNewSection = false;
                    } 
                } 
                if (createNewSection) {
                    part = createSection(enumeration, headerStripped, header, superHeaderName,
                            superHeader, false);
                    parts.add(part);
                    part.addItemToEnd(item);
                    return;
                } else {
                    hhIndex = 0;
                }
            }
        }
        
        // 1 найти хидер?
        int headerIndex = findHeaderInSuperSection(headerStripped,hhIndex);
        if (headerIndex < 0) {
            // создать подсекцию
            if (addToTop) {
                part = parts.get(hhIndex);
                part.clearSuperHeader();
                part = createSection(enumeration, headerStripped, header, superHeaderName,
                        superHeader, false);
                parts.add(hhIndex, part);
                part.addItemToBegin(item);
            } else {
                if (parts.size() != 0 && parts.get(0).trancated
                        && parts.get(0).getHeader() == null
                        && latestItemHeader != null
                        && latestItemHeader.compareTo(header) == 0
                        && hhIndex == 0) {
                    parts.get(0).addItemToEnd(item);
                } else {
                    part = createSection(enumeration, headerStripped, header, false);
                    parts.add(findLastSectionInSuperSection(hhIndex) + 1, part);
                    part.addItemToEnd(item);
                }
            }
        } else {
            part = parts.get(headerIndex);
            if (addToTop) {    
                part.addItemToBegin(item);
            } else {
                part.addItemToEnd(item);
            }                
        }
    }

    private void initHeadersOfBrokenSection(NirvanaWiki wiki) {
        latestItemHeaderHeader = null;
        latestItemHeader = null;
        if (!addToTop) {
            if (this.parts.size() > 0) {
                Section items = parts.get(0);
                if (items.getSuperHeader() == null) {
                    for (int i = items.getSize() - 1; i >= 0; i--) {
                        Calendar c = NewPages.getNewPagesItemDate(wiki, items.getItem(i));
                        if (c != null) {
                            latestItemHeaderHeader =
                                    ArchiveSettings.getHeaderForDate(c, superHeaderFormat);
                            latestItemHeader = ArchiveSettings.getHeaderForDate(c, headerFormat);
                            log.debug("latestItemHeaderHeader : {} for date: {}",
                                    latestItemHeaderHeader, c);
                            return;
                        }
                    }
                }
            }
        }
    }

    private int findSuperHeader(String hh) {
        int index = -1;
        Section part = null;
        if (addToTop) {
            for (int i = 0; i < parts.size(); i++) {
                part = parts.get(i);
                if (part.getSuperHeader() != null && part.getSuperHeader().equals(hh)) {
                    index = i;
                    break;
                }
            }
        } else {
            for (int i = parts.size() - 1; i >= 0; i--) {
                part = parts.get(i);
                if (part.getSuperHeader() != null && part.getSuperHeader().equals(hh)) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    private int findHeaderInSuperSection(String header, int startFrom) {
        Section part = parts.get(startFrom);
        String hh = part.getSuperHeader();
        for (int i = startFrom; i < this.parts.size(); i++) {
            part = parts.get(i);
            if (part.getSuperHeader() != null) {
                if (hh == null) {
                    return -1;
                } else if (!part.getSuperHeader().equals(hh)) {
                    return -1;
                }
            }
            if (part.getHeader() != null && part.getHeader().equals(header)) return i;
        }        
        return -1;
    }

    private int findLastSectionInSuperSection(int startFrom) {
        Section part = parts.get(startFrom);
        String hh = part.getSuperHeader();
        int i = 0;
        for (i = startFrom; i < this.parts.size(); i++) {
            part = parts.get(i);
            if (part.getSuperHeader() != null) {
                if (hh == null) {
                    break;
                } else if (!part.getSuperHeader().equals(hh)) {
                    break;
                }
            }
        }        
        return (i - 1);
    }

    @Override
    public void update(NirvanaWiki wiki, String archiveName, boolean minor, boolean bot)
            throws LoginException, IOException {
        String text = this.toString();
        if (!text.isEmpty()) {
            wiki.edit(archiveName, text, updateSummary(), minor, bot);
        }
    }
}
