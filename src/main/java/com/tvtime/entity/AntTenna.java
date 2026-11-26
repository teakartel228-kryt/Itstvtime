package com.tvtime.entity;

import com.tvtime.TvTimeConfig;
import com.tvtime.registry.ModEntities;
import com.tvtime.registry.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Mr. "Ant" Tenna - boss of the show. Balance is vanilla-scale: 300 HP (Wither = 300), armor 6, no regeneration.
 * Attacks are adapted from the "Cold Place" fight of Chapter 3 (see the wiki text): 
 *   Star Cast (rings of stars with a gap - jump/step through the gap), Hard Cut (a slice line across the arena after a telegraph),
 *   Rimshot (two laughing stars that home in and swell), Mini-games from 50% HP (falling TVs / Cooking Channel fireballs /
 *   Lightners Live beat pulses) and the Final Episode below 20% HP (faster cycle + the "Mikes" employees).
 * Phase music: 1 "IT'S TV TIME", 2 "THAT MAKES YOU SCREAM", 3 "It's Time to End You".
 */
public class AntTenna extends Monster {
	private enum Attack { NONE, STAR_CAST, HARD_CUT, RIMSHOT, TV_RAIN, COOKING, LIVE }

	private final ServerBossEvent bossBar = new ServerBossEvent(Component.translatable("entity.tvtime.ant_tenna"),
		BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);

	private int phase = 1;
	private Attack attack = Attack.NONE;
	private int attackTick, cooldown = 80;
	private int musicLeft;
	private boolean mikesSpawned;
	private final List<Ring> rings = new ArrayList<>();
	private final List<LaughStar> stars = new ArrayList<>();
	private final List<Vec3> tvTargets = new ArrayList<>();
	private Vec3 cutPoint, cutDir;

	private static final class Ring { double r; final double gap; final boolean full; final Set<UUID> hit = new HashSet<>();
		Ring(double gap, boolean full) { this.gap = gap; this.full = full; } }
	private static final class LaughStar { Vec3 pos; int life = 120; LaughStar(Vec3 p) { pos = p; } }

