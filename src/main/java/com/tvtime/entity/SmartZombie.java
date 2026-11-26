package com.tvtime.entity;

import com.tvtime.TvTimeConfig;
import com.tvtime.registry.ModEntities;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Zombie that spawns from the three spawn eggs (normal / fast / slow). Normal / fast / slow differ in BLOCK-BREAKING speed (see breakSpeedMultiplier).
 * Block breaking, ranged weapons and noise hunting are added in ZombieMixin (shared with vanilla zombies).
 */
public class SmartZombie extends Zombie {

	private static final Item[] WEAPONS = {
		Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.DIAMOND_SWORD,
		Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.DIAMOND_AXE,
		Items.IRON_SHOVEL, Items.IRON_PICKAXE, Items.BOW, Items.BOW, Items.CROSSBOW, Items.TRIDENT, Items.MACE
	};

	public SmartZombie(EntityType<? extends Zombie> type, Level level) {
		super(type, level);
		setCanBreakDoors(true);
		setCanPickUpLoot(true); // picks up better weapons from the ground
	}

	/** Block-breaking speed multiplier, depends on which spawn egg / entity type this is (movement speed is vanilla for all). */
	public double breakSpeedMultiplier() {
		TvTimeConfig c = TvTimeConfig.get();
		EntityType<?> t = getType();
		return t == ModEntities.FAST_SMART_ZOMBIE ? c.eggBreakSpeedFast : t == ModEntities.SLOW_SMART_ZOMBIE ? c.eggBreakSpeedSlow : c.eggBreakSpeedNormal;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Zombie.createAttributes()
			.add(Attributes.FOLLOW_RANGE, 40.0)
			.add(Attributes.STEP_HEIGHT, 1.0);
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
		super.populateDefaultEquipmentSlots(random, difficulty);
		if (getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && random.nextFloat() < 0.75F) {
			setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(WEAPONS[random.nextInt(WEAPONS.length)]));
		}
	}
}
