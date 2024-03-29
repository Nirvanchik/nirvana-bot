/**
 *  @(#)MockNirvanaWiki.java 03.04.2016
 *  Copyright © 2016 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.wiki;

import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

/**
 * Mocks a lot of methods of {@link NirvanaWiki} and {@link org.wikipedia.Wiki}
 * Prevents access to Wiki with Mediawiki REST API with methods like <code>edit()</code>.
 */
public class MockNirvanaWiki extends NirvanaWiki {
    private static final String ASSERT_MSG = "You forgot to mock something for Wiki.java?";
    private Map<String,String []> whatTranscludesMap = new HashMap<>();
    private Map<String, String> pageTextMap = new HashMap<>();
    private Map<Long, String> namespaceIdMap = new HashMap<>();
    private Map<String, Integer> namespaceMap = new HashMap<>();
    private Map<String, Revision> topRevMap = new HashMap<>();
    private Map<String, Revision> firstRevMap = new HashMap<>();
    private Map<String, String []> pageTemplatesMap = new HashMap<>();
    private Map<String, Boolean> pageExistsMap = new HashMap<>();
    private Map<String, String []> whatLinksHereMap = new HashMap<>();
    private Map<String, Map<String, Object>> userInfoMap = new HashMap<>();
    private Map<String, MultiKeyMap> pageHistoryMap = new HashMap<>();
    private Map<Long, Revision> revMap = new HashMap<>();
    private Map<Long, String> revTextMap = new HashMap<>();
    private Map<String, Map> pageInfoMap = new HashMap<>();
    private Map<String, String> redirectMap = new HashMap<>();

    private LinkedList<String> fetchQueue = new LinkedList<>();

    private String token;

    private User user;

    private List<EditInfo> edits = new ArrayList<>();
    private boolean allowEdits = false;
    private boolean allowLogin = false;

    public static class EditInfo extends EditInfoMinimal {
        String summary;
        boolean minor;
        boolean bot;
        OffsetDateTime basetime;

