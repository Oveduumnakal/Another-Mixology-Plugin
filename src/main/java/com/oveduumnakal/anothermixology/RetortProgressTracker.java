package com.oveduumnakal.anothermixology;

/**
 * Tracks the Retort's progress bar so concentrate clicks can be capped on the last tick before completion.
 *
 * <p>The current progress resets for each potion, so a value left over from the previous potion never counts.
 */
class RetortProgressTracker
{
	private boolean potionLoaded;
	private int currentProgress;

	void onPotionLoaded()
	{
		potionLoaded = true;
		currentProgress = 0;
	}

	void onPotionCleared()
	{
		potionLoaded = false;
		currentProgress = 0;
	}

	void onProgressChanged(int value)
	{
		if (!potionLoaded)
			return;

		currentProgress = value;
	}

	int currentProgress()
	{
		return currentProgress;
	}

	boolean potionLoaded()
	{
		return potionLoaded;
	}
}
