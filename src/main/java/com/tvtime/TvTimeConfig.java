package com.tvtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** config/tvtime.json - created on first start. */
public final class TvTimeConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static TvTimeConfig instance = new TvTimeConfig();

	/** Vanilla zombies and husks also get the smart AI (block breaking, weapons, noise). */
	public boolean vanillaZombiesAreSmart = true;
	public boolean zombiesBreakBlocks = true;
	/** Block-breaking speed of vanilla zombies that got the smart AI: 1.0 = exactly like a player, 0.8 = a bit weaker. */
	public double zombieBreakSpeedMultiplier = 0.8;
	/** Spawn-egg zombies: normal = player speed, fast = faster than a player, slow = slower. */
	public double eggBreakSpeedNormal = 1.0;
	public double eggBreakSpeedFast = 2.0;
	public double eggBreakSpeedSlow = 0.4;
	/** Blocks harder than this are never broken (obsidian = 50). Dirt 0.5, planks 2, stone 1.5, iron ore 3. */
	public float zombieMaxBlockHardness = 5.0F;
	public boolean zombiesUseRangedWeapons = true;
	public boolean zombiesHearNoise = true;
	/** Sound volume that counts as "loud" (explosion 4, note block 3, goat horn 16, block break 1). */
	public float noiseVolumeThreshold = 1.5F;
	public double noiseMaxRange = 64.0;
	public int noiseMemoryTicks = 600;
	public int huntTicks = 400;
	public boolean bossMusic = true;

	public static TvTimeConfig get() { return instance; }

	public static void load() {
		Path file = FabricLoader.getInstance().getConfigDir().resolve("tvtime.json");
		try {
			if (Files.exists(file)) {
				TvTimeConfig c = GSON.fromJson(Files.readString(file), TvTimeConfig.class);
				if (c != null) instance = c;
			}
			Files.writeString(file, GSON.toJson(instance)); // rewrite so new options appear
		} catch (IOException | RuntimeException e) {
			TvTimeMod.LOGGER.error("Could not read tvtime.json, using defaults", e);
		}
	}
}
