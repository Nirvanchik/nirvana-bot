/**
 *  @(#)LocalizationManager.java 14.01.2017
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

import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.annotation.VisibleForTesting;
import org.wikipedia.nirvana.base.BasicBot;
import org.wikipedia.nirvana.base.BotFatalError;
import org.wikipedia.nirvana.util.FileTools;
import org.wikipedia.nirvana.util.TextUtils;
import org.wikipedia.nirvana.wiki.NirvanaWiki;
import org.wikipedia.nirvana.wiki.WikiUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

/**
 * Initializes {@link Localizer} from .INI file, from wiki page. Updates wiki
 * page with new terms detected during bot work.
 */
public class LocalizationManager {
    public static final String DEFAULT_LANG = "ru";
    public static final String DEFAULT_TRANSLATIONS_DIR = "translations";

    private static final String ERR_TRANSLATION_NOT_FOUND = "Translation file %s not found";
    
    private static final String ERR_INVALID_TRANSLATION =
            "Invalid template expression: %s. Translation ignored.";

    private final Logger log;
    @SuppressWarnings("unused")
    private final String outDir;
    private final String cacheDir;
    private final String translationsDir;
    private final String lang;
    private final String customTranslation;

    /**
     * Constructs LocalizationManager for specified language.
     */
    public LocalizationManager(String outDir, String cacheDir, String translationsDir,
            String lang) {
        this(outDir, cacheDir, translationsDir, lang, null);
    }

    /**
     * Constructs LocalizationManager for specified language with custom translations ini file.
     * Translation for this language in {@link #translationsDir} will be ignored.
     * <code>customTranslation</code> may be null.
     */
    public LocalizationManager(String outDir, String cacheDir, String translationsDir, String lang,
            String customTranslation) {
        this.outDir = outDir;
        this.cacheDir = cacheDir;
        this.translationsDir = translationsDir;
        this.lang = lang;
        this.customTranslation = customTranslation;
        log = LogManager.getLogger(this.getClass());
    }

    /**
     * Loads translations from default location and from wiki. Translations from wiki will
     * override default translations.
     */
    public void load(NirvanaWiki wiki, String wikiTranslationPage) throws IOException,
            BotFatalError {
        if (lang.equals(DEFAULT_LANG) && !BasicBot.DEBUG_BUILD) {
            Localizer.init(Localizer.DEFAULT_LOCALIZATION);
            return;
        }
        long lastModified = loadDefaultImpl();
        // WikiUtils.removeCategories() below calls localizer
        // so it must be initialized.
        Localizer.getInstance().setInitialized();
        String wikiTranslationText = getWikiTranslationPage(wiki, wikiTranslationPage,
                lastModified);
        if (wikiTranslationText != null) {
            wikiTranslationText = WikiUtils.removeComments(wikiTranslationText);
            wikiTranslationText = WikiUtils.removePreTags(wikiTranslationText);
            wikiTranslationText = WikiUtils.removeCategories(wikiTranslationText);
            int cnt = parseTranslationsToLocalizer(wikiTranslationText, Localizer.getInstance());
            log.info(String.format("%d translations loaded from %s", cnt, wikiTranslationPage));
        }
        Localizer.getInstance().setInitialized();
    }

