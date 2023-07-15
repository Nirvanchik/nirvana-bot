/**
 *  @(#)StatisticsBot.java
 *  Copyright © 2022 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.statistics;

import static org.wikipedia.nirvana.util.OptionsUtils.validateIntegerSetting;

import org.wikipedia.Wiki;
import org.wikipedia.nirvana.archive.ArchiveSettings;
import org.wikipedia.nirvana.archive.ArchiveSettings.Period;
import org.wikipedia.nirvana.archive.ScanArchiveSettings;
import org.wikipedia.nirvana.base.BasicBot;
import org.wikipedia.nirvana.base.BotTemplateParser;
import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.nirvanabot.BotFatalError;
import org.wikipedia.nirvana.nirvanabot.NirvanaBot;
import org.wikipedia.nirvana.nirvanabot.PortalConfig;
import org.wikipedia.nirvana.nirvanabot.SystemTime;
import org.wikipedia.nirvana.util.DateTools;
import org.wikipedia.nirvana.util.FileTools;
import org.wikipedia.nirvana.util.NumberTools;
import org.wikipedia.nirvana.util.OptionsUtils;

import org.apache.commons.lang3.time.StopWatch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

/**
 * Bot that calculates statistics tables and user ratings of new pages creation
 * on specific Wikipedia projects. The source of data is currently archive pages generated
 * by NirvanaBot. Results are saved at wiki pages.
 *
 * See bot description in Wikipedia: https://ru.wikipedia.org/wiki/Участник:NirvanaBot/Статистика
 *
 */
public class StatisticsBot extends BasicBot {
    public static final String ERR_NO_VARIABLE =
            "В параметре \"{}\" не задан переменный параметр. " +
            "Значение этого параметра не принято.";

    public static final String DEFAULT_CACHE_FOLDER = "cache";
    //public static final int START_YEAR = 2008;
    private static boolean TASK = false;
    private static String TASK_LIST_FILE = "task.txt";
    public static final String delimeter = "\n";
    public static final String YES_RU = "да";
    public static final String NO_RU = "нет";
    public static final boolean SORT_DEFAULT = true;
    private static final String STATUS_INFO_FORMAT = "%1$-40s time spent:%2$s";
    private static final String DEFAULT_HEADER = "";
    private static final String DEFAULT_FOOTER = "";
    private static final int MIN_SIZE = 14 * 1024;  // 14 Kb

    private static String statSettingsTemplate = null;

    private static final String VERSION = "v1.3";

    public static final String INFO =
            "StatisticsBot " + VERSION + " Makes statistics of new created pages and ratings\n" +
            "Copyright (C) 2022 Dmitry Trofimovich (KIN)\n" +
            "\n";
    private static boolean USE_CACHE_ONLY = false;
    private static boolean USE_CACHE = true;

    private SystemTime systemTime;

    public void showInfo() {
        System.out.print(INFO);
    }

    /**
     * Constructs bot instance. 
     */
    public StatisticsBot() {
    }

    /**
     * Constructs bot instance with specific flags. 
     */
    public StatisticsBot(int flags) {
        super(flags);
    }

    public static void main(String[] args) {
        BasicBot bot = new StatisticsBot();
        System.exit(bot.run(args));
    }
    
    public String getCacheDir() {
        return outDir + "/" + DEFAULT_CACHE_FOLDER;
    }

