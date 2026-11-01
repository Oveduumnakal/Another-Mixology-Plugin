package com.oveduumnakal.anothermixology;

import java.util.ArrayList;
import java.util.List;

/**
 * Measures the player's pace from the order sets delivered this session: a least-squares line through each set's
 * time against its potion count gives the seconds per potion (slope) and per delivery (intercept). Until
 * {@link #MIN_SETS} sets are seen, or when the fit makes no sense, it answers {@link Pace#DEFAULT}.
 */
final class PaceTracker
{
	static final int MIN_SETS = 10;

	/** Sets slower than this are breaks or detours, not play, and are left out. */
	static final double MAX_SET_SECONDS = 100;

	private final List<double[]> sets = new ArrayList<>();

	/** Records one delivered set: how long it took from the previous delivery and how many potions it held. */
	void addSet(double seconds, int potions)
	{
		if (potions > 0 && seconds > 0 && seconds <= MAX_SET_SECONDS)
			sets.add(new double[]{potions, seconds});
	}

	Pace pace()
	{
		int n = sets.size();
		if (n < MIN_SETS)
			return Pace.DEFAULT;

		double sumX = 0;
		double sumY = 0;
		double sumXx = 0;
		double sumXy = 0;
		for (double[] set : sets)
		{
			sumX += set[0];
			sumY += set[1];
			sumXx += set[0] * set[0];
			sumXy += set[0] * set[1];
		}

		double spread = n * sumXx - sumX * sumX;
		if (spread == 0)
			return Pace.DEFAULT;

		double perPotion = (n * sumXy - sumX * sumY) / spread;
		double perDelivery = (sumY - perPotion * sumX) / n;
		if (perPotion <= 0 || perDelivery < 0)
			return Pace.DEFAULT;

		return new Pace(perPotion, perDelivery);
	}

	void clear()
	{
		sets.clear();
	}
}
