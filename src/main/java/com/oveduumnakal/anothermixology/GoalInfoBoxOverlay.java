package com.oveduumnakal.anothermixology;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.inject.Inject;

import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.SplitComponent;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.QuantityFormatter;

/**
 * The rewards panel: a row per tracked reward with its icon, estimated time to completion and progress, a row for all
 * of them together, then a progress bar per resin colour. Faint lines separate the sections. It shows only while the
 * potion orders do; the Show rewards panel setting hides it, and Show reward times drops the time column.
 */
class GoalInfoBoxOverlay extends OverlayPanel
{
	private static final int BORDER_SIZE = 3;
	private static final int VERTICAL_GAP = 2;
	private static final int ICON_AND_GOAL_GAP = 5;
	private static final int MIN_WIDTH = 250;
	private static final int COMPONENT_SPRITE_SIZE = 16;
	private static final Color PROGRESS_BAR_BACKGROUND_COLOR = new Color(61, 56, 49);
	private static final Color READY_COLOR = Color.GREEN;
	private static final Color PENDING_COLOR = Color.GRAY;

	private final AnotherMixologyPlugin plugin;
	private final AnotherMixologyConfig config;
	private final ItemManager itemManager;
	private final SpriteManager spriteManager;

	private final BufferedImage[] componentSprites = new BufferedImage[PotionComponent.ENTRIES.length];

	@Inject
	GoalInfoBoxOverlay(
			AnotherMixologyPlugin plugin,
			AnotherMixologyConfig config,
			ItemManager itemManager,
			SpriteManager spriteManager
	)
	{
		super(plugin);
		this.plugin = plugin;
		this.config = config;
		this.itemManager = itemManager;
		this.spriteManager = spriteManager;

		setPosition(OverlayPosition.TOP_RIGHT);
		setPriority(PRIORITY_MED);
		panelComponent.setPreferredSize(new Dimension(MIN_WIDTH, 0));
		panelComponent.setBorder(new Rectangle(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));
		panelComponent.setGap(new Point(0, VERTICAL_GAP));
	}

	/**
	 * Draws the reward rows, the all-rewards row when more than one reward is tracked, and a progress bar per resin
	 * colour.
	 */
	@Override
	public Dimension render(Graphics2D graphics)
	{
		var goal = plugin.getGoal();
		var estimates = plugin.rewardEstimates();
		if (!config.showRewardsPanel() || !goal.isMultiMode() || !plugin.isOrdersPanelVisible() || estimates == null)
			return null;

		graphics.setFont(FontManager.getRunescapeSmallFont());
		var metrics = graphics.getFontMetrics();
		int labelColumn = 0;
		for (var row : estimates.rows)
			labelColumn = Math.max(labelColumn, metrics.stringWidth(row.label));

		boolean showTimes = config.showRewardTimes();
		var numberMetrics = graphics.getFontMetrics(NumberFont.matching(graphics, graphics.getFont()));
		int width = panelWidth(labelColumn, numberMetrics.stringWidth(RewardRowComponent.WIDEST_TIME),
				numberMetrics.stringWidth(RewardRowComponent.WIDEST_PERCENT), showTimes);
		panelComponent.setPreferredSize(new Dimension(width, 0));

		long now = plugin.playedMillis();
		int rewards = estimates.rows.size() - 1;
		for (int i = 0; i < rewards; i++)
			panelComponent.getChildren().add(rowComponent(estimates, i, labelColumn, now, showTimes));

		if (rewards > 1)
		{
			panelComponent.getChildren().add(new SeparatorComponent());
			panelComponent.getChildren().add(rowComponent(estimates, rewards, labelColumn, now, showTimes));
		}

		panelComponent.getChildren().add(new SeparatorComponent());
		for (var component : PotionComponent.ENTRIES)
			createProgressBar(goal, component);

		return super.render(graphics);
	}

	/**
	 * Returns the panel width: the icon, name, time and percent columns with a gap between each, plus the border, and
	 * never less than {@code MIN_WIDTH}. Without times the time column and its gap are left out.
	 */
	static int panelWidth(int labelColumn, int timeWidth, int percentWidth, boolean showTimes)
	{
		int width = RewardRowComponent.ICON_COLUMN + labelColumn + RewardRowComponent.GAP + percentWidth
				+ BORDER_SIZE * 2;
		if (showTimes)
			width += timeWidth + RewardRowComponent.GAP;

		return Math.max(MIN_WIDTH, width);
	}

