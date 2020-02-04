/**
 *  @(#)FileTools.java
 *  Copyright © 2011 Dmitry Trofimovich (KIN)
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

package org.wikipedia.nirvana.util;

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

import javax.annotation.Nullable;

/**
 * Utilities for reading and writing files.
 * Default encoding for all operations is UTF-8 but it can be changed using
 * {@link #setDefaultEncoding(String)} method.
 */
public class FileTools {
    public static final String UTF8 = "UTF-8";
    public static final String CP1251 = "CP1251";
    public static String sDefaultEncoding = UTF8;
    private static String sDefaultOut = null;

    protected static org.apache.logging.log4j.Logger log4j;
    protected static Logger logger;

    static {
        try {
            log4j = org.apache.logging.log4j.LogManager.getLogger(FileTools.class.getName());
        } catch (java.lang.NoClassDefFoundError e) {
            logger = Logger.getLogger("FileTools");
        }
    }

    /**
     * Sets default encoding. All subsequent method calls in this class will use this encoding
     * if not specified explicitely in arguments.
     *
     * @param encoding Encoding name. Use constants from this class: {@link #UTF8}, {@link #CP1251},
     *     etc.
     */
    public static void setDefaultEncoding(String encoding) {
        sDefaultEncoding = encoding;
    }

    public static void setDefaultOut(String folder) {
        sDefaultOut = folder;
    }

