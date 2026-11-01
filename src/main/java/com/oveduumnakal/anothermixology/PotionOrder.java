package com.oveduumnakal.anothermixology;

import java.util.List;

/**
 * One potion order on the conveyor: its slot, the potion type and modifier it asks for, whether it has been
 * fulfilled, and whether a digweed improved its potion.
 */
public class PotionOrder
{
	private final int idx;
	private final PotionType potionType;
	private final PotionModifier potionModifier;

	private boolean fulfilled;
	private boolean improved;

	public PotionOrder(int idx, PotionType potionType, PotionModifier potionModifier)
	{
		this.idx = idx;
		this.potionType = potionType;
		this.potionModifier = potionModifier;
	}

	public int idx()
	{
		return idx;
	}

	public PotionType potionType()
	{
		return potionType;
	}

	public PotionModifier potionModifier()
	{
		return potionModifier;
	}

	/**
	 * Copies the fulfilled and improved flags from the previous orders to the rebuilt ones that are the same order:
	 * same slot, potion and modifier. Rebuilding the list (a sorting change, re-entering the lab) then keeps "done"
	 * orders done, while a new order set, which starts from an empty list, starts fresh.
	 */
	static void keepFulfilled(List<PotionOrder> previous, List<PotionOrder> rebuilt)
	{
		for (PotionOrder order : rebuilt)
		{
			for (PotionOrder old : previous)
			{
				if (old.idx == order.idx && old.potionType == order.potionType
						&& old.potionModifier == order.potionModifier)
				{
					order.fulfilled |= old.fulfilled;
					order.improved |= old.improved;
				}
			}
		}
	}

	public void setImproved(boolean improved)
	{
		this.improved = improved;
	}

	/** Returns whether a digweed doubled this order's potion. */
	public boolean improved()
	{
		return improved;
	}

	public void setFulfilled(boolean fulfilled)
	{
		this.fulfilled = fulfilled;
	}

	public boolean fulfilled()
	{
		return fulfilled;
	}

	@Override
	public String toString()
	{
		return "PotionOrder{" +
				"idx=" + idx +
				", potionType=" + potionType +
				", potionModifier=" + potionModifier +
				'}';
	}
}
