/**
 *  @(#)ImageFinderTest.java 04.03.2017
 *  Copyright © 2017 Dmitry Trofimovich (KIN, Nirvanchik, DimaTrofimovich@gmail.com)
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

package org.wikipedia.nirvana.nirvanabot.imagefinder;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.wikipedia.nirvana.localization.Localizer;
import org.wikipedia.nirvana.localization.TestLocalizationManager;
import org.wikipedia.nirvana.util.FileTools;
import org.wikipedia.nirvana.wiki.NirvanaWiki;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

/**
 * Unit-tests for {@link ImageFinder}, and its descendants.
 * Contains tests for {@link ImageFinder}, {@link ImageFinderInCard}, {@link ImageFinderInBody}.
 */
public class ImageFinderTest {
    public static final String TEST_DATA_PATH = "src/test/resources/test_data/pages/";
    public static final String PICTURE_SEARCH_TAGS_RU =
            "image file,Фото,портрет,Изображение,Файл,File";
    public static final String PICTURE_SEARCH_TAGS_RU_EXTRA = 
            "image file,Фото,портрет,Изображение,Файл,File,флаг";
    public static final String PICTURE_SEARCH_TAGS_RU_CUSTOM =
            "Валюта:Изображение;Монета:Аверс,Реверс;image file,Фото,портрет,Изображение,Файл,File";

