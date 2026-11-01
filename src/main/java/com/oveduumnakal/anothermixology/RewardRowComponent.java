package com.oveduumnakal.anothermixology;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.TextComponent;

/**
 * One row of the rewards panel: the reward's icon and name, its progress percent on the right, and between them its
 * estimated time, centred in a slot every row shares so the times form one column.
 */
class RewardRowComponent implements LayoutableRenderableEntity
{
	static final int HEIGHT = 20;
	static final int ICON_COLUMN = 24;
	static final int GAP = 8;

	/** The widest time and percent; every row's time slot and percent column are this wide. */
	static final String WIDEST_TIME = "00h 00m 00s";
	static final String WIDEST_PERCENT = "100%";
	private static final int ICON_HEIGHT = 18;

	private final BufferedImage icon;
	private final String label;
	private final String time;
	private final Color timeColor;
	private final String percent;
	private final Color percentColor;
	private final int labelColumn;

	private Point preferredLocation = new Point();
	private Dimension preferredSize = new Dimension(0, HEIGHT);
	private final Rectangle bounds = new Rectangle();

	/**
	 * @param icon        the reward icon, or null for a row without one
	 * @param labelColumn width of the name column, shared by every row so the times line up
	 */
	RewardRowComponent(BufferedImage icon, String label, String time, Color timeColor, String percent,
			Color percentColor, int labelColumn)
	{
		this.icon = icon;
		this.label = label;
		this.time = time;
		this.timeColor = timeColor;
		this.percent = percent;
		this.percentColor = percentColor;
		this.labelColumn = labelColumn;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		int x = preferredLocation.x;
		int y = preferredLocation.y;
		int width = preferredSize.width;
		int baseline = OutlinedProgressBarComponent.centeredBaseline(graphics, y, HEIGHT);

		if (icon != null && icon.getHeight() > 0)
		{
			int iconWidth = icon.getWidth() * ICON_HEIGHT / icon.getHeight();
			graphics.drawImage(icon, x + (ICON_COLUMN - iconWidth) / 2, y + (HEIGHT - ICON_HEIGHT) / 2, iconWidth,
					ICON_HEIGHT, null);
		}

		int labelX = icon != null ? x + ICON_COLUMN : x;
		drawText(graphics, label, labelX, baseline, Color.WHITE);
		NumberFont.with(graphics, NumberFont.matching(graphics, graphics.getFont()), () ->
		{
			var metrics = graphics.getFontMetrics();
			int numberBaseline = OutlinedProgressBarComponent.centeredBaseline(graphics, y, HEIGHT);
			int labelEnd = x + ICON_COLUMN + labelColumn;
			int percentStart = x + width - metrics.stringWidth(WIDEST_PERCENT);
			int slotWidth = metrics.stringWidth(WIDEST_TIME);
			int slotX = labelEnd + (percentStart - labelEnd - slotWidth) / 2;
			int timeX = slotX + (slotWidth - metrics.stringWidth(time)) / 2;
			drawText(graphics, time, timeX, numberBaseline, timeColor);
			drawText(graphics, percent, x + width - metrics.stringWidth(percent), numberBaseline, percentColor);
		});

		bounds.setBounds(x, y, width, HEIGHT);
		return new Dimension(width, HEIGHT);
	}

	private static void drawText(Graphics2D graphics, String text, int x, int y, Color color)
	{
		var component = new TextComponent();
		component.setText(text);
		component.setColor(color);
		component.setPosition(new Point(x, y));
		component.render(graphics);
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
