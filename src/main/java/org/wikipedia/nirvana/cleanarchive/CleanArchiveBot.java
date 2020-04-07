/**
 *  @(#)CleanArchiveBot.java 28.05.2018
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

package org.wikipedia.nirvana.cleanarchive;

import org.wikipedia.Wiki;
import org.wikipedia.nirvana.BasicBot;
import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.nirvanabot.BotFatalError;
import org.wikipedia.nirvana.nirvanabot.NewPages;
import org.wikipedia.nirvana.util.FileTools;
import org.wikipedia.nirvana.util.TextUtils;
import org.wikipedia.nirvana.wiki.WikiBooster;

import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.security.auth.login.LoginException;

/**
 * Bot that removes specified list of pages from specified new pages archives.
 * See {@link #PROGRAM_INFO} value for more details about this class.
 */
public class CleanArchiveBot extends BasicBot {
    public static final String YES_RU = "да";
    public static final String NO_RU = "нет";

    private String taskFile;

    private static final boolean DEBUG = false;
    private static final Method DEFAULT_METHOD = Method.METHOD_1;
    private static final boolean DEFAULT_INCLUDE_REDIRECTS = true;

    enum Format {
        TXT,
        TSV
    }

    enum Method {
        METHOD_1("1"),
        METHOD_2("2"),
        METHOD_3("3");

        public final String name;

        Method(String name) {
            this.name = name;
        }
    }

    private static class Task {
        public final String badPagesFile;
        public final String archive;
        public final String separator;
        public final Format format;
        public final Method method;
        public final boolean includeRedirects;

        public Task(String badPagesFile, String archive, String separator, Format format,
                Method method, boolean redirects) {
            this.badPagesFile = badPagesFile;
            this.archive = archive;
            this.separator = separator;
            this.format = format;
            this.method = method;
            this.includeRedirects = redirects;
        }        
    }

    /**
     * Default constructor.
     */
    public CleanArchiveBot() {
        // Empty
    }

    /**
     * Constructor with flags parameter.
     */
    public CleanArchiveBot(int flags) {
        super(flags);
    }

    @Override
    public void showUsage() {
        
    }

    private static final String VERSION = "v0.01";

    public static String PROGRAM_INFO = 
            "CleanArchiveBot " + VERSION + " a bot for http://ru.wikipedia.org\n" +
            "Removes \"bad\" (irrelevant) articles from archive (or archive group).\n" +
            "\"Bad\" articles should be given as a file with list of new line separated items,\n" +
            "archive should be given as full page name, archive group should be given as title " +
            "prefix.\n" +
            "Copyright (C) 2018 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)" +
            "\n";

    @Override
    public void showInfo() {
        System.out.print(PROGRAM_INFO);
    }

    @Override
    protected boolean loadCustomProperties(Map<String,String> launchParams) {
        taskFile = properties.getProperty("task-file");
        if (taskFile == null) {
            log.error("You must specify \"task-file\" in xml config.");
            return false;
        }
        log.info("Task file: {}", taskFile);
        return true;
    }

    /* (non-Javadoc)
     * @see org.wikipedia.nirvana.BasicBot#go()
     */
    @Override
    protected void go() throws InterruptedException, BotFatalError {
        Localizer.init(Localizer.NO_LOCALIZATION);
        NewPages.initStatics();

        log.info("Read task information in {}", this.taskFile);
        Task task = readTask(this.taskFile);
        log.info("Read pages list in {}", task.badPagesFile);
        List<String> pages;
        try {
            pages = readPages(task.badPagesFile, task.format);
        } catch (IOException e) {
            throw new BotFatalError(String.format("Failed to read %1$s", task.badPagesFile));
        }
        log.info("{} pages{} will be checked.", pages.size(),
                task.includeRedirects ? " (with redirects)" : "");
        log.info("Method {} selecled.", task.method.name);
        switch (task.method) {
            case METHOD_1:
                goMethod1(task, pages);
                break;
            case METHOD_2:
                goMethod2(task, pages);
                break;
            case METHOD_3:
                goMethod3(task, pages);
                break;
            default:
                throw new BotFatalError("Unknown method");
        }

    }

