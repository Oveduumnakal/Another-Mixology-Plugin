package com.oveduumnakal.anothermixology;

import java.util.Set;

/**
 * Estimates how long reaching a resin target takes. The remaining resin is worked off in chunks: for each chunk, every
 * possible order set is weighted by its in-game chance, the strategy picks its delivery, and that gives each colour's
 * useful resin per second at the player's pace. The colours then shrink at those rates until a colour finishes or the
 * chunk is used up. Within about 1.5 % of the simulated average for the Reagent Pouch from any starting resin.
 */
final class RewardTimeEstimator
{
	/** Resin worked off per step, summed over the colours. */
	private static final double CHUNK = 2000;
	private static final int MAX_STEPS = 1000;
	private static final double[] UNIT_COST = {1, 1, 1};

	private RewardTimeEstimator()
	{
	}

	/**
	 * Returns the estimated seconds to reach the target from the given resin, 0 when it is already reached, or
	 * infinity when the strategy makes no progress.
	 */
	static double seconds(RecommendationStrategy strategy, int[] resin, int[] target, Pace pace)
	{
		double[] deficit = new double[3];
		for (int colour = 0; colour < 3; colour++)
			deficit[colour] = Math.max(target[colour] - resin[colour], 0);

		double seconds = 0;
		for (int step = 0; step < MAX_STEPS && !done(deficit); step++)
		{
			int[] whole = new int[3];
			int[] now = new int[3];
			for (int colour = 0; colour < 3; colour++)
			{
				whole[colour] = (int) Math.ceil(deficit[colour]);
				now[colour] = target[colour] - whole[colour];
			}

			double[] rate = rates(strategy, now, target, whole, pace);
			double total = rate[0] + rate[1] + rate[2];
			if (total <= 0)
				return Double.POSITIVE_INFINITY;

			double dt = CHUNK / total;
			for (int colour = 0; colour < 3; colour++)
			{
				if (deficit[colour] >= 1 && rate[colour] > 0)
					dt = Math.min(dt, deficit[colour] / rate[colour]);
			}

			for (int colour = 0; colour < 3; colour++)
				deficit[colour] = Math.max(0, deficit[colour] - rate[colour] * dt);

			seconds += dt;
		}

		return seconds;
	}

	private static boolean done(double[] deficit)
	{
		return deficit[0] < 1 && deficit[1] < 1 && deficit[2] < 1;
	}

	/** Returns each colour's expected useful resin per second, over every order set, under the strategy. */
	private static double[] rates(RecommendationStrategy strategy, int[] resin, int[] target, int[] deficit,
			Pace pace)
	{
		RecommendationPolicy policy = strategy.createPolicy();
		double[] weight = ExpectedValuePolicy.weights(deficit);
		double longRunRate = strategy == RecommendationStrategy.EXPECTED_VALUE
				? ExpectedValuePolicy.longRunRate(deficit, weight)
				: 0;
		double[] gained = new double[3];
		double seconds = 0;
		for (var draw : ExpectedValuePolicy.orderDraws())
		{
			int[] delivery = strategy == RecommendationStrategy.EXPECTED_VALUE
					? ExpectedValuePolicy.bestSubset(OrderSet.of(draw.orders), UNIT_COST, deficit, weight,
							longRunRate)
					: slots(policy.decide(draw.orders, resin, target));
			int bonusFifths = 4 + delivery.length;
			for (int colour = 0; colour < 3; colour++)
			{
				int base = 0;
				for (int slot : delivery)
					base += draw.orders[slot].resinYield(colour);

				gained[colour] += draw.chance * Math.min(deficit[colour], base * bonusFifths / 5);
			}

			seconds += draw.chance * (delivery.length * pace.secondsPerPotion + pace.secondsPerDelivery);
		}

		for (int colour = 0; colour < 3; colour++)
			gained[colour] /= seconds;

		return gained;
	}

	private static int[] slots(Set<Integer> picked)
	{
		return picked.stream()
				.mapToInt(Integer::intValue)
				.sorted()
				.toArray();
	}
}
