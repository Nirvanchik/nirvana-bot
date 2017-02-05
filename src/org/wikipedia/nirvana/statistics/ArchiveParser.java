/**
 *  @(#)ArchiveParser.java
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

package org.wikipedia.nirvana.statistics;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.DateTools;
import org.wikipedia.nirvana.FileTools;
import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.archive.ArchiveSettings;
import org.wikipedia.nirvana.archive.ArchiveSettings.Period;
import org.wikipedia.nirvana.nirvanabot.NewPages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Get new pages archives from wiki, parse it, put data from archive to database
 *
 */
public class ArchiveParser {
	private boolean DEBUG = false;
    protected final Logger log;

	private ArchiveSettings archiveSettings;
	private ArchiveDatabase2 db;
	NirvanaWiki wiki;
    private final String cacheDir;

	/**
	 * Constructor
	 */
    public ArchiveParser(ArchiveSettings archiveSettings, ArchiveDatabase2 db, NirvanaWiki wiki,
            String cacheDir) {
        log = LogManager.getLogger(this.getClass().getName());
		this.archiveSettings = archiveSettings;
		this.db = db;
		this.wiki = wiki;
        this.cacheDir = cacheDir;
	}
	
	private interface PurgeDatabase{
		public void purge(ArchiveDatabase2 db);
	};
	
	/**
	 * Parses archives and puts data to database
	 * @param cache if true, it will save cache after any big change in database
	 * @throws IOException
	 */
	public void getData(boolean cache) throws IOException {
        DateTools dateTools = DateTools.getInstance();
		// Архив состоит из одной единственной страницы
		if(archiveSettings.isSingle()) {
			PurgeDatabase purge = null;
			if(!db.isEmpty()) {
				purge = new PurgeDatabase() {
					@Override
					public void purge(ArchiveDatabase2 db) {
						db.removeAllItems();
					}
				};
			}
			getDataFromArchive(archiveSettings.archive,db,purge,archiveSettings.addToTop,true/*,true*/);
			if(cache) db.save();
			db.markEnd();
		} else { 
			// Архив состоит из нескольких страниц
			boolean markAll = db.isEmpty();
			int startYear = archiveSettings.startYear;
			Calendar c = Calendar.getInstance();
			int endYear = c.get(Calendar.YEAR);
			int curQ = c.get(Calendar.MONTH)/3+1;
			ArchiveItem last = db.getLastFromCache();
			//boolean checkLast = false;
			// Если в кэше уже что-то есть, то не парсим все архивы.
			// Те годы, которые есть в кэше, мы пропускаем, стартуем парсинг архивов, 
			// начиная с последнего года, найденного в кэше.
			if (last!=null && last.year>startYear) {
				startYear = last.year; 
				log.info("db already contains data. scanning data from " + String.valueOf(startYear)+" year");
			}
			// Диапазон лет, захватываемых одной страницей архива (обычно 1)
			int year_offset = 1;
			for(int year=startYear;year<=endYear;year+=year_offset) {				
				String archiveThisYear = null;
				
				if (archiveSettings.firstArchive != null && year<archiveSettings.startYear2) {
					archiveThisYear = archiveSettings.firstArchive;
					year_offset = archiveSettings.startYear2-archiveSettings.startYear;
				} else {
					archiveThisYear = archiveSettings.archive.replace(
							ArchiveSettings.Period.YEAR.template(), String.valueOf(year));
					year_offset = 1;
				}
				// Если период по годам, или мы парсим первую групповую страницу архива,
				// т.е. разбивать на кварталы не нужно
				if(archiveSettings.archivePeriod==Period.YEAR || 
						archiveThisYear==archiveSettings.firstArchive || 
						archiveThisYear.equals(archiveSettings.firstArchive)) {
					PurgeDatabase purge = null;
					if(last!=null && last.year==year) {
						// db.removeItemsFromLastYear(); disabled because of new method
						//checkLast = true;
						purge = new PurgeDatabase(){
							@Override
							public void purge(ArchiveDatabase2 db) {
								db.removeItemsFromLastYear();
								//db.markYearBegin(year);
							}
						};
					} else {
						//checkLast = false;
						//db.markYearBegin(year);
					}		
					//db.markYearBegin(year);
					getDataFromArchive(archiveThisYear,db,purge,archiveSettings.addToTop,false/*year_offset>1*//*,checkLast*/);
					if(cache) db.save();
				} else {
					// Нужно разбить на кварталы и пройтись по кварталам
					int qStart = 1;
					int qEnd = 4;
					// skip archives of old quarters, which we have loaded to db already from cache
					if(last!=null && year==last.year) {
						int lq = last.getQuarter();
						if(lq>qStart) {
							qStart = lq;
						}
					}
					// skip future quarters
					if(year==endYear) {
						qEnd = curQ; 
					}
					if(DEBUG) {qStart = 2;qEnd=2;endYear=2010;}
					//db.markYearBegin(year);
					for(int q=qStart;q<=qEnd;q++) {
						String archive = archiveThisYear.replace(
								Period.QUARTER.template(), String.valueOf(q));
						archive = archive.replace(
                                Period.SEASON.template(), dateTools.seasonString(q - 1));
						PurgeDatabase purge = null;
						if(last!=null && year==last.year && q==last.getQuarter()) {
							//checkLast = true;
							//db.removeItemsFromLastQuarter();
							purge = new PurgeDatabase(){
								@Override
								public void purge(ArchiveDatabase2 db) {
									db.removeItemsFromLastQuarter();
								}
							};
						} else {
							//checkLast = false;
						}
						getDataFromArchive(archive,db,purge,archiveSettings.addToTop,false/*,checkLast*/);
						if(cache) db.save();
					}
				}
			}
			if(markAll) {
				db.markYears();
			} else {
				db.markYearsWhichNotMarked();
			}
		}		
	}
	
