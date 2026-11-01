package com.oveduumnakal.anothermixology;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the rows of the rewards panel: one per tracked reward, then one for all of them together. Each row has the
 * resin target its time estimate aims for and its progress, the share of its resin cost already covered.
 */
final class RewardRows
{
	private RewardRows()
	{
	}

	/** One row of the rewards panel. */
	static final class Row
	{
		/** The reward, or null for the all-rewards row. */
		final RewardItem item;
		final String label;

		/** The resin per colour the time estimate aims for, counted from the player's current resin. */
		final int[] target;
		final double progress;

		Row(RewardItem item, String label, int[] target, double progress)
		{
			this.item = item;
			this.label = label;
			this.target = target;
			this.progress = progress;
		}

		/** True when the player can already afford what this row counts. */
		boolean ready()
		{
			return progress >= 1;
		}
	}

	/**
	 * Returns the reward rows followed by the all-rewards row. With {@link RewardOrder#EACH_ON_ITS_OWN} each reward
	 * counts all the resin on its own; with {@link RewardOrder#IN_ORDER} rewards are bought top to bottom, so each
	 * counts only the resin left after the rewards above it, and its target includes theirs.
	 */
	static List<Row> build(List<Goal.Selection> selections, int[] resin, RewardOrder order)
	{
		List<Row> rows = new ArrayList<>();
		int[] above = new int[3];
		int[] all = new int[3];
		for (var selection : selections)
		{
			int[] cost = new int[3];
			for (var component : PotionComponent.ENTRIES)
				cost[component.ordinal()] = selection.item.componentCost(component) * selection.quantity;

			boolean inOrder = order == RewardOrder.IN_ORDER;
			int[] target = new int[3];
			int[] available = new int[3];
			for (int colour = 0; colour < 3; colour++)
			{
				target[colour] = cost[colour] + (inOrder ? above[colour] : 0);
				available[colour] = resin[colour] - (inOrder ? above[colour] : 0);
				above[colour] += cost[colour];
				all[colour] += cost[colour];
			}

			String label = selection.quantity > 1
					? selection.item.itemName() + " x" + selection.quantity
					: selection.item.itemName();
			rows.add(new Row(selection.item, label, target, progress(available, cost)));
		}

		rows.add(new Row(null, "All Rewards:", all, progress(resin, all)));
		return rows;
	}

	/** Returns the share of the cost covered, summed over the colours, from 0 to 1; 1 when nothing is needed. */
	static double progress(int[] available, int[] cost)
	{
		int covered = 0;
		int total = 0;
		for (int colour = 0; colour < 3; colour++)
		{
			covered += Math.max(0, Math.min(available[colour], cost[colour]));
			total += cost[colour];
		}

		return total == 0 ? 1 : covered / (double) total;
	}

	/** Formats seconds as {@code 08h 53m 09s}. */
	static String formatTime(double seconds)
	{
		long total = Math.round(seconds);
		return String.format("%02dh %02dm %02ds", total / 3600, total / 60 % 60, total % 60);
	}
}
