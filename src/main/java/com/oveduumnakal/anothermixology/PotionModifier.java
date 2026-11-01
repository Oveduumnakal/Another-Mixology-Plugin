package com.oveduumnakal.anothermixology;

/** The extra processing step an order calls for, with the station that does it. */
public enum PotionModifier
{
	/** Agitated on the Agitator. */
	HOMOGENOUS(AlchemyObject.AGITATOR),
	/** Concentrated on the Retort. */
	CONCENTRATED(AlchemyObject.RETORT),
	/** Crystallised on the Alembic. */
	CRYSTALISED(AlchemyObject.ALEMBIC);

	private static final PotionModifier[] TYPES = PotionModifier.values();

	private final AlchemyObject alchemyObject;

	PotionModifier(AlchemyObject alchemyObject)
	{
		this.alchemyObject = alchemyObject;
	}

	public static PotionModifier from(int potionModifierId)
	{
		if (potionModifierId < 0 || potionModifierId >= TYPES.length)
			return null;

		return TYPES[potionModifierId];
	}

	public AlchemyObject alchemyObject()
	{
		return alchemyObject;
	}
}