	public AntTenna(EntityType<? extends AntTenna> type, Level level) {
		super(type, level);
		this.xpReward = 200;
		this.setPersistenceRequired();
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 300.0)
			.add(Attributes.MOVEMENT_SPEED, 0.27)
			.add(Attributes.ATTACK_DAMAGE, 9.0)
			.add(Attributes.ARMOR, 6.0)
			.add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
			.add(Attributes.FOLLOW_RANGE, 48.0)
			.add(Attributes.SCALE, 1.6);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
		goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 32.0F));
		goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override public boolean shouldDespawnInPeaceful() { return false; }
	@Override public boolean removeWhenFarAway(double dist) { return false; }
	@Override public boolean canChangeDimensions(boolean portal) { return false; }

	private float difficultyScale(ServerLevel level) {
		return switch (level.getDifficulty()) { case PEACEFUL, EASY -> 0.7F; case NORMAL -> 1.0F; case HARD -> 1.35F; };
	}

	// ---------------------------------------------------------------- tick
	@Override
	public void tick() {
		super.tick();
		if (!(level() instanceof ServerLevel sl)) return;
		bossBar.setProgress(getHealth() / getMaxHealth());

		int newPhase = getHealth() <= getMaxHealth() * 0.2F ? 3 : getHealth() <= getMaxHealth() * 0.5F ? 2 : 1;
		if (newPhase != phase || tickCount == 1) { phase = newPhase; onPhase(sl); }
		if (TvTimeConfig.get().bossMusic && --musicLeft <= 0) playMusic(sl);

		tickRings(sl); tickStars(sl);

		LivingEntity target = getTarget();
		if (attack == Attack.NONE) {
			if (target != null && --cooldown <= 0) startAttack(sl, target);
		} else {
			runAttack(sl, target);
		}
	}

	private void onPhase(ServerLevel sl) {
		stopMusic(sl); musicLeft = 0;
		if (tickCount == 1) say(sl, "tvtime.boss.start");
		else if (phase == 2) say(sl, "tvtime.boss.phase2");
		else if (phase == 3) {
			say(sl, "tvtime.boss.phase3");
			sl.playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 3.0F, 1.4F);
			spawnMikes(sl);
		}
		bossBar.setColor(phase == 1 ? BossEvent.BossBarColor.RED : phase == 2 ? BossEvent.BossBarColor.YELLOW : BossEvent.BossBarColor.PURPLE);
	}

	private void say(ServerLevel sl, String key) {
		for (ServerPlayer p : sl.players()) if (p.distanceToSqr(this) < 64 * 64) p.displayClientMessage(Component.translatable(key), false);
	}

	private void spawnMikes(ServerLevel sl) {
		if (mikesSpawned) return; mikesSpawned = true;
		for (int i = 0; i < 3; i++) {   // the three "Mikes": normal / fast / slow employees with random weapons
			EntityType<? extends SmartZombie> t = i == 0 ? ModEntities.SMART_ZOMBIE : i == 1 ? ModEntities.FAST_SMART_ZOMBIE : ModEntities.SLOW_SMART_ZOMBIE;
			SmartZombie z = t.spawn(sl, blockPosition().offset(random.nextInt(7) - 3, 0, random.nextInt(7) - 3), EntitySpawnReason.MOB_SUMMONED);
			if (z != null && getTarget() != null) z.setTarget(getTarget());
		}
	}

	// ---------------------------------------------------------------- music (boss theme per phase, restarted when it ends)
	private SoundEvent phaseSound() { return phase == 1 ? ModSounds.BOSS_PHASE1 : phase == 2 ? ModSounds.BOSS_PHASE2 : ModSounds.BOSS_PHASE3; }
	private int phaseLength() { return phase == 1 ? 3310 : phase == 2 ? 2908 : 2736; }   // ticks, = length of the audio file
	private void playMusic(ServerLevel sl) {
		sl.playSound(null, getX(), getY(), getZ(), phaseSound(), SoundSource.MUSIC, 6.0F, 1.0F);
		musicLeft = phaseLength();
	}
	private void stopMusic(ServerLevel sl) {
		for (SoundEvent e : new SoundEvent[]{ModSounds.BOSS_PHASE1, ModSounds.BOSS_PHASE2, ModSounds.BOSS_PHASE3}) {
			ClientboundStopSoundPacket pkt = new ClientboundStopSoundPacket(e.location(), SoundSource.MUSIC);
			for (ServerPlayer p : sl.players()) p.connection.send(pkt);
		}
	}

	// ---------------------------------------------------------------- attack chooser
	private void startAttack(ServerLevel sl, LivingEntity target) {
		Attack[] pool = phase == 1 ? new Attack[]{Attack.STAR_CAST, Attack.HARD_CUT, Attack.RIMSHOT}
			: new Attack[]{Attack.STAR_CAST, Attack.HARD_CUT, Attack.RIMSHOT, Attack.TV_RAIN, Attack.COOKING, Attack.LIVE};
		attack = pool[random.nextInt(pool.length)];
		attackTick = 0;
		switch (attack) {
			case HARD_CUT -> {
				Vec3 to = target.position().subtract(position()).multiply(1, 0, 1);
				Vec3 fwd = to.lengthSqr() < 1e-4 ? new Vec3(1, 0, 0) : to.normalize();
				cutDir = new Vec3(-fwd.z, 0, fwd.x);                       // perpendicular -> the line "cuts the screen"
				cutPoint = target.position();
				setDeltaMovement(getDeltaMovement().add(0, 0.7, 0));       // Tenna hops before he claps
			}
			case TV_RAIN -> {
				tvTargets.clear();
				for (Player p : sl.players()) if (p.distanceToSqr(this) < 48 * 48 && !p.isSpectator())
					for (int i = 0; i < 6; i++) tvTargets.add(p.position().add((random.nextDouble() - .5) * 12, 0, (random.nextDouble() - .5) * 12));
				tvTargets.add(target.position());
			}
			default -> {}
		}
	}

	private int cooldownFor() { return phase == 1 ? 70 : phase == 2 ? 50 : 28; }

	private void runAttack(ServerLevel sl, LivingEntity target) {
		attackTick++;
		float dmg = difficultyScale(sl);
		switch (attack) {
			case STAR_CAST -> {                      // two rings, 20 ticks apart, each with a gap
				if (attackTick == 1 || attackTick == 21) rings.add(new Ring(random.nextDouble() * Math.PI * 2, false));
				if (attackTick == 1) sl.playSound(null, blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 3.0F, 1.2F);
				if (attackTick > 30) endAttack();
			}
			case HARD_CUT -> {
				if (attackTick < 28) {               // telegraph
					if (attackTick % 2 == 0) line(sl, ParticleTypes.CRIT, 0.6);
				} else if (attackTick == 28) {       // snap
					line(sl, ParticleTypes.SWEEP_ATTACK, 1.0);
					sl.playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 3.0F, 0.6F);
					sl.playSound(null, BlockPos_of(cutPoint), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.5F, 1.5F);
					for (ServerPlayer p : sl.players()) {
						if (p.isSpectator() || p.isCreative()) continue;
						Vec3 rel = p.position().subtract(cutPoint).multiply(1, 0, 1);
						double along = rel.dot(cutDir), across = Math.sqrt(Math.max(0, rel.lengthSqr() - along * along));
						if (across < 1.6 && Math.abs(along) < 22 && p.getY() - getY() < 3.5) {
							p.hurtServer(sl, damageSources().mobAttack(this), 9.0F * dmg);
							Vec3 side = rel.dot(cutDir.cross(new Vec3(0, 1, 0))) >= 0 ? cutDir.cross(new Vec3(0, 1, 0)) : cutDir.cross(new Vec3(0, 1, 0)).scale(-1);
							p.push(side.x * 0.9, 0.5, side.z * 0.9); p.hurtMarked = true;
						}
					}
				} else if (attackTick > 36) endAttack();
			}
			case RIMSHOT -> {                        // two homing laughing stars
				if (attackTick == 1) {
					stars.add(new LaughStar(position().add(2, 2.5, 0)));
					stars.add(new LaughStar(position().add(-2, 2.5, 0)));
					sl.playSound(null, blockPosition(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.HOSTILE, 3.0F, 1.7F);
				}
				if (attackTick > 20) endAttack();
			}
			case TV_RAIN -> {                        // "umbrella" mini-game: mark spots, then TVs fall
				if (attackTick < 40) {
					for (Vec3 v : tvTargets) {
						double a = random.nextDouble() * Math.PI * 2;
						sl.sendParticles(ParticleTypes.END_ROD, v.x + Math.cos(a) * 1.8, v.y + 0.1, v.z + Math.sin(a) * 1.8, 1, 0, 0, 0, 0);
					}
				} else if (attackTick == 40) {
					for (Vec3 v : tvTargets) {
						sl.sendParticles(ParticleTypes.EXPLOSION, v.x, v.y + 0.5, v.z, 1, 0, 0, 0, 0);
						for (ServerPlayer p : sl.players())
							if (!p.isSpectator() && !p.isCreative() && p.position().distanceToSqr(v) < 4.0) p.hurtServer(sl, damageSources().mobAttack(this), 7.0F * dmg);
					}
					sl.playSound(null, blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 4.0F, 0.5F);
				} else if (attackTick > 48) endAttack();
			}
			case COOKING -> {                        // "Cooking Channel": a fan of rolling fireballs
				if (attackTick == 1 && target != null) {
					Vec3 base = target.position().add(0, 0.5, 0).subtract(position().add(0, 1.5, 0)).normalize();
					for (int i = -2; i <= 2; i++) {
						Vec3 d = base.yRot((float) Math.toRadians(i * 14)).scale(0.8);
						SmallFireball fb = new SmallFireball(sl, this, d);
						fb.setPos(getX() + d.x, getY() + 1.6 * getScale(), getZ() + d.z);
						sl.addFreshEntity(fb);
					}
					playSound(SoundEvents.BLAZE_SHOOT, 3.0F, 0.8F);
				}
				if (attackTick > 20) endAttack();
			}
			case LIVE -> {                           // "Lightners Live": 4 beats, a pulse on each - jump over it
				if (attackTick % 12 == 1 && attackTick < 48) {
					rings.add(new Ring(0, true));
					sl.playSound(null, blockPosition(), SoundEvents.NOTE_BLOCK_BASEDRUM.value(), SoundSource.HOSTILE, 3.0F, 1.0F);
					sl.sendParticles(ParticleTypes.NOTE, getX(), getY() + 4, getZ(), 6, 1.5, 0.5, 1.5, 1);
				}
				if (attackTick > 55) endAttack();
			}
			default -> endAttack();
		}
	}

	private static net.minecraft.core.BlockPos BlockPos_of(Vec3 v) { return net.minecraft.core.BlockPos.containing(v); }

	private void endAttack() { attack = Attack.NONE; cooldown = cooldownFor(); }

	private void line(ServerLevel sl, net.minecraft.core.particles.SimpleParticleType type, double step) {
		for (double s = -22; s <= 22; s += step * 2)
			sl.sendParticles(type, cutPoint.x + cutDir.x * s, cutPoint.y + 0.3, cutPoint.z + cutDir.z * s, 1, 0, 0.1, 0, 0);
	}

	// ---------------------------------------------------------------- expanding rings (stars / beat pulses)
	private void tickRings(ServerLevel sl) {
		float dmg = difficultyScale(sl);
		for (Iterator<Ring> it = rings.iterator(); it.hasNext(); ) {
			Ring ring = it.next();
			ring.r += 0.5;
			if (ring.r > 30) { it.remove(); continue; }
			int n = Math.max(16, (int) (ring.r * 6));
			for (int i = 0; i < n; i++) {
				double a = i * Math.PI * 2 / n;
				if (!ring.full && angleDiff(a, ring.gap) < 0.45) continue;          // the gap
				sl.sendParticles(ring.full ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.END_ROD,
					getX() + Math.cos(a) * ring.r, getY() + 0.6, getZ() + Math.sin(a) * ring.r, 1, 0, 0, 0, 0);
			}
			for (ServerPlayer p : sl.players()) {
				if (p.isSpectator() || p.isCreative() || ring.hit.contains(p.getUUID())) continue;
				double dx = p.getX() - getX(), dz = p.getZ() - getZ(), dist = Math.sqrt(dx * dx + dz * dz);
				if (Math.abs(dist - ring.r) > 0.9) continue;
				double rel = p.getY() - getY();
				if (ring.full ? rel < 0.9 : rel < 1.7 && rel > -1.5) {              // pulses are dodged by jumping, stars hit body height
					if (!ring.full && angleDiff(Math.atan2(dz, dx), ring.gap) < 0.45) continue;
					ring.hit.add(p.getUUID());
					p.hurtServer(sl, damageSources().mobAttack(this), (ring.full ? 5.0F : 6.0F) * dmg);
				}
			}
		}
	}

	private static double angleDiff(double a, double b) {
		double d = Math.abs(((a - b) % (Math.PI * 2) + Math.PI * 3) % (Math.PI * 2) - Math.PI);
		return Math.PI - d;                       // 0 when equal
	}

	// ---------------------------------------------------------------- homing laughing stars
	private void tickStars(ServerLevel sl) {
		float dmg = difficultyScale(sl);
		for (Iterator<LaughStar> it = stars.iterator(); it.hasNext(); ) {
			LaughStar s = it.next();
			s.life--;
			Player near = sl.getNearestPlayer(s.pos.x, s.pos.y, s.pos.z, 40, p -> !p.isSpectator() && !p.isCreative());
			if (near != null) {
				Vec3 dir = near.position().add(0, 1, 0).subtract(s.pos).normalize();
				s.pos = s.pos.add(dir.scale(0.32));
			}
			double size = 0.8 + (120 - s.life) / 120.0 * 0.9;                       // grows over time
			sl.sendParticles(ParticleTypes.FIREWORK, s.pos.x, s.pos.y, s.pos.z, 4, size * .3, size * .3, size * .3, 0.01);
			if (s.life % 10 == 0) sl.sendParticles(ParticleTypes.NOTE, s.pos.x, s.pos.y + 0.8, s.pos.z, 1, 0, 0, 0, 1);
			boolean gone = s.life <= 0;
			if (near != null && near.position().add(0, 1, 0).distanceTo(s.pos) < size + 0.4) {
				near.hurtServer(sl, damageSources().mobAttack(this), 5.0F * dmg);
				sl.sendParticles(ParticleTypes.EXPLOSION, s.pos.x, s.pos.y, s.pos.z, 1, 0, 0, 0, 0);
				gone = true;
			}
			if (gone) it.remove();
		}
	}

	// ---------------------------------------------------------------- boss bar, death, misc
	@Override public void startSeenByPlayer(ServerPlayer p) { super.startSeenByPlayer(p); bossBar.addPlayer(p); }
	@Override public void stopSeenByPlayer(ServerPlayer p) { super.stopSeenByPlayer(p); bossBar.removePlayer(p); }

	@Override
	public void die(DamageSource source) {
		if (level() instanceof ServerLevel sl) { stopMusic(sl); say(sl, "tvtime.boss.death"); bossBar.removeAllPlayers(); }
		super.die(source);
	}

	@Override
	public void remove(RemovalReason reason) {
		if (level() instanceof ServerLevel sl && reason != RemovalReason.KILLED) { stopMusic(sl); bossBar.removeAllPlayers(); }
		super.remove(reason);
	}

	@Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.IRON_GOLEM_HURT; }
	@Override protected SoundEvent getDeathSound() { return SoundEvents.IRON_GOLEM_DEATH; }
}
