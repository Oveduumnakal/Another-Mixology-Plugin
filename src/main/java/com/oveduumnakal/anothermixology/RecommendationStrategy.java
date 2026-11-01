package com.oveduumnakal.anothermixology;

/** The rule set that marks each order brew or skip; skipped orders are drawn in the skip colour. */
public enum RecommendationStrategy
{
	EXPECTED_VALUE("Expected value"),
	BOTTLENECK("Bottleneck"),
	ADAPTIVE("Adaptive"),
	GREEDY("Greedy (brew all 3)");

	private final String name;

	RecommendationStrategy(String name)
	{
		this.name = name;
	}

	/** Returns a fresh policy for this strategy. */
	public RecommendationPolicy createPolicy()
	{
		switch (this)
		{
			case BOTTLENECK:
				return new BottleneckPolicy();
			case ADAPTIVE:
				return new MetaPolicy();
			case GREEDY:
				return (orders, resin, target) -> MetaPolicy.all3();
			default:
				return new ExpectedValuePolicy();
		}
	}

	@Override
	public String toString()
	{
		return name;
	}
}
