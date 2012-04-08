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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.HTTPTools;
import org.wikipedia.nirvana.NirvanaWiki;

/**
 * @author kin
 *
 */
public class NewPagesWithImages extends NewPages {
	private String regexToFindImage;
	
	public static class RevisionWithImage extends Revision {
		String image;

		public RevisionWithImage(Wiki wiki, long revid, Calendar timestamp,
				String title, String summary, String user, boolean minor,
				boolean bot, int size, String image) {
			wiki.super(revid, timestamp, title, summary, user, minor, bot, size);
			this.image = image;
		}
		
		public RevisionWithImage(Wiki wiki, Revision r, String image) {
			wiki.super(r.getRevid(), r.getTimestamp(), r.getPage(), 
					r.getSummary(), r.getUser(), r.isMinor(), r.isBot(), r.getSize());
			this.image = image;
		}
		
		public String getImage() { return this.image; }
		public void setImage(String image) { this.image = image; }
		
	}
	/**
	 * @param lang
	 * @param categories
	 * @param categoriesToIgnore
	 * @param usersToIgnore
	 * @param page
	 * @param archive
	 * @param ns
	 * @param depth
	 * @param hours
	 * @param maxItems
	 * @param format
	 * @param delimeter
	 * @param header
	 * @param footer
	 * @param markEdits
	 */
	public NewPagesWithImages(String lang, ArrayList<String> categories,
			ArrayList<String> categoriesToIgnore,
			ArrayList<String> usersToIgnore, String page, String archive,
			int ns, int depth, int hours, int maxItems, String format,
			String delimeter, String header, String footer, boolean minor, boolean bot,
			boolean inCard) {
		super(lang, categories, categoriesToIgnore, usersToIgnore, page,
				archive, null,
				ns, depth, hours, maxItems, format, delimeter, header,
				footer, minor, bot);
		this.format = this.format.replace("%(имя файла)", "%4$s");
		if(inCard) {
			regexToFindImage = "\\| *image file *= *(?<filename>.+?) *\n";
		} else {
			regexToFindImage = "\\[\\[(Image|File|Файл|Изображение):(?<filename>.+?)(\\||\\])";
		}
	}
	
	public Data processData(NirvanaWiki wiki, String text) throws IOException {
		log.info("Processing data for [[" + this.pageName+"]]");
		HashSet<String> ignore = getIgnorePages(null);
		//Revision r = wiki.getFirstRevision("Кай Юлий Цезарь");
		ArrayList<Revision> pageInfoList = new ArrayList<Revision>(30);
		HashSet<String> pages = new HashSet<String>();
		/*for (String category : categories) {
			log.info("Processing data of " + category);
		}*/
		for(String category : categories) {		
			log.info("Processing data of " + category);
			String line;
			String pageList = pageLists.get(category);
			StringReader r = new StringReader(pageList);
			BufferedReader b = new BufferedReader(r);
	        while ((line = b.readLine()) != null)
	        {
	            String[] groups = line.split("\t");
	            if (groups[0].equals(String.valueOf(namespace)))
	            {
	                String title = groups[1].replace('_', ' ');
	                if (ignore.contains(title))
	                {
	                    continue;
	                }
	                if (namespace != 0)
	                {	                	
	                    title = wiki.namespaceIdentifier(namespace) + ":" + title;	                	
	                	continue;
	                }	                
	                
	                //Calendar.getInstance().
	                
	                if (!pages.contains(title))
	                {
	                	long revId;
		                try {
		                	revId = Long.parseLong(groups[5]);
		                } catch(NumberFormatException e) {
		                	log.error(e.toString());
		                	continue;
		                }		               
		                //log.debug("title = "+title);
		                String article = "";
		                try {
		                	article = wiki.getPageText(title);
		                } catch(java.io.FileNotFoundException e) {
		                	log.warn(e.toString()+" "+title); // page was created and renamed or deleted after that
		                	continue;
		                }
		                //FileTools.dump(article, "dump", title);
		                Revision page = null;
		                if(NirvanaWiki.isRedirect(article)) {
		                	
		                	page = new RevisionWithImage(wiki,wiki.getRevision(revId),null); 
		                	//log.debug("REDIRECT to: "+page.getPage());
		                	try {
			                	article = wiki.getPageText(page.getPage());
			                } catch(java.io.FileNotFoundException e) {
			                	log.warn(e.toString()+" "+page.getPage()); // page was created and renamed or deleted after that
			                	continue;
			                }
		                	//FileTools.dump(article, "dump", page.getPage());
		                }
		                
		                Pattern p = Pattern.compile(this.regexToFindImage);
		                Matcher m = p.matcher(article);
		               // FileTools.dump(article, "dump", title+".txt");
		                if(m.find()) {	
		                	//log.debug("found pattern");
		                	String image = m.group("filename").trim();	                	
		                	//log.debug("image group = "+((image==null)?"":image));
		                	
		                	if(image!=null && !image.isEmpty()) {
		                		if(page==null) {
		                			page = new RevisionWithImage(wiki, revId, Calendar.getInstance(), title, "", "",false,false,0,image);
		                		} else {
		                			((RevisionWithImage)page).setImage(image);
		                		}
		                		pages.add(title);
		                		pageInfoList.add(page);
		                	
		                	}
		                }
	                }
	            }
	        }//while		    
		}//for
		java.util.Collections.sort(pageInfoList, new Comparator<Revision>() {

			@Override
			public int compare(Revision r1, Revision r2) {				
				return (int)(r2.getRevid() - r1.getRevid());
			}		
			
		});
		
	
		List<String> subset = new ArrayList<String>();
		List<String> includedPages = new ArrayList<String>();
		List<String> includedImages = new ArrayList<String>();
		int count = pageInfoList.size();
		count = count<maxItems?count:maxItems;
		for (int i = 0; i < count ; ++i)
		{
			
			RevisionWithImage page = new RevisionWithImage(wiki,
		    		wiki.getRevision(pageInfoList.get(i).getRevid()),
		    		((RevisionWithImage)pageInfoList.get(i)).getImage()); 
		    
		    if (page != null && !usersToIgnore.contains(HTTPTools.removeEscape(page.getUser())))
		    {		    	
		    	String title = HTTPTools.removeEscape(page.getPage());
		    	String time = null;
		    	if(NirvanaBot.TIME_FORMAT.equalsIgnoreCase("long")) 
		    		time = page.getTimestamp().getTime().toString();
		    	else {
		    		time = String.format("%1$tFT%1$tTZ",page.getTimestamp());
		    	}
		    	String element = String.format(this.format,
		    			namespace!=0?title.substring(wiki.namespaceIdentifier(this.namespace).length()+1):title,
		    			HTTPTools.removeEscape(page.getUser()), time, page.getImage()
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
