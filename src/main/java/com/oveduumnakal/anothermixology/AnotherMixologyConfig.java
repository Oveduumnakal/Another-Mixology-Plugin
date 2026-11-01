package com.oveduumnakal.anothermixology;

import java.awt.Color;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Notification;
import net.runelite.client.config.Range;

import static com.oveduumnakal.anothermixology.AnotherMixologyConfig.CONFIG_GROUP;

/** The plugin's settings, grouped into collapsible sections. */
@ConfigGroup(CONFIG_GROUP)
public interface AnotherMixologyConfig extends Config
{
	String CONFIG_GROUP = "anothermixology";

	@ConfigSection(
			name = "Colors",
			description = "Recipe style and the colours used for pastes, skipped orders and highlights",
			position = 11
	)
	String COLORS = "Colors";

	@ConfigItem(
			section = COLORS,
			keyName = "useLetters",
			name = "Use Letters",
			description = "Show recipes as M/A/L letters instead of coloured blocks",
			position = 1
	)
	default boolean useLetters()
	{
		return false;
	}

	@ConfigItem(
			section = COLORS,
			keyName = "colorblindMode",
			name = "Colorblind mode",
			description = "Preset Mox, Aga and Lye colours that stay distinct for the chosen colour blindness.<br>"
					+ "Off uses the Mox, Aga and Lye colours below",
			position = 2
	)
	default ColorblindMode colorblindMode()
	{
		return ColorblindMode.OFF;
	}

	@ConfigItem(
			section = COLORS,
			keyName = "stationHighlightColor",
			name = "Station color",
			description = "Configures the default station highlight color",
			position = 3
	)
	default Color stationHighlightColor()
	{
		return Color.MAGENTA;
	}

	@ConfigItem(
			section = COLORS,
			keyName = "stationQuickActionHighlightColor",
			name = "Quick-action color",
			description = "Configures the station quick-action highlight color",
			position = 4
	)
	default Color stationQuickActionHighlightColor()
	{
		return Color.GREEN;
	}

	@ConfigItem(
			section = COLORS,
			keyName = "digweedHighlightColor",
			name = "DigWeed color",
			description = "Configures the digweed highlight color",
			position = 5
	)
	default Color digweedHighlightColor()
	{
		return Color.GREEN;
	}

	@ConfigItem(
			section = COLORS,
			keyName = "accessibilitySkipColor",
			name = "Skip color",
			description = "Colour for orders the strategy says you should skip (reroll)",
			position = 6
	)
	default Color skipColor()
	{
		return new Color(0x4A4A4A);
	}

	@ConfigItem(
			section = COLORS,
			keyName = "accessibilityMoxColor",
			name = "Mox color",
			description = "Colour for Mox (recipe, potion names, lever, progress bar) while Colorblind mode is Off",
			position = 7
	)
	default Color moxColor()
	{
		return new Color(0x3984FF);
	}

	@ConfigItem(
			section = COLORS,
			keyName = "accessibilityAgaColor",
			name = "Aga color",
			description = "Colour for Aga (recipe, potion names, lever, progress bar) while Colorblind mode is Off",
			position = 8
	)
	default Color agaColor()
	{
		return new Color(0x6BFF76);
	}

	@ConfigItem(
			section = COLORS,
			keyName = "accessibilityLyeColor",
			name = "Lye color",
			description = "Colour for Lye (recipe, potion names, lever, progress bar) while Colorblind mode is Off",
			position = 9
	)
	default Color lyeColor()
	{
		return new Color(0xFF0000);
	}

	@ConfigSection(
			name = "Highlights",
			description = "Highlighting related configuration",
			position = 12
	)
	String HIGHLIGHTS = "Highlights";

	@ConfigItem(
			keyName = "recommendationStrategy",
			name = "Strategy",
			description = "Expected value: the delivery with the most useful resin per unit of time, favouring the "
					+ "colours furthest behind. Counts the walk to the conveyor belt and each station's time.<br>"
					+ "Bottleneck: favours the single colour furthest behind.<br>"
					+ "Adaptive: the original three-mode policy.<br>"
					+ "Greedy: always brew all 3.",
			position = 0
	)
	default RecommendationStrategy recommendationStrategy()
	{
		return RecommendationStrategy.EXPECTED_VALUE;
	}

