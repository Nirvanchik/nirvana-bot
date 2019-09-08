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
 * This file is encoded with UTF-8.
 * */

package org.wikipedia.nirvana.nirvanabot.pagesfetcher;

import org.wikipedia.nirvana.WikiTools;
import org.wikipedia.nirvana.WikiTools.Service;
import org.wikipedia.nirvana.nirvanabot.templates.TemplateFilter;

import java.io.IOException;
import java.util.List;

// TODO: Make it instance-based class.
// Client should create an instance of this class and call
// factory methods.
// Implementations may be hidden from user visibility.
// This will serve good for testing.
/**
 * @author kin
 *
 */
public abstract class FetcherFactory {

    private FetcherFactory() {
    }

	public static class NewPagesFetcher implements PageListFetcher {
		protected int hours;

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

    public static class NewPagesWithTemplatesFetcher implements PageListFetcher {
        protected TemplateFilter templateFilter;
        protected int hours;

        public NewPagesWithTemplatesFetcher(TemplateFilter templateFilter, int hours) {
            this.templateFilter = templateFilter;
            this.hours = hours;
        }

        @Override
        public String loadNewPagesForCatListAndIgnore(Service service,
                List<String> categories, List<String> categoriesToIgnore,
                String language, int depth, int namespace) throws IOException,
                InterruptedException {
            return WikiTools.loadNewPagesWithTemplatesForCatListAndIgnoreWithService(
                    service, categories, categoriesToIgnore, language, depth, depth,
                    templateFilter.getTemplates(), templateFilter.listType(), namespace);
        }

        @Override
        public String loadNewPagesForCat(Service service, String category,
                String language, int depth, int namespace) throws IOException,
                InterruptedException {
            return WikiTools.loadNewPagesWithTemplatesForCatWithService(
                    service, category, language, depth, hours, templateFilter.getTemplates(),
                    templateFilter.listType(), namespace);
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
        TemplateFilter templateFilter;

        public PagesWithTemplatesFetcher(TemplateFilter templateFilter) {
            this.templateFilter = templateFilter;
		}

		@Override
		public String loadNewPagesForCatListAndIgnore(Service service,
		        List<String> categories, List<String> categoriesToIgnore,
		        String language, int depth, int namespace) throws IOException,
		        InterruptedException {
			return WikiTools.loadPagesWithTemplatesForCatListAndIgnoreWithService(service, 
                    categories, categoriesToIgnore, language, depth, templateFilter.getTemplates(),
                    templateFilter.listType(), namespace);
		}

        @Override
        public String loadNewPagesForCat(Service service, String category,
                String language, int depth, int namespace) throws IOException,
                InterruptedException {
        	return WikiTools.loadPagesWithTemplatesForCatWithService(service, 
                    category, language, depth, templateFilter.getTemplates(),
                    templateFilter.listType(), namespace);
        }
	}
}
