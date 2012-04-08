/**
 *  @(#)NirvanaWiki.java 0.02 07/04/2012
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

package org.wikipedia.nirvana;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import org.wikipedia.Wiki;

/**
 * @author KIN 
 * based on Wiki.java version 0.25, revision 46  
 */
public class NirvanaWiki extends Wiki {
	
    private static final long serialVersionUID = -8745212681497644127L;
    
    //private HashMap<Integer,String> namespaceStrings = null;

	private static org.apache.log4j.Logger log = null;	
	
	private boolean dumpMode = false;
	private String dumpFolder = "dump";
	/**
	 * 
	 */
	public NirvanaWiki() {
		log = org.apache.log4j.Logger.getLogger(Wiki.class.getName());
	}

	/**
	 * @param domain
	 */
	public NirvanaWiki(String domain) {
		super(domain);
		log = org.apache.log4j.Logger.getLogger(Wiki.class.getName());
	}

	/**
	 * @param domain
	 * @param scriptPath
	 */
	public NirvanaWiki(String domain, String scriptPath) {
		super(domain, scriptPath);
		log = org.apache.log4j.Logger.getLogger(Wiki.class.getName());
	}
	
	public void setDumpMode(String folder) {
		dumpMode = true;
		dumpFolder = folder;
	}
	
	public void setDumpMode() {
		dumpMode = true;
	}
	
	/**
     *  Logs a successful result.
     *  @param text string the string to log
     *  @param method what we are currently doing
     *  @param level the level to log at
     *  @since 0.06
     */
    protected void log(Level level, String text, String method)
    {
        StringBuilder sb = new StringBuilder(100);
        //sb.append('[');
        //sb.append(domain);
        //sb.append("] ");
        sb.append(method);
        sb.append("() - ");
        sb.append(text);
        sb.append('.');
        if(level==Level.SEVERE) {
        	log.error(sb.toString());
        } else if(level==Level.WARNING) {
        	log.warn(sb.toString());
        } else if(level==Level.CONFIG || level==Level.INFO) {
        	log.info(sb.toString());
        } else if(level==Level.FINE) {
        	log.debug(sb.toString());
        }
        	
     //   logger.logp(level, "Wiki", method + "()", sb.toString());
    }

    /**
     *  Logs a url fetch.
     *  @param url the url we are fetching
     *  @param method what we are currently doing
     *  @since 0.08
     */
    protected void logurl(String url, String method)
    {
        log.debug(method + "() - fetching URL: " + url);
    }
    
	 /**
     *  Change the logging level of this object's Logger object.
     *  @param Level
     *  @since 0.24
     */
    public void setLogLevel(Level level)
    {
    	super.setLogLevel(level);
    	log.setLevel(org.apache.log4j.Level.OFF);
    }

    public static String loadPageList(String category, String language, int depth, int hours) throws IOException, InterruptedException
    {
		log.debug("Downloading data for category " + category);
		String url_path = "/~daniel/WikiSense/CategoryIntersect.php";
        String url_query = 
        		String.format("wikilang=%1$s&wikifam=.wikipedia.org&basecat=%2$s&basedeep=%3$d&mode=rc&hours=%4$d&onlynew=on&go=Сканировать&format=csv&userlang=ru",
                language,
                category,
                depth,
                hours);
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
		return page;
    }
    
    public static String loadPageList(String category, String language, int depth) throws IOException, InterruptedException
    {
        log.debug("Downloading data for " + category);
        String url_path = "/~magnus/catscan_rewrite.php";
        String url_query = 
        		String.format("language=%1$s&depth=%3$d&categories=%2$s&&sortby=title&format=tsv&doit=submit",
                language,
                category,
                depth);
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
		return page;        
    }
 
    
    
    public synchronized void edit(String title, String text, String summary, boolean minor, boolean bot,
            int section) throws IOException, LoginException {
    	if(!this.dumpMode) {
    		super.edit(title, text, summary, minor, bot, section);
    	} else {
    		String fileNew = title+".new.txt";
    		String fileOld = title+".old.txt";
    		String old = null;
    		try {old=this.getPageText(title);} catch (FileNotFoundException e) {old="";}
   			FileTools.dump(old, dumpFolder, fileOld);
    		FileTools.dump(text, dumpFolder, fileNew);
    	}
    }
    
    public void edit(String title, String text, String summary, boolean minor, boolean bot) throws IOException, LoginException
    {
    	this.edit(title, text, summary, minor, bot, -2);
    }

    
    public void prepend(String title, String stuff, boolean minor, boolean bot) throws IOException, LoginException
    {
        StringBuilder text = new StringBuilder(100000);
        text.append(stuff);
        text.append(getPageText(title));
        this.edit(title, text.toString(), "+" + stuff, minor, bot);
    }
    
    public void prepend(String title, String stuff, String comment, boolean minor, boolean bot) throws IOException, LoginException
    {
        StringBuilder text = new StringBuilder(100000);
        text.append(stuff);
        text.append(getPageText(title));
        this.edit(title, text.toString(), comment, minor, bot);
    }
    
    public void prependOrCreate(String title, String stuff, String comment, boolean minor, boolean bot) throws IOException, LoginException
    {
        StringBuilder text = new StringBuilder(100000);
        text.append(stuff);
        try {
        	text.append(getPageText(title));
        } catch (FileNotFoundException e) {
        	log.debug("page "+title+" does not exist -> create");
        }
        this.edit(title, text.toString(), comment, minor, bot);
    }
    
    public void append(String title, String stuff, String comment, boolean minor, boolean bot) throws IOException, LoginException
    {
        StringBuilder text = new StringBuilder(100000);        
        text.append(getPageText(title));
        text.append(stuff);
        this.edit(title, text.toString(), comment, minor, bot);
    }
    
    public void appendOrCreate(String title, String stuff, String comment, boolean minor, boolean bot) throws IOException, LoginException
    {
        StringBuilder text = new StringBuilder(100000);        
        try {
        	text.append(getPageText(title));
        } catch (FileNotFoundException e) {
        	log.debug("page "+title+" does not exist -> create");
        }
        text.append(stuff);
        this.edit(title, text.toString(), comment, minor, bot);
    }
    
    public static boolean isRedirect(String article) {    	
    	Pattern p = Pattern.compile("^\\#(REDIRECT|перенаправление)\\s*\\[\\[.+\\]\\].*$",
    			Pattern.CASE_INSENSITIVE | Pattern.DOTALL /*| Pattern.UNIX_LINES*/);
    	Matcher m = p.matcher(article);    	
    	return m.matches();
    }
}
