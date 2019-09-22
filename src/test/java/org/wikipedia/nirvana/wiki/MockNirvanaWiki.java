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

import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

/**
 * Mocks a lot of methods of {@link NirvanaWiki} and {@link org.wikipedia.Wiki}
 * Prevents access to Wiki with Mediawiki REST API with methods like <code>edit()</code>.
 */
public class MockNirvanaWiki extends NirvanaWiki {
    private Map<String,String []> whatTranscludesMap = new HashMap<>();
    private Map<String, String> pageTextMap = new HashMap<>();
    private Map<Long, String> namespaceIdMap = new HashMap<>();
    private Map<String, Integer> namespaceMap = new HashMap<>();
    private Map<String, Revision> topRevMap = new HashMap<>();
    private Map<String, Revision> firstRevMap = new HashMap<>();
    private Map<String, String []> pageTemplatesMap = new HashMap<>();
    private Map<String, Boolean> pageExistsMap = new HashMap<>();
    private Map<String, String []> whatLinksHereMap = new HashMap<>();

    private LinkedList<String> fetchQueue = new LinkedList<>();

    private User user;

    private List<EditInfo> edits = new ArrayList<>();
    private boolean allowEdits = false;

    public static class EditInfo extends EditInfoMinimal {
        String summary;
        boolean minor;
        boolean bot;
        Calendar basetime;

        /**
         * Constructs EditInfo object with all fields specified.
         */
        public EditInfo(String title, String text, String summary, boolean minor, boolean bot,
                int section, Calendar basetime) {
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

    @Override
    public synchronized void login(String username, char[] password)
            throws IOException, FailedLoginException {
        // We don't login in tests
        user = new MockUser(username);
    }

    @Override
    public User getCurrentUser() {
        return user;
    }

    @Override
    protected String fetch(String url, String caller) throws IOException {
        log.debug("fetch url: {}", url);
        Assert.assertFalse(fetchQueue.isEmpty());
        return fetchQueue.removeFirst();
    }

    public void mockFetchSequential(String response) {
        fetchQueue.addLast(response);
    }

    @Override
    protected String post(String url, String text, String caller) throws IOException {
        log.debug("post url: {}", url);
        throw new RuntimeException("Post is not allowed in tests!");
    }

    @Override
    public String[] whatTranscludesHere(String title, int... ns) throws IOException {
        Assert.assertTrue(whatTranscludesMap.containsKey(title));
        return whatTranscludesMap.get(title);
    }

    public void mockWhatTranscludesHere(String title, String []whatTranscludes) {
        whatTranscludesMap.put(title, whatTranscludes);
    }

    @Override
    public String getPageText(String title) throws IOException {
        if (!pageTextMap.containsKey(title)) {
            throw new FileNotFoundException(title);
        }
        String text = pageTextMap.get(title);
        // TODO(KIN): Do we really need this?
        if (text.equals("<java.io.FileNotFoundException>")) {
            throw new FileNotFoundException(title);
        }
        return text;
    }

    @Override
    public String[] getPageText(String[] titles) throws IOException {
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
    public String namespaceIdentifier(int namespace) throws IOException {
        Assert.assertTrue(namespaceIdMap.containsKey(new Long(namespace)));
        return namespaceIdMap.get(new Long(namespace));
    }

    public void mockNamespaceIdentifier(Long namespaceNumber, String namespaceName) {
        namespaceIdMap.put(namespaceNumber, namespaceName);
    }

    @Override
    public int namespace(String title) throws IOException {
        Assert.assertTrue(namespaceMap.containsKey(title));
        return namespaceMap.get(title);
    }

    public void mockNamespace(String title, Integer namespaceNumber) {
        namespaceMap.put(title, namespaceNumber);
    }

    @Override
    public Revision getTopRevision(String title) throws IOException {
        if (!topRevMap.containsKey(title)) return null;
        return topRevMap.get(title);
    }

    public void mockFirstRevision(String title, Revision revision) {
        firstRevMap.put(title, revision);
    }

    @Override
    public Revision getFirstRevision(String title) throws IOException {
        if (!firstRevMap.containsKey(title)) return null;
        return firstRevMap.get(title);
    }

    @Override
    public synchronized void edit(String title, String text, String summary, boolean minor,
            boolean bot, int section, Calendar basetime) throws IOException, LoginException {
        EditInfo edit = new EditInfo(title, text, summary, minor, bot, section, basetime);
        edits.add(edit);
        if (allowEdits) {
            super.edit(title, text, summary, minor, bot, section, basetime);
        }
    }

    public List<EditInfo> getEdits() {
        return edits;
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
        if (pageExistsMap.containsKey(title)) {
            return pageExistsMap.get(title);
        }
        return false;
    }

    public void mockExists(String title, boolean exists) {
        pageExistsMap.put(title, exists);
    }

    @Override
    public String[] whatLinksHere(String title, int... ns) throws IOException {
        if (!whatLinksHereMap.containsKey(title)) return new String [0];
        return whatLinksHereMap.get(title);
    }

    public void mockWhatLinksHere(String title, String [] links) {
        whatLinksHereMap.put(title, links);
    }

    public void mockWhatLinksHere(String title, List<String> links) {
        whatLinksHereMap.put(title, links.toArray(new String[links.size()]));
    }
}
