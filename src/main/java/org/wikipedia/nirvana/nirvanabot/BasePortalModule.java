/**
 *  @(#)BasePortalModule.java
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

package org.wikipedia.nirvana.nirvanabot;

import org.wikipedia.Wiki;
import org.wikipedia.nirvana.base.BotFatalError;
import org.wikipedia.nirvana.error.AfterUpdateFailure;
import org.wikipedia.nirvana.error.DangerousEditException;
import org.wikipedia.nirvana.error.InvalidLineFormatException;
import org.wikipedia.nirvana.error.ServiceError;
import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.nirvanabot.report.ReportItem;
import org.wikipedia.nirvana.util.SystemTime;
import org.wikipedia.nirvana.wiki.CatScanTools;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import javax.security.auth.login.LoginException;

/**
 * Abstract logic of updating procedure of any portal module.
 * This code is used to minimize duplications of similar code (DRY principle).
 *
 */
public abstract class BasePortalModule implements PortalModule {
    protected final Logger log;
    protected final PageFormatter pageFormatter;
    protected final SystemTime systemTime;
    protected final Localizer localizer;

    protected String language;
    protected List<String> categories;
    protected List<String> categoriesToIgnore;
    protected List<List<String>> categoryGroups;
    protected List<List<String>> categoryToIgnoreGroups;
    protected String pageName;

    protected String format;
    protected String formatString;
    protected int maxItems;
    protected CatScanTools.Service service;
    protected String delimeter;    
    protected int depth;
    protected int namespace;
    protected String namespaceIdentifier;
    protected boolean minor;
    protected boolean bot;
    protected boolean fastMode;
    
    private String summaryNew;
    private String summaryUpdate;
    
    protected boolean checkPlaceholdersBeforeUpdate = true;
    
    protected String currentUser = null;

    public class UpdateResults {
        String newText;
        int totalCount = 0;
        int oldCount = 0;
        // Some old items are removed when page is updated with new items,
        // such items considered outdated.
        // If we know how many of them then we can calculate how many new items were added.
        int outdateCount = 0;
        
        UpdateResults() {
        }

        int newPagesCount() {
            if (totalCount == 0) return 0;
            int count = totalCount - (oldCount - outdateCount);
            if (count < 0) return 0;
            return count;
        }

        boolean needUpdate(String oldText) {
            if (newText == null || newText.isEmpty()) {
                return false;
            }
            return !newText.equals(oldText) && !newText.equals(oldText.trim());
        }
        
        void logStat() {
            log.debug("updated items count: {}", totalCount);
            log.debug("new items count: {}", newPagesCount());
            log.debug("old items count: {}", oldCount);
            log.debug("outdated count: {}", outdateCount);
        }
        
        void outdateItem(String item) {
            outdateCount++; 
        }
    }
    
    /**
     * Default constructor.
     */
    public BasePortalModule(PortalParam param, PageFormatter pageFormatter, SystemTime systemTime) {
        log = LogManager.getLogger(this.getClass().getName());
        this.pageFormatter = pageFormatter;
        this.systemTime = systemTime;
        this.localizer = Localizer.getInstance();
        BotVariables.init();
        initLocalizedVars();

        this.language = param.lang;
        this.categories = param.categories;
        this.categoriesToIgnore = param.categoriesToIgnore;
        categoryGroups = param.categoryGroups;
        categoryToIgnoreGroups = param.categoryToIgnoreGroups;
        this.pageName = param.page;

        this.maxItems = param.maxItems;
        this.format = param.format;

        this.formatString = format
                .replace(BotVariables.TITLE, "%1$s")
                .replace(BotVariables.DATE, "%3$s");

        this.delimeter = param.delimeter;
        this.depth = param.depth;
        this.namespace = param.ns;
        this.minor = param.minor;
        this.bot = param.bot;
        this.service = param.service;
        this.fastMode = param.fastMode;

    }
    