    private void goMethod1(Task task, List<String> pages)
            throws InterruptedException, BotFatalError {
        int namespace = getNamespaceFromPagePrefix(task.archive);
        log.debug("Namepspace for {} is {}", task.archive, namespace);
        int num = 0;
        if (pages.size() == 0) {
            log.warn("File list {} is empty", task.badPagesFile);
        }
        HashMap<String, List<String>> cleanArchives = new HashMap<>();
        log.info("--------------------------");
        log.info("Search articles to remove");
        int counter = 0;
        for (String page: pages) {            
            num++;
            log.info("Analyze page {} of {}: {}", num, pages.size(), page);
            String [] redirects;
            try {
                redirects = wiki.whatLinksHereForked(page, true, false, 0);
            } catch (IOException e) {
                log.error("Failed to get redirects for page {}: {}", page, e);
                throw new BotFatalError(e);
            }
            List<String> pageAndRedirects = new ArrayList<>(Arrays.asList(redirects));
            pageAndRedirects.add(page);
            for (String toCheck: pageAndRedirects) {
                log.debug("Check page: {}", toCheck);
                String [] whatLinks;
                try {
                    whatLinks = wiki.whatLinksHere(toCheck, namespace);
                } catch (IOException e) {
                    log.error("Failed to get \"what links here\" pages for {}", toCheck);
                    throw new BotFatalError(e);
                }
                for (String link: whatLinks) {
                    if (link.startsWith(task.archive)) {
                        log.info("Shedule to remove {} from {}", toCheck, link);
                        List<String> toRemove;
                        if (cleanArchives.containsKey(link)) {
                            toRemove = cleanArchives.get(link); 
                        } else {
                            toRemove = new ArrayList<>();
                            cleanArchives.put(link, toRemove);
                        }
                        toRemove.add(toCheck);
                        counter++;
                    }
                }
            }
            if (DEBUG && num == 10) break;
        }
        if (counter == 0) {
            log.warn("Nothing found to remove.");
            return;
        }
        log.info("--------------------------");
        log.info("Remove {} pages from {} archive pages", counter, cleanArchives.size());
        clean(cleanArchives, task);
    }

    private void goMethod2(Task task, List<String> pages)
            throws InterruptedException, BotFatalError {
        int namespace = getNamespaceFromPagePrefix(task.archive);
        log.debug("Namepspace for {} is {}", task.archive, namespace);
        int num = 0;
        if (pages.size() == 0) {
            log.warn("File list {} is empty", task.badPagesFile);
        }
        HashMap<String, List<String>> cleanArchives = new HashMap<>();
        log.info("--------------------------");
        log.info("Search articles to remove");
        int counter = 0;
        for (String page: pages) {
            num++;
            log.info("Analyze page {} of {}: {}", num, pages.size(), page);
            List<String> pageAndRedirects = new ArrayList<>();
            pageAndRedirects.add(page);
            if (task.includeRedirects) {
                String [] redirects;
                try {
                    redirects = wiki.whatLinksHereForked(page, true, false, 0);
                } catch (IOException e) {
                    log.error("Failed to get redirects for page {}: {}", page, e);
                    throw new BotFatalError(e);
                }
                pageAndRedirects.addAll(Arrays.asList(redirects));
            }
            
            String [] whatLinks;
            try {
                whatLinks = wiki.whatLinksHereForked(page, false, true, namespace);
            } catch (IOException e) {
                log.error("Failed to get \"what links here\" pages for {}", page);
                throw new BotFatalError(e);
            }
            for (String link: whatLinks) {
                if (link.startsWith(task.archive)) {
                    log.info("Shedule to remove {} from {}", page, link);
                    List<String> toRemove;
                    if (cleanArchives.containsKey(link)) {
                        toRemove = cleanArchives.get(link); 
                    } else {
                        toRemove = new ArrayList<>();
                        cleanArchives.put(link, toRemove);
                    }
                    toRemove.addAll(pageAndRedirects);
                    counter++;
                }
            }
            if (DEBUG && num == 10) break;
        }
        if (counter == 0) {
            log.warn("Nothing found to remove.");
            return;
        }
        log.info("--------------------------");
        log.info("Remove {} pages from {} archive pages", counter, cleanArchives.size());
        clean(cleanArchives, task);
        
    }

