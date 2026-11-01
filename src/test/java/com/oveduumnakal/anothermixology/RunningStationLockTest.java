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

import net.runelite.api.Client;
import net.runelite.api.GraphicsObject;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.SpotanimID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.overlay.OverlayManager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the Agitator and Alembic running locks through the plugin, with a mocked client: clicks on a running station
 * are consumed except during its quick-action window, which opens on the spot animation and closes on the progress
 * updates that follow. A station counts as running only while the player's last click went to it.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class RunningStationLockTest
{
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
		when(config.lockAgitatorWhileRunning()).thenReturn(true);
		when(config.lockAlembicWhileRunning()).thenReturn(true);
		when(client.getWidget(InterfaceID.MM_OVERLAY, 0)).thenReturn(mock(Widget.class));

		WidgetLoaded loaded = new WidgetLoaded();
		loaded.setGroupId(InterfaceID.MM_OVERLAY);
		plugin.onWidgetLoaded(loaded);
	}

	@Test
	public void idleStationsAreNotLocked()
	{
		assertFalse(click("Agitator", AlchemyObject.AGITATOR).isConsumed());
		assertFalse(click("Alembic", AlchemyObject.ALEMBIC).isConsumed());
	}

	@Test
	public void runningAgitatorIsLockedOutsideTheQuickAction()
	{
		start("Agitator", AlchemyObject.AGITATOR, VarbitID.MM_LAB_AGITATOR_POTION);

		assertTrue(click("Agitator", AlchemyObject.AGITATOR).isConsumed());
	}

	/** Walking away pauses the station with its potion still in it; the next click must resume it. */
	@Test
	public void pausedStationCanBeResumed()
	{
		start("Agitator", AlchemyObject.AGITATOR, VarbitID.MM_LAB_AGITATOR_POTION);
		start("Alembic", AlchemyObject.ALEMBIC, VarbitID.MM_LAB_ALEMBIC_POTION);
		walk();

		assertFalse(click("Agitator", AlchemyObject.AGITATOR).isConsumed());
		assertTrue(click("Agitator", AlchemyObject.AGITATOR).isConsumed());
		assertFalse(click("Alembic", AlchemyObject.ALEMBIC).isConsumed());
	}

	/** Interface clicks, such as minimising the chat box, do not stop a station, so the lock stays. */
	@Test
	public void interfaceClickKeepsTheLock()
	{
		start("Agitator", AlchemyObject.AGITATOR, VarbitID.MM_LAB_AGITATOR_POTION);
		menuClick(MenuAction.CC_OP);

		assertTrue(click("Agitator", AlchemyObject.AGITATOR).isConsumed());
	}

	/** Clicking another object stops the station, like walking away. */
	@Test
	public void objectClickPausesTheStation()
	{
		start("Agitator", AlchemyObject.AGITATOR, VarbitID.MM_LAB_AGITATOR_POTION);
		click("Mixing vessel", AlchemyObject.MIXING_VESSEL);

		assertFalse(click("Agitator", AlchemyObject.AGITATOR).isConsumed());
	}

	/** A blocked click does not count as working the clicked station or as clicking away. */
	@Test
	public void blockedClickKeepsTheLock()
	{
		start("Agitator", AlchemyObject.AGITATOR, VarbitID.MM_LAB_AGITATOR_POTION);
		click("Agitator", AlchemyObject.AGITATOR);

		assertTrue(click("Agitator", AlchemyObject.AGITATOR).isConsumed());
	}

	/** The Agitator's window stays open for two progress updates after the spot animation. */
	@Test
	public void agitatorQuickActionWindowLetsClicksThroughForTwoUpdates()
	{
		start("Agitator", AlchemyObject.AGITATOR, VarbitID.MM_LAB_AGITATOR_POTION);
		spotAnim(SpotanimID.VFX_MACHINERY_ALCHEMY01_AGITATOR01);
		assertFalse(click("Agitator", AlchemyObject.AGITATOR).isConsumed());

		tick(1);
		varbit(VarbitID.MM_AGITATOR_PROGRESS, 5);
		assertFalse(click("Agitator", AlchemyObject.AGITATOR).isConsumed());

		tick(2);
		varbit(VarbitID.MM_AGITATOR_PROGRESS, 6);
		assertTrue(click("Agitator", AlchemyObject.AGITATOR).isConsumed());
	}

	/** The Alembic's window closes on the first progress update after the spot animation. */
	@Test
	public void alembicQuickActionWindowLetsClicksThroughForOneUpdate()
	{
		start("Alembic", AlchemyObject.ALEMBIC, VarbitID.MM_LAB_ALEMBIC_POTION);
		spotAnim(SpotanimID.VFX_MACHINERY_ALCHEMY01_ALEMBIC01);
		assertFalse(click("Alembic", AlchemyObject.ALEMBIC).isConsumed());

		varbit(VarbitID.MM_ALEMBIC_PROGRESS, 5);
		assertTrue(click("Alembic", AlchemyObject.ALEMBIC).isConsumed());
	}

	/** The Alembic's quick action lets one click through; a second click in the same window is consumed. */
	@Test
	public void alembicQuickActionLetsOneClickThrough()
	{
		start("Alembic", AlchemyObject.ALEMBIC, VarbitID.MM_LAB_ALEMBIC_POTION);
		spotAnim(SpotanimID.VFX_MACHINERY_ALCHEMY01_ALEMBIC01);

		assertFalse(click("Alembic", AlchemyObject.ALEMBIC).isConsumed());
		assertTrue(click("Alembic", AlchemyObject.ALEMBIC).isConsumed());
	}

	/** The Agitator's two-tick quick action lets one click through per tick. */
	@Test
	public void agitatorQuickActionLetsOneClickThroughPerTick()
	{
		start("Agitator", AlchemyObject.AGITATOR, VarbitID.MM_LAB_AGITATOR_POTION);
		spotAnim(SpotanimID.VFX_MACHINERY_ALCHEMY01_AGITATOR01);

		assertFalse(click("Agitator", AlchemyObject.AGITATOR).isConsumed());
		assertTrue(click("Agitator", AlchemyObject.AGITATOR).isConsumed());

		tick(1);
		varbit(VarbitID.MM_AGITATOR_PROGRESS, 5);
		assertFalse(click("Agitator", AlchemyObject.AGITATOR).isConsumed());
		assertTrue(click("Agitator", AlchemyObject.AGITATOR).isConsumed());
	}

	/** Each new quick action gives the station its clicks again. */
	@Test
	public void nextQuickActionRestoresTheClicks()
	{
		start("Alembic", AlchemyObject.ALEMBIC, VarbitID.MM_LAB_ALEMBIC_POTION);
		spotAnim(SpotanimID.VFX_MACHINERY_ALCHEMY01_ALEMBIC01);
		click("Alembic", AlchemyObject.ALEMBIC);
		tick(1);
		varbit(VarbitID.MM_ALEMBIC_PROGRESS, 5);

		tick(2);
		spotAnim(SpotanimID.VFX_MACHINERY_ALCHEMY01_ALEMBIC01);
		assertFalse(click("Alembic", AlchemyObject.ALEMBIC).isConsumed());
	}

	@Test
	public void lockOffLetsRunningStationClicksThrough()
	{
		when(config.lockAgitatorWhileRunning()).thenReturn(false);
		start("Agitator", AlchemyObject.AGITATOR, VarbitID.MM_LAB_AGITATOR_POTION);

		assertFalse(click("Agitator", AlchemyObject.AGITATOR).isConsumed());
	}

	/** Clicks an idle station and loads a potion into it, as the game does. */
	private void start(String target, AlchemyObject object, int potionVarbit)
	{
		assertFalse(click(target, object).isConsumed());
		varbit(potionVarbit, 1);
	}

	private void tick(int tickCount)
	{
		when(client.getTickCount()).thenReturn(tickCount);
	}

	private void walk()
	{
		menuClick(MenuAction.WALK);
	}

	private void menuClick(MenuAction action)
	{
		MenuEntry entry = mock(MenuEntry.class);
		when(entry.getType()).thenReturn(action);
		plugin.onMenuOptionClicked(new MenuOptionClicked(entry));
	}

	private void varbit(int varbitId, int value)
	{
		VarbitChanged event = new VarbitChanged();
		event.setVarbitId(varbitId);
		event.setVarpId(-1);
		event.setValue(value);
		plugin.onVarbitChanged(event);
	}

	private void spotAnim(int id)
	{
		GraphicsObject graphicsObject = mock(GraphicsObject.class);
		when(graphicsObject.getId()).thenReturn(id);
		plugin.onGraphicsObjectCreated(new GraphicsObjectCreated(graphicsObject));
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
}
