package com.oveduumnakal.anothermixology;

import java.util.Comparator;

/** How the order list in the interface is sorted. Vanilla leaves the game's own order untouched. */
public enum PotionOrderSorting
{
	VANILLA("Vanilla (random)", null),
	BY_STATION("By station", PotionComparators.byStation()),
	SHORTEST_PATH("Shortest Path", PotionComparators.shortestPath());

	private final String name;
	private final Comparator<PotionOrder> comparator;

	PotionOrderSorting(String name, Comparator<PotionOrder> comparator)
	{
		this.name = name;
		this.comparator = comparator;
	}

	public Comparator<PotionOrder> comparator()
	{
		return comparator;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
