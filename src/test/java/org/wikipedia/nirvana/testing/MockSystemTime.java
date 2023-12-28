/**
 *  @(#)MockSystemTime.java
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

package org.wikipedia.nirvana.testing;

import org.wikipedia.nirvana.util.SystemTime;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Mocking version of {@link SystemTime} used for testing.
 *
 */
public class MockSystemTime extends SystemTime {
    private Calendar calendar;
    private OffsetDateTime dateTime;
    private long timeMillis;
    
    private List<Long> waitTimeTicks = new ArrayList<>();
    private SleepCallback sleepCallback;
    
    public interface SleepCallback {
        void beforeSleep(long currentTime, long sleepTime);

        void afterSleep(long currentTime, long sleepTime);
    }

    /**
     * Default constructor.
     */
    public MockSystemTime() {
        this(OffsetDateTime.of(2023, 12, 2, 0, 0, 0, 0, ZoneOffset.UTC));
    }

    /**
     * Default constructor.
     */
    public MockSystemTime(OffsetDateTime offsetDateTime) {
        dateTime = offsetDateTime;
        calendar = Calendar.getInstance();
        calendar.setTime(Date.from(dateTime.toInstant()));
        timeMillis = dateTime.toInstant().toEpochMilli();
    }

    /**
     * Constructs object initialized with time in milliseconds since Epoch. 
     */
    public MockSystemTime(long timeMillis) {
        dateTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(timeMillis), ZoneOffset.UTC);
        calendar = Calendar.getInstance();
        calendar.setTime(Date.from(Instant.ofEpochMilli(timeMillis)));
        this.timeMillis = timeMillis;
    }

    @Override
    public Calendar now() {
        return calendar;
    }

    @Override
    public OffsetDateTime nowOdt() {
        return dateTime;
    }

    @Override
    public long currentTimeMillis() {
        return timeMillis;
    }

    @Override
    public void sleep(long millis) throws InterruptedException {
        if (this.sleepCallback != null) {
            this.sleepCallback.beforeSleep(timeMillis, millis);
        }
        timeMillis += millis;
        waitTimeTicks.add(millis);
        if (this.sleepCallback != null) {
            this.sleepCallback.afterSleep(timeMillis, millis);
        }
    }
    
    public List<Long> getTimeTicks() {
        return waitTimeTicks;
    }
    
    public void reset() {
        waitTimeTicks.clear();
    }
    
    public void setSleepCallback(SleepCallback callback) {
        this.sleepCallback = callback;
    }
}
