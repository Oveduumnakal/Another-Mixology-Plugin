package com.oveduumnakal.anothermixology;

import java.util.List;

/**
 * The rewards panel rows with the estimated seconds each needs, as of {@link #madeAtMillis}. A time is NaN while it is
 * still being worked out and infinite when the strategy makes no progress.
 */
final class RewardEstimates
{
	final List<RewardRows.Row> rows;
	final double[] seconds;

	/** When the estimates' inputs were read, in {@link PlayClock} milliseconds played. */
	final long madeAtMillis;

	RewardEstimates(List<RewardRows.Row> rows, double[] seconds, long madeAtMillis)
	{
		this.rows = rows;
		this.seconds = seconds;
		this.madeAtMillis = madeAtMillis;
	}

	/**
	 * Returns a row's estimate in whole seconds, counted down by the whole seconds played since it was made and never
	 * below 0. Every row counts the same whole seconds, so the rows tick together.
	 */
	long secondsLeft(int row, long playedMillis)
	{
		return Math.max(0, Math.round(seconds[row]) - (playedMillis - madeAtMillis) / 1000);
	}
}
