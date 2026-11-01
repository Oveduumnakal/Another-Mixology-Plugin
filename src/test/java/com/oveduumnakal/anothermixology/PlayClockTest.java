package com.oveduumnakal.anothermixology;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Tests for {@link PlayClock}. */
public class PlayClockTest
{
	@Test
	public void standsStillBeforeTheFirstAction()
	{
		assertEquals(0, new PlayClock().played(50_000));
	}

	@Test
	public void runsWhileThePlayerKeepsActing()
	{
		var clock = new PlayClock();
		clock.active(10_000);
		clock.active(13_000);
		clock.active(16_000);

		assertEquals(7_000, clock.played(17_000));
	}

	@Test
	public void stopsOnceThePlayerIsIdle()
	{
		var clock = new PlayClock();
		clock.active(10_000);

		long stopped = PlayClock.IDLE_MILLIS;
		assertEquals(stopped, clock.played(10_000 + PlayClock.IDLE_MILLIS));
		assertEquals(stopped, clock.played(100_000));
	}

	@Test
	public void resumesFromWhereItStoppedAfterAnIdleSpell()
	{
		var clock = new PlayClock();
		clock.active(10_000);
		clock.active(100_000);

		assertEquals(PlayClock.IDLE_MILLIS + 2_000, clock.played(102_000));
	}
}
