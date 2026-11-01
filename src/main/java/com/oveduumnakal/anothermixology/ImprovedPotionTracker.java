package com.oveduumnakal.anothermixology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Follows which inventory slots hold potions a digweed improved. An improved potion keeps its item id, so the slot the
 * player used the digweed on is recorded and followed: a drag moves it to the slot the same item appears in, and a
 * potion that leaves for a station is picked up again when its processed form comes back. Free of client state so it
 * can be unit tested; inventories are given as item ids by slot.
 */
final class ImprovedPotionTracker
{
	/** Improved slots and the item id each holds. */
	private final Map<Integer, Integer> slots = new HashMap<>();

	/** Improved potions that left the inventory, most likely into a station, waiting to come back processed. */
	private final List<PotionType> away = new ArrayList<>();

	private int clickedSlot = -1;
	private int clickedItemId;

	/** Records the potion slot the player just used a digweed on, before the game confirms it. */
	void clicked(int slot, int itemId)
	{
		clickedSlot = slot;
		clickedItemId = itemId;
	}

	/**
	 * Records the improved potion once the game confirms it: the clicked slot when it still holds that potion, or else
	 * the first untracked slot holding a potion of the type.
	 */
	void confirmed(PotionType type, int[] items)
	{
		int slot = clickedSlot;
		clickedSlot = -1;
		if (slot >= 0 && slot < items.length && items[slot] == clickedItemId
				&& PotionType.fromItemId(items[slot]) == type)
		{
			slots.put(slot, items[slot]);
			return;
		}

		for (int i = 0; i < items.length; i++)
		{
			if (!slots.containsKey(i) && PotionType.fromItemId(items[i]) == type)
			{
				slots.put(i, items[i]);
				return;
			}
		}
	}

	/**
	 * Moves the tracked slots along with an inventory change. A tracked potion whose slot changed is looked for in a
	 * slot that newly holds the same item (a drag or swap); if there is none it is taken to be in a station, and the
	 * next slot to newly hold its processed form is tracked instead.
	 */
	void inventoryChanged(int[] before, int[] after)
	{
		List<Integer> moved = new ArrayList<>();
		slots.entrySet().removeIf(entry ->
		{
			int slot = entry.getKey();
			if (slot < after.length && after[slot] == entry.getValue())
				return false;

			moved.add(entry.getValue());
			return true;
		});

		for (int itemId : moved)
		{
			int slot = newSlotHolding(before, after, itemId);
			if (slot >= 0)
			{
				slots.put(slot, itemId);
				continue;
			}

			var type = PotionType.fromItemId(itemId);
			if (type != null)
				away.add(type);
		}

		away.removeIf(type ->
		{
			int slot = newSlotHolding(before, after, type.modifiedItemId());
			if (slot < 0)
				return false;

			slots.put(slot, type.modifiedItemId());
			return true;
		});
	}

	/** Returns the inventory slots holding improved potions. */
	Set<Integer> slots()
	{
		return Set.copyOf(slots.keySet());
	}

	/** Forgets every improved potion, for a new order set. */
	void clear()
	{
		slots.clear();
		away.clear();
		clickedSlot = -1;
	}

	private int newSlotHolding(int[] before, int[] after, int itemId)
	{
		for (int i = 0; i < after.length; i++)
		{
			boolean wasThere = i < before.length && before[i] == itemId;
			if (after[i] == itemId && !wasThere && !slots.containsKey(i))
				return i;
		}

		return -1;
	}
}
