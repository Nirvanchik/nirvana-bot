/**
 *  @(#)ArchiveSettingsParser.java
 *  Copyright © 2024 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.archive;

import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_ABOVE;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_BELOW;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_ENUMERATE_WITH_HASH;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_ENUMERATE_WITH_HASH2;

import org.wikipedia.nirvana.archive.ArchiveSettings.Enumeration;
import org.wikipedia.nirvana.archive.ArchiveSettings.Period;
import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.nirvanabot.PortalConfig;
import org.wikipedia.nirvana.util.OptionsUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser that can parse all settings related to archive update info {@link ArchiveSettings}. 
 *
 */
public class ArchiveSettingsParser {
    private static final String ERROR_PARAMETER_HAS_MULTIPLE_VALUES =
            "Для параметра \"%1$s\" дано несколько значений. Использовано значение по умолчанию.";

    private static final String ERROR_PARAMETER_MISSING_VARIABLE =
            "В параметре бота \"%1$s\" не задан ключ с переменным значением. " +
            "Значение этого параметра не принято.";

    private static final String ERROR_SAME_PERIOD =
            "Параметр \"%1$s\" и параметр \"%2$s\" имеют одинаковый период повторения: %3$s";

    protected Localizer localizer;

    /**
     * Default constructor.
     */
    public ArchiveSettingsParser(Localizer localizer) {
        this.localizer = localizer;
    }

    /**
     * Parse archive settings from {@link PortalConfig}.
     *
     * @param config portal config with settings parsed as simple map of strings
     * @return archive settings. All errors will be saved in "errors" member of settings.
     */
    public ArchiveSettings parse(PortalConfig config) {
        if (!config.hasKey(PortalConfig.KEY_ARCHIVE)) {
            return null;
        }
        ArchiveSettings archiveSettings = new ArchiveSettings(config.get(PortalConfig.KEY_ARCHIVE));
        parseAll(archiveSettings, config);
        return archiveSettings;
    }
    
    protected void parseAll(ArchiveSettings archiveSettings, PortalConfig config) {
        archiveSettings.errors.addAll(parseArchiveName(archiveSettings));

        if (config.hasKey(PortalConfig.KEY_ARCHIVE_HEADER_FORMAT) ||
                config.hasKey(PortalConfig.KEY_ARCHIVE_SUBHEADER_FORMAT)) {
            archiveSettings.errors.addAll(parseArchiveHeaders(archiveSettings, config));
        }

        if (config.hasKey(PortalConfig.KEY_ARCHIVE_PARAMS)) {
            archiveSettings.errors.addAll(parseArchiveSettings(archiveSettings,
                    config.get(PortalConfig.KEY_ARCHIVE_PARAMS)));
        }        
    }

    protected static ArrayList<String> parseArchiveName(ArchiveSettings archiveSettings) {
        ArrayList<String> errors = new ArrayList<String>();        
        if (archiveSettings.archive.contains(Period.YEAR.template())) {
            archiveSettings.archivePeriod = Period.YEAR;
        }
        if (archiveSettings.archive.contains(Period.SEASON.template())) {
            archiveSettings.archivePeriod = Period.SEASON;
        }
        if (archiveSettings.archive.contains(Period.QUARTER.template())) {
            archiveSettings.archivePeriod = Period.QUARTER;
        }
        if (archiveSettings.archive.contains(Period.MONTH.template())) {
            archiveSettings.archivePeriod = Period.MONTH;
        }            
        return errors;
    }
    
    protected List<String> parseArchiveSettings(ArchiveSettings archiveSettings,
            String settings) {
        List<String> items = OptionsUtils.optionToList(settings);
        return parseArchiveSettings(archiveSettings, items);
    }

    protected List<String> parseArchiveSettings(ArchiveSettings archiveSettings,
            List<String> items) {
        ArrayList<String> errors = new ArrayList<String>();
        if (items.contains(STR_ABOVE) && items.contains(STR_BELOW)) {
            String format = localizer.localize(ERROR_PARAMETER_HAS_MULTIPLE_VALUES);
            String param = String.format("%1$s (%2$s/%3$s)", PortalConfig.KEY_ARCHIVE_PARAMS,
                    STR_ABOVE, STR_BELOW);
            errors.add(String.format(format, param));
        } else if (items.contains(STR_ABOVE)) {
            archiveSettings.addToTop = true;
        } else if (items.contains(STR_BELOW)) {
            archiveSettings.addToTop = false;
        }
        int cnt = 0;
        if (items.contains(STR_ENUMERATE_WITH_HASH) ||
                items.contains(STR_ENUMERATE_WITH_HASH2)) {
            cnt++;
            archiveSettings.enumeration = Enumeration.HASH;
        }
        if (cnt > 1) {
            archiveSettings.enumeration = Enumeration.NONE;
            String format = localizer.localize(ERROR_PARAMETER_HAS_MULTIPLE_VALUES);
            String param = String.format("%1$s (%2$s)", PortalConfig.KEY_ARCHIVE_PARAMS,
                    localizer.localize("нумерация"));
            errors.add(String.format(format, param));
        }
        return errors;
    }
    
    private ArrayList<String> parseArchiveHeaders(ArchiveSettings archiveSettings,
            PortalConfig config) {
        ArrayList<String> errors = new ArrayList<String>();

        Period p1 = Period.NONE;
        final String key1 = PortalConfig.KEY_ARCHIVE_HEADER_FORMAT;
        if (config.hasKey(key1)) {
            String str = config.getUnquoted(key1);
            p1 = ArchiveSettings.getHeaderPeriod(str);
            if (p1 != Period.NONE) {
                archiveSettings.headerFormat = str;
            } else {
                String format = localizer.localize(ERROR_PARAMETER_MISSING_VARIABLE);
                errors.add(String.format(format, key1));
            }
        }

        Period p2 = Period.NONE;
        final String key2 = PortalConfig.KEY_ARCHIVE_SUBHEADER_FORMAT;
        if (config.hasKey(key2)) {
            String str = config.getUnquoted(key2);
            p2 = ArchiveSettings.getHeaderPeriod(str);
            if (p2 != Period.NONE) {
                archiveSettings.superHeaderFormat = archiveSettings.headerFormat;
                archiveSettings.headerFormat = str;
            } else {
                String format = localizer.localize(ERROR_PARAMETER_MISSING_VARIABLE);
                errors.add(String.format(format, key2));
            }
        }

        if (p1 != Period.NONE && p1 == p2) {
            String format = localizer.localize(ERROR_SAME_PERIOD);
            errors.add(String.format(format, key1, key2, p1.template()));
        }
        return errors;
    }

}
