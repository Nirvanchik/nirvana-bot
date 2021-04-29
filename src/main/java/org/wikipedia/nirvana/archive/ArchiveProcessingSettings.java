/**
 *  @(#)ArchiveProcessingSettings.java
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

package org.wikipedia.nirvana.archive;


/**
 * Class to keep settings of archive processing and updating rules.
 */
public class ArchiveProcessingSettings extends ScanArchiveSettings {
    /**
     * Flag for processing existing archive. If <code>true</code> archive should be sorted according
     * to new page creation dage and 'addToTop' parameter.
     */
    public boolean sorted;
    /**
     * Flag for processing existing archive. If <code>true</code> the code should search duplicated
     * items and remove them.
     */
    public boolean removeDuplicates;

    /**
     * Constructs instance of ArchiveProcessingSettings.
     */
    public ArchiveProcessingSettings() {
        super();
    }
}