	@ConfigItem(
			keyName = "inventoryPotionTags",
			name = "Inventory Potion Tags",
			description = "How potions should be tagged in the inventory",
			position = 1
	)
	default InventoryPotionTagType inventoryPotionTagType()
	{
		return InventoryPotionTagType.COLORED;
	}

	@ConfigItem(
			keyName = "potionOrderSorting",
			name = "Order sorting",
			description = "Determines how potion orders are sorted in the interface",
			position = 1
	)
	default PotionOrderSorting potionOrderSorting()
	{
		return PotionOrderSorting.SHORTEST_PATH;
	}

	@ConfigItem(
			keyName = "hideOrdersPanel",
			name = "Hide orders panel",
			description = "Hides the game's potion orders list and shows the orders, recipes and recommendations in a "
					+ "plugin overlay instead",
			position = 1
	)
	default boolean hideOrdersPanel()
	{
		return true;
	}

	@ConfigItem(
			section = HIGHLIGHTS,
			keyName = "highlightLevers",
			name = "Levers",
			description = "Highlight levers",
			position = 0
	)
	default boolean highlightLevers()
	{
		return true;
	}

	@ConfigItem(
			section = HIGHLIGHTS,
			keyName = "highlightNextLeverOnly",
			name = "Next lever only",
			description = "Only highlight the next lever to pull for the order you're mixing, instead of all three "
					+ "levers",
			position = 1
	)
	default boolean highlightNextLeverOnly()
	{
		return true;
	}

	@ConfigItem(
			section = HIGHLIGHTS,
			keyName = "highlightMixingVessel",
			name = "Mixing vessel",
			description = "Highlight the mixing vessel green when it holds three pastes and is ready to mix",
			position = 2
	)
	default boolean highlightMixingVessel()
	{
		return true;
	}

	@ConfigItem(
			section = HIGHLIGHTS,
			keyName = "highlightConveyorBelt",
			name = "Conveyor belt",
			description = "Highlight the conveyor belt green when your finished potions match every order you're "
					+ "making",
			position = 3
	)
	default boolean highlightConveyorBelt()
	{
		return true;
	}

	@ConfigItem(
			section = HIGHLIGHTS,
			keyName = "highlightStations",
			name = "Stations",
			description = "Toggles alchemical station highlighting on or off",
			position = 4
	)
	default boolean highlightStations()
	{
		return true;
	}

	@ConfigItem(
			keyName = "waitForFullOrder",
			name = "Wait for full order",
			description = "Don't highlight any station until every order you're making has been mixed and is in your "
					+ "inventory",
			position = 2
	)
	default boolean waitForFullOrder()
	{
		return true;
	}

	@ConfigItem(
			section = HIGHLIGHTS,
			keyName = "highlightQuickActionEvents",
			name = "Quick-action events",
			description = "Toggles station quick-action events highlighting on or off",
			position = 5
	)
	default boolean highlightQuickActionEvents()
	{
		return true;
	}

	@ConfigItem(
			keyName = "displayResin",
			name = "Display resin amount",
			description = "Display total resin amounts",
			position = 2
	)
	default boolean displayResin()
	{
		return true;
	}

	@ConfigItem(
			keyName = "notifyDigweed",
			name = "Notify DigWeed",
			description = "Toggles digweed notifications on or off",
			position = 5
	)
	default Notification notifyDigWeed()
	{
		return Notification.ON;
	}

	@ConfigItem(
			section = HIGHLIGHTS,
			keyName = "highlightDigweed",
			name = "DigWeed",
			description = "Toggles digweed highlighting on or off",
			position = 6
	)
	default boolean highlightDigWeed()
	{
		return true;
	}

	@ConfigItem(
			section = HIGHLIGHTS,
			keyName = "highlightDigweedTarget",
			name = "Digweed target",
			description = "While you hold a digweed, outlines it and the potion to add it to, and flashes a digweed "
					+ "on that order.<br>It picks the recommended potion where doubling adds the most useful resin.",
			position = 7
	)
	default boolean highlightDigweedTarget()
	{
		return true;
	}

	@ConfigItem(
			keyName = "soundEffectAlembic",
			name = "Fix Alembic quick-action sound effect",
			description = "Fixes the Alembic quick-action sound effect to play at the correct time",
			position = 8
	)
	default boolean soundEffectAlembic()
	{
		return true;
	}

