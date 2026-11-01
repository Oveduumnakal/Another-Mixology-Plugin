package com.oveduumnakal.anothermixology;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Tests for the labels of {@link GoalInfoBoxOverlay}. */
public class GoalInfoBoxOverlayTest
{
	@Test
	public void percentRoundsDownSoOnlyDoneShowsAHundred()
	{
		assertEquals("99%", GoalInfoBoxOverlay.percent(0.999));
		assertEquals("100%", GoalInfoBoxOverlay.percent(1));
		assertEquals("0%", GoalInfoBoxOverlay.percent(0));
	}

	@Test
	public void barMiddleFollowsTheShowProgressSetting()
	{
		var data = new Goal.ComponentData(2500, 10000);
		assertEquals("25%", GoalInfoBoxOverlay.centerLabel(ProgressLabel.PERCENT, data));
		assertEquals("(7,500)", GoalInfoBoxOverlay.centerLabel(ProgressLabel.NUMBER, data));
		assertEquals("", GoalInfoBoxOverlay.centerLabel(ProgressLabel.NONE, data));
	}
}
