package com.misterd.agritechevolved.blockentity.custom;

import com.misterd.agritechevolved.Config;
import com.misterd.agritechevolved.block.custom.ComposterBlock;
import com.misterd.agritechevolved.blockentity.ATEBlockEntities;
import com.misterd.agritechevolved.gui.custom.ComposterMenu;
import com.misterd.agritechevolved.item.ATEItems;
import com.misterd.agritechevolved.util.RegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

/**
 * Forge 1.20.1 port. See {@link AdvancedPlanterBlockEntity} for the rationale behind the
 * substitution of the NeoForge transfer API with {@link ItemStackHandler} /
 * {@link IItemHandler} and a directly implemented {@link IEnergyStorage}.
 */
public class ComposterBlockEntity extends BlockEntity implements MenuProvider, IEnergyStorage {

    private static final int INPUT_SLOTS_START = 0;
    private static final int INPUT_SLOTS_COUNT = 12;
    private static final int OUTPUT_SLOTS_START = 12;
    private static final int OUTPUT_SLOTS_COUNT = 3;
    private static final int MODULE_SLOT = 15;
    private static final int TOTAL_SLOTS = 16;

    private static final String SM_MK1 = "community_agritechevolved:sm_mk1";
    private static final String SM_MK2 = "community_agritechevolved:sm_mk2";
    private static final String SM_MK3 = "community_agritechevolved:sm_mk3";

    private static final float COMPOST_TARGET = 7.0f;

    private int progress = 0;
    private int energyStored = 0;

