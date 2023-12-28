/**
 *  @(#)StatisticsMonth.java
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
 * Statistics table where each row show stat per month.
 *
 */
public class StatisticsMonth extends Statistics {
    int year = 0;

    /**
     * Constructs class instance to generate global statistics. 
     */
    public StatisticsMonth(NirvanaWiki wiki, String cacheDir, String type, SystemTime systemTime)
            throws BadAttributeValueExpException, IOException {
        super(wiki, cacheDir, type, systemTime);
        year = 0;
    }
    
    /**
     * Constructs class instance to generate one-year statistics. 
     */
    public StatisticsMonth(NirvanaWiki wiki,  String cacheDir, String type, SystemTime systemTime,
            int year) throws BadAttributeValueExpException, IOException {
        super(wiki, cacheDir, type, systemTime);
        this.year = year;
    }

    /**
     * Load data from database to statistics.
     */
    public void put(ArchiveDatabase2 db) throws IllegalStateException {
        // 1) find out period of the first week
        ListIterator<ArchiveItem> it = null;
        if (this.year == 0) it = db.getIterator();
        else it = db.getYearIterator(year);
        HashMap<String,Integer> userstat = new HashMap<String,Integer>(100);
        int[] articlestat = new int[31];
        Arrays.fill(articlestat, 0);
        
        int total = 0;
        int curMonth = 0;
        int y = 0;
        ArchiveItem item = null;
        if (it.hasNext()) {
            item = it.next();
            if (this.year != 0 && item.year != this.year) {
                throw new IllegalStateException("year processing: " + this.year +
                        ", item's year: " + item.year + ", item: " + item.article);
            }
            y = item.year;            
            curMonth = item.month;
            userstat.put(item.user, 1);
            articlestat[item.day - 1] = 1;
            total = 1;
        }

        while (it.hasNext()) {
            item = it.next();
            if (this.year != 0 && item.year != this.year) { 
                throw new IllegalStateException("year processing: " + this.year +
                        ", item's year: " + item.year + ", item: " + item.article);
            }
            if (item.month != curMonth) {
                // calculate stat and add to list 
                this.addStat(y, curMonth, total, articlestat, userstat);
                curMonth = item.month;
                if (y != item.year) y = item.year;
                // zero stat
                total = 0;
                Arrays.fill(articlestat, 0);
                userstat.clear();
            } 
            // add item to current stat            
            Integer n = userstat.get(item.user);
            if (n == null) userstat.put(item.user, 1);
            else userstat.put(item.user, n + 1);            
            articlestat[item.day - 1]++;
            total++;            
        }
        // last stat
        if (item != null) {
            this.addStat(y, curMonth, total, articlestat, userstat);
        }
        
        calcTotal();
    }
    
    
    void addStat(int year, int month, int total, int[] articlestat, Map<String, Integer> userstat) {
        StatItem stat = new StatItem();
        stat.number = month + 1;    
        stat.month = month;
        stat.year = year;
        stat.articles = total;
        
        int min = 1000000;
        int max = 0;
        int sum = 0;
        Calendar c = Calendar.getInstance();
        c.set(year, month, 1);
        int mdays = c.getActualMaximum(Calendar.DAY_OF_MONTH);
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

}
