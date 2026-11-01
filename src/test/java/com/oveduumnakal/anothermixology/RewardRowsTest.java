package com.oveduumnakal.anothermixology;

import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Tests for {@link RewardRows}. */
public class RewardRowsTest
{
	private static final List<Goal.Selection> POUCH_AND_GOGGLES = List.of(
			new Goal.Selection(RewardItem.REAGENT_POUCH, 1),
			new Goal.Selection(RewardItem.PRESCRIPTION_GOGGLES, 1));

	@Test
	public void eachOnItsOwnCountsAllTheResinForEveryReward()
	{
		int[] resin = {13800, 11200, 15100};
		var rows = RewardRows.build(POUCH_AND_GOGGLES, resin, RewardOrder.EACH_ON_ITS_OWN);

		assertEquals(3, rows.size());
		assertTrue(rows.get(0).ready());
		assertArrayEquals(new int[]{13800, 11200, 15100}, rows.get(0).target);
		assertEquals("All Rewards:", rows.get(2).label);
		assertFalse(rows.get(2).ready());
	}

	@Test
	public void inOrderCountsOnlyTheResinLeftAfterTheRowsAbove()
	{
		int[] resin = {13800, 11200, 15100};
		var rows = RewardRows.build(POUCH_AND_GOGGLES, resin, RewardOrder.IN_ORDER);

		assertTrue(rows.get(0).ready());
		assertEquals(0, rows.get(1).progress, 1e-9);
		int[] allTarget = rows.get(2).target;
		int[] gogglesTarget = rows.get(1).target;
		assertArrayEquals(allTarget, gogglesTarget);
	}

	@Test
	public void progressIsTheShareOfTheCostCovered()
	{
		assertEquals(0.5, RewardRows.progress(new int[]{100, 0, 50}, new int[]{100, 0, 200}), 1e-9);
		assertEquals(1, RewardRows.progress(new int[]{0, 0, 0}, new int[]{0, 0, 0}), 1e-9);
	}

	@Test
	public void quantityGoesInTheLabel()
	{
		var rows = RewardRows.build(List.of(new Goal.Selection(RewardItem.ALDARIUM, 20)), new int[3],
				RewardOrder.EACH_ON_ITS_OWN);
		assertEquals(RewardItem.ALDARIUM.itemName() + " x20", rows.get(0).label);
	}

	@Test
	public void formatsTimeAsHoursMinutesSeconds()
	{
		assertEquals("08h 53m 09s", RewardRows.formatTime(8 * 3600 + 53 * 60 + 9));
		assertEquals("00h 00m 00s", RewardRows.formatTime(0));
	}

	@Test
	public void estimatesCountDownAndStopAtZero()
	{
		var estimates = new RewardEstimates(List.of(), new double[]{100}, 1_000);
		assertEquals(70, estimates.secondsLeft(0, 31_000));
		assertEquals(0, estimates.secondsLeft(0, 500_000));
	}

	@Test
	public void rowsTickOnTheSameSecond()
	{
		var estimates = new RewardEstimates(List.of(), new double[]{100.2, 50.7}, 0);
		for (long played = 0; played < 5_000; played += 100)
		{
			long first = estimates.secondsLeft(0, played);
			long second = estimates.secondsLeft(1, played);
			assertEquals(100 - 51, first - second);
		}
	}
}
