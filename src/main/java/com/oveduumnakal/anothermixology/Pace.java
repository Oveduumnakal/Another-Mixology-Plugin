package com.oveduumnakal.anothermixology;

/** How long the player takes per potion and per delivery, in seconds. */
final class Pace
{
	/**
	 * The fit over 40 order sets of 2026-10-29 lab play: 11.3 seconds per delivery, and 15.5, 18.7 and 15.4 seconds per
	 * Alembic, Retort and Agitator potion, averaged here since every modifier is ordered equally often.
	 */
	static final Pace DEFAULT = new Pace((15.5 + 18.7 + 15.4) / 3, 11.3);

	final double secondsPerPotion;
	final double secondsPerDelivery;

	Pace(double secondsPerPotion, double secondsPerDelivery)
	{
		this.secondsPerPotion = secondsPerPotion;
		this.secondsPerDelivery = secondsPerDelivery;
	}

	@Override
	public boolean equals(Object other)
	{
		if (!(other instanceof Pace))
			return false;

		var pace = (Pace) other;
		return pace.secondsPerPotion == secondsPerPotion && pace.secondsPerDelivery == secondsPerDelivery;
	}

	@Override
	public int hashCode()
	{
		return Double.hashCode(secondsPerPotion) * 31 + Double.hashCode(secondsPerDelivery);
	}
}
