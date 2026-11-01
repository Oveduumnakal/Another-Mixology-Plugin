package com.oveduumnakal.anothermixology;

import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;

import com.google.inject.Guice;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.GraphicsObject;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.VarbitComposition;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.SpotanimID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.ui.overlay.OverlayManager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Plugin-level tests with a mocked client and config: the click guards, the reaction to setting changes, the quick
 * action and the reward notification. The lab is entered through the orders widget loading, as in game.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class AnotherMixologyPluginLogicTest
{
	private static final int FOUND_GEM_SOUND = 2655;
	private static final int MOX_PASTE = 1;
	private static final int LYE_PASTE = 3;

	@Mock
	@Bind
	private Client client;

	@Mock
	@Bind
	private AnotherMixologyConfig config;

	@Mock
	@Bind
	private ConfigManager configManager;

	@Mock
	@Bind
	private OverlayManager overlayManager;

	@Mock
	@Bind
	private Notifier notifier;

	@Mock
	@Bind
	private ClientThread clientThread;

	@Mock
	@Bind
	private ScheduledExecutorService executor;

	@Mock
	@Bind
	private AnotherMixologyOverlay overlay;

	@Mock
	@Bind
	private InventoryPotionOverlay potionOverlay;

	@Mock
	@Bind
	private OrdersPanelOverlay ordersPanelOverlay;

	@Mock
	@Bind
	private GoalInfoBoxOverlay goalInfoBoxOverlay;

	@Inject
	private AnotherMixologyPlugin plugin;

	@Before
	public void setUp()
	{
		Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
		when(config.potionOrderSorting()).thenReturn(PotionOrderSorting.VANILLA);
		when(config.recommendationStrategy()).thenReturn(RecommendationStrategy.EXPECTED_VALUE);
		when(client.getWidget(InterfaceID.MM_OVERLAY, 0)).thenReturn(mock(Widget.class));

		WidgetLoaded loaded = new WidgetLoaded();
		loaded.setGroupId(InterfaceID.MM_OVERLAY);
		plugin.onWidgetLoaded(loaded);
	}

	@Test
	public void retortClickIsBlockedOnTheLastTickWithTheCapOn()
	{
		when(config.capConcentrate()).thenReturn(true);
		varbit(VarbitID.MM_LAB_RETORT_POTION, 1);
		varbit(VarbitID.MM_RETORT_PROGRESS, 15);

		assertTrue(click("Retort", AlchemyObject.RETORT).isConsumed());
	}

	@Test
	public void retortClickIsAllowedWithTheCapOff()
	{
		varbit(VarbitID.MM_LAB_RETORT_POTION, 1);
		varbit(VarbitID.MM_RETORT_PROGRESS, 15);

		assertFalse(click("Retort", AlchemyObject.RETORT).isConsumed());
	}

	@Test
	public void retortClickIsAllowedEarlyWithTheCapOn()
	{
		when(config.capConcentrate()).thenReturn(true);
		varbit(VarbitID.MM_LAB_RETORT_POTION, 1);
		varbit(VarbitID.MM_RETORT_PROGRESS, 5);

		assertFalse(click("Retort", AlchemyObject.RETORT).isConsumed());
	}

	@Test
	public void idleLockBlocksAnEmptyStationNoOrderNeeds()
	{
		when(config.lockAlembicWhenIdle()).thenReturn(true);

		assertTrue(click("<col=ffff>Alembic</col>", AlchemyObject.ALEMBIC).isConsumed());
		assertFalse(click("Agitator", AlchemyObject.AGITATOR).isConsumed());
	}

	@Test
	public void idleLockAllowsAStationThatIsInUse()
	{
		when(config.lockAlembicWhenIdle()).thenReturn(true);
		varbit(VarbitID.MM_LAB_ALEMBIC_POTION, 1);

		assertFalse(click("Alembic", AlchemyObject.ALEMBIC).isConsumed());
	}

	@Test
	public void clicksOutsideTheLabAreNeverBlocked()
	{
		when(config.lockAlembicWhenIdle()).thenReturn(true);
		WidgetClosed closed = new WidgetClosed(InterfaceID.MM_OVERLAY, 0, false);
		plugin.onWidgetClosed(closed);

		assertFalse(click("Alembic", AlchemyObject.ALEMBIC).isConsumed());
	}

	@Test
	public void onlyAStrategyChangeCreatesANewPolicy()
	{
		configChanged("accessibilityMoxColor");
		verify(config, never()).recommendationStrategy();

		configChanged("recommendationStrategy");
		verify(config).recommendationStrategy();
	}

	@Test
	public void alembicQuickActionPlaysTheFixedSoundOnlyWhileCrystallising()
	{
		when(config.highlightQuickActionEvents()).thenReturn(true);
		when(config.soundEffectAlembic()).thenReturn(true);

		alembicSpotAnim();
		verify(client, never()).playSoundEffect(FOUND_GEM_SOUND);

		varbit(VarbitID.MM_LAB_ALEMBIC_POTION, 1);
		alembicSpotAnim();
		verify(client).playSoundEffect(FOUND_GEM_SOUND);
	}

	@Test
	public void rewardNotificationFiresOnceWhenTheGoalIsReached()
	{
		when(config.trackPrescriptionGoggles()).thenReturn(true);
		when(client.getVarpValue(AnotherMixologyPlugin.VARP_MOX_RESIN)).thenReturn(9000);
		when(client.getVarpValue(AnotherMixologyPlugin.VARP_AGA_RESIN)).thenReturn(9000);
		when(client.getVarpValue(AnotherMixologyPlugin.VARP_LYE_RESIN)).thenReturn(9000);
		resinChanged();
		verify(notifier, never()).notify(any(), anyString());

		when(client.getVarpValue(AnotherMixologyPlugin.VARP_LYE_RESIN)).thenReturn(9350);
		resinChanged();
		resinChanged();
		verify(notifier, times(1)).notify(any(), anyString());
	}

	@Test
	public void potionOrderUpdateIsNotQueuedBeforeLogin()
	{
		orderVarbitAtIndex(7);
		when(client.getGameState()).thenReturn(GameState.LOGIN_SCREEN);

		plugin.triggerPotionOrderUpdate();

		verify(client, never()).queueChangedVarp(anyInt());
	}

	@Test
	public void potionOrderUpdateIsQueuedWhileLoggedIn()
	{
		orderVarbitAtIndex(7);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);

		plugin.triggerPotionOrderUpdate();

		verify(client).queueChangedVarp(7);
	}

	@Test
	public void digweedMarksSurviveALogoutAndLogin()
	{
		orders(PotionType.MARLEYS_MOONLIGHT, PotionType.LIPLACK_LIQUOR);
		enterLab();
		digweedMixedInto("Marley's moonlight");
		assertTrue(order(PotionType.MARLEYS_MOONLIGHT).improved());

		gameState(GameState.LOGIN_SCREEN);
		gameState(GameState.LOGGING_IN);
		orders();
		varbit(VarbitID.MM_LAB_ORDER_1_TYPE, 0);
		enterLab();
		gameState(GameState.LOGGED_IN);
		orders(PotionType.MARLEYS_MOONLIGHT, PotionType.LIPLACK_LIQUOR);
		enterLab();

		assertTrue(order(PotionType.MARLEYS_MOONLIGHT).improved());
	}

	@Test
	public void aDeliveryWhileLoggedInStillClearsTheDigweedMarks()
	{
		orders(PotionType.MARLEYS_MOONLIGHT, PotionType.LIPLACK_LIQUOR);
		enterLab();
		digweedMixedInto("Marley's moonlight");

		varbit(VarbitID.MM_LAB_ORDER_1_TYPE, 0);
		enterLab();

		assertFalse(order(PotionType.MARLEYS_MOONLIGHT).improved());
	}

	@Test
	public void theLoginReloadEndsAfterAFewTicks()
	{
		orders(PotionType.MARLEYS_MOONLIGHT, PotionType.LIPLACK_LIQUOR);
		enterLab();
		digweedMixedInto("Marley's moonlight");
		gameState(GameState.LOGIN_SCREEN);
		gameState(GameState.LOGGED_IN);
		for (int tick = 0; tick < 3; tick++)
			plugin.onGameTick(new GameTick());

		varbit(VarbitID.MM_LAB_ORDER_1_TYPE, 0);
		enterLab();

		assertFalse(order(PotionType.MARLEYS_MOONLIGHT).improved());
	}

	/** Sets the order varbits to the given potions, each with the Agitator's modifier, and the rest to none. */
	private void orders(PotionType... types)
	{
		int[] typeVarbits = {VarbitID.MM_LAB_ORDER_1_TYPE, VarbitID.MM_LAB_ORDER_2_TYPE, VarbitID.MM_LAB_ORDER_3_TYPE};
		int[] modifierVarbits = {
				VarbitID.MM_LAB_ORDER_1_MODIFIER, VarbitID.MM_LAB_ORDER_2_MODIFIER, VarbitID.MM_LAB_ORDER_3_MODIFIER
		};
		for (int slot = 0; slot < 3; slot++)
		{
			boolean set = slot < types.length;
			when(client.getVarbitValue(typeVarbits[slot])).thenReturn(set ? types[slot].ordinal() + 1 : 0);
			when(client.getVarbitValue(modifierVarbits[slot]))
					.thenReturn(set ? PotionModifier.HOMOGENOUS.ordinal() + 1 : 0);
		}
	}

	/** Loads the orders widget, as on entering the lab or logging back in, which rebuilds the orders. */
	private void enterLab()
	{
		WidgetLoaded loaded = new WidgetLoaded();
		loaded.setGroupId(InterfaceID.MM_OVERLAY);
		plugin.onWidgetLoaded(loaded);
	}

	private void digweedMixedInto(String potion)
	{
		ChatMessage message = new ChatMessage();
		message.setType(ChatMessageType.GAMEMESSAGE);
		message.setMessage("You mix the digweed into the @mes_hl_pur@" + potion
				+ "</col>, drastically improving its quality!");
		plugin.onChatMessage(message);
	}

	private void gameState(GameState state)
	{
		GameStateChanged event = new GameStateChanged();
		event.setGameState(state);
		plugin.onGameStateChanged(event);
	}

	private PotionOrder order(PotionType type)
	{
		return plugin.potionOrders()
				.stream()
				.filter(order -> order.potionType() == type)
				.findFirst()
				.orElseThrow();
	}

	@Test
	public void theGoalIsWorkedOutOnEnteringTheLab()
	{
		when(config.trackReagentPouch()).thenReturn(true);
		when(client.getVarpValue(AnotherMixologyPlugin.VARP_MOX_RESIN)).thenReturn(1000);

		enterLab();

		assertTrue(plugin.getGoal().isMultiMode());
	}

	@Test
	public void everyLeverIsFreeWhenTheFullVesselMatchesNoOrder()
	{
		when(config.leverClickProtection()).thenReturn(true);
		orders(PotionType.MARLEYS_MOONLIGHT, PotionType.LIPLACK_LIQUOR);
		vessel(MOX_PASTE, MOX_PASTE, MOX_PASTE);
		enterLab();

		assertFalse(click("Mox lever", AlchemyObject.MOX_LEVER).isConsumed());
		assertFalse(click("Lye lever", AlchemyObject.LYE_LEVER).isConsumed());
	}

	@Test
	public void leversStayBlockedWhenTheFullVesselMakesAnOrder()
	{
		when(config.leverClickProtection()).thenReturn(true);
		orders(PotionType.MARLEYS_MOONLIGHT, PotionType.LIPLACK_LIQUOR);
		vessel(MOX_PASTE, MOX_PASTE, LYE_PASTE);
		enterLab();

		assertTrue(click("Mox lever", AlchemyObject.MOX_LEVER).isConsumed());
	}

	/** Sets the paste in each mixer slot (1 Mox, 2 Aga, 3 Lye) with the vessel not yet mixed. */
	private void vessel(int... slots)
	{
		int[] slotVarbits = {VarbitID.MM_LAB_MIXER_SLOT_0, VarbitID.MM_LAB_MIXER_SLOT_1, VarbitID.MM_LAB_MIXER_SLOT_2};
		for (int slot = 0; slot < slotVarbits.length; slot++)
			when(client.getVarbitValue(slotVarbits[slot])).thenReturn(slots[slot]);

		when(client.getVarbitValue(VarbitID.MM_LAB_VESSEL_READY)).thenReturn(0);
	}

	private void orderVarbitAtIndex(int varpIndex)
	{
		VarbitComposition varbitType = mock(VarbitComposition.class);
		when(varbitType.getIndex()).thenReturn(varpIndex);
		when(client.getVarbit(VarbitID.MM_LAB_ORDER_1_TYPE)).thenReturn(varbitType);
	}

	private void varbit(int varbitId, int value)
	{
		VarbitChanged event = new VarbitChanged();
		event.setVarbitId(varbitId);
		event.setVarpId(-1);
		event.setValue(value);
		plugin.onVarbitChanged(event);
	}

	private void resinChanged()
	{
		VarbitChanged event = new VarbitChanged();
		event.setVarbitId(-1);
		event.setVarpId(AnotherMixologyPlugin.VARP_LYE_RESIN);
		plugin.onVarbitChanged(event);
	}

	private MenuOptionClicked click(String target, AlchemyObject object)
	{
		MenuEntry entry = mock(MenuEntry.class);
		when(entry.getType()).thenReturn(MenuAction.GAME_OBJECT_FIRST_OPTION);
		when(entry.getIdentifier()).thenReturn(object.objectId());
		when(entry.getTarget()).thenReturn(target);
		MenuOptionClicked event = new MenuOptionClicked(entry);
		plugin.onMenuOptionClicked(event);
		return event;
	}

	private void configChanged(String key)
	{
		ConfigChanged event = new ConfigChanged();
		event.setGroup(AnotherMixologyConfig.CONFIG_GROUP);
		event.setKey(key);
		plugin.onConfigChanged(event);
	}

	private void alembicSpotAnim()
	{
		GraphicsObject graphicsObject = mock(GraphicsObject.class);
		when(graphicsObject.getId()).thenReturn(SpotanimID.VFX_MACHINERY_ALCHEMY01_ALEMBIC01);
		plugin.onGraphicsObjectCreated(new GraphicsObjectCreated(graphicsObject));
	}
}