    private void goMethod3(Task task, List<String> pages)
            throws InterruptedException, BotFatalError {
        int namespace = getNamespaceFromPagePrefix(task.archive);
        log.debug("Namepspace for {} is {}", task.archive, namespace);
        if (pages.size() == 0) {
            log.warn("No pages found in {} to check. Exit.", task.badPagesFile);
            return;
        }
        String [] archives;
        try {
            archives = wiki.listPages(task.archive, null, namespace);
        } catch (IOException e) {
            log.error("Failed to get pages with prefix {}", task.archive);
            throw new BotFatalError(e);
        }
        WikiBooster booster = new WikiBooster(wiki, archives);
        ArrayList<String> toCheck = new ArrayList<String>(pages);
        if (task.includeRedirects) {
            log.info("Get redirects for {} pages", pages.size());
            for (String page: pages) {
                String [] redirects;
                try {
                    redirects = wiki.whatLinksHereForked(page, true, false, 0);
                } catch (IOException e) {
                    log.error("Failed to get redirects for page {}: {}", page, e);
                    throw new BotFatalError(e);
                }
                toCheck.addAll(Arrays.asList(redirects));
            }
        }
        HashMap<String, List<String>> cleanArchives = new HashMap<>();
        List<String> archiveList = Arrays.asList(archives);
        Collections.sort(archiveList);
        int counter = 0;
        log.info("Try to remove {} pages from {} archive pages", toCheck.size(),
                archiveList.size());
        for (String archive: archiveList) {
            log.info("Search bad pages in archive: {}", archive);
            ArrayList<String> badPages = new ArrayList<>();
            String text;
            try {
                text = booster.getPageText(archive);
            } catch (IOException e) {
                log.error("Failed to get text for page: {}", archive);
                throw new BotFatalError(e);
            }
            for (String page: toCheck) {
                if (text.contains(page)) {
                    log.debug("Found {} in {}", page, archive);
                    badPages.add(page);
                }
            }
            log.info("{} probably bad pages found in {}", badPages.size(), archive);
            if (badPages.size() > 0) {                
                cleanArchives.put(archive, badPages);
            }
            counter += badPages.size();
        }
        log.info("--------------------------");
        log.info("Remove {} pages from {} archive pages", counter, cleanArchives.size());
        clean(cleanArchives, task, booster);
    }

    private void clean(Map<String, List<String>> cleanArchives, Task task) throws BotFatalError {
        WikiBooster booster = new WikiBooster(wiki, new ArrayList<>(cleanArchives.keySet()));
        clean(cleanArchives, task, booster);
    }

    private void clean(Map<String, List<String>> cleanArchives, Task task, WikiBooster booster)
            throws BotFatalError {
        int num = 0;
        List<String> archives = new ArrayList<>(cleanArchives.keySet());
        Collections.sort(archives);
        int total = 0;
        for (String archive: archives) {
            List<String> badPages = cleanArchives.get(archive);
            num++;
            int counter = 0;
            log.info("Remove {} probably bad pages from {} ({} of {})", badPages.size(), archive,
                    num, cleanArchives.size());
            String text;
            try {
                text = booster.getPageText(archive);
            } catch (IOException e) {
                log.error("Failed to get text of {}: {}", archive, e);
                throw new BotFatalError(e);
            }
            String[] parts = text.split(task.separator);
            List<String> updatedParts = new ArrayList<>();
            for (String part: parts) {
                boolean good = true;
                for (String badPage: badPages) {
                    if (part.contains(badPage)) {
                        if (badPage.equals(NewPages.getNewPagesItemArticle(part))) {
                            good = false;
                            counter++;
                            log.info("Remove line: \"{}\" because it contains \"{}\"", part,
                                    badPage);
                        }
                    }
                }
                if (good) {
                    updatedParts.add(part);
                }
            }
            log.info("{} lines removed from {}", counter, archive);
            if (counter > 0) {
                text = StringUtils.join(updatedParts, task.separator);
                // It's OK to end every wiki page with new line character
                // If you don't add it then Mediawiki will add it anyway.
                if (!text.endsWith("\n")) {
                    text += "\n";
                }

                try {
                    wiki.edit(archive, text, COMMENT);
                } catch (LoginException | IOException e) {
                    log.error("Failed to update page {}", archive);
                    throw new BotFatalError(e);
                }
                total += counter;
            }
        }
        log.info("{} lines removed in all", total);
    }

