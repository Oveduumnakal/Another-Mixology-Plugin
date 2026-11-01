package com.oveduumnakal.anothermixology;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

/** Outlines the lab objects the plugin wants the player to click next. */
public class AnotherMixologyOverlay extends Overlay
{
	private final AnotherMixologyPlugin plugin;
	private final ModelOutlineRenderer modelOutlineRenderer;

	@Inject
	AnotherMixologyOverlay(AnotherMixologyPlugin plugin, ModelOutlineRenderer modelOutlineRenderer)
	{
		this.plugin = plugin;
		this.modelOutlineRenderer = modelOutlineRenderer;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		for (var highlightedObject : plugin.highlightedObjects().values())
			modelOutlineRenderer.drawOutline(highlightedObject.object(), highlightedObject.outlineWidth(),
					highlightedObject.color(), highlightedObject.feather());

		return null;
	}
}
