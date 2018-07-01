/**
 *  @(#)Rating.java 20/10/2012
 *  Copyright © 2012 - 2014 Dmitry Trofimovich (KIN)
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
 * This file is encoded with UTF-8.
 * */
package org.wikipedia.nirvana.statistics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import javax.management.BadAttributeValueExpException;

import org.wikipedia.nirvana.FileTools;
import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.NumberTools;
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
	protected static final int RESERVE_PERCENT_FOR_RENAMED_USERS = 30;
	int year;	
	int size = DEFAULT_SIZE;
	boolean filterBySize = true;
	public int minSize = 0;

	/**
	 * @param type
	 * @throws FileNotFoundException
	 * @throws BadAttributeValueExpException
	 */
    public Rating(NirvanaWiki wiki, String cacheDir, String type) throws FileNotFoundException,
			BadAttributeValueExpException {
        super(wiki, cacheDir, type);
		year = 0;
	}
	
	/**
	 * @param type
	 * @throws FileNotFoundException
	 * @throws BadAttributeValueExpException
	 */
    public Rating(NirvanaWiki wiki, String cacheDir, String type, int year)
            throws FileNotFoundException, BadAttributeValueExpException {
        super(wiki, cacheDir, type);
		this.year = year;		
	}
	
	public void setOptions(Map<String,String> options) {
		super.setOptions(options);
        String key = "размер";
		if(options.containsKey(key) && !options.get(key).isEmpty()) {
			try {
				this.size = Integer.parseInt(options.get(key));
			} catch(NumberFormatException e) {
                log.warn(String.format(NirvanaBot.ERROR_PARSE_INTEGER_FORMAT_STRING_EN, key,
                        options.get(key)));
			}					
		}
        key = "статьи от";
		if(options.containsKey(key) && !options.get(key).isEmpty()) {
			this.filterBySize = true;
			try {
				this.minSize = NumberTools.parseFileSize(options.get(key));
			} catch(NumberFormatException e) {
                log.warn(String.format(NirvanaBot.ERROR_PARSE_INTEGER_FORMAT_STRING_EN, key,
                        options.get(key)));
			}
		}
	}
	public void put(ArchiveDatabase2 db) throws IllegalStateException {
		Statistics reporter = null;
		if (!this.filterBySize) {
			if(year==0) reporter = StatisticsFabric.getReporterWithUserData(this);
			else 		reporter = StatisticsFabric.getReporterWithUserData(this,year);
		}
		if (reporter != null) {
			this.totalUserStat = reporter.getUserStat();
		} else {			
			putFromDb(db);
		}
		analyze();
	}

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
			if (!(this.filterBySize && item.size<this.minSize))
				this.totalUserStat.put(item.user, 1);		
		}
		while(it.hasNext()) {
			item = it.next();
			if(this.year!=0 && item.year!=this.year) 
				throw new IllegalStateException("year processing: "+this.year+", item's year: "+item.year+", item: "+item.article);
			if (this.filterBySize && item.size<this.minSize)
				continue;
			Integer n = totalUserStat.get(item.user);
			if (n==null) this.totalUserStat.put(item.user, 1);
			else this.totalUserStat.put(item.user, n+1);			
		}
	}
	
//	private void incrementUserCnt()
	
	
	protected void additionalProcessing() {
		
	}

	protected void analyze() {
        // Заполняем таблицу items (список строк отчёта)
		for (Map.Entry<String,Integer> entry:totalUserStat.entrySet()) {
			StatItem stat = new StatItem();
			stat.user = entry.getKey();
			stat.userArticles = entry.getValue();
			this.items.add(stat);
		}
        // Сортируем таблицу в нужном порядке (в порядке убывания количества статей)
		Collections.sort(this.items, 
			new Comparator<StatItem>(){
				@Override
				public int compare(StatItem item1, StatItem item2) {				
					return (int)(item2.userArticles - item1.userArticles);
				}					
			});

        // Подрезаем "лишние" строки в таблице, но оставляя резерв на случай дубликатов
		int maxsize = this.size + this.size*RESERVE_PERCENT_FOR_RENAMED_USERS/100;

		//this.totalUserStat.clear();
		if(items.size()>maxsize) {			
			//this.items = new ArrayList<StatItem>(this.items.subList(0, size));
			this.items.subList(maxsize, this.items.size()).clear();					
		}

        // Дополнительный процессинг
		additionalProcessing();

        // Удаляем дубликаты
		boolean duplicates = removeDuplicates();

        // Опять сортируем, так как мёрж дубликатов мог сломать сортировку
		if (duplicates) {
			Collections.sort(this.items, 
				new Comparator<StatItem>(){
					@Override
					public int compare(StatItem item1, StatItem item2) {				
						return (int)(item2.userArticles - item1.userArticles);
					}					
				});
		}

        // Обрезаем, на этот раз до строго указанного числа элементов
		if(items.size()>this.size) {			
			//this.items = new ArrayList<StatItem>(this.items.subList(0, size));
			this.items.subList(this.size, this.items.size()).clear();					
		}

        // Назначаем позицию участников в рейтинге
		int num = 1;
		//int pos = 1;
		int a = 0;
		if(items.size()>0)
			a = items.get(0).userArticles;
		for(int i = 0;i<items.size();i++) {
			StatItem stat = items.get(i);
			if(stat.userArticles<a) {
				a = stat.userArticles;
				//num++;
                num = i + 1;
                // Чтобы все нижележащие в рейтинге не сдвигались и не поднимались по рейтингу
                // из-за вышестоящих, и чтобы номер последнего отражал количество участников в
                // рейтинге.
			} 
			stat.number = num;
		}

        // Подсчитываем прогресс (рост/стабильность/падение)
		if (this.itemTemplate.contains("%(прогресс)")) calcProgress();
	}


	protected boolean removeDuplicates() {
		boolean duplicates = false;
		int n = items.size();
		for(int i=0;i<n;i++) {
			StatItem item = items.get(i);
			String redir = null;
			try {
				redir = wiki.resolveRedirect("User:"+item.user);
			} catch (FileNotFoundException e) {
				// ignore error
			} catch (IOException e) {
				// TODO how to handle this without declaring excepion?
				//e.printStackTrace();
			}
			if(redir!=null) {
				redir = redir.substring(redir.indexOf(":")+1);
				for(int j=0;j<n;j++) {
					if(j==i) continue;	// fix bug when redirection to User_talk removed user from rating
					StatItem item2 = items.get(j);
					if(item2.user.equals(redir)) {
						merge(i,j);
						items.remove(i);
						n--;
						i--;
						duplicates = true;
						break;
					}
					
				}
			}
			
		}
		return duplicates;
	}
	
	/**
     * Смерживает двух юзеров, указанных по индексам
     * Используется при удалении дубликатов (дубликаты возникают, когда юзер меняет имя)
     * @param srcIndex индекс юзера-дубликата (откуда мёржить)
     * @param destIndex индекс оригинального юзера (куда мёржить)
	 */
	protected void merge(int srcIndex, int destIndex) {
		StatItem src = items.get(srcIndex);
		StatItem dest = items.get(destIndex);
		dest.userArticles += src.userArticles;
	}

	private void calcProgress() {
        Path startingDir = Paths.get(cacheDir);
        String pattern = FileTools.normalizeFileName(portal)+"."+this.type+".????-??-??.js";

        Finder finder = new Finder(pattern);
        //Files.w
        try {
			Files.walkFileTree(startingDir,EnumSet.noneOf(FileVisitOption.class), 1, finder);
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
        file = cacheDir + "\\" + String.format("%1$s.%2$s.%3$tF.js",
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
