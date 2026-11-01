package com.oveduumnakal.anothermixology;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.google.inject.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.runelite.api.Client;
import net.runelite.api.FontID;
import net.runelite.api.GameState;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.PostMenuSort;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.SpotanimID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;

import static com.oveduumnakal.anothermixology.AlchemyObject.AGA_LEVER;
import static com.oveduumnakal.anothermixology.AlchemyObject.LYE_LEVER;
import static com.oveduumnakal.anothermixology.AlchemyObject.MOX_LEVER;
import static com.oveduumnakal.anothermixology.PotionComponent.AGA;
import static com.oveduumnakal.anothermixology.PotionComponent.LYE;
import static com.oveduumnakal.anothermixology.PotionComponent.MOX;

/**
 * RuneLite plugin that extends the Mastering Mixology minigame: it adds each order's recipe to the interface and
 * highlights the stations, levers, vessel and quick actions to use next. It also tracks reward goals, recommends
 * orders with a selectable strategy, protects against misclicks and lets you pick the colours.
 */
@PluginDescriptor(name = "Another Mixology Plugin")
public class AnotherMixologyPlugin extends Plugin
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AnotherMixologyPlugin.class);

	private static final int PROC_MASTERING_MIXOLOGY_BUILD_POTION_ORDERS = 7063;
	private static final int PROC_MASTERING_MIXOLOGY_BUILD_REAGENTS = 7064;

	private static final int VARBIT_POTION_ORDER_1 = VarbitID.MM_LAB_ORDER_1_TYPE;
	private static final int VARBIT_POTION_MODIFIER_1 = VarbitID.MM_LAB_ORDER_1_MODIFIER;
	private static final int VARBIT_POTION_ORDER_2 = VarbitID.MM_LAB_ORDER_2_TYPE;
	private static final int VARBIT_POTION_MODIFIER_2 = VarbitID.MM_LAB_ORDER_2_MODIFIER;
	private static final int VARBIT_POTION_ORDER_3 = VarbitID.MM_LAB_ORDER_3_TYPE;
	private static final int VARBIT_POTION_MODIFIER_3 = VarbitID.MM_LAB_ORDER_3_MODIFIER;

	static final int VARP_LYE_RESIN = VarPlayerID.MIXOLOGY_LYE_POINTS;
	static final int VARP_AGA_RESIN = VarPlayerID.MIXOLOGY_AGA_POINTS;
	static final int VARP_MOX_RESIN = VarPlayerID.MIXOLOGY_MOX_POINTS;

	private static final int VARBIT_ALEMBIC_PROGRESS = VarbitID.MM_ALEMBIC_PROGRESS;
	private static final int VARBIT_AGITATOR_PROGRESS = VarbitID.MM_AGITATOR_PROGRESS;

	private static final int VARBIT_AGITATOR_QUICKACTION = VarbitID.MM_LAB_HIT_SKILLSHOT_AGITATOR;
	private static final int VARBIT_ALEMBIC_QUICKACTION = VarbitID.MM_LAB_HIT_SKILLSHOT_ALEMBIC;

	private static final int VARBIT_MIXING_VESSEL_POTION = VarbitID.MM_LAB_VESSEL_READY;
	private static final int VARBIT_AGITATOR_POTION = VarbitID.MM_LAB_AGITATOR_POTION;
	private static final int VARBIT_RETORT_POTION = VarbitID.MM_LAB_RETORT_POTION;
	private static final int VARBIT_ALEMBIC_POTION = VarbitID.MM_LAB_ALEMBIC_POTION;

	private static final int VARBIT_MIXER_SLOT_0 = VarbitID.MM_LAB_MIXER_SLOT_0;
	private static final int VARBIT_MIXER_SLOT_1 = VarbitID.MM_LAB_MIXER_SLOT_1;
	private static final int VARBIT_MIXER_SLOT_2 = VarbitID.MM_LAB_MIXER_SLOT_2;

	private static final Color READY_HIGHLIGHT_COLOR = Color.GREEN;

	/**
	 * Game ticks a let-through lever pull is counted as in the vessel while waiting for the mixer varbits.
	 */
	private static final int PENDING_LEVER_PULL_TICKS = 4;

	/**
	 * Index into {@link #resinIconIdx} of the block sprite drawn in the skip colour.
	 */
	private static final int SKIP_ICON = 3;

	private static final int VARBIT_DIGWEED_NORTH_EAST = VarbitID.MM_HERB_READY_1;
	private static final int VARBIT_DIGWEED_SOUTH_EAST = VarbitID.MM_HERB_READY_2;
	private static final int VARBIT_DIGWEED_SOUTH_WEST = VarbitID.MM_HERB_READY_3;
	private static final int VARBIT_DIGWEED_NORTH_WEST = VarbitID.MM_HERB_READY_4;

	private static final int SPOT_ANIM_AGITATOR = SpotanimID.VFX_MACHINERY_ALCHEMY01_AGITATOR01;
	private static final int SPOT_ANIM_ALEMBIC = SpotanimID.VFX_MACHINERY_ALCHEMY01_ALEMBIC01;

	private static final int COMPONENT_POTION_ORDERS_GROUP_ID = InterfaceID.MM_OVERLAY;
	private static final int COMPONENT_POTION_ORDERS = InterfaceID.MmOverlay.CONTENT;

	private static final int LABS_REGION_ID = 5521;
	private static final int LABS_REGION_PLANE = 0;

	private static final int FOUND_GEM = 2655;

	private static final double SECONDS_PER_TICK = 0.6;

	/** Game ticks after a login during which the client reloads the lab's varbits and inventory. */
	private static final int LOGIN_RELOAD_TICKS = 3;

	/** The animation id of a player doing nothing. */
	private static final int NO_ANIMATION = -1;

	@Inject
	private Client client;

	@Inject
	private AnotherMixologyConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Notifier notifier;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private AnotherMixologyOverlay overlay;

	@Inject
	private InventoryPotionOverlay potionOverlay;

	@Inject
	private OrdersPanelOverlay ordersPanelOverlay;

	@Inject
	private GoalInfoBoxOverlay goalInfoBoxOverlay;

	private final Map<AlchemyObject, HighlightedObject> highlightedObjects = new LinkedHashMap<>();
	private List<PotionOrder> potionOrders = Collections.emptyList();

	/**
	 * The last order set that was not empty, so its done and digweed marks come back when the same orders reappear
	 * after a logout; emptied on a delivery.
	 */
	private List<PotionOrder> knownOrders = Collections.emptyList();

	/** True from logging out, hopping or losing the connection until the next login. */
	private boolean loggedOut;

	/** Game ticks left of the reload after the last login. */
	private int loginReloadTicks;
	private boolean inLab = false;

	private PotionType alembicPotionType;
	private PotionType agitatorPotionType;
	private PotionType retortPotionType;

	private int previousAgitatorProgess;
	private int previousAlembicProgress;

	private final RetortProgressTracker retortProgressTracker = new RetortProgressTracker();

	private int agitatorQuickActionTicks = 0;
	private int alembicQuickActionTicks = 0;

	private boolean previouslyAboveRewardThreshold = false;

	private final Goal goal = new Goal(RewardItem.NONE);

	/** The policy behind the recommended-potion highlight, created from the Strategy setting. */
	private RecommendationPolicy recommendationPolicy = RecommendationStrategy.EXPECTED_VALUE.createPolicy();

	/**
	 * Order slots the recommendation policy picked on the last order rebuild; null when recommendations are off.
	 */
	private Set<Integer> recommendedSlots = null;

	/** The order slot a held digweed should go on, from the last order rebuild; -1 when none. */
	private int digweedTargetSlot = -1;

	/** How many digweed the inventory holds. */
	private int digweedHeld;

	/** The inventory slots of potions a digweed improved. */
	private final ImprovedPotionTracker improvedPotions = new ImprovedPotionTracker();

	/** The inventory's item ids by slot at the last change, to follow improved potions; null before the first. */
	private int[] lastInventory;

	/** The player's measured pace, from the order sets delivered this session. */
	private final PaceTracker paceTracker = new PaceTracker();

	/** The game tick of the last delivery, to time the next set; -1 before the first. */
	private int lastDeliveryTick = -1;

	/** The time the player has spent playing in the lab, which the reward times count down by. */
	private final PlayClock playClock = new PlayClock();

	/** Where the player stood on the last game tick, to tell when they move; null before the first. */
	private WorldPoint lastPlayerLocation;

	/** The rewards panel rows and their time estimates; null when no reward is tracked. */
	private volatile RewardEstimates rewardEstimates;

	/** The inputs the current estimates were made from, so unchanged inputs are not worked out again. */
	private String rewardEstimatesKey = "";

	private Future<?> rewardEstimateTask;
	/** The sprite the game shows beside each order slot, which is the icon of the station its modifier needs. */
	private final Map<Integer, Integer> orderSprites = new HashMap<>();

	/**
	 * Lever state used by lever click protection; counts pending lever pulls as already in the vessel.
	 */
	private MixGuidance.LeverState leverState = MixGuidance.LeverState.FREE;

	/**
	 * Lever state used for highlights; follows the confirmed vessel contents, so a pulled lever stays lit
	 * until its paste lands.
	 */
	private MixGuidance.LeverState highlightLeverState = MixGuidance.LeverState.FREE;

	/**
	 * True once the mixed potion has reached the inventory but the game has not yet cleared the vessel varbits.
	 * While set, the vessel is treated as empty so its highlight and the next lever update without that lag.
	 */
	private boolean vesselCollected = false;

	/**
	 * Lever pulls the client has sent that the mixer varbits do not show yet, oldest first. They count as already
	 * in the vessel, so a fast double click cannot add a second paste before the server catches up.
	 */
	private final List<PotionComponent> pendingLeverPulls = new ArrayList<>();

	private int pendingLeverPullsExpireTick = 0;

	private int unfinishedInInventory = 0;

	private int[] resinIconIdx = null;
	private net.runelite.api.IndexedSprite[] resinIconSprites = null;

	public Map<AlchemyObject, HighlightedObject> highlightedObjects()
	{
		return highlightedObjects;
	}

	public boolean isInLab()
	{
		return inLab;
	}

	/** Returns the current orders, in the order they are displayed. */
	List<PotionOrder> potionOrders()
	{
		return potionOrders;
	}

	/** Returns the order slot a held digweed should go on, or -1 when there is none. */
	int digweedTargetSlot()
	{
		return digweedTargetSlot;
	}

	/** Returns the inventory indexes of potions a digweed improved. */
	Set<Integer> improvedInventoryIndexes()
	{
		return improvedPotions.slots();
	}

	/** Returns the order a held digweed should go on, or null when there is none. */
	PotionOrder digweedTargetOrder()
	{
		for (var order : potionOrders)
		{
			if (order.idx() == digweedTargetSlot)
				return order;
		}

		return null;
	}

	/**
	 * Returns the inventory index of the potion a held digweed should go on: the first potion of the target order's
	 * type. Returns -1 when there is no target or that potion is not in the inventory.
	 */
	int digweedTargetInventoryIndex()
	{
		var order = digweedTargetOrder();
		var inventory = client.getItemContainer(InventoryID.INV);
		if (order == null || inventory == null)
			return -1;

		var items = inventory.getItems();
		for (int i = 0; i < items.length; i++)
		{
			if (PotionType.fromItemId(items[i].getId()) == order.potionType())
				return i;
		}

		return -1;
	}

	/** Returns the order slots the recommendation policy picks, or null when recommendations are off. */
	Set<Integer> recommendedSlots()
	{
		return recommendedSlots;
	}

	/** Returns the sprite id of the station icon beside the order in the given slot, or -1 before it has been read. */
	int orderSpriteId(int idx)
	{
		return orderSprites.getOrDefault(idx, -1);
	}

	/** Returns the paste of the given colour left in the hopper. */
	int pasteAmount(PotionComponent component)
	{
		switch (component)
		{
			case MOX: return client.getVarbitValue(VarbitID.MM_AVAILABLE_MOX);
			case AGA: return client.getVarbitValue(VarbitID.MM_AVAILABLE_AGA);
			default:  return client.getVarbitValue(VarbitID.MM_AVAILABLE_LYE);
		}
	}

	/** Returns the resin of the given colour the player has earned. */
	int resinAmount(PotionComponent component)
	{
		return client.getVarpValue(component.resinVarpId());
	}

	/**
	 * @return true if the player is in the labs region (the area where the minigame takes place)
	 * the isInlab method only checks if they are inside the actual lab room where the UI is active
	 */
	public boolean isInLabRegion()
	{
		Player player = client.getLocalPlayer();
		return player != null && player.getWorldLocation().getRegionID() == LABS_REGION_ID
				&& player.getWorldLocation().getPlane() == LABS_REGION_PLANE;
	}

	@Provides
	AnotherMixologyConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AnotherMixologyConfig.class);
	}

	@Override
	protected void startUp()
	{
		syncRewardPriority();
		overlayManager.add(overlay);
		overlayManager.add(potionOverlay);
		overlayManager.add(goalInfoBoxOverlay);
		overlayManager.add(ordersPanelOverlay);
		previouslyAboveRewardThreshold = false;
		recommendationPolicy = config.recommendationStrategy().createPolicy();
		clientThread.invokeLater(this::registerResinIcons);

		if (client.getGameState() == GameState.LOGGED_IN)
			clientThread.invokeLater(this::initialize);
	}

	/**
	 * Adds the recipe block sprites to the client's mod icons, or repaints them if they are already there.
	 *
	 * @return false while the client has not loaded its mod icons yet, so the client thread runs this again later
	 */
	private boolean registerResinIcons()
	{
		if (resinIconIdx != null)
		{
			applyResinIconPalettes();
			return true;
		}

		net.runelite.api.IndexedSprite[] mod = client.getModIcons();
		if (mod == null)
			return false;

		final int count = SKIP_ICON + 1;
		int base = mod.length;
		net.runelite.api.IndexedSprite[] extended = Arrays.copyOf(mod, base + count);
		int[] indices = new int[count];
		net.runelite.api.IndexedSprite[] sprites = new net.runelite.api.IndexedSprite[count];
		for (int i = 0; i < count; i++)
		{
			net.runelite.api.IndexedSprite s = createBlockSprite(resinIconColor(i));
			extended[base + i] = s;
			indices[i] = base + i;
			sprites[i] = s;
		}

		client.setModIcons(extended);
		resinIconIdx = indices;
		resinIconSprites = sprites;
		triggerPotionOrderUpdate();
		return true;
	}

	private net.runelite.api.IndexedSprite createBlockSprite(Color color)
	{
		final int size = 9;
		net.runelite.api.IndexedSprite s = client.createIndexedSprite();
		byte[] pixels = new byte[size * size];
		for (int i = 0; i < pixels.length; i++)
		{
			int x = i % size;
			int y = i / size;
			boolean edge = x == 0 || x == size - 1 || y == 0 || y == size - 1;
			pixels[i] = (byte) (edge ? 1 : 2);
		}

		s.setPixels(pixels);
		s.setPalette(blockPalette(color));
		s.setWidth(size);
		s.setHeight(size);
		s.setOriginalWidth(size);
		s.setOriginalHeight(size);
		return s;
	}

	private static int[] blockPalette(Color color)
	{
		return new int[]{0, 0x000000, color.getRGB() & 0xFFFFFF};
	}

	/**
	 * @param icon a component ordinal, or {@link #SKIP_ICON}
	 */
	private Color resinIconColor(int icon)
	{
		if (icon == SKIP_ICON)
			return config.skipColor();

		return effectiveComponentColor(PotionComponent.ENTRIES[icon]);
	}

	private void applyResinIconPalettes()
	{
		if (resinIconSprites == null)
			return;

		for (int i = 0; i < resinIconSprites.length; i++)
		{
			net.runelite.api.IndexedSprite s = resinIconSprites[i];
			if (s != null)
				s.setPalette(blockPalette(resinIconColor(i)));
		}
	}

	/** Returns the colour chosen in the Colors section for a paste: the colorblind preset, or the custom colour. */
	Color effectiveComponentColor(PotionComponent c)
	{
		Color preset = config.colorblindMode().color(c);
		if (preset != null)
			return preset;

		switch (c)
		{
			case MOX: return config.moxColor();
			case AGA: return config.agaColor();
			default:  return config.lyeColor();
		}
	}

	/**
	 * Removes the overlays and highlights and clears the per-session state. The game's orders panel is shown again
	 * and rebuilt, so neither a hidden panel nor the appended recipes outlive the plugin.
	 */
	@Override
	protected void shutDown()
	{
		if (rewardEstimateTask != null)
			rewardEstimateTask.cancel(true);

		rewardEstimates = null;
		rewardEstimatesKey = "";
		overlayManager.remove(overlay);
		overlayManager.remove(potionOverlay);
		overlayManager.remove(goalInfoBoxOverlay);
		overlayManager.remove(ordersPanelOverlay);
		inLab = false;
		highlightedObjects.clear();
		agitatorQuickActionTicks = 0;
		alembicQuickActionTicks = 0;
		pendingLeverPulls.clear();
		vesselCollected = false;
		retortProgressTracker.onPotionCleared();
		clientThread.invokeLater(() ->
		{
			var baseWidget = client.getWidget(COMPONENT_POTION_ORDERS);
			if (baseWidget != null)
				setOrdersPanelHidden(baseWidget, false);

			triggerPotionOrderUpdate();
		});
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		var state = event.getGameState();
		if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING)
			highlightedObjects.clear();

		if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING || state == GameState.CONNECTION_LOST)
		{
			loggedOut = true;
		}
		else if (state == GameState.LOGGED_IN && loggedOut)
		{
			loggedOut = false;
			loginReloadTicks = LOGIN_RELOAD_TICKS;
		}
	}

	/**
	 * Returns whether the client is logged out or still reloading the lab after a login. Varbits and the inventory
	 * read empty while it does, which is not play: no delivery and no potion leaving.
	 */
	private boolean reloading()
	{
		return loggedOut || loginReloadTicks > 0;
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() != COMPONENT_POTION_ORDERS_GROUP_ID)
			return;

		initialize();
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		if (event.getGroupId() != COMPONENT_POTION_ORDERS_GROUP_ID)
			return;

		highlightedObjects.clear();
		inLab = false;
	}

	/**
	 * Applies a settings change straight away: re-sorts the orders, refreshes the station highlights and the resin
	 * display, clears the digweed highlights when they are turned off and recalculates the goal.
	 *
	 * <p>The orders are also re-decorated when a highlight-related setting changes, so the player sees the effect
	 * immediately without waiting for the next order rebuild.
	 */
	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(AnotherMixologyConfig.CONFIG_GROUP))
			return;

		if (event.getKey().equals("potionOrderSorting"))
			clientThread.invokeLater(this::updatePotionOrders);

		if (event.getKey().equals("highlightStations") || event.getKey().equals("waitForFullOrder"))
			clientThread.invokeLater(this::refreshStationHighlight);

		if (event.getKey().equals("displayResin"))
			clientThread.invokeLater(this::triggerPotionOrderUpdate);

		if (!config.highlightDigWeed())
		{
			unHighlightObject(AlchemyObject.DIGWEED_NORTH_EAST);
			unHighlightObject(AlchemyObject.DIGWEED_SOUTH_EAST);
			unHighlightObject(AlchemyObject.DIGWEED_SOUTH_WEST);
			unHighlightObject(AlchemyObject.DIGWEED_NORTH_WEST);
		}

		if (event.getKey().startsWith("track") || event.getKey().endsWith("Quantity"))
			syncRewardPriority();

		if (event.getKey().startsWith("track") || event.getKey().endsWith("Quantity")
				|| event.getKey().equals(RewardPriority.CONFIG_KEY))
		{
			recalculateGoalData();
			previouslyAboveRewardThreshold = false;
		}

		if (event.getKey().equals("hideOrdersPanel"))
			clientThread.invokeLater(() ->
			{
				var baseWidget = client.getWidget(COMPONENT_POTION_ORDERS);
				if (baseWidget != null)
					setOrdersPanelHidden(baseWidget, config.hideOrdersPanel());

				triggerPotionOrderUpdate();
			});

		if (event.getKey().equals("recommendationStrategy"))
			recommendationPolicy = config.recommendationStrategy().createPolicy();

		if (event.getKey().equals("recommendationStrategy")
				|| event.getKey().equals("useLetters")
				|| event.getKey().equals("colorblindMode")
				|| event.getKey().equals("accessibilityMoxColor")
				|| event.getKey().equals("accessibilityAgaColor")
				|| event.getKey().equals("accessibilityLyeColor")
				|| event.getKey().equals("accessibilitySkipColor")
				|| event.getKey().equals("highlightDigweedTarget")
				|| event.getKey().equals("digweedHighlightColor"))
			clientThread.invokeLater(this::triggerPotionOrderUpdate);

		if (event.getKey().equals("useLetters")
				|| event.getKey().equals("colorblindMode")
				|| event.getKey().equals("accessibilityMoxColor")
				|| event.getKey().equals("accessibilityAgaColor")
				|| event.getKey().equals("accessibilityLyeColor")
				|| event.getKey().equals("accessibilitySkipColor"))
			clientThread.invokeLater(this::applyResinIconPalettes);

		clientThread.invokeLater(this::updateMixGuidance);
		clientThread.invokeLater(this::refreshRewardEstimates);
	}

	/**
	 * Keeps the unfinished-potion count and the station highlight in step with the inventory.
	 *
	 * <p>The highlight is left alone while a potion is in a station. Otherwise it goes to the station of the first
	 * potion in the inventory that still needs one.
	 */
	/**
	 * Marks the order whose potion a digweed just improved, read from the chat message, and rebuilds the orders so the
	 * recommendation counts that potion twice.
	 */
	private static int[] itemIds(ItemContainer container)
	{
		var items = container.getItems();
		int[] ids = new int[items.length];
		for (int i = 0; i < items.length; i++)
			ids[i] = items[i].getId();

		return ids;
	}

	/**
	 * Records the potion slot a digweed is used on, either way round: the digweed on a potion, or a potion on the
	 * digweed. The game's chat message then confirms it.
	 */
	private void recordDigweedUse(MenuOptionClicked event)
	{
		var selected = client.getSelectedWidget();
		if (selected == null)
			return;

		int selectedItem = selected.getItemId();
		int targetItem = event.getItemId();
		if (selectedItem == Digweed.ITEM_ID && PotionType.fromItemId(targetItem) != null)
			improvedPotions.clicked(event.getParam0(), targetItem);
		else if (targetItem == Digweed.ITEM_ID && PotionType.fromItemId(selectedItem) != null)
			improvedPotions.clicked(selected.getIndex(), selectedItem);
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (!inLab)
			return;

		var potionType = Digweed.improvedPotion(event.getMessage());
		if (potionType == null)
			return;

		var order = Digweed.orderImproved(potionOrders, potionType, digweedTargetSlot, recommendedSlots);
		if (order == null)
			return;

		LOGGER.debug("Digweed improved order {}", order);
		order.setImproved(true);
		var inventory = client.getItemContainer(InventoryID.INV);
		if (inventory != null)
			improvedPotions.confirmed(potionType, itemIds(inventory));

		triggerPotionOrderUpdate();
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (inLab && event.getContainerId() == InventoryID.INV)
		{
			markActive();
			int[] items = itemIds(event.getItemContainer());
			if (lastInventory != null && !reloading())
				improvedPotions.inventoryChanged(lastInventory, items);

			lastInventory = items;
			int digweed = event.getItemContainer().count(Digweed.ITEM_ID);
			if (digweed != digweedHeld)
			{
				digweedHeld = digweed;
				triggerPotionOrderUpdate();
			}

			int unfinished = countUnfinishedInInventory();
			if (unfinished > unfinishedInInventory
					&& MixGuidance.vesselFull(mixerSlots(), client.getVarbitValue(VARBIT_MIXING_VESSEL_POTION)))
				vesselCollected = true;

			unfinishedInInventory = unfinished;
			updateMixGuidance();
		}

		if (!inLab || !config.highlightStations() || event.getContainerId() != InventoryID.INV)
			return;

		if (alembicPotionType != null || agitatorPotionType != null || retortPotionType != null)
			return;

		if (waitingForFullOrder())
		{
			unHighlightAllStations();
			return;
		}

		var inventory = event.getItemContainer();

		for (var item : inventory.getItems())
		{
			var potionType = PotionType.fromItemId(item.getId());

			if (potionType == null || potionType.modifiedItemId() == item.getId())
				continue;

			for (var order : potionOrders)
			{
				if (order.potionType() == potionType && !order.fulfilled())
				{
					unHighlightAllStations();
					highlightObject(order.potionModifier().alchemyObject(), config.stationHighlightColor());
					return;
				}
			}
		}
	}

	/**
	 * Reacts to the lab's varbits and varps.
	 *
	 * <p>Delivering a potion resets every order varbit to 0 before setting the new values, so the first order varbit
	 * dropping to 0 clears all the stations and the order list; the new orders then start with nothing done. The
	 * varbits also read 0 while the client logs back in, which is ignored so the orders keep their marks. A
	 * station's potion varbit returning to 0 means that station has finished: its highlight is removed and the
	 * matching order is fulfilled. A progress varbit that drops means a quick action failed and a quick-action varbit
	 * means one was just popped, so both reset that station's highlight. The Agitator's quick action has a window of
	 * two ticks and the Alembic's of one, and the highlight is also reset once that window has passed. Resin varps
	 * recalculate the goal.
	 */
	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		var varbitId = event.getVarbitId();
		var varpId = event.getVarpId();
		var value = event.getValue();

		if (varbitId == VARBIT_POTION_ORDER_1)
		{
			if (value == 0 && reloading())
			{
				LOGGER.debug("Order varbit reset by a login, not a delivery");
			}
			else if (value == 0)
			{
				recordDelivery();
				unHighlightAllStations();
				potionOrders = Collections.emptyList();
				knownOrders = Collections.emptyList();
				improvedPotions.clear();
			}
			else
			{
				clientThread.invokeAtTickEnd(this::updatePotionOrders);
			}
		}
		else if (varbitId == VARBIT_ALEMBIC_POTION)
		{
			if (value == 0)
			{
				unHighlightObject(AlchemyObject.ALEMBIC);
				tryFulfillOrder(alembicPotionType, PotionModifier.CRYSTALISED);
				tryHighlightNextStation();
				LOGGER.debug("Finished crystalising {}", alembicPotionType);
				alembicPotionType = null;
				alembicQuickActionTicks = 0;
			}
			else
			{
				alembicPotionType = PotionType.fromIdx(value - 1);
				LOGGER.debug("Alembic potion type: {}", alembicPotionType);
			}
		}
		else if (varbitId == VARBIT_AGITATOR_POTION)
		{
			if (value == 0)
			{
				unHighlightObject(AlchemyObject.AGITATOR);
				tryFulfillOrder(agitatorPotionType, PotionModifier.HOMOGENOUS);
				tryHighlightNextStation();
				LOGGER.debug("Finished homogenising {}", agitatorPotionType);
				agitatorPotionType = null;
				agitatorQuickActionTicks = 0;
			}
			else
			{
				agitatorPotionType = PotionType.fromIdx(value - 1);
				LOGGER.debug("Agitator potion type: {}", agitatorPotionType);
			}
		}
		else if (varbitId == VARBIT_RETORT_POTION)
		{
			if (value == 0)
			{
				unHighlightObject(AlchemyObject.RETORT);
				tryFulfillOrder(retortPotionType, PotionModifier.CONCENTRATED);
				tryHighlightNextStation();
				LOGGER.debug("Finished concentrating {}", retortPotionType);
				retortPotionType = null;
				retortProgressTracker.onPotionCleared();
			}
			else
			{
				retortPotionType = PotionType.fromIdx(value - 1);
				LOGGER.debug("Retort potion type: {}", retortPotionType);
				retortProgressTracker.onPotionLoaded();
			}
		}
		else if (varbitId == VARBIT_DIGWEED_NORTH_EAST)
		{
			if (value == 1)
			{
				if (config.highlightDigWeed())
					highlightObject(AlchemyObject.DIGWEED_NORTH_EAST, config.digweedHighlightColor());

				notifier.notify(config.notifyDigWeed(), "A digweed has spawned north east.");
			}
			else
			{
				unHighlightObject(AlchemyObject.DIGWEED_NORTH_EAST);
			}
		}
		else if (varbitId == VARBIT_DIGWEED_SOUTH_EAST)
		{
			if (value == 1)
			{
				if (config.highlightDigWeed())
					highlightObject(AlchemyObject.DIGWEED_SOUTH_EAST, config.digweedHighlightColor());

				notifier.notify(config.notifyDigWeed(), "A digweed has spawned south east.");
			}
			else
			{
				unHighlightObject(AlchemyObject.DIGWEED_SOUTH_EAST);
			}
		}
		else if (varbitId == VARBIT_DIGWEED_SOUTH_WEST)
		{
			if (value == 1)
			{
				if (config.highlightDigWeed())
					highlightObject(AlchemyObject.DIGWEED_SOUTH_WEST, config.digweedHighlightColor());

				notifier.notify(config.notifyDigWeed(), "A digweed has spawned south west.");
			}
			else
			{
				unHighlightObject(AlchemyObject.DIGWEED_SOUTH_WEST);
			}
		}
		else if (varbitId == VARBIT_DIGWEED_NORTH_WEST)
		{
			if (value == 1)
			{
				if (config.highlightDigWeed())
					highlightObject(AlchemyObject.DIGWEED_NORTH_WEST, config.digweedHighlightColor());

				notifier.notify(config.notifyDigWeed(), "A digweed has spawned north west.");
			}
			else
			{
				unHighlightObject(AlchemyObject.DIGWEED_NORTH_WEST);
			}
		}
		else if (varbitId == VARBIT_AGITATOR_PROGRESS)
		{
			if (agitatorQuickActionTicks == 2)
			{
				if (config.highlightQuickActionEvents())
					resetStationHighlight(AlchemyObject.AGITATOR);

				agitatorQuickActionTicks = 0;
			}

			if (agitatorQuickActionTicks == 1)
				agitatorQuickActionTicks = 2;

			if (value < previousAgitatorProgess)
				resetStationHighlight(AlchemyObject.AGITATOR);

			previousAgitatorProgess = value;
		}
		else if (varbitId == VARBIT_ALEMBIC_PROGRESS)
		{
			if (alembicQuickActionTicks == 1)
			{
				if (config.highlightQuickActionEvents())
					resetStationHighlight(AlchemyObject.ALEMBIC);

				alembicQuickActionTicks = 0;
			}

			if (value < previousAlembicProgress)
				resetStationHighlight(AlchemyObject.ALEMBIC);

			previousAlembicProgress = value;
		}
		else if (varbitId == VarbitID.MM_RETORT_PROGRESS)
		{
			retortProgressTracker.onProgressChanged(value);
			updateRetortQuickActionHighlight();
		}
		else if (varbitId == VARBIT_AGITATOR_QUICKACTION)
		{
			resetStationHighlight(AlchemyObject.AGITATOR);
		}
		else if (varbitId == VARBIT_ALEMBIC_QUICKACTION)
		{
			resetStationHighlight(AlchemyObject.ALEMBIC);
		}
		else if (varpId == VARP_MOX_RESIN || varpId == VARP_AGA_RESIN || varpId == VARP_LYE_RESIN)
		{
			recalculateGoalData();
			checkRewardThresholdNotification();
		}

		if (value != 0 && isMixerSlot(varbitId) && !pendingLeverPulls.isEmpty())
			pendingLeverPulls.remove(0);

		if (inLab && affectsMixGuidance(varbitId))
		{
			markActive();
			updateMixGuidance();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (loginReloadTicks > 0)
			loginReloadTicks--;

		if (inLab && playerBusy())
			markActive();

		if (pendingLeverPulls.isEmpty() || client.getTickCount() < pendingLeverPullsExpireTick)
			return;

		pendingLeverPulls.clear();
		updateMixGuidance();
	}

	/** Returns whether the player is animating or moved since the last tick. */
	private boolean playerBusy()
	{
		Player player = client.getLocalPlayer();
		if (player == null)
			return false;

		var location = player.getWorldLocation();
		boolean moved = lastPlayerLocation != null && !location.equals(lastPlayerLocation);
		lastPlayerLocation = location;
		return moved || player.getAnimation() != NO_ANIMATION;
	}

	/** Records that the player is playing, so the reward times keep counting down. */
	private void markActive()
	{
		playClock.active(System.currentTimeMillis());
	}

	/** Returns the milliseconds played in the lab, which the reward times count down by. */
	long playedMillis()
	{
		return playClock.played(System.currentTimeMillis());
	}

	private static boolean isMixerSlot(int varbitId)
	{
		return varbitId == VARBIT_MIXER_SLOT_0
				|| varbitId == VARBIT_MIXER_SLOT_1
				|| varbitId == VARBIT_MIXER_SLOT_2;
	}

	private static boolean affectsMixGuidance(int varbitId)
	{
		return isMixerSlot(varbitId)
				|| varbitId == VARBIT_MIXING_VESSEL_POTION
				|| varbitId == VARBIT_ALEMBIC_POTION
				|| varbitId == VARBIT_AGITATOR_POTION
				|| varbitId == VARBIT_RETORT_POTION;
	}

	/**
	 * Notifies once when the goal first becomes affordable.
	 *
	 * <p>{@link Goal} has already summed the costs, including the pack quantities, and its overall progress
	 * is the mean of each colour's clamped percentage, so it reaches 1.0 only when every colour meets its
	 * summed goal.
	 */
	private void checkRewardThresholdNotification()
	{
		if (!goal.isMultiMode())
		{
			previouslyAboveRewardThreshold = false;
			return;
		}

		boolean nowAbove = goal.getOverallProgress() >= 1.0;
		if (nowAbove && !previouslyAboveRewardThreshold)
		{
			notifier.notify(config.thresholdNotification(),
					"You have enough resin to purchase your selected Mastering Mixology rewards.");
		}

		previouslyAboveRewardThreshold = nowAbove;
	}

	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed event)
	{
		if (inLab && alembicPotionType != null && event.getSoundId() == FOUND_GEM && event.getDelay() > 0
				&& config.soundEffectAlembic())
		{
			LOGGER.debug("client found_gem sound effect detected during Alembic, blocking");
			event.consume();
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!inLab)
			return;

		MenuAction action = event.getMenuAction();
		if (action == MenuAction.WIDGET_TARGET_ON_WIDGET)
			recordDigweedUse(event);

		if (action != MenuAction.GAME_OBJECT_FIRST_OPTION
				&& action != MenuAction.GAME_OBJECT_SECOND_OPTION
				&& action != MenuAction.GAME_OBJECT_THIRD_OPTION
				&& action != MenuAction.GAME_OBJECT_FOURTH_OPTION
				&& action != MenuAction.GAME_OBJECT_FIFTH_OPTION)
			return;

		if (isLeverBlocked(event.getId()) || isVesselBlocked(event.getId()))
		{
			event.consume();
			return;
		}

		recordLeverPull(event.getId());
		AlchemyObject station = StationGuard.stationFromTarget(event.getMenuTarget());
		if (station != null && (isCapBlocked(station) || isLockBlocked(station)))
			event.consume();
	}

	private boolean isLeverBlocked(int objectId)
	{
		if (!config.leverClickProtection())
			return false;

		PotionComponent lever = leverComponent(objectId);
		if (lever == null)
			return false;

		updateMixGuidance();
		return !leverState.allows(lever);
	}

	/**
	 * Counts a let-through lever click as a paste already in the vessel until the mixer varbits confirm it.
	 */
	private void recordLeverPull(int objectId)
	{
		PotionComponent lever = leverComponent(objectId);
		if (lever == null)
			return;

		pendingLeverPulls.add(lever);
		pendingLeverPullsExpireTick = client.getTickCount() + PENDING_LEVER_PULL_TICKS;
		updateMixGuidance();
	}

	private static PotionComponent leverComponent(int objectId)
	{
		if (objectId == MOX_LEVER.objectId())
			return MOX;

		if (objectId == AGA_LEVER.objectId())
			return AGA;

		if (objectId == LYE_LEVER.objectId())
			return LYE;

		return null;
	}

	private boolean isVesselBlocked(int objectId)
	{
		if (!config.potionProtection() || objectId != AlchemyObject.MIXING_VESSEL.objectId())
			return false;

		int[] slots = mixerSlots();
		int vesselReady = client.getVarbitValue(VARBIT_MIXING_VESSEL_POTION);
		if (!MixGuidance.vesselFull(slots, vesselReady))
			return false;

		return !vesselMixValid(slots, vesselReady);
	}

	/** True when the vessel's mix is for an order the player plans to make; skipped orders do not count. */
	private boolean vesselMixPlanned(int[] slots, int vesselReady)
	{
		PotionType mix = MixGuidance.vesselPotion(slots, vesselReady);
		return MixGuidance.mixMatchesPlannedOrder(mix, potionOrders, recommendedSlots, potionsOnHand(false));
	}

	private boolean vesselMixValid(int[] slots, int vesselReady)
	{
		PotionType mix = MixGuidance.vesselPotion(slots, vesselReady);
		return MixGuidance.mixMatchesOpenOrder(mix, potionOrders, potionsOnHand(false));
	}

	private boolean isCapBlocked(AlchemyObject station)
	{
		if (station != AlchemyObject.RETORT || !config.capConcentrate() || !retortProgressTracker.potionLoaded())
			return false;

		return StationGuard.concentrateCapReached(
				retortProgressTracker.currentProgress(),
				StationGuard.RETORT_PROGRESS_MAX);
	}

	private boolean isLockBlocked(AlchemyObject station)
	{
		if (!isLockEnabled(station))
			return false;

		boolean occupied = stationOccupied(station);
		return !StationGuard.isStationNeeded(station, occupied, potionOrders, heldItemIds());
	}

	private boolean isLockEnabled(AlchemyObject station)
	{
		if (station == AlchemyObject.ALEMBIC)
			return config.lockAlembicWhenIdle();

		if (station == AlchemyObject.AGITATOR)
			return config.lockAgitatorWhenIdle();

		if (station == AlchemyObject.RETORT)
			return config.lockRetortWhenIdle();

		return false;
	}

	private boolean stationOccupied(AlchemyObject station)
	{
		if (station == AlchemyObject.ALEMBIC)
			return alembicPotionType != null;

		if (station == AlchemyObject.AGITATOR)
			return agitatorPotionType != null;

		if (station == AlchemyObject.RETORT)
			return retortPotionType != null;

		return false;
	}

	private Set<Integer> heldItemIds()
	{
		Set<Integer> ids = new HashSet<>();
		var inventory = client.getItemContainer(InventoryID.INV);
		if (inventory == null)
			return ids;

		for (var item : inventory.getItems())
		{
			if (item.getId() != -1)
				ids.add(item.getId());
		}

		return ids;
	}

	/**
	 * Highlights a station's quick action when its spot animation appears.
	 *
	 * <p>The Alembic's quick action has a window of one tick and the Agitator's a window of two, so a counter goes
	 * 0, 1 (and 2 for the Agitator) and the window closes on that station's next progress update.
	 */
	@Subscribe
	public void onGraphicsObjectCreated(GraphicsObjectCreated event)
	{
		var spotAnimId = event.getGraphicsObject().getId();
		boolean highlight = config.highlightQuickActionEvents();

		if (spotAnimId == SPOT_ANIM_ALEMBIC && alembicPotionType != null)
		{
			alembicQuickActionTicks = 1;
			if (highlight)
			{
				highlightObject(AlchemyObject.ALEMBIC, config.stationQuickActionHighlightColor());

				if (config.soundEffectAlembic())
				{
					LOGGER.debug("Playing manual found_gem sound effect");
					client.playSoundEffect(FOUND_GEM);
				}
			}
		}

		if (spotAnimId == SPOT_ANIM_AGITATOR && agitatorPotionType != null)
		{
			agitatorQuickActionTicks = 1;
			if (highlight)
				highlightObject(AlchemyObject.AGITATOR, config.stationQuickActionHighlightColor());
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		var scriptId = event.getScriptId();
		if (scriptId != PROC_MASTERING_MIXOLOGY_BUILD_POTION_ORDERS
				&& scriptId != PROC_MASTERING_MIXOLOGY_BUILD_REAGENTS)
			return;

		var baseWidget = client.getWidget(COMPONENT_POTION_ORDERS);

		if (baseWidget == null)
			return;

		if (scriptId == PROC_MASTERING_MIXOLOGY_BUILD_POTION_ORDERS)
			updatePotionOrdersComponent(baseWidget);
		else if (config.hideOrdersPanel())
			setOrdersPanelHidden(baseWidget, true);
		else
			appendResins(baseWidget);
	}

	/**
	 * Appends each order's recipe and status to its row in the vanilla interface, colours the row from the
	 * recommendation and moves the rows to match the chosen sorting.
	 *
	 * <p>The rows are found from the interface script (Joshua-F/cs2-scripts, {@code [proc,script7063].cs2}, line 26).
	 * The recommendation is computed once per rebuild, because the orders and the resin state are both
	 * stable until the next refresh. It is null when there is nothing to recommend, for instance when no rewards are
	 * tracked yet.
	 *
	 * <p>The filtered children are laid out as a text for the heading, then a graphic and a text for each order:
	 * <pre>{@code
	 * TEXT - Potion Orders
	 * GRAPHIC - 5673
	 * TEXT - Mammoth-might mix
	 * GRAPHIC - 5672
	 * TEXT - <str>Mixalot</str>
	 * GRAPHIC - 5673
	 * TEXT - Marley's moonlight
	 * }</pre>
	 */
	private void updatePotionOrdersComponent(Widget baseWidget)
	{
		var children = selectChildren(baseWidget,
				widget -> widget.getType() == WidgetType.GRAPHIC || widget.getType() == WidgetType.TEXT);

		if (children.isEmpty())
			return;

		recommendedSlots = computeRecommendedSlots();
		digweedTargetSlot = computeDigweedTarget();

		for (int i = 0; i < potionOrders.size(); i++)
		{
			var order = potionOrders.get(i);
			LOGGER.debug("Updating component for order {}", order);
			var orderGraphic = children.get(order.idx() * 2 + 1);
			var orderText = children.get(order.idx() * 2 + 2);

			if (orderGraphic.getType() != WidgetType.GRAPHIC || orderText.getType() != WidgetType.TEXT)
			{
				LOGGER.debug("Eep Eep! Selected the wrong components!");
				continue;
			}

			orderSprites.put(order.idx(), orderGraphic.getSpriteId());
			LOGGER.debug("Order {} modifier {} sprite {}",
					order.idx(), order.potionModifier(), orderGraphic.getSpriteId());
			boolean skipped = recommendedSlots != null && !order.fulfilled() && !recommendedSlots.contains(order.idx());
			Color skipColor = config.skipColor();
			var builder = new StringBuilder(skipped ? orderText.getText() : potionName(order.potionType(),
					orderText.getText()));

			if (order.fulfilled())
				builder.append(" (<col=00ff00>done!</col>)");
			else
				builder.append(" (")
						.append(recipeText(order.potionType(), skipped, skipColor))
						.append(")");

			if (order.idx() == digweedTargetSlot)
				builder.append(' ').append(ColorUtil.wrapWithColorTag("+digweed", config.digweedHighlightColor()));
			else if (order.improved())
				builder.append(' ').append(ColorUtil.wrapWithColorTag("x2", config.digweedHighlightColor()));

			orderText.setText(builder.toString());

			if (skipped)
				orderText.setTextColor(skipColor.getRGB());

			if (i != order.idx())
			{
				LOGGER.debug("Updating order {} position from {} to {}", order, order.idx(), i);
				var y = 20 + (i * 26) + 3;
				orderGraphic.setOriginalY(y);
				orderText.setOriginalY(y);

				orderGraphic.revalidate();
				orderText.revalidate();
			}
		}

		for (var child : children)
			child.setHidden(config.hideOrdersPanel());

		updateMixGuidance();
	}

	/**
	 * Colours each part of the potion's name in its paste colour, as the orders overlay does. Text that is not the
	 * plain potion name is returned unchanged.
	 */
	private String potionName(PotionType potionType, String text)
	{
		var parts = potionType.nameParts();
		if (!text.equals(String.join("", parts)))
			return text;

		var components = potionType.components();
		var builder = new StringBuilder();
		for (int i = 0; i < parts.length; i++)
			builder.append(ColorUtil.wrapWithColorTag(parts[i], effectiveComponentColor(components[i])));

		return builder.toString();
	}

	/**
	 * Builds the recipe shown after an order's name: coloured blocks, or letters with Use Letters on.
	 * A skipped order draws every letter or block in the skip colour.
	 */
	private String recipeText(PotionType potionType, boolean skipped, Color skipColor)
	{
		boolean blocks = !config.useLetters() && resinIconIdx != null;
		if (!skipped)
			return blocks ? potionType.recipeBlocks(resinIconIdx) : recipeLetters(potionType);

		if (blocks)
		{
			int skipIcon = resinIconIdx[SKIP_ICON];
			return potionType.recipeBlocks(new int[]{skipIcon, skipIcon, skipIcon});
		}

		return ColorUtil.wrapWithColorTag(potionType.abbreviation(), skipColor);
	}

	private String recipeLetters(PotionType potionType)
	{
		var builder = new StringBuilder();
		for (var component : potionType.components())
			builder.append(ColorUtil.wrapWithColorTag(String.valueOf(component.character()),
					effectiveComponentColor(component)));

		return builder.toString();
	}

	/**
	 * Returns the order indices the chosen recommendation policy picks
	 * this turn, or null if there is nothing to recommend (no rewards
	 * selected, or order data missing).
	 *
	 * <p>The target is the summed cost of the selected rewards, which {@link Goal} already computes for the overlay
	 * and the threshold notification. The orders are placed back in their original conveyor slots, because
	 * {@code potionOrders} may be sorted by the sorting setting.
	 */
	private Set<Integer> computeRecommendedSlots()
	{
		if (potionOrders.size() != 3)
			return null;

		int[] target = goalTarget();
		if (target == null)
			return null;

		return recommendationPolicy.decide(orderSet(), currentResin(), target);
	}

	/**
	 * Returns the slot a held digweed should go on, or -1 when the highlight is off, no digweed is held or no order
	 * can take it. The candidates are the recommended orders, or all three when nothing is recommended; without a
	 * reward goal every colour counts the same.
	 */
	private int computeDigweedTarget()
	{
		if (!config.highlightDigweedTarget() || digweedHeld == 0 || potionOrders.size() != 3)
			return -1;

		int[] target = goalTarget();
		int[] deficit = target == null
				? new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE}
				: MetaPolicy.deficit(currentResin(), target);
		Set<Integer> candidates = recommendedSlots == null || recommendedSlots.isEmpty()
				? Set.of(0, 1, 2)
				: recommendedSlots;
		return ExpectedValuePolicy.digweedSlot(orderSet(), candidates, deficit);
	}

	/** Returns the summed cost of the ticked rewards per colour, or null when no reward is ticked. */
	private int[] goalTarget()
	{
		if (!goal.isMultiMode())
			return null;

		var mox = goal.getComponentData(MOX);
		var aga = goal.getComponentData(AGA);
		var lye = goal.getComponentData(LYE);
		if (mox == null || aga == null || lye == null)
			return null;

		int[] target = { mox.goalAmount, aga.goalAmount, lye.goalAmount };
		if (target[0] == 0 && target[1] == 0 && target[2] == 0)
			return null;

		return target;
	}

	private int[] currentResin()
	{
		return new int[]{
				client.getVarpValue(VARP_MOX_RESIN),
				client.getVarpValue(VARP_AGA_RESIN),
				client.getVarpValue(VARP_LYE_RESIN)
		};
	}

	/** Returns the orders by conveyor slot, with their modifiers, digweed improvements and the digweed held. */
	private OrderSet orderSet()
	{
		PotionType[] ordersBySlot = new PotionType[3];
		PotionModifier[] modifiersBySlot = new PotionModifier[3];
		boolean[] improvedBySlot = new boolean[3];
		for (PotionOrder po : potionOrders)
		{
			ordersBySlot[po.idx()] = po.potionType();
			modifiersBySlot[po.idx()] = po.potionModifier();
			improvedBySlot[po.idx()] = po.improved();
		}

		return new OrderSet(ordersBySlot, modifiersBySlot, improvedBySlot, digweedHeld);
	}

	private List<Widget> selectChildren(Widget parent, Predicate<Widget> filter)
	{
		var children = parent.getChildren();

		if (children == null)
			return List.of();

		return Arrays.stream(children)
				.filter(filter)
				.collect(Collectors.toUnmodifiableList());
	}

	/** Hides or shows every element the game draws in the orders panel, including its paste counts. */
	private void setOrdersPanelHidden(Widget baseWidget, boolean hidden)
	{
		for (var child : selectChildren(baseWidget, widget -> true))
			child.setHidden(hidden);
	}

	private void appendResins(Widget baseWidget)
	{
		if (!config.displayResin())
			return;

		var parentWidth = baseWidget.getWidth();
		var dx = parentWidth / 3;
		int x = dx / 2;

		addResinText(baseWidget.createChild(-1, WidgetType.TEXT), x, VARP_MOX_RESIN, MOX);
		addResinText(baseWidget.createChild(-1, WidgetType.TEXT), x + dx, VARP_AGA_RESIN, AGA);
		addResinText(baseWidget.createChild(-1, WidgetType.TEXT), x + dx * 2, VARP_LYE_RESIN, LYE);
	}

	private void initialize()
	{
		var ordersLayer = client.getWidget(COMPONENT_POTION_ORDERS_GROUP_ID, 0);
		if (ordersLayer == null || ordersLayer.isSelfHidden())
			return;

		LOGGER.debug("initialize plugin");
		inLab = true;
		unfinishedInInventory = countUnfinishedInInventory();
		recalculateGoalData();
		updatePotionOrders();
		updateMixGuidance();
		tryHighlightNextStation();
	}

	/**
	 * Outlines a lab object in the given colour. The aga lever is a wall decoration rather than a scenery object, so
	 * the tile's decorative object is checked when no scenery object matches.
	 */
	public void highlightObject(AlchemyObject alchemyObject, Color color)
	{
		var worldView = client.getTopLevelWorldView();

		if (worldView == null)
			return;

		var localPoint = LocalPoint.fromWorld(worldView, alchemyObject.coordinate());

		if (localPoint == null)
			return;

		var tiles = worldView.getScene().getTiles();
		var tile = tiles[worldView.getPlane()][localPoint.getSceneX()][localPoint.getSceneY()];

		for (var gameObject : tile.getGameObjects())
		{
			if (gameObject == null)
				continue;

			if (gameObject.getId() == alchemyObject.objectId())
			{
				highlightedObjects.put(alchemyObject,
						new HighlightedObject(gameObject, color, config.highlightBorderWidth(),
								config.highlightFeather()));
				return;
			}
		}

		var decorativeObject = tile.getDecorativeObject();

		if (decorativeObject != null && decorativeObject.getId() == alchemyObject.objectId())
			highlightedObjects.put(alchemyObject,
					new HighlightedObject(decorativeObject, color, config.highlightBorderWidth(),
							config.highlightFeather()));
	}

	/**
	 * Draws the Retort in the quick-action colour while Concentrate clicks still finish the potion sooner, and back in
	 * the station colour from the last tick on.
	 */
	private void updateRetortQuickActionHighlight()
	{
		if (!config.highlightQuickActionEvents() || !retortProgressTracker.potionLoaded())
			return;

		int progress = retortProgressTracker.currentProgress();
		if (StationGuard.concentrateWorthClicking(progress, StationGuard.RETORT_PROGRESS_MAX))
			highlightObject(AlchemyObject.RETORT, config.stationQuickActionHighlightColor());
		else if (progress > 0)
			resetStationHighlight(AlchemyObject.RETORT);
	}

	/**
	 * Puts a station back to its normal highlight after a quick action: the station colour, or no highlight at all
	 * when station highlights are off, so the quick-action colour never lingers.
	 */
	public void resetStationHighlight(AlchemyObject alchemyObject)
	{
		if (config.highlightStations())
			highlightObject(alchemyObject, config.stationHighlightColor());
		else
			unHighlightObject(alchemyObject);
	}

	public void unHighlightObject(AlchemyObject alchemyObject)
	{
		highlightedObjects.remove(alchemyObject);
	}

	private void unHighlightAllStations()
	{
		unHighlightObject(AlchemyObject.RETORT);
		unHighlightObject(AlchemyObject.ALEMBIC);
		unHighlightObject(AlchemyObject.AGITATOR);
	}

	/**
	 * Recomputes the lever state from the orders, vessel and inventory, then refreshes the lever,
	 * mixing vessel and conveyor belt highlights. A full vessel whose mix is for no order the player plans to make
	 * frees every lever instead of highlighting the vessel, so a mix left from before a relog or an order change, or
	 * one only a skipped order would take, can be corrected.
	 */
	private void updateMixGuidance()
	{
		if (!inLab)
			return;

		int[] slots = mixerSlots();
		int vesselReady = client.getVarbitValue(VARBIT_MIXING_VESSEL_POTION);
		if (MixGuidance.vesselEmpty(slots, vesselReady))
			vesselCollected = false;

		if (vesselCollected)
		{
			slots = new int[slots.length];
			vesselReady = 0;
		}

		PotionOrder target = MixGuidance.nextOrderToMix(potionOrders, recommendedSlots,
				potionsOnHand(!vesselCollected));
		PotionType targetType = target == null ? null : target.potionType();
		highlightLeverState = MixGuidance.leverState(targetType, slots, vesselReady);
		int[] slotsWithPending = MixGuidance.withPendingPulls(slots, pendingLeverPulls);
		leverState = MixGuidance.leverState(targetType, slotsWithPending, vesselReady);
		if (MixGuidance.vesselFull(slots, vesselReady) && !vesselMixPlanned(slots, vesselReady))
		{
			highlightLeverState = MixGuidance.LeverState.FREE;
			leverState = MixGuidance.LeverState.FREE;
		}

		updateLeverHighlights();

		boolean vesselReadyToMix = MixGuidance.vesselFull(slots, vesselReady) && vesselMixPlanned(slots, vesselReady);
		setReadyHighlight(AlchemyObject.MIXING_VESSEL, config.highlightMixingVessel() && vesselReadyToMix);

		boolean deliverable = MixGuidance.readyToDeliver(potionOrders, recommendedSlots, finishedPotionsOnHand());
		setReadyHighlight(AlchemyObject.CONVEYOR_BELT, config.highlightConveyorBelt() && deliverable);
	}

	private void setReadyHighlight(AlchemyObject alchemyObject, boolean highlighted)
	{
		if (highlighted)
			highlightObject(alchemyObject, READY_HIGHLIGHT_COLOR);
		else
			unHighlightObject(alchemyObject);
	}

	private void updateLeverHighlights()
	{
		for (PotionComponent component : PotionComponent.ENTRIES)
		{
			AlchemyObject lever = leverObject(component);
			if (shouldHighlightLever(component))
				highlightObject(lever, effectiveComponentColor(component));
			else
				unHighlightObject(lever);
		}
	}

	private boolean shouldHighlightLever(PotionComponent component)
	{
		if (!config.highlightLevers())
			return false;

		if (!config.highlightNextLeverOnly() || highlightLeverState.mode() == MixGuidance.LeverMode.FREE)
			return true;

		return highlightLeverState.mode() == MixGuidance.LeverMode.PULL && highlightLeverState.next() == component;
	}

	private static AlchemyObject leverObject(PotionComponent component)
	{
		switch (component)
		{
			case MOX:
				return MOX_LEVER;
			case AGA:
				return AGA_LEVER;
			default:
				return LYE_LEVER;
		}
	}

	private int[] mixerSlots()
	{
		return new int[]{
				client.getVarbitValue(VARBIT_MIXER_SLOT_0),
				client.getVarbitValue(VARBIT_MIXER_SLOT_1),
				client.getVarbitValue(VARBIT_MIXER_SLOT_2)
		};
	}

	/**
	 * Counts unfinished potions by type: in the inventory, loaded in a station, and (optionally) mixed in the vessel.
	 */
	private Map<PotionType, Integer> potionsOnHand(boolean includeVessel)
	{
		Map<PotionType, Integer> counts = new EnumMap<>(PotionType.class);
		var inventory = client.getItemContainer(InventoryID.INV);
		if (inventory != null)
		{
			for (var item : inventory.getItems())
			{
				var potionType = PotionType.fromItemId(item.getId());
				if (potionType != null && potionType.itemId() == item.getId())
					counts.merge(potionType, 1, Integer::sum);
			}
		}

		for (PotionType inStation : new PotionType[]{alembicPotionType, agitatorPotionType, retortPotionType})
		{
			if (inStation != null)
				counts.merge(inStation, 1, Integer::sum);
		}

		if (includeVessel)
		{
			var inVessel = PotionType.fromIdx(client.getVarbitValue(VARBIT_MIXING_VESSEL_POTION) - 1);
			if (inVessel != null)
				counts.merge(inVessel, 1, Integer::sum);
		}

		return counts;
	}

	private int countUnfinishedInInventory()
	{
		var inventory = client.getItemContainer(InventoryID.INV);
		if (inventory == null)
			return 0;

		int count = 0;
		for (var item : inventory.getItems())
		{
			var potionType = PotionType.fromItemId(item.getId());
			if (potionType != null && potionType.itemId() == item.getId())
				count++;
		}

		return count;
	}

	/**
	 * Counts finished (station-processed) potions in the inventory by type.
	 */
	private Map<PotionType, Integer> finishedPotionsOnHand()
	{
		Map<PotionType, Integer> counts = new EnumMap<>(PotionType.class);
		var inventory = client.getItemContainer(InventoryID.INV);
		if (inventory == null)
			return counts;

		for (var item : inventory.getItems())
		{
			var potionType = PotionType.fromItemId(item.getId());
			if (potionType != null && potionType.modifiedItemId() == item.getId())
				counts.merge(potionType, 1, Integer::sum);
		}

		return counts;
	}

	private void updatePotionOrders()
	{
		LOGGER.debug("Updating potion orders");
		var rebuilt = getPotionOrders();
		PotionOrder.keepFulfilled(potionOrders, rebuilt);
		PotionOrder.keepFulfilled(knownOrders, rebuilt);
		potionOrders = rebuilt;
		if (!rebuilt.isEmpty())
			knownOrders = rebuilt;

		var potionOrderSorting = config.potionOrderSorting();

		if (potionOrderSorting != PotionOrderSorting.VANILLA)
		{
			LOGGER.debug("Orders pre-sort: {}", potionOrders);
			potionOrders.sort(potionOrderSorting.comparator());
			LOGGER.debug("Sorted orders: {}", potionOrders);
		}

		updateMixGuidance();
		triggerPotionOrderUpdate();
		retryStationHighlight();
	}

	/**
	 * Highlights the next station when nothing is highlighted yet. The orders can arrive after the lab opens, for
	 * instance right after logging in, so the check made when the lab opened may have found no orders.
	 */
	private void retryStationHighlight()
	{
		if (!inLab || alembicPotionType != null || agitatorPotionType != null || retortPotionType != null)
			return;

		if (highlightedObjects.containsKey(AlchemyObject.ALEMBIC)
				|| highlightedObjects.containsKey(AlchemyObject.AGITATOR)
				|| highlightedObjects.containsKey(AlchemyObject.RETORT))
			return;

		tryHighlightNextStation();
	}

	/**
	 * Queues a fake change of the potion order varbit, which makes the client script that builds the order list run
	 * again. Does nothing before login: the client has no varp state to queue against yet, and the order list is built
	 * when its widget loads.
	 */
	public void triggerPotionOrderUpdate()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
			return;

		var varbitType = client.getVarbit(VARBIT_POTION_ORDER_1);

		if (varbitType != null)
			client.queueChangedVarp(varbitType.getIndex());
	}

	private void addResinText(Widget widget, int x, int varp, PotionComponent component)
	{
		var amount = client.getVarpValue(varp);
		var color = effectiveComponentColor(component).getRGB();

		widget.setText(amount + "")
				.setTextShadowed(true)
				.setTextColor(color)
				.setOriginalWidth(20)
				.setOriginalHeight(15)
				.setFontId(FontID.QUILL_8)
				.setOriginalY(0)
				.setOriginalX(x)
				.setYPositionMode(WidgetPositionMode.ABSOLUTE_BOTTOM)
				.setXTextAlignment(WidgetTextAlignment.CENTER)
				.setYTextAlignment(WidgetTextAlignment.CENTER);

		widget.revalidate();
		LOGGER.debug("adding resin text {} at {} with color {}", amount, x, color);
	}

	private void tryFulfillOrder(PotionType potionType, PotionModifier modifier)
	{
		for (var order : potionOrders)
		{
			if (order.potionType() == potionType && order.potionModifier() == modifier && !order.fulfilled())
			{
				LOGGER.debug("Order {} has been fulfilled", order);
				order.setFulfilled(true);
				break;
			}
		}
	}

	/**
	 * True when "Wait for full order" is on and a planned order still has no potion in the inventory or a station.
	 */
	private boolean waitingForFullOrder()
	{
		return config.waitForFullOrder()
				&& MixGuidance.nextOrderToMix(potionOrders, recommendedSlots, potionsOnHand(false)) != null;
	}

	private void refreshStationHighlight()
	{
		if (!config.highlightStations() || waitingForFullOrder())
			unHighlightAllStations();
		else
			tryHighlightNextStation();
	}

	private void tryHighlightNextStation()
	{
		if (!config.highlightStations() || waitingForFullOrder())
			return;

		var inventory = client.getItemContainer(InventoryID.INV);

		if (inventory == null)
			return;

		var order = MixGuidance.orderToProcess(potionOrders, recommendedSlots,
				potionType -> inventory.contains(potionType.itemId()));
		if (order == null)
			return;

		LOGGER.debug("Highlighting station for order {}", order);
		highlightObject(order.potionModifier().alchemyObject(), config.stationHighlightColor());
	}

	private List<PotionOrder> getPotionOrders()
	{
		var potionOrders = new ArrayList<PotionOrder>(3);

		for (int orderIdx = 0; orderIdx < 3; orderIdx++)
		{
			var potionType = getPotionType(orderIdx);
			var potionModifier = getPotionModifier(orderIdx);

			if (potionType == null || potionModifier == null)
				continue;

			potionOrders.add(new PotionOrder(orderIdx, potionType, potionModifier));
		}

		return potionOrders;
	}

	private PotionType getPotionType(int orderIdx)
	{
		if (orderIdx == 0)
			return PotionType.fromIdx(client.getVarbitValue(VARBIT_POTION_ORDER_1) - 1);
		else if (orderIdx == 1)
			return PotionType.fromIdx(client.getVarbitValue(VARBIT_POTION_ORDER_2) - 1);
		else if (orderIdx == 2)
			return PotionType.fromIdx(client.getVarbitValue(VARBIT_POTION_ORDER_3) - 1);
		else
			return null;
	}

	private PotionModifier getPotionModifier(int orderIdx)
	{
		if (orderIdx == 0)
			return PotionModifier.from(client.getVarbitValue(VARBIT_POTION_MODIFIER_1) - 1);
		else if (orderIdx == 1)
			return PotionModifier.from(client.getVarbitValue(VARBIT_POTION_MODIFIER_2) - 1);
		else if (orderIdx == 2)
			return PotionModifier.from(client.getVarbitValue(VARBIT_POTION_MODIFIER_3) - 1);
		else
			return null;
	}

	public Goal getGoal()
	{
		return goal;
	}

	private void recalculateGoalData()
	{
		goal.recalculate(config, client);
		clientThread.invokeLater(this::refreshRewardEstimates);
	}

	/** Brings the saved reward order up to date with the ticked rewards, saving it when it changed. */
	private void syncRewardPriority()
	{
		String saved = config.rewardPriority() == null ? "" : config.rewardPriority();
		List<RewardItem> ticked = Goal.collectSelections(config)
				.stream()
				.map(selection -> selection.item)
				.collect(Collectors.toList());
		String order = RewardPriority.format(RewardPriority.reconcile(RewardPriority.parse(saved), ticked));
		if (!order.equals(saved))
			configManager.setConfiguration(AnotherMixologyConfig.CONFIG_GROUP, RewardPriority.CONFIG_KEY, order);
	}

	/**
	 * Adds "Move up" and "Move down" for each tracked reward to the right-click menu while the mouse is over the
	 * rewards panel. They go just above Cancel, so a left-click on the panel still does what it would without them.
	 */
	@Subscribe
	public void onPostMenuSort(PostMenuSort event)
	{
		var estimates = rewardEstimates;
		if (client.isMenuOpen() || estimates == null || !isInLabRegion())
			return;

		var mouse = client.getMouseCanvasPosition();
		if (!goalInfoBoxOverlay.getBounds().contains(mouse.getX(), mouse.getY()))
			return;

		List<RewardRows.Row> rewards = estimates.rows
				.stream()
				.filter(row -> row.item != null)
				.collect(Collectors.toList());
		for (int i = 0; i < rewards.size(); i++)
		{
			if (i > 0)
				addMoveEntry("Move up", rewards.get(i), -1);

			if (i < rewards.size() - 1)
				addMoveEntry("Move down", rewards.get(i), 1);
		}
	}

	/** Adds one move entry just above Cancel; entries added one after another read top to bottom. */
	private void addMoveEntry(String option, RewardRows.Row row, int direction)
	{
		var menu = client.getMenu();
		int index = Math.min(1, menu.getMenuEntries().length);
		MenuEntry entry = menu.createMenuEntry(index);
		entry.setOption(option);
		entry.setTarget(ColorUtil.wrapWithColorTag(row.label, JagexColors.MENU_TARGET));
		entry.setType(MenuAction.RUNELITE);
		entry.onClick(clicked -> moveReward(row.item, direction));
	}

	/** Moves a tracked reward one place up (-1) or down (+1) in the saved order, from the panel's right-click menu. */
	void moveReward(RewardItem item, int direction)
	{
		var order = RewardPriority.move(RewardPriority.parse(config.rewardPriority()), item, direction);
		configManager.setConfiguration(AnotherMixologyConfig.CONFIG_GROUP, RewardPriority.CONFIG_KEY,
				RewardPriority.format(order));
	}

	/** Returns the rewards panel rows and their time estimates, or null when no reward is tracked. */
	RewardEstimates rewardEstimates()
	{
		return rewardEstimates;
	}

	/**
	 * Times the order set just delivered, from the previous delivery, for the pace. A set with no potion done is an
	 * order refresh without a delivery and is skipped.
	 */
	private void recordDelivery()
	{
		int delivered = (int) potionOrders.stream()
				.filter(PotionOrder::fulfilled)
				.count();
		if (delivered == 0)
			return;

		int tick = client.getTickCount();
		if (lastDeliveryTick >= 0)
			paceTracker.addSet((tick - lastDeliveryTick) * SECONDS_PER_TICK, delivered);

		lastDeliveryTick = tick;
		refreshRewardEstimates();
	}

	/**
	 * Rebuilds the rewards panel rows and, when the resin, rewards, strategy or pace changed, works out their time
	 * estimates in the background. Each row's time shows as soon as it is done, and the panel counts it down until
	 * the next change.
	 */
	private void refreshRewardEstimates()
	{
		var selections = Goal.collectSelections(config);
		if (selections.isEmpty())
		{
			rewardEstimates = null;
			rewardEstimatesKey = "";
			return;
		}

		int[] resin = {
				client.getVarpValue(VARP_MOX_RESIN),
				client.getVarpValue(VARP_AGA_RESIN),
				client.getVarpValue(VARP_LYE_RESIN)
		};
		var rows = RewardRows.build(selections, resin, config.rewardOrder());
		var strategy = config.recommendationStrategy();
		var pace = paceTracker.pace();
		var key = new StringBuilder(Arrays.toString(resin))
				.append(strategy)
				.append(pace.secondsPerPotion)
				.append(pace.secondsPerDelivery);
		for (var row : rows)
			key.append(Arrays.toString(row.target));

		if (key.toString().equals(rewardEstimatesKey))
			return;

		rewardEstimatesKey = key.toString();
		double[] seconds = new double[rows.size()];
		for (int i = 0; i < rows.size(); i++)
			seconds[i] = rows.get(i).ready() ? 0 : Double.NaN;

		long madeAt = playedMillis();
		rewardEstimates = new RewardEstimates(rows, seconds.clone(), madeAt);
		if (rewardEstimateTask != null)
			rewardEstimateTask.cancel(true);

		rewardEstimateTask = executor.submit(() ->
		{
			for (int i = 0; i < rows.size(); i++)
			{
				if (Thread.currentThread().isInterrupted())
					return;

				if (!rows.get(i).ready())
					seconds[i] = RewardTimeEstimator.seconds(strategy, resin, rows.get(i).target, pace);

				rewardEstimates = new RewardEstimates(rows, seconds.clone(), madeAt);
			}
		});
	}

	/** A lab object that is outlined, with the colour, outline width and feather to draw it with. */
	public static class HighlightedObject
	{
		private final TileObject object;
		private final Color color;
		private final int outlineWidth;
		private final int feather;

		private HighlightedObject(TileObject object, Color color, int outlineWidth, int feather)
		{
			this.object = object;
			this.color = color;
			this.outlineWidth = outlineWidth;
			this.feather = feather;
		}

		public TileObject object()
		{
			return object;
		}

		public Color color()
		{
			return color;
		}

		public int outlineWidth()
		{
			return outlineWidth;
		}

		public int feather()
		{
			return feather;
		}
	}
}
