/**
 *  @(#)NirvanaWiki.java 0.10
 *  Copyright © 2011-2017 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.security.auth.login.LoginException;

import org.wikipedia.Wiki;
import org.wikipedia.nirvana.localization.Localizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * based on Wiki.java version 0.31   
 */
public class NirvanaWiki extends Wiki {
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String REDIRECT_NAME_RU = "перенаправление";

	protected static final String DEFULT_DUMP_FOLDER = "dump";
	
    private static final long serialVersionUID = -8745212681497644127L;

    private static Logger log;	

    private final String language;
    private Localizer localizer;
    Pattern redirectPattern;
	private boolean dumpMode = false;
    private String dumpFolder = null;
	private boolean logDomain = false;
	//rev_comment` varbinary(767) NOT NULL, Summaries longer than 200 characters are
    //*  truncated server-side.

    static {
        log = LogManager.getLogger(NirvanaWiki.class.getName());
    }
	/**
	 * @param domain
	 */
	public NirvanaWiki(String domain) {
		super(domain);
        language = DEFAULT_LANGUAGE;
	}
	
	/**
	 * @param domain
	 * @param scriptPath
	 */
	public NirvanaWiki(String domain, String scriptPath) {
		super(domain, scriptPath);
        language = DEFAULT_LANGUAGE;
	}

	public NirvanaWiki(String domain, String scriptPath, String protocol) {
        this(domain, scriptPath, protocol, DEFAULT_LANGUAGE);
	}

    public NirvanaWiki(String domain, String scriptPath, String protocol, String language) {
        super(domain, scriptPath, protocol);
        if (language == null) {
            this.language = DEFAULT_LANGUAGE;
        } else {
            this.language = language;
        }
    }

    private void checkLocalizer() {
        if (localizer == null) {
            localizer = Localizer.getInstance();
        }
    }

    private void checkRedirectPattern() {
        if (redirectPattern == null) {
            String localizedRedirectMarker = "";
            if (language != null && !language.equals(DEFAULT_LANGUAGE)) {
                checkLocalizer();
                localizedRedirectMarker = localizer.localizeStrict(REDIRECT_NAME_RU);
                if (localizedRedirectMarker == null) {
                    localizedRedirectMarker = "";
                }
            }
            int unicode = 0;
            if (!localizedRedirectMarker.isEmpty()) {
                localizedRedirectMarker = "|" + localizedRedirectMarker;
                unicode = Pattern.UNICODE_CASE;
            }
            String pattern = "^\\#(REDIRECT" + localizedRedirectMarker + ")\\s*\\[\\[.+\\]\\].*$";
            redirectPattern = Pattern.compile(pattern,
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL | unicode);
        }
    }

	public void setDumpMode(String folder) {
		dumpMode = true;
		dumpFolder = folder;
	}

    /**
     *  Don't use hardcoded output folders.
     */
    @Deprecated
	public void setDumpMode() {
		setDumpMode(DEFULT_DUMP_FOLDER);
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
    
    @Override
    public synchronized void edit(String title, String text, String summary, boolean minor,
            boolean bot, int section, Calendar basetime) throws IOException, LoginException {
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
        edit(title, text, summary, minor, bot, -2, null);
    }
    
    public void edit(String title, String text, String summary) throws IOException, LoginException
    {
        edit(title, text, summary, false, true, -2, null);
    }
    
    public boolean editIfChanged(String title, String text, String summary, boolean minor, boolean bot) throws IOException, LoginException
    {
    	String old = null;
    	try {
    		old = this.getPageText(title);
    	} catch(FileNotFoundException e) {
    		// ignore. all job is done not here :)
    	}
    	if(old==null || old.length()!=text.length() || !old.equals(text)) { // to compare lengths is faster than comparing 5k chars
    		log.debug("editing "+title);
            edit(title, text, summary, minor, bot, -2, null);
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
        edit(title, text.toString(), "+" + stuff, minor, bot);
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

    public boolean isRedirect(String article) {
        checkRedirectPattern();
        Matcher m = redirectPattern.matcher(article);
        return m.matches();
    }

    public boolean allowEditsByCurrentBot(String text) {
    	return NirvanaWiki.allowBots(text, this.getCurrentUser().getUsername());
    }
    
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
		String nobots = "";
		String pattern = "(\\{\\{(nobots|bots\\|([^\\}]+))\\}\\})";
    	Pattern p = Pattern.compile(pattern,Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
    	Matcher m = p.matcher(text);
    	if(m.find()) {
    		nobots = m.group(1);
    	}
		return nobots;
	}
	
	@Deprecated
	public String [] getPageLinesArray(String title) throws IOException {
		List<String> lines = getPageLines(title); 
        return lines.toArray(new String[lines.size()]);
	}
	
	public List<String> getPageLines(String title) throws IOException {
        // pitfall check
        if (namespace(title) < 0)
            throw new UnsupportedOperationException("Cannot retrieve Special: or Media: pages!");
    
        // go for it
        String url = base + URLEncoder.encode(title, "UTF-8") + "&action=raw";
        List<String> items = fetchLines(url, "getPageLines");
        log(Level.INFO, "getPageLines", "Successfully retrieved text of " + title);
        return decodeList(items);
	}
 
	protected String [] decodeArray(String items[]) {
	 	for(int i=0;i<items.length;i++) {
	 		items[i] = decode(items[i]);
		}
	 	return items;
    }
	
	protected List<String> decodeList(List<String> items) {
	 	for(int i=0;i<items.size();i++) {
	 		items.set(i, decode(items.get(i)));
		}
	 	return items;
    }
	
	protected List<String> fetchLines(String url, String caller) throws IOException {
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
	    return lines;
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
	    Revision r = super.getFirstRevision(title);
	    setResolveRedirects(was);
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
	
    public boolean exists(String title) throws IOException
    {
        return exists(new String[] { title })[0];
    }
    
    public Revision[] getPageHistory(String title, Calendar start, Calendar end) throws IOException {
    	return getPageHistory(title, start, end, false);
    }
    
    public String prefixedName(int namespace, String name) throws IOException {
    	assert namespace != Wiki.MAIN_NAMESPACE;
    	return namespaceIdentifier(namespace) + ":" + name;
    }
    
    public String getPageTextBefore(String title, Calendar c) throws IOException
    {
        // pitfall check
        if (namespace(title) < 0)
            throw new UnsupportedOperationException("Cannot retrieve Special: or Media: pages!");
        
        Revision revs[] = getPageHistory(title, null, c, false);
        if (revs.length == 0) {
        	return null;
        }
        Revision r = revs[0];        
        return r.getText();
    }
    
    public Revision getPageRevisionBefore(String title, Calendar c) throws IOException
    {
        // pitfall check
        if (namespace(title) < 0)
            throw new UnsupportedOperationException("Cannot retrieve Special: or Media: pages!");
        
        Revision revs[] = getPageHistory(title, null, c, false);
        if (revs.length == 0) {
        	return null;
        }
        Revision r = revs[0];  
        return r;
    }

}
