package com.oveduumnakal.anothermixology;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/** Tests for {@link ColorblindMode}. */
public class ColorblindModeTest
{
	@Test
	public void offHasNoPresetColours()
	{
		for (PotionComponent component : PotionComponent.values())
			assertNull(ColorblindMode.OFF.color(component));
	}

	@Test
	public void eachPresetGivesThreeDifferentColours()
	{
		for (ColorblindMode mode : ColorblindMode.values())
		{
			if (mode == ColorblindMode.OFF)
				continue;

			Set<Integer> colours = new HashSet<>();
			for (PotionComponent component : PotionComponent.values())
				colours.add(mode.color(component).getRGB());

			assertEquals(mode.name(), 3, colours.size());
		}
	}
}
