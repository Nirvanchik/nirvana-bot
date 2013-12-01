/**
 *  @(#)NewPages.java 0.03 02/07/2012
 *  Copyright © 2011 - 2013 Dmitry Trofimovich (KIN)
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
import org.wikipedia.nirvana.StringTools;
import org.wikipedia.nirvana.archive.Archive;
import org.wikipedia.nirvana.archive.ArchiveFactory;
import org.wikipedia.nirvana.archive.ArchiveSettings;
import org.wikipedia.nirvana.archive.ArchiveSimple;
import org.wikipedia.nirvana.archive.ArchiveWithEnumeration;
import org.wikipedia.nirvana.archive.ArchiveWithHeaders;
import org.wikipedia.nirvana.archive.ArchiveSettings.Enumeration;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.NewPagesFetcherOBOCatScan;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.NewPagesFetcherOBOCatScan2;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.NewPagesFetcherOneReqCatScan2;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.PageListFetcher;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.RevisionWithId;

/**
 * @author kin
 *
 */
public class NewPages implements PortalModule{
	public static final String PATTERN_NEW_PAGES_LINE_WIKIREF_ARTICLE = "\\[\\[(?<article>[^\\]|]+)(|[^\\]]+)?\\]\\]";
	public static final String PATTERN_NEW_PAGES_LINE_TEMPLATE_ITEM = "\\{\\{(?<template>.+)\\}\\}";
	private static final String COMMENT_DELETED = "<span style=\"color:#966963\"> — '''Статья удалена'''</span>";
	protected String language;
	protected List<String> categories;
	protected List<String> categoriesToIgnore;
	protected Set<String> usersToIgnore;
	protected String pageName;
    protected String archive;
    protected ArchiveSettings archiveSettings;
    protected String format;
    protected String header;
    protected String footer;
    protected String middle;
    protected int maxItems;
    protected int hours;
    protected String service;
    protected String delimeter;    
    protected int depth;
    protected int namespace;
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
    
    protected static org.apache.log4j.Logger log = null;	
    
    protected GetRevisionMethod getRevisionMethod = GetRevisionMethod.GET_REV;
    
    protected boolean UPDATE_FROM_OLD = true;
    
    protected enum GetRevisionMethod {
    	GET_FIRST_REV,
    	GET_FIRST_REV_IF_NEED,
    	GET_REV,
    	GET_TOP_REV
    	//,
    	//GET_LAST_REV_BY_PAGE_NAME
    }
    
    //protected boolean botsAllow = false;
    protected String botsAllowString = null;
	/**
	 * 
	 */
    public NewPages(PortalParam param) {
    	this.language = param.lang;
    	this.categories = param.categories;
    	this.categoriesToIgnore = param.categoriesToIgnore;
    	this.usersToIgnore = new HashSet<String>(param.usersToIgnore);
    	this.pageName = param.page;
    	this.archive = param.archive;
   		this.archiveSettings = param.archSettings;
    	this.maxItems = param.maxItems;
    	this.format = param.format.replace("%(название)", "%1$s").replace("%(автор)", "%2$s").replace("%(дата)", "%3$s");
    	if(param.deletedFlag==PortalParam.Deleted.MARK) {
    		this.format = this.format.replace("(удалено)", "%4$s");
    	}
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
    	
    	log = org.apache.log4j.Logger.getLogger(this.getClass().getName());
    	log.debug("Portal module created for portal subpage [["+this.pageName+"]]");
	}
    
    public String getOldText(Wiki wiki) throws IOException {
    	try
		{
			return wiki.getPageText(this.pageName);
		} catch (java.io.FileNotFoundException e) {
			return "";
		}
    }

	public static class Data {		
		String newText;
		String archiveText;
		List<String> archiveItems = null;
		int newPagesCount;
		int archiveCount;
		int deletedCount;
	}
	
	
	/*
	public void getNewPages(NirvanaWiki wiki, 
			ArrayList<Revision> pageInfoList,
			HashSet<String> pages) throws IOException {
		PageListFetcher pageListFetcher = createPageListFetcher();
		
	}*/
	