    private static final String ARTICLE_WITH_IMAGE_IN_CARD = "ru_image_in_card.txt";
    private static final String ARTICLE_WITH_IMAGE_IN_CARD_ONELINER =
            "ru_image_in_card_oneliner.txt";
    private static final String ARTICLE_WITH_IMAGE_IN_CARD_IMAGE_WIKICODE =
            "ru_image_in_card_image_wikicode.txt";
    private static final String ARTICLE_WITH_IMAGE_IN_CARD_WITH_CUSTOM_TAG =
            "ru_image_in_card_with_custom_tag.txt";
    private static final String ARTICLE_WITH_MANY_IMAGES_IN_CARD = "ru_many_images_in_card.txt";
    private static final String ARTICLE_WITH_IMAGE_IN_TEXT = "ru_image_in_text.txt";
    private static final String ARTICLE_WITH_MANY_IMAGES_IN_TEXT = "ru_many_images_in_text.txt";
    private static final String ARTICLE_WITH_IMAGE_IN_TEMPLATE_IN_CARD =
            "ru_image_in_template_in_card.txt";
    private static final String ARTICLE_WITH_IMAGE_IN_TEMPLATE_IN_CARD_MULTILINE =
            "ru_image_in_template_in_card_multiline.txt";
    @Mock
    private NirvanaWiki wiki;
    @Mock
    private NirvanaWiki commons;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        TestLocalizationManager.init(Localizer.DEFAULT_LOCALIZATION);
    }

    @After
    public void tearDown() throws Exception {
        TestLocalizationManager.reset();
    }

    @Test
    public void findInCard_noImage() throws IOException {
        String articleFile = "ru_no_images.txt";
        String article = FileTools.readFile(TEST_DATA_PATH + articleFile);

        ImageFinder finder = new ImageFinderInCard(wiki, commons, PICTURE_SEARCH_TAGS_RU);
        String result = finder.findImage(article);
        
        Assert.assertNull(result);
    }

    @Test
    public void findInText_noImage() throws IOException {
        String articleFile = "ru_no_images.txt";
        String article = FileTools.readFile(TEST_DATA_PATH + articleFile);
        
        ImageFinder finder = new ImageFinderInBody(wiki, commons);
        String result = finder.findImage(article);
        
        Assert.assertNull(result);
    }

    @Test
    public void findInCard_findsImageInCard() throws IOException {
        String article = FileTools.readFile(TEST_DATA_PATH + ARTICLE_WITH_IMAGE_IN_CARD);
        when(wiki.exists(eq("File:Julius_Caesar_(1914).jpg"))).thenReturn(true);

        ImageFinder finder = new ImageFinderInCard(wiki, commons, PICTURE_SEARCH_TAGS_RU);
        String result = finder.findImage(article);
        
        Assert.assertEquals("Julius_Caesar_(1914).jpg", result);
    }

    @Test
    public void findInText_findsImageInText() throws IOException {
        String article = FileTools.readFile(TEST_DATA_PATH + ARTICLE_WITH_IMAGE_IN_TEXT);
        when(wiki.exists(eq("File:BIPOLAR-Dave Tyo Headbang CBGB.jpg"))).thenReturn(true);

        ImageFinder finder = new ImageFinderInBody(wiki, commons);
        String result = finder.findImage(article);
        
        Assert.assertEquals("BIPOLAR-Dave Tyo Headbang CBGB.jpg", result);
    }

    @Test
    public void findInCard_returnsNullIfImageDoesNotExist() throws IOException {
        String article = FileTools.readFile(TEST_DATA_PATH + ARTICLE_WITH_IMAGE_IN_CARD);
        when(wiki.exists(eq("File:Julius_Caesar_(1914).jpg"))).thenReturn(false);
        when(commons.exists(eq("File:Julius_Caesar_(1914).jpg"))).thenReturn(false);

        ImageFinder finder = new ImageFinderInCard(wiki, commons, PICTURE_SEARCH_TAGS_RU);
        String result = finder.findImage(article);
        
        Assert.assertNull(result);
    }
    
    @Test
    public void findInCard_findsNextImage() throws IOException {
        String article = FileTools.readFile(TEST_DATA_PATH + ARTICLE_WITH_MANY_IMAGES_IN_CARD);
        when(wiki.exists(eq("File:Boris Yeltsin in 1994.PNG"))).thenReturn(false);
        when(commons.exists(eq("File:Boris Yeltsin in 1994.PNG"))).thenReturn(false);
        when(wiki.exists(eq("File:Standard of the President of the Russian Federation.svg")))
                .thenReturn(true);

        ImageFinder finder = new ImageFinderInCard(wiki, commons, PICTURE_SEARCH_TAGS_RU_EXTRA);
        String result = finder.findImage(article);
        
        Assert.assertEquals("Standard of the President of the Russian Federation.svg", result);
    }

    @Test
    public void findInCard_findsImageInCard_imageFromWikicode() throws IOException {
        String article = FileTools.readFile(TEST_DATA_PATH +
                ARTICLE_WITH_IMAGE_IN_CARD_IMAGE_WIKICODE);
        when(wiki.exists(eq("File:Julius_Caesar_(1914).jpg"))).thenReturn(true);

        ImageFinder finder = new ImageFinderInCard(wiki, commons, PICTURE_SEARCH_TAGS_RU);
        String result = finder.findImage(article);
        
        Assert.assertEquals("Julius_Caesar_(1914).jpg", result);
    }

    @Test
    public void findInCard_findsImageInCard_templateOneLiner() throws IOException {
        String article = FileTools.readFile(TEST_DATA_PATH +
                ARTICLE_WITH_IMAGE_IN_CARD_ONELINER);
        when(wiki.exists(eq("File:Jirina-sedlackova.jpg"))).thenReturn(true);

        ImageFinder finder = new ImageFinderInCard(wiki, commons, PICTURE_SEARCH_TAGS_RU);
        String result = finder.findImage(article);
        
        Assert.assertEquals("Jirina-sedlackova.jpg", result);
    }

    @Test
    public void findInCard_findsImageByCustomTag() throws IOException {
        String article = FileTools.readFile(TEST_DATA_PATH +
                ARTICLE_WITH_IMAGE_IN_CARD_WITH_CUSTOM_TAG);
        when(wiki.exists(eq("File:АР Крим аверс.jpg"))).thenReturn(true);

        ImageFinder finder = new ImageFinderInCard(wiki, commons, PICTURE_SEARCH_TAGS_RU_CUSTOM);
        String result = finder.findImage(article);
        
        Assert.assertEquals("АР Крим аверс.jpg", result);
    }

    @Test
    public void findInText_returnsNullIfImageDoesNotExist() throws IOException {
        String article = FileTools.readFile(TEST_DATA_PATH + ARTICLE_WITH_IMAGE_IN_TEXT);
        when(wiki.exists(eq("File:BIPOLAR-Dave Tyo Headbang CBGB.jpg"))).thenReturn(false);
        when(commons.exists(eq("File:BIPOLAR-Dave Tyo Headbang CBGB.jpg"))).thenReturn(false);

        ImageFinder finder = new ImageFinderInBody(wiki, commons);
        String result = finder.findImage(article);
        
        Assert.assertNull(result);
    }

    @Test
    public void findInText_findsNextImage() throws IOException {
        String article = FileTools.readFile(TEST_DATA_PATH + ARTICLE_WITH_MANY_IMAGES_IN_TEXT);
        when(wiki.exists(eq("File:RMK05.jpg"))).thenReturn(false);
        when(commons.exists(eq("File:RMK05.jpg"))).thenReturn(false);
        when(wiki.exists(eq("File:Mormuska.JPG"))).thenReturn(true);

        ImageFinder finder = new ImageFinderInBody(wiki, commons);
        String result = finder.findImage(article);
        
        Assert.assertEquals("Mormuska.JPG", result);
    }
    
    @Test
    public void findInCard_checksMainWiki() throws IOException {
        String article = FileTools.readFile(TEST_DATA_PATH + ARTICLE_WITH_IMAGE_IN_CARD);
        when(wiki.exists(eq("File:Julius_Caesar_(1914).jpg"))).thenReturn(false);
        when(commons.exists(eq("File:Julius_Caesar_(1914).jpg"))).thenReturn(false);

        ImageFinder finder = new ImageFinderInCard(wiki, commons, PICTURE_SEARCH_TAGS_RU);
        finder.findImage(article);
        
        Mockito.verify(wiki).exists(eq("File:Julius_Caesar_(1914).jpg"));
    }

    @Test
    public void findInText_checksMainWiki() throws IOException {
        String article = FileTools.readFile(TEST_DATA_PATH + ARTICLE_WITH_IMAGE_IN_TEXT);
        when(wiki.exists(eq("File:BIPOLAR-Dave Tyo Headbang CBGB.jpg"))).thenReturn(false);
        when(commons.exists(eq("File:BIPOLAR-Dave Tyo Headbang CBGB.jpg"))).thenReturn(false);

        ImageFinder finder = new ImageFinderInBody(wiki, commons);
        finder.findImage(article);
        
        Mockito.verify(wiki).exists(eq("File:BIPOLAR-Dave Tyo Headbang CBGB.jpg"));
    }
    
    @Test
    public void findInCard_checksCommons() throws IOException {
        String article = FileTools.readFile(TEST_DATA_PATH + ARTICLE_WITH_IMAGE_IN_CARD);
        when(wiki.exists(eq("File:Julius_Caesar_(1914).jpg"))).thenReturn(false);
        when(commons.exists(eq("File:Julius_Caesar_(1914).jpg"))).thenReturn(false);

        ImageFinder finder = new ImageFinderInCard(wiki, commons, PICTURE_SEARCH_TAGS_RU);
        finder.findImage(article);
        
        Mockito.verify(commons).exists(eq("File:Julius_Caesar_(1914).jpg"));
    }
    
    @Test
    public void findInText_checksCommons() throws IOException {
        String article = FileTools.readFile(TEST_DATA_PATH + ARTICLE_WITH_IMAGE_IN_TEXT);
        when(wiki.exists(eq("File:BIPOLAR-Dave Tyo Headbang CBGB.jpg"))).thenReturn(false);
        when(commons.exists(eq("File:BIPOLAR-Dave Tyo Headbang CBGB.jpg"))).thenReturn(false);

        ImageFinder finder = new ImageFinderInBody(wiki, commons);
        finder.findImage(article);
        
        Mockito.verify(commons).exists(eq("File:BIPOLAR-Dave Tyo Headbang CBGB.jpg"));
    }
    
    @Test
    public void findInCard_findsImageInTemplate() throws IOException {
        String article = FileTools.readFile(TEST_DATA_PATH +
                ARTICLE_WITH_IMAGE_IN_TEMPLATE_IN_CARD);
        when(wiki.exists(
                eq("File:Vladimir Putin and Aleksandr Lukashenko, BRICS summit 2015 04.jpg")))
                        .thenReturn(true);

        ImageFinder finder = new ImageFinderInCard(wiki, commons, PICTURE_SEARCH_TAGS_RU);
        String result = finder.findImage(article);
        
        Assert.assertEquals("Vladimir Putin and Aleksandr Lukashenko, BRICS summit 2015 04.jpg",
                result);
    }
    
    @Ignore
    @Test
    public void findInCard_findsImageInTemplateMultiline() throws IOException {
        // FIXME: The test fails because the code is raw and immature - need to redo
        // I haven't checked the code on multiline image templates so it doesn't support them. 
        String article = FileTools.readFile(TEST_DATA_PATH +
                ARTICLE_WITH_IMAGE_IN_TEMPLATE_IN_CARD_MULTILINE);
        when(wiki.exists(eq("File:Beatles ad 1965.JPG"))).thenReturn(true);

        ImageFinder finder = new ImageFinderInCard(wiki, commons, PICTURE_SEARCH_TAGS_RU);
        String result = finder.findImage(article);
        
        Assert.assertEquals("Beatles ad 1965.JPG", result);
    }
}
