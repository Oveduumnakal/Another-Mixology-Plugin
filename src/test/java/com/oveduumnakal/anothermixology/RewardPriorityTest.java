package com.oveduumnakal.anothermixology;

import java.util.List;

import org.junit.Test;

import static com.oveduumnakal.anothermixology.RewardItem.ALDARIUM;
import static com.oveduumnakal.anothermixology.RewardItem.POTION_STORAGE;
import static com.oveduumnakal.anothermixology.RewardItem.REAGENT_POUCH;
import static org.junit.Assert.assertEquals;

/** Tests for {@link RewardPriority}. */
public class RewardPriorityTest
{
	@Test
	public void savesAndReadsTheOrder()
	{
		var order = List.of(POTION_STORAGE, REAGENT_POUCH);
		assertEquals(order, RewardPriority.parse(RewardPriority.format(order)));
	}

	@Test
	public void readingSkipsUnknownNamesAndRepeats()
	{
		assertEquals(List.of(POTION_STORAGE, REAGENT_POUCH),
				RewardPriority.parse("POTION_STORAGE, GONE,REAGENT_POUCH,POTION_STORAGE"));
		assertEquals(List.of(), RewardPriority.parse(""));
	}

	@Test
	public void aNewlyTickedRewardGoesToTheBottom()
	{
		var order = RewardPriority.reconcile(List.of(POTION_STORAGE), List.of(REAGENT_POUCH, POTION_STORAGE));
		assertEquals(List.of(POTION_STORAGE, REAGENT_POUCH), order);
	}

	@Test
	public void anUntickedRewardLeavesTheOrder()
	{
		var order = RewardPriority.reconcile(List.of(POTION_STORAGE, ALDARIUM, REAGENT_POUCH),
				List.of(REAGENT_POUCH, POTION_STORAGE));
		assertEquals(List.of(POTION_STORAGE, REAGENT_POUCH), order);
	}

	@Test
	public void sortsSelectionsIntoTheOrder()
	{
		var pouch = new Goal.Selection(REAGENT_POUCH, 1);
		var storage = new Goal.Selection(POTION_STORAGE, 1);
		var aldarium = new Goal.Selection(ALDARIUM, 20);

		var sorted = RewardPriority.sort(List.of(pouch, storage, aldarium), List.of(POTION_STORAGE, REAGENT_POUCH));
		assertEquals(List.of(storage, pouch, aldarium), sorted);
	}

	@Test
	public void movesARewardOnePlaceAndStopsAtTheEnds()
	{
		var order = List.of(REAGENT_POUCH, POTION_STORAGE, ALDARIUM);
		assertEquals(List.of(POTION_STORAGE, REAGENT_POUCH, ALDARIUM),
				RewardPriority.move(order, POTION_STORAGE, -1));
		assertEquals(List.of(REAGENT_POUCH, ALDARIUM, POTION_STORAGE),
				RewardPriority.move(order, POTION_STORAGE, 1));
		assertEquals(order, RewardPriority.move(order, REAGENT_POUCH, -1));
		assertEquals(order, RewardPriority.move(order, ALDARIUM, 1));
	}
}
