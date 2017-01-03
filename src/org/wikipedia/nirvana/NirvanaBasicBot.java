/**
 *  @(#)NirvanaBasicBot.java 27.12.2015
 *  Copyright © 2012-2015 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.wikipedia.nirvana.nirvanabot.BotFatalError;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.login.FailedLoginException;

/**
 * How to use this bot framework?
 * 1) CONFIG
 * Write your config file in xml: config.xml
 * You may choose another file name, but specify it in command line when starting the bot:
 * java - <package_name_of_your_bot>.<YourBotClassName> <your_config_file_name>.xml
 * If you have special properties, load them in redefined {@link loadCustomProperties()}
 * Use {@link properties} to load your properties
 * 2) Bot INFO, LICENSE, USAGE
 * Redefine {@link showLicense()} if your prefer license other than GNU GPL
 * Redefine {@link showInfo()}. Redefine {@link showUsage()}.
 * 3) CONSTRUCTOR
 * Write your constructor and pass int flags to default constructor
 * 4) GO
 * Implement the body of {@link go()} - the main code of what your bot
 * 5) MAIN
 * Implement <code>public static void main()</code> where create your bot instance with specified flags
 * and call {@link run()} to run bot.
 */
public abstract class NirvanaBasicBot {
    public static final int FLAG_SHOW_LICENSE = 0b001;
    /**
     * Please use log4j.properties and {@value #FLAG_DEFAULT_LOG} to configure default logger.
     */
    @Deprecated
    public static final int FLAG_CONSOLE_LOG = 0b0010;
    public static final int FLAG_DEFAULT_LOG = 0b0100;
    public static final int FLAG_NO_LOG      = 0b1000;

	protected static final boolean DEBUG_BUILD = false;
	protected static org.apache.log4j.Logger log = null;
	
	protected static Properties properties = null;
	protected boolean DEBUG_MODE = false;
	
	public static final String YES = "yes";
	public static final String NO = "no";
	
	protected int MAX_LAG = 15;
	protected int THROTTLE_TIME_MS = 10000;
	
	protected NirvanaWiki wiki;
	protected String LANGUAGE= "ru";
	protected String DOMAIN = ".wikipedia.org";
	protected String SCRIPT_PATH = "/w";
	protected String PROTOCOL = "https://";
	protected String COMMENT = "обновление";
	protected int flags = 0;

    @Deprecated
    private ConsoleAppender consoleAppender = null;

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

	public int getFlags() {
		return flags;
	}

    public int run(String args[]) {
		showInfo();
		if((flags & FLAG_SHOW_LICENSE) != 0) {
			showLicense();
		}
        System.out.print("----------------------< BOT STARTED >-------------------------------\n");
		String configFile = getConfig(args);		
		System.out.println("applying config file: "+configFile);
		Map<String,String> launch_params = getLaunchArgs(args);
        int exitCode = 0;
        try {
            startWithConfig(configFile, launch_params);
        } catch (BotFatalError e) {
            System.err.println("Error: " + e.toString());
            e.printStackTrace();
            exitCode = 1;
        }

        cleanup();
        System.out.print(String.format(
                "----------------------< BOT FINISHED (%d) > -----------------------------\n",
                exitCode));
        return exitCode;
    }

    private void cleanup() {
        if (consoleAppender != null) {
            Logger.getRootLogger().removeAppender(consoleAppender);
            consoleAppender = null;
        }
    }

    private Map<String, String> getLaunchArgs(String[] args) {
	    HashMap<String,String> params = new HashMap<String,String>();
	    for(int i=1;i<args.length;i++) {
	    	if(!args[i].startsWith("-")) continue;
	    	String [] parts = args[i].substring(1).split("=", 2);
	    	String left = parts[0];
	    	String right = "1";
	    	if(parts.length == 2) {
	    		right = parts[1];
	    	}
	    	params.put(left, right);
	    }
	    return params;
    }
	/**
	 * this is an example of main function
	 */
    /**
    public static void main(String[] args) {
        NirvanaBasicBot bot = new NirvanaBasicBot(FLAG_DEFAULT_LOG);
        System.exit(bot.run(args));
	}*/
    
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

