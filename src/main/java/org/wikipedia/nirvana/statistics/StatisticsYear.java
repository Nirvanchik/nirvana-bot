/**
 *  @(#)StatisticsYear.java
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

import org.wikipedia.nirvana.nirvanabot.NirvanaBot;
import org.wikipedia.nirvana.util.SystemTime;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import javax.management.BadAttributeValueExpException;

/**
 * Statistics table where each row shows statistics per year.
 *
 */
public class StatisticsYear extends Statistics {
    String reportTemplate = "";
    int startYear = 2008;

    /**
     * Constructs class instance to generate statistics.
     */
    public StatisticsYear(NirvanaWiki wiki, String cacheDir, String type, SystemTime systemTime)
            throws BadAttributeValueExpException, IOException {
        super(wiki, cacheDir, type, systemTime);
        startYear = 2008;
    }

    @Override
    public void setOptions(Map<String,String> options) {
        super.setOptions(options);
        String key = "отчет";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            this.reportTemplate = options.get(key);
        }
        key = "первый год";
        if (options.containsKey(key) && !options.get(key).isEmpty()) {
            try {
                this.startYear = Integer.parseInt(options.get(key));
            } catch (NumberFormatException e) {
                log.warn(String.format(NirvanaBot.ERROR_PARSE_INTEGER_FORMAT_STRING_EN, key,
                        options.get(key)));
            }
        }
    }

    @Override
    public void put(ArchiveDatabase2 db) throws IllegalStateException {
        Calendar c = Calendar.getInstance();
        // 1) find out period of the first week
        ListIterator<ArchiveItem> it = null;
        it = db.getIterator();
        HashMap<String,Integer> userstat = new HashMap<String,Integer>(100);
        //HashMap<Integer,Integer> articlestat = new HashMap<Integer,Integer>(7);
        int[] articlestat = new int[366];
        Arrays.fill(articlestat, 0);
        
        int total = 0;
        int y = 0;
        int day = 0;
        ArchiveItem item = null;
        while (it.hasNext()) {
            item = it.next();
            y = item.year;
            if (y >= this.startYear) { // skip articles before the required year
                userstat.put(item.user, 1);
                c.set(item.year, item.month, item.day);
                day = c.get(Calendar.DAY_OF_YEAR);
                articlestat[day - 1] = 1;
                total = 1;
                break;
            }
        }

        while (it.hasNext()) {
            item = it.next();
            if (item.year != y) {
                // calculate stat and add to list 
                this.addStat(y, total, articlestat, userstat);
                y = item.year;
                // zero stat
                total = 0;
                Arrays.fill(articlestat, 0);
                userstat.clear();
            } 
            // add item to current stat
            Integer n = userstat.get(item.user);
            if (n == null) userstat.put(item.user, 1);
            else userstat.put(item.user, n + 1);
            c.set(item.year, item.month, item.day);
            day = c.get(Calendar.DAY_OF_YEAR);
            articlestat[day - 1]++;
            total++;
        }
        // last stat
        if (item != null) {
            this.addStat(y, total, articlestat, userstat);
        }

        calcTotal();
    }

    void addStat(int year, int total, int[] articlestat, Map<String,Integer> userstat) {
        log.debug("adding item: year={} total: {}", year, total);
        StatItem stat = new StatItem();
        stat.number = items.size() + 1;
        stat.month = 0;
        stat.year = year;
        stat.articles = total;

        int min = 1000000;
        int max = 0;
        int sum = 0;
        Calendar c = Calendar.getInstance();
        c.set(year, 1, 1);
        int mdays = c.getActualMaximum(Calendar.DAY_OF_YEAR);
        for (int i = 0; i < mdays; i++) {
            int n = articlestat[i];
            sum += n;
            if (n < min) min = n;
            if (n > max) max = n;
        }
        stat.articlesDayMid = ((float)sum / (float)mdays);
        stat.articlesDayMax = max;
        stat.articlesDayMin = min;
        String user = "nobody";
        int userc = -1;
        for (Map.Entry<String, Integer> entry : userstat.entrySet()) {
            if (entry.getValue() > userc) {
                user = entry.getKey();
                userc = entry.getValue();
            }
        }
        stat.user = user;
        stat.userArticles = userc;
        if (!items.isEmpty()) {
            StatItem prev = this.items.get(items.size() - 1);
            stat.progress = stat.articles - prev.articles;
        }
        this.items.add(stat);
        addUserStat(userstat);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(customHeader);
        sb.append(header);
        sb.append(DELIMETER);
        for (StatItem item:items) {
            String str = item.toString();
            str = str.replace("%(отчет)", 
                    this.reportTemplate.replace("%(год)", String.valueOf(item.year)));
            sb.append(str);
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

}
