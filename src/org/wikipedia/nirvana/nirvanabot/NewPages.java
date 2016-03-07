/**
 *  @(#)NewPages.java 14.12.2014
 *  Copyright © 2011 - 2014 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.StringUtils;
import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.DateTools;
import org.wikipedia.nirvana.HTTPTools;
import org.wikipedia.nirvana.NirvanaBasicBot;
import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.ServiceError;
import org.wikipedia.nirvana.StringTools;
import org.wikipedia.nirvana.WikiTools;
import org.wikipedia.nirvana.archive.Archive;
import org.wikipedia.nirvana.archive.ArchiveFactory;
import org.wikipedia.nirvana.archive.ArchiveSettings;
import org.wikipedia.nirvana.archive.ArchiveSettings.Enumeration;
import org.wikipedia.nirvana.archive.ArchiveSimple;
import org.wikipedia.nirvana.archive.ArchiveWithEnumeration;
import org.wikipedia.nirvana.archive.ArchiveWithHeaders;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.ProcessorCombinator;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.FetcherFactory;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.PageListFetcher;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.PageListProcessor;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.PageListProcessorFast;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.PageListProcessorSlow;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.RevisionWithId;
import org.wikipedia.nirvana.nirvanabot.tmplfinder.TemplateFindItem;
import org.wikipedia.nirvana.nirvanabot.tmplfinder.TemplateFinder;

/**
 * @author kin
 *
 */
public class NewPages implements PortalModule{
	private static final int WIKI_API_BUNCH_SIZE = 10;
	public static final String PATTERN_NEW_PAGES_LINE_WIKIREF_ARTICLE = "\\[\\[(?<article>[^\\]|]+)(|[^\\]]+)?\\]\\]";
	public static final String PATTERN_NEW_PAGES_LINE_TEMPLATE_ITEM = "\\{\\{(?<template>.+)\\}\\}";
	private static final String COMMENT_DELETED = "<span style=\"color:#966963\"> — '''Статья удалена'''</span>";
	protected String language;
	protected List<String> categories;
	protected List<String> categoriesToIgnore;
	protected List<List<String>> categoryGroups;
	protected List<List<String>> categoryToIgnoreGroups;
	protected Set<String> usersToIgnore;
	protected String pageName;
    protected String archive;
    protected ArchiveSettings archiveSettings;
    protected String format;
    protected String formatString;
    protected String header;
    protected String footer;
    protected String middle;
    protected int maxItems;
    protected int hours;
    protected WikiTools.Service service;
    protected String delimeter;    
    protected int depth;
    protected int namespace;
    protected String namespaceIdentifier;
    //protected boolean markEdits;
    protected boolean minor;
    protected boolean bot;
    protected boolean fastMode;
    
    protected PortalParam.Deleted deletedFlag;
    protected int renamedFlag;
    
	protected String headerLastUsed;
	protected String footerLastUsed;
	protected String middleLastUsed;
	
    protected Map<String,String> pageLists;
    protected Map<String,String> pageListsToIgnore;
    
    protected static org.apache.log4j.Logger log;
    
    protected GetRevisionMethod getRevisionMethod = GetRevisionMethod.GET_REV;
    
    protected boolean UPDATE_FROM_OLD = true;
    protected boolean UPDATE_ARCHIVE = true;
    
    protected String currentUser = null;
    
    protected List<TemplateFindItem> templatesWithData = null;
    
    static {
    	log = org.apache.log4j.Logger.getLogger(NewPages.class);
    }
    
    protected enum GetRevisionMethod {
    	GET_FIRST_REV,
    	GET_FIRST_REV_IF_NEED,
    	GET_FIRST_REV_IF_NEED_SAVE_ORIGINAL,
    	GET_REV,
    	GET_TOP_REV
    	//GET_LAST_REV_BY_PAGE_NAME
    }
    
	/**
	 *  Constructor
	 */
    public NewPages(PortalParam param) {
    	this.language = param.lang;
    	this.categories = param.categories;
    	this.categoriesToIgnore = param.categoriesToIgnore;
    	categoryGroups = param.categoryGroups;
    	categoryToIgnoreGroups = param.categoryToIgnoreGroups;
    	this.usersToIgnore = new HashSet<String>(param.usersToIgnore);
    	this.pageName = param.page;
    	this.archive = param.archive;
   		this.archiveSettings = param.archSettings;
    	this.maxItems = param.maxItems;
    	this.format = param.format;
    	
    	this.formatString = format.replace("%(название)", "%1$s").replace("%(автор)", "%2$s").replace("%(дата)", "%3$s");
    	/*
    	TODO(Nirvanchik): I don't understand what is this for?
    	if(param.deletedFlag==PortalParam.Deleted.MARK) {
    		this.formatString = formatString.replace("%(удалено)", "%4$s");
    	}
    	*/
    	this.hours = param.hours;
    	//this.Module = module;
    	this.delimeter = param.delimeter;
    	this.depth = param.depth;
    	this.header = param.header;
    	if(header==null) this.header = "";
    	this.headerLastUsed = this.header;
    	this.footer = param.footer;
    	if(footer==null) this.footer = "";
    	this.footerLastUsed = this.footer;
    	this.middle = param.middle;
    	if(middle==null) this.middle = "";
    	this.middleLastUsed = this.middle;
    	this.namespace = param.ns;
    	this.minor = param.minor;
    	this.bot = param.bot;
    	this.deletedFlag = param.deletedFlag;
    	this.renamedFlag = param.renamedFlag;
    	this.service = param.service;
    	this.fastMode = param.fastMode;
    	this.templatesWithData = param.templatesWithData;
    	
    	log = org.apache.log4j.Logger.getLogger(this.getClass().getName());
    	log.debug("Portal module created for portal subpage [["+this.pageName+"]]");
	}
    
