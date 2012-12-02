/**
 * 
 */
package org.wikipedia.nirvana;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.lang3.StringUtils;
import org.wikipedia.Wiki;

/**
 * @author kin
 *
 */
public class ListTools {
	enum COMMAND {
		NONE,
		SUBSTRACTION,
		UNION,
		INTERSECTION
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length==0) {
			return;
		}
		COMMAND cmd = COMMAND.NONE;
		String list1 = "",list2="";
		String out = "";
		for(String str:args) {
			if(str.startsWith("-sub")) {
				cmd = COMMAND.SUBSTRACTION;
			} else if(str.startsWith("-uni")) {
				cmd = COMMAND.UNION;
			} else if(str.startsWith("-int")) {
				cmd = COMMAND.INTERSECTION;
			} else if(str.startsWith("-left:")) {
				list1 = str.substring(6);
			} else if(str.startsWith("-list1:")) {
				list1 = str.substring(7);
			} else if(str.startsWith("-a:")) {
				list1 = str.substring(3);
			}else if(str.startsWith("-right:")) {
				list2 = str.substring(7);
			} else if(str.startsWith("-list2:")) {
				list2 = str.substring(7);
			} else if(str.startsWith("-b:")) {
				list2 = str.substring(3);
			} else if(str.startsWith("-out:")) {
				out = str.substring(5);
			}
		}
		if(cmd==COMMAND.NONE || list1.isEmpty() || list2.isEmpty()) {
			return;
		}
		if(out.isEmpty()) {
			out = "out_"+cmd.toString()+".txt";			
		}
		
		String lines1[] = FileTools.readFileToListUTF8(list1);
		String lines2[] = FileTools.readFileToListUTF8(list2);
		//String lines1[] = FileTools.readFileToList(list1);
		//String lines2[] = FileTools.readFileToList(list2);
		if(lines1==null || lines1.length==0) return;
		if(lines2==null || lines2.length==0) return;
		String lines3[] = null;
		switch(cmd) {
			case SUBSTRACTION:
				lines3 = Wiki.relativeComplement(lines1, lines2);
				break;
			case INTERSECTION:
				lines3 = Wiki.intersection(lines1, lines2);
				break;
			case UNION:
				lines3 = new String[0];
				break;
		}
		String text = StringUtils.join(lines3, "\r\n");
		try {
			//File f = new File(out);
			//FileUtils.write(f, "Contents", "UTF-8");
			FileOutputStream os = new FileOutputStream(new File(out));
			OutputStreamWriter sw = new OutputStreamWriter(os,"UTF-8");
			sw.write(text);
			//os.write(text.getBytes());
			sw.close();
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
