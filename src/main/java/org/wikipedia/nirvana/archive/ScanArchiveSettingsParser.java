/**
 *  @(#)ScanArchiveSettingsParser.java
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

import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.nirvanabot.PortalConfig;

/**
 * User settings parser that can load data for {@link ScanArchiveSettings}.
 *
 */
public class ScanArchiveSettingsParser extends ArchiveSettingsParser {

    /**
     *  Default constructor.
     */
    public ScanArchiveSettingsParser(Localizer localizer) {
        super(localizer);
    }

    @Override
    public ScanArchiveSettings parse(PortalConfig config) {
        if (!config.hasKey(PortalConfig.KEY_ARCHIVE)) {
            return null;
        }
        ScanArchiveSettings archiveSettings =
                new ScanArchiveSettings(config.get(PortalConfig.KEY_ARCHIVE));
        parseAll(archiveSettings, config);
        
        return archiveSettings;
    }
}
