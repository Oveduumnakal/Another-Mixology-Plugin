package com.oveduumnakal.anothermixology;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Tests for {@link PotionOrder}. */
public class PotionOrderTest
{
	@Test
	public void rebuiltOrderKeepsItsDoneFlag()
	{
		PotionOrder old = new PotionOrder(1, PotionType.MIXALOT, PotionModifier.CONCENTRATED);
		old.setFulfilled(true);
		PotionOrder rebuilt = new PotionOrder(1, PotionType.MIXALOT, PotionModifier.CONCENTRATED);

		PotionOrder.keepFulfilled(List.of(old), List.of(rebuilt));

		assertTrue(rebuilt.fulfilled());
	}

	@Test
	public void rebuiltOrderKeepsItsDigweedImprovement()
	{
		PotionOrder old = new PotionOrder(1, PotionType.MIXALOT, PotionModifier.CONCENTRATED);
		old.setImproved(true);
		PotionOrder rebuilt = new PotionOrder(1, PotionType.MIXALOT, PotionModifier.CONCENTRATED);

		PotionOrder.keepFulfilled(List.of(old), List.of(rebuilt));

		assertTrue(rebuilt.improved());
	}

	/** A different slot, potion or modifier is a different order, so it starts not done. */
	@Test
	public void changedOrderStartsNotDone()
	{
		PotionOrder old = new PotionOrder(1, PotionType.MIXALOT, PotionModifier.CONCENTRATED);
		old.setFulfilled(true);
		PotionOrder otherSlot = new PotionOrder(2, PotionType.MIXALOT, PotionModifier.CONCENTRATED);
		PotionOrder otherPotion = new PotionOrder(1, PotionType.AZURE_AURA_MIX, PotionModifier.CONCENTRATED);
		PotionOrder otherModifier = new PotionOrder(1, PotionType.MIXALOT, PotionModifier.HOMOGENOUS);

		PotionOrder.keepFulfilled(List.of(old), Arrays.asList(otherSlot, otherPotion, otherModifier));

		assertFalse(otherSlot.fulfilled());
		assertFalse(otherPotion.fulfilled());
		assertFalse(otherModifier.fulfilled());
	}

	@Test
	public void notDoneOrderStaysNotDone()
	{
		PotionOrder old = new PotionOrder(0, PotionType.MIXALOT, PotionModifier.CRYSTALISED);
		PotionOrder rebuilt = new PotionOrder(0, PotionType.MIXALOT, PotionModifier.CRYSTALISED);

		PotionOrder.keepFulfilled(List.of(old), List.of(rebuilt));

		assertFalse(rebuilt.fulfilled());
	}
}
