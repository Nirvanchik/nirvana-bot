/**
 *  @(#)ReportItem.java 
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

package org.wikipedia.nirvana.nirvanabot.report;

import org.wikipedia.nirvana.annotation.VisibleForTesting;
import org.wikipedia.nirvana.localization.LocalizedTemplate;
import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.nirvanabot.NirvanaBot.BotError;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.EqualsExclude;

import java.util.TimeZone;


/**
 * Portal page updatement results collected for reporting.
 * Collects info and prints output for one line of a detailed report.
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@JsonIgnoreProperties({"getHeaderTXT","getHeaderWiki","getFooterTXT","getFooterWiki"})
public class ReportItem {
    private static final int MAX_LEN = 90;
    @JsonIgnore
    private static LocalizedTemplate templateYes;
    @JsonIgnore
    private static LocalizedTemplate templateNo;
    @JsonIgnore
    private static String wordNo;
    @JsonIgnore
    private static boolean initialized;
    public String template;
    public String portal;
    Status status;
    BotError error;
    @EqualsExclude
    @JsonIgnore
    long startTime;
    @EqualsExclude
    @JsonIgnore
    long endTime;
    long timeDiff;
    public int errors;
    UpdateStatus updated = UpdateStatus.NONE;
    UpdateStatus archived = UpdateStatus.NONE;
    public boolean settingsValid;
    int newPagesFound = 0;
    int pagesArchived = 0;
    int times;

    /**
     * Update status of one update action.
     * Some of portal pages require 2 update actions: update list, update archive.
     */
    enum UpdateStatus {
        NONE(0, "N/A", "N/A"),
        NO(1, "No", "Нет"),
        YES(2, "Yes", "Да"),
        ERROR(3, "Error", "Ошибка");

        public final int weight;
        public final String english;
        public final String russian;

        UpdateStatus(int weight, String englishString, String russianString) {
            this.weight = weight;
            english = englishString;
            russian = russianString;
        }

        public static UpdateStatus selectBest(UpdateStatus left, UpdateStatus right) {
            if (right.weight > left.weight) {
                return right;
            }
            return left;
        }

        public boolean isSuccess() {
            return this == YES;
        }

        public boolean isFailure() {
            return this == ERROR;
        }
    }

    /**
     * Portal page update statuses.
     *
     */
    enum Status {
        NONE(0),
        SKIP(1),
        PROCESSED(2),
        UPDATED(3),
        //DENIED,
        ERROR(4);        

        public final int order;

        Status(int order) {
            this.order = order;
        }

        public static Status selectBest(Status left, Status right) {
            if (right.order > left.order) {
                return right;
            }
            return left;
        }

        public boolean isSuccess() {
            return this == PROCESSED || this == UPDATED;
        }

        public boolean isFailure() {
            return this == ERROR;
        }
    }

    /**
     * Default constructor. Used for deserialization by Jackson json lib.
     */
    public ReportItem() {
        // default constructor
    }

    /**
     * Constructs reporting item object with the given bot settings template and portal name.
     *
     * @param template bot settings template.
     * @param name portal page name for which this report item is created.
     */
    public ReportItem(String template, String name) {
        this.template = template;
        this.portal = name;
        startTime = 0;
        endTime = 0;
        errors = 0;
        times = 0;
        newPagesFound = 0;
        pagesArchived = 0;
        status = Status.NONE;
        error = BotError.NONE;
        settingsValid = true;
        //this.equals(obj);
    }

    private static void initStatics() {
        if (!initialized) {
            Localizer localizer = Localizer.getInstance();
            templateYes = localizer.localizeTemplate("Да");
            templateNo = localizer.localizeTemplate("Нет");
            wordNo = localizer.localize("Нет");
            initialized = true;
        }
    }

    @VisibleForTesting
    static void resetFromTests() {
        initialized = false;
    }

    private static String wikiYesNoCancelStringRu(String str, boolean yes, boolean no) {
        initStatics();
        if (yes) {
            return String.format("{{%s|%s}}", templateYes.localizeName(), str);
        } else if (no) {
            return String.format("{{%s|%s}}", templateNo.localizeName(), str);
        } else {
            return str;
        }
    }

    private static String wikiErrorStringRu(String error, boolean isError) {
        if (isError) {
            return String.format("{{%s|%s}}", templateNo.localizeName(), error);
        }
        return "-";
    }
    
    /**
     * Generates reporting table header in TXT format.
     */
    public static String getHeaderTxt() {
        String header = String.format("%1$-90s %2$-9s %3$-9s %4$s  %5$s %6$s", 
                "portal settings page","status","time","new p.","arch.p.","errors");
        header += System.lineSeparator();
        header += String.format("%1$114s %2$s            %3$s"," ", "upd.", "Error");
        return header;
    }

    /**
     * Generates reporting table header in Wiki format.
     */
    public static String getHeaderWiki() {
        Localizer localizer = Localizer.getInstance();
        StringBuilder sb = new StringBuilder();
        sb.append("{| class=\"wikitable sortable\" style=\"text-align:center\"\n");
        sb.append("|-\n");
        sb.append("! №")
                .append(" !! ").append(localizer.localize("портал/проект"))
                .append(" !! ").append(localizer.localize("проходов"))
                .append(" !! ").append(localizer.localize("статус"))
                .append(" !! ").append(localizer.localize("время"))
                .append(" !! ").append(localizer.localize("новых статей"))
                .append(" !! ").append(localizer.localize("список обновлен"))
                .append(" !! ").append(localizer.localize("архив обновлен"))
                .append(" !! ").append(localizer.localize("ошибок"))
                .append(" !! ").append(localizer.localize("ошибка"));
        return sb.toString();
    }

    /**
     * Generates footer in TXT format.
     */
    public static String getFooterTxt() {
        return "";
    }

    /**
     * Generates footer in Wiki format.
     */
    public static String getFooterWiki() {
        return "|}";
    }

    protected long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Generates reporting result line of this portal page in TXT format.
     */
    public String toStringTxt() {
        String line = "";
        String timeString = "N/A";
        if (timeDiff > 0) {
            timeString = String.format("%1$tT", timeDiff - TimeZone.getDefault().getRawOffset());
        }
        String name2 = portal;
        
        String name1 = "";
        if (name2.length() > MAX_LEN) {
            //int separator = max;
            int n = name2.lastIndexOf(' ', MAX_LEN);
            if (n < MAX_LEN - 20 || n < 0) {
                n = MAX_LEN;
            }
            name1 = name2.substring(0, n);
            name2 = name2.substring(n + 1);
        }
        String archivedString = archived.english;
        if (archived.isSuccess() || this.pagesArchived > 0) {
            archivedString = String.format("%1$s(%2$d)", archived.english, this.pagesArchived);
        }
        line = String.format("%1$-90s %2$-9s %3$9s %4$3d %5$-3s  %6$-8s %7$2d %8$-13s", 
                name2, status, timeString,
                this.newPagesFound, updated.english, 
                archivedString,
                this.errors, this.error.toString());
        if (!name1.isEmpty()) {
            line = name1 + System.lineSeparator() + line;
        }
        return line;
    }

    /**
     * Generates reporting result line of this portal page in Wiki format (table item).
     *
     * @param lineNum line number.
     */
    public String toStringWiki(int lineNum) {
        initStatics();
        Localizer localizer = Localizer.getInstance();
        String line = "";
        String timeString = "N/A";
        if (timeDiff > 0) {
            timeString = String.format("%1$tT", timeDiff - TimeZone.getDefault().getRawOffset());
        }
        String upd = wikiYesNoCancelStringRu(
                localizer.localize(updated.russian), updated.isSuccess(), updated.isFailure());
        String arch = wikiYesNoCancelStringRu(
                localizer.localize(archived.russian), archived.isSuccess(), archived.isFailure());
        if (archived.isSuccess() || this.pagesArchived > 0) {
            arch = String.format("%1$s (%2$2d)", arch, this.pagesArchived);
        }
        //| 2 ||align='left'| {{user|Игорь Васильев}}
        String errorStr = wikiErrorStringRu(error.toString(), error != BotError.NONE);
        String statusStr = wikiYesNoCancelStringRu(status.toString(), status.isSuccess(),
                status.isFailure());
        line = String.format(
                "|-\n|%9$d ||align='left'| [[%1$s]] || %10$d || %2$s || %3$s || %4$d || %5$s " +
                "|| %6$s || %7$d || %8$s", 
                portal, statusStr, timeString, 
                newPagesFound, upd, arch,
                errors, errorStr,
                lineNum, times);
        return line;
    }

    /**
     * Call this if skipping this portal page.
     */
    public void skip() {
        this.status = Status.SKIP;
        this.times = 0;
    }

    /**
     * Call this when portal page was really updated.
     */
    public void updated() {
        this.status = Status.UPDATED;
    }

    /**
     * Call this when portal page was processed.
     */
    public void processed() {
        this.status = Status.PROCESSED;
        this.times++;
    }

    public void willUpdateNewPages() {
        this.updated = UpdateStatus.NO;
    }

    public void newPagesUpdated(int count) {
        this.updated = UpdateStatus.YES;
        this.newPagesFound += count;
    }

    public void newPagesUpdateError() {
        this.updated = UpdateStatus.ERROR;
    }

    public void willUpdateArchive() {
        this.archived = UpdateStatus.NO;
    }

    public void archiveUpdated(int count) {
        this.archived = UpdateStatus.YES;
        this.pagesArchived = count;
    }

    public void archiveUpdateError() {
        this.archived = UpdateStatus.ERROR;
    }

    /**
     * Report error status.
     */
    public void error() {
        this.status = Status.ERROR;
    }

    /**
     * Report error status with a known error code.
     * @param error Error code.
     */
    public void error(BotError error) {
        this.status = Status.ERROR;
        this.error = error;
    }
    
    /**
     * Compares 2 report items and returns <code>true</true> if they reference the same reported
     * project page.
     */
    public boolean theSameSource(ReportItem second) {
        if (!template.equals(second.template)) {
            return false;
        }
        if (!portal.equals(second.portal)) {
            return false;
        }
        return true;
    }

    /**
     * Call this when starting updating this portal page.
     */
    public void start() {
        startTime = getCurrentTimeMillis();
    }

    /**
     * Call this when restarting update task (retry).
     */
    public void restart() {
        if (updated.isFailure()) {
            updated = UpdateStatus.NONE;
        }
    }

    /**
     * Call this after finishing updating this portal page.
     */
    public void end() {
        endTime = getCurrentTimeMillis();
        timeDiff = endTime - startTime;
    }

    /**
     * Merges this reporting item with another item.
     * Merge is allowed for the same portal page only.
     * Numberic values are summed.
     * Enum/String values are downgraded in favor of a worse one.
     */
    public void merge(ReportItem right) {
        assert template.equals(right.template) && portal.equals(right.portal);
        newPagesFound += right.newPagesFound;
        pagesArchived += right.pagesArchived;
        times += right.times;
        errors += right.errors;
        settingsValid = settingsValid || right.settingsValid;
        updated = UpdateStatus.selectBest(updated, right.updated);
        archived = UpdateStatus.selectBest(archived, right.archived);
        if (timeDiff > 0 && right.timeDiff > 0) {
            timeDiff = (timeDiff + right.timeDiff) / 2;
        } else if (right.timeDiff > 0) {
            timeDiff = right.timeDiff;
        }
        if (error == BotError.NONE && right.error != BotError.NONE) {
            error = right.error;
        }
        status = Status.selectBest(status, right.status);
    }
    
    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder
                .append("ReportItem[")
                .append("template: ")
                .append(template)
                .append(", portal: ")
                .append(portal)
                .append(", status: ")
                .append(status)
                .append(", error: ")
                .append(error)
                .append(", time diff: ")
                .append(timeDiff)
                .append(", times: ")
                .append(times)
                .append(", errors: ")
                .append(errors)
                .append(", updated: ")
                .append(updated)
                .append(", archived: ")
                .append(archived)
                .append(", settingsValid: ")
                .append(settingsValid)
                .append(", settingsValid: ")
                .append(settingsValid)
                .append(", newPagesFound: ")
                .append(newPagesFound)
                .append(", pagesArchived: ")
                .append(pagesArchived)
                .append("]");
        return strBuilder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, true);
    }
}