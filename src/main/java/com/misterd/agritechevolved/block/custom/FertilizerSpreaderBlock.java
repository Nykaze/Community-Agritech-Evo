package com.misterd.agritechevolved.block.custom;

import com.misterd.agritechevolved.blockentity.ATEBlockEntities;
import com.misterd.agritechevolved.blockentity.custom.FertilizerSpreaderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import net.minecraftforge.network.NetworkHooks;

public class FertilizerSpreaderBlock extends BaseEntityBlock {

    public static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 13, 3, 10, 16),
            Block.box(0, 0, 0, 3, 10, 3),
            Block.box(13, 0, 0, 16, 10, 3),
            Block.box(13, 0, 13, 16, 10, 16),
            Block.box(6, 0, 6, 10, 4, 10),
            Block.box(4, 4, 4, 12, 10, 12),
            Block.box(2, 10, 2, 14, 11, 14),
            Block.box(0, 10, 0, 16, 16, 2),
            Block.box(0, 10, 14, 16, 16, 16),
            Block.box(0, 10, 2, 2, 16, 14),
            Block.box(14, 10, 2, 16, 16, 14)
    );
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public FertilizerSpreaderBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FertilizerSpreaderBlockEntity(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            level.updateNeighborsAt(pos, this);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FertilizerSpreaderBlockEntity spreader) {
            NetworkHooks.openScreen((ServerPlayer) player, spreader, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ATEBlockEntities.FERTILIZER_SPREADER_BE.get()
                ? (lvl, pos, blockState, be) -> FertilizerSpreaderBlockEntity.tick(lvl, pos, blockState, (FertilizerSpreaderBlockEntity) be)
                : null;
    }
}