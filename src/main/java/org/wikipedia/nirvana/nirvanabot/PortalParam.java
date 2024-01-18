/**
 *  @(#)PortalParam.java 14.12.2014
 *  Copyright © 2012 - 2014 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.nirvanabot;

import org.wikipedia.nirvana.archive.ArchiveSettings;
import org.wikipedia.nirvana.nirvanabot.templates.TemplateFilter;
import org.wikipedia.nirvana.wiki.CatScanTools;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Keeps bot settings for portal page to update.
 * Most of settings are bot global defaults, some settings are overridden by wiki users for
 * specific portal page.
 * POJO class.
 *
 */
public class PortalParam {
    public String lang;
    public List<String> categories; 
    public List<String> categoriesToIgnore;
    public String page;
    public CatScanTools.Service service;
    @Nullable
    public ArchiveSettings archSettings;
    public int ns;
    public int depth;
    public int hours;
    public int maxItems;
    public String format;
    public String delimeter;
    @Nonnull
    public String header = "";
    @Nonnull
    public String footer = "";
    public boolean minor;
    public boolean bot;
    public boolean fastMode;
    public String imageSearchTags;
    public String fairUseImageTemplates;
    public int updatesPerDay;
    public TemplateFilter templateFilter = null;
    public String prefix;
    public int tryCount = 1;
    public int catscanTryCount = 1;

    public static final int MAX_CAT_GROUPS = 20;

    public List<List<String>> categoryGroups;
    public List<List<String>> categoryToIgnoreGroups;

    public static final int RENAMED_NEW = 2;

    /**
     * Default constructor.
     */
    public PortalParam() {
        lang = "ru";
        minor = true;
        bot = true;
        imageSearchTags = null;
        fairUseImageTemplates = null;
        updatesPerDay = 1;
        delimeter = "\n";
        categories = new ArrayList<String>();
        categoriesToIgnore = new ArrayList<String>();
    }
}
