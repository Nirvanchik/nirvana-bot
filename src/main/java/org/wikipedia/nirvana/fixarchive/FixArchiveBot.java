/**
 *  @(#)FixArchiveBot.java
 *  Copyright © 2020 Dmitry Trofimovich (KIN)(DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.fixarchive;

import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_REMOVE_DUPLICATES;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_SORT;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_TOSORT;

import org.wikipedia.nirvana.archive.Archive;
import org.wikipedia.nirvana.archive.ArchiveSettings;
import org.wikipedia.nirvana.archive.ArchiveSettings.Period;
import org.wikipedia.nirvana.archive.EnumerationUtils;
import org.wikipedia.nirvana.base.BasicBot;
import org.wikipedia.nirvana.base.BotFatalError;
import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.nirvanabot.BotVariables;
import org.wikipedia.nirvana.nirvanabot.NewPages;
import org.wikipedia.nirvana.nirvanabot.NirvanaBot;
import org.wikipedia.nirvana.nirvanabot.PortalConfig;
import org.wikipedia.nirvana.util.DateTools;
import org.wikipedia.nirvana.util.FileTools;
import org.wikipedia.nirvana.util.OptionsUtils;
import org.wikipedia.nirvana.util.TextUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

/**
 * Bot that modifies new pages archives formatting: headers, enumeration, sort order,
 * removing duplicates, etc.
 */
public class FixArchiveBot extends BasicBot {
    public static final String delimeter = "\n";    
    public static final String ERR_NO_VARIABLE =
            "В параметре \"{}\" не задан переменный параметр. " +
            "Значение этого параметра не принято.";

    private static String taskFile = "task.txt";
    private static final String VERSION = "v1.04";
    public static final String INFO = 
            "FixArchiveBot " + VERSION + " Updates archives of new articles lists at " +
            "http://ru.wikipedia.org\n" +
            "Copyright (C) 2020 Dmitry Trofimovich " +
            "(KIN, Nirvanchik, DimaTrofimovich@gmail.com)\n\n";

    public void showInfo() {
        System.out.print(INFO);
    }

    /**
     * Entry point for bot application.
    */
    public static void main(String[] args) {
        BasicBot bot = new FixArchiveBot();
        System.exit(bot.run(args));
    }

    @Override
    protected void go() throws InterruptedException, BotFatalError {
        // Исходные данные:
        // 1) архив который нужно обновить
        // 2) формат заголовка
        // 3) формат второго заголовка
        // 4) нумерация, сверху/снизу
        // 5) ботоправка,малая правка
        // ~4) удалять старые заголовки?
        // ~5) шапка
        // ~6) подвал

        Localizer.init(Localizer.NO_LOCALIZATION);
        PortalConfig.initStatics();
        BotVariables.init();
        DateTools.init(language);

        String task;
        try {
            task = FileTools.readFile(taskFile);
        } catch (FileNotFoundException e) {
            throw new BotFatalError(String.format("File not found: %s", taskFile), e);
        } catch (IOException e) {
            throw new BotFatalError(String.format("Failed to read file %s", taskFile), e);
        }

        Map<String, String> options = new HashMap<String, String>();
        if (!TextUtils.textOptionsToMap(task, options)) {
            throw new BotFatalError("Invalid settings. "
                    + "All lines must have '=' symbol with the format: 'key=value'.");
        }

        ArchiveProcessingSettings archiveSettings = new ArchiveProcessingSettings();
        String archive = null;
        String key = "архив";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            archive = options.get(key);
            NirvanaBot.parseArchiveName(archiveSettings,options.get(key));
        }
        
