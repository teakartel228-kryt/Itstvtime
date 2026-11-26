package com.tvtime.registry;

import com.tvtime.TvTimeMod;
import com.tvtime.block.BearTrapBlock;
import com.tvtime.block.SpikeTrapBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public final class ModBlocks {
	public static final Block BEAR_TRAP = register("bear_trap", BearTrapBlock::new,
		BlockBehaviour.Properties.of().strength(3.0F, 6.0F).sound(SoundType.CHAIN).noOcclusion().noCollision().requiresCorrectToolForDrops());
	public static final Block SPIKE_TRAP = register("spike_trap", SpikeTrapBlock::new,
		BlockBehaviour.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL).noOcclusion().noCollision().requiresCorrectToolForDrops());

	private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, TvTimeMod.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(props.setId(key)));
	}
	public static void init() {}
}
