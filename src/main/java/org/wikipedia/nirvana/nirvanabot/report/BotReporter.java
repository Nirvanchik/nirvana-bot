/**
 *  @(#)BotReporter.java 
 *  Copyright © 2014 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.nirvanabot.report;

import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.util.FileTools;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.security.auth.login.LoginException;

/**
 * Main bot reporter.
 * Collects daily statistics about bot work.
 * There are two reports: short report and detailed report.
 * Short report is written at bot start and bot finish. It's also called "bot status".
 * Detailed report is written at bot finish and only once per day.
 * Detailed report has info about update for every known portal page processed by the bot.
 *
 * Short and detailed reports are used mostly to debug bot problems quickly.
 * This is a bot radar.
 */
public class BotReporter {
    private static final Logger log;

    public static final String DEFAULT_CACHE_FILE = "report_cache.js";

    private final String cacheDir;

    NirvanaWiki wiki;
    ArrayList<ReportItem> reportItems;
    Calendar timeStarted;
    Calendar timeFinished;

    private int portalsTotal;
    private int portalsChecked;
    private int portalsProcessed;
    private int portalsUpdated;
    private int portalsError;

    String version;

    private String preambula = "";
    private Localizer localizer;

    static {
        log = LogManager.getLogger(BotReporter.class.getName());
    }
    
    protected Calendar getCurrentTime() {
        return Calendar.getInstance();
    }

    /**
     * Constructs reporter instance.
     *
     * @param wiki a Wiki instance to save short and detailed reports in.
     * @param cacheDir cache direcory for saving secondary bot reports.
     * @param capacity detailed report capacity (should be equal or a little higher than a usual
     *                 number of report items.
     * @param started if <code>true</code> the reporter will start its time counter at creating. 
     * @param version a bot version string (is written in Wiki report edit summary).
     * @param preambulaFile a path of the file with detailed report preambula text (Wiki code).
     */
    public BotReporter(NirvanaWiki wiki, String cacheDir, int capacity, boolean started,
            String version, String preambulaFile) {
        this.wiki = wiki;
        this.version = version;
        reportItems = new ArrayList<ReportItem>(capacity);
        if (started) {
            botStarted(true);
        }
        if (preambulaFile != null && !preambulaFile.isEmpty()) {
            preambula = FileTools.readFileSilently(preambulaFile, "");
            if (!preambula.isEmpty() && !preambula.endsWith("\n")) {
                preambula += "\n";
            }
        }
        this.cacheDir = cacheDir;
    }

    /**
     * If not creating reporter with "started" flag, call this when bot has just started to
     * register starting time.
     *
     * @param log <code>true</code> to log start status.
     */
    public void botStarted(boolean log) {
        timeStarted = getCurrentTime();
        if (log) {
            logStartStatus();
        }
    }

    /**
     * Call this when bot has just finished to stop counting bot run time.
     *
     * @param log <code>true</code> to log end status.
     */
    public void botFinished(boolean log) {
        timeFinished = getCurrentTime();
        if (log) {
            logEndStatus();
        }
    }

    /**
     * Set total count of portal pages.
     *
     * @param total total count of portal pages.
     */
    public void setTotal(int total) {
        portalsTotal = total;
    }

    /**
     * Add number to total portal pages count.
     *
     * @param num some number.
     */
    public void addToTotal(int num) {
        portalsTotal += num;
    }

    /**
     * Call this when starting portal page processing.
     */
    public void portalChecked() {
        portalsChecked++;
    }

    /**
     * Call this when bot has processed portal settings and is going to update portal page.
     *
     * @param tryNumber try number (starting from 1).
     */
    public void portalProcessed(int tryNumber) {
        if (tryNumber == 1) {
            portalsProcessed++;
        }
    }

    /**
     * Call this when bot updated portal page successfully.
     */
    public void portalUpdated() {
        portalsUpdated++;
    }

    /**
     * Call this when bot failed to update portal page.
     */
    public void portalError() {
        portalsError++;
    }

    /**
     * Adds new report item to the detailed report.
     *
     * @param reportItem a report item instance.
     */
    public void add(ReportItem reportItem) {
        reportItems.add(reportItem);
    }

