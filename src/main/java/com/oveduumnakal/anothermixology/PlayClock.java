package com.oveduumnakal.anothermixology;

/**
 * Counts the time spent playing, so the reward times count down only while the player works. It runs from an action
 * until {@link #IDLE_MILLIS} after the last one, which covers the short pauses of play, and then stops. Times are
 * {@link System#currentTimeMillis()} values passed in, so it can be unit tested.
 */
final class PlayClock
{
	/** How long after their last action the player still counts as playing. */
	static final long IDLE_MILLIS = 6_000;

	/** The play counted before the current run. */
	private long counted;

	/** When the current run started; -1 before the first action. */
	private long runningSince = -1;

	private long lastActive;

	/** Records that the player did something at {@code now}, starting the clock again if they were idle. */
	void active(long now)
	{
		if (runningSince < 0 || now > lastActive + IDLE_MILLIS)
		{
			counted = played(now);
			runningSince = now;
		}

		lastActive = now;
	}

	/** Returns the milliseconds played up to {@code now}. */
	long played(long now)
	{
		if (runningSince < 0)
			return counted;

		return counted + Math.max(0, Math.min(now, lastActive + IDLE_MILLIS) - runningSince);
	}
}
