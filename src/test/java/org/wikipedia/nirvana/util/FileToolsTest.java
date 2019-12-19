/**
 *  @(#)FileToolsTest.java
 *  Copyright © 2019 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.util;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Tests for {@link FileTools}.
 *
 */
public class FileToolsTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        File outDirDefult = folder.newFolder("tmp");
        FileTools.setDefaultOut(outDirDefult.getPath());
        FileTools.setDefaultEncoding(FileTools.UTF8);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testDump() throws Exception {
        File outDir = folder.newFolder("out");
        FileTools.setDefaultOut(outDir.getPath());

        FileTools.dump("Go CS!", "abc.txt");

        File resultFile = new File(outDir, "abc.txt");
        Assert.assertTrue(resultFile.exists());
        String result = FileTools.readFile(resultFile.getPath());
        Assert.assertEquals("Go CS!", result);
    }

    @Test
    public void testDump_withFolder() throws Exception {
        File outDir = folder.newFolder("out");

        FileTools.dump("Go CS!", outDir.getPath(), "abc.txt");

        File resultFile = new File(outDir, "abc.txt");
        Assert.assertTrue(resultFile.exists());
        String result = FileTools.readFile(resultFile.getPath());
        Assert.assertEquals("Go CS!", result);
    }

    @Test
    public void testDump_whenFolderEndsWithSlash() throws Exception {
        File outDir = folder.newFolder("out");

        FileTools.dump("Go CS!", outDir.getPath() + File.separator, "abc.txt");

        File resultFile = new File(outDir, "abc.txt");
        Assert.assertTrue(resultFile.exists());
        String result = FileTools.readFile(resultFile.getPath());
        Assert.assertEquals("Go CS!", result);
    }

    @Test
    public void testDump_whenFolderDoesNotExist() throws Exception {
        File outDir = folder.newFolder("out");
        String dir = outDir.getPath() + File.separator + "a" + File.separator + "b";

        FileTools.dump("Go CS!", dir, "abc.txt");

        File resultFile = new File(dir, "abc.txt");
        Assert.assertTrue(resultFile.exists());

        String result = FileTools.readFile(resultFile.getPath());
        Assert.assertEquals("Go CS!", result);
    }

    @Test
    public void testDump_withNullFolder() throws Exception {

        FileTools.dump("Go CS!", null, "abc.txt");

        File resultFile = new File("abc.txt");
        Assert.assertTrue(resultFile.exists());

        String result = FileTools.readFile(resultFile.getPath());
        resultFile.delete();
        Assert.assertEquals("Go CS!", result);
    }

    @Test
    public void testAppend() throws Exception {
        File outDir = folder.newFolder("out");
        File resultFile = new File(outDir, "abc.txt");
        FileTools.writeFile("Text 1.", resultFile.getPath());
        
        FileTools.append("Text 2.", resultFile.getPath());
        
        String result = FileTools.readFile(resultFile.getPath());
        Assert.assertEquals("Text 1.Text 2.", result);
    }

    @Test
    public void testWriteFile() throws Exception {
        File outDir = folder.newFolder("out");
        File resultFile = new File(outDir, "abc.txt");
        FileTools.writeFile("Text 1.", resultFile.getPath());
        
        String result = FileTools.readFile(resultFile.getPath());
        Assert.assertEquals("Text 1.", result);
    }

    @Test
    public void testNormalizeFileName() {
        Assert.assertEquals("Bad file_name.", FileTools.normalizeFileName("Bad file\\name."));
        Assert.assertEquals("Bad file_name.", FileTools.normalizeFileName("Bad file?name."));
        Assert.assertEquals("Bad file_name.", FileTools.normalizeFileName("Bad file/name."));
        Assert.assertEquals("Bad file_name.", FileTools.normalizeFileName("Bad file:name."));
        Assert.assertEquals("Bad file_name.", FileTools.normalizeFileName("Bad file*name."));
        Assert.assertEquals("Very_bad_file_name_",
                FileTools.normalizeFileName("Very/bad\\file:name*"));
    }

    @Test
    public void testReadWikiFile() throws Exception {
        File outDir = folder.newFolder("out");
        File resultFile = new File(outDir, "abc_def.txt");

        FileTools.writeFile("Line 1.\nLine 2.\n", resultFile.getPath());

        String text = FileTools.readWikiFile(outDir.getPath(), "abc:def.txt");
        Assert.assertEquals("Line 1.\nLine 2.\n", text);
        
        FileTools.writeFile("Line 1.\r\nLine 2.\r\nLine 3.", resultFile.getPath());
        
        text = FileTools.readWikiFile(outDir.getPath(), "abc:def.txt");
        Assert.assertEquals("Line 1.\nLine 2.\nLine 3.\n", text);
    }

    @Test
    public void testReadFileSilently() throws Exception {
        File outDir = folder.newFolder("out");
        File resultFile = new File(outDir, "abc.txt");

        FileTools.writeFile("ABC", resultFile.getPath());

        String text = FileTools.readFileSilently(resultFile.getPath());
        Assert.assertEquals("ABC", text);
    }

    @Test
    public void testReadFileSilently_noFile() throws Exception {
        File outDir = folder.newFolder("out");
        File resultFile = new File(outDir, "abc.txt");

        String text = FileTools.readFileSilently(resultFile.getPath());
        Assert.assertNull(text);
    }

    @Test
    public void testReadFileSilently_withDefault() throws Exception {
        File outDir = folder.newFolder("out");
        File resultFile = new File(outDir, "abc.txt");

        FileTools.writeFile("ABC", resultFile.getPath());

        String text = FileTools.readFileSilently(resultFile.getPath(), "DEF");
        Assert.assertEquals("ABC", text);

        File resultFile2 = new File(outDir, "def.txt");
        text = FileTools.readFileSilently(resultFile2.getPath(), "DEF");
        Assert.assertEquals("DEF", text);
    }

    @Test
    public void testReadFile() throws Exception {
        File outDir = folder.newFolder("out");
        File resultFile = new File(outDir, "abc.txt");
        FileTools.writeFile("Text 1.", resultFile.getPath());
        
        String result = FileTools.readFile(resultFile.getPath());
        Assert.assertEquals("Text 1.", result);
    }

    @Test
    public void testReadFileToListString() throws Exception  {
        File outDir = folder.newFolder("out");
        File resultFile = new File(outDir, "abc.txt");
        FileTools.writeFile("Text 1.\nText 2.", resultFile.getPath());
        
        List<String> list = FileTools.readFileToList(resultFile.getPath());
        
        Assert.assertEquals(Arrays.asList(new String[] {"Text 1.", "Text 2."}), list);
        
        FileTools.writeFile("Text 1.\n\n\nText 2.", resultFile.getPath());
        
        list = FileTools.readFileToList(resultFile.getPath(), true);
        Assert.assertEquals(Arrays.asList(new String[] {"Text 1.", "Text 2."}), list);

        list = FileTools.readFileToList(resultFile.getPath(), false);
        Assert.assertEquals(Arrays.asList(new String[] {"Text 1.", "", "", "Text 2."}), list);
    }
}
