package com.oveduumnakal.anothermixology;

import java.awt.Font;
import java.awt.image.BufferedImage;

import org.junit.Test;

import net.runelite.client.ui.FontManager;

import static org.junit.Assert.assertEquals;

/** Tests for {@link NumberFont}. */
public class NumberFontTest
{
	@Test
	public void matchedDigitsStandAsTallAsTheTextFonts()
	{
		var graphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
		var frc = graphics.getFontRenderContext();
		for (Font text : new Font[]{FontManager.getRunescapeSmallFont(), FontManager.getRunescapeFont()})
		{
			var matched = NumberFont.matching(graphics, text);
			int textHeight = text.createGlyphVector(frc, "0")
					.getPixelBounds(frc, 0, 0).height;
			int numberHeight = matched.createGlyphVector(frc, "0")
					.getPixelBounds(frc, 0, 0).height;
			assertEquals(text.getName(), textHeight, numberHeight, 1);
		}

		graphics.dispose();
	}
}
