package com.oveduumnakal.anothermixology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Decision logic for the mixing workflow: which order to mix next, which lever to pull,
 * and whether the vessel or conveyor belt is ready. Free of client state so it can be unit tested.
 */
final class MixGuidance
{
	/**
	 * What the levers should do right now.
	 */
	enum LeverMode
	{
		/**
		 * One specific lever is next; the others are wrong.
		 */
		PULL,
		/**
		 * No lever should be pulled: the vessel is full, or every planned order is already covered.
		 */
		BLOCKED,
		/**
		 * The plugin cannot tell what the player is making (vessel contents match no plan), so every lever is allowed.
		 */
		FREE
	}

	/**
	 * Lever decision plus the lever to pull when the mode is {@link LeverMode#PULL}.
	 */
	static final class LeverState
	{
		static final LeverState BLOCKED = new LeverState(LeverMode.BLOCKED, null);
		static final LeverState FREE = new LeverState(LeverMode.FREE, null);

		private final LeverMode mode;
		private final PotionComponent next;

		private LeverState(LeverMode mode, PotionComponent next)
		{
			this.mode = mode;
			this.next = next;
		}

		static LeverState pull(PotionComponent next)
		{
			return new LeverState(LeverMode.PULL, next);
		}

		LeverMode mode()
		{
			return mode;
		}

		PotionComponent next()
		{
			return next;
		}

		/**
		 * @return true if the lever for {@code component} may be pulled under lever click protection
		 */
		boolean allows(PotionComponent component)
		{
			if (mode == LeverMode.FREE)
				return true;

			return mode == LeverMode.PULL && next == component;
		}
	}

	private MixGuidance()
	{
	}

	/**
	 * Maps a {@code MM_LAB_MIXER_SLOT_*} varbit value (1 mox, 2 aga, 3 lye) to its component.
	 *
	 * @return the component, or null for an empty slot or unknown value
	 */
	static PotionComponent componentFromSlotValue(int value)
	{
		if (value < 1 || value > PotionComponent.ENTRIES.length)
			return null;

		return PotionComponent.ENTRIES[value - 1];
	}

	/**
	 * Fills the vessel's empty slots, in order, with lever pulls the server has not reflected yet, so a quick
	 * second click is judged against the vessel it will actually hit.
	 *
	 * @param pending lever pulls not yet shown in the mixer varbits, oldest first
	 * @return a copy of {@code mixerSlots} with the pending pastes added
	 */
	static int[] withPendingPulls(int[] mixerSlots, List<PotionComponent> pending)
	{
		int[] slots = mixerSlots.clone();
		int next = 0;
		for (int i = 0; i < slots.length && next < pending.size(); i++)
		{
			if (slots[i] == 0)
			{
				slots[i] = pending.get(next).ordinal() + 1;
				next++;
			}
		}

		return slots;
	}

	/**
	 * @return true when the vessel holds three pastes (or the game flags it ready) and should be mixed
	 */
	static boolean vesselFull(int[] mixerSlots, int vesselReadyValue)
	{
		if (vesselReadyValue != 0)
			return true;

		for (int slot : mixerSlots)
		{
			if (slot == 0)
				return false;
		}

		return true;
	}

	/**
	 * @return true when the vessel holds no paste and no ready mix
	 */
	static boolean vesselEmpty(int[] mixerSlots, int vesselReadyValue)
	{
		if (vesselReadyValue != 0)
			return false;

		for (int slot : mixerSlots)
		{
			if (slot != 0)
				return false;
		}

		return true;
	}

	/**
	 * @return true if the order is part of the plan: no recommendation is active, or the chosen strategy recommends it
	 */
	static boolean planned(PotionOrder order, Set<Integer> recommendedSlots)
	{
		return recommendedSlots == null || recommendedSlots.isEmpty() || recommendedSlots.contains(order.idx());
	}

	/**
	 * Picks the first planned, unfulfilled order (in display order) that no potion on hand already covers.
	 * Each potion on hand covers at most one order, so duplicate orders each need their own potion.
	 *
	 * @param onHand unfinished potions in the inventory, in a station, or ready in the vessel, by type
	 * @return the order to mix next, or null if every planned order is covered
	 */
	static PotionOrder nextOrderToMix(List<PotionOrder> orders, Set<Integer> recommendedSlots,
			Map<PotionType, Integer> onHand)
	{
		Map<PotionType, Integer> remaining = new EnumMap<>(PotionType.class);
		remaining.putAll(onHand);
		for (PotionOrder order : orders)
		{
			if (order.fulfilled() || !planned(order, recommendedSlots))
				continue;

			int count = remaining.getOrDefault(order.potionType(), 0);
			if (count > 0)
			{
				remaining.put(order.potionType(), count - 1);
				continue;
			}

			return order;
		}

		return null;
	}