	void getDataFromArchive(String archive, 
			ArchiveDatabase2 db, 
			PurgeDatabase purgeFunc, 
			boolean addToTop, 
			boolean markYear/*, boolean checkLast*/) throws IOException {
		log.info("scanning archive [["+archive+"]]");
		//int startFrom = 0;
		String lines[] = null;
		String text = null;
		try {
			text = wiki.getPageText(archive).trim();
		} catch (FileNotFoundException e) {
			log.warn("Archive "+archive + " not found (skiped)");
			return;
		}
		lines = text.split("\n"); 
		
		if(lines.length==0) {
			log.warn("archive "+archive+" is empty");
			return;
		}
		log.info(String.valueOf(lines.length)+" lines found");
		
		int i;
		int year = 0;
		if(addToTop) i = lines.length-1;
		else i = 0;
		int end;
		if(addToTop) end = -1;
		else end = lines.length;
		
		// Почистить остаток в кэше за текущий год/квартал, или весь кэш
		// Заодно, переместить индекс строки в архиве, с которой начинать парсинг
		// т.е. пропустить то, что уже есть в кэше.
		if(purgeFunc!=null) {
			String dump = null;
			try {
                dump = FileTools.readWikiFile(cacheDir, archive + ".txt");
			} catch (FileNotFoundException e) {
				// ignore
				log.info(e);
				log.info("Failed to read "+archive+".txt"+" (ignored)");
			} catch (UnsupportedEncodingException e) {
				log.error(e);
				log.info("Failed to read "+archive+".txt"+" (ignored)");
			}			
			
			if(dump!=null) {
				log.info("applying dump "+archive+".txt");
				dump = dump.trim();				
				if(addToTop && text.endsWith(dump)) {
					String lines2[] = dump.split("\n");
					i -= lines2.length;
					log.info("Skipping last "+String.valueOf(lines2.length)+" lines");
				} else if(!addToTop && text.startsWith(dump)) {
					String lines2[] = dump.split("\n");
					i += lines2.length;					
					log.info("Skipping first "+String.valueOf(lines2.length)+" lines");
				} else {
					purgeFunc.purge(db);
				}
			}
		}	
		// Скинуть сохраненный текст архива в файл
		// Тоже своего рода уровень кэширования
		try {
            FileTools.dump(text, cacheDir, archive + ".txt");
		} catch (UnsupportedEncodingException e) {
			log.error(e);
		}
		text = null;
		
		// Парсить строки из архива и закидывать в базу
		for(;i!=end;) {			
			String line = lines[i];			
			ArchiveItem item = parseLine(line, db.withSize());
			if(item==null) {
				log.trace("parse line: "+line+ "  NO ARTICLE");				
			} else {
				log.trace("parse line: "+line+ "  FOUND -> "+item.article);
				if(markYear && item.year!=year) {
					year = item.year;
					db.markYearBegin(year);
				}
//				if(foundLast)
					db.add(item);
//				else {
//					if(item.equals(last)) 
//						foundLast = true; 
//					else if(item.getDateAsInt()>last.getDateAsInt()) {
//						foundLast = true;
//						db.add(item);
//					} if some article was deleted and recreated, it will breake our db and a lot of duplicates will occur
//				}
			}
			if(addToTop)i--; else i++;
		}
	}
	
