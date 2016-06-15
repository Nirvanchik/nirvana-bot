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
 * Recommended code page for this file is CP1251 (also called Windows-1251).
 * */

package org.wikipedia.nirvana;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import junit.framework.Assert;

/**
 * Mocks a lot of methods of {@link NirvanaWiki} and {@link org.wikipedia.Wiki}
 * Prevents access to Wiki with Mediawiki REST API with methods like <code>edit()</code>.
 */
public class MockNirvanaWiki extends NirvanaWiki {
    private Map<String,String []> whatTranscludesMap = new HashMap<>();
    private Map<String, String> pageTextMap = new HashMap<>();
    private Map<Long, String> namespaceIdMap = new HashMap<>();
    private Map<String, Revision> topRevMap = new HashMap<>();
    private Map<String, Revision> firstRevMap = new HashMap<>();

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
    public synchronized void login(String username, char[] password, boolean rateLimit)
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
        throw new RuntimeException("Fetch is not allowed in tests!");
    }

    @Override
    protected String post(String url, String text, String caller) throws IOException {
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
}