    @Override
    protected boolean loadCustomProperties(Map<String,String> launchParams) {
        statSettingsTemplate = properties.getProperty("stat-settings-template");
        if (statSettingsTemplate == null) {
            if (DEBUG_BUILD) {
                statSettingsTemplate = "Участник:NirvanaBot/test/Параметры статистики";
            } else {
                System.out.println("ABORT: properties not found");
                log.fatal("Statistics settings template name (stat-settings-template) "
                        + "is not specified in settings");
                return false;
            }
        }
        log.info("new pages template : {}", statSettingsTemplate);
        TASK_LIST_FILE = properties.getProperty("task-list-file", TASK_LIST_FILE);
        log.info("task list file: {}", TASK_LIST_FILE);

        TASK = properties.getProperty("task-list", TASK ? YES : NO).equals(YES);
        log.info("task list : {}", TASK);

        USE_CACHE = properties.getProperty("use-cache", USE_CACHE ? YES : NO).equals(YES);
        log.info("use cache: {}", USE_CACHE);

        USE_CACHE_ONLY = properties.getProperty("use-cache-only", USE_CACHE_ONLY ? YES : NO)
                .equals(YES);
        log.info("use cache only: {}", USE_CACHE_ONLY);

        ScanArchiveSettings.setDefaultStartYear(
                validateIntegerSetting(properties, "archive-start-year",
                        ScanArchiveSettings.getDefaultStartYear(), false));

        RatingTotal.setDefaultStartYear(
                validateIntegerSetting(properties, "rating-total-start-year",
                        RatingTotal.getDefaultStartYear(), false));

        return true;
    }

    protected void showStatus(String status, StopWatch watch) {
        watch.stop();
        log.info(String.format(STATUS_INFO_FORMAT, status, watch));
        watch.reset();
        watch.start();
    }

