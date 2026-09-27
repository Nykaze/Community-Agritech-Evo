package com.misterd.agritechevolved.blockentity.custom;

import com.misterd.agritechevolved.AgritechEvolved;
import com.misterd.agritechevolved.Config;
import com.misterd.agritechevolved.block.custom.PlanterBlock;
import com.misterd.agritechevolved.blockentity.ATEBlockEntities;
import com.misterd.agritechevolved.datamap.ATEDataMaps;
import com.misterd.agritechevolved.datamap.FertilizerData;
import com.misterd.agritechevolved.datamap.SoilModifierData;
import com.misterd.agritechevolved.gui.custom.BasicPlanterMenu;
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
import com.misterd.agritechevolved.util.LevelRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.*;

public class PlanterBlockEntity extends BlockEntity implements MenuProvider {

    private @Nullable CropRecipe cachedCropRecipe = null;
    private @Nullable TreeRecipe cachedTreeRecipe = null;
    private @Nullable Item cachedSeedItem = null;
    private Set<Item> cachedValidSoils = null;
    private int soilCacheRevision = -1;
    private int cachedRevision = -1;

    public final ItemStackHandler inventory = new ItemStackHandler(15) {
        @Override
        public int getSlotLimit(int slot) {
            return (slot == 0 || slot == 1) ? 1 : super.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (stack.isEmpty()) return false;
            return switch (slot) {
                case 0 -> isValidPlant(stack);
                case 1 -> isValidSoilForAnyRecipe(stack);
                case 2 -> isFertilizer(PlanterBlockEntity.this.level, stack);
                default -> true;
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (slot == 0) invalidateRecipeCache();
            PlanterBlockEntity.this.setChanged();
            Level lvl = PlanterBlockEntity.this.level;
            if (lvl != null && !lvl.isClientSide()) {
                lvl.sendBlockUpdated(
                        PlanterBlockEntity.this.getBlockPos(),
                        PlanterBlockEntity.this.getBlockState(),
                        PlanterBlockEntity.this.getBlockState(),
                        3
                );
            }
        }
    };

    private final LazyOptional<IItemHandler> insertHandler = LazyOptional.of(() -> new InsertHandler());
    private final LazyOptional<IItemHandler> extractHandler = LazyOptional.of(() -> new ExtractHandler());

    public int growthProgress = 0;
    public int growthTicks = 0;
    private boolean readyToHarvest = false;
    private int lastGrowthStage = -1;

    /**
     * Last computed cycle length in ticks. Kept so a resistance-retaining reset can convert the
     * stored percentage back into a tick count without recomputing every modifier.
     */
    private int lastAdjustedTime = 0;

    public PlanterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ATEBlockEntities.PLANTER_BLOCK_BE.get(), pos, blockState);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        insertHandler.invalidate();
        extractHandler.invalidate();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side != null && side.getAxis().isHorizontal()) {
                return insertHandler.cast();
            }
            if (side == Direction.DOWN) {
                return extractHandler.cast();
            }
        }
        return super.getCapability(cap, side);
    }

    /**
     * Direct access to the output-only handler, used by the silo to pull harvests without
     * going through a capability lookup.
     */
    public IItemHandler getExtractHandler() {
        return extractHandler.resolve().orElse(null);
    }

    public IItemHandler getInsertHandler() {
        return insertHandler.resolve().orElse(null);
    }

    private void invalidateRecipeCache() {
        cachedCropRecipe = null;
        cachedTreeRecipe = null;
        cachedSeedItem = null;
        cachedRevision = -1;
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
        return findTreeRecipe(getStack(0)).isPresent();
    }

    public static boolean isFertilizer(ItemStack stack) {
        return isFertilizer(null, stack);
    }

    public static boolean isFertilizer(Level level, ItemStack stack) {
        return !stack.isEmpty() && ATEDataMaps.getFertilizer(level, stack.getItem()) != null;
    }

    private float getClocheGrowthModifier(BlockState state) {
        return state.getValue(PlanterBlock.CLOCHED) ? (float) Config.getClocheSpeedMultiplier() : 1.0F;
    }

    private float getClocheYieldModifier(BlockState state) {
        return state.getValue(PlanterBlock.CLOCHED) ? (float) Config.getClocheYieldMultiplier() : 1.0F;
    }

    private float getFertilizerGrowthModifier() {
        ItemStack stack = getStack(2);
        if (stack.isEmpty()) return 1.0F;
        FertilizerData data = ATEDataMaps.getFertilizer(level, stack.getItem());
        return data != null ? data.speedMultiplier() : 1.0F;
    }

    private float getFertilizerYieldModifier() {
        ItemStack stack = getStack(2);
        if (stack.isEmpty()) return 1.0F;
        FertilizerData data = ATEDataMaps.getFertilizer(level, stack.getItem());
        return data != null ? data.yieldMultiplier() : 1.0F;
    }

    public float getSoilGrowthModifier(Level level, ItemStack soilStack) {
        if (soilStack.isEmpty()) return 1.0F;
        SoilModifierData data = ATEDataMaps.getSoilModifier(level, soilStack.getItem());
        return data != null ? data.growthModifier() : 1.0F;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PlanterBlockEntity be) {
        if (level.isClientSide()) return;

        ItemStack plantStack = be.getStack(0);
        ItemStack soilStack = be.getStack(1);

        if (plantStack.isEmpty() || soilStack.isEmpty()) {
            be.resetGrowth(PlantTraits.of(plantStack).progressRetainedOnReset());
            return;
        }

        if (!be.isValidPlantSoilCombination(plantStack, soilStack)) {
            be.resetGrowth(PlantTraits.of(plantStack).progressRetainedOnReset());
            return;
        }

        if (!be.readyToHarvest) {
            float soilMod = be.getSoilGrowthModifier(level, soilStack);
            float fertMod = be.getFertilizerGrowthModifier();
            float clocheMod = be.getClocheGrowthModifier(state);
            float traitMod = PlantTraits.of(plantStack).growthMultiplier();
            float totalMod = soilMod * fertMod * clocheMod * traitMod;
            int baseTime = Config.getPlanterBaseProcessingTime();
            int adjustedTime = Math.max(1, Math.round((float) baseTime / totalMod));
            be.lastAdjustedTime = adjustedTime;

            PlanterProcessingTimeEvent timeEvent = new PlanterProcessingTimeEvent(be, plantStack, adjustedTime);
            MinecraftForge.EVENT_BUS.post(timeEvent);
            adjustedTime = timeEvent.getProcessingTime();

            be.growthTicks++;

            be.initializeTraitsIfNeeded(plantStack);
            be.tryMutateTraits(plantStack);

            if (be.growthTicks >= adjustedTime) {
                be.readyToHarvest = true;
                be.growthProgress = 100;
                be.lastGrowthStage = be.getGrowthStage();
                level.sendBlockUpdated(pos, state, state, 3);
                be.setChanged();
            } else {
                be.growthProgress = (int) ((float) be.growthTicks / adjustedTime * 100.0F);
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
            be.harvestPlant(state);
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
        if (retainFraction > 0.0F && this.growthProgress > 0) {
            int base = Math.max(1, this.lastAdjustedTime);
            float keptPercent = this.growthProgress * retainFraction;
            this.growthTicks = Math.max(1, Math.round(base * (keptPercent / 100.0F)));
            this.growthProgress = Math.max(1, Math.min(99,
                    (int) ((float) this.growthTicks / base * 100.0F)));
        } else {
            this.growthProgress = 0;
            this.growthTicks = 0;
        }
        this.readyToHarvest = false;
        this.lastGrowthStage = -1;
        this.setChanged();
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

    public boolean isReadyToHarvest() {
        return readyToHarvest;
    }

    public void harvestPlant(BlockState state) {
        if (!readyToHarvest) return;

        PlanterPreHarvestEvent preEvent = new PlanterPreHarvestEvent(this, getStack(0));
        MinecraftForge.EVENT_BUS.post(preEvent);
        ItemStack seedForDrops = preEvent.getSeed();

        if (!ItemStack.matches(seedForDrops, getStack(0))) {
            ItemStack slotSeed = getStack(0);
            if (!slotSeed.isEmpty()) {
                slotSeed.shrink(1);
                ItemStack previous = getStack(0);
                inventory.setStackInSlot(0, seedForDrops.copy());
                if (!previous.isEmpty()) {
                    ItemStack leftover = new ItemStack(previous.getItem(), 1);
                    ItemStack rest = inventory.insertItem(0, leftover, false);
                    if (!rest.isEmpty()) {
                        Containers.dropItemStack(level, getBlockPos().getX() + 0.5, getBlockPos().getY() + 1.0,
                                getBlockPos().getZ() + 0.5, rest);
                    }
                }
            }
        }

        float fertYield = getFertilizerYieldModifier();
        float clocheYield = getClocheYieldModifier(state);
        float traitYield = PlantTraits.of(getStack(0)).yieldMultiplier();
        List<ItemStack> drops = new ArrayList<>(applyYieldModifier(getHarvestDrops(seedForDrops), fertYield * clocheYield * traitYield));

        PlanterPostHarvestEvent postEvent = new PlanterPostHarvestEvent(this, getStack(0), drops);
        MinecraftForge.EVENT_BUS.post(postEvent);
        drops = postEvent.getDrops();

        for (ItemStack drop : drops) {
            int remaining = drop.getCount();

            for (int slot = 3; slot <= 14 && remaining > 0; slot++) {
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

            for (int slot = 3; slot <= 14 && remaining > 0; slot++) {
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

    public void applyManualFertilizer(float speedMultiplier) {
        if (readyToHarvest) return;
        ItemStack plantStack = getStack(0);
        ItemStack soilStack = getStack(1);
        if (plantStack.isEmpty() || soilStack.isEmpty()) return;

        float soilMod = getSoilGrowthModifier(level, soilStack);
        float clocheMod = getClocheGrowthModifier(getBlockState());
        float traitMod = PlantTraits.of(plantStack).growthMultiplier();
        int adjustedTime = Math.max(1, Math.round(Config.getPlanterBaseProcessingTime() / (soilMod * clocheMod * traitMod)));
        lastAdjustedTime = adjustedTime;

        int boost = Math.max(1, Math.round(adjustedTime * 0.25F * speedMultiplier));
        growthTicks = Math.min(adjustedTime, growthTicks + boost);
        growthProgress = (int) ((float) growthTicks / adjustedTime * 100.0F);

        if (growthTicks >= adjustedTime) {
            readyToHarvest = true;
            growthProgress = 100;
        }

        lastGrowthStage = getGrowthStage();
        setChanged();
    }

    private void consumeFertilizer() {
        ItemStack stack = getStack(2);
        if (stack.isEmpty()) return;
        stack.shrink(1);
        if (stack.isEmpty()) {
            inventory.setStackInSlot(2, ItemStack.EMPTY);
        }
        setChanged();
    }

    private List<ItemStack> applyYieldModifier(List<ItemStack> drops, float mod) {
        if (mod == 1.0F) return drops;
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack drop : drops) {
            ItemStack copy = drop.copy();
            copy.setCount(Math.max(1, Math.round(drop.getCount() * mod)));
            out.add(copy);
        }
        return out;
    }

    private List<ItemStack> getHarvestDrops(ItemStack plantStack) {
        if (plantStack.isEmpty()) return List.of();

        Optional<CropRecipe> crop = findCropRecipe(plantStack);
        List<DropEntry> entries = crop.map(CropRecipe::getDrops)
                .orElseGet(() -> findTreeRecipe(plantStack).map(TreeRecipe::getDrops).orElse(List.of()));

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

    private static void tryOutputItemsBelow(Level level, BlockPos pos, PlanterBlockEntity be) {
        BlockPos below = pos.below();
        BlockEntity targetBe = level.getBlockEntity(below);
        if (targetBe == null) return;

        IItemHandler target = targetBe.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).resolve().orElse(null);
        if (target == null) return;

        boolean changed = false;
        for (int slot = 3; slot <= 14; slot++) {
            ItemStack stack = be.inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            ItemStack simulated = target.insertItem(0, stack.copy(), true);
            int insertable = stack.getCount() - simulated.getCount();
            if (insertable <= 0) continue;

            ItemStack extracted = be.inventory.extractItem(slot, insertable, false);
            if (extracted.isEmpty()) continue;

            ItemStack leftover = target.insertItem(0, extracted, false);
            if (!leftover.isEmpty()) {
                be.inventory.insertItem(slot, leftover, false);
                continue;
            }
            changed = true;
        }

        if (changed) {
            be.setChanged();
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
        }
    }

    public boolean hasOutputSpace() {
        List<ItemStack> drops = getHarvestDrops(getStack(0));

        Map<Integer, Integer> simAmounts = new HashMap<>();
        Map<Integer, Item> simItems = new HashMap<>();
        Map<Integer, Integer> simCapacity = new HashMap<>();

        for (int slot = 3; slot <= 14; slot++) {
            ItemStack s = getStack(slot);
            simAmounts.put(slot, s.getCount());
            simItems.put(slot, s.isEmpty() ? null : s.getItem());
            simCapacity.put(slot, s.isEmpty() ? 64 : s.getMaxStackSize());
        }

        for (ItemStack drop : drops) {
            int remaining = drop.getCount();

            for (int slot = 3; slot <= 14 && remaining > 0; slot++) {
                Item here = simItems.get(slot);
                if (here != null && here == drop.getItem()) {
                    int space = simCapacity.get(slot) - simAmounts.get(slot);
                    int toAdd = Math.min(space, remaining);
                    simAmounts.merge(slot, toAdd, Integer::sum);
                    remaining -= toAdd;
                }
            }

            for (int slot = 3; slot <= 14 && remaining > 0; slot++) {
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

    public ItemStack getStack(int slot) {
        return inventory.getStackInSlot(slot);
    }

    public float getGrowthProgress() {
        return growthProgress / 100.0F;
    }

    public int getGrowthStage() {
        if (isTree()) return growthProgress > 50 ? 1 : 0;
        return Math.min(8, (int) (growthProgress / 12.5F));
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

    public void dropCloche(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(PlanterBlock.CLOCHED)) {
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    new ItemStack(ATEItems.CLOCHE.get())));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", inventory.serializeNBT());
        tag.putInt("growthProgress", growthProgress);
        tag.putInt("growthTicks", growthTicks);
        tag.putBoolean("readyToHarvest", readyToHarvest);
        tag.putInt("lastGrowthStage", lastGrowthStage);
        tag.putInt("lastAdjustedTime", lastAdjustedTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("Items"));
        growthProgress = tag.getInt("growthProgress");
        growthTicks = tag.getInt("growthTicks");
        readyToHarvest = tag.getBoolean("readyToHarvest");
        lastGrowthStage = tag.contains("lastGrowthStage") ? tag.getInt("lastGrowthStage") : -1;
        lastAdjustedTime = tag.getInt("lastAdjustedTime");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.community_agritechevolved.basic_planter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new BasicPlanterMenu(id, inventory, this);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    private class InsertHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return inventory.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 2) return stack;
            if (!isFertilizer(PlanterBlockEntity.this.level, stack)) return stack;
            return inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (stack.isEmpty()) return false;
            return switch (slot) {
                case 0 -> isValidPlant(stack);
                case 1 -> isValidSoilForAnyRecipe(stack);
                case 2 -> isFertilizer(PlanterBlockEntity.this.level, stack);
                default -> true;
            };
        }
    }

    private class ExtractHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return inventory.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot < 3) return ItemStack.EMPTY;
            return inventory.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    }
}
