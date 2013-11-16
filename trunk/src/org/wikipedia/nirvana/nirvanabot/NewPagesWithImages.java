/**
 *  @(#)NewPagesWithImages.java 07/04/2012
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

import org.apache.commons.lang3.StringUtils;
import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.HTTPTools;
import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.nirvanabot.imagefinder.ImageFinder;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.PageListFetcher;
import org.wikipedia.nirvana.nirvanabot.pagesfetcher.RevisionWithId;

/**
 * @author kin
 *
 */
public class NewPagesWithImages extends NewPages {
	//private String regexToFindImage;
	private ImageFinder imageFinder;
	private NirvanaWiki commons;
	
	public static class RevisionWithImage extends RevisionWithId {
		String image;

		public RevisionWithImage(Wiki wiki, long revid, Calendar timestamp,
				String title, String summary, String user, boolean minor,
				boolean bot, boolean rvnew, int size, long id, String image) {
			super(wiki, revid, timestamp, title, summary, user, minor, bot, rvnew, size, id);
			this.image = image;
		}
		
		public RevisionWithImage(Wiki wiki, Revision r, long id, String image) {
			super(wiki, r.getRevid(), r.getTimestamp(), r.getPage(), 
					r.getSummary(), r.getUser(), r.isMinor(), r.isBot(), r.isNew(), r.getSize(), id);
			this.image = image;
		}
		
		public String getImage() { return this.image; }
		public void setImage(String image) { this.image = image; }
		
	}
	/**
	 * @param param
	 * @param commons
	 * @param imageFinder
	 */
	public NewPagesWithImages(PortalParam param, NirvanaWiki commons, ImageFinder imageFinder) {
		super(param);
		if(this.archiveSettings!=null) {
			this.archiveSettings = null;
		}
		this.format = this.format.replace("%(имя файла)", "%4$s");
		this.imageFinder = imageFinder;
		this.commons = commons;
		/*if(inCard) {
			regexToFindImage = "\\| *(image file|Аверс|Реверс|Изображение аверса|Изображение реверса) *= *(?<filename>.+?) *\n";
		} else {
			regexToFindImage = "\\[\\[(Image|File|Файл|Изображение):(?<filename>.+?)(\\||\\])";
		}*/
	}
	
