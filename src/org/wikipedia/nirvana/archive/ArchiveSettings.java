/**
 *  @(#)ArchiveSettings.java 02/07/2012
 *  Copyright © 2014 Dmitry Trofimovich (KIN)(DimaTrofimovich@gmail.com)
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
package org.wikipedia.nirvana.archive;

import java.util.Calendar;

import org.wikipedia.nirvana.DateTools;

/**
 * @author kin
 *
 */
public class ArchiveSettings {
	private static int START_YEAR_DEFAULT = 2003;
	public String archive;
	public String headerFormat;
	public String headerHeaderFormat;
	public boolean addToTop;
	public boolean removeDeleted;
	public Period archivePeriod;
	public Enumeration enumeration;
	public boolean sorted;
	public boolean removeDuplicates;
	public int parseCount;
	public int startYear;
	public int startYear2;
	public String firstArchive;
	//public boolean supportCategory;
	
	public static void setDefaultStartYear(int year) {
		START_YEAR_DEFAULT = year;
	}
	
	public static int getDefaultStartYear() {
		return START_YEAR_DEFAULT;
	}
	
	public enum Enumeration {
		NONE,
		HASH,
		HTML,
		HTML_GLOBAL,
		DETECT
	};

	public enum Period {
		NONE (100,null,false),
		DAY (1,"%(день)",true),
		WEEK (2,"%(недел€)",true),
		MONTH (3,"%(мес€ц)",false),
		MONTHRP (3,"%(мес€ц в родительном падеже)",false),
		QUARTER (4,"%(квартал)",true),
		SEASON (4,"%(сезон)",true),
		YEAR (6,"%(год)",true);
		private final int degree;
		private final String template;
		private final boolean numeric;
		Period(int degree, String template, boolean numeric) {
			this.degree = degree;
			this.template = template;
			this.numeric = numeric;
		}
		public int degree() { return degree; }
		public String template() { return template; }
		public boolean isNumeric() { return numeric; }
		//public String 
	};
	
	public ArchiveSettings() {
		archive = null;
		archivePeriod = Period.NONE;
		addToTop = true;
		removeDeleted = false;
		headerFormat = null;
		headerHeaderFormat = null;
		enumeration = Enumeration.NONE;
		sorted = false;
		removeDuplicates = false;
		parseCount = ArchiveWithHeaders.HOW_MANY_ITEMS_TO_PARSE_DEFAULT; 
		startYear = START_YEAR_DEFAULT;
		startYear2 = -1;
		firstArchive = null;
		//supportCategory = false;
	}
	
	public boolean isSingle() {
		return (archivePeriod==Period.NONE);
	}
	
	public boolean withoutHeaders() {
		return (headerFormat==null);
	}
	
	public boolean isSimple() {
		return ((addToTop==true) && isSingle() && withoutHeaders() && !hasHtmlEnumeration() /*&& !supportCategory*/); 
	}
	
	public boolean hasHtmlEnumeration () {
		return (enumeration == Enumeration.HTML || enumeration == Enumeration.HTML_GLOBAL);
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
			name = name.replace(Period.QUARTER.template, String.valueOf(c.get(Calendar.MONTH)/3+1));
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
