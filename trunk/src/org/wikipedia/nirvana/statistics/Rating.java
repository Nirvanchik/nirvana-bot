/**
 *  @(#)Rating.java 20/10/2012
 *  Copyright � 2012 Dmitry Trofimovich (KIN)
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import javax.management.BadAttributeValueExpException;

import org.wikipedia.nirvana.FileTools;
import org.wikipedia.nirvana.nirvanabot.NirvanaBot;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author kin
 *
 */
public class Rating extends Statistics {
	private static final int DEFAULT_SIZE = 20;
	int year;	
	int size = DEFAULT_SIZE;

	/**
	 * @param type
	 * @throws FileNotFoundException
	 * @throws BadAttributeValueExpException
	 */
	public Rating(String type) throws FileNotFoundException,
			BadAttributeValueExpException {
		super(type);
		year = 0;
	}
	
	/**
	 * @param type
	 * @throws FileNotFoundException
	 * @throws BadAttributeValueExpException
	 */
	public Rating(String type, int year) throws FileNotFoundException,
			BadAttributeValueExpException {
		super(type);
		this.year = year;		
	}
	
	/*
	public String toString() {
		
	}*/
	
	public void setOptions(Map<String,String> options) {
		String key = "������";
		if(options.containsKey(key) && !options.get(key).isEmpty()) {
			try {
				this.size = Integer.parseInt(options.get(key));
			} catch(NumberFormatException e) {
				log.warn(String.format(NirvanaBot.ERROR_PARSE_INTEGER_FORMAT_STRING, key, options.get(key)));
			}					
		}
	}
	public void put(ArchiveDatabase2 db) throws IllegalStateException {
		Statistics reporter = null;
		if(year==0) reporter = StatisticsFabric.getReporterWithUserData(this);
		else 		reporter = StatisticsFabric.getReporterWithUserData(this,year);		
		if(reporter==null) {
			putFromDb(db);
		} else {
			this.totalUserStat = reporter.getUserStat();
		}
		analyze();
	}
//	public void putFromReporter() {
//		Map<String,Integer>
//	}
	public void putFromDb(ArchiveDatabase2 db) throws IllegalStateException {
		totalUserStat.clear();
		ListIterator<ArchiveItem> it = null;
		if(this.year==0) it = db.getIterator();
		else it = db.getYearIterator(year);
		ArchiveItem item = null;
		if(it.hasNext()) {
			item = it.next();
			if(this.year!=0 && item.year!=this.year) 
				throw new IllegalStateException("year processing: "+this.year+", item's year: "+item.year+", item: "+item.article);
			this.totalUserStat.put(item.user, 1);
		}
		while(it.hasNext()) {
			item = it.next();
			if(this.year!=0 && item.year!=this.year) 
				throw new IllegalStateException("year processing: "+this.year+", item's year: "+item.year+", item: "+item.article);
			Integer n = totalUserStat.get(item.user);
			if(n==null) totalUserStat.put(item.user, 1);
			else totalUserStat.put(item.user, n+1);			
		}
	}
	
	protected void analyze() {
		for(Map.Entry<String,Integer> entry:totalUserStat.entrySet()) {
			StatItem stat = new StatItem();
			stat.user = entry.getKey();
			stat.userArticles = entry.getValue();
			this.items.add(stat);
		}
		Collections.sort(this.items, 
			new Comparator<StatItem>(){
				@Override
				public int compare(StatItem item1, StatItem item2) {				
					return (int)(item2.userArticles - item1.userArticles);
				}					
			});
				
		//this.totalUserStat.clear();
		if(items.size()>this.size) {			
			//this.items = new ArrayList<StatItem>(this.items.subList(0, size));
			this.items.subList(size, this.items.size()).clear();					
		}	
		int num = 1;
		int a = 0;
		if(items.size()>0)
			a = items.get(0).userArticles;
		for(int i = 0;i<items.size();i++) {
			StatItem stat = items.get(i);
			if(stat.userArticles<a) {
				a = stat.userArticles;
				num++;
			}
			stat.number = num;
		}
		
		if(this.itemTemplate.contains("%(��������)")) calcProgress();
	}

	private void calcProgress() {
		Path startingDir = Paths.get(Statistics.cacheFolder);
        String pattern = FileTools.normalizeFileName(portal)+"."+this.type+".????-??-??.js";

        Finder finder = new Finder(pattern);
        //Files.w
        try {
			Files.walkFileTree(startingDir, finder);
		} catch (IOException e) {
			log.error(e.toString());
			e.printStackTrace();
		}
        String file = finder.getNewestFile();
        Map<String,Integer> data = new HashMap<String,Integer>(30);
        
        if(file!=null) {
        	
        	ObjectMapper mapper = new ObjectMapper();
    		//List<ArchiveItem> list = null;
//    		File file = new File(prevResultFile);
//    		if(!file.exists()) {
//    			log.warn("file "+dbPath+" does not exist");
//    			return;
//    		}
    		try {
    			data = mapper.readValue(
    					new File(startingDir+"\\"+file), 
    					new TypeReference<Map<String,Integer>>() { });
    		} catch (JsonParseException e) {
    			log.error(e);
    		} catch (JsonMappingException e) {
    			log.error(e);
    		} catch (IOException e) {
    			log.error(e);
    		}
    		if(data!=null) {
    			for(StatItem item:this.items) {
    				Integer n = data.get(item.user);
    				if(n!=null) {
    					item.progress = -(item.number - n); /// smaller value means progress
    				}
    			}
    		}
        }
        
        data.clear();
        for(StatItem item:this.items) {
			data.put(item.user,item.number);
		}
        file = Statistics.cacheFolder+"\\"+String.format("%1$s.%2$s.%3$tF.js",
        		FileTools.normalizeFileName(Statistics.portal),
        		type,
        		Calendar.getInstance());
        ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.writeValue(new File(file), data);
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
		
	}
	

	    public static class Finder
	        extends SimpleFileVisitor<Path> {

	        private final PathMatcher matcher;
	        ArrayList<String> items;

	        Finder(String pattern) {
	            matcher =
	                FileSystems.getDefault()
	                    .getPathMatcher("glob:" + pattern);
	            items = new ArrayList<String>(30);
	        }

	        // Compares the glob pattern against
	        // the file or directory name.
	        void find(Path file) {
	            Path name = file.getFileName();
	            if (name != null && matcher.matches(name)) {
	            	items.add(name.toString());
	                //System.out.println(file);
	            }
	        }

	        // Prints the total number of
	        // matches to standard out.
	        String getNewestFile() {	        	
	        	if(items.size()>0) {
	        		java.util.Collections.sort(items);
	        		return items.get(items.size()-1);
	        	}
	        	return null;
	        }

	        // Invoke the pattern matching
	        // method on each file.
	        @Override
	        public FileVisitResult
	            visitFile(Path file,
	                BasicFileAttributes attrs) {
	            find(file);
	            return FileVisitResult.CONTINUE;
	        }

	        // Invoke the pattern matching
	        // method on each directory.
	        @Override
	        public FileVisitResult
	            preVisitDirectory(Path dir,
	                BasicFileAttributes attrs) {
	            //find(dir); do not visit directory
	            return FileVisitResult.CONTINUE;
	        }

	        @Override
	        public FileVisitResult
	            visitFileFailed(Path file,
	                IOException exc) {
	            System.err.println(exc);
	            return FileVisitResult.CONTINUE;
	        }
	    }

	
	

}
