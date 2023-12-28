/**
 *  @(#)Statistics.java
 *  Copyright © 2023 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.wikipedia.nirvana.base.BotFatalError;
import org.wikipedia.nirvana.util.DateTools;
import org.wikipedia.nirvana.util.FileTools;
import org.wikipedia.nirvana.util.SystemTime;
import org.wikipedia.nirvana.util.TextUtils;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.BadAttributeValueExpException;


/**
 * Base class for statistics report generators.
 * Implements logic that is common for all report types.
 * Provides public API methods for external usage.
 *
 * Provides easy API methods for higher level code:
 * 1) put() method to load data from database
 * 2) toString() method to generate Wiki page text with statistics table. 
 *
 */
public class Statistics {    
    protected final Logger log;

    public static final String DELIMETER = "\n";
    public static final String DEFAULT_INI_FOLDER = "statistics";
    protected final String cacheDir;
    public static String iniFolder = DEFAULT_INI_FOLDER;
    public static String portal = "";

    String header;
    String footer;
    String customHeader = "";
    String customFooter = "";
    String totalTemplate;
    String itemTemplate;
    String type;

    protected List<StatItem> items;
    protected Map<String,Integer> totalUserStat;
    protected TotalItem total;
    protected NirvanaWiki wiki = null;
    protected final DateTools dateTools;
    protected SystemTime systemTime;

    protected class StatItem {
        public int number;
        public int year;
        public int month;
        public String title;
        public int progress;
        public int articles;
        public int articlesDayMin;
        public int articlesDayMax;
        public float articlesDayMid;
        public String user;
        public int userArticles;

        public String toString() {
            String progress = "{{нет изменений}}";
            if (this.progress > 0) {
                progress = "{{рост}}";
            } else if (this.progress < 0) {
                progress = "{{падение}}";
            }
            String mid = "0";
            if (this.articlesDayMid >= 2.0) {
                mid = String.valueOf(Math.round(this.articlesDayMid));
            } else {
                mid = new DecimalFormat("#.#").format(this.articlesDayMid);
            }
            log.debug("item to String " + number + " year " + year + " month " + month);
            String str = itemTemplate
                    .replace("%(номер)", String.valueOf(this.number))
                    .replace("%(месяц)", dateTools.monthString(this.month))
                    .replace("%(год)", String.valueOf(this.year))
                    .replace("%(статей)", String.valueOf(this.articles))
                    .replace("%(прогресс)", progress)
                    .replace("%(MID в день)", mid)
                    .replace("%(MIN в день)", String.valueOf(this.articlesDayMin))
                    .replace("%(MAX в день)", String.valueOf(this.articlesDayMax))
                    .replace("%(участник)", this.user)
                    .replace("%(статей участника)", String.valueOf(this.userArticles));
            return str;
        }

        StatItem() {
            number = 0;
            year = 0;
            month = 0;
            title = "";
            progress = 0;
            articles = 0;
            articlesDayMin = 0;
            articlesDayMax = 0;
            articlesDayMid = 0;
            user = "";
            userArticles = 0;
        }
    }

    protected class TotalItem {
        public int articles;
        public int articlesMid;
        public int articlesDayMin;
        public int articlesDayMax;
        public float articlesDayMid;
        public String user;
        public int userArticles;

        String toString(String template) {
            String mid = "0";
            if (this.articlesDayMid >= 2.0) {
                mid = String.valueOf(Math.round(this.articlesDayMid));
            } else {
                mid = new DecimalFormat("#.#").format(this.articlesDayMid);
            }
            String str = totalTemplate
                    .replace("%(статей)", String.valueOf(this.articles))
                    .replace("%(MID в день)", mid)
                    .replace("%(MIN в день)", String.valueOf(this.articlesDayMin))
                    .replace("%(MAX в день)", String.valueOf(this.articlesDayMax))
                    .replace("%(участник)", this.user)
                    .replace("%(статей участника)", String.valueOf(this.userArticles));
            return str; 
        }

        TotalItem() {
            articles = 0;
            articlesMid = 0;
            articlesDayMin = 0;
            articlesDayMax = 0;
            articlesDayMid = 0;
            user = "";
            userArticles = 0;
        }
    }
    
