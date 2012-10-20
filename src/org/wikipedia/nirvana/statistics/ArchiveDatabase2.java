/**
 *  @(#)ArchiveDatabase.java 20/10/2012
 *  Copyright © 2012 Dmitry Trofimovich (KIN)
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
package org.wikipedia.nirvana.statistics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.wikipedia.nirvana.FileTools;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author kin
 * when filling this db with data, each year (and year after the last) 
 * must be marked by calling markYearBegin()
 */
public class ArchiveDatabase2 {

	public static final String DEFAULT_CACHE_FOLDER = "cache";
	String cacheFolder = DEFAULT_CACHE_FOLDER;
	String name;
	String dbPath;
	ArrayList<ArchiveItem> items;
	ArchiveItem lastItemFromCache = null;
	ArchiveItem max = null;
	Map<Integer,Integer> yearIndexes = null;
	//boolean indexed = false;
	boolean sorted = true;
	///int lastYearFound = 0;
	private Logger log;
	
	/*
	 * this class is not year-safe
	 * the year must be in database
	 * */
	public class ArchiveDBYearIterator implements ListIterator<ArchiveItem>{
		protected ListIterator<ArchiveItem> iterator;
		//int startIndex;
		int endIndex=-1;
		//int index;
		int year;

		ArchiveDBYearIterator(int year) {
			this.year = year;			
			Integer index = yearIndexes.get(year);
			if(index==null) {
				iterator = items.listIterator(items.size());
				endIndex = items.size();
			}
			else {
				iterator = items.listIterator(index);
				index = yearIndexes.get(year+1);
				if(index==null) endIndex = items.size();
				else endIndex = index;
			}
			
			 				
		}

		@Override
		public boolean hasNext() {			
			if( iterator.hasNext()) {
				return iterator.nextIndex()!=endIndex;
			} else {
				return false;
			}
		}

		@Override
		public void add(ArchiveItem item) {
			iterator.add(item);			
		}

		
		@Override
		public boolean hasPrevious() {			
			return iterator.hasPrevious();
		}


		@Override
		public int nextIndex() {
			return iterator.nextIndex();
		}

		@Override
		public ArchiveItem previous() {			
			return iterator.previous();
		}

		@Override
		public int previousIndex() {			
			return iterator.previousIndex();
		}

		@Override
		public void remove() {
			iterator.remove();			
		}

		@Override
		public void set(ArchiveItem item) {
			iterator.set(item);			
		}

		@Override
		public ArchiveItem next() {
			return iterator.next();
		}
				
	};
	
	ArchiveDatabase2(String name, boolean loadFromCache, String cache) {
		this.name = name;
		this.cacheFolder = cache;		
		dbPath = cacheFolder+"\\"+FileTools.normalizeFileName(name)+".js";
		log = org.apache.log4j.Logger.getLogger(this.getClass().getName());
		items = new ArrayList<ArchiveItem>(50000);
		yearIndexes = new HashMap<Integer,Integer>(5);
		if(loadFromCache) {
			load();
			markYears();
		}
	}
	
	public void load()	{
		ObjectMapper mapper = new ObjectMapper();
		List<ArchiveItem> list = null;
		File file = new File(dbPath);
		if(!file.exists()) {
			log.warn("cache file "+dbPath+" does not exist");
			return;
		}
		try {
			list = mapper.readValue(
					new File(dbPath), 
					new TypeReference<List<ArchiveItem>>() { });
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
		if(!list.isEmpty()) {
			log.info(String.valueOf(list.size())+" items loaded from "+dbPath);
			this.items.addAll(list);
			lastItemFromCache = this.items.get(items.size()-1);
			log.info("db loaded successfully. db size: "+items.size()+" items");
		}
	}
	
	public ArchiveItem getLast() {
		if(!items.isEmpty()) {
			return this.items.get(items.size()-1);
		}
		return null;
	}
	public ArchiveItem getLastFromCache() { return lastItemFromCache; }
	
	public void save() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.writeValue(new File(dbPath), items);
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
		log.info("db successfully saved to "+dbPath);
	}
	
	public void sort() {
		log.info("db sort started");
		java.util.Collections.sort(items, new Comparator<ArchiveItem>() {
			@Override
			public int compare(ArchiveItem item1, ArchiveItem item2) {				
				return (int)(item1.getDateAsInt() - item2.getDateAsInt());
			}					
		});
		log.info("db sorted successfully");
		this.sorted = true;
	}
	
