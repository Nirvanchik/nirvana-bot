/**
 *  @(#)ListToolsTest.java
 *  Copyright © 2021 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

import static org.junit.Assert.assertEquals;

import org.wikipedia.nirvana.tool.ListTools.Command;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * Unit tests for {@link ListTools}.
 *
 */
public class ListToolsTest {
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();
    
    private File leftFile;
    private File rightFile;
    private File outFile;

    @Before
    public void setUp() throws Exception {
        leftFile = tempDir.newFile("left.txt");
        rightFile = tempDir.newFile("right.txt");
        outFile = tempDir.newFile("out.txt");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnion() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter( leftFile ))) {
            bw.write("dog\ncat\n");
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter( rightFile ))) {
            bw.write("dog\nrat\ncow");
        }
        new ListTools().merge(Command.UNION, leftFile.getPath(), rightFile.getPath(),
                outFile.getPath());
    }

    @Test
    public void testIntersect() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter( leftFile ))) {
            bw.write("pig\ndog\ncat\ncow\n");
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter( rightFile ))) {
            bw.write("dog\nrat\ncow");
        }
        new ListTools().merge(Command.INTERSECTION, leftFile.getPath(), rightFile.getPath(),
                outFile.getPath());
        String result = new String(Files.readAllBytes(Paths.get(outFile.getPath())))
                .replace("\r\n", "\n");
        assertEquals("dog\ncow", result);
    }
    
    @Test
    public void testSubtract() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter( leftFile ))) {
            bw.write("pig\ndog\ncat\ncow\n");
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter( rightFile ))) {
            bw.write("dog\nrat\ncow");
        }
        new ListTools().merge(Command.SUBSTRACTION, leftFile.getPath(), rightFile.getPath(),
                outFile.getPath());
        String result = new String(Files.readAllBytes(Paths.get(outFile.getPath())))
                .replace("\r\n", "\n");
        assertEquals("pig\ncat", result);
    }
}
