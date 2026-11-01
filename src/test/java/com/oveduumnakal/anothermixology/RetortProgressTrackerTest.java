package com.oveduumnakal.anothermixology;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Tests for {@link RetortProgressTracker}. */
public class RetortProgressTrackerTest
{
	@Test
	public void ignoresProgressWhenNoPotionLoaded()
	{
		RetortProgressTracker tracker = new RetortProgressTracker();
		tracker.onProgressChanged(5);
		assertEquals(0, tracker.currentProgress());
		assertFalse(tracker.potionLoaded());
	}

	@Test
	public void tracksCurrentProgressWhilePotionLoaded()
	{
		RetortProgressTracker tracker = new RetortProgressTracker();
		tracker.onPotionLoaded();
		tracker.onProgressChanged(2);
		tracker.onProgressChanged(4);
		assertTrue(tracker.potionLoaded());
		assertEquals(4, tracker.currentProgress());
	}

	/** A cleared potion resets the progress, so the next potion starts from 0. */
	@Test
	public void progressResetsAcrossPotions()
	{
		RetortProgressTracker tracker = new RetortProgressTracker();
		tracker.onPotionLoaded();
		tracker.onProgressChanged(15);
		tracker.onPotionCleared();
		assertEquals(0, tracker.currentProgress());
		tracker.onPotionLoaded();
		assertEquals(0, tracker.currentProgress());
	}
}
