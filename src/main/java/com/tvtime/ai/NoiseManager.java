package com.tvtime.ai;

import com.tvtime.TvTimeConfig;
import com.tvtime.TvTimeMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Remembers "where was that loud noise" per zombie. Fed by ServerLevelSoundMixin, read by ZombieNoiseGoal. */
public final class NoiseManager {
	public record Noise(Vec3 pos, long expiresAt) {}

	private static final Map<UUID, Noise> NOISES = new HashMap<>();
	private static boolean logged = false;

	private NoiseManager() {}

	public static Noise get(UUID id) { return NOISES.get(id); }
	public static void clear(UUID id) { NOISES.remove(id); }
	public static void cleanup(long now) { NOISES.values().removeIf(n -> n.expiresAt < now); }

	public static void onSound(ServerLevel level, double x, double y, double z, SoundSource source, float volume) {
		TvTimeConfig cfg = TvTimeConfig.get();
		if (!cfg.zombiesHearNoise || volume < cfg.noiseVolumeThreshold) return;
		switch (source) { case AMBIENT, WEATHER, MUSIC, RECORDS, MASTER -> { return; } default -> {} }
		if (!logged) { logged = true; TvTimeMod.LOGGER.info("[tvtime] noise hook is active (first loud sound heard)"); }

		double range = Math.min(cfg.noiseMaxRange, volume * 16.0);
		Vec3 origin = new Vec3(x, y, z);
		long expires = level.getServer().getTickCount() + cfg.noiseMemoryTicks;
		AABB box = new AABB(x - range, y - range, z - range, x + range, y + range, z + range);
		for (Zombie zombie : level.getEntitiesOfClass(Zombie.class, box, SmartAI::isSmart)) {
			if (zombie.getTarget() != null) continue;                   // already busy with someone
			if (zombie.position().distanceToSqr(origin) > range * range) continue;
			NOISES.put(zombie.getUUID(), new Noise(origin, expires));
		}
	}
}