	public void checkSort() {
		
	}
	
	public void add(ArchiveItem item) {
		this.items.add(item);	
		//checkSort();
		// new incoming item mast be newer than all in database otherwise
		// sort order will be broken and functionality fail
		if(this.max!=null) {
			int i = item.getDateAsInt()-max.getDateAsInt(); 
			if(i>0)
				max = item;
			else if(i<0)
				this.sorted = false;
		} else {
			max = item;
		}
	}
	
	boolean isSorted() {
		return this.sorted;
	}
	
	boolean isEmpty() {
		return this.items.isEmpty();
	}
	
	/*
	public void add(String article, String user, int year, int month, int day) {
		ArchiveItem item = new ArchiveItem(article,user,year,month,day);
		this.items.add(item);
		checkSort();
	}*/
	/**
	 * Must be called before calling the first add() for this year
	 * @param year
	 */
	public void markYearBegin(int year) {
		if(!yearIndexes.keySet().contains(year)) {
			this.yearIndexes.put(year,this.items.size());
		}
	}
	
	public void markYearPos(int year, int pos) {
		if(!yearIndexes.keySet().contains(year)) {
			this.yearIndexes.put(year,pos);
		}
	}
	
	public void markEnd() {
		Set<Integer> years = this.yearIndexes.keySet();
		Iterator<Integer> i = years.iterator();
		int maxYear = 0;
		while(i.hasNext()) {
			int y = i.next();
			if(y>maxYear) maxYear = y;
		}
		this.yearIndexes.put(maxYear+1, items.size());
	}
	
	public void markYears() {
		if(items.isEmpty()) return;
		this.yearIndexes.clear();
		ArchiveItem first = items.get(0);
		markYearPos(first.year,0);
		int year = first.year;
		for(int i=0;i<items.size();i++) {
			ArchiveItem item = items.get(i);
			if(item.year>year) {
				//year=item.year; some random year may break our year ordering
				year=item.year; // year++;
				markYearPos(year,i);				
			} else if(item.year<year) {
				this.sorted = false;
			}
		}
	}
	
	public void markYearsWhichNotMarked() {
		Set<Integer> years = this.yearIndexes.keySet();
		Iterator<Integer> it = years.iterator();
		int maxYear = 0;
		while(it.hasNext()) {
			int y = it.next();
			if(y>maxYear) maxYear = y;
		}
		int year = maxYear;
		int i = this.yearIndexes.get(year);
		for(;i<items.size();i++) {
			ArchiveItem item = items.get(i);
			if(item.year>year) {
				//year=item.year; some random year may break our year ordering
				year=item.year; // year++;
				markYearPos(year,i);				
			} else if(item.year<year) {
				this.sorted = false;
			}
		}
	}
	
	public ListIterator<ArchiveItem> getIterator() {		
		return items.listIterator();
	}
	
	/**
	 * 
	 * @param startYear year must be in the database
	 * @return
	 */
	public ListIterator<ArchiveItem> getIterator(int startYear) {
		ListIterator<ArchiveItem> iterator = null;
		iterator = items.listIterator(yearIndexes.get(startYear));
		return iterator;
	}
	
	/**
	 * 
	 * @param year year must be in the database
	 * @return
	 */
	public ListIterator<ArchiveItem> getYearIterator(int year) {
		return new ArchiveDBYearIterator(year);
	}

	public void removeItemsFromLastYear() {
		if(this.lastItemFromCache == null) return;
		Integer pos = this.yearIndexes.get(lastItemFromCache.year);
		for(int i=items.size()-1;i>=pos;i--)
			items.remove(i);
	}

	public void removeItemsFromLastQuarter() {
		if(this.lastItemFromCache == null) return;
		int ypos = this.yearIndexes.get(lastItemFromCache.year);
		int q = lastItemFromCache.getQuarter();
		int m = (q-1)*3;
		int pos = ypos;
		for(;pos<items.size();pos++) {
			ArchiveItem item = items.get(pos);
			if(item.month==m) break;
		}
		for(int i=items.size()-1;i>=pos;i--)
			items.remove(i);		
	}
	
	public void removeAllItems() {
		items.clear();
	}
}
