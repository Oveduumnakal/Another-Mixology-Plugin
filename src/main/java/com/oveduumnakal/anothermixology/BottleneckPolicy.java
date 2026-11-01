package com.oveduumnakal.anothermixology;

import java.util.Set;

/**
 * Static bottleneck rule: brew all 3 orders if at least 2 of them give the colour furthest behind its target;
 * otherwise brew only the orders that give it; otherwise brew the single order that cuts the deficit most. It is the
 * adaptive policy's single-bottleneck rule on its own, with no state.
 */
public class BottleneckPolicy implements RecommendationPolicy
{
	@Override
	public Set<Integer> decide(PotionType[] orders, int[] resin, int[] target)
	{
		int[] deficit = MetaPolicy.deficit(resin, target);
		return MetaPolicy.singleBn(orders, deficit, MetaPolicy.rankColours(deficit)[0]);
	}
}