    /**
     * Sorry, I don't know what is it.
     */
    public void report() {

    }

    /**
     * Initialize localizer instance.
     */
    public void initLocalizer() {
        if (localizer == null) {
            localizer = Localizer.getInstance();
        }
    }

    /**
     * Updates start status at Wiki page.
     *
     * @param page A Wiki page name with a bot status info (short report).
     * @param template A bot template name used to render bot status nicely.
     */
    public void updateStartStatus(String page, String template) throws LoginException, IOException {
        initLocalizer();
        String text = String.format(
                "{{%1$s|status=1|starttime=%2$tF %2$tT|version=%3$s}}",
                template, timeStarted, version);
        wiki.edit(page, text, localizer.localize("Бот запущен"));
    }

    /**
     * Updates end status at Wiki page.
     *
     * @param page A Wiki page name with a bot status info (short report).
     * @param template A bot template name used to render bot status nicely.
     */
    public void updateEndStatus(String page, String template) throws LoginException, IOException {
        initLocalizer();
        String text = String.format(
                "{{%1$s|status=0|starttime=%2$tF %2$tT|endtime=%3$tF %3$tT|time=%4$s" +
                "|total=%5$d|checked=%6$d|processed=%7$d|updated=%8$d|errors=%9$d|version=%10$s}}", 
                template, timeStarted, timeFinished, printTimeDiff(timeStarted, timeFinished),
                portalsTotal, portalsChecked, portalsProcessed, portalsUpdated, portalsError,
                version);
        wiki.edit(page, text, localizer.localize("Бот остановлен"));
    }

    /**
     * Sorry. I don't know what is this.
     */
    public void updateStatus() {

    }

    /**
     * Prints report to String in TXT format.
     */
    public String printReportTxt() {
        StringBuilder sb = new StringBuilder();
        //StringBuffer sbuf = new StringBuffer();
        sb.append(ReportItem.getHeaderTxt()).append(System.lineSeparator());
        for (ReportItem item : reportItems) {
            sb.append(item.toStringTxt());
            sb.append(System.lineSeparator());
        }
        sb.append(ReportItem.getFooterTxt());
        return sb.toString();
    }