	@ConfigItem(
			section = HIGHLIGHTS,
			keyName = "highlightBorderWidth",
			name = "Border width",
			description = "Configures the border width of the object highlights",
			position = 8
	)
	default int highlightBorderWidth()
	{
		return 2;
	}

	@ConfigItem(
			section = HIGHLIGHTS,
			keyName = "highlightFeather",
			name = "Feather",
			description = "Configures the amount of 'feathering' to be applied to the object highlights",
			position = 9
	)
	default int highlightFeather()
	{
		return 1;
	}

	@ConfigSection(
			name = "Reward Tracking",
			description = "Tick the rewards you're working toward.<br>"
					+ "The overlay shows progress toward the combined cost; the notification fires when you meet "
					+ "it.<br>"
					+ "Rewards are listed in the order you tick them; right-click the rewards panel to move "
					+ "one up or down.",
			position = 13
	)
	String REWARD_TRACKING = "RewardTracking";

	@ConfigItem(
			section = REWARD_TRACKING,
			keyName = "thresholdNotification",
			name = "Threshold notification",
			description = "Fires once when your resin totals meet the combined cost of every selected reward",
			position = 1
	)
	default Notification thresholdNotification()
	{
		return Notification.OFF;
	}

	@ConfigItem(
			section = REWARD_TRACKING,
			keyName = "trackPrescriptionGoggles",
			name = "Prescription Goggles",
			description = "Include Prescription Goggles in the tracked totals",
			position = 3
	)
	default boolean trackPrescriptionGoggles()
	{
		return false;
	}

	@ConfigItem(
			section = REWARD_TRACKING,
			keyName = "trackAlchemistLabcoat",
			name = "Alchemist Labcoat",
			description = "Include Alchemist Labcoat in the tracked totals",
			position = 4
	)
	default boolean trackAlchemistLabcoat()
	{
		return false;
	}

	@ConfigItem(
			section = REWARD_TRACKING,
			keyName = "trackAlchemistPants",
			name = "Alchemist Pants",
			description = "Include Alchemist Pants in the tracked totals",
			position = 5
	)
	default boolean trackAlchemistPants()
	{
		return false;
	}

	@ConfigItem(
			section = REWARD_TRACKING,
			keyName = "trackAlchemistGloves",
			name = "Alchemist Gloves",
			description = "Include Alchemist Gloves in the tracked totals",
			position = 6
	)
	default boolean trackAlchemistGloves()
	{
		return false;
	}

	@ConfigItem(
			section = REWARD_TRACKING,
			keyName = "trackReagentPouch",
			name = "Reagent Pouch",
			description = "Include Reagent Pouch in the tracked totals",
			position = 7
	)
	default boolean trackReagentPouch()
	{
		return false;
	}

	@ConfigItem(
			section = REWARD_TRACKING,
			keyName = "trackPotionStorage",
			name = "Potion Storage",
			description = "Include Potion Storage in the tracked totals",
			position = 8
	)
	default boolean trackPotionStorage()
	{
		return false;
	}

	@ConfigItem(
			section = REWARD_TRACKING,
			keyName = "trackAlchemistsAmulet",
			name = "Alchemist's Amulet",
			description = "Include Alchemist's Amulet in the tracked totals",
			position = 9
	)
	default boolean trackAlchemistsAmulet()
	{
		return false;
	}

	@ConfigItem(
			section = REWARD_TRACKING,
			keyName = "chuggingBarrelQuantity",
			name = "Chugging Barrel qty",
			description = "How many Chugging Barrels to include in the tracked totals; 0 leaves them out",
			position = 10
	)
	@Range(min = 0, max = 100000)
	default int chuggingBarrelQuantity()
	{
		return 0;
	}

	@ConfigItem(
			section = REWARD_TRACKING,
			keyName = "apprenticePackQuantity",
			name = "Apprentice Pack qty",
			description = "How many Apprentice Potion Packs to include in the tracked totals; 0 leaves them out",
			position = 11
	)
	@Range(min = 0, max = 100000)
	default int apprenticePackQuantity()
	{
		return 0;
	}

