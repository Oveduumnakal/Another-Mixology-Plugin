package com.oveduumnakal.anothermixology;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;

/** A one-pixel line across the panel, between sections. */
class SeparatorComponent implements LayoutableRenderableEntity
{
	/** Half as strong as the Potion Orders panel's divider, so it reads as very faint. */
	static final Color COLOR = new Color(255, 255, 255, 45);
	private static final int HEIGHT = 5;

	private Point preferredLocation = new Point();
	private Dimension preferredSize = new Dimension(0, HEIGHT);
	private final Rectangle bounds = new Rectangle();

	@Override
	public Dimension render(Graphics2D graphics)
	{
		int x = preferredLocation.x;
		int y = preferredLocation.y + HEIGHT / 2;
		graphics.setColor(COLOR);
		graphics.drawLine(x, y, x + preferredSize.width - 1, y);
		bounds.setBounds(preferredLocation.x, preferredLocation.y, preferredSize.width, HEIGHT);
		return new Dimension(preferredSize.width, HEIGHT);
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
