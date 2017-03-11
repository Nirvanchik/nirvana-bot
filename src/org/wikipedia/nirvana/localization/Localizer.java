/**
 *  @(#)Localizer.java 14.01.2017
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
 * Recommended code page for this file is CP1251 (also called Windows-1251).
 * */

package org.wikipedia.nirvana.localization;

import org.wikipedia.nirvana.annotation.VisibleForTesting;

import org.junit.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Localization module for bot.
 *
 * <p>For provided word returns a version translated to current language (or the provided word if
 * translation not found).
 * Must be initialized with any of the provided initializing methods. 
 */
public class Localizer {
    /**
     * Use this with {@link #init(Localizer)} method if you want to disable localization.
     */
    public static final Localizer NO_LOCALIZATION = new Localizer() {
        @Override
        void addTranslations(Map<String, String> translations) {
            
        }

        Map<String, String> getTranslations() {
            return Collections.emptyMap();
        }

        @Override
        public String localize(String word) {
            return word;
        }

        @Override
        public String localizeStrict(String word) {
            return null;
        }

        @Override
        public LocalizedTemplate localizeTemplate(String template) {
            return new LocalizedTemplate.Default(template);
        }

        @Override
        public LocalizedTemplate localizeTemplateStrict(String template) {
            return null;
        }
    };

    /**
     * Use this with {@link #init(Localizer)} method if you set default localization.
     * Default localization doesn't need to be localized.
     */
    public static final Localizer DEFAULT_LOCALIZATION = new Localizer() {
        @Override
        void addTranslations(Map<String, String> translations) {
        }

        Map<String, String> getTranslations() {
            return Collections.emptyMap();
        }

        @Override
        public String localize(String word) {
            return word;
        }

        @Override
        public String localizeStrict(String word) {
            return word;
        }

        @Override
        public LocalizedTemplate localizeTemplate(String template) {
            return new LocalizedTemplate.Default(template);
        }

        @Override
        public LocalizedTemplate localizeTemplateStrict(String template) {
            return new LocalizedTemplate.Default(template);
        }
    };

    private static Localizer sInstance = null;

    private boolean initialized = false;
    private Map<String, String> translations = new HashMap<>();
    private Map<String, LocalizedTemplate> localizedTemplates = new HashMap<>();

    /**
     * Singleton getter.
     *
     * @return {@link Localizer} instance.
     */
    public static Localizer getInstance() {
        if (sInstance == null) {
            sInstance = new Localizer();
        }
        return sInstance;
    }

    @VisibleForTesting
    static void resetFromTests() {
        sInstance = null;
    }

    /**
     * Initialize with a special {@link Localizer} instance.
     */
    public static void init(Localizer instance) {
        Assert.assertNull(sInstance);
        Assert.assertNotNull(instance);
        sInstance = instance;
    }

    void setInitialized() {
        initialized = true;
    }

    void addTranslations(Map<String, String> translations) {
        this.translations.putAll(translations);
    }

    void addLocalizedTemplates(Map<String, LocalizedTemplate> localizedTemplates) {
        this.localizedTemplates.putAll(localizedTemplates);
    }

    Map<String, String> getTranslations() {
        return Collections.unmodifiableMap(translations);
    }

    Map<String, LocalizedTemplate> getLocalizedTemplates() {
        return Collections.unmodifiableMap(localizedTemplates);
    }

    /**
     * Translates a word to current language. 
     *
     * @param word word to translate
     * @return translated version for this word or original word if no translation found
     */
    public String localize(String word) {        
        return localizeImpl(word, word);
    }

    /**
     * Translates a template (with params) to current language. 
     *
     * @param template template name to translate
     * @return translated version for this template wrapped into {@link LocalizedTemplate}.
     */
    public LocalizedTemplate localizeTemplate(String template) {        
        return localizeTemplateImpl(template, new LocalizedTemplate.Default(template));
    }

    /**
     * Translates a word to current language. 
     *
     * @param word word to translate
     * @return translated version for this word or null if no translation found
     */
    public String localizeStrict(String word) {
        return localizeImpl(word, null);
    }

    /**
     * Translates a template (with params) to current language. 
     *
     * @param template template name to translate
     * @return translated version for this template wrapped into {@link LocalizedTemplate}.
     */
    public LocalizedTemplate localizeTemplateStrict(String template) {
        return localizeTemplateImpl(template, null);
    }

    private String localizeImpl(String word, String defaultWord) {
        Assert.assertTrue("Localizer is used when it's not initialized.", initialized);
        if (translations.containsKey(word)) {
            String localizedWord = translations.get(word);
            if (localizedWord != null) return localizedWord;
        } else {
            translations.put(word, null);
        }
        return defaultWord;
    }

    private LocalizedTemplate localizeTemplateImpl(String template,
            LocalizedTemplate defaultTemplate) {
        Assert.assertTrue("Localizer is used when it's not initialized.", initialized);
        if (localizedTemplates.containsKey(template)) {
            LocalizedTemplate localizedTemplate = localizedTemplates.get(template);
            if (localizedTemplate != null) return localizedTemplate;
        } else {
            localizedTemplates.put(template, null);
        }
        return defaultTemplate;
    }
}
