/**
 * 
 */
package org.wikipedia.nirvana.statistics;

import java.util.List;

import org.wikipedia.nirvana.archive.ArchiveSettings;

/**
 * @author kin
 *
 */
public class StatisticsParam {
	public ArchiveSettings archiveSettings;
	public String archive;
	public List<String> reportTypes;
	public boolean sort;
	public boolean cacheonly;
	public boolean cache;
	public String comment;

}
