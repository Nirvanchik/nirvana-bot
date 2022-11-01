/**
 *  @(#)FetcherFactory.java
 *  Copyright © 2022 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import org.wikipedia.nirvana.nirvanabot.templates.TemplateFilter;
import org.wikipedia.nirvana.wiki.CatScanTools;
import org.wikipedia.nirvana.wiki.CatScanTools.Service;

import java.io.IOException;
import java.util.List;

// TODO: Make it instance-based class.
// Client should create an instance of this class and call
// factory methods.
// Implementations may be hidden from user visibility.
// This will serve good for testing.
/**
 * Factory class that keeps multiple implementations of page list fetchers.
 *
 */
public abstract class FetcherFactory {

    private FetcherFactory() {
    }

    /**
     * Fetcher that can fetch list of new pages.
     *
     */
    public static class NewPagesFetcher implements PageListFetcher {
        protected int hours;

        /**
         * Constructs fetcher using provided "hours" parameter.
         *
         * @param hours it will search only pages created not before this number of hours.
         */
        public NewPagesFetcher(int hours) {
            this.hours = hours;
        }

        @Override
        public String loadNewPagesForCatListAndIgnore(Service service,
                List<String> categories, List<String> categoriesToIgnore,
                String language, int depth, int namespace)
                        throws IOException, InterruptedException {
            return CatScanTools.loadNewPagesForCatListAndIgnoreWithService(service, categories,
                    categoriesToIgnore, language, depth, hours, namespace);
        }

        @Override
        public String loadNewPagesForCat(Service service, String category, String language,
                int depth, int namespace) throws IOException, InterruptedException {
            return CatScanTools.loadNewPagesForCatWithService(service, category, language, depth,
                    hours, namespace);
        }
    }

    /**
     * Fetcher that can fetch list of new pages containing specific templates in their text.
     *
     */
    public static class NewPagesWithTemplatesFetcher implements PageListFetcher {
        protected TemplateFilter templateFilter;
        protected int hours;

        /**
         * Constructs fetcher using provided {@class TemplateFilter} and hours value.
         *
         * @param templateFilter template filter to use when searching new pages.
         * @param hours it will search only pages created not before this number of hours.
         */
        public NewPagesWithTemplatesFetcher(TemplateFilter templateFilter, int hours) {
            this.templateFilter = templateFilter;
            this.hours = hours;
        }

        @Override
        public String loadNewPagesForCatListAndIgnore(Service service,
                List<String> categories, List<String> categoriesToIgnore,
                String language, int depth, int namespace) throws IOException,
                InterruptedException {
            return CatScanTools.loadNewPagesWithTemplatesForCatListAndIgnoreWithService(
                    service, categories, categoriesToIgnore, language, depth, depth,
                    templateFilter.getTemplates(), templateFilter.listType(), namespace);
        }

        @Override
        public String loadNewPagesForCat(Service service, String category,
                String language, int depth, int namespace) throws IOException,
                InterruptedException {
            return CatScanTools.loadNewPagesWithTemplatesForCatWithService(
                    service, category, language, depth, hours, templateFilter.getTemplates(),
                    templateFilter.listType(), namespace);
        }
    }

    /**
     * Fetcher that can fetch list of all wiki pages in provided category or categories.
     *
     */
    public static class PagesFetcher implements PageListFetcher {

        /**
         * Creates fetcher.
         */
        public PagesFetcher() {
        }

        @Override
        public String loadNewPagesForCatListAndIgnore(Service service,
                List<String> categories, List<String> categoriesToIgnore,
                String language, int depth, int namespace) throws IOException,
                InterruptedException {        
            return CatScanTools.loadPagesForCatListAndIgnoreWithService(service, categories,
                    categoriesToIgnore, language, depth, namespace);
        }

        @Override
        public String loadNewPagesForCat(Service service, String category,
                String language, int depth, int namespace) throws IOException,
                InterruptedException {
            return CatScanTools.loadPagesForCatWithService(service, category, language, depth,
                    namespace);
        }
    }

    /**
     * Fetcher that can fetch list of all wiki pages in provided category or categories and 
     * containing specific templates in their body.
     *
     */
    public static class PagesWithTemplatesFetcher implements PageListFetcher {
        TemplateFilter templateFilter;

        /**
         * Constructs fetcher using provided {@class TemplateFilter}.
         *
         * @param templateFilter template filter to use when searching new pages.
         */
        public PagesWithTemplatesFetcher(TemplateFilter templateFilter) {
            this.templateFilter = templateFilter;
        }

        @Override
        public String loadNewPagesForCatListAndIgnore(Service service,
                List<String> categories, List<String> categoriesToIgnore,
                String language, int depth, int namespace) throws IOException,
                InterruptedException {
            return CatScanTools.loadPagesWithTemplatesForCatListAndIgnoreWithService(service,
                    categories, categoriesToIgnore, language, depth, templateFilter.getTemplates(),
                    templateFilter.listType(), namespace);
        }

        @Override
        public String loadNewPagesForCat(Service service, String category,
                String language, int depth, int namespace) throws IOException,
                InterruptedException {
            return CatScanTools.loadPagesWithTemplatesForCatWithService(service,
                    category, language, depth, templateFilter.getTemplates(),
                    templateFilter.listType(), namespace);
        }
    }
}
