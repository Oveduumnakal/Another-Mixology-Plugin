package com.oveduumnakal.anothermixology;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.UnaryOperator;
import javax.inject.Inject;

import net.runelite.api.gameval.ItemID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;

/**
 * Stands in for the game's potion orders panel while it is hidden. It shows one row per order, with the station icon,
 * the name coloured by the paste each part stands for, and the recipe or "done!", then the paste left in the hopper
 * and, when enabled, the resin earned.
 */
public class OrdersPanelOverlay extends Overlay
{
	private static final int PADDING = 4;
	private static final int ICON_SIZE = 16;
	private static final int ROW_HEIGHT = 20;
	private static final int SMALL_ROW_HEIGHT = 16;
	private static final int AMOUNT_ROW_HEIGHT = 20;
	private static final int BLOCK_SIZE = 9;
	private static final int BLOCK_GAP = 2;

	/** How long the digweed icon stays on, then off, while it flashes. */
	private static final long DIGWEED_FLASH_MILLIS = 400;
	private static final int ICON_GAP = 4;
	private static final int NAME_RECIPE_GAP = 8;
	private static final int MIN_WIDTH = 200;
	private static final int DIVIDER_GAP = 4;
	private static final int AMOUNT_ICON_SIZE = 16;
	private static final Color DONE_COLOR = new Color(0, 255, 0);
	private static final Color DIVIDER_COLOR = new Color(255, 255, 255, 90);
	private static final int[] PASTE_ITEM_IDS = {ItemID.MM_MOX_PASTE, ItemID.MM_AGA_PASTE, ItemID.MM_LYE_PASTE};

	private final AnotherMixologyPlugin plugin;
	private final AnotherMixologyConfig config;
	private final SpriteManager spriteManager;
	private final ItemManager itemManager;

	/** Scaled station icons, keyed by the game's sprite, so each is resized once instead of every frame. */
	private final Map<BufferedImage, BufferedImage> scaledStationIcons =
			Collections.synchronizedMap(new WeakHashMap<>());

	/** Scaled paste and resin icons, keyed by the source image. */
	private final Map<BufferedImage, BufferedImage> scaledAmountIcons =
			Collections.synchronizedMap(new WeakHashMap<>());

	/** Item images whose loading is watched, so a copy scaled before the image loaded is dropped once it has. */
	private final Set<BufferedImage> watchedImages = Collections.newSetFromMap(
			Collections.synchronizedMap(new WeakHashMap<>()));

	/** The digweed leaf, drawn again when the digweed colour changes. */
	private BufferedImage digweedIcon;
	private Color digweedIconColor;

	@Inject
	OrdersPanelOverlay(AnotherMixologyPlugin plugin, AnotherMixologyConfig config, SpriteManager spriteManager,
			ItemManager itemManager)
	{
		this.itemManager = itemManager;
		this.plugin = plugin;
		this.config = config;
		this.spriteManager = spriteManager;
		setPosition(OverlayPosition.TOP_LEFT);
	}

