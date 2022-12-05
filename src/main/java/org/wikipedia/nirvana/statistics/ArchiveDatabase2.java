/**
 *  @(#)ArchiveDatabase.java
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

import org.wikipedia.nirvana.util.FileTools;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * Database that used to keep archives of new pages created in technically readable format
 * (currently - Json). This db saves data and reads them very fast, so helps to speed up
 * statistics bot dramatically and reduce HTTP traffic as well.
 * It's more correct to call this class ArchivesCache, so it will may be renamed.
 *
 * When filling this db with data, each year (and year after the last) 
 * must be marked by calling markYearBegin()
 */
public class ArchiveDatabase2 {
    private final Logger log;

    private static final String V1_JS_READ_ERROR_MESSAGE = 
            "Can not deserialize instance of "
            + "org.wikipedia.nirvana.statistics.ArchiveDatabase2$JsonDb out of START_ARRAY token";
    /**
     * First version.
     */
    public static final int V1 = 1;

    /**
     * Second version: 
     * - added version number
     * - added size to ArchiveItem.
     */
    public static final int V2 = 2;

    String name;
    String dbPath;
    ArrayList<ArchiveItem> items;
    ArchiveItem lastItemFromCache = null;
    ArchiveItem max = null;
    Map<Integer,Integer> yearIndexes = null;

    boolean sorted = true;
    private int version = V1;
    
    /**
     * Parsed database data keeper object.
     */
    static class JsonDb {
        public int version;
        public ArrayList<ArchiveItem> items;
        
        public JsonDb(int version, ArrayList<ArchiveItem> items) {
            this.version = version;
            this.items = items;
        }

        public JsonDb(int version, List<ArchiveItem> items) {
            this.version = version;
            this.items = new ArrayList<ArchiveItem>(items.size());
            this.items.addAll(items);
        }

        public JsonDb() {

        }
    }
    
    /**
     * Iterator of DB data extracted only for one specific year.
     * This class is not year-safe
     * the year must be in database
     * */
    public class ArchiveDBYearIterator implements ListIterator<ArchiveItem> {
        protected ListIterator<ArchiveItem> iterator;
        int endIndex = -1;
        int year;

        ArchiveDBYearIterator(int year) {
            this.year = year;
            Integer index = yearIndexes.get(year);
            if (index == null) {
                iterator = items.listIterator(items.size());
                endIndex = items.size();
            } else {
                iterator = items.listIterator(index);
                index = yearIndexes.get(year + 1);
                if (index == null) endIndex = items.size();
                else endIndex = index;
            }
        }

        @Override
        public boolean hasNext() {
            if (iterator.hasNext()) {
                return iterator.nextIndex() != endIndex;
            } else {
                return false;
            }
        }

        @Override
        public void add(ArchiveItem item) {
            iterator.add(item);
        }

        @Override
        public boolean hasPrevious() {
            return iterator.hasPrevious();
        }

        @Override
        public int nextIndex() {
            return iterator.nextIndex();
        }

        @Override
        public ArchiveItem previous() {
            return iterator.previous();
        }

        @Override
        public int previousIndex() {
            return iterator.previousIndex();
        }

        @Override
        public void remove() {
            iterator.remove();
        }

        @Override
        public void set(ArchiveItem item) {
            iterator.set(item);
        }

        @Override
        public ArchiveItem next() {
            return iterator.next();
        }
    }

    ArchiveDatabase2(String name, boolean loadFromCache, String cacheFolder, boolean withSize) {
        this.name = name;
        checkCreateDir(cacheFolder);

        dbPath = cacheFolder + "\\" + FileTools.normalizeFileName(name) + ".js";
        log = LogManager.getLogger(this.getClass().getName());
        items = new ArrayList<ArchiveItem>(50000);
        yearIndexes = new HashMap<Integer,Integer>(5);
        if (withSize) {
            version = V2;
        }
        if (loadFromCache) {
            load();
            markYears();
        }
        
    }

    private void checkCreateDir(String dir) {
        File f = new File(dir);
        if (!f.exists()) {
            f.mkdirs();
            return;
        }

        assert f.isDirectory();
    }

    public boolean withSize() {
        return (version == V2);
    }

    /**
     * Loads database from local file system.
     */
    public void load()    {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(dbPath);
        if (!file.exists()) {
            log.warn("cache file {} does not exist", dbPath);
            return;
        }
        JsonDb jsonDb = null;
        try {
            jsonDb = mapper.readValue(
                    file, 
                    new TypeReference<JsonDb>() { });
        } catch (JsonParseException e) {
            log.error(e);
            return;
        } catch (JsonMappingException e) {
            if (e.getMessage().contains(V1_JS_READ_ERROR_MESSAGE)) {
                // Читаем первую версию, которая не содержит номера версии и самой структуры JsonDb
                List<ArchiveItem> list = null;
                try {
                    list = mapper.readValue(
                            file, 
                            new TypeReference<List<ArchiveItem>>() { });
                } catch (IOException e1) {
                    log.error(e1);
                    return;
                }
                jsonDb = new JsonDb(V1, list);
            } else {
                log.error(e);
                return;
            }
        } catch (IOException e) {
            log.error(e);
            return;
        }
        if (jsonDb.version < this.version) {
            log.warn(String.format("Error: archive outdated! \r\n"
                    + "Archive %s has version %d but we need version %d",
                    dbPath, jsonDb.version, this.version));
            log.info("db loading skipped");
            return;
        }

        if (jsonDb.items.isEmpty()) {
            log.warn(String.format("Archive db %s is empty", dbPath));
            return;
        }
        log.info("{} items loaded from {}", String.valueOf(jsonDb.items.size()), dbPath);
        this.items.addAll(jsonDb.items);
        lastItemFromCache = this.items.get(items.size() - 1);
        log.info("db loaded successfully. db size: {} items", items.size());
    }