	PageListFetcher createPageListFetcher() {
		//return new NewPagesFetcherOBOCatScan(categories, categoriesToIgnore, language, depth, hours, namespace);
		if(service.equalsIgnoreCase(NirvanaBot.SERVICE_CATSCAN)) {
			return new NewPagesFetcherOBOCatScan(categories, categoriesToIgnore, language, depth, hours, namespace);
		} else if (service.equalsIgnoreCase(NirvanaBot.SERVICE_CATSCAN2)) {
			if (fastMode) {
				return new NewPagesFetcherOneReqCatScan2(categories, categoriesToIgnore, language, depth, hours, namespace);
			} else {
				return new NewPagesFetcherOBOCatScan2(categories, categoriesToIgnore, language, depth, hours, namespace);
			}
		}
		throw new Error("Unsupported service name");
		//*/		
	//return new NewPagesFetcherOBOCatScan2(categories, categoriesToIgnore, language, depth, hours, namespace);
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
	
	public void sortPages(ArrayList<Revision> pageInfoList, boolean byRevision) {
		if(byRevision) {
			sortPagesByRevision(pageInfoList);
		} else {
			sortPagesById(pageInfoList);
		}
	}
	
	protected void addNewItem(NirvanaWiki wiki, String title, boolean deleted, String time, String user, 
			List<String> subset, List<String> includedPages) throws IOException {
		String element = null;    	
    	
    	String titleToInsert = title;
    	if(namespace!=0) {
    		log.debug("namespace = "+wiki.namespaceIdentifier(this.namespace)+" namespace="+String.valueOf(namespace)+" title="+title);
    		titleToInsert = title.substring(wiki.namespaceIdentifier(this.namespace).length()+1);
    	}
    	if(this.format.contains("{{") && this.format.contains("}}")) {
    		titleToInsert = pageTitleNormalToEscaped(titleToInsert); // replaces '=' equal-sign by escape-code
    	}
    	element = String.format(this.format,
    			titleToInsert,
    			HTTPTools.removeEscape(user), 
    			time);
    	if(deletedFlag==PortalParam.Deleted.MARK && deleted) {
    		element = markDeleted(element);
    	} 
    	
        if (!subset.contains(element))
        {
            subset.add(element);
            includedPages.add(title);
            log.debug("ADD new line: \t"+element);
        }
	}
	
	public Data getData(NirvanaWiki wiki, String text) throws IOException, InterruptedException {
		log.info("Get data for [[" + this.pageName+"]]");
		
		//Revision r = wiki.getFirstRevision("Кай Юлий Цезарь");
		//HashSet<String> pages = new HashSet<String>();
		/*for (String category : categories) {
			log.info("Processing data of " + category);
		}*/		
		PageListFetcher pageListFetcher = createPageListFetcher();
		log.info("Using pagelist fetcher: "+pageListFetcher);
		if (pageListFetcher.revisionAvailable()) {
			getRevisionMethod = GetRevisionMethod.GET_REV;
		} else {
			getRevisionMethod = GetRevisionMethod.GET_FIRST_REV;
		}
		ArrayList<Revision> pageInfoList = pageListFetcher.getNewPages(wiki);
		
		sortPages(pageInfoList, pageListFetcher.revisionAvailable());		
	
		List<String> subset = new ArrayList<String>();
		List<String> includedPages = new ArrayList<String>();
		int count = pageInfoList.size();
		count = count<maxItems?count:maxItems;
		for (int i = 0; i < count ; ++i)
		{
		    Revision page = null;
		    switch(getRevisionMethod) {
		    	case GET_FIRST_REV:
		    		page = wiki.getFirstRevision(pageInfoList.get(i).getPage());
		    		break;
		    	case GET_REV:
		    		page = wiki.getRevision(pageInfoList.get(i).getRevid());
		    		break;
		    	case GET_TOP_REV:
		    		page = wiki.getTopRevision(pageInfoList.get(i).getPage());
		    		break;
		    	case GET_FIRST_REV_IF_NEED:
		    		if(usersToIgnore.size()==0) {
		    			page = pageInfoList.get(i);
		    		} else {
		    			page = wiki.getFirstRevision(pageInfoList.get(i).getPage());
		    		}
		    		break;
		    	default:
		    		throw new Error("not supported mode");
		    }
		    
		    if (page != null && !usersToIgnore.contains(HTTPTools.removeEscape(page.getUser())))
		    {
		    	//if(namespace!=0) {log.warn("namespace is not 0"); continue;}
		      //  String element = String.format(format,
		        //    Namespace != 0 ? page.Title.Substring(wiki.GetNamespace(Namespace).Length + 1) : page.Title,
		          //  page.Author,
		            //page.FirstEdit.ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ"));
				String title_old = HTTPTools.removeEscape(pageInfoList.get(i).getPage());
		    	String title_new = HTTPTools.removeEscape(page.getPage());
		    	log.debug("check page, title old: "+title_old+", title new: "+title_new);
		    	boolean deleted = false;
		    	if(this.deletedFlag != PortalParam.Deleted.DONT_TOUCH) {		    		 
	                if(!wiki.exists(title_new)) {	                
	                	log.warn("page " +title_new+" deleted"); // page was created and renamed or deleted after that
	                	deleted = true;
	                	if(this.deletedFlag==PortalParam.Deleted.REMOVE) 
	                		continue;
	                }
		    	}
		    	String time = null;
		    	if(NirvanaBot.TIME_FORMAT.equalsIgnoreCase("long")) 
		    		time = page.getTimestamp().getTime().toString();
		    	else {
		    		time = String.format("%1$tFT%1$tTZ",page.getTimestamp());
		    	}
		    	
		    	boolean renamed = !title_new.equals(title_old);
		    	
		    	String user = page.getUser();
		    	
		    	if(renamed) {
			    	if((this.renamedFlag & PortalParam.RENAMED_NEW) != 0 ) {
			    		addNewItem(wiki,title_new,deleted,time,user,subset,includedPages);
			    	} 
			    	if((this.renamedFlag & PortalParam.RENAMED_OLD) != 0) {
			    		addNewItem(wiki,title_old,deleted,time,user,subset,includedPages);
			    	} else {
			    		includedPages.add(title_old); 
			    	}
			    	///if(!includedPages.contains(title_old)) {
			    	//	includedPages.add(title_old); 
			    	//}
			    	
		    	} else {
		    		addNewItem(wiki,title_new,deleted,time,user,subset,includedPages);
		    	}
		    }
		}
		
		ArrayList<String> archiveItems = new ArrayList<String>();
		int oldCount = 0;
		int archiveCount = 0;
		int deletedCount = 0;
	
		// Add elements from old page
		if (true/*count < maxItems /*|| archive!=null*/) { 
			String oldText = text;
			String[] oldItems;
			
			// remove {{bots|allow=}} record
			this.botsAllowString = NirvanaWiki.getAllowBotsString(text);
			//log.debug("bot allow string: "+botsAllowString);
			if(botsAllowString!=null) {
				int pos = oldText.indexOf(botsAllowString);
				oldText = oldText.substring(0, pos) + oldText.substring(pos+botsAllowString.length());
			}
			/*if(archive!=null)*/ /*archiveItems = new ArrayList<String>();*/
			
			//text.matches("(?si).*\\{\\{(nobots|bots\\|(allow=none|deny=(.*?" + user + ".*?|all)|optout=all))\\}\\}.*");
			
			
			if (!footerLastUsed.isEmpty() && oldText.endsWith(footerLastUsed)) {				
				oldText = oldText.substring(0, oldText.length() - footerLastUsed.length());
				oldText = StringTools.trimRight(oldText);
			} else {
				oldText = StringTools.trimRight(oldText);
				if (!footerLastUsed.isEmpty() && oldText.endsWith(footerLastUsed)) {				
					oldText = oldText.substring(0, oldText.length() - footerLastUsed.length());
					oldText = StringTools.trimRight(oldText);
				}
			} 		    	    
			
		    if (!headerLastUsed.isEmpty() && oldText.startsWith(headerLastUsed))
		    {
		        oldText = oldText.substring(headerLastUsed.length());
		        oldText = StringTools.trimLeft(oldText);
		    } else {
		    	oldText = StringTools.trimLeft(oldText);
		    	if (!headerLastUsed.isEmpty() && oldText.startsWith(headerLastUsed))
			    {
			        oldText = oldText.substring(headerLastUsed.length());
			        oldText = StringTools.trimLeft(oldText);
			    }
		    }
		    
		    if(!middle.isEmpty()) {
		    	if (oldText.contains(middle)) {
		    		oldText = oldText.replace(middle, this.delimeter);
		    	} else if(!middle.trim().isEmpty() && oldText.contains(middle.trim())) {
		    		oldText = oldText.replace(middle.trim(), this.delimeter);
		    	}
		    } 
		    //FileTools.dump(footer, "dump", this.pageName +".footer.txt");
		   
		    //oldItems = oldText.split(delimeter); // this includes empty lines, and incorrect result calculation
		    oldItems = StringUtils.splitByWholeSeparator(oldText,delimeter); // removes empty items
		    if(delimeter.equals("\n")) log.debug("delimeter is \\n");
		    else if(delimeter.equals("\r")) log.debug("delimeter is \\r");
		    else log.debug("delimeter is \""+delimeter+"\"");
		    
		
		    
		    
		 // Find intersection of new and old pages
			/*ArrayList<String> intersect = new ArrayList<String>();
			for (int i = 0; i < count ; ++i)
			{
				String title = includedPages.get(i);
				log.debug("check title in old: "+title);
				if(oldText.contains("[["+title+"]]")||oldText.contains("|"+title+"|")) {
					intersect.add(title);
					log.debug("INTERSECTON: "+title+"");
				}
			}*/
		    
		    oldCount = oldItems.length;
		    for (int i = 0; i < oldItems.length; ++i)
		    {
		    	String item = oldItems[i];
		    	//log.trace("check old line: \t"+item+"");
		    	boolean skip = false;
		    	if(oldItems[i].isEmpty()) continue;
		    	for(String title:includedPages) {
		    		String variant1 = "[["+title+"]]";		        		
		    		String variant2 = "|"+
		    				pageTitleNormalToEscaped(
		    						title.substring((namespace==0)?0:wiki.namespaceIdentifier(this.namespace).length()+1)
		    						)+
		    				"|";
		    		if(oldItems[i].contains(variant1) ||
		    				oldItems[i].contains(variant2)) {
		    			skip = true;
		    			log.debug("SKIP old line: \t"+oldItems[i]);
		    			break;
		    		}
		    	}
		    	if(skip) continue;
		        if (subset.size() < maxItems)  {
		        	if(!subset.contains(oldItems[i])) {
		        		boolean deleted = false;
		        		boolean mark_deleted = false;
		        		
		        		if(this.deletedFlag==PortalParam.Deleted.REMOVE || this.deletedFlag==PortalParam.Deleted.MARK) {
	        				String title = pageTitleEscapedToNormal(getNewPagesItemArticle(oldItems[i]));
	        				if(!wiki.exists(title)) {
	    	                	//log.warn(e.toString()+" "+title); // page was created and renamed or deleted after that
	    	                	if(this.deletedFlag==PortalParam.Deleted.REMOVE) {
	    	                		log.debug("REMOVE old line: \t"+oldItems[i]);
	    	                		deleted = true;
	    	                		deletedCount++;
	    	                	}
	    	                	if(this.deletedFlag==PortalParam.Deleted.MARK) {
	    	                		mark_deleted = true;
	    	                		item = markDeleted(item);
	    	                	}
	        				}
	        			} 
		        		if(this.deletedFlag==PortalParam.Deleted.MARK && !mark_deleted) {
		        			item = unmarkDeleted(item);
		        		}
		        		if(!deleted) {
			        		if(UPDATE_FROM_OLD) {
		        				log.debug("ADD old line: \t"+item);
		        				subset.add(item);
			        		} else {
			        			log.debug("ARCHIVE old line: \t"+item);
			        			if(archive!=null) {		        		
					        		archiveItems.add(item);
					        	}
					        	archiveCount++;		        		
			        		}
			        	} 
		        		
		        	} else {
		        		log.debug("SKIP old line: \t"+oldItems[i]);
		        	}
		        } else {
		        	log.debug("ARCHIVE old line: \t"+oldItems[i]);
		        	if(archive!=null) {		        		
		        		archiveItems.add(oldItems[i]);
		        	}
		        	archiveCount++;
		        }
		    }
		}
		
		
		//StringUtils.
		//com.sun.xml.internal.ws.util.StringUtils.
		Data d = new Data();
		//String bots = if(botsAllowString!=null)
		log.debug("bots allow string: "+botsAllowString);
		String bots = "";
		if(botsAllowString!=null) {
			bots = botsAllowString+"\n";
		}
		if(middle.isEmpty())
			d.newText = bots + header + StringUtils.join(subset.toArray(),this.delimeter) + footer;
		else {
			if(subset.size()>1) {
				int m = (subset.size()+1)/2; // 2->1, 3->2, 4->2
				d.newText = bots + header + 
						StringUtils.join(subset.subList(0, m),this.delimeter) + 
						this.middle +
						StringUtils.join(subset.subList(m, subset.size()),this.delimeter) +
						footer;
			} else d.newText = bots + header + 
					StringUtils.join(subset,this.delimeter) + 
					this.middle +					
					footer;
		}
			
		if(archive!=null && archiveItems!=null && archiveItems.size()>0) {
			if(archiveSettings.enumeration==Enumeration.HASH) {
				enumerateWithHash(archiveItems);
			}
			d.archiveText = StringUtils.join(archiveItems.iterator(),delimeter) + "\n";
			//if(archiveSettings!=null)
			d.archiveItems = archiveItems;
		}
		//archiveItems.i
		d.archiveCount = archiveCount;
		d.newPagesCount = subset.size() - (oldCount - archiveCount - deletedCount);
		d.deletedCount = deletedCount;
		if(d.newPagesCount<0) d.newPagesCount = 0;
		log.debug("updated items count: "+subset.size());
		log.debug("old items count: "+oldCount);
		log.debug("archive count: "+archiveCount);
		log.debug("deleted count: "+deletedCount);
		log.debug("new items count: "+d.newPagesCount);
		
		return d;
	}
	
	

	public void enumerateWithHash(ArrayList<String> list) {
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
		
	@Override
	public boolean update(NirvanaWiki wiki, ReportItem reportData, String comment) throws IOException, LoginException, InterruptedException {
		log.debug("=> update()");
		boolean updated = false;
		String text = getOldText(wiki);
		log.debug("old text retrieved");
		Wiki.User user = wiki.getCurrentUser();
		log.debug("current user retrieved");
		if(!NirvanaWiki.allowBots(text, user.getUsername())) {
			//reportData.status = Status.DENIED;
			log.info("bots/nobots template forbids updating this portal section");
			return false;
		}
		//this.botsAllowString = NirvanaWiki.getAllowBotsString(text);
		//getData(wiki);	
				
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
		    if(archive!=null && d.archiveCount>0) {
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
		    if(archive!=null && d.archiveText!=null) {		    	
		    	if(NirvanaBot.UPDATE_PAUSE>0) {
		    		log.debug("Waiting "+ NirvanaBot.UPDATE_PAUSE+" ms");
			    	try {			    		 
			    		Thread.sleep(NirvanaBot.UPDATE_PAUSE);
			    	} catch (InterruptedException e) {					
				    	log.error(e.toString());
						e.printStackTrace();
					}
		    	}
		    	log.info("Updating archive");
	    		updateArchive(wiki, d, reportData);
		    }
		}
		return updated;
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
	    if(NirvanaBasicBot.TryParseTemplate(template, settingsText, options)) {
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
    		log.info("Updating "+ar.getKey());
    		thisArchive.update(wiki,ar.getKey(), minor, bot);
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
				String s = items[i];
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