    /**
     * Updates wiki translations page with new words detected during bot work.
     * Should be called at the end of bot run.
     */
    public void refreshWikiTranslations(NirvanaWiki wiki, String wikiTranslationPage)
            throws IOException, LoginException {
        log.debug("Refresh wiki translations page?");
        if (lang.equals(DEFAULT_LANG) && !BasicBot.DEBUG_BUILD) {
            return;
        }
        // 1492210800 = Fri, 14 Apr 2017 23:00:00 GMT
        String wikiTranslationText = getWikiTranslationPage(wiki, wikiTranslationPage, 1492210800);
        if (wikiTranslationText == null) {
            String editComment = Localizer.getInstance().localize("Создание страницы локализации");
            Map<String, String> translations = Localizer.getInstance().getTranslations();
            if (translations.size() == 0) {
                log.debug("No translations found. Skipping...");
                return;
            }
            String text = translationsToWiki(translations);
            text = WikiUtils.addPreTags(text);
            log.info("Create new translations page: " + wikiTranslationPage);
            wiki.edit(wikiTranslationPage, text, editComment);
            FileTools.dump(text, cacheDir, wikiTranslationPage);
        } else {
            String editComment = Localizer.getInstance().localize(
                    "Добавление новых ключей для перевода");
            Map<String, String> oldTranslations = new HashMap<>();
            String text = WikiUtils.removeComments(wikiTranslationText);
            text = WikiUtils.removePreTags(text);
            TextUtils.textOptionsToMap(text, oldTranslations, true);
            Map<String, String> newTranslations = getNewTranslations(Localizer.getInstance()
                    .getTranslations(), oldTranslations);
            log.info(String.format("Found %d new translation keys.", newTranslations.size()));
            if (newTranslations.size() == 0) {
                log.debug("No new translations found. Skipping...");
                return;
            }
            String newTermsText = translationsToWiki(newTranslations);
            text = wikiTranslationText;
            if (text.contains("<pre>") && text.contains("</pre>")) {
                int index = text.lastIndexOf("</pre>");
                text = text.substring(0, index) + newTermsText + text.substring(index);
            } else {
                log.error("Localization page has no <pre> tags!");
                text = text + newTermsText;
            }
            log.info("Updating translations page: " + wikiTranslationPage);
            wiki.edit(wikiTranslationPage, text, editComment);
            FileTools.dump(text, cacheDir, wikiTranslationPage);
            // TODO : Make the same system for templates translations. 
        }
    }

    private Map<String, String> getNewTranslations(Map<String, String> allTranslations,
            Map<String, String> oldTranslations) {
        Map<String, String> newTranslations = new HashMap<>(allTranslations);
        for (String key : oldTranslations.keySet()) {
            newTranslations.remove(key);
        }
        return newTranslations;
    }

