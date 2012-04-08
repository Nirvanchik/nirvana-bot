/**
 *  @(#)ReportItem.java 07/04/2012
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

import java.util.TimeZone;

import org.wikipedia.nirvana.nirvanabot.NirvanaBot.Status;

/**
 * @author kin
 *
 */
public class ReportItem {
	String portal;
	Status status;
	long startTime;
	long endTime;
	int errors;
	boolean updated;
	boolean archived;
	boolean settingsValid;
	int newPagesFound;
	int pagesArchived;
	
	ReportItem(String name) {
		portal = name;
		startTime = 0;
		endTime = 0;
		errors = 0;
		//archive = false;
		newPagesFound = 0;
		pagesArchived = 0;
		status = Status.NONE;
		settingsValid = true;
	}
	
	public static String getHeader() {
		String header = String.format("%1$-90s %2$-9s %3$-9s %4$s %5$s  %6$s", 
				"portal settings page","status","time","new p.","arch.p.","errors");
		header += "\r\n";
		header += String.format("%1$114s %2$s  %3$s"," ", "updated","archived");
		return header;
	}
	public static String getFooter() {
		return "";
	}
	
	@Override
	public String toString() {
		String line ="";
		long time = 0;
		String timeString = "N/A";
		if(endTime!=0) {
			time = endTime - startTime;
			//Date t = new Date(time);
			//Calendar c = Calendar.getInstance();
			//c.setTimeInMillis(time);
			
			timeString = String.format("%1$tT", time - TimeZone.getDefault().getRawOffset());
			/*
			time = time/1000;
			int hours = (int)time/(60*60);
			int min = (int)time/60 - hours*60;
			int sec = (int)(time - hours*60*60 - min*60); 
			timeString = String.format("%1$2d:%2$2d:%3$2d", hours, min, sec);
			*/
		}
		int max = 90;
		String name2 = portal;
		
		String name1 = "";
		if (name2.length() > max) {
			//int separator = max;
			int n = name2.lastIndexOf(' ', max);
			if(n<max-20 || n<0)
				n = max;
			name1 = name2.substring(0, n);
			name2 = name2.substring(n+1);
		}
		String upd = this.updated?NirvanaBot.YES:NirvanaBot.NO;
		String arch = this.archived?NirvanaBot.YES:NirvanaBot.NO;
		line = String.format("%1$-90s %2$-9s %3$9s %4$3d %5$-3s  %6$3d %7$-3s %8$2d", 
				name2, status, timeString,
				this.newPagesFound, upd, 
				this.pagesArchived, arch,
				this.errors);
		if(!name1.isEmpty()) {
			line = name1+"\r\n"+line;
		}
		return line;			
	}


	public void skip() {
		this.status = Status.SKIP;
		
	}
}