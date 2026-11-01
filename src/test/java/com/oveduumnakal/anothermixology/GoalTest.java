package com.oveduumnakal.anothermixology;

import java.util.List;

import org.junit.Test;

import net.runelite.api.Client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Tests for {@link Goal}: which rewards are selected, and the summed cost and progress per resin colour. */
public class GoalTest
{
	@Test
	public void nothingTickedSelectsNothing()
	{
		assertTrue(Goal.collectSelections(new TickedConfig(false, false)).isEmpty());
	}

	@Test
	public void tickedRewardsAreSelectedOnceEach()
	{
		List<Goal.Selection> selected = Goal.collectSelections(new TickedConfig(true, true));

		assertEquals(2, selected.size());
		assertEquals(RewardItem.PRESCRIPTION_GOGGLES, selected.get(0).item);
		assertEquals(RewardItem.REAGENT_POUCH, selected.get(1).item);
		assertEquals(1, selected.get(0).quantity);
	}

	@Test
	public void recalculateSumsTheCostsAndAveragesTheProgress()
	{
		Client client = mock(Client.class);
		when(client.getVarpValue(AnotherMixologyPlugin.VARP_MOX_RESIN)).thenReturn(22400);
		when(client.getVarpValue(AnotherMixologyPlugin.VARP_AGA_RESIN)).thenReturn(9100);
		when(client.getVarpValue(AnotherMixologyPlugin.VARP_LYE_RESIN)).thenReturn(0);
		Goal goal = new Goal(RewardItem.NONE);

		goal.recalculate(new TickedConfig(true, true), client);

		assertTrue(goal.isMultiMode());
		assertEquals(8600 + 13800, goal.getComponentData(PotionComponent.MOX).goalAmount);
		assertEquals(1.0, goal.getComponentData(PotionComponent.MOX).percentageToGoal, 1e-9);
		assertEquals(0.5, goal.getComponentData(PotionComponent.AGA).percentageToGoal, 1e-9);
		assertEquals(0.0, goal.getComponentData(PotionComponent.LYE).percentageToGoal, 1e-9);
		assertEquals(0.5, goal.getOverallProgress(), 1e-9);
		assertEquals("Selected rewards (2)", goal.getDisplayName());
	}

	@Test
	public void recalculateWithNothingTickedLeavesMultiMode()
	{
		Client client = mock(Client.class);
		Goal goal = new Goal(RewardItem.NONE);

		goal.recalculate(new TickedConfig(false, false), client);

		assertFalse(goal.isMultiMode());
		assertEquals(0, goal.getComponentData(PotionComponent.MOX).goalAmount);
	}

	/** A config with only the Prescription Goggles and Reagent Pouch tick boxes set; everything else is default. */
	private static final class TickedConfig implements AnotherMixologyConfig
	{
		private final boolean goggles;
		private final boolean pouch;

		TickedConfig(boolean goggles, boolean pouch)
		{
			this.goggles = goggles;
			this.pouch = pouch;
		}

		@Override
		public boolean trackPrescriptionGoggles()
		{
			return goggles;
		}

		@Override
		public boolean trackReagentPouch()
		{
			return pouch;
		}
	}
}
