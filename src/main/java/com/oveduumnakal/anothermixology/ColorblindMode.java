package com.oveduumnakal.anothermixology;

import java.awt.Color;

/**
 * Preset paste colours for the three most common kinds of colour blindness. Off uses the Mox, Aga and Lye colours
 * from the Colors section.
 *
 * <p>Each preset was picked by simulating that colour blindness (Machado et al. 2009, full severity) and keeping the
 * colours far apart from each other, from the skip grey and from the dark interface background.
 */
public enum ColorblindMode
{
	OFF("Off", null, null, null),
	PROTANOPIA("Protanopia", new Color(0x3984FF), new Color(0xF0E442), new Color(0xD55E00)),
	DEUTERANOPIA("Deuteranopia", new Color(0x3984FF), new Color(0xF0E442), new Color(0xFF8FD8)),
	TRITANOPIA("Tritanopia", new Color(0x22D3EE), new Color(0xFF8FD8), new Color(0xFF0000));

	private final String name;
	private final Color mox;
	private final Color aga;
	private final Color lye;

	ColorblindMode(String name, Color mox, Color aga, Color lye)
	{
		this.name = name;
		this.mox = mox;
		this.aga = aga;
		this.lye = lye;
	}

	/** Returns the preset colour for a paste, or null when the mode is off. */
	public Color color(PotionComponent component)
	{
		switch (component)
		{
			case MOX: return mox;
			case AGA: return aga;
			default:  return lye;
		}
	}

	@Override
	public String toString()
	{
		return name;
	}
}
