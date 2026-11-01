package com.oveduumnakal.anothermixology;

import java.util.Arrays;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.runelite.api.gameval.ItemID;

import static com.oveduumnakal.anothermixology.PotionComponent.AGA;
import static com.oveduumnakal.anothermixology.PotionComponent.LYE;
import static com.oveduumnakal.anothermixology.PotionComponent.MOX;

/** Every potion type, with its unfinished and finished item ids and its recipe of three resin components. */
public enum PotionType
{
	MAMMOTH_MIGHT_MIX(ItemID.MM_POTION_MMM_UNFINISHED, ItemID.MM_POTION_MMM_FINISHED, MOX, MOX, MOX),
	MYSTIC_MANA_AMALGAM(ItemID.MM_POTION_MMA_UNFINISHED, ItemID.MM_POTION_MMA_FINISHED, MOX, MOX, AGA),
	MARLEYS_MOONLIGHT(ItemID.MM_POTION_MML_UNFINISHED, ItemID.MM_POTION_MML_FINISHED, MOX, MOX, LYE),
	ALCO_AUGMENTATOR(ItemID.MM_POTION_AAA_UNFINISHED, ItemID.MM_POTION_AAA_FINISHED, AGA, AGA, AGA),
	AZURE_AURA_MIX(ItemID.MM_POTION_AAM_UNFINISHED, ItemID.MM_POTION_AAM_FINISHED, AGA, AGA, MOX),
	AQUALUX_AMALGAM(ItemID.MM_POTION_AAL_UNFINISHED, ItemID.MM_POTION_AAL_FINISHED, AGA, LYE, AGA),
	LIPLACK_LIQUOR(ItemID.MM_POTION_LLL_UNFINISHED, ItemID.MM_POTION_LLL_FINISHED, LYE, LYE, LYE),
	MEGALITE_LIQUID(ItemID.MM_POTION_LLM_UNFINISHED, ItemID.MM_POTION_LLM_FINISHED, MOX, LYE, LYE),
	ANTI_LEECH_LOTION(ItemID.MM_POTION_LLA_UNFINISHED, ItemID.MM_POTION_LLA_FINISHED, AGA, LYE, LYE),
	MIXALOT(ItemID.MM_POTION_MAL_UNFINISHED, ItemID.MM_POTION_MAL_FINISHED, MOX, AGA, LYE);

	public static final PotionType[] TYPES = PotionType.values();

	private static final Map<Integer, PotionType> ITEM_MAP;

	static
	{
		var builder = new ImmutableMap.Builder<Integer, PotionType>();
		for (var p : PotionType.values())
		{
			builder.put(p.itemId(), p);
			builder.put(p.modifiedItemId(), p);
		}

		ITEM_MAP = builder.build();
	}

	private final int itemId;
	private final int modifiedItemId;
	private final String abbreviation;
	private final PotionComponent[] components;
	private final int[] resinYield;
	private final int colourCount;
	private final int orderWeight;

	PotionType(int itemId, int modifiedItemId, PotionComponent... components)
	{
		this.itemId = itemId;
		this.modifiedItemId = modifiedItemId;
		this.components = components;
		this.abbreviation = "" + components[0].character() + components[1].character() + components[2].character();
		this.resinYield = resinYield(components);
		this.colourCount = distinctColours(components);
		this.orderWeight = 6 - colourCount;
	}

	/**
	 * Works out the resin an ordered potion gives per colour: 20 of a colour used two or three times, 10 of the single
	 * colour in a two-colour potion, and 20 of each colour in Mixalot.
	 */
	private static int[] resinYield(PotionComponent[] components)
	{
		int[] count = new int[PotionComponent.ENTRIES.length];
		for (var component : components)
			count[component.ordinal()]++;

		boolean twoColours = distinctColours(components) == 2;
		int[] yield = new int[count.length];
		for (int c = 0; c < count.length; c++)
		{
			if (count[c] == 0)
				continue;

			yield[c] = count[c] == 1 && twoColours ? 10 : 20;
		}

		return yield;
	}

	private static int distinctColours(PotionComponent[] components)
	{
		return (int) Arrays.stream(components)
				.distinct()
				.count();
	}

	public static PotionType fromItemId(int itemId)
	{
		return ITEM_MAP.get(itemId);
	}

	public static PotionType fromIdx(int potionTypeId)
	{
		if (potionTypeId < 0 || potionTypeId >= TYPES.length)
			return null;

		return TYPES[potionTypeId];
	}

	public String recipeBlocks(int[] iconIdx)
	{
		StringBuilder sb = new StringBuilder(18);
		for (PotionComponent c : components)
			sb.append("<img=")
					.append(iconIdx[c.ordinal()])
					.append('>');

		return sb.toString();
	}

	public int itemId()
	{
		return itemId;
	}

	public int modifiedItemId()
	{
		return modifiedItemId;
	}

	public PotionComponent[] components()
	{
		return components;
	}

	/** Returns the base resin of the given colour index (0 mox, 1 aga, 2 lye) that fulfilling an order gives. */
	public int resinYield(int colour)
	{
		return resinYield[colour];
	}

	/**
	 * Returns the potion's relative chance of being ordered: 5 for single-colour potions, 4 for two-colour potions and
	 * 3 for Mixalot, as listed on the OSRS Wiki.
	 */
	/** Returns how many different resin colours the recipe uses: 1, 2 or 3 (Mixalot). */
	public int colourCount()
	{
		return colourCount;
	}

	public int orderWeight()
	{
		return orderWeight;
	}

	/**
	 * Returns the potion's name split into the three parts that stand for its three components, in recipe order.
	 * Joining the parts gives the name. Each part starts with its component's letter, except in Mixalot ("ot").
	 */
	public String[] nameParts()
	{
		switch (this)
		{
			case MAMMOTH_MIGHT_MIX: return new String[]{"Mammoth", "-might ", "mix"};
			case MYSTIC_MANA_AMALGAM: return new String[]{"Mystic ", "mana ", "amalgam"};
			case MARLEYS_MOONLIGHT: return new String[]{"Marley's ", "moon", "light"};
			case ALCO_AUGMENTATOR: return new String[]{"Alco", "-augment", "ator"};
			case AZURE_AURA_MIX: return new String[]{"Azure ", "aura ", "mix"};
			case AQUALUX_AMALGAM: return new String[]{"Aqua", "lux ", "amalgam"};
			case LIPLACK_LIQUOR: return new String[]{"Lip", "lack ", "liquor"};
			case MEGALITE_LIQUID: return new String[]{"Mega", "lite ", "liquid"};
			case ANTI_LEECH_LOTION: return new String[]{"Anti", "-leech ", "lotion"};
			default: return new String[]{"Mix", "al", "ot"};
		}
	}

	public String abbreviation()
	{
		return abbreviation;
	}
}
