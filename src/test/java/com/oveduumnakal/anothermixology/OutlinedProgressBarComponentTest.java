package com.oveduumnakal.anothermixology;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import org.junit.Test;

import net.runelite.client.ui.FontManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Tests for {@link OutlinedProgressBarComponent}. */
public class OutlinedProgressBarComponentTest
{
	private static final int BAR_HEIGHT = 16;
	private static final Color BACKGROUND = new Color(61, 56, 49);

	@Test
	public void labelsAreVerticallyCentredInTheBar()
	{
		for (Font font : new Font[]{FontManager.getRunescapeSmallFont(), FontManager.getRunescapeFont()})
		{
			int[] rows = inkRows(font, "1920", "74%", "10K");
			int gapAbove = rows[0];
			int gapBelow = BAR_HEIGHT - 1 - rows[1];
			assertTrue("drew no label", rows[1] >= rows[0]);
			assertEquals("gap above vs below, whatever the panel font", gapAbove, gapBelow, 1);
		}
	}

	/**
	 * Renders an empty bar with the given labels and finds the first and last pixel rows the text touches.
	 *
	 * @param font   the panel font, which the bar's own number font replaces
	 * @param left   left label
	 * @param center center label
	 * @param right  right label
	 * @return first and last ink row, relative to the bar top
	 */
	private static int[] inkRows(Font font, String left, String center, String right)
	{
		int width = 129;
		BufferedImage image = new BufferedImage(width, BAR_HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setFont(font);

		OutlinedProgressBarComponent bar = new OutlinedProgressBarComponent();
		bar.setBackgroundColor(BACKGROUND);
		bar.setValue(0);
		bar.setLeftLabel(left);
		bar.setCenterLabel(center);
		bar.setRightLabel(right);
		bar.setPreferredLocation(new Point(0, 0));
		bar.setPreferredSize(new Dimension(width, BAR_HEIGHT));
		bar.render(graphics);
		graphics.dispose();

		int first = BAR_HEIGHT;
		int last = -1;
		for (int y = 0; y < BAR_HEIGHT; y++)
		{
			for (int x = 0; x < width; x++)
			{
				if (image.getRGB(x, y) != BACKGROUND.getRGB())
				{
					first = Math.min(first, y);
					last = Math.max(last, y);
				}
			}
		}

		return new int[]{first, last};
	}

	@Test
	public void darkTextOnLightFillsAndLightTextOnDarkOnes()
	{
		assertEquals(OutlinedProgressBarComponent.DARK_TEXT, OutlinedProgressBarComponent.textColorOn(Color.GREEN));
		assertEquals(OutlinedProgressBarComponent.DARK_TEXT,
				OutlinedProgressBarComponent.textColorOn(new Color(255, 200, 0)));
		assertEquals(OutlinedProgressBarComponent.LIGHT_TEXT,
				OutlinedProgressBarComponent.textColorOn(new Color(61, 56, 49)));
		assertEquals(OutlinedProgressBarComponent.LIGHT_TEXT,
				OutlinedProgressBarComponent.textColorOn(new Color(40, 40, 200)));
	}
}
