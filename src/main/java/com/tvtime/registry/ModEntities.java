package com.tvtime.registry;

import com.tvtime.TvTimeMod;
import com.tvtime.entity.AntTenna;
import com.tvtime.entity.SmartZombie;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
	public static final EntityType<SmartZombie> SMART_ZOMBIE = register("smart_zombie",
		EntityType.Builder.of(SmartZombie::new, MobCategory.MONSTER).sized(0.6F, 1.95F).eyeHeight(1.74F).clientTrackingRange(8));
	public static final EntityType<SmartZombie> FAST_SMART_ZOMBIE = register("fast_smart_zombie",
		EntityType.Builder.of(SmartZombie::new, MobCategory.MONSTER).sized(0.6F, 1.95F).eyeHeight(1.74F).clientTrackingRange(8));
	public static final EntityType<SmartZombie> SLOW_SMART_ZOMBIE = register("slow_smart_zombie",
		EntityType.Builder.of(SmartZombie::new, MobCategory.MONSTER).sized(0.6F, 1.95F).eyeHeight(1.74F).clientTrackingRange(8));
	/** Hitbox 0.6 x 1.95 is multiplied by the SCALE attribute (1.6) -> about 1 x 3.1 blocks. */
	public static final EntityType<AntTenna> ANT_TENNA = register("ant_tenna",
		EntityType.Builder.of(AntTenna::new, MobCategory.MONSTER).sized(0.6F, 1.95F).eyeHeight(1.74F).clientTrackingRange(12).fireImmune());

	private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, TvTimeMod.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(SMART_ZOMBIE, SmartZombie.createAttributes());
		FabricDefaultAttributeRegistry.register(FAST_SMART_ZOMBIE, SmartZombie.createAttributes());
		FabricDefaultAttributeRegistry.register(SLOW_SMART_ZOMBIE, SmartZombie.createAttributes());
		FabricDefaultAttributeRegistry.register(ANT_TENNA, AntTenna.createAttributes());
	}
}
