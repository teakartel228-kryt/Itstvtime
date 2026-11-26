package com.tvtime.client;

import com.tvtime.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ZombieRenderer;

public class TvTimeClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEntities.SMART_ZOMBIE, ZombieRenderer::new);
		EntityRenderers.register(ModEntities.FAST_SMART_ZOMBIE, ZombieRenderer::new);
		EntityRenderers.register(ModEntities.SLOW_SMART_ZOMBIE, ZombieRenderer::new);
		EntityRenderers.register(ModEntities.ANT_TENNA, TennaRenderer::new);
		// No BlockRenderLayerMap needed: since 26.1 the game picks the chunk layer (solid / cutout / translucent)
		// per quad from the texture's transparency, so the trap textures with transparent pixels render as cutout by themselves.
	}
}
