/**
 *  @(#)FileTools.java 07/04/2012
 *  Copyright © 2011 - 2012 Dmitry Trofimovich (KIN)
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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * @author kin
 *
 */
public class FileTools {
	protected static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FileTools.class.getName());
	
	public static void dump(String text, String folder, String file) throws IOException {
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
		String fileGood = path+file.replaceAll("\\\\|\\/|\\:|\\*|\\?", "_");
		//System.out.println("old:"+file+", new:"+fileGood);
		FileOutputStream out = new FileOutputStream(new File(fileGood));
		out.write(text.getBytes());
		out.close();
	}
	
	public static String normalizeFileName(String file) {
		return file.replaceAll("\\\\|\\/|\\:|\\*|\\?", "_");
	}
	public static void append(String text, String file) throws IOException {
		FileOutputStream out = new FileOutputStream(new File(file),true);
		out.write(text.getBytes());
		out.close();
	}
	public static String read(String folder, String file) throws IOException {
		String fileGood = folder+"\\"+file.replaceAll("\\\\|\\/|\\:|\\*|\\?", "_");
		FileInputStream in = new FileInputStream(new File(fileGood));
		BufferedReader b = new BufferedReader(new InputStreamReader(in, "cp1251"));       

	        // get the text
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
	
	public static String readFile(String fileName) {
		String text = null;
		File file = new File(fileName);
		try {
			//FileInputStream in = new FileInputStream(taskFile);
			//String line;
	        StringBuilder sb = new StringBuilder(10000);
	        FileReader fr = null;
	        fr = new FileReader(file);
	       // BufferedReader br = new BufferedReader(fr);
	        
	        //fr.close();
	        char buf[] = new char[1000];
	       // fr.read(buf);
	        //log.info(new String(buf));
	        int end = 0;
	        while ((end=fr.read(buf))>0)
	        {	        	
	            sb.append(buf,0,end);	           
	        }
	        //sb.append(buf);
			fr.close();
			text = sb.toString();
		} catch (FileNotFoundException e) {
			log.error(e.toString());
			return null;
		} catch (IOException e) {
			log.error(e.toString());
			e.printStackTrace();
			return null;
		}
		return text;
	}
	
	public static String [] readFileToList(String fileName) {
		File file = new File(fileName);
		ArrayList<String> items = new ArrayList<String>(5000);
		try {
			//FileInputStream in = new FileInputStream(taskFile);
			//String line;
	        FileReader fr = null;
	        fr = new FileReader(file);
	        BufferedReader br = new BufferedReader(fr);
	        String line = "";	        
	        while((line=br.readLine())!=null) {
	        	items.add(line);
	        }	        
	        br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			log.error(e.toString());
			return null;
		} catch (IOException e) {
			log.error(e.toString());
			e.printStackTrace();
			return null;
		}
		return items.toArray(new String[0]);
	}
	
	public static String [] readFileToListUTF8(String fileName) {
		File file = new File(fileName);
		ArrayList<String> items = new ArrayList<String>(5000);
		try {
			//FileInputStream in = new FileInputStream(taskFile);
			//String line;
	        //FileReader fr = null;
	        //fr = new FileReader(file);
	        FileInputStream fis = new FileInputStream(file);
	        InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
	        BufferedReader br = new BufferedReader(isr);
	        String line = "";	        
	        while((line=br.readLine())!=null) {
	        	//String UTF8Str = new String(line.getBytes(),"UTF-8");
	        	//items.add(UTF8Str);
	        	items.add(line);
	        }	        
	        br.close();
			isr.close();
			fis.close();
		} catch (FileNotFoundException e) {
			log.error(e.toString());
			return null;
		} catch (IOException e) {
			log.error(e.toString());
			e.printStackTrace();
			return null;
		}
		return items.toArray(new String[0]);
	}
}
