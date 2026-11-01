package com.oveduumnakal.anothermixology;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;

import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.TextComponent;

/**
 * Progress bar with readable text. Mirrors the interface of {@code ProgressBarComponent}, but draws each label in
 * two parts: over the filled part of the bar and over the empty part, each in light or dark text, whichever stands out
 * more against that part's colour. A label the fill edge crosses is drawn partly dark and partly light.
 */
class OutlinedProgressBarComponent implements LayoutableRenderableEntity
{
	private static final int BAR_HEIGHT = 16;
	private static final int TEXT_PADDING = 4;

	private double value = 0.0;
	private String leftLabel = "";
	private String centerLabel = "";
	private String rightLabel = "";
	private Color foregroundColor = Color.GREEN;
	private Color backgroundColor = new Color(61, 56, 49);
	/** Light text, with a black outline. */
	static final Color LIGHT_TEXT = Color.WHITE;

	/** Dark text, drawn without an outline or shadow. */
	static final Color DARK_TEXT = new Color(16, 16, 16);

	/** Background luminance above which dark text stands out more than light (WCAG contrast crossover). */
	private static final double DARK_TEXT_LUMINANCE = 0.179;

	private Point preferredLocation = new Point();
	private Dimension preferredSize = new Dimension(ComponentConstants.STANDARD_WIDTH, BAR_HEIGHT);
	private final Rectangle bounds = new Rectangle();

	void setValue(double value)
	{
		this.value = Math.max(0, Math.min(100, value));
	}

	void setLeftLabel(String s)
	{
		this.leftLabel = s == null ? "" : s;
	}

	void setCenterLabel(String s)
	{
		this.centerLabel = s == null ? "" : s;
	}

	void setRightLabel(String s)
	{
		this.rightLabel = s == null ? "" : s;
	}

	void setForegroundColor(Color c)
	{
		this.foregroundColor = c;
	}

	void setBackgroundColor(Color c)
	{
		this.backgroundColor = c;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		int x = preferredLocation.x;
		int y = preferredLocation.y;
		int width = preferredSize.width;
		int height = preferredSize.height > 0 ? preferredSize.height : BAR_HEIGHT;

		graphics.setColor(backgroundColor);
		graphics.fillRect(x, y, width, height);

		int fgWidth = (int) (width * value / 100.0);
		graphics.setColor(foregroundColor);
		graphics.fillRect(x, y, fgWidth, height);

		var filled = new Rectangle(x, y, fgWidth, height);
		var empty = new Rectangle(x + fgWidth, y, width - fgWidth, height);
		NumberFont.with(graphics, NumberFont.BAR, () -> drawLabels(graphics, x, y, width, height, filled, empty));

		bounds.setBounds(x, y, width, height);
		return new Dimension(width, height);
	}

	private void drawLabels(Graphics2D graphics, int x, int y, int width, int height, Rectangle filled,
			Rectangle empty)
	{
		FontMetrics fm = graphics.getFontMetrics();
		int textY = centeredBaseline(graphics, y, height);
		if (!leftLabel.isEmpty())
			drawLabel(graphics, leftLabel, x + TEXT_PADDING, textY, filled, empty);

		if (!centerLabel.isEmpty())
		{
			int w = fm.stringWidth(centerLabel);
			drawLabel(graphics, centerLabel, x + (width - w) / 2, textY, filled, empty);
		}

		if (!rightLabel.isEmpty())
		{
			int w = fm.stringWidth(rightLabel);
			drawLabel(graphics, rightLabel, x + width - w - TEXT_PADDING, textY, filled, empty);
		}
	}

	/**
	 * Finds the baseline that centres the labels vertically in the bar. A font's ascent can include empty
	 * rows above the glyphs, so it centres on the pixel bounds of a digit (the labels are quantities) instead;
	 * every label shares that baseline. The one-pixel outline grows the text equally up and down.
	 *
	 * @param graphics the graphics, with the label font set
	 * @param y        top of the bar
	 * @param height   height of the bar
	 * @return the baseline y for the labels
	 */
	static int centeredBaseline(Graphics2D graphics, int y, int height)
	{
		FontRenderContext frc = graphics.getFontRenderContext();
		Rectangle digit = graphics.getFont()
			.createGlyphVector(frc, "0")
			.getPixelBounds(frc, 0, 0);
		return y + (height - digit.height) / 2 - digit.y;
	}

	/** Draws a label twice, clipped to the filled and the empty part, in the text colour that suits each. */
	private void drawLabel(Graphics2D graphics, String text, int x, int y, Rectangle filled, Rectangle empty)
	{
		var clip = graphics.getClip();
		graphics.clip(filled);
		drawText(graphics, text, x, y, textColorOn(foregroundColor));
		graphics.setClip(clip);
		graphics.clip(empty);
		drawText(graphics, text, x, y, textColorOn(backgroundColor));
		graphics.setClip(clip);
	}

	/** Draws light text with a black outline, and dark text plain, since a shadow would only blur it. */
	private static void drawText(Graphics2D graphics, String text, int x, int y, Color color)
	{
		if (color != LIGHT_TEXT)
		{
			graphics.setColor(color);
			graphics.drawString(text, x, y);
			return;
		}

		TextComponent t = new TextComponent();
		t.setText(text);
		t.setColor(color);
		t.setOutline(true);
		t.setPosition(new Point(x, y));
		t.render(graphics);
	}

	/** Returns light or dark text, whichever contrasts more with the background colour. */
	static Color textColorOn(Color background)
	{
		return luminance(background) > DARK_TEXT_LUMINANCE ? DARK_TEXT : LIGHT_TEXT;
	}

	/** Returns the colour's relative luminance, from 0 (black) to 1 (white), as WCAG defines it. */
	static double luminance(Color color)
	{
		return 0.2126 * linear(color.getRed()) + 0.7152 * linear(color.getGreen()) + 0.0722 * linear(color.getBlue());
	}

	private static double linear(int channel)
	{
		double c = channel / 255.0;
		return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
	}

	@Override
	public Rectangle getBounds()
	{
		return bounds;
	}

	@Override
	public void setPreferredLocation(Point preferredLocation)
	{
		this.preferredLocation = preferredLocation;
	}

	@Override
	public void setPreferredSize(Dimension preferredSize)
	{
		this.preferredSize = preferredSize;
	}
}
