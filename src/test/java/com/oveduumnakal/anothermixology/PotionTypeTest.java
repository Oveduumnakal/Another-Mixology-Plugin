package com.oveduumnakal.anothermixology;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/** Tests for {@link PotionType}. */
public class PotionTypeTest
{
	/** The joined name parts must match the game's order text exactly, or the order names are not recoloured. */
	@Test
	public void namePartsJoinToTheInGameNames()
	{
		assertEquals("Mammoth-might mix", name(PotionType.MAMMOTH_MIGHT_MIX));
		assertEquals("Mystic mana amalgam", name(PotionType.MYSTIC_MANA_AMALGAM));
		assertEquals("Marley's moonlight", name(PotionType.MARLEYS_MOONLIGHT));
		assertEquals("Alco-augmentator", name(PotionType.ALCO_AUGMENTATOR));
		assertEquals("Azure aura mix", name(PotionType.AZURE_AURA_MIX));
		assertEquals("Aqualux amalgam", name(PotionType.AQUALUX_AMALGAM));
		assertEquals("Liplack liquor", name(PotionType.LIPLACK_LIQUOR));
		assertEquals("Megalite liquid", name(PotionType.MEGALITE_LIQUID));
		assertEquals("Anti-leech lotion", name(PotionType.ANTI_LEECH_LOTION));
		assertEquals("Mixalot", name(PotionType.MIXALOT));
	}

	/**
	 * Each name part starts, after any hyphen, with the letter of the component it stands for. Mixalot is the one
	 * exception: its parts are "Mix", "al" and "ot".
	 */
	@Test
	public void eachNamePartStartsWithItsComponentLetter()
	{
		for (PotionType type : PotionType.values())
		{
			if (type == PotionType.MIXALOT)
				continue;

			String[] parts = type.nameParts();
			PotionComponent[] components = type.components();
			for (int i = 0; i < parts.length; i++)
			{
				char first = Character.toUpperCase(parts[i].replace("-", "").charAt(0));
				assertEquals(type + " part " + i, components[i].character(), first);
			}
		}
	}

	@Test
	public void fromIdxRejectsOutOfRangeValues()
	{
		assertNull(PotionType.fromIdx(-1));
		assertNull(PotionType.fromIdx(PotionType.TYPES.length));
		assertEquals(PotionType.MAMMOTH_MIGHT_MIX, PotionType.fromIdx(0));
	}

	@Test
	public void fromItemIdFindsBothTheUnfinishedAndTheFinishedPotion()
	{
		for (PotionType type : PotionType.values())
		{
			assertEquals(type, PotionType.fromItemId(type.itemId()));
			assertEquals(type, PotionType.fromItemId(type.modifiedItemId()));
		}

		assertNull(PotionType.fromItemId(-1));
	}

	@Test
	public void abbreviationIsTheRecipeLetters()
	{
		assertEquals("MAL", PotionType.MIXALOT.abbreviation());
		assertEquals("ALA", PotionType.AQUALUX_AMALGAM.abbreviation());
	}

	@Test
	public void recipeBlocksUseTheIconOfEachComponent()
	{
		assertEquals("<img=10><img=11><img=12>", PotionType.MIXALOT.recipeBlocks(new int[]{10, 11, 12}));
	}

	private static String name(PotionType type)
	{
		return String.join("", type.nameParts());
	}
}
