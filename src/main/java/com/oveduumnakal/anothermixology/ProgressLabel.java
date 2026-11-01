package com.oveduumnakal.anothermixology;

/** What the middle of each resin bar shows. */
public enum ProgressLabel
{
	NONE("None"),
	PERCENT("Percent"),
	NUMBER("Number");

	private final String name;

	ProgressLabel(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