	ArchiveItem parseLine(String line, boolean getSize) throws IOException {
		ArchiveItem item = null;
		Pattern p = Pattern.compile(NewPages.PATTERN_NEW_PAGES_LINE_WIKIREF_ARTICLE);
		Matcher m = p.matcher(line);
		while(m.find()) {
			String article = m.group("article");
			if(article.contains("Категория:Википедия:Списки новых статей по темам")) 				
				continue;	
			if(	NewPages.userNamespace(article) || NewPages.categoryNamespace(article))
				continue;
			item = getArchiveItemFromTitle(article, getSize);
			if (item != null )
				return item;
		}
		p = Pattern.compile(NewPages.PATTERN_NEW_PAGES_LINE_TEMPLATE_ITEM);
		m = p.matcher(line);
		if(m.find()) {
			String templateString = m.group("template");			
			String []items = templateString.split("\\|");
			if(items[0].trim().equals("u")) return null; // ignore "u" template
			Calendar c = null;			
			String article = null;
			String user = null;
			for(int i=1;i<items.length;i++) {
				String s = items[i];
				if(c==null) {
					c = DateTools.parseDate(s);
					if(c!=null) continue;
				}
				if(article==null && !NewPages.userNamespace(s)) {
					article = s;
					continue;
				} else if(user==null) {
					user = s;
				}				
			}
			
			// We have all data for archive item, and we don't need size
			if (c!=null && article!=null && user!=null && !getSize) {
				item = new ArchiveItem(article, user, c);
				return item;
			} 
			item = getArchiveItemFromTitle(article, getSize);
		}		
		return item;
	}
	
	ArchiveItem getArchiveItemFromTitle(String title, boolean getSize) throws IOException {
		if (title == null) return null;
		ArchiveItem item = null;
		Revision r = null;
		r = wiki.getFirstRevision(title,true);
		if (r != null) {
			/** sometimes shit happens, for instance check this page history 
			 *  http://ru.wikipedia.org/wiki/%D0%9B%D1%91%D0%B2%D0%B0_%D0%91%D0%B8-2
			 *  where 2 first revisions are blank 
			 *  (no user, no revision id, not possible to diff or something)
			 */
			if (r.getUser() == null) { 
				r = tryFindAnotherRev(title, r);				
			} 		
		}
		if (r != null) {
			if (getSize) {
    			Revision last = wiki.getTopRevisionWithNewTitle(title, true);
    			item = new ArchiveItem(r, last.getSize());
			} else {
				item = new ArchiveItem(r);
			}			
		}
		return item;
	}

	Revision tryFindAnotherRev(String article, Revision r) throws IOException {
		Revision []revs = null;
		//HashMap<String,Object> info = null;
		String redirect = null;
			//r = m_wiki.getTopRevisionWithNewTitle(article,true);
			//info = m_wiki.getPageInfo(article);
		redirect = wiki.resolveRedirect(article);
		String realPage = (redirect==null)?article:redirect;
		Calendar c1 = r.getTimestamp();
		Calendar c2 = Calendar.getInstance();
		c2.set(c1.get(Calendar.YEAR)+2, 0, 1); // we give 2 years chance to find next rev
		r = null;
		revs = wiki.getPageHistory(realPage, c2, c1);
		if(revs!=null) {
			for(int i=revs.length-1;i>=0;i--) {
				Revision rev = revs[i];
				if(rev.getUser()!=null) {
					return rev;
				}
			}
		}
		return null;
	}
	
}