    public void createTotal() {
        total = new TotalItem();
    }

    Statistics(NirvanaWiki wiki, String cacheDir, String type, SystemTime systemTime)
            throws BadAttributeValueExpException, IOException {
        this.wiki = wiki;
        this.type = type;
        log = LogManager.getLogger(this.getClass().getName());
        this.cacheDir = cacheDir;
        dateTools = DateTools.getInstance();
        this.systemTime = systemTime;
        init();
    }

    /**
     * Set options specific for this report instance (specific for project).
     */
    public void setOptions(Map<String,String> options) {        
        String key = "шапка";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            customHeader = options.get(key).replace("\\n", "\n");
        }
        key = "подвал";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            customFooter = options.get(key).replace("\\n", "\n");
        }
    }

    private void init() throws BadAttributeValueExpException, IOException {
        items = new ArrayList<StatItem>(100);
        totalUserStat = new HashMap<String,Integer>(200);
        createTotal();
        String ini = Paths.get(iniFolder, type + ".ini").toString();
        String file = FileTools.readFile(ini);
        Map<String, String> options = new HashMap<String, String>();
        if (!TextUtils.textOptionsToMap(file, options)) {
            log.error("incorrect settings for statistics");
            throw new BadAttributeValueExpException(file);
        }
        getSettingsFromIni(options);
    }

    protected void getSettingsFromIni(Map<String, String> options)
            throws BadAttributeValueExpException {
        
        header = options.get("шапка");
        itemTemplate = options.get("формат элемента");
        footer = options.get("подвал");
        totalTemplate = options.get("итого");        
        if (header == null || itemTemplate == null || footer == null) {
            log.error("incorrect settings for statistics");
            throw new BadAttributeValueExpException(this.type);
        }
        header = header.replace("\\n", "\n");
        itemTemplate = itemTemplate.replace("\\n", "\n");
        footer = footer.replace("\\n", "\n");
        if (totalTemplate != null) totalTemplate = totalTemplate.replace("\\n", "\n");
    }

    /**
     * Load statistics data from archive database.
     */
    public void put(ArchiveDatabase2 db) throws BotFatalError {}

    protected void calcTotal() {
        if (this.items.isEmpty()) {
            return;
        }
        this.total.articlesDayMin = 1000000;
        float midday = 0;
        for (StatItem stat: this.items) {
            if (stat.articlesDayMin < total.articlesDayMin) {
                total.articlesDayMin = stat.articlesDayMin;
            }
            if (stat.articlesDayMax > total.articlesDayMax) {
                total.articlesDayMax = stat.articlesDayMax;
            }
            total.articles += stat.articles;
            midday += stat.articlesDayMid;
        }
        total.articlesDayMid = midday / this.items.size();
        total.articlesMid = total.articles / this.items.size();        
        total.userArticles = 0;
        // new version of total user stat
        String user = "nobody";
        int n = -1;
        for (Map.Entry<String, Integer> entry : this.totalUserStat.entrySet()) {
            if (entry.getValue() > n) {
                user = entry.getKey();
                n = entry.getValue();
            }
        }
        this.total.user = user;
        this.total.userArticles = n;
    }

    /**
     * Get statistics table in wiki text format. 
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(customHeader);
        sb.append(header);
        sb.append(DELIMETER);
        for (StatItem item: items) {
            sb.append(item.toString());
            sb.append(DELIMETER);
        }        
        if (this.totalTemplate != null && !totalTemplate.isEmpty()) {
            sb.append(total.toString(totalTemplate));
            sb.append(DELIMETER);
        }
        sb.append(footer);
        sb.append(customFooter);
        return sb.toString();
    }
    
    protected void addUserStat(Map<String,Integer> userstat) {
        for (Map.Entry<String, Integer> entry : userstat.entrySet()) {
            if (entry.getValue() > total.userArticles) {
                String user = entry.getKey();
                int a = entry.getValue();
                Integer n = this.totalUserStat.get(user);
                if (n == null) this.totalUserStat.put(user, a);
                else this.totalUserStat.put(user, n + a);
            }
        }
    }
    
    public Map<String,Integer> getUserStat() {
        return this.totalUserStat;
    }
}