    private String translationsToWiki(Map<String, String> translations) {
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, String> pair : translations.entrySet()) {
            buf.append(pair.getKey()).append(" = ");
            if (pair.getValue() != null) {
                buf.append(pair.getValue());
            }
            buf.append("\n");
        }
        return buf.toString();
    }

    private String getWikiTranslationPage(NirvanaWiki wiki, String wikiTranslationPage,
            long minCacheTimestamp) throws IOException {
        if (!wiki.exists(wikiTranslationPage)) {
            return null;
        }
        String path = cacheDir + "/" + FileTools.normalizeFileName(wikiTranslationPage);
        File file = new File(path);
        String text = null;
        boolean cacheIsDirty = false;
        if (file.exists()) {
            Revision r = wiki.getTopRevision(wikiTranslationPage);
            OffsetDateTime modified = OffsetDateTime.ofInstant(
                    Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault());
            if (r.getTimestamp().isAfter(modified)) {
                cacheIsDirty = true;
            }
            if (file.lastModified() < minCacheTimestamp) {
                cacheIsDirty = true;
            }
        } else {
            cacheIsDirty = true;
        }
        if (cacheIsDirty) {
            text = wiki.getPageText(wikiTranslationPage);
            FileTools.dump(text, cacheDir, wikiTranslationPage);
        } else {
            text = FileTools.readWikiFile(cacheDir, wikiTranslationPage);
        }
        return text;
    }

    /**
     * Loads default translations.
     */
    public void loadDefault() throws BotFatalError {
        if (lang.equals(DEFAULT_LANG) && !BasicBot.DEBUG_BUILD) {
            Localizer.init(Localizer.DEFAULT_LOCALIZATION);
            return;
        }
        loadDefaultImpl();
        Localizer.getInstance().setInitialized();
    }

    private long loadDefaultImpl() throws BotFatalError {
        Localizer localizer = new Localizer();
        String path;
        if (customTranslation != null && !customTranslation.isEmpty()) {
            path = customTranslation;
        } else {
            path = translationsDir + "/" + lang + ".ini";
        }
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            throw new BotFatalError(String.format(ERR_TRANSLATION_NOT_FOUND, path));
        }
        String text;
        long lastModified = file.lastModified();
        try {
            text = FileTools.readFile(path);
        } catch (IOException e) {
            throw new BotFatalError(e);
        }

        assert text != null;

        int cnt = parseTranslationsToLocalizer(text, localizer);
        Localizer.init(localizer);
        log.info(String.format("%d translations loaded from %s", cnt, path));
        return lastModified;
    }

    @VisibleForTesting
    int parseTranslationsToLocalizer(String text, Localizer localizer) {
        Map<String, String> translations = new HashMap<>();
        TextUtils.textOptionsToMap(text, translations, true);
        Map<String, LocalizedTemplate> templateTranslations =
                findRemoveTemplateTranslations(translations);
        localizer.addTranslations(translations);
        localizer.addLocalizedTemplates(templateTranslations);
        return translations.size() + templateTranslations.size();
    }

    private Map<String, LocalizedTemplate> findRemoveTemplateTranslations(
            Map<String, String> translations) {
        Map<String, String> templateTranslationsRaw = new HashMap<>();
        Iterator<Map.Entry<String, String>> it = translations.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> translation = it.next();
            String key = translation.getKey();
            if (key.contains("{{")) {
                templateTranslationsRaw.put(key, translations.get(key));
                it.remove();
            }
        }
        Map<String, LocalizedTemplate> templateTranslations = new HashMap<>();
        Pattern templatePattern = Pattern.compile("\\s*\\{\\{(?<params>[^\\{\\}]+)\\}\\}\\s*");
        for (Map.Entry<String, String> pair: templateTranslationsRaw.entrySet()) {
            if (pair.getValue() == null || pair.getValue().trim().isEmpty()) {
                log.info("Translation not found for %s. Localization skipped.", pair.getKey());
                continue;
            }
            Matcher matcherLeft = templatePattern.matcher(pair.getKey());
            Matcher matcherRight = templatePattern.matcher(pair.getValue());
            if (!matcherLeft.matches()) {
                log.error(ERR_INVALID_TRANSLATION, pair.getKey());
                continue;
            }
            if (!matcherRight.matches()) {
                log.error(ERR_INVALID_TRANSLATION, pair.getValue());
                continue;
            }
            String paramsLeftStr = matcherLeft.group("params");
            String paramsRightStr = matcherRight.group("params");
            String [] paramsLeft = paramsLeftStr.split("\\|");
            String [] paramsRight = paramsRightStr.split("\\|");
            String name = paramsLeft[0].trim();
            String localizedName = paramsRight[0].trim();
            if (name.isEmpty()) {
                log.error(ERR_INVALID_TRANSLATION, pair.getKey());
                continue;
            }
            if (localizedName.isEmpty()) {
                log.error(ERR_INVALID_TRANSLATION, pair.getValue());
                continue;
            }
            if (paramsLeft.length != paramsRight.length) {
                log.error("Incomplete template translation: %s. \nLeft string contains %d parts " +
                        "but right string contains %d parts. Translation ignored.",
                        pair.getValue(), paramsLeft.length, paramsRight.length);
                continue;
            }
            LocalizedTemplate localizedTemplate = new LocalizedTemplate(name, localizedName);
            for (int i = 1; i < paramsLeft.length; i++) {
                String param = paramsLeft[i].trim();
                if (param.isEmpty()) continue;
                String right = paramsRight[i].trim();
                localizedTemplate.putParam(param, right);
            }
            templateTranslations.put(name, localizedTemplate);
        }
        return templateTranslations;
    }

}
