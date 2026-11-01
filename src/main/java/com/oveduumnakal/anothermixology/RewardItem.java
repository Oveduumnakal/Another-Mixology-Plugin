package com.oveduumnakal.anothermixology;

import net.runelite.api.gameval.ItemID;

import static com.oveduumnakal.anothermixology.PotionComponent.AGA;
import static com.oveduumnakal.anothermixology.PotionComponent.LYE;
import static com.oveduumnakal.anothermixology.PotionComponent.MOX;

/**
 * The rewards that can be tracked, with their item id and their resin cost in each colour.
 */
public enum RewardItem
{
	NONE("None", 0, 0, 0, 0),
	APPRENTICE_POTION_PACK("Apprentice Potion Pack", ItemID.MM_POTION_PACK_LOW, 420, 70, 30),
	ADEPT_POTION_PACK("Adept Potion Pack", ItemID.MM_POTION_PACK_MED, 180, 440, 70),
	EXPERT_POTION_PACK("Expert Potion Pack", ItemID.MM_POTION_PACK_HIGH, 410, 320, 480),
	PRESCRIPTION_GOGGLES("Prescription Goggles", ItemID.MM_ALCHEMIST_HAT, 8600, 7000, 9350),
	ALCHEMIST_LABCOAT("Alchemist Labcoat", ItemID.MM_ALCHEMIST_BODY, 2250, 2800, 3700),
	ALCHEMIST_PANTS("Alchemist Pants", ItemID.MM_ALCHEMIST_LEGS, 2250, 2800, 3700),
	ALCHEMIST_GLOVES("Alchemist Gloves", ItemID.MM_ALCHEMIST_GLOVES, 2250, 2800, 3700),
	REAGENT_POUCH("Reagent Pouch", ItemID.MM_SECONDARY_POUCH, 13800, 11200, 15100),
	POTION_STORAGE("Potion Storage", ItemID.MM_POTION_STORAGE_UNLOCK_DUMMY, 7750, 6300, 8950),
	CHUGGING_BARREL("Chugging Barrel", ItemID.MM_PREPOT_DEVICE, 17250, 14000, 18600),
	ALCHEMISTS_AMULET("Alchemist's Amulet", ItemID.AMULET_OF_CHEMISTRY_IMBUED_UNCHARGED, 6900, 5650, 7400),
	ALDARIUM("Aldarium", ItemID.ALDARIUM, 80, 60, 90);

	private final String itemName;
	private final int itemId;
	private final int[] componentCost = new int[PotionComponent.ENTRIES.length];

	RewardItem(String itemName, int itemId, int moxResinCost, int agaResinCost, int lyeResinCost)
	{
		this.itemName = itemName;
		this.itemId = itemId;
		this.componentCost[MOX.ordinal()] = moxResinCost;
		this.componentCost[AGA.ordinal()] = agaResinCost;
		this.componentCost[LYE.ordinal()] = lyeResinCost;
	}

	public String itemName()
	{
		return itemName;
	}

	public int itemId()
	{
		return itemId;
	}

	public int componentCost(PotionComponent component)
	{
		return componentCost[component.ordinal()];
	}
}