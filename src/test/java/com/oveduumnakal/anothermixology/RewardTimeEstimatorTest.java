package com.oveduumnakal.anothermixology;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link RewardTimeEstimator} against the variant simulator's averages (2,000 Reagent Pouch runs each, the
 * Expected value strategy, {@link Pace#DEFAULT} timings), which it must match within 5 %.
 */
public class RewardTimeEstimatorTest
{
	private static final int[] REAGENT_POUCH = {13800, 11200, 15100};

	private static void assertWithin5Percent(double expectedHours, int[] resin)
	{
		double hours = RewardTimeEstimator.seconds(RecommendationStrategy.EXPECTED_VALUE, resin, REAGENT_POUCH,
				Pace.DEFAULT) / 3600;
		assertEquals(expectedHours, hours, expectedHours * 0.05);
	}

	@Test
	public void matchesTheSimulatorFromNothing()
	{
		assertWithin5Percent(5.790, new int[]{0, 0, 0});
	}

	@Test
	public void matchesTheSimulatorHalfWay()
	{
		assertWithin5Percent(2.909, new int[]{6900, 5600, 7550});
	}

	@Test
	public void matchesTheSimulatorNearTheEnd()
	{
		assertWithin5Percent(1.451, new int[]{10104, 8742, 11502});
		assertWithin5Percent(0.296, new int[]{13000, 10800, 14500});
	}

	@Test
	public void reachedTargetTakesNoTime()
	{
		assertEquals(0, RewardTimeEstimator.seconds(RecommendationStrategy.EXPECTED_VALUE, REAGENT_POUCH,
				REAGENT_POUCH, Pace.DEFAULT), 1e-9);
	}

	@Test
	public void otherStrategiesGiveAnEstimateToo()
	{
		double greedy = RewardTimeEstimator.seconds(RecommendationStrategy.GREEDY, new int[3], REAGENT_POUCH,
				Pace.DEFAULT);
		assertTrue(greedy > 4 * 3600 && greedy < 9 * 3600);
	}
}
