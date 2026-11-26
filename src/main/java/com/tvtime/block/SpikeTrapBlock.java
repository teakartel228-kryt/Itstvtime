package com.tvtime.block;

import com.tvtime.entity.AntTenna;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Pressure spike trap: stepping on it fires the spikes for 2 s, they hurt everything standing on them. */
public class SpikeTrapBlock extends Block {
	public static final BooleanProperty EXTENDED = BooleanProperty.create("extended");
	private static final VoxelShape RETRACTED = Block.box(0, 0, 0, 16, 3, 16);
	private static final VoxelShape OUT = Block.box(0, 0, 0, 16, 12, 16);

	public SpikeTrapBlock(Properties props) {
		super(props);
		registerDefaultState(stateDefinition.any().setValue(EXTENDED, false));
	}

	@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(EXTENDED); }

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return state.getValue(EXTENDED) ? OUT : RETRACTED;
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier, boolean precise) {
		if (level.isClientSide() || !(level instanceof ServerLevel sl)) return;
		if (!(entity instanceof LivingEntity living) || entity instanceof AntTenna) return;
		if (!state.getValue(EXTENDED)) {
			level.setBlock(pos, state.setValue(EXTENDED, true), Block.UPDATE_ALL);
			level.scheduleTick(pos, this, 40);
			level.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 1.0F, 1.6F);
		}
		living.hurtServer(sl, sl.damageSources().cactus(), 3.0F); // vanilla i-frames limit this to ~1 hit / 0.5 s
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (state.getValue(EXTENDED)) {
			level.setBlock(pos, state.setValue(EXTENDED, false), Block.UPDATE_ALL);
			level.playSound(null, pos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 1.0F, 1.6F);
		}
	}
}
