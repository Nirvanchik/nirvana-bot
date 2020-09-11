/**
 *  @(#)NirvanaWiki.java
 *  Copyright © 2019 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.wiki;

import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.util.FileTools;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpCookie;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

/**
 * Wiki.java extensions for easy bot development.
 * The most important:
 * 1) logging via log4j
 * 2) debug mode: edit() saves old and new versions of page text to file instead of real editing.
 * 3) helper methods like allowBots() or isRedirect()
 * 4) extra methods to minimize repeatable code working with Wiki.java: prepend(), etc. 
 * based on Wiki.java version 0.33
 */
public class NirvanaWiki extends Wiki {
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String REDIRECT_NAME_RU = "перенаправление";
    private static final String SIGN = " ~~~~";

    protected static final String DEFULT_DUMP_FOLDER = "dump";
    
    private static final long serialVersionUID = -8745212681497644127L;

    protected static Logger log;
    protected static Log4jLoggerWrapper loggerWrapper;

    private final String language;
    private Localizer localizer;
    Pattern redirectPattern;
    private boolean dumpMode = false;
    private String dumpFolder = null;
    protected boolean logDomain = false;

    private boolean loggedIn = false;
    private String username;
    private char[] password;
    
    private static final String BOTS_TEMPLATE_RE_STR = "\\{\\{bots\\s*\\|([^\\}]+)\\}\\}";
    private static final Pattern BOTS_TEMPLATE_RE;
    private static final String ALLOWBOTS_RE_STR = "(\\{\\{(nobots|bots\\|([^\\}]+))\\}\\})";
    private static final Pattern ALLOWBOTS_RE;

