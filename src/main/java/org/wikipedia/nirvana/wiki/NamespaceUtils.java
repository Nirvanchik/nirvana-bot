/**
 *  @(#)NamespaceUtils.java
 *  Copyright © 2020 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.wikipedia.nirvana.localization.Localizer;

/**
 * Simple namespace-related utilities.
 *
 */
public class NamespaceUtils {
    private static String sUserNamespace;
    private static String USER_NAMESPACE_EN = "User:";
    private static String sUserTalkNamespace;
    private static String USER_TALK_NAMESPACE_EN = "User_talk:";
    private static String sUserTalkNamespaceUnderscored;

    private static String sCategoryNamespace;
    private static String CATEGORY_NAMESPACE_EN = "Category:";


    private static boolean initialized; 

    /**
     * Initialize statics before using this class (optional).
     */
    public static void initStatics() {
        if (initialized) {
            return;
        }
        Localizer localizer = Localizer.getInstance();
        sUserNamespace = localizer.localize("Участник:");
        sUserTalkNamespace = localizer.localize("Обсуждение участника:");
        sUserTalkNamespaceUnderscored = sUserTalkNamespace.replace(" ", "_");
        
        sCategoryNamespace = localizer.localize("Категория:");
        initialized = true;
    }

    /**
     * @return <code>true</code> if article is from user namespace or user talk page.
     */
    public static boolean userNamespace(String article) {
        initStatics();
        if (article == null) {
            throw new NullPointerException("article argument is null");
        }

        return (article.startsWith(sUserNamespace) ||
                article.startsWith(sUserTalkNamespace) ||
                article.startsWith(sUserTalkNamespaceUnderscored) ||
                article.startsWith(USER_NAMESPACE_EN) ||
                article.startsWith(USER_TALK_NAMESPACE_EN));
    }

    /**
     * @return <code>true</code> if article is from category namespace.
     */
    public static boolean categoryNamespace(String article) {
    	initStatics();
        if (article == null) {
            throw new NullPointerException("article argument is null");
        }
        return (article.startsWith(sCategoryNamespace) ||
                article.startsWith(CATEGORY_NAMESPACE_EN) );
	}
}
