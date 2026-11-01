package com.oveduumnakal.anothermixology;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.util.HashMap;
import java.util.Map;

/**
 * The monospace fonts the rewards panel draws its numbers in: the times and percents of the reward rows, sized to the
 * row text by {@link #matching}, and the labels of the resin bars. Every digit is the same width, so numbers line up
 * from row to row.
 */
final class NumberFont
{
	static final Font BAR = new Font(Font.MONOSPACED, Font.PLAIN, 11);

	private static final Font BASE = new Font(Font.MONOSPACED, Font.PLAIN, 1);
	private static final int LARGEST_SIZE = 32;

	/** The monospace font already worked out for each text font. */
	private static final Map<Font, Font> MATCHED = new HashMap<>();

	private NumberFont()
	{
	}

	/**
	 * Returns the largest monospace font whose digits stand no taller than {@code font}'s, so numbers sit level with
	 * text drawn in {@code font}.
	 */
	static Font matching(Graphics2D graphics, Font font)
	{
		return MATCHED.computeIfAbsent(font, text ->
		{
			var frc = graphics.getFontRenderContext();
			int target = digitHeight(text, frc);
			var best = BASE.deriveFont(1f);
			for (int size = 2; size <= LARGEST_SIZE; size++)
			{
				var candidate = BASE.deriveFont((float) size);
				if (digitHeight(candidate, frc) > target)
					break;

				best = candidate;
			}

			return best;
		});
	}

	private static int digitHeight(Font font, FontRenderContext frc)
	{
		return font.createGlyphVector(frc, "0")
				.getPixelBounds(frc, 0, 0).height;
	}

	/** Runs {@code draw} with {@code font} set and text smoothed, then puts back the graphics' font and smoothing. */
	static void with(Graphics2D graphics, Font font, Runnable draw)
	{
		var oldFont = graphics.getFont();
		var oldSmoothing = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
		graphics.setFont(font);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		try
		{
			draw.run();
		}
		finally
		{
			graphics.setFont(oldFont);
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					oldSmoothing != null ? oldSmoothing : RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
		}
	}
}
