package com.oveduumnakal.anothermixology;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.annotation.Nullable;
import javax.inject.Inject;

import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

/**
 * Tags each potion in the inventory with its recipe, as colour blocks or, with Use Letters on, as letters. While a
 * digweed has a target, outlines the digweed and the potion it should go on, following each item's shape, and marks
 * potions a digweed improved with a small digweed in the top left corner.
 */
public class InventoryPotionOverlay extends WidgetItemOverlay
{
	private static final int SMALL_DIGWEED_SIZE = 14;

	private final AnotherMixologyPlugin plugin;
	private final AnotherMixologyConfig config;
	private final ItemManager itemManager;

	/** The digweed leaf for the corner mark, drawn again when the digweed colour changes. */
	private BufferedImage smallDigweed;
	private Color smallDigweedColor;

	@Inject
	InventoryPotionOverlay(AnotherMixologyPlugin plugin, AnotherMixologyConfig config, ItemManager itemManager)
	{
		this.plugin = plugin;
		this.config = config;
		this.itemManager = itemManager;
		showOnInventory();
	}

	/**
	 * Draws the recipe on a potion's inventory icon, over a black drop shadow when it is drawn as letters, and the
	 * digweed outline on the digweed and its target potion.
	 */
	@Override
	public void renderItemOverlay(Graphics2D graphics2D, int itemId, WidgetItem widgetItem)
	{
		if (!plugin.isInLab())
			return;

		var bounds = widgetItem.getCanvasBounds();
		if (itemId == Digweed.ITEM_ID)
		{
			if (plugin.digweedTargetOrder() != null)
				drawOutline(graphics2D, itemId, widgetItem);

			return;
		}

		var potion = PotionType.fromItemId(itemId);

		if (potion == null)
			return;

		int index = widgetItem.getWidget().getIndex();
		if (index == plugin.digweedTargetInventoryIndex())
			drawOutline(graphics2D, itemId, widgetItem);

		if (plugin.improvedInventoryIndexes().contains(index))
		{
			var icon = smallDigweed();
			graphics2D.drawImage(icon, bounds.x, bounds.y, null);
		}

		if (config.inventoryPotionTagType() == InventoryPotionTagType.NONE)
			return;

		var x = bounds.x + 5;
		var y = bounds.y + 30;

		if (!config.useLetters())
		{
			drawBlocks(graphics2D, potion, x, y);
			return;
		}

		drawRecipe(graphics2D, potion, x + 1, y + 1, Color.BLACK);

		if (config.inventoryPotionTagType() == InventoryPotionTagType.COLORED)
		{
			drawRecipe(graphics2D, potion, x, y, null);
			return;
		}

		drawRecipe(graphics2D, potion, x, y, Color.WHITE);
	}

	private BufferedImage smallDigweed()
	{
		var color = config.digweedHighlightColor();
		if (smallDigweed == null || !color.equals(smallDigweedColor))
		{
			smallDigweed = DigweedIcon.create(SMALL_DIGWEED_SIZE, color);
			smallDigweedColor = color;
		}

		return smallDigweed;
	}

	/** Outlines the item's shape in the digweed colour. */
	private void drawOutline(Graphics2D graphics2D, int itemId, WidgetItem widgetItem)
	{
		var bounds = widgetItem.getCanvasBounds();
		var outline = itemManager.getItemOutline(itemId, widgetItem.getQuantity(), config.digweedHighlightColor());
		graphics2D.drawImage(outline, bounds.x, bounds.y, null);
	}

	private void drawRecipe(Graphics2D graphics2D, PotionType potion, int x, int y, @Nullable Color color)
	{
		graphics2D.setFont(FontManager.getRunescapeSmallFont());

		if (color != null)
		{
			graphics2D.setColor(color);
			graphics2D.drawString(potion.abbreviation(), x, y);
			return;
		}

		for (var component : potion.components())
		{
			graphics2D.setColor(plugin.effectiveComponentColor(component));
			graphics2D.drawString(String.valueOf(component.character()), x, y);
			x += graphics2D.getFontMetrics().charWidth(component.character());
		}
	}

	private void drawBlocks(Graphics2D graphics2D, PotionType potion, int x, int y)
	{
		final int side = 7;
		int top = y - side + 1;
		for (var component : potion.components())
		{
			graphics2D.setColor(plugin.effectiveComponentColor(component));
			graphics2D.fillRect(x, top, side, side);
			graphics2D.setColor(Color.BLACK);
			graphics2D.drawRect(x, top, side, side);
			x += side + 1;
		}
	}
}
