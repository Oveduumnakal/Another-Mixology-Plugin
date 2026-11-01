package com.oveduumnakal.anothermixology;

import net.runelite.api.gameval.SpriteID;

/**
 * The three resin components, mox, aga and lye, with the letter and sprite that stand for each and the
 * varp that holds the player's stock of it.
 */
public enum PotionComponent
{
	MOX('M', SpriteID.IconAlchemyChemicals01_20x20._0, AnotherMixologyPlugin.VARP_MOX_RESIN),
	AGA('A', SpriteID.IconAlchemyChemicals01_20x20._1, AnotherMixologyPlugin.VARP_AGA_RESIN),
	LYE('L', SpriteID.IconAlchemyChemicals01_20x20._2, AnotherMixologyPlugin.VARP_LYE_RESIN);

	public static final PotionComponent[] ENTRIES = values();

	private final char character;
	private final int spriteId;
	private final int resinVarpId;

	PotionComponent(char character, int spriteId, int resinVarpId)
	{
		this.character = character;
		this.spriteId = spriteId;
		this.resinVarpId = resinVarpId;
	}

	public char character()
	{
		return character;
	}

	public int spriteId()
	{
		return spriteId;
	}

	public int resinVarpId()
	{
		return resinVarpId;
	}
}
