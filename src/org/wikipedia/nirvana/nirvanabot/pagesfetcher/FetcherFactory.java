/**
 *  @(#)FetcherFactory.java 13.12.2014
 *  Copyright © 2014 Dmitry Trofimovich (KIN)(DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.nirvanabot.pagesfetcher;

import java.io.IOException;
import java.util.List;

import org.wikipedia.nirvana.WikiTools;
import org.wikipedia.nirvana.WikiTools.Service;

/**
 * @author kin
 *
 */
public abstract class FetcherFactory {

	/**
	 * 
	 */
	public FetcherFactory() {
		// TODO Auto-generated constructor stub
	}
	
	public static class NewPagesFetcher implements PageListFetcher {
		protected int hours;
		/**
		 * @param hours
		 */
		public NewPagesFetcher(int hours) {
			this.hours = hours;
		}
		
		@Override
		public String loadNewPagesForCatListAndIgnore(Service service,
	            List<String> categories, List<String> categoriesToIgnore,
	            String language, int depth, int namespace) throws IOException, InterruptedException {
			return WikiTools.loadNewPagesForCatListAndIgnoreWithService(service, categories, categoriesToIgnore, language, depth, hours, namespace);
		}

        @Override
        public String loadNewPagesForCat(Service service, String category, String language,
                int depth, int namespace) throws IOException, InterruptedException {
        	return WikiTools.loadNewPagesForCatWithService(service, category, language, depth, hours, namespace);
        }
	}
	
	public static class PagesFetcher implements PageListFetcher {

		/**
		 */
		public PagesFetcher() {
		}

		@Override
        public String loadNewPagesForCatListAndIgnore(Service service,
		        List<String> categories, List<String> categoriesToIgnore,
		        String language, int depth, int namespace) throws IOException,
		        InterruptedException {		
			return WikiTools.loadPagesForCatListAndIgnoreWithService(service, categories, categoriesToIgnore, language, depth, namespace);
		}

        @Override
        public String loadNewPagesForCat(Service service, String category,
                String language, int depth, int namespace) throws IOException,
                InterruptedException {
        	return WikiTools.loadPagesForCatWithService(service, category, language, depth, namespace);
        }
	}
	
	public static class PagesWithTemplatesFetcher implements PageListFetcher {
		List<String> templates;
		WikiTools.EnumerationType templateEnumType;

		public PagesWithTemplatesFetcher(List<String> templates, WikiTools.EnumerationType enumType) {
			this.templates = templates;
			this.templateEnumType = enumType;
		}

		@Override
		public String loadNewPagesForCatListAndIgnore(Service service,
		        List<String> categories, List<String> categoriesToIgnore,
		        String language, int depth, int namespace) throws IOException,
		        InterruptedException {
			return WikiTools.loadPagesWithTemplatesForCatListAndIgnoreWithService(service, 
					categories, categoriesToIgnore, language, depth, templates, templateEnumType, namespace);
		}

        @Override
        public String loadNewPagesForCat(Service service, String category,
                String language, int depth, int namespace) throws IOException,
                InterruptedException {
        	return WikiTools.loadPagesWithTemplatesForCatWithService(service, 
        			category, language, depth, templates, templateEnumType, namespace);
        }
	}

}
