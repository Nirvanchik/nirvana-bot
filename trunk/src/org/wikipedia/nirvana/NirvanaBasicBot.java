/**
 *  @(#)NirvanaBasicBot.java 16/11/2013
 *  Copyright © 2012-2013 Dmitry Trofimovich (KIN)
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.FailedLoginException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author kin
 *
 */
public class NirvanaBasicBot {
	public static final int FLAG_SHOW_LICENSE = 0b01;
	public static final int FLAG_CONSOLE_LOG = 0b010;
	protected static final boolean DEBUG_BUILD = false;
	protected static org.apache.log4j.Logger log = null;
	
	protected static Properties properties = null;
	protected static boolean DEBUG_MODE = false;
	
	public static final String YES = "yes";
	public static final String NO = "no";
	
	protected NirvanaWiki wiki;
	private static String LANGUAGE= "ru";
	protected static String COMMENT = "обновление";
	protected int flags = 0;
	
	public static String LICENSE = 
			"This program is free software: you can redistribute it and/or modify\n" +
			"it under the terms of the GNU General Public License as published by\n" +
			"the Free Software Foundation, either version 3 of the License, or\n" +
			"(at your option) any later version.\n" +
			"\n" +
			"This program is distributed in the hope that it will be useful,\n" +
			"but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
			"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
			"GNU General Public License for more details.\n" +
			"\n" +
			"You should have received a copy of the GNU General Public License\n" +
			"along with this program.  If not, see <http://www.gnu.org/licenses/>\n";
	
	public void showUsage() {
		
	}
	public void showInfo() {
		System.out.print("NirvanaBasicBot v1.2 Copyright (C) 2012-2013 Dmitry Trofimovich (KIN)\n\n");
		
	}
	public void showLicense() {		
		System.out.print(LICENSE);
	}
	
	public NirvanaBasicBot() {
		
	}
	
	public NirvanaBasicBot(int flags) {
		this.flags = flags;
	}

	
	public static NirvanaBasicBot createBot() {
		return new NirvanaBasicBot();
	}
	
	public int getFlags() {
		return flags;
	}
	
	public void run(String args[]) {
		showInfo();
		if((flags & FLAG_SHOW_LICENSE) != 0) {
			showLicense();
		}
		System.out.print("-----------------------------------------------------------------\n");
		String configFile = getConfig(args);		
		System.out.println("applying config file: "+configFile);
		startWithConfig(configFile);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NirvanaBasicBot bot = createBot();
		bot.run(args);
	}
	public String getConfig(String[] args) {
		String configFile = null;
		if(args.length==0) {
			if(DEBUG_BUILD) {
				configFile = "config_test.xml";				
			} else {
				configFile = "config.xml";				
			}			
		} else {
			configFile = args[0];				
		}
		return configFile;
	}
	
