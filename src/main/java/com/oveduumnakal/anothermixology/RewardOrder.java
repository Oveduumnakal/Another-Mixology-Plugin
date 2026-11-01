package com.oveduumnakal.anothermixology;

/** How each tracked reward's row counts the resin the player has. */
public enum RewardOrder
{
	IN_ORDER("In order"),
	EACH_ON_ITS_OWN("Each on its own");

	private final String name;

	RewardOrder(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
