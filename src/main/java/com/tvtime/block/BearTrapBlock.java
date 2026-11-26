package com.tvtime.block;

import com.tvtime.entity.AntTenna;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Custom-model bear trap. Armed (open jaws) -> snaps shut on any living entity; right-click to re-arm. */
public class BearTrapBlock extends Block {
	public static final BooleanProperty ARMED = BooleanProperty.create("armed");
	private static final VoxelShape OPEN = Block.box(1, 0, 1, 15, 8, 15);
	private static final VoxelShape CLOSED = Block.box(1, 0, 1, 15, 5, 15);

	public BearTrapBlock(Properties props) {
		super(props);
		registerDefaultState(stateDefinition.any().setValue(ARMED, true));
	}

	@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(ARMED); }

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return state.getValue(ARMED) ? OPEN : CLOSED;
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier, boolean precise) {
		if (level.isClientSide() || !state.getValue(ARMED)) return;
		if (!(entity instanceof LivingEntity living) || entity instanceof AntTenna || !(level instanceof ServerLevel sl)) return;
		level.setBlock(pos, state.setValue(ARMED, false), Block.UPDATE_ALL);
		living.hurtServer(sl, sl.damageSources().generic(), 5.0F);
		living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 6));
		living.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 100, 1));
		level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.5F, 0.6F);
		level.playSound(null, pos, SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 1.0F, 0.8F);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (state.getValue(ARMED)) return InteractionResult.PASS;
		if (!level.isClientSide()) {
			level.setBlock(pos, state.setValue(ARMED, true), Block.UPDATE_ALL);
			level.playSound(null, pos, SoundEvents.TRIPWIRE_CLICK_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
			player.displayClientMessage(Component.translatable("tvtime.trap.rearmed"), true);
		}
		return InteractionResult.SUCCESS;
	}
}