    protected String getFormatString() {
    	return formatString;
    }
    
    public String getOldText(Wiki wiki) throws IOException {
    	try
		{
			return wiki.getPageText(this.pageName);
		} catch (java.io.FileNotFoundException e) {
			return "";
		}
    }

	public class Data {		
		String newText;
		String archiveText;
		List<String> archiveItems = null;
		int newPagesCount = 0;
		int archiveCount = 0;
		int deletedCount = 0;
		int oldCount = 0;
		public Data() {
			if (archive != null) {
				archiveItems = new ArrayList<String>();
			}
		}
		public void makeArchiveText() {
			if(archive!=null && archiveItems!=null && archiveItems.size()>0) {
				if(archiveSettings.enumeration==Enumeration.HASH) {
					enumerateWithHash(archiveItems);
				}
				archiveText = StringUtils.join(archiveItems.iterator(),delimeter) + "\n";
			}
		}
	}
			
	private String getPortalName() {
		String str = pageName;
		int colon = str.indexOf(':');
		if (colon>=0) {
			str = str.substring(colon+1);
		}
		int slash = str.indexOf('/');
		if (slash > 0) {
			str = str.substring(slash);
		}
		return str;
	}
	
	protected String substParams(String item, boolean constantOnly) {
		if (item.isEmpty()) return item;
		String portal = getPortalName();
		Calendar c = Calendar.getInstance();
		String date = DateTools.printDateDayMonthYearGenitiveRussian(c);
		String str = item.replace("%(бот)", getCurrentUser())
				.replace("%(проект)", portal)
				.replace("%(портал)", portal)
				.replace("%(страница)", pageName);
		if (archiveSettings != null) {
    		String archive = archiveSettings.getArchiveForDate(c);
    		if (archive != null) {
    			str = str.replace("%(архив)", archive);
    		}
		}
		if (!constantOnly) {
			str = str.replace("%(дата)", date);
		}
		return str;
	}
	
	protected class NewPagesBuffer {
		protected NirvanaWiki wiki;
		protected String header;
		protected String middle;
		protected String footer;
		protected List<String> subset = new ArrayList<String>();
		List<String> includedPages = new ArrayList<String>();
		public NewPagesBuffer(NirvanaWiki wiki) {
			this.wiki = wiki;
			this.header = substParams(NewPages.this.header, false);
			this.middle = NewPages.this.middle;
			this.footer = substParams(NewPages.this.footer, false);
		}
		
		public boolean contains(String item) {
			return subset.contains(item);
		}
		
		public int size() {
			return subset.size();
		}
		
		public boolean checkIsDuplicated(String archiveItem) {
			if (contains(archiveItem)) {
				return true;
			}
			for(String title:includedPages) {
	    		String variant1 = "[["+title+"]]";		        		
	    		String variant2 = "|"+
	    				pageTitleNormalToEscaped(
	    						title.substring((namespace==0)?0:namespaceIdentifier.length()+1)
	    						)+
	    				"|";
	    		if(archiveItem.contains(variant1) ||
	    				archiveItem.contains(variant2)) {
	    			return true;	    			
	    		}
	    	}
			return false;
		}
		
		public String getNewText(String bots) {
			String text;
			if (middle.isEmpty()) {
				text = bots + header + StringUtils.join(subset.toArray(), delimeter) + footer;
			} else if (subset.size()>1) {
				int m = (subset.size()+1)/2; // 2->1, 3->2, 4->2
				text = bots + header + 
						StringUtils.join(subset.subList(0, m), delimeter) + 
						middle +
						StringUtils.join(subset.subList(m, subset.size()), delimeter) +
						footer;
			} else {
				text = bots + header + StringUtils.join(subset, delimeter) + middle + footer;
			}
			return text;
		}
		
		protected void addOldItem(String item) {
			subset.add(item);
		}
		
		protected String formatTimeString(Revision rev) {
			if(NirvanaBot.TIME_FORMAT.equalsIgnoreCase("long")) 
	    		return rev.getTimestamp().getTime().toString();
	    	else {
	    		return String.format("%1$tFT%1$tTZ",rev.getTimestamp());
	    	}
		}
		
		String formatTitleString(String title) {
	    	String titleToInsert = title;
	    	if(namespace!=0) {
	    		log.debug("namespace = "+namespaceIdentifier+" namespace="+String.valueOf(namespace)+" title="+title);
	    		titleToInsert = title.substring(namespaceIdentifier.length()+1);
	    	}
	    	if(format.contains("{{") && format.contains("}}")) {
	    		titleToInsert = pageTitleNormalToEscaped(titleToInsert); // replaces '=' equal-sign by escape-code
	    	}
	    	return titleToInsert;
		}
		
