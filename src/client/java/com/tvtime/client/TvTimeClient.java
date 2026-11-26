package com.tvtime.client;

import com.tvtime.registry.ModBlocks;
import com.tvtime.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ZombieRenderer;

public class TvTimeClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEntities.SMART_ZOMBIE, ZombieRenderer::new);
		EntityRenderers.register(ModEntities.FAST_SMART_ZOMBIE, ZombieRenderer::new);
		EntityRenderers.register(ModEntities.SLOW_SMART_ZOMBIE, ZombieRenderer::new);
		EntityRenderers.register(ModEntities.ANT_TENNA, TennaRenderer::new);

		// custom trap models contain thin parts -> cutout_mipped keeps MIPMAPS (plain "cutout" would switch them off)
		BlockRenderLayerMap.putBlock(ModBlocks.BEAR_TRAP, ChunkSectionLayer.CUTOUT_MIPPED);
		BlockRenderLayerMap.putBlock(ModBlocks.SPIKE_TRAP, ChunkSectionLayer.CUTOUT_MIPPED);
	}
}