        /**
         * Constructs EditInfo object with all fields specified.
         */
        public EditInfo(String title, String text, String summary, boolean minor, boolean bot,
                int section, OffsetDateTime basetime) {
            super(title, text, section);
            this.summary = summary;
            this.minor = minor;
            this.bot = bot;
            this.basetime = basetime;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj instanceof EditInfo) {
                EditInfo info = (EditInfo) obj;
                return info.title.equals(this.title) &&
                        info.text.equals(this.text) &&
                        info.summary.equals(this.summary) &&
                        info.minor == this.minor &&
                        info.bot == this.bot &&
                        info.section == this.section;
            }
            if (obj instanceof EditInfoMinimal) {
                return super.equals(obj);
            }
            return false;
        }
    }

    public static class EditInfoMinimal {
        String title;
        String text;
        int section;

        /**
         * Constructs EditInfoMinimal with all fields specified.
         */
        public EditInfoMinimal(String title, String text, int section) {
            this.title = title;
            this.text = text;
            this.section = section;
        }

        /**
         * Copy constructor for converting from {@link EditInfo}.
         */
        public EditInfoMinimal(EditInfo editInfo) {
            this.title = editInfo.title;
            this.text = editInfo.text;
            this.section = editInfo.section;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj instanceof EditInfoMinimal) {
                EditInfoMinimal info = (EditInfoMinimal) obj;
                return info.title.equals(this.title) &&
                        info.text.equals(this.text) &&
                        info.section == this.section;
            }
            return false;
        }

        @Override
        public String toString() {
            StringBuilder temp = new StringBuilder("Edit Info[title=");
            temp.append(title);
            temp.append(", section=");
            temp.append(section);
            temp.append(", text=");
            temp.append(text);
            temp.append("]");
            return temp.toString();
        }
        
    }

    private static final long serialVersionUID = 1L;

    public MockNirvanaWiki(String domain) {
        super(domain);
    }

    public MockNirvanaWiki(String domain, String scriptPath) {
        super(domain, scriptPath);
    }

    public class MockUser extends User {
        private static final long serialVersionUID = 1L;

        public MockUser(String username) {
            super(username);
        }
    }

    public MockNirvanaWiki(String domain, String scriptPath, String protocol) {
        super(domain, scriptPath, protocol);
    }

    public MockNirvanaWiki(String domain, String scriptPath, String protocol, String language) {
        super(domain, scriptPath, protocol, language);
    }

    /**
     * Sometimes we want to allow edits just becauze we use {@link NirvanaWiki}
     * class with debug mode enabled when <code>edit()</code> is already mocked and do not really
     * edit anything but just writes edit text to some file for example.
     * @param allow if <code>true</code> we will call <code>super.edit()</code> in our
     *              <code>edit()</code> method.
     */
    public void allowEdits(boolean allow) {
        this.allowEdits = allow;
    }

    /**
     * If you set to true the real login() method will be called.
     * Should be used with mocking post() requests.
     * @see {@link #mockPostSequential}
     */
    public void allowLogin(boolean allow) {
        this.allowLogin = allow;
    }

    @Override
    public synchronized void login(String username, char[] password)
            throws IOException, FailedLoginException {
        // We don't login in tests usually.
        user = new MockUser(username);
        if (allowLogin) {
            super.login(username, password);
        }
    }

    @Override
    public User getCurrentUser() {
        return user;
    }

    @Override
    protected String fetch(String url, Map<String, String> postparams, String caller)
            throws IOException {
        log.debug("[MOCK] fetch url: {}", url);
        Assert.assertFalse("Unexpected request: " + url, fetchQueue.isEmpty());
        return fetchQueue.removeFirst();
    }

    public void mockFetchSequential(String response) {
        fetchQueue.addLast(response);
    }
    
    @Override
    public String[] whatTranscludesHere(String title, int... ns) throws IOException {
        log.debug("[MOCK] whatTranscludesHere: {}", title);
        Assert.assertTrue(whatTranscludesMap.containsKey(title));
        return whatTranscludesMap.get(title);
    }

    public void mockWhatTranscludesHere(String title, String []whatTranscludes) {
        whatTranscludesMap.put(title, whatTranscludes);
    }

    @Override
    public String getPageText(String title) throws IOException {
        log.debug("[MOCK] getPageText: {}", title);
        if (!pageTextMap.containsKey(title)) {
            throw new IllegalStateException("Unexpected getPageText() call with title: " + title);
        }
        String text = pageTextMap.get(title);
        if (text == null) {
            return null;
        }
        // TODO(KIN): Do we really need this?
        // TODO: Remove it. Use null for that.
        if (text.equals("<java.io.FileNotFoundException>")) {
            throw new FileNotFoundException(title);
        }
        return text;
    }

    @Override
    public String[] getPageText(String[] titles) throws IOException {
        log.debug("[MOCK] getPageText: {}", String.join(", ", titles));
        String[] result = new String [titles.length];
        for (int i = 0; i < titles.length; i++) {
            String title = titles[i];
            String text = null;
            if (pageTextMap.containsKey(title)) {
                text = pageTextMap.get(title);
            }
            result[i] = text;
        }
        return result;
    }

    public void mockPageText(String title, String text) {
        pageTextMap.put(title, text);
    }

    @Override
    public String namespaceIdentifier(int namespace) {
        log.debug("[MOCK] namespaceIdentifier: {}", namespace);
        Assert.assertTrue(namespaceIdMap.containsKey(new Long(namespace)));
        return namespaceIdMap.get(new Long(namespace));
    }

    public void mockNamespaceIdentifier(int namespaceNumber, String namespaceName) {
        this.mockNamespaceIdentifier((long)namespaceNumber, namespaceName);
    }

    public void mockNamespaceIdentifier(Long namespaceNumber, String namespaceName) {
        namespaceIdMap.put(namespaceNumber, namespaceName);
    }

    @Override
    public int namespace(String title) {
        log.debug("[MOCK] namespace: {}", title);
        Assert.assertTrue(ASSERT_MSG, namespaceMap.containsKey(title));
        // TODO: mock populateNamespaceCache() instead.
        return namespaceMap.get(title);
    }

    public void mockNamespace(String title, Integer namespaceNumber) {
        namespaceMap.put(title, namespaceNumber);
    }

    public void mockTopRevision(String title, Revision revision) {
        topRevMap.put(title, revision);
    }

    @Override
    public Revision getTopRevision(String title) throws IOException {
        log.debug("[MOCK] getTopRevision: {}", title);
        Assert.assertTrue(ASSERT_MSG, topRevMap.containsKey(title));
        return topRevMap.get(title);
    }

    public void mockFirstRevision(String title, Revision revision) {
        firstRevMap.put(title, revision);
    }

    @Override
    public Revision getFirstRevision(String title, boolean resolveRedirect) throws IOException {
        log.debug("[MOCK] getFirstRevision: '{}' resolve redirects: {}", title, resolveRedirect);
        if (!firstRevMap.containsKey(title)) {
            log.warn("[MOCK] getFirstRevision: '{}' -> NO DATA for this title! Return null", title);
            return null;
        }
        return firstRevMap.get(title);
    }

    @Override
    public Revision getFirstRevision(String title) throws IOException {
        log.debug("[MOCK] getFirstRevision: {}", title);
        if (!firstRevMap.containsKey(title)) {
            log.warn("[MOCK] getFirstRevision: '{}' -> NO DATA for this title! Return null", title);
            return null;
        }
        return firstRevMap.get(title);
    }

    @Override
    public synchronized void edit(String title, String text, String summary, boolean minor,
            boolean bot, int section, OffsetDateTime basetime) throws IOException, LoginException {
        log.debug("[MOCK] edit: {}", title);
        EditInfo edit = new EditInfo(title, text, summary, minor, bot, section, basetime);
        edits.add(edit);
        if (allowEdits) {
            super.edit(title, text, summary, minor, bot, section, basetime);
        }
    }

    public List<EditInfo> getEdits() {
        return edits;
    }
    
    public void resetEdits() {
        edits.clear();
    }

    /**
     * Will validate that there was made 1 edit and edit page title and text
     * equal to what is expected. 
     */
    public void validateEdit(String page, String text) {
        List<EditInfoMinimal> edits = new ArrayList<>();
        EditInfoMinimal edit = new EditInfoMinimal(page, text, -2);
        edits.add(edit);
        Assert.assertEquals(edits, getEdits());
    }

    /**
     * Will validate that there were made no edits.
     */
    public void validateNoEdits() {
        Assert.assertTrue(getEdits().isEmpty());
    }

    @Override
    public String[] getTemplates(String title, int... ns) throws IOException {
        log.debug("[MOCK] getTemplates: {}", title);
        if (!pageTemplatesMap.containsKey(title)) return new String[0];
        return pageTemplatesMap.get(title);
    }

    public void mockPageTemplates(String title, String[] templates) {
        pageTemplatesMap.put(title, templates);
    }

    public void mockPageTemplates(String title, List<String> templates) {
        pageTemplatesMap.put(title, templates.toArray(new String[templates.size()]));
    }

    @Override
    public String[][] getPagesTemplates(String[] titles, int... ns) throws IOException {
        log.debug("[MOCK] getPagesTemplates: {}", String.join(", ", titles));
        String[][] result = new String [titles.length][];
        for (int i = 0; i < titles.length; i++) {
            String title = titles[i];
            if (!pageTemplatesMap.containsKey(title)) result[i] = new String[0];
            else result[i] = pageTemplatesMap.get(title); 
        }
        return result;
    }

    @Override
    public boolean[][] hasTemplates(String[] titles, String[] templates) throws IOException {
        boolean[][] results = new boolean[titles.length][];
        for (int i = 0; i < titles.length; i++) {
            String title = titles[i];
            boolean[] thisResult = new boolean[templates.length];
            for (int j = 0; j < templates.length; j++) {
                if (!pageTemplatesMap.containsKey(title)) thisResult[j] = false;
                else thisResult[j] =
                        ArrayUtils.contains(pageTemplatesMap.get(titles[i]), templates[j]);
            }
            results[i] = thisResult;
        }
        return results;
    }

    @Override
    public boolean exists(String title) throws IOException {
        log.debug("[MOCK] exists: {}", title);
        if (pageExistsMap.containsKey(title)) {
            return pageExistsMap.get(title);
        }
        return false;
    }

    @Override
    public boolean[] exists(String[] titles) throws IOException {
        log.debug("[MOCK] exists: {}", StringUtils.join(titles, ", "));
        boolean [] ret = new boolean[titles.length];
        for (int i = 0; i < titles.length; i++) {
            ret[i] = pageExistsMap.getOrDefault(titles[i], false);
        }
        return ret;
    }

    public void mockExists(String title, boolean exists) {
        pageExistsMap.put(title, exists);
    }

    @Override
    public String[] whatLinksHere(String title, int... ns) throws IOException {
        log.debug("[MOCK] whatLinksHere: {}", title);
        if (!whatLinksHereMap.containsKey(title)) return new String [0];
        return whatLinksHereMap.get(title);
    }

    public void mockWhatLinksHere(String title, String [] links) {
        whatLinksHereMap.put(title, links);
    }

    public void mockWhatLinksHere(String title, List<String> links) {
        whatLinksHereMap.put(title, links.toArray(new String[links.size()]));
    }

    public void mockUserInfo(String username, Map<String, Object> info) {
        userInfoMap.put(username, info);
    }

    /**
     *  Gets information about the given users. Overridden method.
     */
    @Override
    public Map<String, Object>[] getUserInfo(String... usernames) throws IOException {
        log.debug("[MOCK] getUserInfo: {}", String.join(", ", usernames));
        Map[] info = new HashMap[usernames.length];
        for (int i = 0; i < usernames.length; i++) {
            Assert.assertTrue("User info not found for " + usernames[i],
                    userInfoMap.containsKey(usernames[i]));
            info[i] = userInfoMap.get(usernames[i]);
        }
        return info;
    }

    public void mockPageHistory(String title, long startMs, long endMs, Revision [] revs) {
        pageHistoryMap.computeIfAbsent(title, k -> new MultiKeyMap()).put(
                new MultiKey(new Long(startMs), new Long(endMs)), revs);
    }

    @Override
    public Revision[] getPageHistory(String title, OffsetDateTime start, OffsetDateTime end,
            boolean reverse) throws IOException {
        log.debug("[MOCK] getPageHistory: {}", title);
        Assert.assertTrue(ASSERT_MSG, pageHistoryMap.containsKey(title));
        MultiKeyMap history = pageHistoryMap.get(title);
        MultiKey key = new MultiKey(
                new Long(start.toInstant().toEpochMilli()), end.toInstant().toEpochMilli());
        if (history.containsKey(key)) {
            return (Revision[]) history.get(key);
        }
        // TODO: Remove it. We can mock now() currently
        // Workaround for now() that cannot be mocked easily.
        key = new MultiKey(
                new Long(start.toInstant().toEpochMilli()), new Long(0));
        if (history.containsKey(key)) {
            return (Revision[]) history.get(key);
        }
        // Another workaround for now() that cannot be mocked easily.
        key = new MultiKey(
                new Long(0), end.toInstant().toEpochMilli());
        if (history.containsKey(key)) {
            return (Revision[]) history.get(key);
        }
        return new Revision[] {};
    }

    public void mockRevision(long revId, Revision revision) {
        revMap.put(revId, revision);
    }

    @Override
    public Revision getRevision(long revId) throws IOException {
        log.debug("[MOCK] getRevision: {}", revId);
        Assert.assertTrue(ASSERT_MSG, revMap.containsKey(revId));
        return revMap.get(revId);
    }

    public void mockRevisionText(long revId, String text) {
        revTextMap.put(revId, text);
    }

    @SuppressWarnings("rawtypes")
    public void mockPageInfo(String page, Map pageInfo) {
        pageInfoMap.put(page, pageInfo);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map[] getPageInfo(String[] pages) throws IOException {
        log.debug("[MOCK] getPageInfo: {}", (Object) pages);
        Map [] pageInfo = new Map[pages.length];
        for (int i = 0; i < pages.length; i++) {
            Assert.assertTrue(ASSERT_MSG, pageInfoMap.containsKey(pages[i]));
            pageInfo[i] = pageInfoMap.get(pages[i]);
        }
        return pageInfo;
    }

    /**
     * Replacement for mocked {@link Wiki.Revision#getText()}.
     */
    public String getRevisionText(long revId) {
        log.debug("[MOCK] getRevisionText: {}", revId);
        Assert.assertTrue(ASSERT_MSG, revTextMap.containsKey(revId));
        return revTextMap.get(revId);
    }

    public void mockToken(String token) {
        this.token = token;
    }

    @Override
    public String getToken(String type) throws IOException {
        return token;
    }
    
    public void mockResolveRedirect(String title, String resolvedTitle) {
        redirectMap.put(title, resolvedTitle);
    }
    
    @Override
    public String resolveRedirect(String title) throws IOException {
        log.debug("[MOCK] resolveRedirect: {}", title);
        Assert.assertTrue(ASSERT_MSG, redirectMap.containsKey(title));
        return redirectMap.get(title);
    }

    public void debug() {
        // Empty. Used in tests only.
    }

    public class TestRevision extends Revision {

        public TestRevision(long revid, OffsetDateTime timestamp, String title, String summary,
                String user, boolean minor, boolean bot, boolean rvnew, int size) {
            super(revid, timestamp, title, summary, user, minor, bot, rvnew, size);
        }

        public void setPrevious(long previous) {
            this.previous = previous;
        }

        @Override
        public String getText() throws IOException {
            return getRevisionText(getRevid());
        }
    }

    public class MockRevision {
        public final String title;
        public final String currentTitle;
        @Nullable
        public final TestRevision revision;

        /**
         * Constructs object initialized with provided title, currentTitle and revision (optional).
         */
        public MockRevision(String title, String currentTitle, TestRevision revision) {
            this.title = title;
            this.currentTitle = currentTitle;
            this.revision = revision;
        }
    }
}
