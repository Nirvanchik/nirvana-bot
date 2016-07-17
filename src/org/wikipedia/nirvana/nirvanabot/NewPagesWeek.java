/**
 *  @(#)NewPagesWeek.java
 *  Copyright © 2011-2014 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.ServiceError;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

/**
 * @author kin
 *
 */
public class NewPagesWeek extends NewPages {

	public static final int HOURS = 7 * 24;
	private static final String PAGE_NAME_FORMAT = "%s/День %d";
	private static final int FIRST_DAY = 0;
	private static final int DAYS = 7;
	private static final int LAST_DAY = FIRST_DAY-DAYS+1;
	/**
	 * @param param	 
	 */
	public NewPagesWeek(PortalParam param) {
		super(param);
		UPDATE_FROM_OLD = false;
	}
	
	public class WeekData extends Data {
		Map<String, Data> days = new HashMap<String, Data>(DAYS);
		public WeekData() {			
		}
	}
	
	private class WeekBuffer extends NewPagesBuffer {
		NewPagesBuffer buffers[] = new NewPagesBuffer[DAYS];
		Calendar dates[] = new Calendar[DAYS];
		public WeekBuffer(NirvanaWiki wiki) {
			super(wiki);
			Calendar cStart = Calendar.getInstance();			
			cStart.set(Calendar.HOUR_OF_DAY, 0);
	        cStart.set(Calendar.MINUTE, 0);
	        cStart.set(Calendar.SECOND, 0);
	        cStart.set(Calendar.MILLISECOND, 0);	
	        for(int i=0;i<DAYS;i++) {
	        	Calendar c = Calendar.getInstance();
	        	c.setTimeInMillis(cStart.getTimeInMillis());
	        	dates[i] = c;
	        	buffers[i] = new NewPagesBuffer(wiki);
	        	cStart.add(Calendar.DAY_OF_MONTH, -1);	        	
	        }
		}
		
		@Override
		protected void addNewItem(String title, boolean deleted, Revision rev) throws IOException {
			String element = formatItemString(title, deleted, rev);
	    	
	        if (!subset.contains(element))
	        {	            
	            for (int i = 0; i< DAYS; i++) {
	            	if (rev.getTimestamp().compareTo(dates[i])>=0) {
	            		buffers[i].addNewItem(title, deleted, rev);
	            		subset.add(element);
	    	            if (includedPages != null) {
	    	            	includedPages.add(title);
	    	            }
	    	            log.debug("ADD new line: \t"+element);
	            		break;
	            	}
	            }	            
	        }
		}
	};
	
	protected String makePatternString(String src) {
		String pattern = src;
		pattern = substParams(src, true);
		pattern = pattern.replace("{", "\\{")
				.replace("}", "\\}")
				.replace("|", "\\|")
				.replace("%(дата)", "\\d{1,2}\\s[\\p{InCyrillic}\\w]+\\s\\d{4}");
		return pattern;
	}
	
	protected String trimLeft(String text, String left) {
		String textTrimmed = text;
		if (!left.isEmpty()) {
			Pattern p = Pattern.compile("^\\s*"+makePatternString(left));
			Matcher m = p.matcher(textTrimmed);
			if (m.find()) {
				textTrimmed = textTrimmed.substring(m.group().length());
			}			
	    }
		return textTrimmed;
	}

    public Data getData(NirvanaWiki wiki) throws IOException, InterruptedException, ServiceError,
            BotFatalError {
		log.info("Processing data for [[" + this.pageName+"]]");		

        List<Revision> pageInfoList = getNewPages(wiki);

		//Map<String, Data> wikiPages = new HashMap<String, Data>();
		WeekData data = new WeekData();
		WeekBuffer buffer = new WeekBuffer(wiki);
		int count = pageInfoList.size();
		for (int i = 0; i < count ; ++i)
		{
		    buffer.addNewPage(wiki, pageInfoList.get(i));
		}
		
		for (int day=0,dayNum=0; day< DAYS; day++,dayNum--) {
			String pageName = String.format(PAGE_NAME_FORMAT, this.pageName, dayNum);
			Data d = new Data();
			NewPagesBuffer dayBuf = buffer.buffers[day];
			String text = "";
			try {
				text = wiki.getPageText(pageName);
			} catch (FileNotFoundException e) {
				// ignore
			}
			if (true/*dayNum == LAST_DAY || dayNum == 0*/) {
				analyzeOldText(wiki, text, d, dayBuf);
				if (dayNum == LAST_DAY) {
					data.archiveCount = d.archiveCount;
					data.archiveItems = d.archiveItems;
					data.makeArchiveText();
					d.newPagesCount = 0;
				} else if (dayNum == 0) {
					data.newPagesCount = d.newPagesCount;
					d.newPagesCount = dayBuf.size() - (d.oldCount - d.archiveCount - d.deletedCount);
					if(d.newPagesCount<0) d.newPagesCount = 0;
				} else {
					d.newPagesCount = 0;
				}
				d.archiveCount = 0;
				d.archiveItems = null;
			} /*else {
				String botsAllowString = NirvanaWiki.getAllowBotsString(text);
				//text = extractBotsAllowString(text, botsAllowString);
				log.debug("bots allow string: "+botsAllowString);
				if (botsAllowString != null) {
					botsAllowString = botsAllowString+"\n";
				}
				d.newText = buffer.getNewText(botsAllowString);				
			}*/
			data.days.put(pageName, d);
		}
        return data; 
	}

	@Override
    public boolean update(NirvanaWiki wiki, ReportItem reportData, String comment)
            throws IOException, LoginException, InterruptedException, ServiceError, BotFatalError {
		boolean updated = false;

		String text = getOldText(wiki);
		log.debug("old text retrieved");
		if (!checkAllowBots(wiki, text)) {
			return false;
		}
		
		headerLastUsed = header;
    	footerLastUsed = footer;
    	middleLastUsed = middle;
    	
		WeekData data = (WeekData)getData(wiki);
		reportData.newPagesFound = 0;
		reportData.pagesArchived = data.archiveCount;
		
		for (Entry<String,Data> entry:data.days.entrySet()) {
			Data d = entry.getValue();
			String str;
			if (d.newPagesCount>0) {
				str = "+"+String.valueOf(d.newPagesCount)+" статей";
			} else {
				str = comment;
			} 
			//log.debug(next.getKey()+" = "+next.getValue());
			log.info("Updating [[" + entry.getKey() +"]] " + str);
			if (wiki.editIfChanged(entry.getKey(), d.newText, str, this.minor, this.bot)) {
				updated = true;
				waitPauseIfNeed();
			}
			reportData.updated = updated;
			reportData.newPagesFound += d.newPagesCount;						
		}
		
		if (updated && archive!=null && (data.archiveText!=null || data.archiveItems.size()>0)) {		    	
			waitPauseIfNeed();
	    	log.info("Updating archive");
    		updateArchive(wiki, data, reportData);
	    }
		
		//reportData.newPagesFound = count;	    
		return updated;
	}
	
}