    static {
        log = LogManager.getLogger(NirvanaWiki.class.getName());
        loggerWrapper = new Log4jLoggerWrapper(log);
        Wiki.setLogger(loggerWrapper);
        BOTS_TEMPLATE_RE = Pattern.compile(BOTS_TEMPLATE_RE_STR,
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        ALLOWBOTS_RE = Pattern.compile(ALLOWBOTS_RE_STR,
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL); 
    }

    // TODO (KIN): Use nice SLF4J instead of this monstrous wrapper.
    private static class Log4jLoggerWrapper extends java.util.logging.Logger {
        private static final Map<Level, org.apache.logging.log4j.Level> LEVELS =
                Collections.unmodifiableMap(
                        new HashMap<Level, org.apache.logging.log4j.Level>() {
                            private static final long serialVersionUID = 1L;
                        {
                            put(Level.ALL, org.apache.logging.log4j.Level.ALL);
                            put(Level.FINEST, org.apache.logging.log4j.Level.TRACE);
                            put(Level.FINE, org.apache.logging.log4j.Level.DEBUG);
                            put(Level.CONFIG, org.apache.logging.log4j.Level.INFO);
                            put(Level.INFO, org.apache.logging.log4j.Level.INFO);
                            put(Level.WARNING, org.apache.logging.log4j.Level.WARN);
                            put(Level.SEVERE, org.apache.logging.log4j.Level.ERROR);
                            put(Level.OFF, org.apache.logging.log4j.Level.OFF);
                        }
                        });
        protected Logger log;

        protected Log4jLoggerWrapper(Logger log) {
            super("Wiki", null);
            this.log = log;
        }

        @Override        
        public void log(Level level,
                   String msg) {
            org.apache.logging.log4j.Level log4jLevel =
                    LEVELS.getOrDefault(level, org.apache.logging.log4j.Level.TRACE);
            log.log(log4jLevel, msg);
        }

        @Override
        public void log(Level level,
                   String msg,
                   Object param1) {
            org.apache.logging.log4j.Level log4jLevel =
                    LEVELS.getOrDefault(level, org.apache.logging.log4j.Level.TRACE);
            log.log(log4jLevel, msg, param1);            
        }

        @Override
        public void log(Level level,
                   String msg,
                   Object[] params) {
            org.apache.logging.log4j.Level log4jLevel =
                    LEVELS.getOrDefault(level, org.apache.logging.log4j.Level.TRACE);

            log.log(log4jLevel, sanitizeLogMessage(msg, params), params);
        }

        @Override
        public void logp(Level level,
                String sourceClass,
                String sourceMethod,
                String msg) {
            org.apache.logging.log4j.Level log4jLevel =
                    LEVELS.getOrDefault(level, org.apache.logging.log4j.Level.TRACE);
            String mess = "[" + sourceClass + "#" + sourceMethod + "] " + msg;
            log.log(log4jLevel, mess);
        }

        @Override
        public void logp(Level level,
                String sourceClass,
                String sourceMethod,
                String msg,
                Object param1) {
            org.apache.logging.log4j.Level log4jLevel =
                    LEVELS.getOrDefault(level, org.apache.logging.log4j.Level.TRACE);
            String mess = "[" + sourceClass + "#" + sourceMethod + "] " + msg;
            log.log(log4jLevel, mess, param1);
        }

        @Override
        public void logp(Level level,
                String sourceClass,
                String sourceMethod,
                String msg,
                Object[] params) {
            org.apache.logging.log4j.Level log4jLevel =
                    LEVELS.getOrDefault(level, org.apache.logging.log4j.Level.TRACE);
            String mess = "[" + sourceClass + "#" + sourceMethod + "] " + msg;
            log.log(log4jLevel, sanitizeLogMessage(mess, params), params);
        }

        private String sanitizeLogMessage(String msg, Object[] params) {
            String msgLog4j = msg.replace("{0}", "{}").replace("{1}", "{}").replace("{2}", "{}")
                    .replace("{3}", "{}").replace("{4}", "{}").replace("{5}", "{}")
                    .replace("{6}", "{}").replace("{7}", "{}").replace("{8}", "{}")
                    .replace("{9}", "{}");
            if (StringUtils.countMatches(msgLog4j, "{}") != params.length) {
                log.warn("Cannot convert next log message to log4j compatible format.");
                msgLog4j = msg;
            }
            return msgLog4j;
        }

    }

    /**
     * Public constructor.
     *
     * @param domain Wiki site domain. Example: "en.wikipedia.org".
     */
    public NirvanaWiki(String domain) {
        super(domain);
        language = DEFAULT_LANGUAGE;
    }
    
    /**
     * Public constructor.
     *
     * @param domain Wiki site domain. Example: "en.wikipedia.org".
     * @param scriptPath Wiki site API path. Example: "/w".
     */
    public NirvanaWiki(String domain, String scriptPath) {
        super(domain, scriptPath);
        language = DEFAULT_LANGUAGE;
    }

    /**
     * Public constructor.
     *
     * @param domain Wiki site domain. Example: "en.wikipedia.org".
     * @param scriptPath Wiki site API path. Example: "/w".
     * @param protocol Protocol to use ("https://", "http://").
     */
    public NirvanaWiki(String domain, String scriptPath, String protocol) {
        this(domain, scriptPath, protocol, DEFAULT_LANGUAGE);
    }

    /**
     * Public constructor.
     *
     * @param domain Wiki site domain. Example: "en.wikipedia.org".
     * @param scriptPath Wiki site API path. Example: "/w".
     * @param protocol Protocol to use ("https://", "http://").
     * @param language Wiki site language. Language is used for correct work of isRedirect() method
     *         which is language-dependent.
     */
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

    /**
     * Enables dump mode. In dump mode edit() method doesn't do real edit. It writes old and updated
     * versions of the page to a file (dump) in specified folder.
     *
     * @param folder Folder path where dumps will be written.
     */
    public void setDumpMode(String folder) {
        dumpMode = true;
        dumpFolder = folder;
    }

    /**
     *  Logs a successful result.
     *  @param text string the string to log
     *  @param method what we are currently doing
     *  @param level the level to log at
     *  @since 0.06
     */
    protected void log(Level level, String method, String text) {
        StringBuilder sb = new StringBuilder(100);
        if (logDomain) {
            sb.append('[');
            sb.append(domain);
            sb.append("] ");
        }
        sb.append(method);
        sb.append("() - ");
        sb.append(text);
        sb.append('.');
        if (level == Level.SEVERE) {
            log.error(sb.toString());
        } else if (level == Level.WARNING) {
            log.warn(sb.toString());
        } else if (level == Level.CONFIG || level == Level.INFO) {
            log.info(sb.toString());
        } else if (level == Level.FINE) {
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
     */
    protected void log(Level level, String method, String text, Exception ex) {
        StringBuilder sb = new StringBuilder(100);
        if (logDomain) {
            sb.append('[');
            sb.append(domain);
            sb.append("] ");
        }
        sb.append(method);
        sb.append("() - ");
        sb.append(text);
        sb.append('.');
        if (level == Level.SEVERE) {
            log.error(sb.toString(), ex);
        } else if (level == Level.WARNING) {
            log.warn(sb.toString(), ex);
        } else if (level == Level.CONFIG || level == Level.INFO) {
            log.info(sb.toString(), ex);
        } else if (level == Level.FINE) {
            log.debug(sb.toString(), ex);
        } else {
            log.trace(sb.toString(), ex);
        }
    }

    /**
     *  Logs a url fetch.
     *  @param url the url we are fetching
     *  @param method what we are currently doing
     *  @since 0.08
     */
    protected void logurl(String url, String method) {
        log.debug(method + "() - fetching URL: " + url);
    }
    
    @Override
    public synchronized void edit(String title, String text, String summary, boolean minor,
            boolean bot, int section, OffsetDateTime basetime) throws IOException, LoginException {
        if (!this.dumpMode) {
            super.edit(title, text, summary, minor, bot, section, basetime);
        } else {
            String fileNew = title + ".new.txt";
            String fileOld = title + ".old.txt";
            String old = null;
            try {
                old = this.getPageText(title);
            } catch (FileNotFoundException e) {
                old = "";
            }
            if (old == null) {
                old = "";
            }
            FileTools.dump(old, dumpFolder, fileOld);
            FileTools.dump(text, dumpFolder, fileNew);
        }
    }

    /**
     * Edits a page by setting its text to the supplied value.
     * The same as {@link #edit(String, String, String, boolean, boolean, int, Calendar)} but
     * with default basetime and section arguments. Basetime is null and section is whole text.
     */
    public void edit(String title, String text, String summary, boolean minor, boolean bot)
            throws IOException, LoginException {
        edit(title, text, summary, minor, bot, -2, null);
    }
    
    /**
     * Edits a page by setting its text to the supplied value.
     * The same as {@link #edit(String, String, String, boolean, boolean, int, Calendar)} but
     * with default basetime, section, minor, and bot arguments. Basetime is null, section is whole
     * text, minor is false, bot is true.
     */
    public void edit(String title, String text, String summary) throws IOException, LoginException {
        edit(title, text, summary, false, true, -2, null);
    }

    /**
     * Edits page only if page text was changed. First it gets current page text, then compares it
     * with a new one and then edits if required.
     *
     * @param title the title of the page
     * @param text the text of the page
     * @param summary the edit summary or the title of the new section. See
     *     [[Help:Edit summary]]. Summaries longer than 200 characters are
     *     truncated server-side.
     * @param minor whether the edit should be marked as minor, See [[Help:Minor edit]].
     * @param bot whether to mark the edit as a bot edit (ignored if one does not have the necessary
     *     permissions)
     * @return <code>true</code> if edit was done.
     */
    public boolean editIfChanged(String title, String text, String summary, boolean minor,
            boolean bot) throws IOException, LoginException {
        String old = "";
        try {
            old = this.getPageText(title);
        } catch (FileNotFoundException e) {
            // ignore. all job is done not here :)
        }
        if (old == null) {
            old = "";
        }
        // to compare lengths is faster than comparing 5k chars
        if (old.length() != text.length() || !old.equals(text)) {
            log.debug("Editing {}", title);
            edit(title, text, summary, minor, bot, -2, null);
            return true;
        } else {
            log.debug("Skip updating {} (no changes)", title);
        }
        return false;
    }

    /**
     * Add string at the beginning of wiki page.
     *
     * @param title The title of the page.
     * @param stuff String to add at the beginning.
     * @param minor whether the edit should be marked as minor, See [[Help:Minor edit]].
     * @param bot whether to mark the edit as a bot edit (ignored if one does not have the necessary
     *     permissions)
     */
    public void prepend(String title, String stuff, boolean minor, boolean bot)
            throws IOException, LoginException {
        prepend(title, stuff, "+" + stuff, minor, bot);
    }
    
    /**
     * Add string at the beginning of wiki page.
     *
     * @param title The title of the page.
     * @param stuff String to add at the beginning.
     * @param comment Edit comment (summary).
     * @param minor whether the edit should be marked as minor, See [[Help:Minor edit]].
     * @param bot whether to mark the edit as a bot edit (ignored if one does not have the necessary
     *     permissions)
     */
    public void prepend(String title, String stuff, String comment, boolean minor, boolean bot)
            throws IOException, LoginException {
        StringBuilder text = new StringBuilder(100000);
        text.append(stuff);
        String oldText = getPageText(title);
        if (oldText == null) {
            throw new IOException(String.format("Page %s does not exist.", title));
        }
        text.append(oldText);
        this.edit(title, text.toString(), comment, minor, bot);
    }

    /**
     * Add string at the beginning of wiki page. Create it new page if it dosn't exist.
     *
     * @param title The title of the page.
     * @param stuff String to add at the beginning.
     * @param minor whether the edit should be marked as minor, See [[Help:Minor edit]].
     * @param bot whether to mark the edit as a bot edit (ignored if one does not have the necessary
     *     permissions)
     */
    public void prependOrCreate(String title, String stuff, boolean minor, boolean bot)
            throws IOException, LoginException {
        prependOrCreate(title, stuff, "+" + stuff, minor, bot);
    }

    /**
     * Add string at the beginning of wiki page. Create it new page if it dosn't exist.
     *
     * @param title The title of the page.
     * @param stuff String to add at the beginning.
     * @param comment Edit comment (summary).
     * @param minor whether the edit should be marked as minor, See [[Help:Minor edit]].
     * @param bot whether to mark the edit as a bot edit (ignored if one does not have the necessary
     *     permissions)
     */
    public void prependOrCreate(String title, String stuff, String comment, boolean minor,
            boolean bot) throws IOException, LoginException {
        log.debug("prependOrCreate() -> Page {} will be appended with text size = {}", title,
                stuff.length());
        if (stuff.isEmpty()) return;
        StringBuilder text = new StringBuilder(100000);
        text.append(stuff);
        String oldText = null;
        try {
            oldText = getPageText(title);
        } catch (FileNotFoundException e) {
            // Ignore
        }
        if (oldText != null) {
            text.append(oldText);
        } else {
            log.debug("Page {} does not exist. It will be created.", title);
        }
        this.edit(title, text.toString(), comment, minor, bot);
    }

    /**
     * Add string at the end of wiki page.
     *
     * @param title The title of the page.
     * @param stuff String to add at the beginning.
     * @param comment Edit comment (summary).
     * @param minor whether the edit should be marked as minor, See [[Help:Minor edit]].
     * @param bot whether to mark the edit as a bot edit (ignored if one does not have the necessary
     *     permissions)
     */
    public void append(String title, String stuff, String comment, boolean minor, boolean bot)
            throws IOException, LoginException {
        StringBuilder text = new StringBuilder(100000);
        String oldText = getPageText(title);
        if (oldText == null) {
            throw new IOException(String.format("Page %s does not exist.", title));
        }
        text.append(oldText);
        text.append(stuff);
        this.edit(title, text.toString(), comment, minor, bot);
    }

    /**
     * Add string at the end of wiki page. Create it new page if it dosn't exist.
     *
     * @param title The title of the page.
     * @param stuff String to add at the beginning.
     * @param comment Edit comment (summary).
     * @param minor whether the edit should be marked as minor, See [[Help:Minor edit]].
     * @param bot whether to mark the edit as a bot edit (ignored if one does not have the necessary
     *     permissions)
     */
    public void appendOrCreate(String title, String stuff, String comment, boolean minor,
            boolean bot) throws IOException, LoginException {
        if (stuff.isEmpty()) return;
        StringBuilder text = new StringBuilder(100000);
        String oldText = null;
        try {
            oldText = getPageText(title);
        } catch (FileNotFoundException e) {
            // Ignore
        }
        if (oldText != null) {
            text.append(oldText);
        } else {
            log.debug("Page {} does not exist. It will be created.", title);
        }
        text.append(stuff);
        this.edit(title, text.toString(), comment, minor, bot);
    }

    /**
     * @return <code>true</code> if the specified page is a redirect.
     */
    public boolean isRedirect(String article) {
        checkRedirectPattern();
        Matcher m = redirectPattern.matcher(article);
        return m.matches();
    }

    /**
     * Find out if it is allowed to edit this page by current user. Users can forbid editing any
     * page using Bots or Nobots template for all bots, or for specified bots.
     * See [[Template:Bots]], [[Template:Nobots]] for more details.
     *
     * @param text Wiki text of wiki page.
     * @return <code>true</code> if editing is allowed.
     */
    public boolean allowEditsByCurrentBot(String text) {
        return NirvanaWiki.allowBots(text, this.getCurrentUser().getUsername());
    }

    /**
     * Find out if it is allowed to edit this page by specified user. Users can forbid editing any
     * page using Bots or Nobots template for all bots, or for specified bots.
     * See [[Template:Bots]], [[Template:Nobots]] for more details.
     *
     * @param text Wiki text of wiki page.
     * @param user User (bot) name.
     * @return <code>true</code> if editing is allowed.
     */
    public static boolean allowBots(String text, String user) {
        if (text.contains("{{nobots") || text.contains("{{Nobots")) {
            return false;
        }
        Matcher m = BOTS_TEMPLATE_RE.matcher(text);
        if (m.find()) {
            String paramsString = m.group(1);
            String [] params = paramsString.split("\\|");
            for (String param: params) {
                if (param.contains("allow=none") || param.contains("deny=all") ||
                        param.contains("optout=all")) {
                    return false;
                }
                if (param.contains("allow=all") || param.contains("deny=none")) {
                    return true;
                }
                if (param.trim().startsWith("deny=")) {
                    String value = param.trim().replace("deny=", "").trim();
                    String [] userList = value.split("\\s*,\\s*");
                    if (Arrays.asList(userList).contains(user)) {
                        return false;
                    }
                }
                if (param.trim().startsWith("allow=")) {
                    String value = param.trim().replace("allow=", "").trim();
                    String [] userList = value.split("\\s*,\\s*");
                    if (!Arrays.asList(userList).contains(user)) {
                        return false;
                    }
                }
            }
        }        
        return true;  
    }

    /**
     * Gets allow-bots string found in page text or null if not found. Allow-bots string is a
     * wiki code with [[Bots]] or [[Nobots]] template.
     * See https://ru.wikipedia.org/wiki/Шаблон:Nobots.
     * See https://ru.wikipedia.org/wiki/Шаблон:Bots
     * See https://en.wikipedia.org/wiki/Template:Bots
     *
     * @param text Wiki text of the page.
     * @return Allow-bots string or <code>null</code>.
     */
    @Nullable
    public static String getAllowBotsString(String text) {
        Matcher m = ALLOWBOTS_RE.matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    /**
     * Get page text split by lines.
     *
     * @param title Title of the page.
     * @return page contents put in array of lines. 
     */
    public String [] getPageLinesArray(String title) throws IOException {
        String text = getPageText(title);
        if (text == null) {
            return new String[0];
        }
        return text.split("\n");
    }

    /**
     * Get page text split by lines.
     *
     * @param title Title of the page.
     * @return page contents put in list of strings. 
     */
    public List<String> getPageLines(String title) throws IOException {
        return Arrays.asList(getPageLinesArray(title));
    }
 
    /**
     *  Gets the first revision of a page.
     *  @param title a page
     *  @return the oldest revision of that page
     *  @throws IOException if a network error occurs
     *  @since 0.24
     */
    public Revision getFirstRevision(String title, boolean resolveRedirect) throws IOException {
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
    public Revision getTopRevisionWithNewTitle(String title, boolean resolveRedirect)
            throws IOException {
        boolean was = isResolvingRedirects();
        this.setResolveRedirects(resolveRedirect);
        Revision r = super.getTopRevision(title);
        setResolveRedirects(was);
        return r;
    }

    /**
     * Check if the specified page exists in wiki.
     *
     * @param title Title of the page.
     * @return <code>true</code> if exists.
     */
    public boolean exists(String title) throws IOException {
        return exists(new String[] { title })[0];
    }

    /**
     * Get page history in the specified time range.
     *
     * @param title Title of the page.
     * @param start the EARLIEST of the two dates
     * @param end the LATEST of the two dates
     * @return Array of revisions made in this time range.
     */
    @Deprecated
    public Revision[] getPageHistory(String title, Calendar start, Calendar end)
            throws IOException {
        // TODO: Migrate to Java8 dates.
        OffsetDateTime odtStart = OffsetDateTime.ofInstant(start.toInstant(),
                ZoneId.systemDefault());
        OffsetDateTime odtEnd = OffsetDateTime.ofInstant(end.toInstant(),
                ZoneId.systemDefault());
        return getPageHistory(title, odtStart, odtEnd, false);
    }

    /**
     * Gets the revision history of a page between two dates.
     * @param title a page
     * @param start the EARLIEST of the two dates
     * @param end the LATEST of the two dates
     * @return the revisions of that page in that time span
     * @throws IOException or UncheckedIOException if a network error occurs
     * @throws UnsupportedOperationException if <var>title</var> is a Special or Media page
     */
    public Revision[] getPageHistory(String title, OffsetDateTime start, OffsetDateTime end)
            throws IOException {
        return getPageHistory(title, start, end, false);
    }

    /**
     * Get namespace prefixed page title.
     *
     * @param namespace Namespace integer id.
     * @param name Title of the page.
     * @return Title of the page with namespace name. Example: [[Template:Bots]].
     */
    public String prefixedName(int namespace, String name) throws IOException {
        assert namespace != Wiki.MAIN_NAMESPACE;
        return namespaceIdentifier(namespace) + ":" + name;
    }

    /**
     * Gets page text that the page had before the specified date.
     * Returns null if page did not exist before that date.
     *
     * @param title Title of the page.
     * @param date Date.
     * @return Page text before that <code>date</code> or null.
     */
    @Nullable
    public String getPageTextBefore(String title, Calendar date) throws IOException {
        Revision r = getPageRevisionBefore(title, date);
        if (r == null) return null;
        return r.getText();
    }

    /**
     * Searches for last page revision made before specified date.
     * Returns <code>null</code> if not found.
     *
     * @param title Title of the page.
     * @param date Date.
     * @return Last revision before <code>date</date> or null.
     */
    @Nullable
    public Revision getPageRevisionBefore(String title, Calendar date) throws IOException {
        // pitfall check
        if (namespace(title) < 0) {
            throw new UnsupportedOperationException("Cannot retrieve Special: or Media: pages!");
        }
        // TODO: Switch to Java8 dates.
        OffsetDateTime odtEnd = OffsetDateTime.ofInstant(date.toInstant(),
                ZoneId.systemDefault());
        Revision [] revs = getPageHistory(title, null, odtEnd, false);
        if (revs.length == 0) {
            return null;
        }
        Revision r = revs[0];  
        return r;
    }

    /**
     * Adds a new topic to a specified page in Wiki.
     *
     * @param page Page title where to add new topic.
     * @param message Message that will be added.
     * @param addSignature If <code>true</code> signature (~~~~) will be added after message.
     * @param noDuplicate If <code>true</code> message will not be added to the page if the latter
     *        has it already.
     * @param summary Edit summary.
     */
    public void addTopicToDiscussionPage(String page, String message, boolean addSignature,
            boolean noDuplicate, String summary) throws IOException, LoginException {
        String discussion = null;
        try {
            discussion = getPageText(page);
        } catch (FileNotFoundException e) {
            // ignore
        }
        if (noDuplicate && discussion != null && discussion.contains(message)) {
            log.info("Message already exists at {}", page);
            return;
        }
        if (discussion != null && !allowEditsByCurrentBot(discussion)) {
            log.info("People don't allow edits for this bot on the page {}", page);
            return;
        }
        if (addSignature) message = message + SIGN + '\n';
        discussion = WikiUtils.addTextToDiscussion(message, discussion);
        edit(page, discussion, summary);
    }
    
    /**
     *  Overridden method. Log in to the wiki. 
     *
     *  @param username a username
     *  @param password a string with the password
     */
    @Override
    public synchronized void login(String username, String password) throws IOException,
            FailedLoginException {
        this.login(username, password.toCharArray());
    }


    /**
     * Overridden method. Log in to the wiki.
     */
    @Override
    public synchronized void login(String username, char[] password) throws IOException,
            FailedLoginException {
        this.username = username;
        this.password = password;
        char[] tempPassword = Arrays.copyOf(password, password.length);
        // This will erase data in tempPassword - that is why not password
        super.login(username, tempPassword);
        loggedIn = true;
    }

    @Override
    public synchronized void logout() {
        super.logout();
        this.loggedIn = false;
    }

    /**
     * Relogin (log out and immediately log in).
     * Call it when there was no interaction with Wiki for too long.
     */
    public synchronized void relogin() throws FailedLoginException, IOException {
        this.logout();
        this.login(this.username, this.password);
    }

    @Override
    public String getToken(String type) throws IOException {
        String token = super.getToken(type);
        if (loggedIn && type.equals("csrf")) {
            if (token.isEmpty() || token.equals("+\\")) {
                throw new RuntimeException("Unexpected Dislogin");
            }
        }
        return token;
    }

    /**
     * Print cookies to log. Used for critical problems (dislogin) debug.
     */
    public void dumpCookies() {
        List<HttpCookie> cookieList = cookies.getCookieStore().getCookies();
        log.info("Show cookies for wiki: {}", this.domain);
        for (HttpCookie coo: cookieList) {
            log.info("cookie {} : {}", coo.getName(), coo.getValue());
        }
    }
}