	/** Draws the panel, or nothing when the game's own panel is showing or the player is outside the lab. */
	@Override
	public Dimension render(Graphics2D graphics)
	{
		var orders = plugin.potionOrders();

		if (!config.hideOrdersPanel() || !plugin.isInLab() || orders.isEmpty())
			return null;

		var nameFont = FontManager.getRunescapeFont();
		var smallFont = FontManager.getRunescapeSmallFont();
		int nameWidth = 0;
		for (var order : orders)
			nameWidth = Math.max(nameWidth, nameWidth(graphics, nameFont, order.potionType()));

		graphics.setFont(smallFont);
		int recipeWidth = graphics.getFontMetrics().stringWidth("(done!)");
		int digweedTarget = plugin.digweedTargetSlot();
		boolean digweedShown = digweedTarget >= 0 || orders.stream().anyMatch(PotionOrder::improved);
		int digweedWidth = digweedShown ? ICON_GAP + ICON_SIZE : 0;
		int width = Math.max(MIN_WIDTH,
				PADDING * 2 + ICON_SIZE + ICON_GAP + nameWidth + NAME_RECIPE_GAP + recipeWidth + digweedWidth);
		int rows = config.displayResin() ? 2 : 1;
		int height = PADDING * 2 + SMALL_ROW_HEIGHT + ROW_HEIGHT * orders.size() + DIVIDER_GAP * 2
				+ AMOUNT_ROW_HEIGHT * rows;

		graphics.setColor(ComponentConstants.STANDARD_BACKGROUND_COLOR);
		graphics.fillRect(0, 0, width, height);

		int y = PADDING + graphics.getFontMetrics().getAscent();
		drawText(graphics, "Potion Orders", PADDING, y, Color.WHITE);
		y += SMALL_ROW_HEIGHT - graphics.getFontMetrics().getAscent();

		var recommended = plugin.recommendedSlots();
		for (var order : orders)
		{
			boolean skipped = recommended != null && !order.fulfilled() && !recommended.contains(order.idx());
			drawOrder(graphics, order, skipped, y, nameFont, smallFont, nameWidth);
			drawDigweed(graphics, order, order.idx() == digweedTarget, y,
					PADDING + ICON_SIZE + ICON_GAP + nameWidth + NAME_RECIPE_GAP + recipeWidth + ICON_GAP);
			y += ROW_HEIGHT;
		}

		y += DIVIDER_GAP;
		graphics.setColor(DIVIDER_COLOR);
		graphics.drawLine(PADDING, y, width - PADDING, y);
		y += DIVIDER_GAP;

		graphics.setFont(smallFont);
		drawAmounts(graphics, y, width, true);
		if (config.displayResin())
			drawAmounts(graphics, y + AMOUNT_ROW_HEIGHT, width, false);

		return new Dimension(width, height);
	}

	private int nameWidth(Graphics2D graphics, Font font, PotionType potionType)
	{
		var metrics = graphics.getFontMetrics(font);
		int total = 0;
		for (var part : potionType.nameParts())
			total += metrics.stringWidth(part);

		return total;
	}

	private void drawOrder(Graphics2D graphics, PotionOrder order, boolean skipped, int top, Font nameFont,
			Font smallFont, int nameWidth)
	{
		var icon = stationIcon(order.idx());
		if (icon != null)
			drawIcon(graphics, icon, PADDING, top + (ROW_HEIGHT - icon.getHeight()) / 2);

		graphics.setFont(nameFont);
		int baseline = top + (ROW_HEIGHT + graphics.getFontMetrics().getAscent()) / 2 - 2;
		int x = PADDING + ICON_SIZE + ICON_GAP;
		var parts = order.potionType().nameParts();
		var components = order.potionType().components();
		for (int i = 0; i < parts.length; i++)
		{
			var color = skipped ? skipColor() : plugin.effectiveComponentColor(components[i]);
			drawText(graphics, parts[i], x, baseline, color);
			x += graphics.getFontMetrics().stringWidth(parts[i]);
		}

		graphics.setFont(smallFont);
		x = PADDING + ICON_SIZE + ICON_GAP + nameWidth + NAME_RECIPE_GAP;
		if (order.fulfilled())
			drawText(graphics, "(done!)", x, baseline, DONE_COLOR);
		else
			drawRecipe(graphics, order.potionType(), x, baseline, skipped);
	}

	/**
	 * Draws the digweed icon after an order's recipe: solid once a digweed improved the order's potion, flashing while
	 * the held digweed should go on it.
	 */
	private void drawDigweed(Graphics2D graphics, PotionOrder order, boolean target, int top, int x)
	{
		boolean flashOn = System.currentTimeMillis() / DIGWEED_FLASH_MILLIS % 2 == 0;
		if (!order.improved() && !(target && flashOn))
			return;

		var color = config.digweedHighlightColor();
		if (digweedIcon == null || !color.equals(digweedIconColor))
		{
			digweedIcon = DigweedIcon.create(ICON_SIZE, color);
			digweedIconColor = color;
		}

		drawIcon(graphics, digweedIcon, x, top + (ROW_HEIGHT - ICON_SIZE) / 2);
	}

