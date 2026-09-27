package com.misterd.agritechevolved.block.custom;

import com.misterd.agritechevolved.blockentity.ATEBlockEntities;
import com.misterd.agritechevolved.blockentity.custom.AdvancedPlanterBlockEntity;
import com.misterd.agritechevolved.datamap.ATEDataMaps;
import com.misterd.agritechevolved.gui.custom.AdvancedPlanterMenu;
import com.misterd.agritechevolved.item.ATEItems;
import com.misterd.agritechevolved.item.custom.ClocheItem;
import com.misterd.agritechevolved.util.ATETags;
import com.misterd.agritechevolved.util.RegistryHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
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
import net.minecraftforge.common.ToolActions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.server.level.ServerPlayer;

public class AdvancedPlanterBlock extends BaseEntityBlock {

    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final BooleanProperty CLOCHED = BooleanProperty.create("cloched");
    public static final VoxelShape SHAPE = Shapes.or(
            Block.box(1, 0, 1, 3, 11, 3),
            Block.box(13, 0, 1, 15, 11, 3),
            Block.box(1, 0, 13, 3, 11, 15),
            Block.box(13, 0, 13, 15, 11, 15),
            Block.box(2, 2, 2, 14, 10, 3),
            Block.box(2, 2, 13, 14, 10, 14),
            Block.box(2, 2, 3, 3, 10, 13),
            Block.box(13, 2, 3, 14, 10, 13),
            Block.box(3, 2, 3, 13, 3, 13)
    );

    private static final List<String> FARMLAND_TIERS = List.of(
            "minecraft:farmland",
            "mysticalagriculture:inferium_farmland",
            "mysticalagriculture:prudentium_farmland",
            "mysticalagriculture:tertium_farmland",
            "mysticalagriculture:imperium_farmland",
            "mysticalagriculture:supremium_farmland",
            "mysticalagradditions:insanium_farmland"
    );

    private static final Map<String, String> ESSENCE_TO_FARMLAND = Map.of(
            "mysticalagriculture:inferium_essence", "mysticalagriculture:inferium_farmland",
            "mysticalagriculture:prudentium_essence", "mysticalagriculture:prudentium_farmland",
            "mysticalagriculture:tertium_essence", "mysticalagriculture:tertium_farmland",
            "mysticalagriculture:imperium_essence", "mysticalagriculture:imperium_farmland",
            "mysticalagriculture:supremium_essence", "mysticalagriculture:supremium_farmland",
            "mysticalagradditions:insanium_essence", "mysticalagradditions:insanium_farmland"
    );

    public AdvancedPlanterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(POWERED, false)
                .setValue(CLOCHED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, CLOCHED);
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
        return new AdvancedPlanterBlockEntity(pos, state);
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
        if (!(level.getBlockEntity(pos) instanceof AdvancedPlanterBlockEntity planter)) {
            return InteractionResult.FAIL;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        String heldItemId = RegistryHelper.getItemId(heldItem);

        if (player.isCrouching() && heldItem.isEmpty() && state.getValue(CLOCHED)) {
            return handleClocheRemoval(state, level, pos, player);
        }
        if (player.isCrouching()) {
            return handleCrouchOpen(level, pos, player, planter);
        }
        if (heldItem.getItem() instanceof ClocheItem) {
            return handleClochePlace(state, level, pos, player, heldItem);
        }
        if (planter.isValidPlant(heldItem)) {
            return handlePlantInsert(state, level, pos, player, planter, heldItem);
        }
        if (planter.isValidSoilForAnyRecipe(heldItem)) {
            return handleSoilInsert(state, level, pos, player, planter, heldItem);
        }
        if (isFertilizer(heldItem)) {
            return handleFertilizer(state, level, pos, player, planter, heldItem);
        }
        if (heldItem.getItem() instanceof HoeItem) {
            return handleHoeTill(level, pos, player, planter, heldItem, hand, hitResult);
        }
        if (heldItem.is(ATETags.Items.ATE_MODULES)) {
            return handleModuleInsert(level, pos, state, player, planter, heldItem);
        }
        if (ESSENCE_TO_FARMLAND.containsKey(heldItemId)) {
            return handleEssenceUpgrade(heldItem, level, pos, player, planter, heldItemId);
        }

        if (!level.isClientSide()) openGui(player, planter, pos);
        return InteractionResult.SUCCESS;
    }

    private static boolean isFertilizer(ItemStack stack) {
        return !stack.isEmpty() && ATEDataMaps.getFertilizer(stack.getItem()) != null;
    }

