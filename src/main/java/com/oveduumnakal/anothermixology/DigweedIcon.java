package com.oveduumnakal.anothermixology;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

/**
 * Draws the digweed mark: a five-pointed leaf in the digweed colour with a dark edge, which reads better at a small
 * size than the digweed item image.
 */
final class DigweedIcon
{
	/** Each lobe's angle from straight up, in degrees, and its length as a share of the icon size. */
	private static final double[][] LOBES = {{0, 0.60}, {-50, 0.50}, {50, 0.50}, {-100, 0.34}, {100, 0.34}};

	private DigweedIcon()
	{
	}

	/** Returns a square leaf icon of the given size in the given colour. */
	static BufferedImage create(int size, Color color)
	{
		var image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		var graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		double baseX = size / 2.0;
		double baseY = size * 0.66;
		var leaf = new Area();
		for (double[] lobe : LOBES)
		{
			double length = size * lobe[1];
			double half = length * 0.36;
			var shape = new Path2D.Double();
			shape.moveTo(baseX, baseY);
			shape.curveTo(baseX - half * 1.4, baseY - length * 0.35, baseX - half * 0.6, baseY - length * 0.8, baseX,
					baseY - length);
			shape.curveTo(baseX + half * 0.6, baseY - length * 0.8, baseX + half * 1.4, baseY - length * 0.35, baseX,
					baseY);
			shape.closePath();
			var turn = AffineTransform.getRotateInstance(Math.toRadians(lobe[0]), baseX, baseY);
			leaf.add(new Area(turn.createTransformedShape(shape)));
		}

		var edge = new Color(color.getRed() / 3, color.getGreen() / 3, color.getBlue() / 3);
		float stroke = Math.max(1f, size / 24f);
		graphics.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		graphics.setColor(edge);
		graphics.draw(new Line2D.Double(baseX, baseY, baseX, size - stroke));
		graphics.setColor(color);
		graphics.fill(leaf);
		graphics.setColor(edge);
		graphics.draw(leaf);
		graphics.dispose();
		return image;
	}
}
