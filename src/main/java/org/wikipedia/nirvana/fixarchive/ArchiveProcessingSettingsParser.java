/**
 *  @(#)ArchiveProcessingSettingsParser.java
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

package org.wikipedia.nirvana.fixarchive;

import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_REMOVE_DUPLICATES;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_SORT;
import static org.wikipedia.nirvana.nirvanabot.PortalConfig.STR_TOSORT;

import org.wikipedia.nirvana.archive.ArchiveSettings;
import org.wikipedia.nirvana.archive.ScanArchiveSettingsParser;
import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.nirvanabot.PortalConfig;

import java.util.List;

/**
 * User settings parser that can load data for {@link ArchiveProcessingSettings}.
 *
 */
public class ArchiveProcessingSettingsParser extends ScanArchiveSettingsParser {

    /**
     *  Default constructor.
     */
    public ArchiveProcessingSettingsParser(Localizer localizer) {
        super(localizer);
    }
    
    @Override
    public ArchiveProcessingSettings parse(PortalConfig config) {
        if (!config.hasKey(PortalConfig.KEY_ARCHIVE)) {
            return null;
        }
        ArchiveProcessingSettings archiveSettings =
                new ArchiveProcessingSettings(config.get(PortalConfig.KEY_ARCHIVE));
        parseAll(archiveSettings, config);
        
        return archiveSettings;
    }
    
    @Override
    protected List<String> parseArchiveSettings(ArchiveSettings archiveSettings,
            List<String> items) {
        List<String> errors = super.parseArchiveSettings(archiveSettings, items);
        assert archiveSettings instanceof ArchiveProcessingSettings;
        ArchiveProcessingSettings archiveProcessingSettings =
                (ArchiveProcessingSettings) archiveSettings;
        if (items.contains(STR_TOSORT) || items.contains(STR_SORT)) {
            archiveProcessingSettings.sorted = true;
        }
        if (items.contains(STR_REMOVE_DUPLICATES)) {
            archiveProcessingSettings.removeDuplicates = true;
        }
        return errors;
    }

}
