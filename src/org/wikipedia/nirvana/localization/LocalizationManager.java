/**
 *  @(#)LocalizationManager.java 14.01.2017
 *  Copyright � 2017 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.FileTools;
import org.wikipedia.nirvana.NirvanaBasicBot;
import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.TextUtils;
import org.wikipedia.nirvana.WikiUtils;
import org.wikipedia.nirvana.nirvanabot.BotFatalError;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginException;

import junit.framework.Assert;

/**
 * Initializes {@link Localizer} from .INI file, from wiki page. Updates wiki
 * page with new terms detected during bot work.
 */
public class LocalizationManager {
    public static final String DEFAULT_LANG = "ru";
    public static final String DEFAULT_TRANSLATIONS_DIR = "translations";

    private static final String ERR_TRANSLATION_NOT_FOUND = "Translation file %s not found";

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
        if (lang.equals(DEFAULT_LANG) && !NirvanaBasicBot.DEBUG_BUILD) {
            Localizer.init(Localizer.DEFAULT_LOCALIZATION);
            return;
        }
        loadDefaultImpl();
        String wikiTranslationText = getWikiTranslationPage(wiki, wikiTranslationPage);
        if (wikiTranslationText != null) {
            Map<String, String> translations = new HashMap<>();
            wikiTranslationText = WikiUtils.removeComments(wikiTranslationText);
            wikiTranslationText = WikiUtils.removePreTags(wikiTranslationText);
            TextUtils.textOptionsToMap(wikiTranslationText, translations, true);
            Localizer.getInstance().addTranslations(translations);
            log.info(String.format("%d translations loaded from %s", translations.size(),
                    wikiTranslationPage));
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
        if (lang.equals(DEFAULT_LANG) && !NirvanaBasicBot.DEBUG_BUILD) {
            return;
        }
        String wikiTranslationText = getWikiTranslationPage(wiki, wikiTranslationPage);
        if (wikiTranslationText == null) {
            String editComment = Localizer.getInstance().localize("�������� �������� �����������");
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
                    "���������� ����� ������ ��� ��������");
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

    private String getWikiTranslationPage(NirvanaWiki wiki, String wikiTranslationPage)
            throws IOException {
        if (!wiki.exists(wikiTranslationPage)) {
            return null;
        }
        String path = cacheDir + "/" + FileTools.normalizeFileName(wikiTranslationPage);
        File file = new File(path);
        String text = null;
        boolean cacheIsDirty = false;
        if (file.exists()) {
            Revision r = wiki.getTopRevision(wikiTranslationPage);
            Calendar modified = Calendar.getInstance();
            modified.setTimeInMillis(file.lastModified());
            if (r.getTimestamp().after(modified)) {
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
        if (lang.equals(DEFAULT_LANG) && !NirvanaBasicBot.DEBUG_BUILD) {
            Localizer.init(Localizer.NO_LOCALIZATION);
            return;
        }
        loadDefaultImpl();
        Localizer.getInstance().setInitialized();
    }

    private void loadDefaultImpl() throws BotFatalError {
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
        try {
            text = FileTools.readFile(path);
        } catch (IOException e) {
            throw new BotFatalError(e);
        }
        Assert.assertNotNull(text);
        Map<String, String> translations = new HashMap<>();
        TextUtils.textOptionsToMap(text, translations, true);
        Localizer.getInstance().addTranslations(translations);
        log.info(String.format("%d translations loaded from %s", translations.size(), path));
    }
}
