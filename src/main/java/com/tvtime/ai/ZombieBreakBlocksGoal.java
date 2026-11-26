package com.tvtime.ai;

import com.tvtime.TvTimeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Breaks blocks that stand between the zombie and its target, with the same formula the game uses for players
 * times a multiplier: vanilla zombies 0.8; eggs: normal 1.0 (= player), fast 2.0, slow 0.4 (config/tvtime.json).
 * Result with bare hands (zombie speed 0.8): dirt ~19 ticks, planks ~75 ticks - dirt breaks about 4x faster than wood, as for a player.
 * Digs forward (feet / head level) and straight down when the target is below.
 */
public class ZombieBreakBlocksGoal extends Goal {
	private final Zombie mob;
	private BlockPos pos;
	private int progress, required, lastStage, cooldown;
	private Vec3 lastPos = Vec3.ZERO;
	private int stuckTicks;

	public ZombieBreakBlocksGoal(Zombie mob) {
		this.mob = mob;
		setFlags(EnumSet.of(Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (!TvTimeConfig.get().zombiesBreakBlocks || !(mob.level() instanceof ServerLevel level) || !SmartAI.mobGriefing(level)) return false;
		LivingEntity target = mob.getTarget();
		if (target == null || !target.isAlive()) return false;
		if (cooldown > 0) { cooldown--; return false; }

		// "stuck" detection: barely moved for a second while trying to reach someone
		if (mob.position().distanceToSqr(lastPos) < 0.01) stuckTicks++; else stuckTicks = 0;
		lastPos = mob.position();
		boolean blocked = mob.horizontalCollision || stuckTicks > 20 || mob.getNavigation().getPath() == null;
		if (!blocked || mob.distanceToSqr(target) < 2.5) return false;

		pos = findBlocker(level, target);
		return pos != null;
	}

	private BlockPos findBlocker(ServerLevel level, LivingEntity target) {
		Vec3 d = target.position().subtract(mob.position());
		BlockPos feet = mob.blockPosition();
		if (d.y < -1.5 && Math.abs(d.x) < 1.5 && Math.abs(d.z) < 1.5) {
			BlockPos below = feet.below();
			return breakable(level, below) ? below : null;
		}
		Direction dir = Direction.getApproximateNearest(d.x, 0, d.z);
		BlockPos front = feet.relative(dir);
		BlockPos up = feet.above(2);
		if (breakable(level, front)) return front;
		if (breakable(level, front.above())) return front.above();
		return d.y > 1.0 && breakable(level, up) ? up : null;      // dig upward only when the target is above
	}

	private boolean breakable(ServerLevel level, BlockPos p) {
		BlockState s = level.getBlockState(p);
		if (s.isAir() || !s.getFluidState().isEmpty() || s.getCollisionShape(level, p).isEmpty()) return false;
		float hardness = s.getDestroySpeed(level, p);
		return hardness >= 0 && hardness <= TvTimeConfig.get().zombieMaxBlockHardness;
	}

	private double multiplier() {
		return mob instanceof com.tvtime.entity.SmartZombie sz ? sz.breakSpeedMultiplier() : TvTimeConfig.get().zombieBreakSpeedMultiplier;
	}

	private int ticksToBreak(ServerLevel level, BlockPos p) {
		BlockState s = level.getBlockState(p);
		ItemStack tool = mob.getMainHandItem();
		float speed = tool.getDestroySpeed(s);
		boolean right = !s.requiresCorrectToolForDrops() || tool.isCorrectToolForDrops(s);
		float perTick = speed / s.getDestroySpeed(level, p) / (right ? 30.0F : 100.0F) * (float) multiplier();
		return Math.max(1, (int) Math.ceil(1.0F / perTick));
	}

	@Override
	public void start() {
		ServerLevel level = (ServerLevel) mob.level();
		progress = 0; lastStage = -1;
		required = ticksToBreak(level, pos);
		mob.getNavigation().stop();
	}

	@Override
	public boolean canContinueToUse() {
		if (pos == null || mob.getTarget() == null || !mob.getTarget().isAlive()) return false;
		return mob.distanceToSqr(Vec3.atCenterOf(pos)) <= 9.0 && !mob.level().getBlockState(pos).isAir();
	}

	@Override
	public void tick() {
		ServerLevel level = (ServerLevel) mob.level();
		mob.getLookControl().setLookAt(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
		progress++;
		if (progress % 8 == 0) {
			mob.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT);
			BlockState s = level.getBlockState(pos);
			level.playSound(null, pos, s.getSoundType().getHitSound(), mob.getSoundSource(), 0.6F, 0.8F);
		}
		int stage = (int) ((float) progress / required * 10.0F);
		if (stage != lastStage) { lastStage = stage; level.destroyBlockProgress(mob.getId(), pos, stage); }
		if (progress >= required) {
			level.destroyBlock(pos, true, mob);
			level.destroyBlockProgress(mob.getId(), pos, -1);
			pos = null;
			stuckTicks = 0;
		}
	}

	@Override
	public void stop() {
		if (pos != null && mob.level() instanceof ServerLevel level) level.destroyBlockProgress(mob.getId(), pos, -1);
		pos = null;
		cooldown = 5;
	}
}
