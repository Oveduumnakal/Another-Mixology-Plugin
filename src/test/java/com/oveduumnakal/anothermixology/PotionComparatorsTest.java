package com.oveduumnakal.anothermixology;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/** Tests for {@link PotionComparators} and {@link PotionModifier#from(int)}. */
public class PotionComparatorsTest
{
	private static final PotionOrder RETORT = new PotionOrder(0, PotionType.MIXALOT, PotionModifier.CONCENTRATED);
	private static final PotionOrder AGITATOR = new PotionOrder(1, PotionType.MIXALOT, PotionModifier.HOMOGENOUS);
	private static final PotionOrder ALEMBIC = new PotionOrder(2, PotionType.MIXALOT, PotionModifier.CRYSTALISED);

	@Test
	public void byStationSortsAlembicAgitatorRetort()
	{
		assertEquals(List.of(ALEMBIC, AGITATOR, RETORT), sorted(PotionComparators.byStation()));
	}

	@Test
	public void shortestPathSortsAlembicRetortAgitator()
	{
		assertEquals(List.of(ALEMBIC, RETORT, AGITATOR), sorted(PotionComparators.shortestPath()));
	}

	@Test
	public void byStationBreaksTiesByPotionName()
	{
		PotionOrder azure = new PotionOrder(0, PotionType.AZURE_AURA_MIX, PotionModifier.CRYSTALISED);
		PotionOrder alco = new PotionOrder(1, PotionType.ALCO_AUGMENTATOR, PotionModifier.CRYSTALISED);
		List<PotionOrder> orders = new ArrayList<>(List.of(azure, alco));

		orders.sort(PotionComparators.byStation());

		assertEquals(List.of(alco, azure), orders);
	}

	@Test
	public void modifierFromRejectsOutOfRangeValues()
	{
		assertNull(PotionModifier.from(-1));
		assertNull(PotionModifier.from(PotionModifier.values().length));
		assertEquals(PotionModifier.HOMOGENOUS, PotionModifier.from(0));
	}

	private static List<PotionOrder> sorted(Comparator<PotionOrder> comparator)
	{
		List<PotionOrder> orders = new ArrayList<>(List.of(RETORT, AGITATOR, ALEMBIC));
		orders.sort(comparator);
		return orders;
	}
}