	/**
	 * Picks the open order whose station should take the unfinished potion in the inventory: a planned order first,
	 * then a skipped one, each in display order. Without the planned pass, a skipped order of the same potion type
	 * listed first would claim the potion and leave the planned order open.
	 *
	 * @param inInventory whether an unfinished potion of the type is in the inventory
	 * @return the order, or null if no open order's potion is in the inventory
	 */
	static PotionOrder orderToProcess(List<PotionOrder> orders, Set<Integer> recommendedSlots,
			Predicate<PotionType> inInventory)
	{
		for (boolean wantPlanned : new boolean[]{true, false})
		{
			for (PotionOrder order : orders)
			{
				if (!order.fulfilled() && planned(order, recommendedSlots) == wantPlanned
						&& inInventory.test(order.potionType()))
					return order;
			}
		}

		return null;
	}

	/**
	 * Decides the next lever from the target recipe and the pastes already in the vessel.
	 * Pastes are matched as a multiset, so the vessel order does not matter; the next lever follows the
	 * recipe's displayed order.
	 *
	 * @param target the potion being mixed, or null when nothing needs mixing
	 */
	static LeverState leverState(PotionType target, int[] mixerSlots, int vesselReadyValue)
	{
		if (vesselFull(mixerSlots, vesselReadyValue))
			return LeverState.BLOCKED;

		if (target == null)
			return vesselEmpty(mixerSlots, vesselReadyValue) ? LeverState.BLOCKED : LeverState.FREE;

		List<PotionComponent> needed = new ArrayList<>(List.of(target.components()));
		for (int slot : mixerSlots)
		{
			if (slot == 0)
				continue;

			PotionComponent component = componentFromSlotValue(slot);
			if (component == null || !needed.remove(component))
				return LeverState.FREE;
		}

		return LeverState.pull(needed.get(0));
	}

	/**
	 * Identifies the potion the vessel will produce: the game's ready flag if set, otherwise the three pastes.
	 *
	 * @return the potion, or null if the vessel is not full
	 */
	static PotionType vesselPotion(int[] mixerSlots, int vesselReadyValue)
	{
		PotionType ready = PotionType.fromIdx(vesselReadyValue - 1);
		if (ready != null)
			return ready;

		int[] counts = new int[PotionComponent.ENTRIES.length];
		for (int slot : mixerSlots)
		{
			PotionComponent component = componentFromSlotValue(slot);
			if (component == null)
				return null;

			counts[component.ordinal()]++;
		}

		for (PotionType type : PotionType.TYPES)
		{
			int[] recipe = new int[PotionComponent.ENTRIES.length];
			for (PotionComponent component : type.components())
				recipe[component.ordinal()]++;

			if (Arrays.equals(recipe, counts))
				return type;
		}

		return null;
	}

	/**
	 * True if the mix fills an unfulfilled order (skipped ones included) that no potion on hand already covers.
	 *
	 * @param onHand unfinished potions in the inventory or a station, by type, excluding the vessel itself
	 */
	static boolean mixMatchesOpenOrder(PotionType mix, List<PotionOrder> orders, Map<PotionType, Integer> onHand)
	{
		return mixMatchesPlannedOrder(mix, orders, null, onHand);
	}

	/**
	 * Like {@link #mixMatchesOpenOrder}, counting only the orders the player plans to make: a mix that only a skipped
	 * order would take is one to correct, not to mix.
	 *
	 * @param recommendedSlots the planned order slots; null or empty plans every order
	 */
	static boolean mixMatchesPlannedOrder(PotionType mix, List<PotionOrder> orders, Set<Integer> recommendedSlots,
			Map<PotionType, Integer> onHand)
	{
		if (mix == null)
			return false;

		int open = 0;
		for (PotionOrder order : orders)
		{
			if (!order.fulfilled() && order.potionType() == mix && planned(order, recommendedSlots))
				open++;
		}

		return open > onHand.getOrDefault(mix, 0);
	}

	/**
	 * True when every planned order is fulfilled and the inventory still holds a finished potion for each.
	 *
	 * @param finishedOnHand finished potions in the inventory, by type
	 */
	static boolean readyToDeliver(List<PotionOrder> orders, Set<Integer> recommendedSlots,
			Map<PotionType, Integer> finishedOnHand)
	{
		Map<PotionType, Integer> remaining = new EnumMap<>(PotionType.class);
		remaining.putAll(finishedOnHand);
		boolean anyPlanned = false;
		for (PotionOrder order : orders)
		{
			if (!planned(order, recommendedSlots))
				continue;

			anyPlanned = true;
			int count = remaining.getOrDefault(order.potionType(), 0);
			if (!order.fulfilled() || count == 0)
				return false;

			remaining.put(order.potionType(), count - 1);
		}

		return anyPlanned;
	}
}