	@ConfigItem(
			section = REWARD_TRACKING,
			keyName = "adeptPackQuantity",
			name = "Adept Pack qty",
			description = "How many Adept Potion Packs to include in the tracked totals; 0 leaves them out",
			position = 12
	)
	@Range(min = 0, max = 100000)
	default int adeptPackQuantity()
	{
		return 0;
	}

	@ConfigItem(
			section = REWARD_TRACKING,
			keyName = "expertPackQuantity",
			name = "Expert Pack qty",
			description = "How many Expert Potion Packs to include in the tracked totals; 0 leaves them out",
			position = 13
	)
	@Range(min = 0, max = 100000)
	default int expertPackQuantity()
	{
		return 0;
	}

	@ConfigItem(
			section = REWARD_TRACKING,
			keyName = "aldariumQuantity",
			name = "Aldarium qty",
			description = "How many Aldarium to include in the tracked totals; 0 leaves them out",
			position = 14
	)
	@Range(min = 0, max = 100000)
	default int aldariumQuantity()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "showProgress",
			name = "Reward Progress",
			description = "What the middle of each resin bar in the rewards panel shows.<br>"
					+ "Percent: how far the colour is to its target.<br>"
					+ "Number: the resin still needed.<br>"
					+ "None: nothing.",
			position = 9
	)
	default ProgressLabel showProgress()
	{
		return ProgressLabel.PERCENT;
	}

	@ConfigItem(
			keyName = "rewardOrder",
			name = "Reward Order",
			description = "How the rewards panel counts your resin.<br>"
					+ "In order: rewards are bought top to bottom, so each counts only the resin left after the "
					+ "ones above it.<br>"
					+ "Each on its own: every reward's time and percent count all your resin.",
			position = 10
	)
	default RewardOrder rewardOrder()
	{
		return RewardOrder.IN_ORDER;
	}

	@ConfigItem(
			keyName = RewardPriority.CONFIG_KEY,
			name = "Reward priority",
			description = "The order of the tracked rewards, set by ticking them and from the panel's right-click "
					+ "menu",
			hidden = true
	)
	default String rewardPriority()
	{
		return "";
	}

	@ConfigSection(
			name = "Misclick protection",
			description = "Lock idle or running stations and cap the Concentrate spam-click",
			position = 25
	)
	String MISCLICK_PROTECTION = "MisclickProtection";

	@ConfigItem(
			section = MISCLICK_PROTECTION,
			keyName = "lockAlembicWhenIdle",
			name = "Lock Alembic when idle",
			description = "Makes the Alembic unclickable while it is not in use or highlighted",
			position = 1
	)
	default boolean lockAlembicWhenIdle()
	{
		return false;
	}

	@ConfigItem(
			section = MISCLICK_PROTECTION,
			keyName = "lockAgitatorWhenIdle",
			name = "Lock Agitator when idle",
			description = "Makes the Agitator unclickable while it is not in use or highlighted",
			position = 2
	)
	default boolean lockAgitatorWhenIdle()
	{
		return false;
	}

	@ConfigItem(
			section = MISCLICK_PROTECTION,
			keyName = "lockRetortWhenIdle",
			name = "Lock Retort when idle",
			description = "Makes the Retort unclickable while it is not in use or highlighted",
			position = 3
	)
	default boolean lockRetortWhenIdle()
	{
		return false;
	}

	@ConfigItem(
			section = MISCLICK_PROTECTION,
			keyName = "capConcentrate",
			name = "Cap Concentrate clicks",
			description = "Stops Concentrate clicks on the last tick, when the potion completes on its own anyway, "
					+ "so spare clicks cannot load the next potion",
			position = 4
	)
	default boolean capConcentrate()
	{
		return false;
	}

	@ConfigItem(
			section = MISCLICK_PROTECTION,
			keyName = "leverClickProtection",
			name = "Lever click protection",
			description = "Only the next lever for the order you're mixing can be pulled.<br>"
					+ "All levers stay pullable if the vessel holds pastes that match no order",
			position = 5
	)
	default boolean leverClickProtection()
	{
		return false;
	}

	@ConfigItem(
			section = MISCLICK_PROTECTION,
			keyName = "potionProtection",
			name = "Potion protection",
			description = "Blocks mixing at the vessel when its three pastes don't make a potion for an open order.<br>"
					+ "Turn this off to mix anyway",
			position = 6
	)
	default boolean potionProtection()
	{
		return false;
	}
}