        String str = "";
        key = "формат заголовка в архиве";
        Period p1 = Period.NONE;
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            str = options.get(key);
            p1 = ArchiveSettings.getHeaderPeriod(str);
            if (p1 == Period.NONE) {
                log.error(ERR_NO_VARIABLE, key);
                return;
            } else {
                archiveSettings.headerFormat = str;
            }
        }

        key = "формат подзаголовка в архиве";
        Period p2 = Period.NONE;
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            str = options.get(key); 
            p2 = ArchiveSettings.getHeaderPeriod(str);
            if (p2 == Period.NONE) {
                log.error(ERR_NO_VARIABLE, key);
                return;
            } else {
                archiveSettings.superHeaderFormat = archiveSettings.headerFormat;
                archiveSettings.headerFormat = str;
            }            
        }

        if (p1 != Period.NONE && p2 != Period.NONE && p1 == p2) {
            log.error("Параметр \"формат заголовка в архиве\" и параметр " +
                    "\"формат подзаголовка в архиве\" имеют одинаковый период повторения " +
                    p1.template());
            return;
        }

        key = "параметры архива";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            ArrayList<String> errors = parseArchiveSettings(archiveSettings, options.get(key));
            if (errors.size() > 0) {
                errors.forEach(error -> log.error(error));
                throw new BotFatalError("Invalid settings of archive parameters. See error log.");
            }
        }

        key = "первый год";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            try {
                archiveSettings.startYear = Integer.parseInt(options.get(key));
            } catch (NumberFormatException e) {
                String format = NirvanaBot.ERROR_PARSE_INTEGER_FORMAT_STRING_EN;
                log.warn(String.format(format, key, options.get(key)));
            }
        }

        boolean bot = true;
        boolean minor = true;
        if (options.containsKey("помечать правки") && !options.get("помечать правки").isEmpty()) {
            String mark = options.get("помечать правки").toLowerCase();
            if (mark.equalsIgnoreCase("нет")) {
                bot = false;
                minor = false;
            } else {                
                if (!mark.contains("бот") && mark.contains("малая")) {
                    bot = false;
                } else if (mark.contains("бот") && !mark.contains("малая")) {
                    minor = false;
                }                
            }
        }
        log.info("archive settings OK");

        updateAllArchives(archive, archiveSettings, bot, minor);

    }

    private void updateAllArchives(String archive, ArchiveProcessingSettings archiveSettings,
            boolean bot, boolean minor) throws BotFatalError {
        DateTools dateTools = DateTools.getInstance();
        if (archiveSettings.archivePeriod == Period.NONE) {
            updateArchive(archive, archiveSettings, bot, minor);
        } else {
            int startYear = archiveSettings.startYear;
            Calendar c = Calendar.getInstance();
            int endYear = c.get(Calendar.YEAR);
            if (!archiveSettings.archive.contains(Period.YEAR.template())) {
                startYear = endYear;
            }
            for (int year = startYear; year <= endYear; year++) {
                int startQuarter = 0;
                int endQuarter = 3;
                if (!archiveSettings.archive.contains(Period.QUARTER.template()) && 
                        !archiveSettings.archive.contains(Period.SEASON.template())) {
                    endQuarter = 0;
                }
                for (int q = startQuarter; q <= endQuarter; q++) {
                    String page = archive
                            .replace(Period.YEAR.template(), String.valueOf(year))
                            .replace(Period.SEASON.template(), dateTools.seasonString(q))
                            .replace(Period.QUARTER.template(), String.valueOf(q + 1));
                    updateArchive(page, archiveSettings, bot, minor);
                }
            }
        }
    }

    private void updateArchive(String archive, ArchiveProcessingSettings archiveSettings,
            boolean bot, boolean minor) throws BotFatalError {
        log.info("Updating archive: {}", archive);
        String [] lines = null;
        try {
            String text = wiki.getPageText(archive);
            if (text == null) {
                log.info("Archive does not exist: {}", archive);
                return;
            }
            lines = text.split("\n");
        } catch (IOException e) {
            throw new BotFatalError(String.format("Failed to get text of wiki page %s", archive),
                    e);
        }

        Archive thisArchive;
        try {
            thisArchive = FixArchiveFactory.createArchive(archiveSettings, wiki, archive);
        } catch (IOException e) {
            throw new BotFatalError(String.format("IOException when creating archive %s", archive),
                    e);
        }

        int delta = 0;
        int start;
        int end;
        if (archiveSettings.addToTop) {
            delta = -1;
            start = lines.length - 1;
            end = -1;
        } else {
            delta = 1;
            start = 0;
            end = lines.length;
        }
        Pattern p = Pattern.compile("^==(=?)(\\s*)(?<headername>[^=]+)(\\s*)(=?)==$");

        for (int i = start; i != end; i += delta) {
            String item = lines[i];            
            if (!item.isEmpty() &&
                    archiveSettings.removeDuplicates ||
                    (    item.compareToIgnoreCase(EnumerationUtils.OL) != 0 &&
                         item.compareToIgnoreCase(EnumerationUtils.OL_END) != 0 &&
                         !p.matcher(item).matches())    ) {
                Calendar c = NewPages.getNewPagesItemDate(wiki, item);
                thisArchive.add(item, c);
            }
        }
        try {
            log.debug("Edit page: {} with a comment: {}", archive, comment);
            wiki.edit(archive, thisArchive.toString(), comment, minor, bot);
        } catch (LoginException | IOException e) {
            throw new BotFatalError(String.format("Failed to edit page %s", archive), e);
        }
    }

    @Override
    protected boolean loadCustomProperties(Map<String, String> launchParams) {
        taskFile = properties.getProperty("task-file", taskFile);
        log.info("Task file: {}", taskFile);
        return true;
    }

    static ArrayList<String> parseArchiveSettings(ArchiveProcessingSettings archiveSettings,
            String settings) {
        Localizer localizer = Localizer.getInstance();
        List<String> items = OptionsUtils.optionToList(settings);
        return parseArchiveSettings(archiveSettings, items, localizer);
    }

    static ArrayList<String> parseArchiveSettings(ArchiveProcessingSettings archiveSettings,
            List<String> items, Localizer localizer) {

        if (items.contains(STR_TOSORT) || items.contains(STR_SORT)) {
            archiveSettings.sorted = true;
        }
        if (items.contains(STR_REMOVE_DUPLICATES)) {
            archiveSettings.removeDuplicates = true;
        }
        return NirvanaBot.parseArchiveSettings(archiveSettings, items, localizer);
    }
}
