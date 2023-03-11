/**
 *  @(#)JsonUtils.java
 *  Copyright © 2023 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.testing;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utilities for parsing complicated objects and collections from Json.
 *
 */
public class JsonUtils {

    /**
     * Read list of strings from json object child item.
     */
    public static List<String> readStringList(JSONObject jsonObject, String name) {
        JSONArray list = (JSONArray) jsonObject.get(name);
        Iterator<?> it = list.iterator();
        List<String> result = new ArrayList<String>();
        while (it.hasNext()) {
            result.add((String) it.next());
        }
        return result;
    }

    /**
     * Read multiline string from json object child item.
     */
    public static String readMultiLineString(JSONObject object, String name) {
        return parseMultiLineString(object.get(name));
    }

    /**
     * Parse list of multiline strings from json array.
     */
    public static List<String> parseMultilineStringArray(JSONArray jsonResponces) {
        List<String> list = new ArrayList<String>();
        Iterator<?> it = jsonResponces.iterator();
        while (it.hasNext()) {
            String val = parseMultiLineString(it.next());
            list.add(val);
        }
        return list;
    }

    /**
     * Read json item that can be a single string or array of strings.
     * If it is array of strings, they will be joined with new line symbol into multiline text.
     */
    public static String parseMultiLineString(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof String) {
            return (String) object;
        } else {
            JSONArray list = (JSONArray) object;
            StringBuilder b = new StringBuilder();
            Iterator<?> it = list.iterator();
            while (it.hasNext()) {
                String str = (String) it.next();
                b.append(str).append("\n");
            }
            // Remove last "\n"
            if (b.length() > 0) {
                b.setLength(b.length() - 1);
            }
            return b.toString();
        }
    }

}
