package com.oveduumnakal.anothermixology;

import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Tests for {@link ImprovedPotionTracker}. */
public class ImprovedPotionTrackerTest
{
	private static final int MML = PotionType.MARLEYS_MOONLIGHT.itemId();
	private static final int MML_DONE = PotionType.MARLEYS_MOONLIGHT.modifiedItemId();
	private static final int EMPTY = -1;

	@Test
	public void tracksTheClickedPotionNotTheFirst()
	{
		var tracker = new ImprovedPotionTracker();
		int[] inventory = {MML, MML, EMPTY};
		tracker.clicked(1, MML);
		tracker.confirmed(PotionType.MARLEYS_MOONLIGHT, inventory);

		assertEquals(Set.of(1), tracker.slots());
	}

	@Test
	public void followsADrag()
	{
		var tracker = new ImprovedPotionTracker();
		tracker.clicked(0, MML);
		tracker.confirmed(PotionType.MARLEYS_MOONLIGHT, new int[]{MML, EMPTY, EMPTY});

		tracker.inventoryChanged(new int[]{MML, EMPTY, EMPTY}, new int[]{EMPTY, EMPTY, MML});

		assertEquals(Set.of(2), tracker.slots());
	}

	@Test
	public void followsThePotionThroughAStation()
	{
		var tracker = new ImprovedPotionTracker();
		tracker.clicked(1, MML);
		tracker.confirmed(PotionType.MARLEYS_MOONLIGHT, new int[]{MML, MML, EMPTY});

		tracker.inventoryChanged(new int[]{MML, MML, EMPTY}, new int[]{MML, EMPTY, EMPTY});
		assertEquals(Set.of(), tracker.slots());

		tracker.inventoryChanged(new int[]{MML, EMPTY, EMPTY}, new int[]{MML, EMPTY, MML_DONE});
		assertEquals(Set.of(2), tracker.slots());
	}

	@Test
	public void anotherUnfinishedPotionOfTheTypeIsNotTakenForTheImprovedOne()
	{
		var tracker = new ImprovedPotionTracker();
		tracker.clicked(0, MML);
		tracker.confirmed(PotionType.MARLEYS_MOONLIGHT, new int[]{MML, EMPTY, EMPTY});

		tracker.inventoryChanged(new int[]{MML, EMPTY, EMPTY}, new int[]{EMPTY, EMPTY, EMPTY});
		tracker.inventoryChanged(new int[]{EMPTY, EMPTY, EMPTY}, new int[]{EMPTY, MML, EMPTY});

		assertEquals(Set.of(), tracker.slots());
	}

	@Test
	public void clearForgetsEverything()
	{
		var tracker = new ImprovedPotionTracker();
		tracker.clicked(0, MML);
		tracker.confirmed(PotionType.MARLEYS_MOONLIGHT, new int[]{MML});
		tracker.clear();

		assertEquals(Set.of(), tracker.slots());
	}
}