		String formatItemString(String title, boolean deleted, Revision rev) {
			String element;
			String user = rev.getUser();
			String time = formatTimeString(rev);	    	
	    	String titleToInsert = formatTitleString(title);

	    	element = String.format(formatString, titleToInsert, HTTPTools.removeEscape(user), time);
	    	if(deletedFlag==PortalParam.Deleted.MARK && deleted) {
	    		element = markDeleted(element);
	    	}
	    	return element;
		}
		
		protected void addNewItem(String title, boolean deleted, Revision rev) throws IOException {
			String element = formatItemString(title, deleted, rev);    	
	    	
	        if (!subset.contains(element))
	        {
	            subset.add(element);
	            if (includedPages != null) {
	            	includedPages.add(title);
	            }
	            log.debug("ADD new line: \t"+element);
	        }
		}
		
		/*
		protected void addNewItem(String item) throws IOException {
	        if (!subset.contains(item))
	        {
	            subset.add(item);
	            if (includedPages != null) {
	            	includedPages.add(item);
	            }
	            log.debug("ADD new line: \t"+item);
	        }
		}*/

		protected void addNewPage(NirvanaWiki wiki, Revision pageInfo) throws IOException {
			Revision page = null;
		    switch(getRevisionMethod) {
		    	case GET_FIRST_REV:
		    		page = wiki.getFirstRevision(pageInfo.getPage());
		    		break;
		    	case GET_REV:
		    		page = wiki.getRevision(pageInfo.getRevid());
		    		break;
		    	case GET_TOP_REV:
		    		page = wiki.getTopRevision(pageInfo.getPage());
		    		break;
		    	case GET_FIRST_REV_IF_NEED:
		    		if(usersToIgnore.size()==0) {
		    			page = pageInfo;
		    		} else {		    			
		    			page = wiki.getFirstRevision(pageInfo.getPage());
		    		}
		    		break;
		    	case GET_FIRST_REV_IF_NEED_SAVE_ORIGINAL:
		    		if(usersToIgnore.size()==0) {
		    			page = pageInfo;
		    		} else {
		    			// TODO (Nirvanchik): here we may lost an important information
		    			Revision r = wiki.getFirstRevision(pageInfo.getPage());
		    			if (r != null) {
		    				pageInfo.setUser(r.getUser());
		    			}
		    		}
		    		break;
		    	default:
		    		throw new Error("not supported mode");
		    }
		    
		    if (page != null && !usersToIgnore.contains(HTTPTools.removeEscape(page.getUser())))
		    {
				String title_old = HTTPTools.removeEscape(pageInfo.getPage());
		    	String title_new = HTTPTools.removeEscape(page.getPage());
		    	log.debug("check page, title old: "+title_old+", title new: "+title_new);
		    	boolean deleted = false;
		    	if (deletedFlag != PortalParam.Deleted.DONT_TOUCH && service.hasDeleted) {		    		 
	                if(!wiki.exists(title_new)) {	                
	                	log.warn("page " +title_new+" deleted"); // page was created and renamed or deleted after that
	                	deleted = true;
	                	if (deletedFlag==PortalParam.Deleted.REMOVE) 
	                		return;
	                }
		    	}   	
		    	
		    	boolean renamed = !title_new.equals(title_old);
		    	
		    	if(renamed) {
			    	if((renamedFlag & PortalParam.RENAMED_NEW) != 0 ) {
			    		addNewItem(title_new,deleted,page);
			    	} 
			    	if((renamedFlag & PortalParam.RENAMED_OLD) != 0) {
			    		addNewItem(title_old,deleted,page);
			    	} else {
			    		if (includedPages != null) {
			    			includedPages.add(title_old);
			    		}
			    	}		    	
		    	} else {
		    		addNewItem(title_new,deleted,page);
		    	}
		    }
		}	
	}
	
	protected PageListProcessor createPageListProcessorWithFetcher(WikiTools.Service service, PageListFetcher fetcher,
			List<String> categories, List<String> categoriesToIgnore) {
		if (service.supportsFastMode() && fastMode) {
			return new PageListProcessorFast(service, categories, categoriesToIgnore, language, depth, namespace, fetcher);
		} else {
			return new PageListProcessorSlow(service, categories, categoriesToIgnore, language, depth, namespace, fetcher);			
		}
	}
	
	protected PageListProcessor createPageListFetcherForGroup(List<String> categories, List<String> categoriesToIgnore) {
		FetcherFactory.NewPagesFetcher fetcher = new FetcherFactory.NewPagesFetcher(hours);
		return createPageListProcessorWithFetcher(this.service, fetcher, categories, categoriesToIgnore);
	}
	
	protected PageListProcessor createPageListProcessor() {
		List<PageListProcessor> fetchers = new ArrayList<PageListProcessor>(3);
		fetchers.add(createPageListFetcherForGroup(this.categories, this.categoriesToIgnore));
		for(int i = 0;i<categoryGroups.size();i++) {
			if (categoryGroups.get(i).size()>0) {
				fetchers.add(createPageListFetcherForGroup(categoryGroups.get(i), categoryToIgnoreGroups.get(i)));
			}
		}
		if(fetchers.size()>1)
			return new ProcessorCombinator(fetchers);
		else
			return fetchers.get(0);
	}
	