    @Override
    protected void go() throws InterruptedException, BotFatalError {
        Localizer.init(Localizer.NO_LOCALIZATION);
        PortalConfig.initStatics();
        DateTools.init(language);
        systemTime = initSystemTime();

        String [] portalSettingsPages = null;
        String userNamespace;
        try {
            userNamespace = wiki.namespaceIdentifier(Wiki.USER_NAMESPACE);
        } catch (UncheckedIOException e) {
            throw new BotFatalError("Failed to get user namespace", e);
        }
        BotTemplateParser botTemplateParser =
                new BotTemplateParser(statSettingsTemplate, userNamespace);
        try {
            portalSettingsPages = wiki.whatTranscludesHere(statSettingsTemplate,
                    Wiki.ALL_NAMESPACES);
        } catch (IOException e) {
            throw new BotFatalError("Failed to get portal list", e);
        }
        Arrays.sort(portalSettingsPages);

        List<String> tasks = null;
        if (TASK) {
            try {
                tasks = FileTools.readFileToList(TASK_LIST_FILE, true);
            } catch (FileNotFoundException e) {
                throw new BotFatalError("File not found: " + TASK_LIST_FILE);
            } catch (IOException e) {
                throw new BotFatalError(e);
            }
        }

        int portalIndex = 0;
        String portalName = null;
        while (portalIndex < portalSettingsPages.length) {
            portalName = portalSettingsPages[portalIndex];
            log.info("Processing portal: {}", portalName);
            if (tasks != null) {
                boolean skip = true;
                for (String task : tasks) {
                    log.debug("task: {}", task);
                    if (portalName.startsWith(task.trim())) {
                        skip = false;
                        break;
                    }
                }
                if (skip) {
                    log.info("SKIP portal: {}", portalName);
                    portalIndex++;
                    continue;
                }
            }

            StopWatch watch = new StopWatch();
            StopWatch wtotal = new StopWatch();

            try {
                wtotal.start();
                watch.start();

                String portalSettingsText = wiki.getPageText(portalName);

                if (debugMode || BasicBot.DEBUG_BUILD) {
                    FileTools.dump(portalSettingsText, portalName + ".settings.txt");
                }

                Map<String, String> options = new HashMap<String, String>();
                if (!botTemplateParser.tryParseTemplate(portalSettingsText, options)) {
                    log.error("validate portal settings FAILED");
                    continue;
                }
                log.info("validate portal settings OK");
                logPortalSettings(options);

                StatisticsParam params = new StatisticsParam();
                if (!readPortalSettings(options, params)) {
                    log.error("Failed to read portal settings, portal ABORTED");
                    continue;
                }

                // create db
                String dbName = params.archiveSettings.archive.substring(0,
                        params.archive.indexOf("/"));

                showStatus("parameter processing finished", watch);

                String cacheDir = getCacheDir();
                ArchiveDatabase2 db = new ArchiveDatabase2(dbName, params.cache, cacheDir,
                        params.filterBySize);

                showStatus("database created", watch);

                if (!params.cacheonly) {
                    ArchiveParser parser = new ArchiveParser(params.archiveSettings, db, wiki,
                            cacheDir, systemTime);
                    try {
                        parser.getData(params.cache);
                    } catch (IOException e) {            
                        log.fatal(e);
                        return;
                    }
                }
                showStatus("data loaded to database", watch);

                if (!db.isSorted()) {
                    log.warn("db is not sorted");
                    if (params.sort) {
                        db.sort();
                        db.markYears();
                        showStatus("data sorted", watch);
                    }
                }

                if (params.cache && !params.cacheonly) { 
                    db.save();
                }

                int endYear = systemTime.now().get(Calendar.YEAR);

                Statistics.portal = dbName;

                // create statistics
                for (String type: params.reportTypes) {
                    String key = type;
                    String destination = null;
                    if (options.containsKey(key) && !options.get(key).isEmpty()) {
                        destination = options.get(key);
                    } else {
                        log.error("Output destination for {} is not defined", type);
                        continue;
                    }

                    Map<String,String> suboptions = getSubOptions(options, type);
                    if (suboptions.get("первый год") == null) {
                        suboptions.put("первый год",
                                String.valueOf(params.archiveSettings.startYear));
                    }
                    if (params.header != null && suboptions.get("шапка") == null) {
                        suboptions.put("шапка", params.header);
                    }
                    if (params.footer != null && suboptions.get("подвал") == null) {
                        suboptions.put("подвал", params.footer);
                    }
                    if (params.filterBySize && suboptions.get("статьи от") == null) {
                        suboptions.put("статьи от", String.valueOf(params.minSize));
                    }

                    Statistics stat = null;
                    if (destination.contains("%(год)")) {
                        for (int year = params.archiveSettings.startYear; year <= endYear; year++) {
                            log.info("creating report of type: {} for year: {}", type,
                                    String.valueOf(year));
                            stat = StatisticsFabric.createReporter(wiki, cacheDir, type, systemTime,
                                    year);
                            if (stat == null) {
                                log.error("Report type {} is not defined", type);
                                break;
                            }
                            if (suboptions != null && !suboptions.isEmpty()) {
                                stat.setOptions(suboptions);
                            }
                            stat.put(db);
                            String text = stat.toString();
                            String article = destination.replace("%(год)", String.valueOf(year));
                            wiki.editIfChanged(article, text, params.comment, true, true);
                        }
                    } else {
                        log.info("creating report of type: {}", type);
                        stat = StatisticsFabric.createReporter(wiki, cacheDir, type, systemTime);
                        if (stat == null) {
                            log.error("Report type {} is not defined", type);
                            continue;
                        } 
                        if (suboptions != null && !suboptions.isEmpty()) {
                            stat.setOptions(suboptions);
                        }
                        stat.put(db);
                        String text = stat.toString();
                        String article = destination;
                        wiki.editIfChanged(article, text, params.comment, true, true);
                    }
                }
            } catch (IOException e) {
                throw new BotFatalError(e);
            } catch (LoginException e) {
                throw new BotFatalError(e);
            } catch (IllegalStateException e) {
                throw new BotFatalError(e);
            } catch (Exception e) {
                log.fatal(e,e);
                e.printStackTrace();
                continue;
            } finally {
                showStatus("statistics creation finished", watch);
                watch.stop();        
                wtotal.stop();
                log.info(String.format(STATUS_INFO_FORMAT, "FINISHED. total time ->", wtotal));
                StatisticsFabric.purge();
                portalIndex++;
            }
        }

    }

    private static List<String> optionToList(Map<String, String> options, String key) {
        if (options.containsKey(key)) {            
            return OptionsUtils.optionToList(options.get(key), false);
        }
        return Collections.emptyList();
    }

    private boolean readPortalSettings(Map<String, String> options, StatisticsParam params) {
        params.archiveSettings = new ScanArchiveSettings();
        params.archive = null;
        String key = "архив";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            params.archive = options.get(key);
            NirvanaBot.parseArchiveName(params.archiveSettings, options.get(key));            
        } else {
            log.error("Параметр \"архив\" не найден в настройках");
            return false;            
        }

