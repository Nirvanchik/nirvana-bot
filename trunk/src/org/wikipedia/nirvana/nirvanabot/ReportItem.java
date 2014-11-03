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
import org.wikipedia.nirvana.nirvanabot.NirvanaBot.Status;

/**
 * @author kin
 *
 */
public class ReportItem {
	private static final int MAX_LEN = 90;
	String template;
	String portal;
	Status status;
	BotError error;
	long startTime;
	long endTime;
	int errors;
	boolean updated;
	boolean archived;
	boolean settingsValid;
	int newPagesFound;
	int pagesArchived;
	
	ReportItem(String template, String name) {
		this.template = template;
		this.portal = name;
		startTime = 0;
		endTime = 0;
		errors = 0;
		//archive = false;
		newPagesFound = 0;
		pagesArchived = 0;
		status = Status.NONE;
		error = BotError.NONE;
		settingsValid = true;
	}
	
	public static String getHeaderTXT() {
		String header = String.format("%1$-90s %2$-9s %3$-9s %4$s %5$s  %6$s", 
				"portal settings page","status","time","new p.","arch.p.","errors");
		header += "\r\n";
		header += String.format("%1$114s %2$s     %3$s  %4$s"," ", "upd.","arch.","Error");
		return header;
	}
	
	public static String getFooterTXT() {
		return "";
	}
	
	public String toStringTXT() {
		String line ="";
		long time = 0;
		String timeString = "N/A";
		if (endTime != 0) {
			time = endTime - startTime;
			timeString = String.format("%1$tT", time - TimeZone.getDefault().getRawOffset());
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
		String upd = this.updated?NirvanaBot.YES:NirvanaBot.NO;
		String arch = this.archived?NirvanaBot.YES:NirvanaBot.NO;
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


	public void skip() {
		this.status = Status.SKIP;
		
	}
}