	public void sortPagesByRevision(ArrayList<Revision> pageInfoList) {
		java.util.Collections.sort(pageInfoList, new Comparator<Revision>() {

			@Override
			public int compare(Revision r1, Revision r2) {				
				return (int)(r2.getRevid() - r1.getRevid());
			}		
			
		});
	}

	public void sortPagesById(ArrayList<Revision> pageInfoList) {
		java.util.Collections.sort(pageInfoList, new Comparator<Revision>() {

			@Override
			public int compare(Revision r1, Revision r2) {				
				return (int)(((RevisionWithId)r2).getId() - ((RevisionWithId)r1).getId());
			}		
			
		});
	}
	
	public void sortPagesByName(ArrayList<Revision> pageInfoList) {
		java.util.Collections.sort(pageInfoList, new Comparator<Revision>() {

			@Override
			public int compare(Revision r1, Revision r2) {				
				return r1.getPage().compareTo(r2.getPage());
			}		
			
		});
	}
	
	public void sortPagesByDate(ArrayList<Revision> pageInfoList) {
		java.util.Collections.sort(pageInfoList, new Comparator<Revision>() {

			@Override
			public int compare(Revision r1, Revision r2) {				
				return r2.getTimestamp().compareTo(r1.getTimestamp());
			}		
			
		});
	}
	
	public void sortPages(ArrayList<Revision> pageInfoList, boolean byRevision) {
		if(byRevision) {
			sortPagesByRevision(pageInfoList);
		} else {
			sortPagesById(pageInfoList);
		}
	}
	


	
	protected ArrayList<Revision> getNewPages(NirvanaWiki wiki) throws IOException, InterruptedException, ServiceError {
		PageListProcessor pageListProcessor = createPageListProcessor();
		log.info("Using pagelist fetcher: "+pageListProcessor);
		if (getRevisionMethod == GetRevisionMethod.GET_REV) {
    		if (pageListProcessor.revisionAvailable()) {
    			getRevisionMethod = GetRevisionMethod.GET_REV;
    		} else {
    			getRevisionMethod = GetRevisionMethod.GET_FIRST_REV;
    		}
		}
		ArrayList<Revision> pageInfoList = pageListProcessor.getNewPages(wiki);
		
		pageInfoList = filterPagesByCondition(pageInfoList, wiki);
		
		sortPages(pageInfoList, pageListProcessor.revisionAvailable());		
	
		if(pageListProcessor.mayHaveDuplicates()) {
			removeDuplicatesInSortedList(pageInfoList);
		}
		return pageInfoList;
	}
	
	protected ArrayList<Revision> filterPagesByCondition(ArrayList<Revision> pageInfoList, NirvanaWiki wiki) throws IOException {
		if (this.templatesWithData == null || templatesWithData.size() == 0) {
			return pageInfoList;
		}
		ArrayList<Revision> list = new ArrayList<Revision>();
		TemplateFinder finder = new TemplateFinder(templatesWithData.toArray(new TemplateFindItem[0]), wiki);
		for (Revision r: pageInfoList) {
			if (finder.find(r.getPage())) {
				list.add(r);
			}
		}
		return list;
	}
	
	protected String trimRight(String text, String right) {
		String textTrimmed = text;
		if (!right.isEmpty() && textTrimmed.endsWith(right)) {				
			textTrimmed = text.substring(0, textTrimmed.length() - right.length());
			textTrimmed = StringTools.trimRight(textTrimmed);
		} else {
			textTrimmed = StringTools.trimRight(textTrimmed);
			if (!right.isEmpty() && textTrimmed.endsWith(right)) {				
				textTrimmed = text.substring(0, textTrimmed.length() - right.length());
				textTrimmed = StringTools.trimRight(textTrimmed);
			}
		} 
		return textTrimmed;
	}
	
	protected String trimLeft(String text, String left) {
		String textTrimmed = text;
		if (!left.isEmpty() && textTrimmed.startsWith(left))
	    {
			textTrimmed = text.substring(left.length());
			textTrimmed = StringTools.trimLeft(textTrimmed);
	    } else {
	    	textTrimmed = StringTools.trimLeft(textTrimmed);
	    	if (!left.isEmpty() && textTrimmed.startsWith(left))
		    {
	    		textTrimmed = textTrimmed.substring(left.length());
	    		textTrimmed = StringTools.trimLeft(textTrimmed);
		    }
	    }
		return textTrimmed;
	}
	
	protected String trimMiddle(String text, String middle) {
		String textTrimmed = text;
		if(!middle.isEmpty()) {
	    	if (textTrimmed.contains(middle)) {
	    		textTrimmed = textTrimmed.replace(middle, delimeter);
	    	} else if(!middle.trim().isEmpty() && textTrimmed.contains(middle.trim())) {
	    		textTrimmed = textTrimmed.replace(middle.trim(), delimeter);
	    	}
	    }
		return textTrimmed;
	}
	
	protected String extractBotsAllowString(String text, String botsAllowString) {
		if (botsAllowString != null) {
			int pos = text.indexOf(botsAllowString);
			text = text.substring(0, pos) + text.substring(pos+botsAllowString.length());
		}
		return text;
	}
	
