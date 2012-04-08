/**
 *  @(#)ArchiveSettings.java 07/04/2012
 *  Copyright � 2011 - 2012 Dmitry Trofimovich (KIN)
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

import java.util.Calendar;

import org.wikipedia.nirvana.DateTools;

/**
 * @author kin
 *
 */
public class ArchiveSettings {
	public String archive;
	public String headerFormat;
	public String headerHeaderFormat;
	public boolean addToTop;
	public boolean removeDeleted;
	public Period archivePeriod;

	public enum Period {
		NONE (100,null),
		DAY (1,"%(����)"),
		WEEK (2,"%(������)"),
		MONTH (3,"%(�����)"),
		MONTHRP (3,"%(����� � ����������� ������)"),
		QUARTER (4,"%(�������)"),
		SEASON (4,"%(�����)"),
		YEAR (6,"%(���)");
		private final int degree;
		private final String template;
		Period(int degree, String template) {
			this.degree = degree;
			this.template = template;
		}
		public int degree() { return degree; }
		public String template() { return template; }
	};
	
	ArchiveSettings() {
		archive = null;
		archivePeriod = Period.NONE;
		addToTop = true;
		removeDeleted = false;
		headerFormat = null;
		headerHeaderFormat = null;
	}
	
	public boolean isSingle() {
		return (archivePeriod==Period.NONE);
	}
	
	public boolean withoutHeaders() {
		return (headerFormat==null);
	}
	
	public boolean isSimple() {
		return ((addToTop==true) && isSingle() && withoutHeaders()); 
	}
	
	public static ArchiveSettings.Period getHeaderPeriod(String format) {
		Period p_min = Period.NONE;
		for(Period p : Period.values()) {
			if(p.template!=null && format.contains(p.template)) {
				if(p.degree<p_min.degree) {
					p_min = p;
				}
			}
		}
		return p_min;
	}
	
	public String getArchiveForDate(Calendar c) {
		if(archivePeriod==Period.NONE) {
			return archive;
		}
		String name = archive;
		if(name.contains(Period.YEAR.template))
			name = name.replace(Period.YEAR.template, String.valueOf(c.get(Calendar.YEAR)));
		if(name.contains(Period.SEASON.template))
			name = name.replace(Period.SEASON.template, DateTools.russianSeasons[c.get(Calendar.MONTH)/3]);
		if(name.contains(Period.QUARTER.template))
			name = name.replace(Period.QUARTER.template, String.valueOf(c.get(Calendar.MONTH)/3));
		if(name.contains(Period.MONTH.template))
			name = name.replace(Period.MONTH.template, DateTools.russianMonths[c.get(Calendar.MONTH)]);
		return name;
	}
	public static String getHeaderForDate(Calendar c, String format) {
		String header=format;
		if(header==null)
			return header;
		String year,month,season,quarter,week,day,month_monat;
		year = String.valueOf(c.get(Calendar.YEAR));
		season = DateTools.russianSeasons[c.get(Calendar.MONTH)/3];
		quarter = String.valueOf(c.get(Calendar.MONTH)/3);
		month = DateTools.russianMonths[c.get(Calendar.MONTH)];
		month_monat = DateTools.russianMonat[c.get(Calendar.MONTH)];
		week = String.valueOf(c.get(Calendar.WEEK_OF_YEAR));
		day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
		return header.replace(Period.YEAR.template, year).
				replace(Period.SEASON.template, season).
				replace(Period.QUARTER.template, quarter).
				replace(Period.MONTH.template, month).
				replace(Period.MONTHRP.template, month_monat).
				replace(Period.WEEK.template, week).
				replace(Period.DAY.template, day);
	}
	
	public String getHeaderForDate(Calendar c) {
		return ArchiveSettings.getHeaderForDate(c, headerFormat);
	}
	
	public String getHeaderHeaderForDate(Calendar c) {
		return ArchiveSettings.getHeaderForDate(c, headerHeaderFormat);
	}

}
