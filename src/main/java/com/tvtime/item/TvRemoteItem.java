package com.tvtime.item;

import com.tvtime.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/** Right-click a block to start the show: summons Mr. Ant-Tenna (survival way to fight the boss). */
public class TvRemoteItem extends Item {
	public TvRemoteItem(Properties props) { super(props); }

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		if (!(ctx.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
		BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
		if (ModEntities.ANT_TENNA.spawn(level, pos, EntitySpawnReason.MOB_SUMMONED) == null) return InteractionResult.FAIL;
		level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.0F, 0.5F);
		if (ctx.getPlayer() == null || !ctx.getPlayer().isCreative()) ctx.getItemInHand().shrink(1);
		return InteractionResult.CONSUME;
	}
}