	/** Returns the station icon for an order slot, scaled down to fit the row, or null before the game's is read. */
	private BufferedImage stationIcon(int idx)
	{
		int spriteId = plugin.orderSpriteId(idx);
		if (spriteId < 0)
			return null;

		var sprite = spriteManager.getSprite(spriteId, 0);
		if (sprite == null || sprite.getHeight() <= ICON_SIZE)
			return sprite;

		return scaled(scaledStationIcons, sprite,
				image -> ImageUtil.resizeImage(image, image.getWidth() * ICON_SIZE / image.getHeight(), ICON_SIZE,
						true));
	}

	/** Draws the mox, aga and lye amounts as an icon and the amount, each in its component's colour. */
	private void drawAmounts(Graphics2D graphics, int top, int width, boolean paste)
	{
		FontMetrics metrics = graphics.getFontMetrics();
		int cell = (width - PADDING * 2) / PotionComponent.ENTRIES.length;
		int baseline = top + AMOUNT_ROW_HEIGHT / 2 + (metrics.getAscent() - metrics.getDescent()) / 2;

		for (var component : PotionComponent.ENTRIES)
		{
			int amount = paste ? plugin.pasteAmount(component) : plugin.resinAmount(component);
			var text = String.valueOf(amount);
			var icon = paste ? pasteIcon(component) : resinIcon(component);
			int x = PADDING + cell * component.ordinal();
			if (icon != null)
			{
				drawIcon(graphics, icon, x, top + (AMOUNT_ROW_HEIGHT - icon.getHeight()) / 2);
				x += icon.getWidth() + ICON_GAP;
			}

			drawText(graphics, text, x, baseline, plugin.effectiveComponentColor(component));
		}
	}

	private BufferedImage pasteIcon(PotionComponent component)
	{
		return fit(itemManager.getImage(PASTE_ITEM_IDS[component.ordinal()]));
	}

	private BufferedImage resinIcon(PotionComponent component)
	{
		return fit(spriteManager.getSprite(component.spriteId(), 0));
	}

	/** Returns the image scaled down to fit the amount row, or null when it is not loaded yet. */
	private BufferedImage fit(BufferedImage image)
	{
		if (image == null || image.getHeight() <= AMOUNT_ICON_SIZE)
			return image;

		return scaled(scaledAmountIcons, image,
				source -> ImageUtil.resizeImage(source, AMOUNT_ICON_SIZE, AMOUNT_ICON_SIZE, true));
	}

	/**
	 * Returns the cached scaled copy of an image, scaling it on first use. An item image that is still loading is
	 * scaled blank, so its copy is dropped when it finishes loading and scaled again on the next frame.
	 */
	private BufferedImage scaled(Map<BufferedImage, BufferedImage> cache, BufferedImage source,
			UnaryOperator<BufferedImage> resize)
	{
		var cached = cache.get(source);
		if (cached != null)
			return cached;

		var result = resize.apply(source);
		cache.put(source, result);
		if (source instanceof AsyncBufferedImage && watchedImages.add(source))
			((AsyncBufferedImage) source).onLoaded(() -> cache.remove(source));

		return result;
	}

	private Color skipColor()
	{
		return config.skipColor();
	}

	/**
	 * Draws three colour blocks, or the three resin letters with Use Letters on. A skipped order is drawn all in the
	 * skip colour.
	 */
	private void drawRecipe(Graphics2D graphics, PotionType potionType, int x, int baseline, boolean skipped)
	{
		var metrics = graphics.getFontMetrics();

		for (var component : potionType.components())
		{
			var color = skipped ? skipColor() : plugin.effectiveComponentColor(component);

			if (!config.useLetters())
			{
				graphics.setColor(color);
				graphics.fillRect(x, baseline - BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
				x += BLOCK_SIZE + BLOCK_GAP;
			}
			else
			{
				var letter = String.valueOf(component.character());
				drawText(graphics, letter, x, baseline, color);
				x += metrics.stringWidth(letter);
			}
		}
	}

	private void drawText(Graphics2D graphics, String text, int x, int baseline, Color color)
	{
		graphics.setColor(color);
		graphics.drawString(text, x, baseline);
	}

	private void drawIcon(Graphics2D graphics, BufferedImage image, int x, int y)
	{
		graphics.drawImage(image, x, y, null);
	}
}
