package com.oveduumnakal.anothermixology;

/**
 * The three visible orders as a recommendation policy sees them: each slot's potion and modifier, whether a digweed
 * already improved the slot's potion, and how many digweed the player holds. Arrays are indexed by order slot; an
 * entry is null (or false) when it is not known.
 */
public final class OrderSet
{
	private final PotionType[] orders;
	private final PotionModifier[] modifiers;
	private final boolean[] improved;
	private final int digweed;

	public OrderSet(PotionType[] orders, PotionModifier[] modifiers, boolean[] improved, int digweed)
	{
		this.orders = orders;
		this.modifiers = modifiers;
		this.improved = improved;
		this.digweed = digweed;
	}

	/** Returns the orders with no modifier, improvement or digweed known. */
	public static OrderSet of(PotionType[] orders)
	{
		return new OrderSet(orders, new PotionModifier[orders.length], new boolean[orders.length], 0);
	}

	public PotionType[] orders()
	{
		return orders;
	}

	public PotionModifier[] modifiers()
	{
		return modifiers;
	}

	/** Returns, per slot, whether a digweed already doubled that order's potion. */
	public boolean[] improved()
	{
		return improved;
	}

	/** Returns how many digweed the player holds, ready to double a potion. */
	public int digweed()
	{
		return digweed;
	}
}
