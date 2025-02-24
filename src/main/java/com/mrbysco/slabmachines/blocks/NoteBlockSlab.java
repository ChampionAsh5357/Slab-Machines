package com.mrbysco.slabmachines.blocks;

import com.mrbysco.slabmachines.blocks.base.CustomSlabBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.CommonHooks;

import javax.annotation.Nullable;

public class NoteBlockSlab extends CustomSlabBlock {
	public static final EnumProperty<NoteBlockInstrument> INSTRUMENT = BlockStateProperties.NOTEBLOCK_INSTRUMENT;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final IntegerProperty NOTE = BlockStateProperties.NOTE;

	public NoteBlockSlab(Properties properties) {
		super(properties.strength(2.5F).sound(SoundType.WOOD));
		this.registerDefaultState(this.defaultBlockState().setValue(INSTRUMENT, NoteBlockInstrument.HARP).setValue(NOTE, Integer.valueOf(0)).setValue(POWERED, Boolean.valueOf(false)));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.setInstrument(context.getLevel(), context.getClickedPos(), super.getStateForPlacement(context));
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState state1, LevelAccessor level, BlockPos pos, BlockPos pos1) {
		boolean flag = direction.getAxis() == Direction.Axis.Y;
		BlockState noteState = flag ? this.setInstrument(level, pos, state) : super.updateShape(state, direction, state1, level, pos, pos1);
		return direction == Direction.DOWN ? noteState : super.updateShape(state, direction, state1, level, pos, pos1);
	}

	private BlockState setInstrument(LevelAccessor accessor, BlockPos pos, BlockState state) {
		NoteBlockInstrument noteblockinstrument = accessor.getBlockState(pos.above()).instrument();
		if (noteblockinstrument.worksAboveNoteBlock()) {
			return state.setValue(INSTRUMENT, noteblockinstrument);
		} else {
			NoteBlockInstrument noteblockinstrument1 = accessor.getBlockState(pos.below()).instrument();
			NoteBlockInstrument noteblockinstrument2 = noteblockinstrument1.worksAboveNoteBlock() ? NoteBlockInstrument.HARP : noteblockinstrument1;
			return state.setValue(INSTRUMENT, noteblockinstrument2);
		}
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
		ItemStack itemstack = player.getItemInHand(hand);
		if (itemstack.is(ItemTags.NOTE_BLOCK_TOP_INSTRUMENTS) && result.getDirection() == Direction.UP) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		} else if (level.isClientSide) {
			return ItemInteractionResult.SUCCESS;
		} else {
			int _new = CommonHooks.onNoteChange(level, pos, state, state.getValue(NOTE), state.cycle(NOTE).getValue(NOTE));
			if (_new == -1) return ItemInteractionResult.FAIL;
			state = state.setValue(NOTE, _new);
			level.setBlock(pos, state, 3);
			this.playNote(player, state, level, pos);
			player.awardStat(Stats.TUNE_NOTEBLOCK);
			return ItemInteractionResult.CONSUME;
		}
	}

	@Override
	public void attack(BlockState state, Level level, BlockPos pos, Player player) {
		if (!level.isClientSide) {
			this.playNote(player, state, level, pos);
			player.awardStat(Stats.PLAY_NOTEBLOCK);
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		boolean flag = level.hasNeighborSignal(pos);
		if (flag != state.getValue(POWERED)) {
			if (flag) {
				this.playNote((Entity) null, state, level, pos);
			}

			level.setBlock(pos, state.setValue(POWERED, Boolean.valueOf(flag)), 3);
		}
	}

	private void playNote(@Nullable Entity entity, BlockState state, Level level, BlockPos pos) {
		if (state.getValue(INSTRUMENT).worksAboveNoteBlock() || level.getBlockState(pos.above()).isAir()) {
			level.blockEvent(pos, this, 0, 0);
			level.gameEvent(entity, GameEvent.NOTE_BLOCK_PLAY, pos);
		}
	}

	@Override
	public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		net.neoforged.neoforge.event.level.NoteBlockEvent.Play e = new net.neoforged.neoforge.event.level.NoteBlockEvent.Play(level, pos, state, state.getValue(NOTE), state.getValue(INSTRUMENT));
		if (net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(e).isCanceled()) return false;
		state = state.setValue(NOTE, e.getVanillaNoteId()).setValue(INSTRUMENT, e.getInstrument());
		NoteBlockInstrument noteblockinstrument = state.getValue(INSTRUMENT);
		float f;
		if (noteblockinstrument.isTunable()) {
			int i = state.getValue(NOTE);
			f = (float) Math.pow(2.0D, (double) (i - 12) / 12.0D);
			level.addParticle(ParticleTypes.NOTE, (double) pos.getX() + 0.5D, (double) pos.getY() + 1.2D, (double) pos.getZ() + 0.5D, (double) i / 24.0D, 0.0D, 0.0D);
		} else {
			f = 1.0F;
		}

		Holder<SoundEvent> holder;
		if (noteblockinstrument.hasCustomSound()) {
			ResourceLocation resourcelocation = this.getCustomSoundId(level, pos);
			if (resourcelocation == null) {
				return false;
			}

			holder = Holder.direct(SoundEvent.createVariableRangeEvent(resourcelocation));
		} else {
			holder = noteblockinstrument.getSoundEvent();
		}

		level.playSeededSound((Player) null, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, holder, SoundSource.RECORDS, 3.0F, f, level.random.nextLong());
		return true;
	}

	@Nullable
	private ResourceLocation getCustomSoundId(Level level, BlockPos pos) {
		BlockEntity blockentity = level.getBlockEntity(pos.above());
		if (blockentity instanceof SkullBlockEntity skullblockentity) {
			return skullblockentity.getNoteBlockSound();
		} else {
			return null;
		}
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> blockStateBuilder) {
		blockStateBuilder.add(TYPE, WATERLOGGED, INSTRUMENT, POWERED, NOTE);
	}
}
