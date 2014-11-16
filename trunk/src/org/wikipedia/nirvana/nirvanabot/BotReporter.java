/**
 *  @(#)BotReporter.java 
 *  Copyright © 2014 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import javax.security.auth.login.LoginException;

import org.wikipedia.nirvana.FileTools;
import org.wikipedia.nirvana.NirvanaWiki;

/**
 * @author kin
 *
 */
public class BotReporter {
	private static org.apache.log4j.Logger log;
	NirvanaWiki wiki;
	ArrayList<ReportItem> reportItems;
	Calendar timeStarted;
	Calendar timeFinished;
	
	private int portalsTotal;
	private int portalsChecked;
	private int portalsProcessed;
	private int portalsUpdated;
	private int portalsError;
	
	static {
		log = org.apache.log4j.Logger.getLogger(BotReporter.class.getName());
	}
	
	public BotReporter(NirvanaWiki wiki, int capacity, boolean started) {
		this.wiki = wiki;
		reportItems = new ArrayList<ReportItem>(capacity);
		if (started) {
			botStarted(true);
		}
	}
	
	public void botStarted(boolean log) {
		timeStarted = Calendar.getInstance();
		if (log) {
			logStartStatus();
		}
	}
	
	public void botFinished(boolean log) {
		timeFinished = Calendar.getInstance();
		if (log) {
			logEndStatus();
		}
	}
	
	public void setTotal(int total) {
		portalsTotal = total;
	}
	
	public void addToTotal(int num) {
		portalsTotal += num;
	}
	
	public void portalChecked() {
		portalsChecked++;
	}
	public void portalProcessed() {
		portalsProcessed++;
	}
	public void portalUpdated() {
		portalsUpdated++;
	}
	public void portalError() {
		portalsError++;
	}
	/**
     * @param reportItem
     */
    public void add(ReportItem reportItem) {
	    reportItems.add(reportItem);	    
    }
    
    public void report() {
    	
    }
    
    public void updateStartStatus(String page, String template) throws LoginException, IOException {
    	String text = String.format("{{%1$s|status=1|starttime=%2$tF %2$tT}}", template, timeStarted);
    	wiki.edit(page, text, "Бот запущен");
    }
    
    public void updateEndStatus(String page, String template) throws LoginException, IOException {
    	String text = String.format("{{%1$s|status=0|starttime=%2$tF %2$tT|endtime=%3$tF %3$tT|time=%4$s"+
    			"|total=%5$d|checked=%6$d|processed=%7$d|updated=%8$d|errors=%9$d}}", 
    			template, timeStarted, timeFinished, printTimeDiff(timeStarted, timeFinished),
    			portalsTotal, portalsChecked, portalsProcessed, portalsUpdated, portalsError);
    	wiki.edit(page, text, "Бот остановлен");
    }

    public void updateStatus() {
    	
    }
    
    public void reportTXT(String fileName) {
    	if (timeFinished == null) {
    		timeFinished = Calendar.getInstance();
    	}
		log.info("generating report . . .");
		StringBuilder sb = new StringBuilder();
		//StringBuffer sbuf = new StringBuffer();
		sb.append(ReportItem.getHeaderTXT()).append("\r\n");
		for(ReportItem item : reportItems) {
			sb.append(item.toStringTXT());
			sb.append("\r\n");
		}
		sb.append(ReportItem.getFooterTXT());
		try {
			FileTools.writeFile(sb.toString(), fileName);				
		} catch (IOException e) {
			log.error(e.toString());
			e.printStackTrace();				
		}			
		log.info("report is generated!");    	
    }
	/**
     * 
     */
    
    private void logStartStatus() {
    	log.info(String.format("BOT STARTED %1$tF %1$tT", timeStarted));
    }
    
    public void logStatus() {
        log.info("portals: "+String.valueOf(portalsTotal)
    			+", checked: "+String.valueOf(portalsChecked)
    			+", processed: "+String.valueOf(portalsProcessed)
    			+", updated: "+String.valueOf(portalsUpdated)
    			+", errors: "+String.valueOf(portalsError));    	
    }
    
    
    private void logEndStatus() {
    	long start = timeStarted.getTimeInMillis();
    	long end = timeFinished.getTimeInMillis();
    	log.info(String.format("BOT STARTED %1$tF %1$tT", timeStarted));
		log.info(String.format("BOT FINISHED %1$tF %1$tT ",timeFinished));
		log.info("FULL WORK TIME: "+printTimeDiff(end-start));
    }

    public static String printTimeDiff(Calendar time1, Calendar time2) {		
    	long start = time1.getTimeInMillis();
    	long end = time2.getTimeInMillis();
		return printTimeDiff(end - start);
	}
    
    public static String printTimeDiff(long diff) {
		int hours = (int) ((diff)/(60L*60L*1000L));
		int min = (int) ((diff)/(60L*1000L) - (long)hours*60L);
		int sec = (int) ((diff)/(1000L) - (long)hours*60L*60L - (long)min*60L);
		return String.format("%d h %d m %d s", hours, min, sec);
	}
}