	/**
	 * Builds a reward row, its time counted down to {@code now}, in {@link PlayClock} milliseconds played. Without
	 * times the row has no time column.
	 */
	private RewardRowComponent rowComponent(RewardEstimates estimates, int index, int labelColumn, long now,
			boolean showTimes)
	{
		var row = estimates.rows.get(index);
		double seconds = estimates.seconds[index];
		String time;
		Color timeColor;
		if (!showTimes)
		{
			time = null;
			timeColor = null;
		}
		else if (row.ready())
		{
			time = "Ready";
			timeColor = READY_COLOR;
		}
		else if (Double.isNaN(seconds))
		{
			time = "...";
			timeColor = PENDING_COLOR;
		}
		else if (Double.isInfinite(seconds))
		{
			time = "--";
			timeColor = PENDING_COLOR;
		}
		else
		{
			time = RewardRows.formatTime(estimates.secondsLeft(index, now));
			timeColor = Color.WHITE;
		}

		var icon = row.item == null ? null : itemManager.getImage(row.item.itemId());
		return new RewardRowComponent(icon, row.label, time, timeColor, percent(row.progress),
				row.ready() ? READY_COLOR : Color.WHITE, labelColumn);
	}

	/** Formats a 0 to 1 progress as a whole percent, rounded down so 100% means done. */
	static String percent(double progress)
	{
		return (int) Math.floor(progress * 100) + "%";
	}

	private void createProgressBar(Goal goal, PotionComponent component)
	{
		var data = goal.getComponentData(component);
		var componentSprite = getComponentSprite(component);

		if (componentSprite == null)
			return;

		var imageComponent = new ImageComponent(componentSprite);
		var progressBarComponent = new OutlinedProgressBarComponent();

		progressBarComponent.setForegroundColor(plugin.effectiveComponentColor(component));
		progressBarComponent.setBackgroundColor(PROGRESS_BAR_BACKGROUND_COLOR);
		progressBarComponent.setValue(data.percentageToGoal * 100);
		progressBarComponent.setLeftLabel(QuantityFormatter.quantityToStackSize(data.currentAmount));
		progressBarComponent.setRightLabel(QuantityFormatter.quantityToStackSize(data.goalAmount));
		progressBarComponent.setCenterLabel(centerLabel(config.showProgress(), data));

		var progressBarSplit = SplitComponent.builder()
				.first(imageComponent)
				.second(progressBarComponent)
				.orientation(ComponentOrientation.HORIZONTAL)
				.gap(new Point(ICON_AND_GOAL_GAP, 0))
				.build();

		panelComponent.getChildren().add(progressBarSplit);
	}

	/** Returns the middle label of a resin bar: its percent, the resin still needed, or nothing. */
	static String centerLabel(ProgressLabel label, Goal.ComponentData data)
	{
		switch (label)
		{
			case PERCENT:
				return percent(data.percentageToGoal);
			case NUMBER:
				int remaining = Math.max(data.goalAmount - data.currentAmount, 0);
				return remaining > 0 ? "(" + QuantityFormatter.quantityToStackSize(remaining) + ")" : "";
			default:
				return "";
		}
	}

	private BufferedImage getComponentSprite(PotionComponent component)
	{
		var sprite = componentSprites[component.ordinal()];

		if (sprite == null)
			sprite = componentSprites[component.ordinal()] = createComponentSprite(component);

		return sprite;
	}

	/**
	 * Fetches a resin sprite, resized and centred on a square canvas.
	 *
	 * @return the sprite, or null if it is not loaded yet
	 */
	private BufferedImage createComponentSprite(PotionComponent component)
	{
		var sprite = spriteManager.getSprite(component.spriteId(), 0);
		if (sprite != null)
		{
			var resizedImage = ImageUtil.resizeImage(sprite, COMPONENT_SPRITE_SIZE, COMPONENT_SPRITE_SIZE, true);
			return ImageUtil.resizeCanvas(resizedImage, COMPONENT_SPRITE_SIZE, COMPONENT_SPRITE_SIZE);
		}

		return null;
	}
}
