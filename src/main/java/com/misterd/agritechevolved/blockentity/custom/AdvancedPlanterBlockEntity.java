package com.misterd.agritechevolved.blockentity.custom;

import com.misterd.agritechevolved.AgritechEvolved;
import com.misterd.agritechevolved.Config;
import com.misterd.agritechevolved.block.custom.AdvancedPlanterBlock;
import com.misterd.agritechevolved.blockentity.ATEBlockEntities;
import com.misterd.agritechevolved.datamap.ATEDataMaps;
import com.misterd.agritechevolved.datamap.FertilizerData;
import com.misterd.agritechevolved.datamap.SoilModifierData;
import com.misterd.agritechevolved.gui.custom.AdvancedPlanterMenu;
import com.misterd.agritechevolved.item.ATEItems;
import com.misterd.agritechevolved.integration.PlanterPostHarvestEvent;
import com.misterd.agritechevolved.integration.PlanterPreHarvestEvent;
import com.misterd.agritechevolved.integration.PlanterProcessingTimeEvent;
import com.misterd.agritechevolved.trait.PlantTraits;
import com.misterd.agritechevolved.recipe.ATERecipeTypes;
import com.misterd.agritechevolved.recipe.CropRecipe;
import com.misterd.agritechevolved.recipe.DropEntry;
import com.misterd.agritechevolved.recipe.SingleStackContainer;
import com.misterd.agritechevolved.recipe.TreeRecipe;
import com.misterd.agritechevolved.util.ATETags;
import com.misterd.agritechevolved.util.RegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import com.misterd.agritechevolved.util.LevelRecipes;
import com.misterd.agritechevolved.util.LevelRecipes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Forge 1.20.1 port. The NeoForge original used the modern transfer API
 * ({@code ItemStacksResourceHandler}, {@code Transaction}, {@code ResourceHandler}) and the
 * modern recipe holder/input types. Those are replaced here by the Forge 47.x equivalents:
 * {@link ItemStackHandler} / {@link IItemHandler} for storage, {@link IEnergyStorage}
 * implemented directly on the block entity, and the block entity's own {@code save/load}
 * hooks. Capabilities are exposed by overriding {@link #getCapability} rather than through
 * a registration event.
 */
public class AdvancedPlanterBlockEntity extends BlockEntity implements MenuProvider, IEnergyStorage {

    private static final int SLOT_PLANT = 0;
    private static final int SLOT_SOIL = 1;
    private static final int SLOT_MODULE_1 = 2;
    private static final int SLOT_MODULE_2 = 3;
    private static final int SLOT_FERTILIZER = 4;
    private static final int SLOT_OUTPUT_MIN = 5;
    private static final int SLOT_OUTPUT_MAX = 16;
    private static final int TOTAL_SLOTS = 17;

    private static final String SM_MK1 = "community_agritechevolved:sm_mk1";
    private static final String SM_MK2 = "community_agritechevolved:sm_mk2";
    private static final String SM_MK3 = "community_agritechevolved:sm_mk3";
    private static final String YM_MK1 = "community_agritechevolved:ym_mk1";
    private static final String YM_MK2 = "community_agritechevolved:ym_mk2";
    private static final String YM_MK3 = "community_agritechevolved:ym_mk3";

    private @Nullable CropRecipe cachedCropRecipe = null;
    private @Nullable TreeRecipe cachedTreeRecipe = null;
    private @Nullable Item cachedSeedItem = null;
    private Set<Item> cachedValidSoils = null;
    private int soilCacheRevision = -1;
    private int cachedRevision = -1;

    public final ItemStackHandler inventory = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        public int getSlotLimit(int slot) {
            return (slot == SLOT_PLANT || slot == SLOT_SOIL || slot == SLOT_MODULE_1 || slot == SLOT_MODULE_2)
                    ? 1
                    : super.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (stack.isEmpty()) return false;
            return switch (slot) {
                case SLOT_PLANT -> {
                    if (!isValidPlant(stack)) yield false;
                    ItemStack soil = getStack(SLOT_SOIL);
                    if (soil.isEmpty()) yield true;
                    yield isValidPlantSoilCombination(stack, soil);
                }
                case SLOT_SOIL -> {
                    if (!isValidSoilForAnyRecipe(stack)) yield false;
                    ItemStack plant = getStack(SLOT_PLANT);
                    if (plant.isEmpty()) yield true;
                    yield isValidPlantSoilCombination(plant, stack);
                }
                case SLOT_MODULE_1, SLOT_MODULE_2 -> stack.is(ATETags.Items.ATE_MODULES);
                case SLOT_FERTILIZER -> isFertilizer(AdvancedPlanterBlockEntity.this.level, stack);
                default -> true;
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            AdvancedPlanterBlockEntity.this.setChanged();
            invalidateRecipeCache();
            Level lvl = AdvancedPlanterBlockEntity.this.level;
            if (lvl != null && !lvl.isClientSide()) {
                BlockPos p = AdvancedPlanterBlockEntity.this.getBlockPos();
                lvl.sendBlockUpdated(p, AdvancedPlanterBlockEntity.this.getBlockState(),
                        AdvancedPlanterBlockEntity.this.getBlockState(), 3);
            }
        }
    };

    private final LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> this);

    private final LazyOptional<IItemHandler> insertCapability =
            LazyOptional.of(() -> new FertilizerInsertHandler(this));
    private final LazyOptional<IItemHandler> extractCapability =
            LazyOptional.of(() -> new OutputExtractHandler(this));

    private int growthProgress = 0;
    private int growthTicks = 0;
    private boolean readyToHarvest = false;
    private int lastGrowthStage = -1;

    /**
     * Last computed cycle length in ticks. Kept so a resistance-retaining reset can convert the
     * stored percentage back into a tick count without recomputing every modifier.
     */
    private int lastAdjustedTime = 0;
    private int energyStored = 0;
    private float currentTotalModifier = 1.0F;

    public AdvancedPlanterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ATEBlockEntities.ADVANCED_PLANTER_BLOCK_BE.get(), pos, blockState);
    }

    private void invalidateRecipeCache() {
        cachedCropRecipe = null;
        cachedTreeRecipe = null;
        cachedSeedItem = null;
        cachedRevision = -1;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.community_agritechevolved.advanced_planter");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new AdvancedPlanterMenu(id, inv, this);
    }

    @Nullable
    private RecipeManager getRecipes() {
        return LevelRecipes.get(level);
    }

    private void refreshRecipeCacheIfNeeded(ItemStack seed) {
        if (seed.isEmpty()) {
            invalidateRecipeCache();
            return;
        }
        Item seedItem = seed.getItem();
        if (seedItem == cachedSeedItem && cachedRevision == AgritechEvolved.RECIPE_REVISION) return;

        invalidateRecipeCache();
        RecipeManager rm = getRecipes();
        if (rm == null) return;

        cachedSeedItem = seedItem;
        cachedRevision = AgritechEvolved.RECIPE_REVISION;
        SingleStackContainer input = new SingleStackContainer(seed);

        Optional<CropRecipe> crop = rm.getRecipeFor(ATERecipeTypes.CROP_TYPE.get(), input, level);
        if (crop.isPresent()) {
            cachedCropRecipe = crop.get();
            return;
        }

        Optional<TreeRecipe> tree = rm.getRecipeFor(ATERecipeTypes.TREE_TYPE.get(), input, level);
        tree.ifPresent(recipe -> cachedTreeRecipe = recipe);
    }

    private Optional<CropRecipe> findCropRecipe(ItemStack seed) {
        if (seed.isEmpty()) return Optional.empty();
        refreshRecipeCacheIfNeeded(seed);
        return Optional.ofNullable(cachedCropRecipe);
    }

    private Optional<TreeRecipe> findTreeRecipe(ItemStack sapling) {
        if (sapling.isEmpty()) return Optional.empty();
        refreshRecipeCacheIfNeeded(sapling);
        return Optional.ofNullable(cachedTreeRecipe);
    }

    public boolean isValidPlant(ItemStack stack) {
        if (level == null) return false;
        return findCropRecipe(stack).isPresent() || findTreeRecipe(stack).isPresent();
    }

    private Set<Item> getValidSoils() {
        if (cachedValidSoils != null && soilCacheRevision == AgritechEvolved.RECIPE_REVISION) {
            return cachedValidSoils;
        }
        RecipeManager rm = getRecipes();
        if (rm == null) return Set.of();
        Set<Item> soils = new HashSet<>();
        for (Recipe<?> recipe : rm.getRecipes()) {
            if (recipe instanceof CropRecipe crop) {
                for (Ingredient ing : crop.getSoils()) {
                    for (ItemStack stack : ing.getItems()) soils.add(stack.getItem());
                }
            } else if (recipe instanceof TreeRecipe tree) {
                for (Ingredient ing : tree.getSoils()) {
                    for (ItemStack stack : ing.getItems()) soils.add(stack.getItem());
                }
            }
        }
        cachedValidSoils = soils;
        soilCacheRevision = AgritechEvolved.RECIPE_REVISION;
        return cachedValidSoils;
    }

    public boolean isValidSoilForAnyRecipe(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return getValidSoils().contains(stack.getItem());
    }

    public boolean isValidPlantSoilCombination(ItemStack plant, ItemStack soil) {
        Optional<CropRecipe> crop = findCropRecipe(plant);
        if (crop.isPresent()) return crop.get().matchesSoil(soil);
        Optional<TreeRecipe> tree = findTreeRecipe(plant);
        if (tree.isPresent()) return tree.get().matchesSoil(soil);
        return false;
    }

    public boolean isTree() {
        return findTreeRecipe(getStack(SLOT_PLANT)).isPresent();
    }

    public static boolean isFertilizer(ItemStack stack) {
        return isFertilizer(null, stack);
    }

    public static boolean isFertilizer(Level level, ItemStack stack) {
        return !stack.isEmpty() && ATEDataMaps.getFertilizer(level, stack.getItem()) != null;
    }

    // ------------------------------------------------------------------ energy

    @Override
    public int getEnergyStored() {
        return energyStored;
    }

    @Override
    public int getMaxEnergyStored() {
        return Config.getPlanterEnergyBuffer();
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = Math.min(maxReceive, getMaxEnergyStored() - energyStored);
        if (received <= 0) return 0;
        if (!simulate) {
            energyStored += received;
            setChanged();
        }
        return received;
    }

    /**
     * The original energy handler was insert-only: the advanced planter buffers power but
     * never pushes it back out, so extraction is deliberately a no-op.
     */
    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    private boolean consumeEnergy() {
        int required = Math.round(Config.getPlanterBasePowerConsumption() * getModulePowerModifier());
        if (energyStored < required) return false;
        energyStored -= required;
        setChanged();
        return true;
    }

    // ------------------------------------------------------------------ modules

    public float getModuleSpeedModifier() {
        float speed = 1.0F, penalty = 1.0F;
        for (int slot = SLOT_MODULE_1; slot <= SLOT_MODULE_2; slot++) {
            String id = RegistryHelper.getItemId(getStack(slot));
            if (id.isEmpty()) continue;
            speed *= switch (id) {
                case SM_MK1 -> (float) Config.getSpeedModuleMk1Multiplier();
                case SM_MK2 -> (float) Config.getSpeedModuleMk2Multiplier();
                case SM_MK3 -> (float) Config.getSpeedModuleMk3Multiplier();
                default -> 1.0F;
            };
            penalty *= switch (id) {
                case YM_MK1 -> (float) Config.getYieldModuleMk1SpeedPenalty();
                case YM_MK2 -> (float) Config.getYieldModuleMk2SpeedPenalty();
                case YM_MK3 -> (float) Config.getYieldModuleMk3SpeedPenalty();
                default -> 1.0F;
            };
        }
        return speed * penalty;
    }

    public float getModuleYieldModifier() {
        float yield = 1.0F;
        for (int slot = SLOT_MODULE_1; slot <= SLOT_MODULE_2; slot++) {
            String id = RegistryHelper.getItemId(getStack(slot));
            yield *= switch (id) {
                case YM_MK1 -> (float) Config.getYieldModuleMk1Multiplier();
                case YM_MK2 -> (float) Config.getYieldModuleMk2Multiplier();
                case YM_MK3 -> (float) Config.getYieldModuleMk3Multiplier();
                default -> 1.0F;
            };
        }
        return yield;
    }

    public float getModulePowerModifier() {
        float power = 1.0F;
        for (int slot = SLOT_MODULE_1; slot <= SLOT_MODULE_2; slot++) {
            String id = RegistryHelper.getItemId(getStack(slot));
            power *= switch (id) {
                case SM_MK1 -> (float) Config.getSpeedModuleMk1PowerMultiplier();
                case SM_MK2 -> (float) Config.getSpeedModuleMk2PowerMultiplier();
                case SM_MK3 -> (float) Config.getSpeedModuleMk3PowerMultiplier();
                default -> 1.0F;
            };
        }
        return power;
    }

    private float getClocheGrowthModifier() {
        return getBlockState().getValue(AdvancedPlanterBlock.CLOCHED)
                ? (float) Config.getClocheSpeedMultiplier() : 1.0F;
    }

    private float getClocheYieldModifier() {
        return getBlockState().getValue(AdvancedPlanterBlock.CLOCHED)
                ? (float) Config.getClocheYieldMultiplier() : 1.0F;
    }

    private float getFertilizerGrowthModifier() {
        return getFertilizerModifier(true);
    }

    private float getFertilizerYieldModifier() {
        return getFertilizerModifier(false);
    }

    private float getFertilizerModifier(boolean forSpeed) {
        ItemStack stack = getStack(SLOT_FERTILIZER);
        if (stack.isEmpty()) return 1.0F;
        FertilizerData data = ATEDataMaps.getFertilizer(level, stack.getItem());
        return data != null ? (forSpeed ? data.speedMultiplier() : data.yieldMultiplier()) : 1.0F;
    }

    public float getSoilGrowthModifier(Level level, ItemStack soil) {
        if (soil.isEmpty()) return 1.0F;
        SoilModifierData data = ATEDataMaps.getSoilModifier(level, soil.getItem());
        return data != null ? data.growthModifier() : 1.0F;
    }

    // ------------------------------------------------------------------ ticking

    public static void tick(Level level, BlockPos pos, BlockState state, AdvancedPlanterBlockEntity be) {
        if (level.isClientSide()) return;

        boolean powered = be.energyStored > 0;
        if (state.getValue(AdvancedPlanterBlock.POWERED) != powered) {
            level.setBlock(pos, state.setValue(AdvancedPlanterBlock.POWERED, powered), 3);
        }

        ItemStack plant = be.getStack(SLOT_PLANT);
        ItemStack soil = be.getStack(SLOT_SOIL);

        if (plant.isEmpty() || soil.isEmpty()) {
            be.resetGrowth(PlantTraits.of(plant).progressRetainedOnReset());
            return;
        }

        if (!be.isValidPlantSoilCombination(plant, soil)) {
            be.resetGrowth(PlantTraits.of(plant).progressRetainedOnReset());
            return;
        }

        if (!be.readyToHarvest) {
            if (!be.consumeEnergy()) return;

            float totalModifier = be.getSoilGrowthModifier(level, soil)
                    * be.getModuleSpeedModifier()
                    * be.getFertilizerGrowthModifier()
                    * be.getClocheGrowthModifier()
                    * PlantTraits.of(plant).growthMultiplier();
            be.currentTotalModifier = totalModifier;

            int growthTime = Math.max(1, Math.round(Config.getAdvancedPlanterBaseProcessingTime() / totalModifier));
            be.lastAdjustedTime = growthTime;

            PlanterProcessingTimeEvent timeEvent = new PlanterProcessingTimeEvent(be, plant, growthTime);
            MinecraftForge.EVENT_BUS.post(timeEvent);
            growthTime = timeEvent.getProcessingTime();
            be.lastAdjustedTime = growthTime;

            be.growthTicks++;

            be.initializeTraitsIfNeeded(plant);
            be.tryMutateTraits(plant);

            if (be.growthTicks >= growthTime) {
                be.readyToHarvest = true;
                be.growthProgress = 100;
                be.lastGrowthStage = be.getGrowthStage();
                level.sendBlockUpdated(pos, state, state, 3);
                be.setChanged();
            } else {
                be.growthProgress = (int) (be.growthTicks / (float) growthTime * 100);
                int stage = be.getGrowthStage();
                boolean stageChanged = stage != be.lastGrowthStage;
                if (stageChanged) {
                    be.lastGrowthStage = stage;
                }
                if (stageChanged || (be.isTree() && be.growthTicks % 10 == 0)) {
                    level.sendBlockUpdated(pos, state, state, 3);
                    be.setChanged();
                }
            }
        }

        if (be.readyToHarvest && be.hasOutputSpace()) {
            be.harvestPlant();
            tryOutputItemsBelow(level, pos, be);
        }
    }

    private void resetGrowth() {
        resetGrowth(0.0F);
    }

    /**
     * Wipes the growth cycle. The resistance trait keeps a fraction of the progress already
     * banked, so a plant that briefly loses its soil (item removed, wrong soil inserted) does
     * not always start from zero. Harvest readiness is never preserved, only progress.
     */
    private void resetGrowth(float retainFraction) {
        if (retainFraction > 0.0F && growthProgress > 0) {
            int base = Math.max(1, lastAdjustedTime);
            float keptPercent = growthProgress * retainFraction;
            growthTicks = Math.max(1, Math.round(base * (keptPercent / 100.0F)));
            growthProgress = Math.max(1, Math.min(99,
                    (int) ((float) growthTicks / base * 100.0F)));
        } else {
            growthProgress = 0;
            growthTicks = 0;
        }
        readyToHarvest = false;
        lastGrowthStage = -1;
        setChanged();
    }

    /**
     * Gives a seed that has never grown traits a starting roll, so planting an ordinary seed
     * produces a plant that can be improved. Only runs while the seed is actually growing.
     */
    public void initializeTraitsIfNeeded(ItemStack plantStack) {
        if (level == null || plantStack == null || plantStack.isEmpty()) return;
        if (PlantTraits.hasTraits(plantStack)) return;
        PlantTraits.applyTo(plantStack, PlantTraits.roll(level.getRandom()));
        setChanged();
    }

    /**
     * Rolls the plant's mutability trait. On a hit, one random trait that is not yet maxed is
     * bumped a level and written back to the seed, so the improvement survives a harvest cycle
     * and travels with the seed when the crop regrows.
     */
    public void tryMutateTraits(ItemStack plantStack) {
        if (level == null || plantStack == null || plantStack.isEmpty()) return;
        PlantTraits traits = PlantTraits.of(plantStack);
        float chance = traits.mutationChancePerTick();
        if (chance <= 0.0F) return;
        if (level.getRandom().nextFloat() >= chance) return;

        PlantTraits improved = traits.improve(level.getRandom());
        if (improved == null) return;
        PlantTraits.applyTo(plantStack, improved);
        setChanged();
    }

    public float getGrowthProgress() {
        return growthProgress / 100.0F;
    }

    public int getGrowthStage() {
        return isTree()
                ? (growthProgress > 50 ? 1 : 0)
                : Math.min(8, (int) (growthProgress / 12.5F));
    }

    private static void tryOutputItemsBelow(Level level, BlockPos pos, AdvancedPlanterBlockEntity be) {
        BlockPos below = pos.below();
        BlockEntity targetBe = level.getBlockEntity(below);
        if (targetBe == null) return;

        IItemHandler target = targetBe.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).resolve().orElse(null);
        if (target == null) return;

        boolean changed = false;
        for (int slot = SLOT_OUTPUT_MIN; slot <= SLOT_OUTPUT_MAX; slot++) {
            ItemStack stack = be.inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            int available = stack.getCount();

            ItemStack simulated = target.insertItem(0, stack.copy(), true);
            int insertable = available - simulated.getCount();
            if (insertable <= 0) continue;

            ItemStack extracted = be.inventory.extractItem(slot, insertable, false);
            if (extracted.isEmpty()) continue;

            ItemStack leftover = target.insertItem(0, extracted, false);
            if (!leftover.isEmpty()) {
                be.inventory.insertItem(slot, leftover, false);
            }
            be.setChanged();
            changed = true;
        }

        if (changed) {
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level == null) return;
        if (getBlockState().getValue(AdvancedPlanterBlock.CLOCHED)) {
            level.addFreshEntity(new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                    worldPosition.getZ() + 0.5, new ItemStack(ATEItems.CLOCHE.get())));
        }
        drops();
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++) {
            inv.setItem(i, getStack(i));
        }
        Containers.dropContents(level, worldPosition, inv);
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    public boolean hasOutputSpace() {
        List<ItemStack> drops = getHarvestDrops(getStack(SLOT_PLANT));
        Map<Integer, Integer> simAmounts = new HashMap<>();
        Map<Integer, Item> simItems = new HashMap<>();
        Map<Integer, Integer> simCap = new HashMap<>();

        for (int slot = SLOT_OUTPUT_MIN; slot <= SLOT_OUTPUT_MAX; slot++) {
            ItemStack s = getStack(slot);
            simAmounts.put(slot, s.getCount());
            simItems.put(slot, s.isEmpty() ? null : s.getItem());
            simCap.put(slot, s.isEmpty() ? 64 : s.getMaxStackSize());
        }

        for (ItemStack drop : drops) {
            int remaining = drop.getCount();

            for (int slot = SLOT_OUTPUT_MIN; slot <= SLOT_OUTPUT_MAX && remaining > 0; slot++) {
                Item here = simItems.get(slot);
                if (here != null && here == drop.getItem()) {
                    int space = simCap.get(slot) - simAmounts.get(slot);
                    int toAdd = Math.min(space, remaining);
                    simAmounts.merge(slot, toAdd, Integer::sum);
                    remaining -= toAdd;
                }
            }

            for (int slot = SLOT_OUTPUT_MIN; slot <= SLOT_OUTPUT_MAX && remaining > 0; slot++) {
                if (simItems.get(slot) == null) {
                    simItems.put(slot, drop.getItem());
                    simAmounts.put(slot, remaining);
                    remaining = 0;
                }
            }

            if (remaining > 0) return false;
        }
        return true;
    }

    public void harvestPlant() {
        if (!readyToHarvest) return;

        PlanterPreHarvestEvent preEvent = new PlanterPreHarvestEvent(this, getStack(SLOT_PLANT));
        MinecraftForge.EVENT_BUS.post(preEvent);
        ItemStack seedForDrops = preEvent.getSeed();

        if (!ItemStack.matches(seedForDrops, getStack(SLOT_PLANT))) {
            ItemStack slotSeed = getStack(SLOT_PLANT);
            if (!slotSeed.isEmpty()) {
                slotSeed.shrink(1);
                inventory.setStackInSlot(SLOT_PLANT, seedForDrops.copy());
                ItemStack previous = getStack(SLOT_PLANT);
                if (!previous.isEmpty()) {
                    ItemStack leftover = new ItemStack(previous.getItem(), 1);
                    ItemStack rest = inventory.insertItem(SLOT_PLANT, leftover, false);
                    if (!rest.isEmpty()) {
                        Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0,
                                worldPosition.getZ() + 0.5, rest);
                    }
                }
            }
        }

        float yieldModifier = getFertilizerYieldModifier() * getModuleYieldModifier() * getClocheYieldModifier()
                * PlantTraits.of(getStack(SLOT_PLANT)).yieldMultiplier();
        List<ItemStack> drops = new ArrayList<>(applyYieldModifier(getHarvestDrops(seedForDrops), yieldModifier));

        PlanterPostHarvestEvent postEvent = new PlanterPostHarvestEvent(this, getStack(SLOT_PLANT), drops);
        MinecraftForge.EVENT_BUS.post(postEvent);
        drops = postEvent.getDrops();

        for (ItemStack drop : drops) {
            int remaining = drop.getCount();

            for (int slot = SLOT_OUTPUT_MIN; slot <= SLOT_OUTPUT_MAX && remaining > 0; slot++) {
                ItemStack existing = getStack(slot);
                if (!existing.isEmpty() && existing.is(drop.getItem())) {
                    int space = existing.getMaxStackSize() - existing.getCount();
                    if (space <= 0) continue;
                    int toAdd = Math.min(space, remaining);
                    ItemStack add = drop.copy();
                    add.setCount(toAdd);
                    ItemStack notInserted = inventory.insertItem(slot, add, false);
                    remaining -= toAdd - notInserted.getCount();
                }
            }

            for (int slot = SLOT_OUTPUT_MIN; slot <= SLOT_OUTPUT_MAX && remaining > 0; slot++) {
                if (getStack(slot).isEmpty()) {
                    int toPlace = Math.min(remaining, drop.getMaxStackSize());
                    ItemStack add = drop.copy();
                    add.setCount(toPlace);
                    ItemStack notInserted = inventory.insertItem(slot, add, false);
                    remaining -= toPlace - notInserted.getCount();
                }
            }

            if (remaining > 0) break;
        }

        consumeFertilizer();
        resetGrowth();
    }

    public boolean isReadyToHarvest() {
        return readyToHarvest;
    }

    public void applyManualFertilizer(float speedMultiplier) {
        if (readyToHarvest) return;
        ItemStack soil = getStack(SLOT_SOIL);
        ItemStack plant = getStack(SLOT_PLANT);
        float totalMod = getSoilGrowthModifier(level, soil)
                * getModuleSpeedModifier()
                * getClocheGrowthModifier()
                * PlantTraits.of(plant).growthMultiplier()
                * speedMultiplier;
        int growthTime = Math.max(1, Math.round(Config.getAdvancedPlanterBaseProcessingTime() / totalMod));
        lastAdjustedTime = growthTime;
        int boost = Math.max(1, Math.round(growthTime * 0.1f * speedMultiplier));
        growthTicks = Math.min(growthTicks + boost, growthTime);
        growthProgress = (int) (growthTicks / (float) growthTime * 100);
        if (growthTicks >= growthTime) {
            readyToHarvest = true;
            growthProgress = 100;
        }
        setChanged();
    }

    private void consumeFertilizer() {
        ItemStack stack = getStack(SLOT_FERTILIZER);
        if (stack.isEmpty()) return;
        inventory.extractItem(SLOT_FERTILIZER, 1, false);
        setChanged();
    }

    private List<ItemStack> applyYieldModifier(List<ItemStack> drops, float modifier) {
        if (modifier == 1.0F) return drops;
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack drop : drops) {
            result.add(drop.copyWithCount(Math.max(1, Math.round(drop.getCount() * modifier))));
        }
        return result;
    }

    private List<ItemStack> getHarvestDrops(ItemStack plant) {
        if (plant.isEmpty()) return List.of();

        Optional<CropRecipe> crop = findCropRecipe(plant);
        List<DropEntry> entries = crop.map(CropRecipe::getDrops)
                .orElseGet(() -> findTreeRecipe(plant).map(TreeRecipe::getDrops).orElse(List.of()));

        List<ItemStack> drops = new ArrayList<>();
        RandomSource rng = level != null ? level.getRandom() : RandomSource.create();
        for (DropEntry entry : entries) {
            if (rng.nextFloat() <= entry.chance()) {
                int count = entry.max() > entry.min()
                        ? entry.min() + rng.nextInt(entry.max() - entry.min() + 1)
                        : entry.min();
                drops.add(new ItemStack(entry.item(), count));
            }
        }
        return drops;
    }

    // ------------------------------------------------------------------ capabilities

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyCapability.cast();
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return (side == Direction.DOWN ? extractCapability : insertCapability).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCapability.invalidate();
        insertCapability.invalidate();
        extractCapability.invalidate();
    }

    /**
     * Direct access to the output-only handler, used by the silo to pull harvests without
     * going through a capability lookup.
     */
    public IItemHandler getExtractHandler() {
        return extractCapability.resolve().orElse(null);
    }

    public IItemHandler getInsertHandler() {
        return insertCapability.resolve().orElse(null);
    }

    /**
     * Insert side of the advanced planter: only the fertilizer slot accepts automation input.
     */
    private static class FertilizerInsertHandler implements IItemHandler {
        private final AdvancedPlanterBlockEntity be;

        FertilizerInsertHandler(AdvancedPlanterBlockEntity be) {
            this.be = be;
        }

        @Override
        public int getSlots() {
            return be.inventory.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return be.inventory.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != SLOT_FERTILIZER) return stack;
            if (!isFertilizer(be.level, stack)) return stack;
            return be.inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return be.inventory.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (stack.isEmpty()) return false;
            return switch (slot) {
                case SLOT_PLANT -> be.isValidPlant(stack);
                case SLOT_SOIL -> be.isValidSoilForAnyRecipe(stack);
                case SLOT_FERTILIZER -> isFertilizer(be.level, stack);
                default -> false;
            };
        }
    }

    /**
     * Extract side (facing down): automation may only pull harvested output.
     */
    private static class OutputExtractHandler implements IItemHandler {
        private final AdvancedPlanterBlockEntity be;

        OutputExtractHandler(AdvancedPlanterBlockEntity be) {
            this.be = be;
        }

        @Override
        public int getSlots() {
            return be.inventory.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return be.inventory.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot < SLOT_OUTPUT_MIN) return ItemStack.EMPTY;
            return be.inventory.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return be.inventory.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    }

    // ------------------------------------------------------------------ sync

    public ItemStack getStack(int slot) {
        return inventory.getStackInSlot(slot);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", inventory.serializeNBT());
        tag.putInt("growthProgress", growthProgress);
        tag.putInt("growthTicks", growthTicks);
        tag.putBoolean("readyToHarvest", readyToHarvest);
        tag.putInt("energyStored", energyStored);
        tag.putInt("lastGrowthStage", lastGrowthStage);
        tag.putInt("lastAdjustedTime", lastAdjustedTime);
        tag.putFloat("currentTotalModifier", currentTotalModifier);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            inventory.deserializeNBT(tag.getCompound("Items"));
        }
        growthProgress = tag.getInt("growthProgress");
        growthTicks = tag.getInt("growthTicks");
        readyToHarvest = tag.getBoolean("readyToHarvest");
        energyStored = tag.getInt("energyStored");
        lastGrowthStage = tag.contains("lastGrowthStage") ? tag.getInt("lastGrowthStage") : -1;
        lastAdjustedTime = tag.getInt("lastAdjustedTime");
        currentTotalModifier = tag.contains("currentTotalModifier") ? tag.getFloat("currentTotalModifier") : 1.0F;
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
}