    private List<String> readPages(String fileName, Format format)
            throws FileNotFoundException, IOException {
        if (format.equals(Format.TXT)) {
            return FileTools.readFileToList(fileName);
        } else if (format.equals(Format.TSV)) {
            return readPagesFromTsv(fileName);
        } else {
            throw new IllegalStateException("Unexpected format " + format.toString());
        }
    }

    private List<String> readPagesFromTsv(String fileName)
            throws FileNotFoundException, IOException {
        List<String> pages = new ArrayList<>();
        List<String> lines = FileTools.readFileToList(fileName);
        int x = 0;
        for (String line: lines) {
            String [] items = line.split("\t");
            if (items.length > 1) {
                pages.add(items[1].replace("_", " "));
            }
            x++;
            if (DEBUG && x == 100) break;
        }
        return pages;
    }

    private int getNamespaceFromPagePrefix(String pagePrefix) throws BotFatalError {
        int namespace;
        try {
            namespace = wiki.namespace(pagePrefix);
        } catch (IOException e) {
            log.error("Failed to get namespace for {}", pagePrefix);
            throw new BotFatalError(e);
        }
        if (namespace == Wiki.MAIN_NAMESPACE) {
            throw new BotFatalError(
                    String.format("Namespace of archive %1$s is main. This must not be so.",
                            pagePrefix));
        }
        return namespace;
    }

    private Task readTask(String taskFile) throws BotFatalError {
        String task = FileTools.readFileSilently(taskFile);
        if (task == null) {
            throw new BotFatalError(String.format("Failed to read %1$s", taskFile));
        }
        Map<String, String> options = new HashMap<>();
        if (!TextUtils.textOptionsToMap(task, options)) {
            throw new BotFatalError(String.format("Invalid text in %1$s:\n%2$s", taskFile, task));
        }
        String archive = readRequiredKey("архив", options);
        String badPages = readRequiredKey("список плохих статей", options);
        String separator = "\n";
        String key = "разделитель";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            separator = options.get(key).replace("\\n", "\n");
        }

        Format format = Format.TXT;
        key = "формат";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            if (Format.TSV.toString().toLowerCase(Locale.ENGLISH).equals(options.get(key))) {
                format = Format.TSV;
            }
        }

        Method method = DEFAULT_METHOD;
        key = "метод";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {            
            if (Method.METHOD_1.name.equals(options.get(key))) {
                method = Method.METHOD_1;
            } else if (Method.METHOD_2.name.equals(options.get(key))) {
                method = Method.METHOD_2;
            } else if (Method.METHOD_3.name.equals(options.get(key))) {
                method = Method.METHOD_3;
            } else {
                throw new BotFatalError("Unknown method " + options.get(key));
            }
        }
        boolean redirects = DEFAULT_INCLUDE_REDIRECTS;
        key = "редиректы";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            if (YES_RU.equals(options.get(key))) {
                redirects = true;
            } else if (NO_RU.equals(options.get(key))) {
                redirects = false;
            } else {
                throw new BotFatalError("Unexpected value of " + key + ": " + options.get(key));
            }
        }
        
        return new Task(badPages, archive, separator, format, method, redirects);
    }
    
    private String readRequiredKey(String key, Map<String, String> options) throws BotFatalError {
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            return options.get(key);
        } else {
            throw new BotFatalError(String.format("Missing required parameter in options: %1$s",
                    key));
        }
    }

    /**
     * Entry point of bot application.
     */
    public static void main(String[] args) {
        BasicBot bot = new CleanArchiveBot();
        System.exit(bot.run(args));
    }

}
