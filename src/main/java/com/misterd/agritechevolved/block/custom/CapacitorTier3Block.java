package com.misterd.agritechevolved.block.custom;

import com.misterd.agritechevolved.blockentity.ATEBlockEntities;
import com.misterd.agritechevolved.blockentity.custom.CapacitorBlockEntity;
import com.misterd.agritechevolved.component.ATEDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.List;
import net.minecraftforge.network.NetworkHooks;

public class CapacitorTier3Block extends BaseEntityBlock {

    public static final BooleanProperty HAS_ENERGY = BooleanProperty.create("has_energy");
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public CapacitorTier3Block(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HAS_ENERGY, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_ENERGY, FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CapacitorBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CapacitorBlockEntity capacitor) {
            NetworkHooks.openScreen((ServerPlayer) player, capacitor, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            level.updateNeighborsAt(pos, this);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        if (!drops.isEmpty() && builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof CapacitorBlockEntity capacitor) {
            int energy = capacitor.getEnergyStored();
            if (energy > 0) {
                ItemStack drop = drops.get(0);
                ATEDataComponents.setStoredEnergy(drop, energy);
                ATEDataComponents.setCapacitorTier(drop, capacitor.getTier());
            }
        }
        return drops;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        int storedEnergy = ATEDataComponents.getStoredEnergy(stack);
        if (storedEnergy > 0 && level.getBlockEntity(pos) instanceof CapacitorBlockEntity capacitor) {
            capacitor.forceSetEnergy(storedEnergy);
            capacitor.setChanged();
        }
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ATEBlockEntities.CAPACITOR_BE.get()
                ? (lvl, pos, blockState, be) -> CapacitorBlockEntity.tick(lvl, pos, blockState, (CapacitorBlockEntity) be)
                : null;
    }
}