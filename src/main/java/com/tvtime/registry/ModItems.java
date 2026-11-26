package com.tvtime.registry;

import com.tvtime.TvTimeMod;
import com.tvtime.item.TvRemoteItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;

public final class ModItems {
	public static final Item TV_SCRAP = register("tv_scrap", Item::new, new Item.Properties());
	public static final Item TV_REMOTE = register("tv_remote", TvRemoteItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

	// armor (durability = base * vanilla slot multiplier 11/16/15/13)
	public static final Item TENNA_HELMET = register("tenna_helmet", Item::new,
		new Item.Properties().humanoidArmor(ModArmor.TENNA, ArmorType.HELMET).durability(ArmorType.HELMET.getDurability(ModArmor.BASE_DURABILITY)));
	public static final Item TENNA_CHESTPLATE = register("tenna_chestplate", Item::new,
		new Item.Properties().humanoidArmor(ModArmor.TENNA, ArmorType.CHESTPLATE).durability(ArmorType.CHESTPLATE.getDurability(ModArmor.BASE_DURABILITY)));
	public static final Item TENNA_LEGGINGS = register("tenna_leggings", Item::new,
		new Item.Properties().humanoidArmor(ModArmor.TENNA, ArmorType.LEGGINGS).durability(ArmorType.LEGGINGS.getDurability(ModArmor.BASE_DURABILITY)));
	public static final Item TENNA_BOOTS = register("tenna_boots", Item::new,
		new Item.Properties().humanoidArmor(ModArmor.TENNA, ArmorType.BOOTS).durability(ArmorType.BOOTS.getDurability(ModArmor.BASE_DURABILITY)));

	// music discs
	public static final Item DISC_TV_TIME = disc("music_disc_tv_time", ModSounds.SONG_TV_TIME);
	public static final Item DISC_TENNA_EX = disc("music_disc_tenna_ex", ModSounds.SONG_TENNA_EX);
	public static final Item DISC_END_YOU = disc("music_disc_end_you", ModSounds.SONG_END_YOU);
	public static final Item DISC_KAIZO = disc("music_disc_kaizo", ModSounds.SONG_KAIZO);

	// blocks
	public static final Item BEAR_TRAP = block("bear_trap", ModBlocks.BEAR_TRAP);
	public static final Item SPIKE_TRAP = block("spike_trap", ModBlocks.SPIKE_TRAP);

	// spawn eggs
	public static final Item SMART_ZOMBIE_EGG = egg("smart_zombie_spawn_egg", ModEntities.SMART_ZOMBIE);
	public static final Item FAST_SMART_ZOMBIE_EGG = egg("fast_smart_zombie_spawn_egg", ModEntities.FAST_SMART_ZOMBIE);
	public static final Item SLOW_SMART_ZOMBIE_EGG = egg("slow_smart_zombie_spawn_egg", ModEntities.SLOW_SMART_ZOMBIE);
	public static final Item ANT_TENNA_EGG = egg("ant_tenna_spawn_egg", ModEntities.ANT_TENNA);

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties props) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, TvTimeMod.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(props.setId(key)));
	}
	private static Item disc(String name, ResourceKey<net.minecraft.world.item.JukeboxSong> song) {
		return register(name, Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(song));
	}
	private static Item block(String name, Block block) {
		return register(name, p -> new BlockItem(block, p), new Item.Properties().useBlockDescriptionPrefix());
	}
	private static Item egg(String name, EntityType<?> type) {
		return register(name, SpawnEggItem::new, new Item.Properties().spawnEgg(type));
	}
	public static void init() {}
}
