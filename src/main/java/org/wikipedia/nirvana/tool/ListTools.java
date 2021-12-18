/**
 *  @(#)ListTools.java 
 *  Copyright © 2012 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.tool;

import org.wikipedia.ArrayUtils;
import org.wikipedia.nirvana.util.FileTools;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.logging.Logger;

/**
 * Simple utility application that can merge text files that contain lists of items separated by
 * new line symbols. Merge operations supported: union, intersection and subtraction. *
 */
public class ListTools {

    Logger log = Logger.getLogger(ListTools.class.getSimpleName());

    /**
     * Empty constructor.
     */
    public ListTools() {
        // empty
    }

    enum Command {
        NONE,
        SUBSTRACTION,
        UNION,
        INTERSECTION
    }

    /**
     * Merge two files specified by file name.
     *
     * @param cmd merge command.
     * @param leftFileName file name of the left file
     * @param rightFileName file name of the right file
     * @param outFileName file name of the result file
     */
    public void merge(Command cmd, String leftFileName, String rightFileName,
            String outFileName) throws IOException {
        List<String> lines1;
        try {
            lines1 = FileTools.readFileToList(leftFileName, FileTools.UTF8, true);
        } catch (IOException e1) {
            log.severe("Failed to read file: " + leftFileName);
            throw e1;
        }
        List<String> lines2;
        try {
            lines2 = FileTools.readFileToList(rightFileName, FileTools.UTF8, true);
        } catch (IOException e1) {
            log.severe("Failed to read file: " + rightFileName);
            throw e1;
        }
        if (lines1.size() == 0) {
            log.severe("The first list is empty");
            throw new IllegalArgumentException();
        }
        if (lines2.size() == 0) {
            log.severe("The second list is empty");
            throw new IllegalArgumentException();
        }
        String [] lines3 = null;
        switch (cmd) {
            case SUBSTRACTION:
                lines3 = ArrayUtils.relativeComplement(
                        lines1.toArray(new String[0]), lines2.toArray(new String[0]));
                break;
            case INTERSECTION:
                lines3 = ArrayUtils.intersection(
                        lines1.toArray(new String[0]), lines2.toArray(new String[0]));
                break;
            case UNION:
                log.severe("Union command not supported. Sorry!");
                throw new IllegalArgumentException();
            default:
                log.severe("Unexpected command: " + cmd);
                throw new IllegalArgumentException();
        }
        String text = StringUtils.join(lines3, "\r\n");
        try (OutputStreamWriter sw = new OutputStreamWriter(new FileOutputStream(
                new File(outFileName)), "UTF-8")) {
            sw.write(text);
        }        
    }

    /**
     * Entry point.
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("List merge utility. Command line:\n"
                    + "> java -jar listtools.jar "
                    + "COMMAND -left <LEFT FILE> -right <RIGHT FILE> [-out <OUT FILE>]\n"
                    + "Commands: intersect, subtract, union.");
            return;
        }
        Command cmd = Command.NONE;
        String list1 = "";
        String list2 = "";
        String out = "";
        for (String str: args) {
            if (str.startsWith("sub")) {
                cmd = Command.SUBSTRACTION;
            } else if (str.startsWith("uni")) {
                cmd = Command.UNION;
            } else if (str.startsWith("int")) {
                cmd = Command.INTERSECTION;
            } else if (str.startsWith("-left:")) {
                list1 = str.substring(6);
            } else if (str.startsWith("-list1:")) {
                list1 = str.substring(7);
            } else if (str.startsWith("-a:")) {
                list1 = str.substring(3);
            } else if (str.startsWith("-right:")) {
                list2 = str.substring(7);
            } else if (str.startsWith("-list2:")) {
                list2 = str.substring(7);
            } else if (str.startsWith("-b:")) {
                list2 = str.substring(3);
            } else if (str.startsWith("-out:")) {
                out = str.substring(5);
            }
        }
        if (cmd == Command.NONE) {
            return;
        }
        if (list1.isEmpty() || list2.isEmpty()) { 
            return;
        }
        if (out.isEmpty()) {
            out = "out_" + cmd.toString() + ".txt";            
        }

        new ListTools().merge(cmd, list1, list2, out);
    }
}
