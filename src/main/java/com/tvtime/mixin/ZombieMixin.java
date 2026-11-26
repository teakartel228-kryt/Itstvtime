package com.tvtime.mixin;

import com.tvtime.ai.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Zombie.class)
public abstract class ZombieMixin extends Monster {
	protected ZombieMixin(EntityType<? extends Monster> type, Level level) { super(type, level); }

	@Inject(method = "registerGoals", at = @At("TAIL"))
	private void tvtime$smartGoals(CallbackInfo ci) {
		Zombie self = (Zombie) (Object) this;
		if (!SmartAI.isSmart(self)) return;
		this.goalSelector.addGoal(1, new ZombieRangedWeaponGoal(self));
		this.goalSelector.addGoal(1, new ZombieNoiseGoal(self));
		this.goalSelector.addGoal(3, new ZombieBreakBlocksGoal(self));
	}
}
