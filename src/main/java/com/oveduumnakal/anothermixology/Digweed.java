package com.oveduumnakal.anothermixology;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.runelite.api.gameval.ItemID;
import net.runelite.client.util.Text;

/**
 * Digweed handling that needs no client state: the item, reading which potion a digweed improved from the chat, and
 * which order that potion belongs to. A digweed doubles one potion's resin; the improved potion keeps its item id, so
 * the chat message is the only sign of which one it was.
 */
final class Digweed
{
	static final int ITEM_ID = ItemID.MM_LAB_SPECIAL_HERB;

	/**
	 * Matches "You mix the digweed into the Mixalot, drastically improving its quality!"; the OSRS Wiki quotes the same
	 * ending for the reverse use of a potion on the digweed. The game wraps the potion name in a colour code such as
	 * {@code @mes_hl_pur@}, which {@link #COLOUR_CODE} strips.
	 */
	private static final Pattern IMPROVED = Pattern.compile("into the (.+?), drastically improving its quality",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern COLOUR_CODE = Pattern.compile("@[a-z_]+@");

	private Digweed()
	{
	}

	/**
	 * Returns the potion a digweed just improved, read from a chat message, or null if the message is not about
	 * digweed.
	 */
	static PotionType improvedPotion(String message)
	{
		Matcher matcher = IMPROVED.matcher(COLOUR_CODE.matcher(Text.removeTags(message)).replaceAll(""));
		if (!matcher.find())
			return null;

		String name = matcher.group(1).trim();
		for (PotionType type : PotionType.TYPES)
		{
			if (String.join("", type.nameParts()).equalsIgnoreCase(name))
				return type;
		}

		return null;
	}

	/**
	 * Picks the order an improved potion belongs to among the orders of its type not yet improved: the digweed target
	 * slot first, then a recommended order, then any, each in display order.
	 *
	 * @param targetSlot the slot the digweed was highlighted for, or -1
	 * @return the order, or null if no order of that type is left to improve
	 */
	static PotionOrder orderImproved(List<PotionOrder> orders, PotionType type, int targetSlot,
			Set<Integer> recommendedSlots)
	{
		PotionOrder planned = null;
		PotionOrder any = null;
		for (PotionOrder order : orders)
		{
			if (order.potionType() != type || order.improved())
				continue;

			if (order.idx() == targetSlot)
				return order;

			if (planned == null && MixGuidance.planned(order, recommendedSlots))
				planned = order;

			if (any == null)
				any = order;
		}

		return planned != null ? planned : any;
	}
}
