/**
 *  @(#)LocalizedTemplate.java 17.02.2017
 *  Copyright © 2017 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.localization;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a localization possibility for templates copied in different wikis.
 * Wikis in different languages have the same templates often but those templates
 * may have different title and different parameter names. 
 */
public class LocalizedTemplate {
    protected final String name;
    private final String localizedName;
    private final Map<String, String> params = new HashMap<>();

    /**
     * Constructs localized template object with original title <code>name</code> and localized
     * title <code>localizedName</code>.
     */
    public LocalizedTemplate(String name, String localizedName) {
        this.name = name;
        this.localizedName = localizedName;
    }
    
    /**
     * @return localized name of this template (without Template: prefix).
     */
    public String localizeName() {
        return localizedName;
    }
    
    /**
     * @return localized param name for param <code>param</code>.
     */
    public String localizeParam(String param) {
        return params.get(param);
    }
    
    void putParam(String param, String translation) {
        params.put(param, translation);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LocalizedTemplate{{ ").append(name).append(" => ").append(localizedName);
        for (Map.Entry<String, String> pair: params.entrySet()) {
            sb.append("|").append(pair.getKey()).append(" => ").append(pair.getValue());
        }
        sb.append("}}");
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof LocalizedTemplate) {
            LocalizedTemplate right = (LocalizedTemplate) obj;
            if (!name.equals(right.name)) return false;
            if (!localizedName.equals(right.localizedName)) return false;
            if (!params.equals(right.params)) return false;
            return true;
        }
        return false;
    }
    
    /**
     * Use this for default translations (when language is the default language).
     */
    public static class Default extends LocalizedTemplate {

        public Default(String name) {
            super(name, null);
        }
        
        @Override
        public String localizeName() {
            return name;
        }
        
        @Override
        public String localizeParam(String param) {
            return param;
        }

        @Override
        public String toString() {
            return "LocalizedTemplate{{" + name + "(no translation)}}";
        }
    }
}
