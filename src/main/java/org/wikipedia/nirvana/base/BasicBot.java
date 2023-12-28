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

package org.wikipedia.nirvana.base;

import org.wikipedia.nirvana.annotation.LocalizedBySettings;
import org.wikipedia.nirvana.annotation.VisibleForTesting;
import org.wikipedia.nirvana.util.FileTools;
import org.wikipedia.nirvana.util.LogUtils;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.security.auth.login.FailedLoginException;

/**
 * How to use this bot framework?
 * 1) CONFIG
 * Write your config file in xml: config.xml
 * You may choose another file name, but specify it in command line when starting the bot:
 * <pre>
 * java - [package_name_of_your_bot].[YourBotClassName] [your_config_file_name].xml
 * </pre>
 * If you have special properties, load them in redefined {@link #loadCustomProperties()}
 * Use {@link #properties} to load your properties
 * 2) Bot INFO, LICENSE, USAGE
 * Redefine {@link #showLicense()} if your prefer license other than GNU GPL
 * Redefine {@link #showInfo()}. Redefine {@link #showUsage()}.
 * 3) CONSTRUCTOR
 * Write your constructor and pass int flags to default constructor
 * 4) GO
 * Implement the body of {@link #go()} - the main code of what your bot
 * 5) MAIN
 * Implement <code>public static void main()</code> and there create your bot instance with
 * specific flags and call {@link #run()} to run bot. Example of typical main() function:
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
    public static final int NO_FLAGS = 0;
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

    // TODO: Make it non-static.
    protected static Logger log = null;

    protected static Properties properties = null;

    protected boolean debugMode = false;

    public static final String YES = "yes";
    public static final String NO = "no";

    /**
     * This is maxlag parameter they are talking about here:
     * https://www.mediawiki.org/wiki/Manual:Maxlag_parameter
     * Specified in seconds.
     */
    protected int maxLag = 5;
    protected int throttleTimeMs = 10000;

    protected NirvanaWiki wiki;

    protected String outDir = ".";
    protected String dumpDir = DEFAULT_DUMP_DIR;
    protected String language = "ru";
    protected String domain = ".wikipedia.org";
    protected String scriptPath = "/w";
    protected String protocol = "https://";
    @LocalizedBySettings
    protected String comment = "обновление";
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
    
    /**
     * Print help text about bot usage to stdout.
     */
    public void showUsage() {
        
    }

    /**
     * Prints basic information about bot (name, version, copyright, purpose) when the bot starts
     * with {@link #run(String[])} method. Override it to print information about your bot. 
     */
    public void showInfo() {
        System.out.print("BasicBot v1.2 Copyright (C) 2018 Dmitry Trofimovich (KIN)\n\n");
    }

    /**
     * Prints bot license information.
     */
    public void showLicense() {        
        System.out.print(LICENSE);
    }
    
    /**
     * Constructs bot instance with default flags.
     */
    public BasicBot() {
        
    }
    
    /**
     * Constructs bot instance with specific flags.
     */
    public BasicBot(int flags) {
        this.flags = flags;
    }

    /**
     * @return bot flags (bit mask).
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Run bot.
     * @param args command line arguments.
     */
    public int run(String[] args) {
        showInfo();
        if ((flags & FLAG_SHOW_LICENSE) != 0) {
            showLicense();
        }
        System.out.print("----------------------< BOT STARTED >-------------------------------\n");
        String configFile = getConfig(args);        
        System.out.println("Applying config file: " + configFile);
        Map<String, String> launchParams = getLaunchArgs(args);
        int exitCode = 0;
        try {
            startWithConfig(configFile, launchParams);
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
        HashMap<String, String> params = new HashMap<String, String>();
        for (int i = 1; i < args.length; i++) {
            if (!args[i].startsWith("-")) continue;
            String [] parts = args[i].substring(1).split("=", 2);
            String left = parts[0];
            // TODO: Unexpected default. Replace with empty string.
            String right = "1";
            if (parts.length == 2) {
                right = parts[1];
            }
            params.put(left, right);
        }
        return params;
    }

    /**
     * @return path to bot config file.
     */
    protected String getConfig(String[] args) {
        if (args.length == 0) {
            return DEBUG_BUILD ? "config_test.xml" : "config.xml";
        } else {
            return args[0];
        }
    }

    /**
     * Initialize and start bot.
     *
     * Reads and initialize bot settings. Initializes logging. Logs in to wiki site.
     * Calls {@link #go()} method.
     * 
     * NOTE: Users should usually call {@link #run()}. 
     */
    protected void startWithConfig(String cfg, Map<String, String> launchParams)
            throws BotFatalError {
        properties = new Properties();
        try {
            InputStream in = new FileInputStream(cfg);
            if (cfg.endsWith(".xml")) {
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
        if (login == null || pw == null || login.isEmpty() || pw.isEmpty()) {
            String accountFile = properties.getProperty("wiki-account-file");
            if (accountFile == null || accountFile.isEmpty()) {
                throw new BotFatalError("wiki-login or wiki-password or wiki-account-file is not "
                        + "specified in settings");
            }
            Properties loginProp = new Properties();
            try {
                InputStream in = new FileInputStream(accountFile);
                if (accountFile.endsWith(".xml")) {
                    loginProp.loadFromXML(in);
                } else {
                    loginProp.load(in);
                }
                in.close();
            } catch (FileNotFoundException e) {
                throw new BotFatalError("File " + accountFile + " not found", e);
            } catch (IOException e) {
                throw new BotFatalError("Failed to read " + accountFile, e);
            }
            login = loginProp.getProperty("wiki-login");
            if (login == null || login.isEmpty()) {
                throw new BotFatalError("wiki-login is not found in settings file " + accountFile);
            }
            pw = loginProp.getProperty("wiki-password");
            if (pw == null || pw.isEmpty()) {
                throw new BotFatalError("wiki-password is not found in settings file " +
                        accountFile);
            }
        }
        
        log.info("login={}, password=(not shown)", login);

        language = properties.getProperty("wiki-lang", language);
        log.info("language={}", language);
        domain = properties.getProperty("wiki-domain", domain);
        log.info("domain={}", domain);
        protocol = properties.getProperty("wiki-protocol", protocol);
        log.info("protocol={}", protocol);
        comment = properties.getProperty("update-comment", comment);
        maxLag = Integer.valueOf(properties.getProperty("wiki-maxlag", String.valueOf(maxLag)));
        throttleTimeMs = Integer.valueOf(properties.getProperty("wiki-throttle",
                String.valueOf(throttleTimeMs)));
        log.info("comment={}", comment);
        debugMode = properties.getProperty("debug-mode", debugMode ? YES : NO).equals(YES);
        log.info("DEBUG_MODE={}", debugMode);
        
        if (!loadCustomProperties(launchParams)) {
            log.fatal("Failed to load all required properties. Exiting...");
            return;
        }
        String domain = this.domain;
        if (domain.startsWith(".")) {
            domain = language + domain;
        }
        wiki = createWiki(domain, scriptPath, protocol, language);

        configureWikiBeforeLogin();
        
        log.info("Login to {} [login: {}, password: (not shown)]", domain, login);
        try {
            wiki.login(login, pw.toCharArray());
        } catch (FailedLoginException e) {
            throw new BotFatalError(String.format("Failed to login to %s, login: %s, password: %s",
                    domain, login, pw));
        } catch (IOException e) {
            throw new BotFatalError(e);
        }

        if (debugMode) {
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

    /**
     * @return main wiki object.
     */
    public NirvanaWiki getWiki() {
        return wiki;
    }

    protected void onInterrupted(InterruptedException exception) {
        
    }

    /**
     * Override it to customize {@link #wiki} before wiki.login() will be called.
     */
    protected void configureWikiBeforeLogin() {
        wiki.setMaxLag(maxLag);
        wiki.setThrottle(throttleTimeMs);
    }

    protected abstract void go() throws InterruptedException, BotFatalError;

    protected boolean loadCustomProperties(Map<String, String> launchParams)
            throws BotSettingsError {
        return true;
    }

    /**
     * Initialize logger.
     * 
     * Usually called automatically by startWithConfig().
     * Use this if you are using this class without startWithConfig() method.
     */
    @VisibleForTesting
    public void initLog() throws BotFatalError {
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

    /**
     * Init logger from tests.
     */
    @VisibleForTesting
    public static void initLogFromTest() {
        log = LogManager.getLogger(BasicBot.class.getName());
    }

    protected void logPortalSettings(Map<String, String> parameters) {
        LogUtils.logParametersMap(log, parameters);
    }
}