	public Data getData(NirvanaWiki wiki, String text) throws IOException, InterruptedException {
		log.info("Processing data for [[" + this.pageName+"]]");
		
		/*for (String category : categories) {
			log.info("Processing data of " + category);
		}*/
		
		PageListFetcher pageListFetcher = createPageListFetcher();
		ArrayList<Revision> pageInfoListNotFiltered = pageListFetcher.getNewPages(wiki);
		ArrayList<Revision> pageInfoList = new ArrayList<Revision>(30);
		
		for(Revision r: pageInfoListNotFiltered) {
			String article = "";
			long revId = r.getRevid();
			long id = ((RevisionWithId)r).getId();
			String title = r.getPage();
			Revision page = null;
            try {
            	article = wiki.getPageText(r.getPage());
            } catch(java.io.FileNotFoundException e) {
            	log.warn(e.toString()+" "+r.getPage()); // page was created and renamed or deleted after that
            	continue;
            }
            if(NirvanaWiki.isRedirect(article)) {            
            	if(pageListFetcher.revisionAvailable()) {
            		page = new RevisionWithImage(wiki,wiki.getRevision(revId),id, null);
            	} else {
            		page = new RevisionWithImage(wiki, wiki.getFirstRevision(title, true), id, null); 
            	}
            	//log.debug("REDIRECT to: "+page.getPage());
            	try {
                	article = wiki.getPageText(page.getPage());
                } catch(java.io.FileNotFoundException e) {
                	log.warn(e.toString()+" "+page.getPage()); // page was created and renamed or deleted after that
                	continue;
                }
            	//FileTools.dump(article, "dump", page.getPage());
            }
            String image = imageFinder.findImage(wiki, commons, article);
            if(image!=null) {	
            	//log.debug("found pattern");
            	//String image = m.group("filename").trim();	                	
            	log.debug("image found = "+image);
            	
            		if(page==null) {
            			page = new RevisionWithImage(wiki, revId, Calendar.getInstance(), title, "", "",false,false, true, 0, id, image);
            		} else {
            			((RevisionWithImage)page).setImage(image);
            		}            		
            		pageInfoList.add(page);
            		log.debug("adding page to list: "+title);
            }
		}
		
		sortPages(pageInfoList, pageListFetcher.revisionAvailable());
		
	
		List<String> subset = new ArrayList<String>();
		List<String> includedPages = new ArrayList<String>();
		List<String> includedImages = new ArrayList<String>();
		int count = pageInfoList.size();
		count = count<maxItems?count:maxItems;
		for (int i = 0; i < count ; ++i)
		{
			
			RevisionWithImage page = (RevisionWithImage)pageInfoList.get(i);
			if(page.getSize()==0) {
				if(pageListFetcher.revisionAvailable()) {
            		page = new RevisionWithImage(wiki, wiki.getRevision(page.getRevid()), page.getId(), page.getImage());
            	} else {
            		page = new RevisionWithImage(wiki, wiki.getFirstRevision(page.getPage()), page.getId(), page.getImage()); 
            	}
			}
		    
		    if (page != null && !usersToIgnore.contains(HTTPTools.removeEscape(page.getUser())))
		    {		    	
		    	String title = HTTPTools.removeEscape(page.getPage());
		    	String time = null;
		    	if(NirvanaBot.TIME_FORMAT.equalsIgnoreCase("long")) 
		    		time = page.getTimestamp().getTime().toString();
		    	else {
		    		time = String.format("%1$tFT%1$tTZ",page.getTimestamp());
		    	}
		    	/*
		    	log.debug("title -> ["+title+"]");
		    	log.debug("user -> ["+page.getUser()+"]");
		    	log.debug("image -> ["+page.getImage()+"]");
		    	log.debug("title2 -> "+(namespace!=0?title.substring(wiki.namespaceIdentifier(this.namespace).length()+1):title));
		    	log.debug("user2 -> "+HTTPTools.removeEscape(page.getUser()));
		    	log.debug("time -> "+time);
		    	log.debug("format -> "+this.format);*/
		    	String element = String.format(this.format,
		    			namespace!=0?title.substring(wiki.namespaceIdentifier(this.namespace).length()+1):title,
		    			HTTPTools.removeEscape(page.getUser()), 
		    			time, 
		    			page.getImage()
		    			);
		    	
		        if (!subset.contains(element))
		        {
		            subset.add(element);
		            includedPages.add(title);
		            includedImages.add(page.getImage());
		            log.debug("ADD new line: \t"+element);
		        }
		    }
		}
		
		ArrayList<String> archiveItems = new ArrayList<String>();
		int oldCount = 0;
		int archiveCount = 0;
	
		// Add elements from old page
		if (true/*count < maxItems /*|| archive!=null*/) { 
			String oldText = text;
			String[] oldItems;
			/*if(archive!=null)*/ /*archiveItems = new ArrayList<String>();*/
			oldText = oldText.trim();
		    	    
		    if (!header.isEmpty() && oldText.startsWith(header))
		    {
		        oldText = oldText.substring(header.length());
		    }
		    //FileTools.dump(footer, "dump", this.pageName +".footer.txt");
		    if (!footer.isEmpty() && oldText.endsWith(footer))
		    {
		        oldText = oldText.substring(0, oldText.length() - footer.length());
		    }
		    oldItems = oldText.split(delimeter.replace("|", "\\|"));
		    if(delimeter.equals("\n")) log.debug("delimeter is \\n");
		    else if(delimeter.equals("\r")) log.debug("delimeter is \\r");
		    else log.debug("delimeter is "+delimeter);
	 
		    oldCount = oldItems.length;
		    for (int i = 0; i < oldItems.length; ++i)
		    {
		    	//log.debug("check old line: \t"+items[i]+"");
		    	boolean skip = false;
		    	if(oldItems[i].isEmpty()) continue;
		    	for(String image:includedImages) {
		    		if(oldItems[i].contains(image+"|")) {
		    			skip = true;
		    			log.debug("SKIP old line: \t"+oldItems[i]);
		    			break;
		    		}
		    	}
		    	if(skip) continue;
		        if (subset.size() < maxItems)  {
		        	if(!subset.contains(oldItems[i])) { 
		        		subset.add(oldItems[i]);		
		        		log.debug("ADD old line: \t"+oldItems[i]);
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
		
		Data d = new Data();
		d.newText = header + StringUtils.join(subset.toArray(),this.delimeter) + footer;
		if(archive!=null && archiveItems!=null && archiveItems.size()>0) {
			d.archiveText = StringUtils.join(archiveItems.toArray(),delimeter) + "\n";
			//if(archiveSettings!=null)
				d.archiveItems = archiveItems;
		}
		d.archiveCount = archiveCount;
		d.newPagesCount = subset.size() - (oldCount - archiveCount);
		if(d.newPagesCount<0) d.newPagesCount = 0;
		log.debug("updated items count: "+subset.size());
		log.debug("old items count: "+oldCount);
		log.debug("archive count: "+archiveCount);
		log.debug("new items count: "+d.newPagesCount);
		
		return d;
	}

}
