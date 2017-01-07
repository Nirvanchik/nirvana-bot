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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.wikipedia.nirvana.FileTools;
import org.wikipedia.nirvana.NirvanaWiki;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author kin
 *
 */
public class BotReporter {
    private static final Logger log;

	public static final String DEFAULT_CACHE_FOLDER = "cache";
	public static final String DEFAULT_CACHE_FILE = "report_cache.js";

	NirvanaWiki wiki;
	ArrayList<ReportItem> reportItems;
	Calendar timeStarted;
	Calendar timeFinished;
	
	private int portalsTotal;
	private int portalsChecked;
	private int portalsProcessed;
	private int portalsUpdated;
	private int portalsError;
	
	String version;

    private String preambula = "";

    static {
        log = LogManager.getLogger(BotReporter.class.getName());
    }

	public BotReporter(NirvanaWiki wiki, int capacity, boolean started, String version, String preambulaFile) {
		this.wiki = wiki;
		this.version = version;
		reportItems = new ArrayList<ReportItem>(capacity);
		if (started) {
			botStarted(true);
		}
        if (preambulaFile!= null && !preambulaFile.isEmpty()) {
            preambula = FileTools.readFileSilently(preambulaFile, "");
            if (!preambula.isEmpty() && !preambula.endsWith("\n")) {
                preambula += "\n";
            }
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
    	String text = String.format("{{%1$s|status=1|starttime=%2$tF %2$tT|version=%3$s}}", template, timeStarted, version);
    	wiki.edit(page, text, "Бот запущен");
    }
    
    public void updateEndStatus(String page, String template) throws LoginException, IOException {
    	String text = String.format("{{%1$s|status=0|starttime=%2$tF %2$tT|endtime=%3$tF %3$tT|time=%4$s"+
    			"|total=%5$d|checked=%6$d|processed=%7$d|updated=%8$d|errors=%9$d|version=%10$s}}", 
    			template, timeStarted, timeFinished, printTimeDiff(timeStarted, timeFinished),
    			portalsTotal, portalsChecked, portalsProcessed, portalsUpdated, portalsError, version);
    	wiki.edit(page, text, "Бот остановлен");
    }

    public void updateStatus() {
    	
    }
    
    public void reportTXT(String fileName) {
    	if (timeFinished == null) {
    		timeFinished = Calendar.getInstance();
    	}
    	if (!fileName.endsWith(".txt")) {
    		fileName = fileName + ".txt";
    	}
		log.info("generating report (TXT) . . .");
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
    
    public void reportWiki(String reportPage, boolean mainLaunch) {
    	// 1) Load old data (if any)
    	String path = DEFAULT_CACHE_FOLDER + "\\" + DEFAULT_CACHE_FILE;
    	File file = new File(path);
    	List<ReportItem> oldData = null;		
    	if (file.exists()) {
    		log.debug("File with old data found: " + path);
    		// Check last modification time. Ignore if it's too old
			long lastModTime = file.lastModified();
    		if (lastModTime != 0) {
    			Calendar c = Calendar.getInstance();
    			c.add(Calendar.DAY_OF_MONTH, -1);
    			if (lastModTime > c.getTimeInMillis()) {
    				log.debug("Load old data");
    				oldData = load(file);
    			}   			
    		}
    		log.debug("Delete old data");
    		file.delete();
    	}
    	// 2) Merge with old data (if any)
    	if (oldData != null) {
    		merge(oldData);
    	}
    	// 3) Report to wiki or save to file
    	if (mainLaunch) {
    		doReportWiki(reportPage);    		
    	} else {
    		save(file);
    	}
    }
    
    public void merge(List<ReportItem> data) {
    	log.debug("Merge old data");
    	// 3 cases here:
    	// 1) lists are equal (ideal)
    	// 2) left has values that right has not
    	// 3) right has values that left has not
    	ArrayList<ReportItem> right = new ArrayList<ReportItem>(data);
    	for (ReportItem r: reportItems) {
    		for (int j = 0; j < right.size(); j++) {
    			if (r.equals(right.get(j))) {
    				ReportItem rMerge = right.get(j);
    				r.merge(rMerge);
    				right.remove(j);
    				break;
    			}
    		}    		
    	}
    	reportItems.addAll(right);
    }
    
    
    public void doReportWiki(String reportPage) {
    	if (timeFinished == null) {
    		timeFinished = Calendar.getInstance();
    	}
    	log.info("generating report (Wiki) . . .");
		StringBuilder sb = new StringBuilder();
        sb.append(preambula);
		sb.append(ReportItem.getHeaderWiki()).append("\n");
		int i = 0;
		for(ReportItem item : reportItems) {
			sb.append(item.toStringWiki(i));
			sb.append("\n");
			i++;
		}
		sb.append(ReportItem.getFooterWiki());
		
		try {
	        wiki.edit(reportPage, sb.toString(), "Отчёт по работе бота за сутки");
        } catch (LoginException | IOException e) {
	        log.error("Failed to update report.", e);
        }    	
    }
	/**
     * 
     */
    
    private void logStartStatus() {
    	log.info(String.format("BOT STARTED %1$tF %1$tT version: %2$s", timeStarted, version));
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
    
    public List<ReportItem> load(File file) {
    	ObjectMapper mapper = new ObjectMapper();
    	List<ReportItem> items = null;
    	try {
    		items = mapper.readValue(
					file, 
					new TypeReference<List<ReportItem>>() { });
		} catch(IOException e1) {
			log.error(e1);
			return null;
		}
    	return items;
    }
    
    public void save(File file) {
    	ObjectMapper mapper = new ObjectMapper();		
		try {
			mapper.writeValue(file, reportItems);
		} catch (JsonParseException e) {
			log.error(e);
			return;
		} catch (JsonMappingException e) {
			log.error(e);
			return;
		} catch (IOException e) {
			log.error(e);
			return;
		}
		log.info("report items successfully saved to "+file.getPath());
    }
}
