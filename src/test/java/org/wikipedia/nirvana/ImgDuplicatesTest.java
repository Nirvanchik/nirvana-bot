package org.wikipedia.nirvana;

import org.junit.Assert;
import org.junit.Test;
import org.wikipedia.nirvana.nirvanabot.ImgDuplicatesBot;

/**
 * @author kmorozov
 * Unit-tests for {@link ImgDuplicatesBot}.
 */

public class ImgDuplicatesTest {

    @Test
    public void testBot() {
        BasicBot bot = new ImgDuplicatesBot(BasicBot.FLAG_CONSOLE_LOG);
        int result = bot.run(new String[] {"configImgDuplicates.xml"});

        Assert.assertEquals(result, 0);
    }
}
