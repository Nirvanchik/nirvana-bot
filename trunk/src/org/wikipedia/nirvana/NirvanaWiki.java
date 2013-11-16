/**
 *  @(#)NirvanaWiki.java 0.06 16/11/2013
 *  Copyright © 2011 - 2013 Dmitry Trofimovich (KIN)
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.security.auth.login.LoginException;

import org.wikipedia.Wiki;

/**
 * @author KIN 
 * based on Wiki.java version 0.28, revision 166  
 */
public class NirvanaWiki extends Wiki {
	
    private static final long serialVersionUID = -8745212681497644127L;

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NirvanaWiki.class.getName());	
	
	private boolean dumpMode = false;
	private String dumpFolder = "dump";
	private boolean logDomain = false;
	/**
	 * 
	 */
	public NirvanaWiki() {
	}

	/**
	 * @param domain
	 */
	public NirvanaWiki(String domain) {
		super(domain);
	}

	/**
	 * @param domain
	 * @param scriptPath
	 */
	public NirvanaWiki(String domain, String scriptPath) {
		super(domain, scriptPath);
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
    protected void log(Level level, String method, String text)
    {
        StringBuilder sb = new StringBuilder(100);
        if(logDomain) {
        	sb.append('[');
        	sb.append(domain);
            sb.append("] ");
        }
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
        } else {
        	log.trace(sb.toString());
        }
    }
    
	/**
     *  Logs a successful result.
     *  @param text string the string to log
     *  @param method what we are currently doing
     *  @param level the level to log at
     *  @since 0.06
     */
    protected void log(Level level, String method, String text, Exception e)
    {
        StringBuilder sb = new StringBuilder(100);
        if(logDomain) {
        	sb.append('[');
        	sb.append(domain);
            sb.append("] ");
        }
        sb.append(method);
        sb.append("() - ");
        sb.append(text);
        sb.append('.');
        if(level==Level.SEVERE) {
        	log.error(sb.toString(), e);
        } else if(level==Level.WARNING) {
        	log.warn(sb.toString(), e);
        } else if(level==Level.CONFIG || level==Level.INFO) {
        	log.info(sb.toString(), e);
        } else if(level==Level.FINE) {
        	log.debug(sb.toString(), e);
        } else {
        	log.trace(sb.toString(), e);
        }
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
    public void setLogLevel(org.apache.log4j.Level level)
    {
    	//logger.setLevel(level);
    	//log.setLevel(org.apache.log4j.Level.OFF);
    	log.setLevel(level);
    }
    
    public synchronized void edit(String title, String text, String summary, boolean minor, boolean bot,
            int section) throws IOException, LoginException {
    	if(!this.dumpMode) {
    		super.edit(title, text, summary, minor, bot, section, null);    		
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
    
    public boolean editIfChanged(String title, String text, String summary, boolean minor) throws IOException, LoginException
    {
    	String old = null;
    	try {
    		old = this.getPageText(title);
    	} catch(FileNotFoundException e) {
    		// ignore. all job is done not here :)
    	}
    	if(old==null || old.length()!=text.length() || !old.equals(text)) { // to compare lengths is faster than comparing 5k chars
    		edit(title, text, summary, minor, true, -2);
    		return true;
    	} else {
    		log.debug("skip updating "+title+ " (no changes)");
    	}
    	return false;
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
    	log.debug("prependOrCreate -> "+title +" appended text size = "+stuff.length());
    	if(stuff.isEmpty()) return;
        StringBuilder text = new StringBuilder(100000);
        text.append(stuff);
        try {
        	log.debug("prependOrCreate -> getting old text");
        	text.append(getPageText(title));
        	log.debug("prependOrCreate -> new text size = "+text.length());
        } catch (FileNotFoundException e) {
        	log.debug("page "+title+" does not exist -> create");
        }
        this.edit(title, text.toString(), comment, minor, bot);
        log.debug("prependOrCreate -> EXIT(OK)");
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
    	if(stuff.isEmpty()) return;
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
    
    /* this is implementation from http://en.wikipedia.org/wiki/Template:Bots#Java
     * Does not work for {{bots|allow=OtherBot}} case
    public static boolean allowBots(String text, String user)
    {
    	return !text.matches("(?si).*\\{\\{(nobots|bots\\|(allow=none|deny=(.*?" + user + ".*?|all)|optout=all))\\}\\}.*");
    }*/
    
	public static boolean allowBots(String text, String user)
    {
		log.debug("=> allowBots()");
		String pattern = "\\{\\{(nobots|bots\\|(allow=none|deny=(.*?" + user + ".*?|all)|optout=all))\\}\\}";
    	//Pattern p = Pattern.compile("\\[\\[(?<article>.+)\\]\\]");
    	Pattern p = Pattern.compile(pattern,Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
    	Matcher m = p.matcher(text);
    	boolean b1 = m.find();
    	log.debug("m.find() finished");
    	pattern = "\\{\\{(bots\\|(allow=([^\\}]+?)))\\}\\}"; 
    	p = Pattern.compile(pattern,Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
    	m = p.matcher(text);
    	boolean b2 = false;
    	//m.find()
    	if(m.find()) {
    		String who = m.group(3);
    		if(who.trim().compareToIgnoreCase("none")!=0 && 
    			who.trim().compareToIgnoreCase("all")!=0 &&
    			!who.contains(user)) {
    			b2 = true;    			
    		}
    	}
    	log.debug("m.find() finished");
    	log.debug("<= allowBots()");
        return (!b1 && !b2);  
    }
	
	public static String getAllowBotsString(String text) {
		String nobots = null;
		String pattern = "(\\{\\{(nobots|bots\\|([^\\}]+))\\}\\})";
    	Pattern p = Pattern.compile(pattern,Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
    	Matcher m = p.matcher(text);
    	//boolean b1 = m.matches();
    	if(m.find()) {
    		//log("matches");
    		nobots = m.group(1);
    	}
		return nobots;
	}
	
	public String [] getPageLines(String title) throws IOException {
	        // pitfall check
	    if (namespace(title) < 0)
	        throw new UnsupportedOperationException("Cannot retrieve Special: or Media: pages!");
	
	    // go for it
	    String url = base + URLEncoder.encode(title, "UTF-8") + "&action=raw";
	    String [] temp = fetchLines(url, "getPageLines");
	    log(Level.INFO, "getPageLines", "Successfully retrieved text of " + title);
	        return decode(temp);
	}
 
	protected String [] decode(String items[]) {
	        // Remove entity references. Oddly enough, URLDecoder doesn't nuke these.
	 	for(int i=0;i<items.length;i++) {
	 		String item = items[i];
	 		items[i] = item.replace("&lt;", "<").
	 				replace("&gt;", ">").
	 				replace("&amp;", "&").
	 				replace("&quot;", "\"").
	 				replace("&#039;", "'");
		 	}
	 	return items;
    }
	
	protected String [] fetchLines(String url, String caller) throws IOException {
		logurl(url, caller);
	    URLConnection connection = new URL(url).openConnection();
	    connection.setConnectTimeout(CONNECTION_CONNECT_TIMEOUT_MSEC);
	    connection.setReadTimeout(CONNECTION_READ_TIMEOUT_MSEC);
	    setCookies(connection);
	    connection.connect();
	    grabCookies(connection);
	
		    // check lag
		int lag = connection.getHeaderFieldInt("X-Database-Lag", -5);
		if (lag > maxlag)
		{
		    try
		    {
		        synchronized(this)
		        {
		            int time = connection.getHeaderFieldInt("Retry-After", 10);
		            log(Level.WARNING, caller, "Current database lag " + lag + " s exceeds " + maxlag + " s, waiting " + time + " s.");
		            Thread.sleep(time * 1000);
		        }
		    }
		    catch (InterruptedException ex)
		    {
		        // nobody cares
		    }
		    return fetchLines(url, caller); // retry the request
		    }
		 
		    
		    BufferedReader in = new BufferedReader(new InputStreamReader(
		        zipped ? new GZIPInputStream(connection.getInputStream()) : connection.getInputStream(), "UTF-8"));
		
		// get the text
		String line;
		ArrayList<String> lines = new ArrayList<String>(1000);
		while ((line = in.readLine()) != null)
		{
		    lines.add(line);
		    //text.append("\n");
		}
	    in.close();
	    return lines.toArray(new String[0]);
	}
 
	 /**
	 *  Gets the first revision of a page.
	 *  @param title a page
	 *  @return the oldest revision of that page
	 *  @throws IOException if a network error occurs
	 *  @since 0.24
	 */
	public Revision getFirstRevision(String title, boolean resolveRedirect) throws IOException
	{
		boolean was = isResolvingRedirects();
	    this.setResolveRedirects(resolveRedirect);
	    this.setResolveRedirects(resolveRedirect);
	    Revision r = super.getFirstRevision(title);
	    return r;
	}
	
	/**
	 *  Gets the most recent revision of a page.
	 *  @param title a page
	 *  @return the most recent revision of that page
	 *  @throws IOException if a network error occurs
	 *  @since 0.24
	 */
	public Revision getTopRevisionWithNewTitle(String title, boolean resolveRedirect) throws IOException
	{
		boolean was = isResolvingRedirects();
	    this.setResolveRedirects(resolveRedirect);
	    Revision r = super.getTopRevision(title);
	    setResolveRedirects(was);
	    return r;
	}
	
	private class ContribsIterator implements Iterator<Revision[]> {
    	private boolean endReached = false;
    	private String user;

		@Override
        public boolean hasNext() {
	        return !endReached;
        }

		@Override
        public Revision[] next() {
	        // TODO Auto-generated method stub
	        return null;
        }

		@Override
        public void remove() {
	        //throw new OperationNotSupportedException();	        
        }
		
		public ContribsIterator(String user) {
			this.user = user;
		}
    	
    }

    public boolean exists(String title) throws IOException
    {
        return exists(new String[] { title })[0];
    }

}
