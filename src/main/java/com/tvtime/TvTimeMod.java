package com.tvtime;

import com.tvtime.ai.NoiseManager;
import com.tvtime.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TvTimeMod implements ModInitializer {
	public static final String MOD_ID = "tvtime";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }

	@Override
	public void onInitialize() {
		TvTimeConfig.load();
		ModSounds.init();
		ModBlocks.init();
		ModEntities.init();
		ModItems.init();
		ModTabs.init();
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 200 == 0) NoiseManager.cleanup(server.getTickCount());
		});
		LOGGER.info("TV Time loaded - IT'S TV TIME!");
	}
}
