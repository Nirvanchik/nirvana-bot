/**
 *  @(#)StatisticsParam.java 
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

import org.wikipedia.nirvana.archive.ScanArchiveSettings;

import java.util.List;

/**
 * Keeps project settings used to produce statistics pages.
 *
 */
public class StatisticsParam {
    public ScanArchiveSettings archiveSettings;
    public String archive;
    public List<String> reportTypes;
    public boolean sort;
    public boolean cacheonly;
    public boolean cache;
    public boolean filterBySize;
    public int minSize;
    public String comment;
    public String header;
    public String footer;

}