	public void startWithConfig(String cfg) {
		properties = new Properties();
		try {
			InputStream in = new FileInputStream(cfg);
			if(cfg.endsWith(".xml")) {
				properties.loadFromXML(in);			
			} else {
				properties.load(in);		
			}
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("ABORT: file "+cfg+" not found");
			//e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
		initLog();
		
		String login = properties.getProperty("wiki-login");
		String pw = properties.getProperty("wiki-password");
		if(login==null || pw==null || login.isEmpty() || pw.isEmpty()) {
			String accountFile = properties.getProperty("wiki-account-file");
			if(accountFile==null || accountFile.isEmpty()) {
				System.out.println("ABORT: login info not found in properties");
				log.fatal("wiki-login or wiki-password or wiki-account-file is not specified in settings");
				return;
			}
			Properties loginProp = new Properties();
			try {
				InputStream in = new FileInputStream(accountFile);
				if(accountFile.endsWith(".xml")) {
					loginProp.loadFromXML(in);			
				} else {
					loginProp.load(in);		
				}
				in.close();
			} catch (FileNotFoundException e) {
				System.out.println("ABORT: file "+accountFile+" not found");
				log.fatal("ABORT: file "+accountFile+" not found");
				return;
			} catch (IOException e) {	
				System.out.println("ABORT: failed to read "+accountFile);
				log.fatal("failed to read "+accountFile+" : "+e);
				return;
			}
			login = loginProp.getProperty("wiki-login");
			pw = loginProp.getProperty("wiki-password");
			if(login==null || pw==null || login.isEmpty() || pw.isEmpty()) {
				System.out.println("ABORT: login info not found in file "+accountFile);
				log.fatal("wiki-login or wiki-password or wiki-account-file is not found in file "+accountFile);
				return;
			}
		}
		
		log.info("login="+login+",password=(not shown)");
		
		LANGUAGE = properties.getProperty("wiki-lang",LANGUAGE);
		log.info("language="+LANGUAGE);
		COMMENT = properties.getProperty("update-comment",COMMENT);
		log.info("comment="+COMMENT);
		DEBUG_MODE = properties.getProperty("debug-mode",DEBUG_MODE?YES:NO).equals(YES);
		log.info("DEBUG_MODE="+DEBUG_MODE);
		
		
		loadCustomProperties();
		
		wiki = new NirvanaWiki( LANGUAGE + ".wikipedia.org" );
		wiki.setMaxLag( 15 );
		log.info("login to "+LANGUAGE+ ".wikipedia.org, login: "+login+", password: (not shown)");
		try {
			wiki.login(login, pw.toCharArray());
		} catch (FailedLoginException e) {
			log.fatal("Failed to login to "+LANGUAGE+ ".wikipedia.org, login: "+login+", password: "+pw);
			return;
		} catch (IOException e) {			
			log.fatal(e.toString());
			e.printStackTrace();
			return;
		}
		
		if(NirvanaBasicBot.DEBUG_MODE) {
			wiki.setDumpMode("dump");
		}	
		
		log.warn("BOT STARTED");
		
		go();		
		
		wiki.logout();		
		//print( "db lag (seconds): " + m_wiki.getCurrentDatabaseLag() );
		log.warn("EXIT");
	}
	
	protected void go() {
		log.info("This is a basic bot framework");
		log.info("It doesn't have any practical use, but can be utilized as a basis to create a new Bot");
	}
	protected boolean loadCustomProperties() {
		return true;
	}
	protected void initLog() {
		String log4jSettings = properties.getProperty("log4j-settings");
		if(log4jSettings==null || log4jSettings.isEmpty() || !(new File(log4jSettings)).exists()) {
			Properties properties = new Properties();
			if((flags & FLAG_CONSOLE_LOG)!=0) {
				System.out.println("INFO: console logs enabled");			
				ConsoleAppender console = new ConsoleAppender(); //create appender
				  //configure the appender
				  String PATTERN = "%d [%p|%C{1}] %m%n"; //%c will giv java path
				  console.setLayout(new PatternLayout(PATTERN)); 
				  console.setThreshold(Level.DEBUG);
				  console.activateOptions();
				  //add appender to any Logger (here is root)
				  Logger.getRootLogger().addAppender(console);
				  //properties.setProperty("log4j.rootLogger", "DEBUG, stdout");					
			} else {
				System.out.println("INFO: logs disabled");			
				properties.setProperty("log4j.rootLogger", "OFF");
				PropertyConfigurator.configure(properties);
			}
			
			log = org.apache.log4j.Logger.getLogger(this.getClass().getName());
			//log.setLevel(Level.OFF);
			//nologs = true;
		} else {
			PropertyConfigurator.configure(log4jSettings);
			log = org.apache.log4j.Logger.getLogger(this.getClass().getName());	
			System.out.println("INFO: using log settings : " + log4jSettings);
		}
	}
	
	protected static int validateIntegerSetting(Properties pop, String name, int def, boolean notifyNotFound) {
		int val = def;
		try {
			String str = properties.getProperty(name);
			if(str==null) {
				if(notifyNotFound) { 
					log.info("settings: integer value not found in settings ("+name+")");
				}
				return val;
			}
			val = Integer.parseInt(str);				
		}catch(NumberFormatException e){
			log.error("invalid settings: error when parsing integer values of "+name);
			//properties.setProperty(name, String.valueOf(def));
		} catch(NullPointerException e){
			if(notifyNotFound) {
				log.info("settings: integer value not found in settings ("+name+"), using default");
			}
			//properties.setProperty(name, String.valueOf(def));
		}
		return val;
	}
	
	public static boolean textOptionsToMap(String text, Map<String, String> parameters)
    {
		String lines[] = text.split("\r|\n");
		log.debug("Archive settings");
		for(String line: lines) {			
			if(line.trim().isEmpty()) continue;
			log.debug(line);
			int index = line.indexOf("=");
			if(index<0) return false;
			parameters.put(line.substring(0,index).trim(), line.substring(index+1).trim());
		}
		return true;
    }
	
	protected void logPortalSettings(Map<String, String> parameters) {
		Set<Entry<String,String>> set = parameters.entrySet();
		Iterator<Entry<String,String>> it = set.iterator();
		while(it.hasNext()) {
			Entry<String,String> next = it.next();
			log.debug(next.getKey()+" = "+next.getValue());
		}
	}
	
	protected static ArrayList<String> optionToStringArray(String option, boolean withDQuotes) {
		ArrayList<String> list = new ArrayList<String>();
		String separator;
		if (withDQuotes && option.contains("\"")) {
			separator = "(\"\\s*,\\s*\"|^\\s*\"|\"\\s*$)";// old separator = "\","
		} else {
			separator = ",";
		}
		String[] items = option.split(separator);
		for (int i = 0; i < items.length; ++i) {
			String cat = items[i].trim();
			if (!cat.isEmpty()) {
				list.add(cat);
			}
		}
		return list;
	}

	public static boolean TryParseTemplate(String template, String text, Map<String, String> parameters)
    {
		log.debug("portal settings parse started");
        //parameters = null;
        //String str = "^{{"+newpagesTemplateName+".*({{.+({{.+}})?.*}})?.*}}.*$";
        //String str = "^\\{\\{"+newpagesTemplateName+".*\\}\\}.*$"; // works
        //String str = "^(\\{\\{"+newpagesTemplateName+".*(\\{\\{.+\\}\\})?.*\\}\\})(.*)$";
        //String name = newpagesTemplateName.substring(0, newpagesTemplateName.length()-1);
        //char last = newpagesTemplateName.charAt(newpagesTemplateName.length()-1);
		String recognizeTemplate = template;
		String ns1 = "Участник:";
		String ns2 = "User:";
		if(recognizeTemplate.startsWith(ns1))  {
			recognizeTemplate = recognizeTemplate.substring(ns1.length());			
		} else if (recognizeTemplate.startsWith(ns2)) {
			recognizeTemplate = recognizeTemplate.substring(ns2.length());			
		}
		recognizeTemplate = "("+ns1+"|"+ns2+")"+recognizeTemplate;
        String str = "^(\\{\\{"+recognizeTemplate+")(.+)$"; // GOOD
        //String str = "(\\{\\{"+newpagesTemplateName+"(.*(\\{\\{.+?\\}\\})*.*))+?\\}\\}";
        //String str = "(\\{\\{"+newpagesTemplateName+"(.*(\\{\\{[^\\{\\}]+\\}\\})*[^\\{]*))\\}\\}";
        //log.debug("pattern:"+str);
        Pattern pattern = Pattern.compile(str,Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
        //Regex templateRE = new Regex(@"\{\{(User):ClaymoreBot/Новые стать(и).",
          //  RegexOptions.IgnoreCase | RegexOptions.Singleline);
        Matcher m = pattern.matcher(text);
       // Match m = templateRE.Match(text);
        /*if(!m.find())
        	return false;
        */
        if (!m.matches())
        {
        	log.error("portal settings parse error (doesn't match pattern)");
            return false;
        }
        
        log.debug("group count = "+m.groupCount());
        //for(int i=0;i<m.groupCount();i++) {
        //	log.debug("group "+(i)+": "+m.group(i));
        //}
        
        //text.substring(m.end(1),text.length());
        
        
        int index = 1;
        int begin = m.end(1) + 1;
        int end = -1;
        for (int i = begin; i < text.length() - 1; ++i)
        {
            if (text.charAt(i) == '{' && text.charAt(i+1) == '{')
            {
                ++index;
            }
            else if (text.charAt(i) == '}' && text.charAt(i+1) == '}')
            {
                --index;
                if (index == 0)
                {
                    end = i;
                    break;
                }
            }
        }

        if (end == -1)
        {
            return false;
        }
        Pattern commentPattern = Pattern.compile("<!--(.+?)-->");
        
        String parameterString = text.substring(begin, end);
        //log.debug("parameter string: "+parameterString);
        String[] ps = parameterString.split("\\|");
        String lastKey = "";
        String lastVal = "";
        for(int i =0;i<ps.length;i++) {
        	String p = ps[i];
            //p.
            //Pattern equalPattern = Pattern.compile("([^=])=");
        	//log.debug("checking string: "+p);
        	boolean newStringToLastVal = false;
        	int count = StringTools.howMany(p, '=');
        	if(count==0 && i==0) continue;
        	if(!lastVal.isEmpty() && lastVal.endsWith("{")) { // {| означает начало таблицы
        		newStringToLastVal = true;        		
        	} else if(count>0) {
        		int eq = p.indexOf('=');
        		String first = p.substring(0,eq); 
        		String last = p.substring(eq+1);
        		String key = first.trim().toLowerCase();
        		if(key.equals("align") || key.equals("style")) {
        			newStringToLastVal = true;
        		} else {
	        		Matcher mComment = commentPattern.matcher(last);
	            	String value = mComment.replaceAll("").trim();
	        		parameters.put(key, value);
	                lastKey = key;        	
	                lastVal = value;
        		}
        	} else {
        		if (!lastKey.isEmpty())
                {
        			newStringToLastVal = true;                	
                }
        	}  
        	if(newStringToLastVal) {
        		Matcher mkey = commentPattern.matcher(p);
                String value = mkey.replaceAll("").trim();
                //parameters[lastKey] = parameters[lastKey] + "|" + value;
                lastVal = parameters.get(lastKey)+"|"+value;
                parameters.put(lastKey, lastVal);
        	}
        }
        log.debug("portal settings parse finished");
        return true;
    }
}
