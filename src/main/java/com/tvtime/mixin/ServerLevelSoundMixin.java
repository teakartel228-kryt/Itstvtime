package com.tvtime.mixin;

import com.tvtime.ai.NoiseManager;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Every sound the server plays at a position passes through playSeededSound. Two descriptors are listed because Mojang changed
 * the first parameter between versions (Player -> Entity); require=0 means a non-matching one is just skipped.
 * If neither matches in your exact version, zombie noise hunting is silently off (the log line "noise hook is active" will not appear).
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelSoundMixin {
	@Inject(method = "playSeededSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V",
		at = @At("HEAD"), require = 0)
	private void tvtime$soundEntity(Entity except, double x, double y, double z, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch, long seed, CallbackInfo ci) {
		NoiseManager.onSound((ServerLevel) (Object) this, x, y, z, source, volume);
	}

	@Inject(method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V",
		at = @At("HEAD"), require = 0)
	private void tvtime$soundPlayer(Player except, double x, double y, double z, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch, long seed, CallbackInfo ci) {
		NoiseManager.onSound((ServerLevel) (Object) this, x, y, z, source, volume);
	}
}
