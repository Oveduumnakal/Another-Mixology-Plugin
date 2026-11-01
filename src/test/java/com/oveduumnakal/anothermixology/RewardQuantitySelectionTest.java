package com.oveduumnakal.anothermixology;

import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Tests that a reward quantity of 0 leaves the reward out and a positive one selects it that many times. */
public class RewardQuantitySelectionTest
{
	@Test
	public void zeroQuantityIsNotTracked()
	{
		assertTrue(Goal.collectSelections(new QuantityConfig(0)).isEmpty());
	}

	@Test
	public void positiveQuantityIsTrackedWithThatQuantity()
	{
		List<Goal.Selection> selected = Goal.collectSelections(new QuantityConfig(12));

		assertEquals(1, selected.size());
		assertEquals(RewardItem.ALDARIUM, selected.get(0).item);
		assertEquals(12, selected.get(0).quantity);
	}

	/** A config where only the Aldarium quantity is set. */
	private static final class QuantityConfig implements AnotherMixologyConfig
	{
		private final int aldarium;

		QuantityConfig(int aldarium)
		{
			this.aldarium = aldarium;
		}

		@Override
		public int aldariumQuantity()
		{
			return aldarium;
		}
	}
}
