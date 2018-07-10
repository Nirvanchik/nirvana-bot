/**
 *  @(#)BasicBot.java 01.06.2018
 *  Copyright © 2018 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana;

import org.wikipedia.nirvana.annotation.LocalizedBySettings;
import org.wikipedia.nirvana.nirvanabot.BotFatalError;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
 * If you have special properties, load them in redefined {@link #loadCustomProperties()}
 * Use {@link #properties} to load your properties
 * 2) Bot INFO, LICENSE, USAGE
 * Redefine {@link #showLicense()} if your prefer license other than GNU GPL
 * Redefine {@link #showInfo()}. Redefine {@link #showUsage()}.
 * 3) CONSTRUCTOR
 * Write your constructor and pass int flags to default constructor
 * 4) GO
 * Implement the body of {@link go()} - the main code of what your bot
 * 5) MAIN
 * Implement <code>public static void main()</code> where create your bot instance with specified flags
 * and call {@link run()} to run bot. Example of typical main() function:
 * <pre>
 * {@code
 * public static void main(String[] args) {
 *     BasicBot bot = new NirvanaArchiveBot();
 *     System.exit(bot.run(args));
 * }
 * }
 * </pre>
 */
public abstract class BasicBot {
    public static final int FLAG_SHOW_LICENSE = 0b001;
    /**
     * Please use log4j.properties and {@value #FLAG_DEFAULT_LOG} to configure default logger.
     */
    @Deprecated
    public static final int FLAG_CONSOLE_LOG = 0b0010;
    public static final int FLAG_DEFAULT_LOG = 0b0100;
    /**
     * Not supported. Works the same as {@link #FLAG_DEFAULT_LOG}
     */
    @Deprecated
    public static final int FLAG_NO_LOG      = 0b1000;

    public static final boolean DEBUG_BUILD = false;
    public static final String DEFAULT_DUMP_DIR = "dump";

    protected static Logger log = null;

	protected static Properties properties = null;

	protected boolean DEBUG_MODE = false;
	
	public static final String YES = "yes";
	public static final String NO = "no";

    /**
     * This is maxlag parameter they are talking about here:
     * https://www.mediawiki.org/wiki/Manual:Maxlag_parameter
     * Specified in seconds.
     */
    protected int maxLag = 5;
	protected int THROTTLE_TIME_MS = 10000;

	protected NirvanaWiki wiki;

    protected String outDir = ".";
    protected String dumpDir = DEFAULT_DUMP_DIR;
	protected String LANGUAGE= "ru";
	protected String DOMAIN = ".wikipedia.org";
	protected String SCRIPT_PATH = "/w";
	protected String PROTOCOL = "https://";
    @LocalizedBySettings
    protected String COMMENT = "обновление";
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

    /**
     * Prints basic information about bot (name, version, copyright, purpose) when the bot starts
     * with {@link #run(String[])} method. Override it to print information about your bot. 
     */
	public void showInfo() {
        System.out.print("BasicBot v1.2 Copyright (C) 2018 Dmitry Trofimovich (KIN)\n\n");
	}

	public void showLicense() {		
		System.out.print(LICENSE);
	}
	
	public BasicBot() {
		
	}
	
	public BasicBot(int flags) {
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
            if (log == null) {
                System.err.println("Error: " + e.toString());
            } else {
                log.fatal(e);
            }
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
        outDir = properties.getProperty("out-dir", outDir);
        checkWorkDir();

		initLog();
        log.info("Work directory: " + outDir);
        dumpDir = outDir + "/" + DEFAULT_DUMP_DIR;
        FileTools.setDefaultOut(dumpDir);

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
        maxLag = Integer.valueOf(properties.getProperty("wiki-maxlag", String.valueOf(maxLag)));
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
        wiki = createWiki(domain, SCRIPT_PATH, PROTOCOL, LANGUAGE);

		configureWikiBeforeLogin();
		
		log.info("login to " + domain + ", login: "+login+", password: (not shown)");
		try {
			wiki.login(login, pw.toCharArray());
		} catch (FailedLoginException e) {
            throw new BotFatalError(String.format("Failed to login to %s, login: %s, password: %s",
                    domain, login, pw));
        } catch (IOException e) {
            throw new BotFatalError(e);
		}

		if(DEBUG_MODE) {
            wiki.setDumpMode(dumpDir);
		}	

        log.info("BOT STARTED");
		try {
	        go();
        } catch (InterruptedException e) {
        	onInterrupted(e);
        } finally {
            wiki.logout();
        }
        log.info("EXIT");
	}

    private void checkWorkDir() throws BotFatalError {
        File d = new File(outDir);
        if (!d.exists()) {
            d.mkdirs();
            return;
        }
        if (!d.isDirectory()) {
            throw new BotFatalError(String.format(
                    "Directory %s specified in \"work-dir\" parameter must be a directory",
                    outDir));
        }
    }

    protected NirvanaWiki createWiki(String domain, String path, String protocol,
            String language) {
        return new NirvanaWiki(domain, path, protocol, language);
    }

	protected void onInterrupted(InterruptedException e) {
		
	}

    /**
     * Override it to customize {@link #wiki} before wiki.login() will be called.
     */
    protected void configureWikiBeforeLogin() {
        wiki.setMaxLag(maxLag);
		wiki.setThrottle(THROTTLE_TIME_MS);
    }

    protected abstract void go() throws InterruptedException, BotFatalError;

	protected boolean loadCustomProperties(Map<String,String> launch_params) {
		return true;
	}

    protected void initLog() throws BotFatalError {
        if ((flags & FLAG_DEFAULT_LOG) != 0) {
            System.out.println(
                    "INFO: logs must be configured automatically from log4j2.properties");
        } else if ((flags & FLAG_NO_LOG) != 0) {
            System.out.println("INFO: skip log4j configuration.");
        } else if ((flags & FLAG_CONSOLE_LOG) != 0) {
            System.out.println("INFO: skip log4j configuration. " +
                    "Will be configured from log4j2.properties or console logger will be used.");
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
            System.setProperty("log4j.configurationFile", log4jSettings);
            System.setProperty("log4j.nirvana.outdir", outDir);
			System.out.println("INFO: using log settings : " + log4jSettings);
		}
        log = LogManager.getLogger(this.getClass().getName());
	}

	protected void logPortalSettings(Map<String, String> parameters) {
		Set<Entry<String,String>> set = parameters.entrySet();
		Iterator<Entry<String,String>> it = set.iterator();
		while(it.hasNext()) {
			Entry<String,String> next = it.next();
			log.debug(next.getKey()+" = "+next.getValue());
		}
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
