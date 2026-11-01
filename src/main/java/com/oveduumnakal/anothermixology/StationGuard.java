package com.oveduumnakal.anothermixology;

import java.util.List;
import java.util.Set;

import net.runelite.client.util.Text;

/**
 * Decides which clicks on the Alembic, Agitator and Retort are consumed: stations that have nothing to do, and
 * Concentrate clicks that would load the next potion.
 */
final class StationGuard
{
	/**
	 * Maximum value of {@code VarbitID.MM_RETORT_PROGRESS}, a fixed game constant: the bar runs from 0 to 16, then
	 * resets to 0 on completion.
	 */
	static final int RETORT_PROGRESS_MAX = 16;

	private StationGuard()
	{
	}

	static AlchemyObject stationFromTarget(String rawTarget)
	{
		if (rawTarget == null)
			return null;

		String target = Text.removeTags(rawTarget);
		if (target.equals("Alembic"))
			return AlchemyObject.ALEMBIC;

		if (target.equals("Agitator"))
			return AlchemyObject.AGITATOR;

		if (target.equals("Retort"))
			return AlchemyObject.RETORT;

		return null;
	}

	static boolean isStationNeeded(AlchemyObject station, boolean occupied, List<PotionOrder> orders,
			Set<Integer> heldItemIds)
	{
		if (occupied)
			return true;

		for (PotionOrder order : orders)
		{
			if (!order.fulfilled()
					&& order.potionModifier().alchemyObject() == station
					&& heldItemIds.contains(order.potionType().itemId()))
				return true;
		}

		return false;
	}

	/**
	 * Whether passive fill alone completes the potion on the next tick. A click then finishes no sooner, so it can
	 * be blocked. Earlier clicks still save at least one tick, whatever the per-click progress.
	 */
	static boolean concentrateCapReached(int currentProgress, int maxProgress)
	{
		return currentProgress >= maxProgress - 1;
	}

	/**
	 * Whether the Retort is running and a Concentrate click still finishes it sooner: from the first progress up to,
	 * but not including, the last tick.
	 */
	static boolean concentrateWorthClicking(int currentProgress, int maxProgress)
	{
		return currentProgress > 0 && !concentrateCapReached(currentProgress, maxProgress);
	}
}
