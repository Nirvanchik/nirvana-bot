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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author kin
 *
 */
public class FileTools {
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
}
