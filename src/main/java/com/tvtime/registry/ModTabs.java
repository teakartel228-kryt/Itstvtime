package com.tvtime.registry;

import com.tvtime.TvTimeMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModTabs {
	public static final CreativeModeTab TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TvTimeMod.id("main"),
		FabricItemGroup.builder()
			.title(Component.translatable("itemGroup.tvtime"))
			.icon(() -> new ItemStack(ModItems.TENNA_HELMET))
			.displayItems((params, out) -> {
				out.accept(ModItems.ANT_TENNA_EGG);
				out.accept(ModItems.TV_REMOTE);
				out.accept(ModItems.SMART_ZOMBIE_EGG);
				out.accept(ModItems.FAST_SMART_ZOMBIE_EGG);
				out.accept(ModItems.SLOW_SMART_ZOMBIE_EGG);
				out.accept(ModItems.TENNA_HELMET);
				out.accept(ModItems.TENNA_CHESTPLATE);
				out.accept(ModItems.TENNA_LEGGINGS);
				out.accept(ModItems.TENNA_BOOTS);
				out.accept(ModItems.TV_SCRAP);
				out.accept(ModItems.DISC_TV_TIME);
				out.accept(ModItems.DISC_TENNA_EX);
				out.accept(ModItems.DISC_END_YOU);
				out.accept(ModItems.DISC_KAIZO);
				out.accept(ModItems.BEAR_TRAP);
				out.accept(ModItems.SPIKE_TRAP);
			}).build());

	public static void init() {}
}