    public ArchiveItem getLastFromCache() {
        return lastItemFromCache;
    }

    /**
     * Saves database to local file system.
     * Does not throw exception if error happens (only prints error message to log).
     */
    public void save() {
        ObjectMapper mapper = new ObjectMapper();
        JsonDb db = new ArchiveDatabase2.JsonDb(version, items);
        try {
            mapper.writeValue(new File(dbPath), db);
        } catch (JsonParseException e) {
            log.error(e);
            return;
        } catch (JsonMappingException e) {
            log.error(e);
            return;
        } catch (IOException e) {
            log.error(e);
            return;
        }
        log.info("db successfully saved to {}", dbPath);
    }
    
    /**
     * Sorts all database items.
     * Sort order is always the same - by page creation date (historical order).
     * Database must be sorted before generating statistics to make correct reports.
     */
    public void sort() {
        log.info("db sort started");
        java.util.Collections.sort(items, new Comparator<ArchiveItem>() {
            @Override
            public int compare(ArchiveItem item1, ArchiveItem item2) {
                return (int)(item1.getDateAsInt() - item2.getDateAsInt());
            }
        });
        log.info("db sorted successfully");
        this.sorted = true;
    }
    
    /**
     * Adds archive item to database.
     */
    public void add(ArchiveItem item) {
        this.items.add(item);
        // new incoming item mast be newer than all in database otherwise
        // sort order will be broken and functionality fail
        if (this.max != null) {
            int i = item.getDateAsInt() - max.getDateAsInt();
            if (i > 0) {
                max = item;
            } else if (i < 0) {
                this.sorted = false;
            }
        } else {
            max = item;
        }
    }

    boolean isSorted() {
        return this.sorted;
    }

    boolean isEmpty() {
        return this.items.isEmpty();
    }

    /**
     * Must be called before calling the first add() for this year.
     */
    public void markYearBegin(int year) {
        if (!yearIndexes.keySet().contains(year)) {
            this.yearIndexes.put(year, this.items.size());
        }
    }

    private void markYearPos(int year, int pos) {
        if (!yearIndexes.keySet().contains(year)) {
            this.yearIndexes.put(year, pos);
        }
    }

    /**
     * Should be called when database finished updating with new data.
     */
    public void markEnd() {
        Set<Integer> years = this.yearIndexes.keySet();
        Iterator<Integer> i = years.iterator();
        int maxYear = 0;
        while (i.hasNext()) {
            int y = i.next();
            if (y > maxYear) maxYear = y;
        }
        this.yearIndexes.put(maxYear + 1, items.size());
    }

    /**
     * Fill year indexes (scan all archive).
     */
    public void markYears() {
        if (items.isEmpty()) return;
        this.yearIndexes.clear();
        ArchiveItem first = items.get(0);
        markYearPos(first.year,0);
        int year = first.year;
        for (int i = 0; i < items.size(); i++) {
            ArchiveItem item = items.get(i);
            if (item.year > year) {
                // Some random year may break our year ordering
                year = item.year; // year++;
                markYearPos(year, i);
            } else if (item.year < year) {
                this.sorted = false;
            }
        }
    }

    /**
     * Fill year indexes incrementally (scan new part of archive only). 
     */
    public void markYearsWhichNotMarked() {
        Set<Integer> years = this.yearIndexes.keySet();
        Iterator<Integer> it = years.iterator();
        int maxYear = 0;
        while (it.hasNext()) {
            int y = it.next();
            if (y > maxYear) maxYear = y;
        }
        int year = maxYear;
        int i = this.yearIndexes.get(year);
        for (; i < items.size(); i++) {
            ArchiveItem item = items.get(i);
            if (item.year > year) {
                //year=item.year; some random year may break our year ordering
                year = item.year; // year++;
                markYearPos(year, i);
            } else if (item.year < year) {
                this.sorted = false;
            }
        }
    }

    public ListIterator<ArchiveItem> getIterator() {
        return items.listIterator();
    }

    /**
     * Returns iterator that iterates items starting from specific year.
     *
     * @param startYear year must be in the database
     */
    public ListIterator<ArchiveItem> getIterator(int startYear) {
        ListIterator<ArchiveItem> iterator = null;
        iterator = items.listIterator(yearIndexes.get(startYear));
        return iterator;
    }
    
    /**
     * Returns iterator that iterates items created in specific year.
     *
     * @param year year must be in the database
     */
    public ListIterator<ArchiveItem> getYearIterator(int year) {
        return new ArchiveDBYearIterator(year);
    }

    /**
     * Removes items created in current year.
     */
    public void removeItemsFromLastYear() {
        if (this.lastItemFromCache == null) return;
        Integer pos = this.yearIndexes.get(lastItemFromCache.year);
        for (int i = items.size() - 1; i >= pos; i--) {
            items.remove(i);
        }
    }

    /**
     * Removes items with a creation date from last quarter of current year.
     */
    public void removeItemsFromLastQuarter() {
        if (this.lastItemFromCache == null) return;
        int ypos = this.yearIndexes.get(lastItemFromCache.year);
        int q = lastItemFromCache.getQuarter();
        int m = (q - 1) * 3;
        int pos = ypos;
        for (; pos < items.size(); pos++) {
            ArchiveItem item = items.get(pos);
            if (item.month == m) break;
        }
        for (int i = items.size() - 1; i >= pos; i--) {
            items.remove(i);
        }
    }

    public void removeAllItems() {
        items.clear();
    }
}