	public class PageInfo {
		String item;
		String title;
		boolean exists;
		public PageInfo(String item) {
			this.item = item;
			this.exists = true;
			if (deletedFlag==PortalParam.Deleted.REMOVE || deletedFlag==PortalParam.Deleted.MARK) {
				extractTitle();
			}
		}
		private void extractTitle() {
			title = getNewPagesItemArticle(item);
			if (title != null) title = pageTitleEscapedToNormal(title);
		}
	}
	
	private void checkDeleted(NirvanaWiki wiki, List<PageInfo> pages) throws IOException {
		ArrayList<String> titles = new ArrayList<String>();
		boolean pagesExist[];// = new boolean[bunch.size()];
		for (int j = 0; j < pages.size(); j++) {
			if (pages.get(j).title != null) {
				titles.add(pages.get(j).title);
			}
		}
		pagesExist = wiki.exists(titles.toArray(new String[0]));
		for (int j = 0, k = 0; j < pages.size(); j++) {
			if (pages.get(j).title != null) {
				pages.get(j).exists = pagesExist[k];
				k++;
			}	
		}
	}
	
	public void analyzeOldText(NirvanaWiki wiki, String text, Data d, NewPagesBuffer buffer) throws IOException {
		log.debug("analyzing old text");
		String oldText = text;
		String[] oldItems;
		
		// remove {{bots|allow=}} record
		String botsAllowString = NirvanaWiki.getAllowBotsString(text);
		
		log.debug("analyzing old text -> trancate header/footer/middle");
		oldText = extractBotsAllowString(oldText, botsAllowString);
		oldText = trimRight(oldText, footerLastUsed);		    	    
		oldText = trimLeft(oldText, headerLastUsed);	
		oldText = trimMiddle(oldText, middleLastUsed);
	    
	    //FileTools.dump(footer, "dump", this.pageName +".footer.txt");
	   
	    oldItems = StringUtils.splitByWholeSeparator(oldText, delimeter); // removes empty items
	    if(delimeter.equals("\n")) log.debug("delimeter is \\n");
	    else if(delimeter.equals("\r")) log.debug("delimeter is \\r");
	    else log.debug("delimeter is \""+delimeter+"\"");
	    
    
	    log.debug("analyzing old text -> parse items, len = "+oldItems.length);
	    
		d.oldCount = oldItems.length;
		int pos = 0;		
	    while (pos < oldItems.length && UPDATE_FROM_OLD && buffer.size() < maxItems)
	    {
	    	ArrayList<PageInfo> bunch = new ArrayList<PageInfo>(WIKI_API_BUNCH_SIZE);
	    	for(; bunch.size()<WIKI_API_BUNCH_SIZE && pos<oldItems.length; pos++) {
	    		String item = oldItems[pos];
	    		if (item.isEmpty()) continue;
		    	log.trace("check old line: \t"+item+"");
		    	if (buffer.checkIsDuplicated(item)) {
		    		log.debug("SKIP old line: \t"+item);
		    		continue;
		    	}
		    	bunch.add(new PageInfo(item));		    	
	    	}	    	
	    	if (this.deletedFlag==PortalParam.Deleted.REMOVE || this.deletedFlag==PortalParam.Deleted.MARK) {
	    		checkDeleted(wiki, bunch);    			
    		}
	    	int j = 0;
	    	for (; j < bunch.size() && buffer.size() < maxItems; j++) {
				String item = bunch.get(j).item;
				if (this.deletedFlag == PortalParam.Deleted.REMOVE && !bunch.get(j).exists) {
            		log.debug("REMOVE old line: \t"+item);
            		d.deletedCount++;
            	} else {
    				if (this.deletedFlag == PortalParam.Deleted.MARK ){
    					item = bunch.get(j).exists? unmarkDeleted(item):markDeleted(item);
    				}  
    				log.debug("ADD old line: \t"+item);
    				buffer.addOldItem(item);
        		}
	    	}
	    	pos = pos - bunch.size()+j;	    	
	    }
	    for(; pos < oldItems.length; pos++) {
	    	String item = oldItems[pos];
	    	if (item.isEmpty()) continue;
	    	log.trace("check old line: \t"+item+"");
	    	if (buffer.checkIsDuplicated(item)) {
	    		log.debug("SKIP old line: \t"+item);
	    		continue;
	    	}
	    	log.debug("ARCHIVE old line:"+item);
        	if(UPDATE_ARCHIVE && archive!=null) {		        		
        		d.archiveItems.add(item);
        	}
        	d.archiveCount++;
	    }
	    log.debug("bots allow string: "+botsAllowString);
		if (!botsAllowString.isEmpty()) {
			botsAllowString = botsAllowString+"\n";
		} 
		d.newText = buffer.getNewText(botsAllowString);
	}
	
	protected NewPagesBuffer createPagesBuffer(NirvanaWiki wiki) {
		return new NewPagesBuffer(wiki);
	}
	
