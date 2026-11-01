package com.oveduumnakal.anothermixology;

import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the Adaptive strategy, {@link MetaPolicy}. Its state is observed through its decisions: with a Mixalot
 * and two single-colour orders, the balanced rule brews all three while the bottleneck rule brews only the Mixalot.
 */
public class MetaPolicyTest
{
	private static final PotionType[] MAL_AND_SINGLES = {
			PotionType.MIXALOT, PotionType.ALCO_AUGMENTATOR, PotionType.LIPLACK_LIQUOR};
	private static final int[] NO_RESIN = {0, 0, 0};

	@Test
	public void unevenDeficitsUseTheBottleneckRule()
	{
		Set<Integer> brew = new MetaPolicy().decide(MAL_AND_SINGLES, NO_RESIN, new int[]{1000, 500, 100});

		assertEquals(Set.of(0), brew);
	}

	@Test
	public void evenDeficitsSwitchToTheBalancedRule()
	{
		Set<Integer> brew = new MetaPolicy().decide(MAL_AND_SINGLES, NO_RESIN, new int[]{1000, 1000, 1000});

		assertEquals(Set.of(0, 1, 2), brew);
	}

	/**
	 * The balanced state is entered below a 20 % spread and left only above 25 %, so a 22 % spread keeps it once
	 * entered while a fresh policy still uses the bottleneck rule.
	 */
	@Test
	public void balancedStateHasHysteresis()
	{
		int[] spread22 = {1000, 1000, 780};
		MetaPolicy policy = new MetaPolicy();
		policy.decide(MAL_AND_SINGLES, NO_RESIN, new int[]{1000, 1000, 1000});

		assertEquals(Set.of(0, 1, 2), policy.decide(MAL_AND_SINGLES, NO_RESIN, spread22));
		assertEquals(Set.of(0), new MetaPolicy().decide(MAL_AND_SINGLES, NO_RESIN, spread22));
	}

	@Test
	public void resetReturnsToTheBottleneckRule()
	{
		int[] spread22 = {1000, 1000, 780};
		MetaPolicy policy = new MetaPolicy();
		policy.decide(MAL_AND_SINGLES, NO_RESIN, new int[]{1000, 1000, 1000});
		policy.reset();

		assertEquals(Set.of(0), policy.decide(MAL_AND_SINGLES, NO_RESIN, spread22));
	}

	/** Without a Mixalot and with a single-colour order, the balanced rule brews only the best order. */
	@Test
	public void balancedRuleBrewsTheBestSingleOrderWithoutMixalot()
	{
		PotionType[] orders = {PotionType.MAMMOTH_MIGHT_MIX, PotionType.AQUALUX_AMALGAM, PotionType.LIPLACK_LIQUOR};

		Set<Integer> brew = new MetaPolicy().decide(orders, NO_RESIN, new int[]{1000, 1000, 1000});

		assertEquals(Set.of(1), brew);
	}

	@Test
	public void deficitNeverGoesBelowZero()
	{
		int[] deficit = MetaPolicy.deficit(new int[]{500, 0, 2000}, new int[]{1000, 1000, 1000});

		assertEquals(500, deficit[0]);
		assertEquals(1000, deficit[1]);
		assertEquals(0, deficit[2]);
	}

	@Test
	public void ranksColoursByDeficitWithTiesToTheLowerIndex()
	{
		Integer[] ranked = MetaPolicy.rankColours(new int[]{300, 900, 900});

		assertEquals(Integer.valueOf(1), ranked[0]);
		assertEquals(Integer.valueOf(2), ranked[1]);
		assertEquals(Integer.valueOf(0), ranked[2]);
	}
}
