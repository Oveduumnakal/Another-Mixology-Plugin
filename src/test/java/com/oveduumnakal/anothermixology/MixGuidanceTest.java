package com.oveduumnakal.anothermixology;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import static com.oveduumnakal.anothermixology.PotionComponent.AGA;
import static com.oveduumnakal.anothermixology.PotionComponent.LYE;
import static com.oveduumnakal.anothermixology.PotionComponent.MOX;
import static com.oveduumnakal.anothermixology.PotionType.AQUALUX_AMALGAM;
import static com.oveduumnakal.anothermixology.PotionType.MAMMOTH_MIGHT_MIX;
import static com.oveduumnakal.anothermixology.PotionType.MIXALOT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link MixGuidance}.
 */
public class MixGuidanceTest
{
	private static final int EMPTY = 0;
	private static final int M = 1;
	private static final int A = 2;
	private static final int L = 3;

	private static PotionOrder order(int idx, PotionType type)
	{
		return new PotionOrder(idx, type, PotionModifier.CRYSTALISED);
	}

	private static Map<PotionType, Integer> counts(PotionType... types)
	{
		Map<PotionType, Integer> map = new EnumMap<>(PotionType.class);
		for (PotionType type : types)
			map.merge(type, 1, Integer::sum);

		return map;
	}

	@Test
	public void orderToProcessPrefersThePlannedOrderOfTheSameType()
	{
		PotionOrder skipped = new PotionOrder(2, MAMMOTH_MIGHT_MIX, PotionModifier.CRYSTALISED);
		PotionOrder planned = new PotionOrder(0, MAMMOTH_MIGHT_MIX, PotionModifier.HOMOGENOUS);
		List<PotionOrder> orders = List.of(skipped, planned);

		assertSame(planned, MixGuidance.orderToProcess(orders, Set.of(0), type -> type == MAMMOTH_MIGHT_MIX));

		planned.setFulfilled(true);
		assertSame(skipped, MixGuidance.orderToProcess(orders, Set.of(0), type -> type == MAMMOTH_MIGHT_MIX));
		assertNull(MixGuidance.orderToProcess(orders, Set.of(0), type -> type == MIXALOT));
	}

	@Test
	public void nextOrderSkipsCoveredFulfilledAndSkippedOrders()
	{
		PotionOrder first = order(0, MIXALOT);
		PotionOrder second = order(1, MIXALOT);
		PotionOrder third = order(2, MAMMOTH_MIGHT_MIX);
		List<PotionOrder> orders = List.of(first, second, third);

		assertSame(first, MixGuidance.nextOrderToMix(orders, null, counts()));
		assertSame(second, MixGuidance.nextOrderToMix(orders, null, counts(MIXALOT)));
		assertSame(third, MixGuidance.nextOrderToMix(orders, null, counts(MIXALOT, MIXALOT)));
		assertNull(MixGuidance.nextOrderToMix(orders, Set.of(0, 1), counts(MIXALOT, MIXALOT)));

		first.setFulfilled(true);
		assertSame(second, MixGuidance.nextOrderToMix(orders, null, counts()));
	}

	@Test
	public void leverFollowsRecipeOrderAndIgnoresVesselOrder()
	{
		int[] empty = {EMPTY, EMPTY, EMPTY};
		assertEquals(AGA, MixGuidance.leverState(AQUALUX_AMALGAM, empty, 0).next());
		assertEquals(LYE, MixGuidance.leverState(AQUALUX_AMALGAM, new int[]{A, EMPTY, EMPTY}, 0).next());
		assertEquals(AGA, MixGuidance.leverState(AQUALUX_AMALGAM, new int[]{L, EMPTY, EMPTY}, 0).next());
		assertEquals(AGA, MixGuidance.leverState(AQUALUX_AMALGAM, new int[]{A, L, EMPTY}, 0).next());
	}

	@Test
	public void leverModes()
	{
		MixGuidance.LeverState wrongPaste = MixGuidance.leverState(MAMMOTH_MIGHT_MIX, new int[]{L, EMPTY, EMPTY}, 0);
		assertEquals(MixGuidance.LeverMode.FREE, wrongPaste.mode());
		assertTrue(wrongPaste.allows(AGA));

		MixGuidance.LeverState full = MixGuidance.leverState(MIXALOT, new int[]{M, A, L}, 0);
		assertEquals(MixGuidance.LeverMode.BLOCKED, full.mode());
		assertFalse(full.allows(MOX));

		assertEquals(MixGuidance.LeverMode.BLOCKED,
				MixGuidance.leverState(null, new int[]{EMPTY, EMPTY, EMPTY}, 0).mode());
		assertEquals(MixGuidance.LeverMode.FREE, MixGuidance.leverState(null, new int[]{M, EMPTY, EMPTY}, 0).mode());

		MixGuidance.LeverState pull = MixGuidance.leverState(MIXALOT, new int[]{M, EMPTY, EMPTY}, 0);
		assertTrue(pull.allows(AGA));
		assertFalse(pull.allows(MOX));
		assertFalse(pull.allows(LYE));
	}

