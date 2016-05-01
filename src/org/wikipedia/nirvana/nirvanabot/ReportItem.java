/**
 *  @(#)ReportItem.java 
 *  Copyright © 2011 - 2014 Dmitry Trofimovich (KIN)(DimaTrofimovich@gmail.com)
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

import java.util.TimeZone;

import org.wikipedia.nirvana.nirvanabot.NirvanaBot.BotError;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;


/**
 * @author kin
 *
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@JsonIgnoreProperties({"getHeaderTXT","getHeaderWiki","getFooterTXT","getFooterWiki"})
public class ReportItem {
	private static final int MAX_LEN = 90;
	String template;
	String portal;
	Status status;
	BotError error;
	@JsonIgnore
	long startTime;
	@JsonIgnore
	long endTime;
	long timeDiff;
	int errors;
	boolean updated;
	boolean archived;
	boolean settingsValid;
	int newPagesFound;
	int pagesArchived;
	int times;
	
	enum Status {
		NONE (0),
		SKIP (1),
		PROCESSED (2),
		UPDATED (3),
		//DENIED,
		ERROR (4);		
		public final int order;
		Status(int order) {
			this.order = order;
		}
		public static Status selectBest(Status left, Status right) {
			if (right.order > left.order) {
				return right;
			}
			return left;
		}
		
		public boolean isSuccess() {
			return this == PROCESSED || this == UPDATED;
		}
		
		public boolean isFailure() {
			return this == ERROR;
		}
	};
	
	public ReportItem() {
		// default constructor
	}
	
	public ReportItem(String template, String name) {
		this.template = template;
		this.portal = name;
		startTime = 0;
		endTime = 0;
		errors = 0;
		times = 0;
		//archive = false;
		newPagesFound = 0;
		pagesArchived = 0;
		status = Status.NONE;
		error = BotError.NONE;
		settingsValid = true;
		//this.equals(obj);
	}
	
	private static String wikiYesNoStringRu(boolean b) {
		return b?"{{Да}}":"Нет";
	}
	
	private static String wikiYesNoCancelStringRu(String str, boolean yes, boolean no) {
		if (yes) {
			return String.format("{{Да|%s}}", str);
		} else if (no) {
			return String.format("{{Нет|%s}}", str);
		} else {
			return str;
		}
	}
	private static String wikiErrorStringRu(String error, boolean isError) {
		if (isError) {
			return String.format("{{Нет|%s}}", error);
		}
		return "-";
	}
	
	public static String getHeaderTXT() {
		String header = String.format("%1$-90s %2$-9s %3$-9s %4$s %5$s  %6$s", 
				"portal settings page","status","time","new p.","arch.p.","errors");
		header += "\r\n";
		header += String.format("%1$114s %2$s     %3$s  %4$s"," ", "upd.","arch.","Error");
		return header;
	}
	
	public static String getHeaderWiki() {
		String header = "{| class=\"wikitable sortable\" style=\"text-align:center\"\n" +
				"|-\n" +
                "! № !! портал/проект !! проходов !! статус !! время !! новых статей !! н.с. обновлены !! статей в архив !! архив обновлен !! ошибок !! ошибка";
		return header;
	}
	
	public static String getFooterTXT() {
		return "";
	}

	public static String getFooterWiki() {
		return "|}";
	}
	
	public String toStringTXT() {
		String line = "";
		String timeString = "N/A";
		if (timeDiff > 0) {
			timeString = String.format("%1$tT", timeDiff - TimeZone.getDefault().getRawOffset());
		}
		String name2 = portal;
		
		String name1 = "";
		if (name2.length() > MAX_LEN) {
			//int separator = max;
			int n = name2.lastIndexOf(' ', MAX_LEN);
			if(n<MAX_LEN-20 || n<0) {
				n = MAX_LEN;
			}
			name1 = name2.substring(0, n);
			name2 = name2.substring(n+1);
		}
		String upd = this.updated? NirvanaBot.YES: NirvanaBot.NO;
		String arch = this.archived? NirvanaBot.YES: NirvanaBot.NO;
		line = String.format("%1$-90s %2$-9s %3$9s %4$3d %5$-3s  %6$3d %7$-3s %8$2d %9$-13s", 
				name2, status, timeString,
				this.newPagesFound, upd, 
				this.pagesArchived, arch,
				this.errors, this.error.toString());
		if(!name1.isEmpty()) {
			line = name1+"\r\n"+line;
		}
		return line;			
	}
	
	public String toStringWiki(int lineNum) {
		String line = "";
		String timeString = "N/A";
		if (timeDiff > 0) {
			timeString = String.format("%1$tT", timeDiff - TimeZone.getDefault().getRawOffset());
		}
		String upd = wikiYesNoStringRu(this.updated);
		String arch = wikiYesNoStringRu(this.archived);
		//| 2 ||align='left'| {{user|Игорь Васильев}}
		String errorStr = wikiErrorStringRu(error.toString(), error!= BotError.NONE);
		String statusStr = wikiYesNoCancelStringRu(status.toString(), status.isSuccess(), status.isFailure());
		line = String.format("|-\n|%10$d ||align='left'| [[%1$s]] || %11$d || %2$s || %3$s || %4$d || %5$s || %6$d || %7$s || %8$d || %9$s", 
				portal, statusStr, timeString, 
				newPagesFound, upd, pagesArchived, arch,
				errors, errorStr,
				lineNum, times);
		return line;
	}


	public void skip() {
		this.status = Status.SKIP;		
		this.times = 0;
	}
	
	public void updated() {
		this.status = Status.UPDATED;
	}
	
	public void processed() {
		this.status = Status.PROCESSED;
		this.times++;
	}
	
	public void error() {
		this.status = Status.ERROR;
	}
	
	public void error(BotError error) {
		this.status = Status.ERROR;
		this.error = error;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (!(o instanceof ReportItem)) {
			return false;
		}
		ReportItem a = (ReportItem)o;
		if (!template.equals(a.template)) {
			return false;
		}
		if (!portal.equals(a.portal)) {
			return false;
		}
		return true;
	}

	/**
     * 
     */
    public void start() {
	    startTime = System.currentTimeMillis();	    
    }

	/**
     * 
     */
    public void end() {
    	endTime = System.currentTimeMillis();	
    	timeDiff = endTime - startTime;
    }
    
    public void merge(ReportItem r) {
    	assert template.equals(r.template) && portal.equals(r.portal);
    	newPagesFound += r.newPagesFound;
    	pagesArchived += r.pagesArchived;
    	times += r.times;
    	errors += r.errors;
    	settingsValid = settingsValid || r.settingsValid;
    	updated = updated || r.updated;
    	archived = archived || r.archived;
    	if (timeDiff > 0 && r.timeDiff > 0) {
    		timeDiff = (timeDiff + r.timeDiff)/2;
    	} else if (r.timeDiff > 0) {
    		timeDiff = r.timeDiff;
    	}
    	if (error == BotError.NONE && r.error != BotError.NONE) {
    		error = r.error;
    	}
    	status = Status.selectBest(status, r.status);
    }
    
    
}