    public void startWithConfig(String cfg, Map<String,String> launch_params)
            throws BotFatalError {
		properties = new Properties();
		try {
			InputStream in = new FileInputStream(cfg);
			if(cfg.endsWith(".xml")) {
				properties.loadFromXML(in);			
			} else {
				properties.load(in);		
			}
			in.close();
        } catch (IOException e) {
            throw new BotFatalError(e);
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
		
		LANGUAGE = properties.getProperty("wiki-lang", LANGUAGE);
		log.info("language=" + LANGUAGE);
		DOMAIN = properties.getProperty("wiki-domain", DOMAIN);
		log.info("domain=" + DOMAIN);
		PROTOCOL = properties.getProperty("wiki-protocol", PROTOCOL);
		log.info("protocol=" + PROTOCOL);
		COMMENT = properties.getProperty("update-comment",COMMENT);
		MAX_LAG = Integer.valueOf(properties.getProperty("wiki-maxlag", String.valueOf(MAX_LAG)));
		THROTTLE_TIME_MS = Integer.valueOf(properties.getProperty("wiki-throttle", String.valueOf(THROTTLE_TIME_MS)));
		log.info("comment="+COMMENT);
		DEBUG_MODE = properties.getProperty("debug-mode",DEBUG_MODE?YES:NO).equals(YES);
		log.info("DEBUG_MODE="+DEBUG_MODE);
		
		
		if (!loadCustomProperties(launch_params)) {
			log.fatal("Failed to load all required properties. Exiting...");
			return;
		}
		String domain = DOMAIN;
		if (domain.startsWith(".")) {
			domain = LANGUAGE + DOMAIN;
		}
        wiki = createWiki(domain, SCRIPT_PATH, PROTOCOL);

		configureWikiBeforeLogin();
		
		log.info("login to " + domain + ", login: "+login+", password: (not shown)");
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
		
		if(DEBUG_MODE) {
			wiki.setDumpMode();
		}	
		
		log.warn("BOT STARTED");
		
		try {
	        go();
        } catch (InterruptedException e) {
        	onInterrupted(e);
        }		
		
		wiki.logout();		
		log.warn("EXIT");
	}

    protected NirvanaWiki createWiki(String domain, String path, String protocol) {
        return new NirvanaWiki(domain, path, protocol);
    }

	protected void onInterrupted(InterruptedException e) {
		
	}
	
	/**
     * 
     */
    protected void configureWikiBeforeLogin() {
		wiki.setMaxLag( MAX_LAG );
		wiki.setThrottle(THROTTLE_TIME_MS);
    }

	protected abstract void go() throws InterruptedException;
	
	protected boolean loadCustomProperties(Map<String,String> launch_params) {
		return true;
	}

    protected void initLog() throws BotFatalError {
        if ((flags & FLAG_DEFAULT_LOG) != 0) {
            System.out.println(
                    "INFO: logs must be configured automatically from log4j.properties");
        } else if ((flags & FLAG_NO_LOG) != 0) {
            System.out.println("INFO: logs disabled");
            properties.setProperty("log4j.rootLogger", "OFF");
            PropertyConfigurator.configure(properties);
        } else if ((flags & FLAG_CONSOLE_LOG) != 0) {
            System.out.println("INFO: console logs enabled");
            ConsoleAppender consoleAppender = new ConsoleAppender();
            // %c will giv java path
            String PATTERN = "%d [%p|%C{1}] %m%n";
            consoleAppender.setLayout(new PatternLayout(PATTERN)); 
            consoleAppender.setThreshold(Level.DEBUG);
            consoleAppender.activateOptions();
            Logger.getRootLogger().addAppender(consoleAppender);
        } else {
            String log4jSettings = properties.getProperty("log4j-settings", "");
            if (log4jSettings.isEmpty()) {
                throw new BotFatalError("You must specify log4j settings file in " +
                        "log4j-settings\" property of bot configuration.");
            }
            File f = new File(log4jSettings);
            if (!f.exists()) {
                throw new BotFatalError(String.format("File \"%s\" not found.", log4jSettings));
            }
            if (!f.isFile()) {
                throw new BotFatalError(String.format("\"%s\" must be a file.", log4jSettings));
            }
			PropertyConfigurator.configure(log4jSettings);
			System.out.println("INFO: using log settings : " + log4jSettings);
		}
		log = org.apache.log4j.Logger.getLogger(this.getClass().getName());
	}

	protected static int validateIntegerSetting(Properties pop, String name, int def, boolean notifyNotFound) {
		int val = def;
		try {
			String str = properties.getProperty(name);
			if (str == null) {
				if (notifyNotFound) { 
					log.info("settings: integer value not found in settings ("+name+")");
				}
				return val;
			}
			val = Integer.parseInt(str);				
		} catch (NumberFormatException e){
			log.error("invalid settings: error when parsing integer values of "+name);
		} catch (NullPointerException e){
			if (notifyNotFound) {
				log.info("settings: integer value not found in settings ("+name+"), using default");
			}
		}
		return val;
	}
	
	public static boolean textOptionsToMap(String text, Map<String, String> parameters)
    {		
		return textOptionsToMap(text, parameters, "#", "//");
    }
	
	public static boolean textOptionsToMap(String text, Map<String, String> parameters, String... commentSeparators)
    {
		String lines[] = text.split("\r|\n");
		for(String line: lines) {			
			if(line.trim().isEmpty()) continue;
			log.debug(line);
			if (commentSeparators != null && commentSeparators.length > 0) {
				if (StringUtils.startsWithAny(line.trim(), commentSeparators)) {
					continue;
				}
			}
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
	
	public static ArrayList<String> optionToStringArray(String option) {
		return optionToStringArray(option, false, ",");
	}
	
	public static ArrayList<String> optionToStringArray(String option, boolean withDQuotes) {
		return optionToStringArray(option, withDQuotes, ",");
	}
	
	public static ArrayList<String> optionToStringArray(String option, boolean withDQuotes, String separator) {
		ArrayList<String> list = new ArrayList<String>();
		String separatorPattern;
		if (withDQuotes && option.contains("\"")) {
			separatorPattern = "(\"\\s*"+separator+"\\s*\"|^\\s*\"|\"\\s*$)"; 
		} else {
			separatorPattern = separator;
		}
		String[] items = option.split(separatorPattern);
		for (int i = 0; i < items.length; ++i) {
			String cat = items[i].trim();
			if (!cat.isEmpty()) {
				list.add(cat);
			}
		}
		return list;
	}

	public static boolean TryParseTemplate(String template, 
										   String userNamespace, 
										   String text, 
										   Map<String, String> parameters, 
										   boolean splitByNewLine)
    {
		log.debug("portal settings parse started");
		log.debug("template = "+template);
		log.debug("text = "+StringTools.trancateTo(text, 100));
        //String str = "^\\{\\{"+newpagesTemplateName+".*\\}\\}.*$"; // works
        //String str = "^(\\{\\{"+newpagesTemplateName+".*(\\{\\{.+\\}\\})?.*\\}\\})(.*)$";
		String recognizeTemplate = template;
		String userEn = "User:";
		String userLc = userNamespace+":";
		if(recognizeTemplate.startsWith(userEn))  {
			recognizeTemplate = recognizeTemplate.substring(userEn.length());			
		} else if (recognizeTemplate.startsWith(userLc)) {
			recognizeTemplate = recognizeTemplate.substring(userLc.length());			
		}
		recognizeTemplate = "("+userEn+"|"+userLc+")"+recognizeTemplate.replace("(", "\\(").replace(")", "\\)");
		// We don't start from ^ because we allow any text before template
		
		if (!WikiUtils.parseTemplate(recognizeTemplate, text, parameters, splitByNewLine)) {
			log.error("portal settings parse error");
			return false;
		}

        parameters.put("BotTemplate", template);
        log.debug("portal settings parse finished");
        return true;
    }
}