        String str = "";
        key = "формат заголовка в архиве";
        Period p1 = Period.NONE;
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            str = options.get(key);
            p1 = ArchiveSettings.getHeaderPeriod(str);
            if (p1 == Period.NONE) {
                log.error(ERR_NO_VARIABLE, key);
                return false;
            } else {
                params.archiveSettings.headerFormat = str;
            }
        }

        key = "параметры архива";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            parseArchiveSettings(params.archiveSettings, options.get(key));
        }

        params.reportTypes = null;
        key = "тип";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            params.reportTypes = optionToList(options, key);
        } else {
            log.error("Параметр \"тип\" не найден в настройках");
            return false;
        }

        if (params.reportTypes.isEmpty()) {
            log.error("Значение параметра 'тип' пустое");
            return false;
        }

        key = "комментарий";
        params.comment = comment;
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            params.comment = options.get(key);
        }

        key = "первый год";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            try {
                params.archiveSettings.startYear = Integer.parseInt(options.get(key));
            } catch (NumberFormatException e) {
                log.warn(String.format(NirvanaBot.ERROR_PARSE_INTEGER_FORMAT_STRING_EN, key,
                        options.get(key)));
            }
        }

        key = "первый архив";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            params.archiveSettings.firstArchive = options.get(key);
        }

        key = "первый год после первого архива";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            try {
                params.archiveSettings.startYear2 = Integer.parseInt(options.get(key));
            } catch (NumberFormatException e) {
                log.warn(String.format(NirvanaBot.ERROR_PARSE_INTEGER_FORMAT_STRING_EN, key,
                        options.get(key)));
            }
        }

        params.sort = SORT_DEFAULT;
        key = "сортировать";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            if (options.get(key).equalsIgnoreCase(YES_RU)) {
                params.sort = true;
            } else if (options.get(key).equalsIgnoreCase(NO_RU)) {
                params.sort = false;
            }
        }
        
        params.cacheonly = USE_CACHE_ONLY;
        key = "только кэш";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            if (options.get(key).equalsIgnoreCase(YES_RU)) {
                params.cacheonly = true;
            } else if (options.get(key).equalsIgnoreCase(NO_RU)) {
                params.cacheonly = false;
            }
        }
        
        params.cache = USE_CACHE;
        key = "кэш";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            if (options.get(key).equalsIgnoreCase(YES_RU)) {
                params.cache = true;
            } else if (options.get(key).equalsIgnoreCase(NO_RU)) {
                params.cache = false;
            }
        }
        
        params.filterBySize = false;
        params.minSize = MIN_SIZE;
        key = "статьи от";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            params.filterBySize = true;
            try {
                params.minSize = NumberTools.parseFileSize(options.get(key));
            } catch (NumberFormatException e) {
                log.warn(String.format(NirvanaBot.ERROR_PARSE_INTEGER_FORMAT_STRING_EN, key,
                        options.get(key)));
            }
        }

        params.header = DEFAULT_HEADER;
        key = "шапка";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            params.header = options.get(key).replace("\\n", "\n");
        }

        params.footer = DEFAULT_FOOTER;
        key = "подвал";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            params.footer = options.get(key).replace("\\n", "\n");
        }

        return true;
    }

    Map<String,String> getSubOptions(Map<String,String> options,String option) {
        Map<String,String> suboptions = new HashMap<String,String>();
        for (Map.Entry<String,String> entry: options.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(option) && key.contains(".")) {
                if (suboptions == null) {
                    suboptions = new HashMap<String,String>();
                }
                String subkey = key.substring(key.indexOf('.') + 1).trim();
                suboptions.put(subkey, entry.getValue());
            }
        }
        return suboptions;
    }

    static ArrayList<String> parseArchiveSettings(ScanArchiveSettings archiveSettings,
            String settings) {
        Localizer localizer = Localizer.getInstance();
        List<String> items = OptionsUtils.optionToList(settings);
        return NirvanaBot.parseArchiveSettings(archiveSettings, items, localizer);
    }

    protected SystemTime initSystemTime() {
        return new SystemTime();
    }

    protected long getTimeInMillis() {
        return systemTime.now().getTimeInMillis();
    }
}