    private void initLocalizedVars() {
        summaryNew = localizer.localize("новых");
        summaryUpdate = localizer.localize("обновление");
    }

    protected Calendar now() {
        return systemTime.now();
    }

    protected String getFormatString() {
        return formatString;
    }
    
    protected String getOldText(Wiki wiki) throws IOException {
        String text = wiki.getPageText(this.pageName);
        if (text == null) {
            text = "";
        }
        return text;
    }

    protected StringBuilder makeSummary(UpdateResults updateResults) {
        StringBuilder summaryBuilder = new StringBuilder("");
        if (updateResults.newPagesCount() > 0) {
            summaryBuilder.append("+");
        }            
        if (updateResults.newPagesCount() == 0) {
            summaryBuilder.append(summaryUpdate);
        } else {
            summaryBuilder.append(updateResults.newPagesCount()).append(summaryNew);
        }
        return summaryBuilder;
    }
    
    protected void checkPlaceholders(String formatString) throws InvalidLineFormatException {
        if (formatString.contains("%(")) {
            throw new InvalidLineFormatException(PortalConfig.KEY_FORMAT, formatString);
        }
    }
    
    protected String getCurrentUser(NirvanaWiki wiki) {
        if (this.currentUser == null) {
            Wiki.User user = wiki.getCurrentUser();
            this.currentUser = user.getUsername();
        }
        return this.currentUser;
    }
    
    protected String getCurrentUser() {
        return this.currentUser;
    }
    
    protected boolean checkAllowBots(NirvanaWiki wiki, String text) {
        
        log.debug("current user retrieved");
        if (!NirvanaWiki.allowBots(text, getCurrentUser(wiki))) {
            log.info("bots/nobots template forbids updating this portal section");
            return false;
        }
        return true;
    }
    
    protected void beforeGetData() throws IOException {
        // do nothing
    }

    protected void afterUpdate(NirvanaWiki wiki, UpdateResults updateResults,
            ReportItem reportData) throws InterruptedException, AfterUpdateFailure {
        reportData.newPagesUpdated(updateResults.newPagesCount());
        try {
            pageFormatter.notifyNewPagesUpdated();
        } catch (IOException e) {
            throw new AfterUpdateFailure(e);
        }
    }
    
    protected abstract UpdateResults getData(NirvanaWiki wiki, String text)
            throws IOException, InterruptedException, ServiceError, BotFatalError,
            InvalidLineFormatException, DangerousEditException;

    @Override
    public boolean update(NirvanaWiki wiki, ReportItem reportData, String comment)
            throws IOException, LoginException, InterruptedException, ServiceError, BotFatalError,
            InvalidLineFormatException, DangerousEditException, AfterUpdateFailure {
        log.debug("=> update()");

        if (checkPlaceholdersBeforeUpdate) {
            checkPlaceholders(formatString);
        }

        this.namespaceIdentifier = wiki.namespaceIdentifier(this.namespace);
        boolean updated = false;
        String text = getOldText(wiki);
        log.debug("old text retrieved");
        assert text != null;

        if (!checkAllowBots(wiki, text)) {
            return false;
        }
        
        beforeGetData();

        UpdateResults updateResults;
        try {
            updateResults = getData(wiki, text);
        } finally {
            reportData.reportCatscanStat(CatScanTools.getQuieriesStat());
        }

        if (updateResults.newText == null) {
            log.trace("d.newText = null");
            return false;
        }

        reportData.willUpdateNewPages();

        if (updateResults.needUpdate(text)) {
            StringBuilder summaryBuilder = makeSummary(updateResults);
            String summary = summaryBuilder.toString();
            try {
                log.info("Updating [[{}]] {}", this.pageName, summary);
                wiki.edit(pageName, updateResults.newText, summary, this.minor, this.bot);
                updated = true;
            } catch (Exception e) {
                reportData.newPagesUpdateError();
                throw e;
            }
            afterUpdate(wiki, updateResults, reportData);            
        }
        return updated;
    }
}
