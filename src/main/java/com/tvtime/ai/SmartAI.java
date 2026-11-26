package com.tvtime.ai;

import com.tvtime.TvTimeConfig;
import com.tvtime.entity.SmartZombie;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.gamerules.GameRules;

public final class SmartAI {
	private SmartAI() {}

	/** Which zombies get the extended AI. Drowned / zombie villagers / piglins are left alone. */
	public static boolean isSmart(Zombie z) {
		if (z instanceof SmartZombie) return true;
		EntityType<?> t = z.getType();
		return TvTimeConfig.get().vanillaZombiesAreSmart && (t == EntityTypes.ZOMBIE || t == EntityTypes.HUSK);
	}

	public static boolean mobGriefing(ServerLevel level) {
		return level.getGameRules().get(GameRules.MOB_GRIEFING);
	}
}