    private InteractionResult handleClocheRemoval(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(CLOCHED, false), 3);
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, new ItemStack(ATEItems.CLOCHE.get())));
            level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 0.5F, 1.2F);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleCrouchOpen(Level level, BlockPos pos, Player player, AdvancedPlanterBlockEntity planter) {
        if (!level.isClientSide()) openGui(player, planter, pos);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleClochePlace(BlockState state, Level level, BlockPos pos, Player player, ItemStack heldItem) {
        if (state.getValue(CLOCHED)) return InteractionResult.FAIL;
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(CLOCHED, true), 3);
            if (!player.getAbilities().instabuild) heldItem.shrink(1);
            level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handlePlantInsert(BlockState state, Level level, BlockPos pos, Player player, AdvancedPlanterBlockEntity planter, ItemStack heldItem) {
        if (!planter.getStack(0).isEmpty()) {
            if (!level.isClientSide()) openGui(player, planter, pos);
            return InteractionResult.SUCCESS;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack existingSoil = planter.getStack(1);
        if (!existingSoil.isEmpty() && !planter.isValidPlantSoilCombination(heldItem, existingSoil)) {
            player.displayClientMessage(Component.translatable("message.community_agritechevolved.invalid_seed_soil_combination").withStyle(ChatFormatting.GOLD), true);
            return InteractionResult.SUCCESS;
        }

        if (insertOne(planter, 0, heldItem) && !player.getAbilities().instabuild) {
            heldItem.shrink(1);
        }
        level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.sendBlockUpdated(pos, state, state, 2);
        planter.setChanged();
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleSoilInsert(BlockState state, Level level, BlockPos pos, Player player, AdvancedPlanterBlockEntity planter, ItemStack heldItem) {
        if (!planter.getStack(1).isEmpty()) {
            if (!level.isClientSide()) openGui(player, planter, pos);
            return InteractionResult.SUCCESS;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack existingPlant = planter.getStack(0);
        if (!existingPlant.isEmpty() && !planter.isValidPlantSoilCombination(existingPlant, heldItem)) {
            player.displayClientMessage(Component.translatable("message.community_agritechevolved.invalid_seed_soil_combination").withStyle(ChatFormatting.GOLD), true);
            return InteractionResult.SUCCESS;
        }

        if (insertOne(planter, 1, heldItem) && !player.getAbilities().instabuild) {
            heldItem.shrink(1);
        }
        level.playSound(null, pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
        level.sendBlockUpdated(pos, state, state, 2);
        planter.setChanged();
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleFertilizer(BlockState state, Level level, BlockPos pos, Player player, AdvancedPlanterBlockEntity planter, ItemStack heldItem) {
        if (planter.getStack(0).isEmpty() || planter.getStack(1).isEmpty() || planter.isReadyToHarvest()) {
            if (!level.isClientSide()) openGui(player, planter, pos);
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide()) {
            var fertData = ATEDataMaps.getFertilizer(level, heldItem.getItem());
            if (fertData != null) {
                planter.applyManualFertilizer(fertData.speedMultiplier());
                if (!player.getAbilities().instabuild) heldItem.shrink(1);
                level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                            pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 6, 0.3, 0.2, 0.3, 0.0);
                }
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleHoeTill(Level level, BlockPos pos, Player player, AdvancedPlanterBlockEntity planter, ItemStack heldItem, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack soilStack = planter.getStack(1);
        if (!soilStack.isEmpty() && soilStack.getItem() instanceof BlockItem soilBlockItem) {
            BlockState soilState = soilBlockItem.getBlock().defaultBlockState();
            BlockState result = soilState.getToolModifiedState(new UseOnContext(level, player, hand, heldItem, hitResult), ToolActions.HOE_TILL, false);
            if (result != null && result.getBlock() != soilState.getBlock() && replaceSlot(planter, 1, new ItemStack(result.getBlock()))) {
                level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.getAbilities().instabuild) {
                    heldItem.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private InteractionResult handleModuleInsert(Level level, BlockPos pos, BlockState state, Player player, AdvancedPlanterBlockEntity planter, ItemStack heldItem) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        for (int slot = 2; slot <= 3; slot++) {
            if (planter.getStack(slot).isEmpty()) {
                if (insertOne(planter, slot, heldItem) && !player.getAbilities().instabuild) {
                    heldItem.shrink(1);
                }
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.2F);
                level.sendBlockUpdated(pos, state, state, 2);
                planter.setChanged();
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleEssenceUpgrade(ItemStack stack, Level level, BlockPos pos, Player player, AdvancedPlanterBlockEntity planter, String heldItemId) {
        ItemStack soilStack = planter.getStack(1);
        if (!soilStack.isEmpty() && soilStack.getItem() instanceof BlockItem soilBlockItem) {
            String soilId = RegistryHelper.getBlockId(soilBlockItem.getBlock());
            boolean isValidFarmland = soilId.equals("minecraft:farmland")
                    || (soilId.startsWith("mysticalagriculture:") && soilId.endsWith("_farmland"))
                    || (soilId.startsWith("mysticalagradditions:") && soilId.endsWith("_farmland"));

            if (isValidFarmland) {
                String farmlandId = ESSENCE_TO_FARMLAND.get(heldItemId);
                Block resultBlock = RegistryHelper.getBlock(farmlandId);
                if (resultBlock != null) {
                    int currentTier = FARMLAND_TIERS.indexOf(soilId);
                    int targetTier = FARMLAND_TIERS.indexOf(farmlandId);
                    if (targetTier <= currentTier) {
                        if (!level.isClientSide()) {
                            player.displayClientMessage(Component.translatable("message.community_agritechevolved.same_ma_farmland").withStyle(ChatFormatting.GOLD), true);
                        }
                        return InteractionResult.SUCCESS;
                    }

                    if (replaceSlot(planter, 1, new ItemStack(resultBlock))) {
                        if (!player.getAbilities().instabuild) stack.shrink(1);
                        level.playSound(player, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.0F);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    private static boolean insertOne(AdvancedPlanterBlockEntity planter, int slot, ItemStack source) {
        return planter.inventory.insertItem(slot, source.copyWithCount(1), false).isEmpty();
    }

    private static boolean replaceSlot(AdvancedPlanterBlockEntity planter, int slot, ItemStack replacement) {
        ItemStack current = planter.getStack(slot);
        if (current.isEmpty()) return false;
        planter.inventory.setStackInSlot(slot, replacement);
        planter.setChanged();
        return true;
    }

    private void openGui(Player player, AdvancedPlanterBlockEntity planter, BlockPos pos) {
        NetworkHooks.openScreen((ServerPlayer) player, planter, pos);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ATEBlockEntities.ADVANCED_PLANTER_BLOCK_BE.get()
                ? (lvl, pos, blockState, be) -> AdvancedPlanterBlockEntity.tick(lvl, pos, blockState, (AdvancedPlanterBlockEntity) be)
                : null;
    }
}