    /**
     * Write report to TXT file.
     *
     * @param fileName a file path to write report in.
     */
    public void reportTxt(String fileName) {
        File f = new File(fileName);
        File dir = f.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!fileName.endsWith(".txt")) {
            fileName = fileName + ".txt";
        }
        log.info("generating report (TXT) . . .");
        try {
            FileTools.writeFile(printReportTxt(), fileName);
        } catch (IOException e) {
            log.error(e.toString());
            e.printStackTrace();
        }            
        log.info("report is generated!");
    }

    /**
     * Write (if main bot launch) or save (if secondary launch) detailed report.
     *
     * @param reportPage A wiki report page name to save report in.
     * @param mainLaunch <code>true</code> if this is a main bot launch in this day (all daily
     *                   reports should be merged and reported, or just save otherwise).
     */
    public void reportWiki(String reportPage, boolean mainLaunch) {
        // 1) Load old data (if any)
        String path = cacheDir + "/" + DEFAULT_CACHE_FILE;
        File file = new File(path);
        List<ReportItem> oldData = null;        
        if (file.exists()) {
            log.debug("File with old data found: " + path);
            // Check last modification time. Ignore if it's too old
            long lastModTime = file.lastModified();
            if (lastModTime != 0) {
                Calendar c = getCurrentTime();
                c.add(Calendar.DAY_OF_MONTH, -1);
                if (lastModTime > c.getTimeInMillis()) {
                    log.debug("Load old data");
                    oldData = load(file);
                }
            }
            log.debug("Delete old data");
            file.delete();
        }
        // 2) Merge with old data (if any)
        if (oldData != null) {
            merge(oldData);
        }
        // 3) Report to wiki or save to file
        if (mainLaunch) {
            doReportWiki(reportPage);
        } else {
            save(file);
        }
    }

    /**
     * Merge current detailed report with another report data.
     *
     * @param data Report items to merge with.
     */
    public void merge(List<ReportItem> data) {
        log.debug("Merge old data");
        // 3 cases here:
        // 1) lists are equal (ideal)
        // 2) left has values that right has not
        // 3) right has values that left has not
        ArrayList<ReportItem> rightItems = new ArrayList<ReportItem>(data);
        for (ReportItem left: reportItems) {
            for (int j = 0; j < rightItems.size(); j++) {
                if (left.theSameSource(rightItems.get(j))) {
                    ReportItem right = rightItems.get(j);
                    left.merge(right);
                    rightItems.remove(j);
                    break;
                }
            }
        }
        reportItems.addAll(rightItems);
    }

    /**
     * Prints report to String in Wiki format.
     */
    public String printReportWiki() {
        StringBuilder sb = new StringBuilder();
        sb.append(preambula);
        sb.append(ReportItem.getHeaderWiki()).append("\n");
        int i = 0;
        for (ReportItem item : reportItems) {
            sb.append(item.toStringWiki(i));
            sb.append("\n");
            i++;
        }
        sb.append(ReportItem.getFooterWiki());
        return sb.toString();
    }

    /**
     * Write a detailed report at wiki.
     *
     * @param reportPage a wiki page name to write report in.
     */
    public void doReportWiki(String reportPage) {
        initLocalizer();
        log.info("Generating report (Wiki) . . .");

        try {
            wiki.edit(reportPage, printReportWiki(),
                    localizer.localize("Отчёт по работе бота за сутки"));
        } catch (LoginException | IOException e) {
            log.error("Failed to update report.", e);
        }
    }

    private void logStartStatus() {
        log.info(String.format("BOT STARTED %1$tF %1$tT version: %2$s", timeStarted, version));
    }

    /**
     * Print short report to log.
     */
    public void logStatus() {
        log.info("portals: {}, checked: {}, processed: {}, updated: {}, errors: {}",
                portalsTotal, portalsChecked, portalsProcessed, portalsUpdated, portalsError);
    }


    private void logEndStatus() {
        if (timeStarted == null) {
            throw new IllegalStateException("timeStarted is null. botStarted() not called");
        }
        long start = timeStarted.getTimeInMillis();
        if (timeFinished == null) {
            throw new IllegalStateException("timeFinished is null. botFinished() not called");
        }
        long end = timeFinished.getTimeInMillis();
        log.info(String.format("BOT STARTED %1$tF %1$tT", timeStarted));
        log.info(String.format("BOT FINISHED %1$tF %1$tT", timeFinished));
        log.info("FULL WORK TIME: {}", printTimeDiff(end - start));
    }

    /**
     * Formats time diff between 2 Calendar instances in "H M S" format.
     *
     * @param time1 the first time to get diff.
     * @param time2 the second (later) time to get diff.
     * @return a string with time difference in English.
     */
    public static String printTimeDiff(Calendar time1, Calendar time2) {
        long start = time1.getTimeInMillis();
        long end = time2.getTimeInMillis();
        return printTimeDiff(end - start);
    }
    
    /**
     * Formats time diff between 2 Calendar instances in "H M S" format.
     *
     * @param diff a time difference in milliseconds.
     * @return a string with time difference in English.
     */
    public static String printTimeDiff(long diff) {
        int hours = (int) ((diff) / (60L * 60L * 1000L));
        int min = (int) ((diff) / (60L * 1000L) - (long)hours * 60L);
        int sec = (int) ((diff) / (1000L) - (long)hours * 60L * 60L - (long)min * 60L);
        return String.format("%d h %d m %d s", hours, min, sec);
    }

    /**
     * Loads report items from the specified file.
     *
     * @param file a file path (json).
     * @return report items.
     */
    public List<ReportItem> load(File file) {
        log.info("Load report items from {}", file.getPath());
        ObjectMapper mapper = new ObjectMapper();
        List<ReportItem> items = null;
        try {
            items = mapper.readValue(
                    file, 
                    new TypeReference<List<ReportItem>>() { });
        } catch (IOException e1) {
            log.error(e1);
            return null;
        }
        return items;
    }

    /**
     * Saves this report to the specified file.
     *
     * @param file a file path (json).
     */
    public void save(File file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(file, reportItems);
        } catch (IOException e) {
            log.error(e);
            return;
        }
        log.info("Report items successfully saved to {}", file.getPath());
    }
}
