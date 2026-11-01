package com.oveduumnakal.anothermixology;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** Tests for {@link StationGuard}. */
public class StationGuardTest
{
	@Test
	public void stationFromTargetStripsTagsAndMatchesNames()
	{
		assertEquals(AlchemyObject.RETORT, StationGuard.stationFromTarget("<col=00ff00>Retort</col>"));
		assertEquals(AlchemyObject.ALEMBIC, StationGuard.stationFromTarget("Alembic"));
		assertEquals(AlchemyObject.AGITATOR, StationGuard.stationFromTarget("Agitator"));
		assertNull(StationGuard.stationFromTarget("Bank booth"));
		assertNull(StationGuard.stationFromTarget(null));
	}

	@Test
	public void occupiedStationIsAlwaysNeeded()
	{
		assertTrue(StationGuard.isStationNeeded(
				AlchemyObject.RETORT, true, Collections.emptyList(), Collections.emptySet()));
	}

	@Test
	public void emptyStationNeededWhenAnUnfulfilledOrderMatchesAHeldPotion()
	{
		PotionType type = firstConcentrateType();
		PotionOrder order = new PotionOrder(0, type, PotionModifier.CONCENTRATED);
		List<PotionOrder> orders = Collections.singletonList(order);
		Set<Integer> held = new HashSet<>();
		held.add(type.itemId());

		assertTrue(StationGuard.isStationNeeded(AlchemyObject.RETORT, false, orders, held));
	}

	@Test
	public void emptyStationNotNeededWhenOrderFulfilled()
	{
		PotionType type = firstConcentrateType();
		PotionOrder order = new PotionOrder(0, type, PotionModifier.CONCENTRATED);
		order.setFulfilled(true);
		List<PotionOrder> orders = Collections.singletonList(order);
		Set<Integer> held = new HashSet<>();
		held.add(type.itemId());

		assertFalse(StationGuard.isStationNeeded(AlchemyObject.RETORT, false, orders, held));
	}

	@Test
	public void emptyStationNotNeededWhenPotionNotHeld()
	{
		PotionType type = firstConcentrateType();
		PotionOrder order = new PotionOrder(0, type, PotionModifier.CONCENTRATED);
		List<PotionOrder> orders = Collections.singletonList(order);

		assertFalse(StationGuard.isStationNeeded(
				AlchemyObject.RETORT, false, orders, Collections.emptySet()));
	}

	/**
	 * With a maximum of 16, passive fill completes from 15 on the next tick, so the cap blocks at 15. At 14 a click
	 * still finishes one tick sooner, so it is let through.
	 */
	@Test
	public void concentrateCapBlocksOnlyOnTheLastTick()
	{
		assertFalse(StationGuard.concentrateCapReached(13, 16));
		assertFalse(StationGuard.concentrateCapReached(14, 16));
		assertTrue(StationGuard.concentrateCapReached(15, 16));
	}

	/** The quick-action colour shows from the first progress up to the last tick. */
	@Test
	public void concentrateWorthClickingUntilTheLastTick()
	{
		assertFalse(StationGuard.concentrateWorthClicking(0, 16));
		assertTrue(StationGuard.concentrateWorthClicking(1, 16));
		assertTrue(StationGuard.concentrateWorthClicking(14, 16));
		assertFalse(StationGuard.concentrateWorthClicking(15, 16));
	}

	@Test
	public void runningLockBlocksOnlyWhileRunningWithoutQuickAction()
	{
		assertTrue(StationGuard.runningLockBlocks(true, true, false));
		assertFalse(StationGuard.runningLockBlocks(true, true, true));
		assertFalse(StationGuard.runningLockBlocks(true, false, false));
		assertFalse(StationGuard.runningLockBlocks(false, true, false));
	}

	/** Any potion type works, because the modifier (concentrated) drives the station. */
	private static PotionType firstConcentrateType()
	{
		return PotionType.values()[0];
	}
}
