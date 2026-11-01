package com.oveduumnakal.anothermixology;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/** Tests for {@link PaceTracker}. */
public class PaceTrackerTest
{
	@Test
	public void usesTheDefaultUntilEnoughSets()
	{
		var tracker = new PaceTracker();
		for (int i = 0; i < PaceTracker.MIN_SETS - 1; i++)
			tracker.addSet(20 + 10 * (i % 3 + 1), i % 3 + 1);

		assertSame(Pace.DEFAULT, tracker.pace());
	}

	@Test
	public void fitsSecondsPerPotionAndPerDelivery()
	{
		var tracker = new PaceTracker();
		for (int i = 0; i < 12; i++)
		{
			int potions = i % 3 + 1;
			tracker.addSet(12 + 15 * potions, potions);
		}

		var pace = tracker.pace();
		assertEquals(15, pace.secondsPerPotion, 1e-9);
		assertEquals(12, pace.secondsPerDelivery, 1e-9);
	}

	@Test
	public void leavesOutBreaks()
	{
		var tracker = new PaceTracker();
		for (int i = 0; i < 12; i++)
		{
			int potions = i % 3 + 1;
			tracker.addSet(12 + 15 * potions, potions);
		}

		tracker.addSet(600, 3);
		assertEquals(15, tracker.pace().secondsPerPotion, 1e-9);
	}
}
