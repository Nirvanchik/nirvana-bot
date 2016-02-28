/**
 *  @(#)UserStatBot.java 19/09/2013
 *  Copyright © 2013 - 2014 Dmitry Trofimovich (KIN)
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

package org.wikipedia.nirvana.userstat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.wikipedia.Wiki;
import org.wikipedia.nirvana.FileTools;
import org.wikipedia.nirvana.HTTPTools;
import org.wikipedia.nirvana.NirvanaBasicBot;

/**
 * @author kin
 *
 */
public class UserStatBot extends NirvanaBasicBot {
	private static String TASK_LIST_FILE = "task.txt";
	public static final String delimeter = "\n";
	public static final String INFO = 
			"UserStatBot v1.0 Calculates disambigs created by users at http://ru.wikipedia.org\n" +
			"Copyright (C) 2013 Dmitry Trofimovich (KIN)\n" +		
			"\n";
		
	private List<String> disambigCats = new ArrayList<String>(20);
	
	public UserStatBot() {
		super();
	}
	public UserStatBot(int flags) {
		super(flags);
	}
	
	public static NirvanaBasicBot createBot() {
		return new UserStatBot(FLAG_CONSOLE_LOG);
	}
	
	public void showInfo() {
		System.out.print(INFO);
	}
	
	@Override
	protected boolean loadCustomProperties(Map<String,String> launch_params) {
		TASK_LIST_FILE = properties.getProperty("task-list-file",TASK_LIST_FILE);
		log.info("task list file: "+TASK_LIST_FILE);
		//COMMENT = properties.getProperty("update-comment", COMMENT);
		return true;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NirvanaBasicBot bot = createBot();
		bot.run(args);
	}
	
	protected void go() {
		//log.info("Bot");
		
		String tasks[] = FileTools.readFileToArray(TASK_LIST_FILE, true);
		
		if(tasks==null) {
			return;
		}
		
		Map<String,Integer> data = new LinkedHashMap<String,Integer>();
		
		//log.setLevel(Level.INFO);
		
		LogManager.getRootLogger().setLevel(Level.INFO);
		wiki.setLogLevel(Level.WARN);
		
		int c1000 = 0;
		
		try{
			log.info("loading disambigs");
			String url_path = "/~daniel/WikiSense/CategoryIntersect.php";
	        String url_query = "wikilang=ru&wikifam=.wikipedia.org&basecat=Многозначные_термины&basedeep=6&mode=cl&go=Сканировать&format=csv&userlang=ru";
	        URI uri=null;
			try {
				uri = new URI("http","toolserver.org",url_path,url_query,null);
			} catch (URISyntaxException e) {			
				log.error(e.toString());
			}
			String page = null;
			try {
				page = HTTPTools.fetch(uri.toASCIIString(),true);
			} catch (java.net.SocketTimeoutException e) {
				log.warn(e.toString()+", retry again ...");
				Thread.sleep(10000);
				page = HTTPTools.fetch(uri.toASCIIString(),true);
			}
			StringReader sr = new StringReader(page);
			BufferedReader b = new BufferedReader(sr);
			String line;
			while ((line = b.readLine()) != null) {
				String[] groups = line.split("\t");
				String cat = groups[1];
				log.info("found disambig category: "+cat);
				disambigCats.add(cat);
			}
			
			
		for(String user:tasks) {
			if(user.isEmpty()) continue;
			user = user.trim();
			log.info("analyze user: "+user);
			List<String> contribs = getUserContribs(user);
			log.info("user articles: "+String.valueOf(contribs.size()));
			
			log.info("creating database");
			UserDisambigDatabase db = new UserDisambigDatabase(user, true, UserDisambigDatabase.DEFAULT_CACHE_FOLDER);
			
			int counter = 0;
			c1000 = 0;
			int errors = 0;
			for(String article:contribs) {
				log.debug("check article: "+article);
				UserArticleInfo infoFromDb = db.get(article);
				boolean disambig;
				if(infoFromDb == null) {
    				try {    					
    					disambig = isDisambig(article);
        				if(disambig) {
        					log.debug("disambig found: "+article);
        					counter++;    					
        				}
        				int namespace = wiki.namespace(article);
        				Wiki.Revision last = wiki.getTopRevision(article);        				
        				db.add(article, disambig, namespace, last.getTimestamp().getTimeInMillis());
    				} catch (IOException e) {
    					log.error(e + " (error ignored)");
    					errors++;
    					if(errors>=5) {
    						throw e;
    					}
    				}
				} else {					
					if(infoFromDb.isDisambig) {
    					log.debug("disambig found in db: "+article);
    					counter++;    					
    				} 
				}
				c1000++;
				if(c1000%1000==0) {
					log.info(String.valueOf(c1000)+" articles checked");
				}
			}
			db.save();
			log.info("user disambigs: "+String.valueOf(counter));
			data.put(user, counter);
		}
		
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String,Integer> pair:data.entrySet()) {
			sb.append(pair.getKey());
			sb.append(":");
			sb.append(String.valueOf(pair.getValue()));
			sb.append("\r\n");
		}		
		FileTools.dump(sb.toString(), "dump", "out_user_disambigs.txt");
		} catch (IOException e) {			
			log.error(e, e);
		} catch (InterruptedException e) {
			log.error(e, e);
		}
		
	}
	
	private List<String> getUserContribsV2(String user) throws IOException, InterruptedException {
		ArrayList<String> contribs = new ArrayList<String>(5000);
		Wiki.Revision [] revs = wiki.getUser(user).contribs(0);
		return contribs;
	}
	private List<String> getUserContribs(String user) throws IOException, InterruptedException {
		ArrayList<String> contribs = new ArrayList<String>(5000);
		String url_query = String.format("name=%1$s&lang=ru&wiki=wikipedia&namespace=0&redirects=noredirects&getall=1",user);
		URI uri=null;
		try {
			uri = new URI("http","toolserver.org","/~tparis/pages/index.php",url_query,null);
		} catch (URISyntaxException e) {			
			log.error(e.toString());
		}
		String page = null;
		try {
			page = HTTPTools.fetch(uri.toASCIIString(),true);
		} catch (java.net.SocketTimeoutException e) {
			log.warn(e.toString()+", retry again ...");
			
			Thread.sleep(10000);
			page = HTTPTools.fetch(uri.toASCIIString(),true);
		}
		
		String key = "<li><a href=\"http://ru.wikipedia.org/wiki/";
		int index = page.indexOf(key);
		while(index>=0) {
			int start = index;
			index = page.indexOf("\">",index);
			int end = index;
			String article = page.substring(start+key.length(), end);
			contribs.add(article);
			index = page.indexOf(key, index);			
		}
				//https://toolserver.org/~tparis/pages/index.php?name=%D0%9D%D0%B8%D1%80%D0%B2%D0%B0%D0%BD%D1%8C%D1%87%D0%B8%D0%BA&lang=ru&wiki=wikipedia&namespace=0&redirects=noredirects&getall=1
		return contribs;
	}
	
	private boolean isDisambig(String article) throws IOException {
		String[] cats;
		try {
			cats = wiki.getCategories(article);
		} catch(IOException e) {
			log.warn("getCategories() failed " + e.toString() +", retrying again ...");
			cats = wiki.getCategories(article);
			//HTTPTools.fetch("");
		}
		String prefix = "Категория:";
		for(String cat:cats) {			
			String s = cat; 			
			if(s.startsWith(prefix)) {
				s = s.substring(prefix.length());
			}
			s = s.replace(' ', '_');
			log.debug("check cat: "+s);
			if(disambigCats.contains(s)) {
				return true;
			}
		}
		return false;
	}

}
