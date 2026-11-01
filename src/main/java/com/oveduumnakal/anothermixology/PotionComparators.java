package com.oveduumnakal.anothermixology;

import java.util.Comparator;

/** Orderings for the potion orders shown in the interface. */
public class PotionComparators
{
	/**
	 * Orders by modifier, crystalised before homogenous before concentrated, then by potion type name.
	 * Concentrate comes last to avoid misclicks on the retort.
	 */
	public static Comparator<PotionOrder> byStation()
	{
		return Comparator.comparingInt((PotionOrder order) ->
		{
			switch (order.potionModifier())
			{
				case CRYSTALISED:
					return 0;
				case HOMOGENOUS:
					return 1;
				case CONCENTRATED:
					return 2;
				default:
					return Integer.MAX_VALUE;
			}
		})
				.thenComparing(order -> order.potionType().name());
	}

	/** Orders by modifier, crystalised then concentrated then homogenous (the "Shortest Path" sorting option). */
	public static Comparator<PotionOrder> shortestPath()
	{
		return Comparator.comparing(order ->
		{
			switch (order.potionModifier())
			{
				case CRYSTALISED:
					return 1;
				case CONCENTRATED:
					return 2;
				case HOMOGENOUS:
					return 3;
				default:
					throw new IllegalStateException("Unexpected value: " + order.potionModifier().toString());
			}
		});
	}
}
