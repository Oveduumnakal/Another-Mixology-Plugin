package com.oveduumnakal.anothermixology;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/** Tests for {@link Digweed}. */
public class DigweedTest
{
	@Test
	public void readsTheImprovedPotionFromTheChat()
	{
		assertEquals(PotionType.MIXALOT,
				Digweed.improvedPotion("You mix the digweed into the Mixalot, drastically improving its quality!"));
		assertEquals(PotionType.MARLEYS_MOONLIGHT, Digweed.improvedPotion("You mix the digweed into the "
				+ "@mes_hl_pur@Marley's moonlight</col>, drastically improving its quality!"));
		assertEquals(PotionType.MARLEYS_MOONLIGHT, Digweed.improvedPotion("<col=ef1020>You mix the digweed into the "
				+ "Marley's moonlight, drastically improving its quality!</col>"));
		assertEquals(PotionType.MAMMOTH_MIGHT_MIX, Digweed.improvedPotion(
				"You mix the mammoth-might mix into the Mammoth-might mix, drastically improving its quality!"));
	}

	@Test
	public void ignoresOtherMessages()
	{
		assertNull(Digweed.improvedPotion("This potion has already been improved with digweed."));
		assertNull(Digweed.improvedPotion("You mix the digweed into the Soup, drastically improving its quality!"));
	}

	@Test
	public void improvedPotionGoesToTheTargetThenARecommendedOrder()
	{
		PotionOrder skipped = new PotionOrder(0, PotionType.MIXALOT, PotionModifier.CRYSTALISED);
		PotionOrder planned = new PotionOrder(1, PotionType.MIXALOT, PotionModifier.HOMOGENOUS);
		PotionOrder target = new PotionOrder(2, PotionType.MIXALOT, PotionModifier.CONCENTRATED);
		List<PotionOrder> orders = List.of(skipped, planned, target);

		assertSame(target, Digweed.orderImproved(orders, PotionType.MIXALOT, 2, Set.of(1, 2)));
		assertSame(planned, Digweed.orderImproved(orders, PotionType.MIXALOT, -1, Set.of(1, 2)));

		planned.setImproved(true);
		target.setImproved(true);
		assertSame(skipped, Digweed.orderImproved(orders, PotionType.MIXALOT, -1, Set.of(1, 2)));
		assertNull(Digweed.orderImproved(orders, PotionType.LIPLACK_LIQUOR, -1, Set.of(1, 2)));
	}
}
