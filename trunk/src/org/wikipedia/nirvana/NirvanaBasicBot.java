/**
 *  @(#)NirvanaBasicBot.java 02/07/2012
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

package org.wikipedia.nirvana;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.security.auth.login.FailedLoginException;

import org.apache.log4j.PropertyConfigurator;

/**
 * @author kin
 *
 */
public class NirvanaBasicBot {
	protected static final boolean DEBUG_BUILD = false;
	public static org.apache.log4j.Logger log = null;
	
	protected static Properties properties = null;
	protected static boolean DEBUG_MODE = false;
	
	public static final String YES = "yes";
	public static final String NO = "no";
	
	protected NirvanaWiki wiki;
	private static String LANGUAGE= "ru";
	protected static String COMMENT = "обновление";
	
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
		System.out.print("NirvanaBasicBot v1.0 Copyright (C) 2012 Dmitry Trofimovich (KIN)\n\n");
		
	}
	public void showLicense() {		
		System.out.print(LICENSE);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//System.out.print("This is a basic class NirvanaBasicBot. It is not useful.\n");
		NirvanaBasicBot bot = new NirvanaBasicBot();
		bot.showInfo();
		bot.showLicense();
		System.out.print("-----------------------------------------------------------------\n");
		String configFile = bot.getConfig(args);		
		System.out.println("applying config file: "+configFile);
		bot.startWithConfig(configFile);
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
		if(login==null || pw==null) {
			System.out.println("ABORT: properties not found");
			log.fatal("login (wiki-login) or password(wiki-password) is not specified in settings");
			return;
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
	protected void loadCustomProperties() {				
	}
	protected void initLog() {
		String log4jSettings = properties.getProperty("log4j-settings");
		if(log4jSettings==null || log4jSettings.isEmpty() || !(new File(log4jSettings)).exists()) {
			System.out.println("INFO: logs disabled");
			Properties prop_no_logs = new Properties();
			prop_no_logs.setProperty("log4j.rootLogger", "OFF");
			PropertyConfigurator.configure(prop_no_logs);
			log = org.apache.log4j.Logger.getLogger(NirvanaBasicBot.class.getName());
			//log.setLevel(Level.OFF);
			//nologs = true;
		} else {
			PropertyConfigurator.configure(log4jSettings);
			log = org.apache.log4j.Logger.getLogger(NirvanaBasicBot.class.getName());	
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

}
