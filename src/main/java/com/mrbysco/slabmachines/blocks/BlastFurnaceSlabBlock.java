package com.mrbysco.slabmachines.blocks;

import com.mrbysco.slabmachines.blockentity.furnace.BlastFurnaceSlabBlockEntity;
import com.mrbysco.slabmachines.blocks.base.CustomSlabBlock;
import com.mrbysco.slabmachines.blocks.base.enums.CustomSlabType;
import com.mrbysco.slabmachines.init.SlabRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class BlastFurnaceSlabBlock extends AbstractFurnaceSlabBlock {

	public BlastFurnaceSlabBlock(Properties properties) {
		super(properties.strength(2.0F, 10.0F).sound(SoundType.STONE));
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult result) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			if (level.getBlockEntity(pos) instanceof BlastFurnaceSlabBlockEntity blastFurnaceBlockEntity) {
				player.openMenu(blastFurnaceBlockEntity);
				player.awardStat(Stats.INTERACT_WITH_BLAST_FURNACE);
			}
			return InteractionResult.CONSUME;
		}
	}

	@Override
	public void animateTick(BlockState stateIn, Level level, BlockPos pos, RandomSource rand) {
		if (stateIn.getValue(LIT)) {
			double posX = (double) pos.getX() + 0.5D;
			double posY = (double) pos.getY() + ((stateIn.getValue(CustomSlabBlock.TYPE) == CustomSlabType.TOP) ? 0.5D : 0);
			double posZ = (double) pos.getZ() + 0.5D;
			if (rand.nextDouble() < 0.1D) {
				level.playLocalSound(posX, posY, posZ, SoundEvents.BLASTFURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
			}

			Direction direction = stateIn.getValue(FACING);
			Direction.Axis direction$axis = direction.getAxis();
			double d3 = 0.52D;
			double d4 = rand.nextDouble() * 0.6D - 0.3D;
			double d5 = direction$axis == Direction.Axis.X ? (double) direction.getStepX() * d3 : d4;
			double d6 = rand.nextDouble() * 6.0D / 16.0D;
			double d7 = direction$axis == Direction.Axis.Z ? (double) direction.getStepZ() * d3 : d4;
			level.addParticle(ParticleTypes.SMOKE, posX + d5, posY + d6, posZ + d7, 0.0D, 0.0D, 0.0D);
			level.addParticle(ParticleTypes.FLAME, posX + d5, posY + d6, posZ + d7, 0.0D, 0.0D, 0.0D);
		}
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return createFurnaceTicker(level, blockEntityType, SlabRegistry.BLAST_FURNACE_SLAB_BE.get());
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BlastFurnaceSlabBlockEntity(pos, state);
	}
}
