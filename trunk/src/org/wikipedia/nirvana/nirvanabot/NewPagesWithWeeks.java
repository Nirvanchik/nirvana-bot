/**
 *  @(#)NewPagesWithWeeks.java 07/04/2012
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.StringUtils;
import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.DateTools;
import org.wikipedia.nirvana.HTTPTools;
import org.wikipedia.nirvana.NirvanaWiki;

/**
 * @author kin
 *
 */
public class NewPagesWithWeeks extends NewPages {

	public static final int HOURS = 192;
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
	 * @param minor
	 * @param bot
	 */
	public NewPagesWithWeeks(String lang, ArrayList<String> categories,
			ArrayList<String> categoriesToIgnore,
			ArrayList<String> usersToIgnore, String page, String archive,
			int ns, int depth, int hours, int maxItems, String format,
			String delimeter, String header, String footer, boolean minor,
			boolean bot) {
		super(lang, categories, categoriesToIgnore, usersToIgnore, page,
				archive, null,
				ns, depth, HOURS, maxItems, format, delimeter, header,
				footer, minor, bot);
		
	}
	
	public void getNewPages(NirvanaWiki wiki, 
			ArrayList<Revision> pageInfoList,
			HashSet<String> pages,
			HashSet<String> ignore) throws IOException {
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
	                	log.debug("Namespace is not 0");
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
		                Revision page = wiki.getRevision(revId); 
	                	if(page!=null) {
	                		pages.add(title);
	                		pageInfoList.add(page);
	                	} else {
	                		log.warn("revision not found: " + revId+ ", title: "+title);
	                	}
	                }
	            }
	        }//while		    
		}//for
	}
	
	public Map<String,Data> processData(NirvanaWiki wiki) throws IOException {
		log.info("Processing data for [[" + this.pageName+"]]");
		HashSet<String> ignore = getIgnorePages(null);
		//Revision r = wiki.getFirstRevision("Кай Юлий Цезарь");
		ArrayList<Revision> pageInfoList = new ArrayList<Revision>(30);
		HashSet<String> pages = new HashSet<String>();
		/*for (String category : categories) {
			log.info("Processing data of " + category);
		}*/		
		getNewPages(wiki,pageInfoList,pages,ignore);
		
		java.util.Collections.sort(pageInfoList, new Comparator<Revision>() {
			@Override
			public int compare(Revision r1, Revision r2) {				
				return (int)(r1.getTimestamp().compareTo(r2.getTimestamp()));
			}				
		});
		
		Map<String, Data> wikiPages = new HashMap<String, Data>();
		         
        Calendar cStart = Calendar.getInstance();
        Calendar cEnd = Calendar.getInstance();
        //cStart.add(Calendar.DAY_OF_MONTH, 0);   
        cStart.set(Calendar.HOUR_OF_DAY, 0);
        cStart.set(Calendar.MINUTE, 0);
        cStart.set(Calendar.SECOND, 0);
        cStart.set(Calendar.MILLISECOND, 0);
        cEnd.setTimeInMillis(cStart.getTimeInMillis());
        cEnd.add(Calendar.DAY_OF_MONTH, 1);        
        for (int i = 0; i < 7; ++i,
        		cStart.add(Calendar.DAY_OF_MONTH, -1),
        		cEnd.add(Calendar.DAY_OF_MONTH, -1))
        {
            //List<Revision> subset = new ArrayList<Revision>();
            List<String> result = new ArrayList<String>();
            Iterator<Revision> it = pageInfoList.iterator();
            while(it.hasNext()) {
            	Revision r = it.next();
            	if(r.getTimestamp().compareTo(cStart)>=0 && r.getTimestamp().compareTo(cEnd)<0) {
            	//	subset.add(r);
            		String title = HTTPTools.removeEscape(r.getPage());
    		    	String time = null;
    		    	if(NirvanaBot.TIME_FORMAT.equalsIgnoreCase("long")) 
    		    		time = r.getTimestamp().getTime().toString();
    		    	else {
    		    		time = String.format("%1$tFT%1$tTZ",r.getTimestamp());
    		    	}
    		    	String element = String.format(this.format,
    		    			namespace!=0?title.substring(wiki.namespaceIdentifier(this.namespace).length()+1):title,
    		    			r.getUser(), time
    		    			);
            		result.add(element);
            	}
            }                
            
            if (result.size() != 0)
            {
                String pageName = this.pageName + "/" + DateTools.printDateDayMonthYearGenitiveRussian(cStart);
                Data d = new Data();
        		d.newText = header + StringUtils.join(result.toArray(),this.delimeter) + footer;
        		d.newPagesCount = result.size();
        		
        		String oldText = "";
        		try {
        		oldText = wiki.getPageText(pageName);
        		} catch (java.io.FileNotFoundException e) {
        			
        		}
        		d.archiveText = oldText;
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
    		    String[] oldItems = oldText.split(delimeter);
    		    int oldCount = 0;
    		    for (String item : oldItems) {
    		    	if(!item.isEmpty()) oldCount++;
    		    }
    			d.archiveCount = oldCount;
    			d.newPagesCount -= oldCount;
                wikiPages.put(pageName, d);
            }
        }
        return wikiPages; 
	}
	
	

	
	@Override
	public boolean update(NirvanaWiki wiki, ReportItem reportData, String comment) throws IOException, LoginException, InterruptedException {
		boolean updated = false;
		getData(wiki);
		Map<String,Data> d = processData(wiki);
		reportData.pagesArchived = 0;
		reportData.newPagesFound = 0;
		
		Set<Entry<String,Data>> set = d.entrySet();
		Iterator<Entry<String,Data>> it = set.iterator();
		
		while(it.hasNext()) {
			Entry<String,Data> next = it.next();
			String str = "+"+String.valueOf(next.getValue().newPagesCount)+" новых";
			//log.debug(next.getKey()+" = "+next.getValue());
			log.info("Updating [[" + next.getKey() +"]] " + str);
			wiki.edit(next.getKey(), next.getValue().newText, str, this.minor, this.bot);
			updated = true;
			reportData.updated = updated;
			reportData.newPagesFound += next.getValue().newPagesCount;
		}
		
		//reportData.newPagesFound = count;	    
		return updated;
	}
	/*
	@Override
	public boolean dump(NirvanaWiki wiki, ReportItem reportData, String folder,String file) throws IOException, LoginException, InterruptedException {
		boolean updated = false;
		String text = getData(wiki);
		String fileNew;
		String fileOld;
		fileOld = file+".old.txt";
		
		if(fileOld!=null) {
			FileTools.dump(text, folder, fileOld);
		}
		
		Map<String,Data> d = processData(wiki);
		reportData.pagesArchived = 0;
		reportData.newPagesFound = 0;
		
		Set<Entry<String,Data>> set = d.entrySet();
		Iterator<Entry<String,Data>> it = set.iterator();
		
		while(it.hasNext()) {
			Entry<String,Data> next = it.next();
			String str = "+"+String.valueOf(next.getValue().newPagesCount)+" новых";
			log.info("Dumping [[" + next.getKey() +"]] " + str);		
			fileOld = next.getKey()+".old.txt";
			FileTools.dump(next.getValue().archiveText, folder, fileOld);
			fileNew = next.getKey()+".new.txt";
			FileTools.dump(next.getValue().newText, folder, fileNew);
			updated = true;
			reportData.updated = updated;
			reportData.newPagesFound += next.getValue().newPagesCount;
		}	
		
		return updated;
	}
*/
}