	public Data getData(NirvanaWiki wiki, String text) throws IOException, InterruptedException, ServiceError {
		log.info("Get data for [[" + this.pageName+"]]");
		
		ArrayList<Revision> pageInfoList = getNewPages(wiki);
		
		NewPagesBuffer buffer = createPagesBuffer(wiki);
		int count = pageInfoList.size();
		count = count<maxItems?count:maxItems;
		for (int i = 0; i < count ; ++i)
		{
		    buffer.addNewPage(wiki, pageInfoList.get(i));
		}
	
		// Add elements from old page
		Data d = new Data();
				
	    
		if (true/*count < maxItems /*|| archive!=null*/) { 
			analyzeOldText(wiki, text, d, buffer);
		}
		
		if (UPDATE_ARCHIVE) d.makeArchiveText();
		
		//archiveItems.i
		d.newPagesCount = buffer.size() - (d.oldCount - d.archiveCount - d.deletedCount);
		if(d.newPagesCount<0) d.newPagesCount = 0;
		log.debug("updated items count: "+buffer.size());
		log.debug("old items count: "+d.oldCount);
		log.debug("archive count: "+d.archiveCount);
		log.debug("deleted count: "+d.deletedCount);
		log.debug("new items count: "+d.newPagesCount);
		
		return d;
	}
	
	

	/**
     * @param pageInfoList
     */
    private void removeDuplicatesInSortedList(ArrayList<Revision> list) {
    	log.debug("removing duplicates from list");
	    int i = 1;
	    while(i<list.size()) {
	    	if (list.get(i).getPage().equals(list.get(i-1).getPage())) {
	    		list.remove(i);
	    	} else {
	    		i++;
	    	}
	    }
    }

	public void enumerateWithHash(List<String> list) {
		for(int i =0; i<list.size();i++) {
			String item = list.get(i);
			if(item.startsWith("#")) {
				// do nothing
			} else if(item.startsWith("*")) {
				item = "#" + item.substring(1);
			} else {
				item = "# " + item;
			}
			list.set(i, item);
		}
		return;
	}
	
	protected String getCurrentUser(NirvanaWiki wiki) {
		if (this.currentUser == null) {
			Wiki.User user = wiki.getCurrentUser();
			this.currentUser = user.getUsername();
		}
		return this.currentUser;
	}
	
	protected String getCurrentUser() {
		return this.currentUser;
	}
	
	protected boolean checkAllowBots(NirvanaWiki wiki, String text) {
		
		log.debug("current user retrieved");
		if(!NirvanaWiki.allowBots(text, getCurrentUser(wiki))) {
			//reportData.status = Status.DENIED;
			log.info("bots/nobots template forbids updating this portal section");
			return false;
		}
		return true;
	}
		
	@Override
	public boolean update(NirvanaWiki wiki, ReportItem reportData, String comment) throws IOException, LoginException, InterruptedException, ServiceError {
		log.debug("=> update()");
		this.namespaceIdentifier = wiki.namespaceIdentifier(this.namespace);
		boolean updated = false;
		String text = getOldText(wiki);
		log.debug("old text retrieved");
		
		//this.botsAllowString = NirvanaWiki.getAllowBotsString(text);
		//getData(wiki);
		if (!checkAllowBots(wiki, text)) {
			return false;
		}
				
		getHeaderFooterChanges(wiki, reportData.template, reportData.portal);
				
		Data d = getData(wiki, text);
		reportData.pagesArchived = d.archiveCount;
		reportData.newPagesFound = d.newPagesCount;
		
		if(text==null) {
			log.trace("text = null");
			text="";
		}
		
		if(d.newText==null) {
			log.trace("d.newText = null");
			return false;
		}
	    
		if (d.newText!=null && 
				!d.newText.isEmpty() && 
				!(d.newText.equals(text) || d.newText.equals(text.trim())))
		{
		    
		    String str = "+"+String.valueOf(d.newPagesCount)+" новых";
		    if(UPDATE_ARCHIVE && archive!=null && d.archiveCount>0) {
		    	str = str + ", -"+d.archiveCount+" в архив";
		    }
		    if(this.deletedFlag == PortalParam.Deleted.REMOVE && d.deletedCount>0) {
		    	str = str + ", -"+d.deletedCount+ " удаленных";
		    }
		    log.info("Updating [[" + this.pageName+"]] " + str);
		    wiki.edit(pageName, d.newText, str, this.minor, this.bot);
		    updated = true;
		    reportData.updated = updated;
		    //wiki.Save(Page, newText, Module.UpdateComment, !MarkEdits ? MinorFlags.NotMinor : MinorFlags.None, MarkEdits);
		    if(UPDATE_ARCHIVE && archive!=null && (d.archiveText!=null || d.archiveItems.size()>0)) {		    	
		    	waitPauseIfNeed();
		    	log.info("Updating archive");
	    		updateArchive(wiki, d, reportData);
		    }
		}
		return updated;
	}
	
	protected void waitPauseIfNeed() {
		if(NirvanaBot.UPDATE_PAUSE>0) {
    		log.debug("Waiting "+ NirvanaBot.UPDATE_PAUSE+" ms");
	    	try {			    		 
	    		Thread.sleep(NirvanaBot.UPDATE_PAUSE);
	    	} catch (InterruptedException e) {					
		    	log.error(e.toString());
				e.printStackTrace();
			}
    	}
	}
	