    public final ItemStackHandler inventory = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        public int getSlotLimit(int slot) {
            return slot == MODULE_SLOT ? 1 : super.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (stack.isEmpty()) return false;
            if (slot >= INPUT_SLOTS_START && slot < INPUT_SLOTS_START + INPUT_SLOTS_COUNT)
                return isCompostableItem(stack);
            if (slot >= OUTPUT_SLOTS_START && slot < OUTPUT_SLOTS_START + OUTPUT_SLOTS_COUNT)
                return true;
            return slot == MODULE_SLOT && isSpeedModule(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            ComposterBlockEntity.this.setChanged();
            Level lvl = ComposterBlockEntity.this.level;
            if (lvl != null && !lvl.isClientSide()) {
                lvl.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new ComposterHandler(this));
    private final LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> this);

    public ComposterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ATEBlockEntities.COMPOSTER_BE.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ComposterBlockEntity be) {
        if (level.isClientSide()) return;

        boolean changed = false;
        int requiredEnergy = Config.getComposterBasePowerConsumption();
        boolean hasPower = be.energyStored >= requiredEnergy;

        int baseTime = Config.getComposterBaseProcessingTime();
        if (hasPower) baseTime /= 3;
        int actualTime = (int) Math.max(1, baseTime / be.getModuleSpeedModifier());

        if (be.canProcess()) {
            if (be.progress == 0) be.progress = 1;
            else be.progress++;
            changed = true;

            if (be.progress >= actualTime) {
                be.processItems();
                if (hasPower) {
                    int adjusted = (int) Math.ceil(requiredEnergy * be.getModulePowerModifier());
                    be.energyStored = Math.max(0, be.energyStored - adjusted);
                }
                be.progress = 0;
            }
        } else if (be.progress > 0) {
            be.progress = 0;
            changed = true;
        }

        if (changed) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            boolean shouldBePowered = be.progress > 0;
            boolean currentlyPowered = state.getValue(ComposterBlock.POWERED);
            if (shouldBePowered != currentlyPowered) {
                level.setBlock(pos, state.setValue(ComposterBlock.POWERED, shouldBePowered), 3);
            }
        }
    }

    private float getTotalCompostValue() {
        float total = 0f;
        for (int i = INPUT_SLOTS_START; i < INPUT_SLOTS_START + INPUT_SLOTS_COUNT; i++) {
            ItemStack stack = getStack(i);
            if (!stack.isEmpty()) {
                total += getCompostChance(stack) * stack.getCount();
            }
        }
        return total;
    }

    private boolean canProcess() {
        return getTotalCompostValue() >= COMPOST_TARGET && hasSpaceForBiomass();
    }

    private boolean hasSpaceForBiomass() {
        ItemStack biomass = new ItemStack(ATEItems.BIOMASS.get());
        for (int i = OUTPUT_SLOTS_START; i < OUTPUT_SLOTS_START + OUTPUT_SLOTS_COUNT; i++) {
            ItemStack output = getStack(i);
            if (output.isEmpty()) return true;
            if (ItemStack.isSameItemSameTags(output, biomass) && output.getCount() < output.getMaxStackSize()) return true;
        }
        return false;
    }

    private void processItems() {
        float remaining = COMPOST_TARGET;
        for (int i = INPUT_SLOTS_START; i < INPUT_SLOTS_START + INPUT_SLOTS_COUNT && remaining > 0f; i++) {
            ItemStack stack = getStack(i);
            if (stack.isEmpty()) continue;
            float chance = getCompostChance(stack);
            if (chance <= 0f) continue;
            int needed = (int) Math.ceil(remaining / chance);
            int taken = Math.min(needed, stack.getCount());
            inventory.extractItem(i, taken, false);
            remaining -= chance * taken;
        }
        addBiomassToOutput();
    }

    private void addBiomassToOutput() {
        ItemStack biomass = new ItemStack(ATEItems.BIOMASS.get());
        for (int i = OUTPUT_SLOTS_START; i < OUTPUT_SLOTS_START + OUTPUT_SLOTS_COUNT; i++) {
            ItemStack leftover = inventory.insertItem(i, biomass.copy(), false);
            if (leftover.getCount() < biomass.getCount()) return;
        }
    }

    /**
     * 1.20.1 has no {@code ComposterBlock.getValue(ItemStack)}; the compost chance table is the
     * {@link net.minecraft.world.level.block.ComposterBlock#COMPOSTABLES} map keyed by item.
     */
    private static float getCompostChance(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0f;
        return net.minecraft.world.level.block.ComposterBlock.COMPOSTABLES.getFloat(stack.getItem());
    }

    public boolean isCompostableItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return getCompostChance(stack) > 0f;
    }

    private boolean isSpeedModule(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String id = RegistryHelper.getItemId(stack);
        return id.equals(SM_MK1) || id.equals(SM_MK2) || id.equals(SM_MK3);
    }

    private double getModuleSpeedModifier() {
        ItemStack module = getStack(MODULE_SLOT);
        if (module.isEmpty()) return 1.0;
        return switch (RegistryHelper.getItemId(module)) {
            case SM_MK1 -> Config.getSpeedModuleMk1Multiplier();
            case SM_MK2 -> Config.getSpeedModuleMk2Multiplier();
            case SM_MK3 -> Config.getSpeedModuleMk3Multiplier();
            default -> 1.0;
        };
    }

    private double getModulePowerModifier() {
        ItemStack module = getStack(MODULE_SLOT);
        if (module.isEmpty()) return 1.0;
        return switch (RegistryHelper.getItemId(module)) {
            case SM_MK1 -> Config.getSpeedModuleMk1PowerMultiplier();
            case SM_MK2 -> Config.getSpeedModuleMk2PowerMultiplier();
            case SM_MK3 -> Config.getSpeedModuleMk3PowerMultiplier();
            default -> 1.0;
        };
    }

    public IItemHandler getItemHandler() {
        return itemHandler.resolve().orElse(null);
    }

    /**
     * Insert is limited to input and module slots, extraction to the output slots.
     */
    private static class ComposterHandler implements IItemHandler {
        private final ComposterBlockEntity be;

        ComposterHandler(ComposterBlockEntity be) {
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
            boolean isInput = slot >= INPUT_SLOTS_START && slot < INPUT_SLOTS_START + INPUT_SLOTS_COUNT;
            boolean isModule = slot == MODULE_SLOT;
            if (!isInput && !isModule) return stack;
            return be.inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            boolean isOutput = slot >= OUTPUT_SLOTS_START && slot < OUTPUT_SLOTS_START + OUTPUT_SLOTS_COUNT;
            if (!isOutput) return ItemStack.EMPTY;
            return be.inventory.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return be.inventory.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return be.inventory.isItemValid(slot, stack);
        }
    }

    // ------------------------------------------------------------------ energy

    @Override
    public int getEnergyStored() {
        return energyStored;
    }

    @Override
    public int getMaxEnergyStored() {
        return Config.getComposterEnergyBuffer();
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

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyCapability.cast();
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        energyCapability.invalidate();
    }

    // ------------------------------------------------------------------ sync

    public ItemStack getStack(int slot) {
        return inventory.getStackInSlot(slot);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
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

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        int baseTime = Config.getComposterBaseProcessingTime();
        if (energyStored >= Config.getComposterBasePowerConsumption()) baseTime /= 3;
        return (int) Math.max(1, baseTime / getModuleSpeedModifier());
    }

    public int getCompostValueCollected() { return (int) (getTotalCompostValue() * 100 / COMPOST_TARGET); }
    public int getCompostValueRequired() { return 100; }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", inventory.serializeNBT());
        tag.putInt("progress", progress);
        tag.putInt("energyStored", energyStored);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            inventory.deserializeNBT(tag.getCompound("Items"));
        }
        progress = tag.getInt("progress");
        energyStored = tag.getInt("energyStored");
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

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.community_agritechevolved.composter");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ComposterMenu(containerId, playerInventory, this);
    }
}
