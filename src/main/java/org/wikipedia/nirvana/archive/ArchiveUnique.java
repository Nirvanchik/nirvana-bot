/**
 *  @(#)ArchiveUnique.java 13.07.2014
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

package org.wikipedia.nirvana.archive;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

import javax.security.auth.login.LoginException;

import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.nirvanabot.NewPages;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

/**
 * @author kin
 *
 */
public class ArchiveUnique extends ArchiveSimple {
    NirvanaWiki wiki;

    //protected ArrayList<String> items;
    protected HashMap<String,Integer> uniqueItemIndexes;
    protected HashMap<String,Revision> uniqueItemRevisions;
    
    public String toString() {
        while(items.remove(null));
        if(addToTop) {
            Collections.reverse(items);
            return super.toString();
        }
        else
            return super.toString();  // Для склейки нужно отсутствие переноса. 
    }
    
    public ArchiveUnique(NirvanaWiki wiki, String lines[], boolean addToTop, String delimeter) {
        super(addToTop, delimeter);
        log.debug("ArchiveUnique created");
        this.wiki = wiki;
        //this.addToTop = addToTop;
        //this.delimeter = delimeter;
        //items = new ArrayList<String>();
        uniqueItemIndexes = new HashMap<String,Integer>();
        uniqueItemRevisions = new HashMap<String,Revision>();
    }

    @Override
    public void add(String item, Calendar c) {
        //this.newLines++;
        String title = NewPages.getNewPagesItemArticle(item);
        //boolean skip = false;
        if(title!=null) {
            String origTitle = null;
            try {
                origTitle = wiki.resolveRedirect(title);
            } catch (IOException e) {                
                //e.printStackTrace();
            }
            if(origTitle!=null)
                title = origTitle;
            if(this.uniqueItemIndexes.containsKey(title)) {
                /*int index = uniqueItemIndexes.get(title);
                String oldItem = items.get(index);
                if(oldItem.length()<item.length()) {
                    items.set(index, item);
                }*/
                // skip
            } else {                
                items.add(item);
                uniqueItemIndexes.put(title, items.size()-1);
            }
        } else {
            items.add(item);
        }
    }
    /*
    public void add(String item) {
        //this.newLines++;
        String title = NewPages.getNewPagesItemArticle(item);
        //boolean skip = false;
        if(title!=null) {
            String origTitle = null;
            Revision r = null;
            try {
                r = wiki.getFirstRevision(title, true);
            } catch (IOException e) {                
                //e.printStackTrace();
            }
            if(r==null) {
                items.add(item);
            } else {
                origTitle = r.getPage();
                if(!origTitle.equals(title))
                    title = origTitle;
                
                if(this.uniqueItemIndexes.containsKey(title)) {
                    int index = uniqueItemIndexes.get(title);
                    //String oldItem = items.get(index);
                    Revision oldRev = this.uniqueItemRevisions.get(title);
                    if(oldRev==null || oldRev.getTimestamp().after(r.getTimestamp())) {
                        items.set(index, null);
                        items.add(item);
                        uniqueItemIndexes.put(title, items.size()-1);
                        uniqueItemRevisions.put(title, r);
                    }
                    
                } else {                
                    items.add(item);
                    uniqueItemIndexes.put(title, items.size()-1);
                    uniqueItemRevisions.put(title, r);
                }
            }
        } else {
            items.add(item);
        }
    }*/
    
    public void update(NirvanaWiki wiki,String archiveName, boolean minor, boolean bot) throws LoginException, IOException {
        throw new java.lang.UnsupportedOperationException("update is not supported, use toString() instead");
    }
    /**
     * 
     */
    
    
    

}