	/**
	 * @throws IOException 
     * 
     */
    private void getHeaderFooterChanges(NirvanaWiki wiki, String template, String portalSettingsPage) throws IOException {
	    Revision rNewPages = wiki.getTopRevision(pageName);
	    if(rNewPages == null) {
	    	return;
	    }
	    
	    Revision []revs = wiki.getPageHistory(portalSettingsPage, Calendar.getInstance(), rNewPages.getTimestamp());
	    if(revs.length==0) {
	    	return;
	    }
	    
	    Revision r = revs[revs.length-1].getPrevious();
	    
	    if(r==null) {
	    	return;
	    }
	    // get last used header/footer
	    log.info("portal params were changed after last use");
	    
	    String settingsText = r.getText();
	    Map<String, String> options = new HashMap<String,String>();
	    String userNamespace = wiki.namespaceIdentifier(Wiki.USER_NAMESPACE);
	    if(NirvanaBasicBot.TryParseTemplate(template, userNamespace, settingsText, options, true)) {
	    	headerLastUsed = NirvanaBot.getDefaultHeader();
	    	footerLastUsed = NirvanaBot.getDefaultFooter();
	    	middleLastUsed = NirvanaBot.getDefaultMiddle();
	    	String key = "шапка";
	    	if (options.containsKey(key) && !options.get(key).isEmpty())
			{
	    		headerLastUsed = options.get(key).replace("\\n", "\n");
			}
						
			key = "подвал";
			if (options.containsKey(key) && !options.get(key).isEmpty())
			{
				footerLastUsed = options.get(key).replace("\\n", "\n");
			}
			key = "середина";
			if (options.containsKey(key) && !options.get(key).isEmpty())
			{
				middleLastUsed = options.get(key).replace("\\n", "\n");
			}
			
			if(headerLastUsed == null) {
				headerLastUsed = "";
			}
			
			if(footerLastUsed == null) {
				footerLastUsed = "";
			}
			
			if(middleLastUsed == null) {
				middleLastUsed = "";
			}
	    }
    }

	public void updateArchive(NirvanaWiki wiki, Data d, ReportItem reportData) throws LoginException, IOException {
    	if(archiveSettings==null || archiveSettings.isSimple()) {	
    		log.debug("archive has simple format");
    		log.info("Updating "+archive);
    		wiki.prependOrCreate(archive, d.archiveText, "+"+d.archiveCount+" статей", this.minor, this.bot);
    		reportData.archived = true;
	    	return;
    	}
    	
    	// we need here a map of archives - Archive objects 

    	log.debug("archive has difficult format");
    	
    	Archive defaultArchive = null;
    	String defaultArhiveName = null;
    	if(archiveSettings.isSingle()) {
    		defaultArhiveName = archiveSettings.archive;    		
    	} else {
    		defaultArhiveName = archiveSettings.getArchiveForDate(Calendar.getInstance());
    	}
    	defaultArchive = ArchiveFactory.createArchive(archiveSettings, wiki, defaultArhiveName, delimeter);
    	
		HashMap<String,Archive> hmap = null;
		hmap = 	new HashMap<String,Archive>(3);
		hmap.put(defaultArhiveName, defaultArchive);

    	
    	for(int i = d.archiveItems.size()-1;i>=0;i--) {
    		String item = d.archiveItems.get(i);
    		log.debug("archiving item: "+item);
    		Calendar c = getNewPagesItemDate(wiki,item);
    		if(c==null) {
    			defaultArchive.add(item);
    			continue;
    		} 
    		// 1) get archive object for this item
    		Archive thisArchive = defaultArchive;
    		if(!archiveSettings.isSingle()) {
    			String arname = archiveSettings.getArchiveForDate(c);
    			thisArchive = hmap.get(arname);
    			if(thisArchive == null) {
    				thisArchive = ArchiveFactory.createArchive(archiveSettings, wiki, arname, delimeter);    				
    				hmap.put(arname, thisArchive);
    			}
    		}
    		// find where to put this item
    		if(archiveSettings.withoutHeaders()) {
    			if(!archiveSettings.hasHtmlEnumeration()) {
    				((ArchiveSimple)thisArchive).add(item);
    			} else {
    				((ArchiveWithEnumeration)thisArchive).add(item);
    			}
    		} else {
	    		String thisHeader = archiveSettings.getHeaderForDate(c);
	    		String superHeader = archiveSettings.getHeaderHeaderForDate(c);
	    		if(superHeader==null) {
	    			((ArchiveWithHeaders)thisArchive).add(item, thisHeader);
	    		} else {
	    			((ArchiveWithHeaders)thisArchive).add(item, thisHeader,superHeader);
	    		}
    		}
    		
    	}    	
    	
    	Iterator<Entry<String, Archive>> it = hmap.entrySet().iterator();
    	while(it.hasNext()) {
    		Entry<String, Archive> ar = it.next();
    		Archive thisArchive = ar.getValue();
    		if (thisArchive.newItemsCount()>0) {
    			log.info("Updating "+ar.getKey());
    			thisArchive.update(wiki,ar.getKey(), minor, bot);
    		}
    	}   		
   		reportData.archived = true;	    		
    	return;
	}
		
