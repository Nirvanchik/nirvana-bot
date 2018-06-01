/**
 *  @(#)BotVariables.java 12.03.2017
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

package org.wikipedia.nirvana.nirvanabot;

import org.wikipedia.nirvana.localization.Localizer;

/**
 * Placeholders used by bot. Localized automatically.
 * You must call {@link #init()} method before using this class.
 */
public class BotVariables {
    private static boolean initialized;
    
    public static String TITLE;
    public static String TEXT_WITH_TITLE;
    public static String DATE;
    public static String DISCUSSION;
    public static String FILE_NAME;
    public static String AUTHOR;
    public static String BOT;
    public static String PROJECT;
    public static String PORTAL;
    public static String PAGE;
    public static String ARCHIVE;

    /**
     * Initialization. Must be called before using any constants of this class.
     */
    public static void init() {
        if (initialized) return;
        Localizer localizer = Localizer.getInstance();

        TITLE = localizer.localize("%(��������)");
        TEXT_WITH_TITLE = localizer.localize("%(�����_�_���������)");
        DATE = localizer.localize("%(����)");
        DISCUSSION = localizer.localize("%(����������)");
        DISCUSSION = localizer.localize("%(����������)");
        FILE_NAME = localizer.localize("%(��� �����)");
        AUTHOR = localizer.localize("%(�����)");
        BOT = localizer.localize("%(���)");
        PROJECT = localizer.localize("%(������)");
        PORTAL = localizer.localize("%(������)");
        PAGE = localizer.localize("%(��������)");
        ARCHIVE = localizer.localize("%(�����)");

        initialized = true;
    }
}
