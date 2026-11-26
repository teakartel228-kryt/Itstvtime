package com.tvtime.ai;

import com.tvtime.TvTimeConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * 1) walk to the place of a loud sound,
 * 2) look for the nearest survival player around it and lock on,
 * 3) hunt that player (re-pathing even without line of sight) until close enough for the normal melee goal.
 */
public class ZombieNoiseGoal extends Goal {
	private final Zombie mob;
	private ServerPlayer hunted;
	private int huntLeft;
	private int repath;
	private Vec3 noiseSpot;
	private int searchTicks;

	public ZombieNoiseGoal(Zombie mob) {
		this.mob = mob;
		setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (!(mob.level() instanceof ServerLevel)) return false;
		if (mob.getTarget() != null) return false;
		NoiseManager.Noise n = NoiseManager.get(mob.getUUID());
		return n != null && n.expiresAt() > mob.level().getServer().getTickCount();
	}

	@Override
	public void start() {
		NoiseManager.Noise n = NoiseManager.get(mob.getUUID());
		noiseSpot = n == null ? mob.position() : n.pos();
		hunted = null;
		searchTicks = 100;
		mob.getNavigation().moveTo(noiseSpot.x, noiseSpot.y, noiseSpot.z, 1.1);
	}

	@Override
	public boolean canContinueToUse() {
		if (hunted != null) {
			if (!hunted.isAlive() || hunted.isSpectator() || hunted.isCreative() || hunted.level() != mob.level()) return false;
			return huntLeft > 0 && mob.distanceToSqr(hunted) > 6.25;      // closer than 2.5 -> let the melee goal take over
		}
		return searchTicks > 0 && NoiseManager.get(mob.getUUID()) != null;
	}

	@Override
	public void tick() {
		if (hunted != null) {
			huntLeft--;
			if (--repath <= 0) {
				repath = 8;
				mob.getNavigation().moveTo(hunted, 1.15);
			}
			if (mob.distanceToSqr(hunted) < 256) mob.setTarget(hunted);
			return;
		}
		searchTicks--;
		boolean arrived = mob.getNavigation().isDone() || mob.position().distanceToSqr(noiseSpot) < 9.0;
		if (arrived || searchTicks % 20 == 0) {
			ServerLevel level = (ServerLevel) mob.level();
			ServerPlayer best = null; double bd = 24 * 24;
			for (ServerPlayer p : level.players()) {
				if (!p.isAlive() || p.isSpectator() || p.isCreative()) continue;
				double d = p.position().distanceToSqr(noiseSpot);
				if (d < bd) { bd = d; best = p; }
			}
			if (best != null) {                                    // someone is near the noise -> go for them
				hunted = best; huntLeft = TvTimeConfig.get().huntTicks; repath = 0;
				mob.setTarget(best);
				NoiseManager.clear(mob.getUUID());
			} else if (arrived) {
				NoiseManager.clear(mob.getUUID());                 // nobody there, give up
			}
		}
	}

	@Override
	public void stop() {
		hunted = null;
		if (mob.getTarget() == null) NoiseManager.clear(mob.getUUID());
		mob.getNavigation().stop();
	}
}