    private static void logD(Object ob) {
        if (log4j != null) {
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
        if (log4j != null) {
            log4j.error(ob);
        } else {
            logger.severe(ob.toString());
        }
    }

    /**
     * Dump text to file.
     * Mostly used to save Wiki pages to disk.
     * Default encoding will be used when converting Unicode characters.
     * Default parent folder will be used to save file (can be changed with
     * {@link #setDefaultOut(String)} method). 
     * File name will be normalized (see {@link #normalizeFileName(String)} for details.
     *
     * @param text Text to write to file (wiki code).
     * @param file File name.
     * @throws IOException if failed to write file.
     */
    public static void dump(String text, String file) throws IOException {
        assert sDefaultOut != null;

        dump(text, sDefaultOut, file, sDefaultEncoding);
    }

    /**
     * Dump text to file.
     * Mostly used to save Wiki pages to disk.
     * Default encoding will be used when converting Unicode characters.
     * 
     * The same as {@link #writeFile(String, String, String)} but with some features:
     *   - you should specify parent folder and file name separately;
     *   - parent folder may be missing: it will be created;
     *   - file name will be normalized (see {@link #normalizeFileName(String)} for details.
     *
     * @param text Text to write to file (wiki code).
     * @param folder Parent folder (path) of the file.
     * @param file File name.
     * @throws IOException if failed to write file.
     */
    public static void dump(String text, String folder, String file) throws IOException {
        dump(text, folder, file, sDefaultEncoding);
    }

    /**
     * Dump text to file.
     * Mostly used to save Wiki pages to disk.
     * 
     * The same as {@link #writeFile(String, String, String)} but with some features:
     *   - you should specify parent folder and file name separately;
     *   - parent folder may be missing: it will be created;
     *   - file name will be normalized (see {@link #normalizeFileName(String)} for details.
     *
     * @param text Text to write to file (wiki code).
     * @param folder Parent folder (path) of the file.
     * @param file File name.
     * @param encoding Encoding to use.
     * @throws IOException if failed to write file.
     */
    public static void dump(String text, @Nullable String folder, String file, String encoding)
            throws IOException {
        String path = "";
        if (folder != null) {
            if (folder.endsWith(File.separator)) {
                folder = folder.substring(0, folder.length() - 1);
            }
            String folderGood = folder;
            File parentFolder = new File(folderGood);
            if (!parentFolder.exists()) {
                parentFolder.mkdirs();        
            }    
            if (!folderGood.isEmpty()) {
                path = folderGood + File.separator;
            }
        }
        String fileGood = path + normalizeFileName(file);
        try (OutputStreamWriter or = new OutputStreamWriter(
                new FileOutputStream(new File(fileGood)), encoding)) {
            or.write(text);
        }
    }

    /**
     * Add text to the end of file.
     * File must exist or {@link FileNotFoundException} will be raised.
     * 
     * @param text Text to write at the end of file.
     * @param file File name or file path.
     * @throws FileNotFoundException if file not found.
     * @throws IOException if failed to write file.
     */
    public static void append(String text, String file) throws FileNotFoundException, IOException {
        append(text, file, UTF8);
    }

    /**
     * Add text to the end of file.
     * File must exist or {@link FileNotFoundException} will be raised.
     * 
     * @param text Text to write at the end of file.
     * @param file File name or file path.
     * @param encoding Encoding to use when converting Unicode chars.
     * @throws FileNotFoundException if file not found.
     * @throws IOException if failed to write file.
     */
    public static void append(String text, String file, String encoding)
            throws FileNotFoundException, IOException {
        try (OutputStreamWriter or = new OutputStreamWriter(
                new FileOutputStream(new File(file), true), encoding)) {
            or.write(text);
        }
    }

    /**
     * Write string to file.
     * Default encoding is used to convert Unicode chars in the file.
     *
     * @param text A String with text to save.
     * @param file File name or file path.
     * @throws IOException if failed to write file.
     */
    public static void writeFile(String text, String file) throws IOException {
        writeFile(text, file, UTF8);
    }

    /**
     * Write string to file.
     *
     * @param text A String with text to save.
     * @param file File name or file path.
     * @param encoding Encoding to use when saving file.
     * @throws IOException if failed to write file.
     */
    public static void writeFile(String text, String file, String encoding) throws IOException {
        try (OutputStreamWriter or = new OutputStreamWriter(
                new FileOutputStream(new File(file)), encoding)) {
            or.write(text);
        }
    }

    /**
     * Normalize file name. That means replace all characters that cannot be used in path with '_'.
     *
     * @param file File name.
     * @return Normalized file name.
     */
    public static String normalizeFileName(String file) {
        return file.replaceAll("\\\\|\\/|\\:|\\*|\\?", "_");
    }

    /**
     * Reads wiki file - a file where a wiki page was written.
     * Default encoding (set by {@link #setDefaultEncoding(String)}) will be used.
     * "wiki file" means:
     *   - file name is normalized (forbidden path symbols replaced with '_');
     *   - line endings are replaced to universal format ('\n');
     *   - last line ends with line ending.
     *
     * @param path File path (must be normalized).
     * @return String with wiki text.
     * @throws FileNotFoundException if file not found.
     * @throws IOException if failed to read file.
     */
    public static String readWikiFileFromPath(String path) throws IOException {
        return readWikiFileImpl(path, sDefaultEncoding);
    }

    /**
     * Reads wiki file - a file where a wiki page was written.
     * Default encoding (set by {@link #setDefaultEncoding(String)}) will be used.
     * "wiki file" means:
     *   - file name is normalized (forbidden path symbols replaced with '_');
     *   - line endings are replaced to universal format ('\n');
     *   - last line ends with line ending.
     *
     * @param folder Folder name or path with the required file.
     * @param file File name (or Wiki page title represented as file name).
     * @return String with wiki text.
     * @throws FileNotFoundException if file not found.
     * @throws IOException if failed to read file.
     */
    public static String readWikiFile(String folder, String file) throws IOException {
        return readWikiFile(folder, file, sDefaultEncoding);
    }

    /**
     * Reads wiki file - a file where a wiki page was written.
     * That means:
     *   - file name is normalized (forbidden path symbols replaced with '_');
     *   - line endings are replaced to universal format ('\n');
     *   - last line ends with line ending.
     *
     * @param folder Folder name or path with the required file.
     * @param file File name (or Wiki page title represented as file name).
     * @param encoding Encoding to decode string.
     * @return String with wiki text.
     * @throws FileNotFoundException if file not found.
     * @throws IOException if failed to read file.
     */
    public static String readWikiFile(String folder, String file, String encoding)
            throws FileNotFoundException, IOException {
        return readWikiFileImpl(folder + "\\" + normalizeFileName(file), encoding);
    }

    private static String readWikiFileImpl(String path, String encoding)
            throws FileNotFoundException, IOException {
        File fileGood = new File(path);
        logD("reading  file: " + fileGood.getPath());
        logD("absolute path: " +  fileGood.getAbsolutePath());
        StringBuilder text = new StringBuilder(100000);
        try (BufferedReader b = new BufferedReader(new InputStreamReader(
                new FileInputStream(fileGood), encoding))) {
            String line;
            while ((line = b.readLine()) != null) {                
                text.append(line);
                text.append("\n");
            }
        }
        return text.toString();
    }

    /**
     * Reads file contents to string. Default encoding will be used to read file.
     * Never raises exception.
     * Returns null if fails to read file.
     *
     * @param fileName File name (or file path).
     * @return String with file contents or <code>null</code>.
     */
    @Nullable
    public static String readFileSilently(String fileName) {
        return readFileSilently(fileName, null, sDefaultEncoding);
    }

    /**
     * Reads file contents to string. Default encoding will be used to read file.
     * Never raises exception.
     * Returns default value if fails to read file.
     *
     * @param fileName File name (or file path).
     * @param defaultVal Default value.
     * @return String with file contents or default value.
     */
    public static String readFileSilently(String fileName, String defaultVal) {
        return readFileSilently(fileName, defaultVal, sDefaultEncoding);
    }

    /**
     * Reads file contents to string.
     * Never raises exception.
     * Returns default value if failes to read file.
     *
     * @param fileName File name (or file path).
     * @param defaultVal Default value.
     * @param encoding Encoding with which to convert file to Unicode.
     * @return String with file contents or default value.
     */
    public static String readFileSilently(String fileName, String defaultVal, String encoding) {
        String text = defaultVal;
        try {
            text = readFile(fileName, encoding);            
        } catch (IOException e) {
            logE("Failed to read file: " + fileName);
            logE(e);
        }
        if (text == defaultVal && defaultVal != null) {
            logW("Using default value instead: " + defaultVal);
        }
        return text;
    }

    /**
     * Reads file to string.
     * Default encoding will be used to convert file to Unicode String.
     *
     * @param fileName File name (or file path).
     * @return String with file contents parsed to Unicode.
     */
    public static String readFile(String fileName)
            throws FileNotFoundException, IOException {
        return readFile(fileName, sDefaultEncoding);
    }

    /**
     * Reads file to string.
     *
     * @param fileName File name (or file path).
     * @param encoding Encoding to use. We advise to use {@link FileTools#UTF8}.
     * @return String with file contents parsed to Unicode using specified encoding.
     */
    public static String readFile(String fileName, String encoding)
            throws FileNotFoundException, IOException {
        String text = null;
        File file = new File(fileName);        
        StringBuilder sb = new StringBuilder(10000);
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(file), encoding)) {
            char [] buf = new char[1000];
            while (true) {
                int readCount = reader.read(buf);
                if (readCount < 0) {
                    break;
                }
                sb.append(buf, 0, readCount);
            }
            text = sb.toString();
        }
        return text;
    }
    
    /**
     * Reads file contents to list of strings.
     * It will use default encoding and will remove all empty lines.
     * 
     * @param fileName File name (or file path).
     * @return list of strings.
     * @throws FileNotFoundException if file not found.
     * @throws IOException if failed to read file.
     */
    public static List<String> readFileToList(String fileName)
            throws FileNotFoundException, IOException {
        return readFileToList(fileName, sDefaultEncoding, true);
    }

    /**
     * Reads file contents to list of strings.
     *
     * The same as {@link #readFileToList(String, String, boolean)}.
     * Default encoding will be used to read file.
     * Default encoding can be set by {@link FileTools#setDefaultEncoding(String)} method.
     *
     * @param fileName File name (or file path).
     * @param removeEmpty If <code>true</code>, empty lines will be removed from result list.
     * @return list of strings.
     * @throws FileNotFoundException if file not found.
     * @throws IOException if failed to read file.
     */
    public static List<String> readFileToList(String fileName, boolean removeEmpty)
            throws FileNotFoundException, IOException {
        return readFileToList(fileName, sDefaultEncoding, removeEmpty);
    }

    /**
     * Reads file contents to list of strings.
     *
     * @param fileName File name (or file path).
     * @param encoding Encoding to use. We advise to use {@link FileTools#UTF8}.
     * @param removeEmpty If <code>true</code>, empty lines will be removed from result list.
     * @return list of strings.
     * @throws FileNotFoundException if file not found.
     * @throws IOException if failed to read file.
     */
    public static List<String> readFileToList(String fileName, String encoding,
            boolean removeEmpty) throws FileNotFoundException, IOException {
        File file = new File(fileName);
        ArrayList<String> items = new ArrayList<String>(5000);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), encoding))) {
            String line = "";
            while ((line = br.readLine()) != null) {
                if (!(removeEmpty && line.trim().length() == 0)) {
                    items.add(line);
                }
            }            
        }
        return items;
    }
}
