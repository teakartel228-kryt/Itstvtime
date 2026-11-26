package com.tvtime.ai;

import com.tvtime.TvTimeConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.EnumSet;

/** Bow, crossbow and trident users: keep 5-18 blocks away and shoot. Everything else is handled by the vanilla melee goal
 *  (swords/axes/mace/spear/pickaxes already apply their attack-damage attributes to the zombie). */
public class ZombieRangedWeaponGoal extends Goal {
	private final Zombie mob;
	private int cooldown = 20;
	private int strafeDir = 1, strafeTime;

	public ZombieRangedWeaponGoal(Zombie mob) {
		this.mob = mob;
		setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	private static boolean isRanged(ItemStack s) { return s.is(Items.BOW) || s.is(Items.CROSSBOW) || s.is(Items.TRIDENT); }

	@Override
	public boolean canUse() {
		if (!TvTimeConfig.get().zombiesUseRangedWeapons) return false;
		LivingEntity t = mob.getTarget();
		if (t == null || !t.isAlive() || !isRanged(mob.getMainHandItem())) return false;
		double d = mob.distanceToSqr(t);
		return d > 16 && d < 18 * 18 && mob.hasLineOfSight(t);
	}

	@Override public boolean canContinueToUse() { return canUse() || (mob.getTarget() != null && mob.distanceToSqr(mob.getTarget()) > 9 && isRanged(mob.getMainHandItem()) && mob.hasLineOfSight(mob.getTarget()) && mob.distanceToSqr(mob.getTarget()) < 22 * 22); }

	@Override public void start() { mob.setAggressive(true); }
	@Override public void stop() { mob.setAggressive(false); }

	@Override
	public void tick() {
		LivingEntity t = mob.getTarget();
		if (t == null) return;
		mob.getLookControl().setLookAt(t, 30.0F, 30.0F);
		double d = mob.distanceTo(t);
		mob.getNavigation().stop();
		if (--strafeTime <= 0) { strafeTime = 20 + mob.getRandom().nextInt(30); strafeDir = mob.getRandom().nextBoolean() ? 1 : -1; }
		mob.getMoveControl().strafe(d < 7 ? -0.6F : 0.0F, 0.5F * strafeDir);
		if (--cooldown > 0) return;

		ItemStack weapon = mob.getMainHandItem();
		ServerLevel level = (ServerLevel) mob.level();
		double dx = t.getX() - mob.getX(), dz = t.getZ() - mob.getZ();
		double h = Math.sqrt(dx * dx + dz * dz);
		float inaccuracy = 14 - level.getDifficulty().getId() * 4;
		if (weapon.is(Items.TRIDENT)) {
			ThrownTrident trident = new ThrownTrident(level, mob, weapon.copy());
			trident.shoot(dx, t.getY(0.3333) - trident.getY() + h * 0.2, dz, 1.6F, inaccuracy);
			trident.pickup = AbstractArrow.Pickup.DISALLOWED;
			level.addFreshEntity(trident);
			mob.playSound(SoundEvents.TRIDENT_THROW.value(), 1.0F, 1.0F);
			cooldown = 70;
		} else {
			boolean crossbow = weapon.is(Items.CROSSBOW);
			Arrow arrow = new Arrow(level, mob, new ItemStack(Items.ARROW), weapon.copy());
			arrow.shoot(dx, t.getY(0.3333) - arrow.getY() + h * 0.2, dz, crossbow ? 3.15F : 1.6F, inaccuracy);
			arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
			level.addFreshEntity(arrow);
			mob.playSound(crossbow ? SoundEvents.CROSSBOW_SHOOT : SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (mob.getRandom().nextFloat() * 0.4F + 0.8F));
			cooldown = crossbow ? 55 : 40;
		}
	}
}