	@Test
	public void pendingPullsFillEmptySlotsAndBlockDoubleClick()
	{
		int[] slots = MixGuidance.withPendingPulls(new int[]{A, EMPTY, EMPTY}, List.of(LYE));
		assertEquals(List.of(A, L, EMPTY), List.of(slots[0], slots[1], slots[2]));

		int[] beforeServer = MixGuidance.withPendingPulls(new int[]{EMPTY, EMPTY, EMPTY}, List.of(MOX));
		assertFalse(MixGuidance.leverState(MIXALOT, beforeServer, 0).allows(MOX));
		assertTrue(MixGuidance.leverState(MIXALOT, beforeServer, 0).allows(AGA));

		int[] overflow = MixGuidance.withPendingPulls(new int[]{M, A, EMPTY}, List.of(LYE, LYE));
		assertEquals(List.of(M, A, L), List.of(overflow[0], overflow[1], overflow[2]));
	}

	@Test
	public void vesselPotionMatchesPasteMultiset()
	{
		assertEquals(AQUALUX_AMALGAM, MixGuidance.vesselPotion(new int[]{L, A, A}, 0));
		assertEquals(MIXALOT, MixGuidance.vesselPotion(new int[]{EMPTY, EMPTY, EMPTY}, MIXALOT.ordinal() + 1));
		assertNull(MixGuidance.vesselPotion(new int[]{M, M, EMPTY}, 0));
	}

	@Test
	public void mixValidOnlyForUncoveredOpenOrder()
	{
		PotionOrder mixalot = order(0, MIXALOT);
		List<PotionOrder> orders = List.of(mixalot, order(1, AQUALUX_AMALGAM));

		assertTrue(MixGuidance.mixMatchesOpenOrder(MIXALOT, orders, counts()));
		assertFalse(MixGuidance.mixMatchesOpenOrder(MIXALOT, orders, counts(MIXALOT)));
		assertFalse(MixGuidance.mixMatchesOpenOrder(MAMMOTH_MIGHT_MIX, orders, counts()));

		mixalot.setFulfilled(true);
		assertFalse(MixGuidance.mixMatchesOpenOrder(MIXALOT, orders, counts()));
	}

	@Test
	public void aMixOnlyASkippedOrderTakesIsNotPlanned()
	{
		List<PotionOrder> orders = List.of(order(0, MIXALOT), order(1, AQUALUX_AMALGAM));

		assertTrue(MixGuidance.mixMatchesPlannedOrder(MIXALOT, orders, Set.of(0), counts()));
		assertFalse(MixGuidance.mixMatchesPlannedOrder(AQUALUX_AMALGAM, orders, Set.of(0), counts()));
		assertTrue(MixGuidance.mixMatchesOpenOrder(AQUALUX_AMALGAM, orders, counts()));
		assertTrue(MixGuidance.mixMatchesPlannedOrder(AQUALUX_AMALGAM, orders, Set.of(), counts()));
	}

	@Test
	public void deliverWhenEveryPlannedOrderIsFulfilledAndHeld()
	{
		PotionOrder first = order(0, MIXALOT);
		PotionOrder second = order(1, MIXALOT);
		PotionOrder skipped = order(2, MAMMOTH_MIGHT_MIX);
		List<PotionOrder> orders = List.of(first, second, skipped);
		first.setFulfilled(true);
		second.setFulfilled(true);

		assertFalse(MixGuidance.readyToDeliver(orders, null, counts(MIXALOT, MIXALOT)));
		assertTrue(MixGuidance.readyToDeliver(orders, Set.of(0, 1), counts(MIXALOT, MIXALOT)));
		assertFalse(MixGuidance.readyToDeliver(orders, Set.of(0, 1), counts(MIXALOT)));
	}

	@Test
	public void slotValuesOutsideOneToThreeAreNoComponent()
	{
		assertNull(MixGuidance.componentFromSlotValue(0));
		assertNull(MixGuidance.componentFromSlotValue(4));
		assertSame(MOX, MixGuidance.componentFromSlotValue(M));
		assertSame(LYE, MixGuidance.componentFromSlotValue(L));
	}

	@Test
	public void vesselEmptyOnlyWithNoPasteAndNoReadyPotion()
	{
		assertTrue(MixGuidance.vesselEmpty(new int[]{EMPTY, EMPTY, EMPTY}, 0));
		assertFalse(MixGuidance.vesselEmpty(new int[]{M, EMPTY, EMPTY}, 0));
		assertFalse(MixGuidance.vesselEmpty(new int[]{EMPTY, EMPTY, EMPTY}, 1));
	}

	/** More pending pulls than empty slots: the extra pulls are dropped, the vessel just reads full. */
	@Test
	public void pendingPullsBeyondTheEmptySlotsAreDropped()
	{
		int[] slots = MixGuidance.withPendingPulls(new int[]{M, A, EMPTY}, List.of(LYE, LYE, MOX));

		assertEquals(L, slots[2]);
		assertTrue(MixGuidance.vesselFull(slots, 0));
	}

	/** An empty recommendation set means nothing is recommended, so every order is part of the plan. */
	@Test
	public void emptyRecommendationPlansEveryOrder()
	{
		assertTrue(MixGuidance.planned(order(2, MIXALOT), Set.of()));
		assertTrue(MixGuidance.planned(order(2, MIXALOT), null));
		assertFalse(MixGuidance.planned(order(2, MIXALOT), Set.of(0)));
	}

	@Test
	public void vesselPotionPrefersTheReadyPotion()
	{
		assertSame(AQUALUX_AMALGAM, MixGuidance.vesselPotion(new int[]{M, M, M}, AQUALUX_AMALGAM.ordinal() + 1));
		assertNull(MixGuidance.vesselPotion(new int[]{M, EMPTY, EMPTY}, 0));
	}
}
