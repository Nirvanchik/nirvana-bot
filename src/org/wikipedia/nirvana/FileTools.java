/**
 *  @(#)FileTools.java 07/04/2012
 *  Copyright © 2011 - 2014 Dmitry Trofimovich (KIN)
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

package org.wikipedia.nirvana;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author kin
 *
 */
public class FileTools {
	public static final String UTF8 = "UTF-8";
	public static final String CP1251 = "CP1251";
	public static String DEFAULT_ENCODING = UTF8;

    protected static org.apache.logging.log4j.Logger log4j;
	protected static Logger logger;
	static {
		try {
            log4j = org.apache.logging.log4j.LogManager.getLogger(FileTools.class.getName());
		} catch (java.lang.NoClassDefFoundError e) {
			logger = Logger.getLogger("FileTools");
		}
	}
	
	public void setDefaultEncoding(String encoding) {
		DEFAULT_ENCODING = encoding;
	}
	
	public static void dump(String text, String folder, String file) throws IOException {
		dump(text, folder, file, DEFAULT_ENCODING);
	}
	
	private static void logD(Object ob) {
		if (log4j!=null) {
			log4j.debug(ob);
		} else {
			logger.fine(ob.toString());
		}
	}

    private static void logW(Object ob) {
        if (log4j != null) {
            log4j.warn(ob);
        } else {
            logger.warning(ob.toString());
        }
    }

	private static void logE(Object ob) {
		if (log4j!=null) {
			log4j.error(ob);
		} else {
			logger.severe(ob.toString());
		}
	}
	
	public static void dump(String text, String folder, String file, String encoding) throws IOException {
		String path = "";
		if(folder!=null) {
			if(folder.endsWith("\\"))
				folder = folder.substring(0, folder.length()-1);
			String folderGood = folder;//.replaceAll("\\\\", "_");
			File parentFolder = new File(folderGood);
			if(!parentFolder.exists()) {
				parentFolder.mkdirs();		
			}	
			if(!folderGood.isEmpty())
				path = folderGood+"\\";
		}
		String fileGood = path+normalizeFileName(file);
		FileOutputStream out = new FileOutputStream(new File(fileGood));
		OutputStreamWriter or = new OutputStreamWriter(out, encoding);
		or.write(text);
		or.close();
	}
	
	public static void append(String text, String file) throws IOException {
		append(text,file,UTF8);
	}
	
	public static void append(String text, String file, String encoding) throws IOException {
		FileOutputStream out = new FileOutputStream(new File(file),true);
		OutputStreamWriter or = new OutputStreamWriter(out, encoding);
		or.write(text);
		or.close();
	}
	
	public static void appendOld(String text, String file) throws IOException {
		FileOutputStream out = new FileOutputStream(new File(file),true);
		out.write(text.getBytes());
		out.close();
	}
	
	public static void writeFile(String text, String file) throws IOException {
		writeFile(text, file, UTF8);
	}
	
	public static void writeFile(String text, String file, String encoding) throws IOException {
		FileOutputStream out = new FileOutputStream(new File(file));
		OutputStreamWriter or = new OutputStreamWriter(out, encoding);
		or.write(text);
		or.close();
	}
	
	public static String normalizeFileName(String file) {
		return file.replaceAll("\\\\|\\/|\\:|\\*|\\?", "_");
	}
	
	public static String readWikiFile(String folder, String file) throws IOException {
		return readWikiFile(folder, file, DEFAULT_ENCODING);
	}
	
	public static String readWikiFile(String folder, String file, String encoding) throws IOException {
		File fileGood = new File(folder+"\\"+normalizeFileName(file));
		logD("reading  file: "+fileGood.getPath());
		logD("absolute path: "+ fileGood.getAbsolutePath());
		
		FileInputStream in = new FileInputStream(fileGood);
		BufferedReader b = new BufferedReader(new InputStreamReader(in, encoding));       
        String line;
        StringBuilder text = new StringBuilder(100000);
        while ((line = b.readLine()) != null)
        {	        	
            text.append(line);
            text.append("\n");
        }
        in.close();
	    return text.toString();
	}

    public static String readFileSilently(String fileName) {
        return readFileSilently(fileName, null, DEFAULT_ENCODING);
    }

    public static String readFileSilently(String fileName, String defaultVal) {
        return readFileSilently(fileName, defaultVal, DEFAULT_ENCODING);
    }

    public static String readFileSilently(String fileName, String defaultVal, String encoding) {
        String text = defaultVal;
		try {
	        text = readFile(fileName, encoding);			
        } catch (IOException e) {
            logE("Failed to read file: " + fileName);
            logE(e);
		}
        if (text == defaultVal && defaultVal != null) {
            logW("Using default value instead: "+ defaultVal);
        }
		return text;
	}
	
	public static String readFile(String fileName) throws IOException {
		return readFile(fileName, DEFAULT_ENCODING);
	}
	
	public static String readFile(String fileName, String encoding) throws IOException {
		String text = null;
		File file = new File(fileName);		
		
        StringBuilder sb = new StringBuilder(10000);
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader reader = new InputStreamReader(fis, encoding);
        char buf[] = new char[1000];
        while (true) {
            int readCount = reader.read(buf);
            if (readCount < 0) {
              break;
            }
            sb.append(buf, 0, readCount);
          }
        text = sb.toString();
        reader.close();
		return text;
	}
	
	@Deprecated
	public static String [] readFileToArray(String fileName, boolean removeEmpty) {
		return readFileToArray(fileName, DEFAULT_ENCODING, removeEmpty);
	}

	public static List<String> readFileToList(String fileName) {
		return readFileToList(fileName, DEFAULT_ENCODING, true);
	}
	
	public static List<String> readFileToList(String fileName, boolean removeEmpty) {
		return readFileToList(fileName, DEFAULT_ENCODING, removeEmpty);
	}

	@Deprecated
	public static String [] readFileToArray(String fileName, String encoding, boolean removeEmpty) {
		return readFileToList(fileName, encoding, removeEmpty).toArray(new String[0]);
	}

	public static List<String> readFileToList(String fileName, String encoding, boolean removeEmpty) {
		File file = new File(fileName);
		ArrayList<String> items = new ArrayList<String>(5000);
		try {
	        FileInputStream fis = new FileInputStream(file);
	        InputStreamReader isr = new InputStreamReader(fis, encoding);
	        BufferedReader br = new BufferedReader(isr);
	        String line = "";	        
	        while((line=br.readLine())!=null) {
	        	if (!(removeEmpty && line.trim().length() == 0))
	        		items.add(line);
	        }	        
	        br.close();
			isr.close();
			fis.close();
		} catch (FileNotFoundException e) {
			logE(e.toString());
			return null;
		} catch (IOException e) {
			logE(e.toString());
			e.printStackTrace();
			return null;
		}
		return items;
	}
}
