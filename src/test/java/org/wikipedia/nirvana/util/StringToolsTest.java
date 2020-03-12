package org.wikipedia.nirvana.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Unit-tests for {@link StringTools}.
 *
 */
public class StringToolsTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testTrimDoubleQuoteIfFound() {
        Assert.assertEquals("some string", StringTools.trimDoubleQuoteIfFound("some string"));
        Assert.assertEquals("\"some string", StringTools.trimDoubleQuoteIfFound("\"some string"));
        Assert.assertEquals("some string\"", StringTools.trimDoubleQuoteIfFound("some string\""));
        Assert.assertEquals("some \"string", StringTools.trimDoubleQuoteIfFound("some \"string"));
        Assert.assertEquals("some \"str\"", StringTools.trimDoubleQuoteIfFound("some \"str\""));
        Assert.assertEquals("some string", StringTools.trimDoubleQuoteIfFound("\"some string\""));
        Assert.assertEquals("\"some str\"", StringTools.trimDoubleQuoteIfFound("\"\"some str\"\""));
    }

    @Test
    public void testHowMany() {
        Assert.assertEquals(0, StringTools.howMany("abc def ggg", 'x'));
        Assert.assertEquals(2, StringTools.howMany("abcbc", 'c'));
        Assert.assertEquals(3, StringTools.howMany("abc def ggg\n kk", ' '));
    }
    
    @Test
    public void testCountPaddedCharsAtLeft() {
        Assert.assertEquals(0, StringTools.countPaddedCharsAtLeft("abbbc bc", 'b'));
        Assert.assertEquals(3, StringTools.countPaddedCharsAtLeft("bbbc bc", 'b'));
        Assert.assertEquals(1, StringTools.countPaddedCharsAtLeft("a abbbc bc", 'a'));
    }

    @Test
    public void testTrimLeft() {
        Assert.assertEquals("", StringTools.trimLeft(""));
        Assert.assertEquals("", StringTools.trimLeft("   "));
        Assert.assertEquals("", StringTools.trimLeft("\t"));
        Assert.assertEquals("abc", StringTools.trimLeft("abc"));
        Assert.assertEquals("abc", StringTools.trimLeft("   abc"));
        Assert.assertEquals("abc    ", StringTools.trimLeft("   abc    "));
        Assert.assertEquals("abc", StringTools.trimLeft("\tabc"));
        Assert.assertEquals("abc", StringTools.trimLeft(" \t abc"));
        Assert.assertEquals("abc", StringTools.trimLeft(" \n abc"));
    }

    @Test
    public void testTrimRight() {
        Assert.assertEquals("", StringTools.trimRight(""));
        Assert.assertEquals("", StringTools.trimRight("   "));
        Assert.assertEquals("", StringTools.trimRight("\t"));
        Assert.assertEquals("abc", StringTools.trimRight("abc"));
        Assert.assertEquals("abc", StringTools.trimRight("abc   "));
        Assert.assertEquals("    abc", StringTools.trimRight("    abc    "));
        Assert.assertEquals("abc", StringTools.trimRight("abc\t"));
        Assert.assertEquals("abc", StringTools.trimRight("abc \t "));
        Assert.assertEquals("abc", StringTools.trimRight("abc \n "));
    }

    @Test
    public void testSplitBottom() {
        Assert.assertNull(StringTools.splitBottom(null, "\n", 1));
        Assert.assertArrayEquals(new String[] {}, StringTools.splitBottom("", "\n", 1));
        Assert.assertArrayEquals(new String[] {"abc"}, StringTools.splitBottom("abc", "\n", 1));
        Assert.assertArrayEquals(
                new String[] {"abc", "def"},
                StringTools.splitBottom("abc\ndef", "\n", 1));
        Assert.assertArrayEquals(
                new String[] {"abc\ndef", "jkl"},
                StringTools.splitBottom("abc\ndef\njkl", "\n", 1));
    }

    @Test
    public void testTrancateTo() {
        Assert.assertEquals("", StringTools.trancateTo("", 0));
        Assert.assertEquals("abc", StringTools.trancateTo("abc", 5));
        Assert.assertEquals("abcde", StringTools.trancateTo("abcde", 5));
        Assert.assertEquals("abcde", StringTools.trancateTo("abcdefgh", 5));
    }

    @Test
    public void testAddPrefixToList() {
        List<String> list = Arrays.asList(new String[] {"aa", "bb", "cc"});
        Assert.assertEquals(Arrays.asList(new String[] {"hoaa", "hobb", "hocc"}),
                StringTools.addPrefixToList(list, "ho"));
    }

    @Test
    public void isSpace() {
        Assert.assertTrue(StringTools.isSpace("\n"));
        Assert.assertTrue(StringTools.isSpace("\r"));
        Assert.assertTrue(StringTools.isSpace("\t"));
        Assert.assertTrue(StringTools.isSpace(" "));
        Assert.assertTrue(StringTools.isSpace("\r\n \t   \n"));
        Assert.assertTrue(StringTools.isSpace(""));

        Assert.assertFalse(StringTools.isSpace("\rA"));
        Assert.assertFalse(StringTools.isSpace("   ."));
        Assert.assertFalse(StringTools.isSpace("-"));
    }
}
