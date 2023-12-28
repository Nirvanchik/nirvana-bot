/**
 *  @(#)StatisticsWeek.java
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
 * Statistics table where each row shows stat per week.
 *
 */
public class StatisticsWeek extends Statistics {
    int year;
    // This is a very ugly workaround to deal with the first 3 days of the year.
    // Если мы трактуем недели года по ГОСТ ИСО 8601-2001
    // с отсчётом от понедельника до воскресенья (либо от воскресенья до субботы для США)
    // и год начался к примеру в пятницу, то согласно ГОСТ, первой неделей будет считаться
    // не Пт-Вс (1-7 января), а Пн-Вс (4-10 января). Т.к. минимальное число для подсчёта недели
    // - это 4 дня.
    // Но при таком подходе, мы будем терять статистику за 1-3 дня вникуда.
    // Текущий (грязный) костыль заключается в том, что неделя 0 (несуществующая), 
    // просто прячется из таблицы.
    // Но в суммарные данные она всё равно засчитывается.
    public static final boolean HIDE_WEEK_0 = true;


    /**
     * Constructs class instance to generate global statistics.
     */
    public StatisticsWeek(NirvanaWiki wiki, String cacheDir, String type, SystemTime systemTime,
            int year) throws BadAttributeValueExpException, IOException {
        super(wiki, cacheDir, type, systemTime);
        this.year = year;
    }

    /**
     * Constructs class instance to generate one-year statistics.
     */
    public StatisticsWeek(NirvanaWiki wiki, String cacheDir, String type, SystemTime systemTime)
            throws BadAttributeValueExpException, IOException {
        super(wiki, cacheDir, type, systemTime);
        this.year = 0;
    }

    @Override
    public void put(ArchiveDatabase2 db) throws IllegalStateException {
        Calendar c = systemTime.now();
        c.setFirstDayOfWeek(dateTools.getFirstDayOfWeek());
        c.setMinimalDaysInFirstWeek(dateTools.getMinimalDaysInFirstWeek());

        // 1) find out period of the first week
        ListIterator<ArchiveItem> it = null;
        if (this.year == 0) it = db.getIterator();
        else it = db.getYearIterator(year);
        HashMap<String,Integer> userstat = new HashMap<String,Integer>(100);
        //HashMap<Integer,Integer> articlestat = new HashMap<Integer,Integer>(7);
        int[] articlestat = new int[7];
        Arrays.fill(articlestat, 0);

        
        int total = 0;
        int curWeek = 0;
        int y = 0;
        ArchiveItem item = null;
        if (it.hasNext()) item = it.next();
        //else return;
        if (item != null && this.year != 0 && item.year != this.year) {
            throw new IllegalStateException("year processing: " + this.year + ", item's year: " +
                    item.year + ", item: " + item.article);
        }
        if (item != null) {
            // DateTools.dayToWeek(item.year, item.month, item.day);
            y = item.year;
            c.clear();
            c.set(item.year, item.month, item.day, 0, 0, 0);

            curWeek = c.get(Calendar.WEEK_OF_YEAR);
            if (item.month == 0 && item.day <= 7 && curWeek > 50) {
                curWeek = 0;
            }
            int wday = c.get(Calendar.DAY_OF_WEEK) - 1;  
            userstat.put(item.user, 1);
            articlestat[wday] = 1;
            total = 1;
        }

        while (it.hasNext()) {
            item = it.next();
            if (this.year != 0 && item.year != this.year) {
                throw new IllegalStateException("year processing: " + this.year +
                        ", item's year: " + item.year + ", item: " + item.article);
            }
            c.clear();
            c.set(item.year, item.month, item.day, 0, 0, 0);
            //int week = DateTools.dayToWeek(item.year, item.month, item.day);
            int week = c.get(Calendar.WEEK_OF_YEAR);
            
            if (item.month == 0 && item.day <= 7 && week > 50) {
                week = 0;
            }
            //boolean ny = (this.year != 0 && item.year != y);
            if (week != curWeek) {
                // calculate stat and add to list 
                this.addStat(y, curWeek, total, articlestat, userstat);
                curWeek = week;
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
            int wday = c.get(Calendar.DAY_OF_WEEK) - 1; 
            articlestat[wday]++;
            total++;
        }
        // last stat
        if (item != null) {
            this.addStat(y, curWeek, total, articlestat, userstat);
        }
        
        calcTotal();
    }


    void addStat(int year, int week, int total, int[] articlestat, Map<String,Integer> userstat) {
        StatItem weekStat = new StatItem();
        weekStat.number = week;            
        weekStat.month = dateTools.weekToMonth(year, week);
        weekStat.year = year;
        weekStat.articles = total;
        
        int min = 1000000;
        int max = 0;
        int sum = 0;
        for (int n: articlestat) {
            sum += n;
            if (n < min) min = n;
            if (n > max) max = n;
        }
        weekStat.articlesDayMid = (float) (sum / 7.0);
        weekStat.articlesDayMax = max;
        weekStat.articlesDayMin = min;
        String user = "nobody";
        int userc = -1;
        for (Map.Entry<String, Integer> entry : userstat.entrySet()) {
            if (entry.getValue() > userc) {
                user = entry.getKey();
                userc = entry.getValue();
            }
        }            
        weekStat.user = user;
        weekStat.userArticles = userc;
        if (!items.isEmpty()) {
            StatItem prev = this.items.get(items.size() - 1);
            weekStat.progress = weekStat.articles - prev.articles;
            // weekStat.progress = (int) ((prev.articles - weekStat.articles)
            // / (prev.articles == 0?1:(prev.articles*0.1)));
        }
        this.items.add(weekStat);
        addUserStat(userstat);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(customHeader);
        sb.append(header);
        sb.append(DELIMETER);
        for (int i = 0; i < items.size(); i++) {
            StatItem item = items.get(i);
            // week 0 is a fake week that hides 1-3 days of year beginning
            // that do not counted into week 1 of the year. So we hide it.
            // (here we count weeks in terms of business week).
            if (item.number == 0 && items.size() > 1 && HIDE_WEEK_0) continue;
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
}
