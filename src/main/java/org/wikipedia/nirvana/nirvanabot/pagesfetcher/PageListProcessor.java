/**
 * @(#)PageListProcessor.java 13.07.2014
 * Copyright © 2013 - 2014 Dmitry Trofimovich (KIN)(DimaTrofimovich@gmail.com)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * WARNING: This file may contain Russian characters.
 * This file is encoded with UTF-8.
 */

/**
 * WARNING: This file may contain Russian characters.
 * This file is encoded with UTF-8.
 * */

package org.wikipedia.nirvana.nirvanabot.pagesfetcher;

import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.ServiceError;

import java.io.IOException;
import java.util.List;

/**
 * @author kin
 *
 */
public interface PageListProcessor {
    List<Revision> getNewPages(NirvanaWiki wiki) throws IOException, InterruptedException, ServiceError;

    boolean revisionAvailable();

    boolean mayHaveDuplicates();
}
