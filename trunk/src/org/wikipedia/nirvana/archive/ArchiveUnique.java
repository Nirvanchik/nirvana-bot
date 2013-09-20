/**
 * 
 */
package org.wikipedia.nirvana.archive;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import javax.security.auth.login.LoginException;

import org.wikipedia.Wiki.Revision;
import org.wikipedia.nirvana.NirvanaWiki;
import org.wikipedia.nirvana.nirvanabot.NewPages;

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
			//return StringUtils.join(items, delimeter)+delimeter;// ������� ������ ����� ��� �������
		}
		else
			return super.toString(); // ��� ������� ����� ���������� �������� 
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
	
	public void add(String item) {
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
