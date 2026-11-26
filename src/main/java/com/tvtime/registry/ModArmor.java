package com.tvtime.registry;

import com.tvtime.TvTimeMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;

/**
 * Tenna armor sits between iron and diamond:
 * (helmet / chestplate / leggings / boots)
 *   iron:    2/6/5/2 = 15 points, durability x15, toughness 0
 *   diamond: 3/8/6/3 = 20 points, durability x33, toughness 2
 *   tenna:   3/7/5/2 = 17 points, durability x25, toughness 1
 */
public final class ModArmor {
	public static final int BASE_DURABILITY = 25;
	public static final ResourceKey<EquipmentAsset> ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, TvTimeMod.id("tenna"));
	public static final TagKey<Item> REPAIRS = TagKey.create(Registries.ITEM, TvTimeMod.id("repairs_tenna_armor"));

	public static final ArmorMaterial TENNA = new ArmorMaterial(
		BASE_DURABILITY,
		defense(),
		11, SoundEvents.ARMOR_EQUIP_IRON, 1.0F, 0.0F, REPAIRS, ASSET);

	private static EnumMap<ArmorType, Integer> defense() {
		EnumMap<ArmorType, Integer> m = new EnumMap<>(ArmorType.class);
		m.put(ArmorType.BOOTS, 2);
		m.put(ArmorType.LEGGINGS, 5);
		m.put(ArmorType.CHESTPLATE, 7);
		m.put(ArmorType.HELMET, 3);
		m.put(ArmorType.BODY, 7);
		return m;
	}

	private ModArmor() {}
}
