/**
 *  @(#)DiscussedPages.java 14.12.2014
 *  Copyright © 2014 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.DateTools;
import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.ServiceError;

/**
 * @author kin
 *
 */
public class DiscussedPages extends Pages {
	String prefix;

	protected class DiscussedPagesBuffer extends NewPagesBuffer {

		/**
         * @param wiki
         */
        public DiscussedPagesBuffer(NirvanaWiki wiki) {
	        super(wiki);	        
        }
        
        @Override
        protected String getTimeString(Revision rev) {
        	return DateTools.printDateDayMonthYearGenitiveRussian(rev.getTimestamp());
		}
	}
	
	private static class RevisionWithModifiedDate extends Revision {

		/**
         */
        public RevisionWithModifiedDate(Wiki wiki, Revision r, Calendar newDate) {
	        wiki.super(r.getRevid(), newDate, r.getPage(), r.getSummary(), r.getUser(), r.isMinor(), r.isBot(), r.isNew(), r.getSize());
	        // TODO Auto-generated constructor stub
        }
		
	}

	/**
	 * @param param
	 */
	public DiscussedPages(PortalParam param) {
		super(param);
		this.prefix = param.prefix;
	}
	
	@Override
	protected NewPagesBuffer createPagesBuffer(NirvanaWiki wiki) {
		return new DiscussedPagesBuffer(wiki);
	}
	
	@Override
	public void sortPages(ArrayList<Revision> pageInfoList, boolean byRevision) {
		// no sort (sort implemented in another place)
	}	
	
	@Override
	protected ArrayList<Revision> getNewPages(NirvanaWiki wiki) throws IOException, InterruptedException, ServiceError {
		ArrayList<Revision> pageInfoList = super.getNewPages(wiki);
		ArrayList<Revision> pageInfoListNew = new ArrayList<Revision>();
		//int count = pageInfoList.size();
		//count = count<maxItems?count:maxItems;
		for (int i = 0; i < pageInfoList.size() && pageInfoListNew.size() < maxItems; ++i)
		{
			Revision r = pageInfoList.get(i);
			String[] linkedPages = wiki.whatLinksHere(r.getPage(), Wiki.PROJECT_NAMESPACE);
			SortedSet<Calendar> dates = new TreeSet<Calendar>();
			for (String linked:linkedPages) {
				//linked = linked.substring(wiki.namespaceIdentifier(Wiki.PROJECT_NAMESPACE).length()+1);
				if (linked.startsWith(this.prefix)) {
					linked = linked.substring(this.prefix.length());
					Calendar c = DateTools.parseDateStringDayMonthYearGenitiveRussian(linked, new Locale("ru"));
					if (c != null) {
						dates.add(c);
					}
				}
			}
			if (dates.size() > 0) {
				pageInfoListNew.add(new RevisionWithModifiedDate(wiki, r, dates.last()));
			}		    
		}
		sortPagesByDate(pageInfoListNew);
		return pageInfoListNew;
	}

}
