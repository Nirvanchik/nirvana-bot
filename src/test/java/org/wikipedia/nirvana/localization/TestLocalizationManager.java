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

import java.util.HashMap;
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

    /**
     * Initialize {@link org.wikipedia.nirvana.localization.Localizer} with English localized
     * strings used in reporting code (org.wikipedia.nirvana.nirvanabot.report). 
     */
    public static void initWithReportingEnglishTranslations() {
        Map<String, String> translations = new HashMap<>();
        translations.put("Бот запущен", "Bot started");
        translations.put("Бот остановлен", "Bot stopped");
        translations.put("Да", "Yes");
        translations.put("Нет", "No");
        translations.put("Ошибка", "Error");
        translations.put("портал/проект", "portal/project");
        translations.put("проходов", "launches");
        translations.put("статус", "status");
        translations.put("время", "time");
        translations.put("новых статей", "new pages");
        translations.put("список обновлен", "list updated");
        translations.put("статей в архив", "pages in archive");
        translations.put("архив обновлен", "archive updated");
        translations.put("ошибок", "errors");
        translations.put("ошибка", "error");
        
        Map<String, LocalizedTemplate> localizedTemplates = new HashMap<>();
        localizedTemplates.put("Да", new LocalizedTemplate("Да", "Yes"));
        localizedTemplates.put("Нет", new LocalizedTemplate("Нет", "No"));
        init(translations, localizedTemplates);
    }
}