	public static Calendar getNewPagesItemDate(NirvanaWiki wiki, String item) {
		Calendar c = null;
		//NewPagesItem itemData = null;
		Pattern p = Pattern.compile(PATTERN_NEW_PAGES_LINE_WIKIREF_ARTICLE);
		Matcher m = p.matcher(item);
		boolean foundBrackets = false;
		while(m.find()) {
			String article = m.group("article");
			if(article.startsWith(":"))
				article = article.substring(1); // special case when title starts from : this : is not included in title
			if(article.contains("Категория:Википедия:Списки новых статей по темам")) {
				// foundBrackets should stay false for this case
				continue;
			}
			Revision r = null;
			try {
				r = wiki.getFirstRevision(article,true);
			} catch (IOException e) {				
				//e.printStackTrace();
				log.warn("article "+article+" not found");
			}
			if(r!=null) {
				return r.getTimestamp();
			}
			foundBrackets = true;
		}
		if(foundBrackets) return null;
		// если не нашли по [[]] скобочкам, продолжаем искать по {{}}
		p = Pattern.compile(PATTERN_NEW_PAGES_LINE_TEMPLATE_ITEM);
		m = p.matcher(item);
		if(m.find()) {
			String templateString = m.group("template");			
			//log.debug("count = "+String.valueOf(m.groupCount()));
			
			//if(templateString!=null) System.out.println("found template: "+templateString);
			String []items = templateString.split("\\|");
			// 2012-02-26T16:10:36Z
			//p = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z");
			for(String s:items) {
				c = DateTools.parseDate(s);
				if(c!=null) return c;
			}
			for(int i=1;i<items.length;i++) {
				String s = items[i];
				//System.out.println("check string: "+s);
				Revision r = null;
				try {
					r = wiki.getFirstRevision(s,true);
					//r = null;
				} catch (IOException e) {
					log.warn("page "+s+ " not found");
				}
				if(r!=null) return r.getTimestamp();
			}
		}
		return null;
	}
	
	public static boolean userNamespace(String article) {
		return (article.startsWith("Участник:") ||
				article.startsWith("Обсуждение_участника") ||
				article.startsWith("Обсуждение участника") ||
				article.startsWith("User:") ||
				article.startsWith("User_talk:"));
	}
	public static boolean categoryNamespace(String article) {
		return (article.startsWith("Категория:") ||
				article.startsWith("Category:") );
	}

	// TODO протестировать эту функцию на другие пространства имён!!!
	public static String getNewPagesItemArticle(String item) {
		Pattern p = Pattern.compile(PATTERN_NEW_PAGES_LINE_WIKIREF_ARTICLE);
		Matcher m = p.matcher(item);
		while(m.find()) {
			String article = m.group("article");
			if(article.startsWith(":"))
				article = article.substring(1); // special case when title starts from : this : is not included in title
			if(article.contains("Категория:Википедия:Списки новых статей по темам")) {				
				continue;			
			}
			if(!userNamespace(article))
				return article;
		}
		p = Pattern.compile(PATTERN_NEW_PAGES_LINE_TEMPLATE_ITEM);
		m = p.matcher(item);
		if(m.find()) {
			String templateString = m.group("template");			
			//log.debug("count = "+String.valueOf(m.groupCount()));
			
			//if(templateString!=null) System.out.println("found template: "+templateString);
			String []items = templateString.split("\\|");
			// 2012-02-26T16:10:36Z
			//p = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z");
			for(int i=1;i<items.length;i++) {
				String s = items[i].trim();
				String article = s;
				if(!userNamespace(article))
					return article;
			}			
		}
		return null;
	}
	public static String unmarkDeleted(String item) {
		String str = item;
		int pos = str.indexOf(COMMENT_DELETED);
		if(pos>=0) {			
			str = str.substring(0, pos)+
					((pos+COMMENT_DELETED.length())<str.length()?str.substring(pos+COMMENT_DELETED.length()):"");
			return str;
		}
		Pattern p = Pattern.compile(".*\\{\\{(Новая статья|Новая статья2)\\|.+\\|.+\\|.+\\|уд\\}\\}.*");
		Matcher m = p.matcher(item);
		if(m.matches()) {
			pos = str.indexOf("|уд}}");
			if(pos>=0) {
				str = str.substring(0,pos)+str.substring(pos+3);
			}
		}
		return str;
	}
	public static String markDeleted(String item) {
		String str = item;
		if(str.contains("'''Статья удалена'''") || str.contains("|уд}}"))
			return str;
		Pattern p = Pattern.compile(".*\\[\\[(.+)\\]\\].*");
		Matcher m = p.matcher(item);
		if(m.matches()) {
			str = str + COMMENT_DELETED;
		}
		p = Pattern.compile(".*\\{\\{(Новая статья|Новая статья2)\\|.+\\|.+\\|.+\\|\\}\\}.*");
		m = p.matcher(item);
		if(m.matches() && str.contains("{{Новая статья")) {
			int pos = str.indexOf("}}");
			str = str.substring(0, pos)+"уд"+str.substring(pos);
		} else {
			p = Pattern.compile(".*\\{\\{(Новая статья|Новая статья2)\\|.+\\|.+\\|.+\\}\\}.*");
			m = p.matcher(item);
			if(m.matches() && str.contains("{{Новая статья")) {
				int pos = str.indexOf("}}");
				str = str.substring(0, pos)+"|уд"+str.substring(pos);
			}
		}
		return str;
	}
	
	public static String pageTitleNormalToEscaped(String str) {
		return str.replace("=", "&#61;");
	}
	
	public static String pageTitleEscapedToNormal(String str) {
		return str.replace("&#61;","=");
	}
}
