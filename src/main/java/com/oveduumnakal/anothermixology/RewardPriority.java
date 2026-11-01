package com.oveduumnakal.anothermixology;

import java.util.ArrayList;
import java.util.List;

/**
 * The order the tracked rewards are listed and bought in, saved as a comma separated list of {@link RewardItem}
 * names. A reward goes to the bottom when it is ticked and leaves when it is unticked; the player moves one up or
 * down from the rewards panel's right-click menu. Free of client state so it can be unit tested.
 */
final class RewardPriority
{
	static final String CONFIG_KEY = "rewardPriority";

	private RewardPriority()
	{
	}

	/** Reads a saved order, skipping names that are not rewards. */
	static List<RewardItem> parse(String saved)
	{
		List<RewardItem> order = new ArrayList<>();
		if (saved == null)
			return order;

		for (String name : saved.split(","))
		{
			for (RewardItem item : RewardItem.values())
			{
				if (item.name().equals(name.trim()) && !order.contains(item))
					order.add(item);
			}
		}

		return order;
	}

	static String format(List<RewardItem> order)
	{
		List<String> names = new ArrayList<>();
		for (RewardItem item : order)
			names.add(item.name());

		return String.join(",", names);
	}

	/**
	 * Brings a saved order up to date with the ticked rewards: unticked ones are dropped and newly ticked ones are
	 * added at the bottom, in the order given.
	 */
	static List<RewardItem> reconcile(List<RewardItem> order, List<RewardItem> ticked)
	{
		List<RewardItem> result = new ArrayList<>(order);
		result.retainAll(ticked);
		for (RewardItem item : ticked)
		{
			if (!result.contains(item))
				result.add(item);
		}

		return result;
	}

	/** Returns the selections sorted into the saved order; any the order lacks keep their place after it. */
	static List<Goal.Selection> sort(List<Goal.Selection> selections, List<RewardItem> order)
	{
		List<Goal.Selection> sorted = new ArrayList<>();
		for (RewardItem item : order)
		{
			for (Goal.Selection selection : selections)
			{
				if (selection.item == item)
					sorted.add(selection);
			}
		}

		for (Goal.Selection selection : selections)
		{
			if (!order.contains(selection.item))
				sorted.add(selection);
		}

		return sorted;
	}

	/** Returns the order with the reward moved one place up (-1) or down (+1); unchanged at either end. */
	static List<RewardItem> move(List<RewardItem> order, RewardItem item, int direction)
	{
		List<RewardItem> result = new ArrayList<>(order);
		int from = result.indexOf(item);
		int to = from + direction;
		if (from < 0 || to < 0 || to >= result.size())
			return result;

		result.set(from, result.get(to));
		result.set(to, item);
		return result;
	}
}
