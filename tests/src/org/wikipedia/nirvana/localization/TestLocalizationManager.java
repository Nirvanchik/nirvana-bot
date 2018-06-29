/**
 *  @(#)TestLocalizationManager.java 03.02.2017
 *  Copyright © 2017 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.localization;

import java.util.Map;

/**
 * Initializes {@link Localizer} with customized translation strings.
 */
public class TestLocalizationManager {
    public static void init(Map<String, String> translations) {
        init(translations, null);
    }

    /**
     * Initializes {@link Localizer} with predefined translations and/or localized templates.
     *
     * @param translations map with regular translations
     * @param localizedTemplates map with templates localizations
     */
    public static void init(Map<String, String> translations,
            Map<String, LocalizedTemplate> localizedTemplates) {
        Localizer localizer = new Localizer();
        if (translations != null) {
            localizer.addTranslations(translations);
        }
        if (localizedTemplates != null) {
            localizer.addLocalizedTemplates(localizedTemplates);
        }
        localizer.setInitialized();
        Localizer.init(localizer);
    }

    public static void init(Localizer localizer) {
        Localizer.init(localizer);
        Localizer.getInstance().setInitialized();
    }

    public static void reset() {
        Localizer.resetFromTests();
    }
}
