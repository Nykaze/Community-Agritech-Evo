package com.misterd.agritechevolved.blockentity.custom;

import com.misterd.agritechevolved.Config;
import com.misterd.agritechevolved.block.custom.BiomassBurnerBlock;
import com.misterd.agritechevolved.blockentity.ATEBlockEntities;
import com.misterd.agritechevolved.gui.custom.BiomassBurnerMenu;
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
public class BiomassBurnerBlockEntity extends BlockEntity implements MenuProvider, IEnergyStorage {

    private static final String BIOMASS = "community_agritechevolved:biomass";
    private static final String CRUDE_BIOMASS = "community_agritechevolved:crude_biomass";
    private static final String COMPACTED_BIOMASS = "community_agritechevolved:compacted_biomass";
    private static final String COMPACTED_BIOMASS_BLOCK = "community_agritechevolved:compacted_biomass_block";

    public final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isFuel(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            BiomassBurnerBlockEntity.this.setChanged();
            Level lvl = BiomassBurnerBlockEntity.this.level;
            if (lvl != null && !lvl.isClientSide()) {
                lvl.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new FuelHandler(this));
    private final LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> this);

    private int energyStored = 0;
    private int progress = 0;
    private int maxProgress = 0;
    private int currentBurnValue = 0;
    private boolean isBurning = false;

    public BiomassBurnerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ATEBlockEntities.BURNER_BE.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BiomassBurnerBlockEntity be) {
        if (level.isClientSide()) return;

        boolean changed = false;

        if (!be.isBurning && be.canStartBurning()) {
            be.startBurning();
            changed = true;
        }

        if (be.isBurning && be.energyStored < Config.getBurnerEnergyBuffer()) {
            be.progress++;
            if (be.currentBurnValue > 0) {
                int generated = Math.min(be.currentBurnValue, Config.getBurnerEnergyBuffer() - be.energyStored);
                be.energyStored += generated;
                if (generated > 0) changed = true;
            }
            if (be.progress >= be.maxProgress) {
                be.completeBurning();
                changed = true;
            }
        }

        if (be.distributeEnergy()) changed = true;

        if (changed) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }

        boolean shouldBurn = be.isBurning;
        boolean currentlyBurn = state.getValue(BiomassBurnerBlock.BURNING);
        if (shouldBurn != currentlyBurn) {
            level.setBlock(pos, state.setValue(BiomassBurnerBlock.BURNING, shouldBurn), 3);
        }
    }

    private boolean canStartBurning() {
        ItemStack fuel = getStack(0);
        return !fuel.isEmpty() && energyStored < Config.getBurnerEnergyBuffer() && isFuel(fuel);
    }

    private void startBurning() {
        ItemStack fuel = getStack(0);
        if (fuel.isEmpty()) return;

        String id = RegistryHelper.getItemId(fuel);
        int baseRF, burnDuration, baseDuration;

        switch (id) {
            case BIOMASS -> {
                baseRF = Config.getBurnerBiomassRfValue();
                burnDuration = Config.getBurnerBiomassBurnDuration();
                baseDuration = 100;
            }
            case COMPACTED_BIOMASS -> {
                baseRF = Config.getBurnerCompactedBiomassRfValue();
                burnDuration = Config.getBurnerCompactedBiomassBurnDuration();
                baseDuration = 180;
            }
            case COMPACTED_BIOMASS_BLOCK -> {
                baseRF = Config.getBurnerCompactedBiomassBlockRfValue();
                burnDuration = Config.getBurnerCompactedBiomassBlockBurnDuration();
                baseDuration = 180;
            }
            case CRUDE_BIOMASS -> {
                baseRF = Config.getBurnerCrudeBiomassRfValue();
                burnDuration = Config.getBurnerCrudeBiomassBurnDuration();
                baseDuration = 50;
            }
            default -> {
                return;
            }
        }

        int totalRF = (int) ((float) baseRF * ((float) burnDuration / baseDuration));
        if (totalRF <= 0 || burnDuration <= 0) return;

        maxProgress = burnDuration;
        currentBurnValue = totalRF / maxProgress;
        progress = 0;
        isBurning = true;

        inventory.extractItem(0, 1, false);
    }

    private void completeBurning() {
        progress = 0;
        maxProgress = 0;
        currentBurnValue = 0;
        isBurning = false;
    }

    private boolean distributeEnergy() {
        if (energyStored <= 0) return false;

        boolean distributed = false;
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = worldPosition.relative(dir);
            BlockEntity neighborBe = level.getBlockEntity(neighborPos);
            if (neighborBe == null) continue;

            IEnergyStorage neighbor = neighborBe
                    .getCapability(ForgeCapabilities.ENERGY, dir.getOpposite())
                    .resolve().orElse(null);
            if (neighbor == null) continue;

            int toTransfer = Math.min(1000, energyStored);
            if (toTransfer <= 0) continue;

            int transferred = neighbor.receiveEnergy(toTransfer, false);
            if (transferred > 0) {
                energyStored -= transferred;
                distributed = true;
            }
        }
        return distributed;
    }

    private static boolean isFuel(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String id = RegistryHelper.getItemId(stack);
        return id.equals(BIOMASS) || id.equals(CRUDE_BIOMASS)
                || id.equals(COMPACTED_BIOMASS) || id.equals(COMPACTED_BIOMASS_BLOCK);
    }

    public IItemHandler getItemHandler() {
        return itemHandler.resolve().orElse(null);
    }

    private static class FuelHandler implements IItemHandler {
        private final BiomassBurnerBlockEntity be;

        FuelHandler(BiomassBurnerBlockEntity be) {
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
            if (!isFuel(stack)) return stack;
            return be.inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return be.inventory.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return be.inventory.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isFuel(stack);
        }
    }

    // ------------------------------------------------------------------ energy

    /**
     * The burner only ever pushes energy out, so this is an extract-only buffer.
     */
    @Override
    public int getEnergyStored() {
        return energyStored;
    }

    @Override
    public int getMaxEnergyStored() {
        return Config.getBurnerEnergyBuffer();
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = Math.min(maxExtract, energyStored);
        if (extracted <= 0) return 0;
        if (!simulate) {
            energyStored -= extracted;
            setChanged();
        }
        return extracted;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return false;
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

    public int getMaxEnergy() {
        return Config.getBurnerEnergyBuffer();
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public boolean isBurning() {
        return isBurning;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", inventory.serializeNBT());
        tag.putInt("energyStored", energyStored);
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
        tag.putInt("currentBurnValue", currentBurnValue);
        tag.putBoolean("isBurning", isBurning);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            inventory.deserializeNBT(tag.getCompound("Items"));
        }
        energyStored = tag.getInt("energyStored");
        progress = tag.getInt("progress");
        maxProgress = tag.getInt("maxProgress");
        currentBurnValue = tag.getInt("currentBurnValue");
        isBurning = tag.getBoolean("isBurning");
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
        return Component.translatable("gui.community_agritechevolved.biomass_burner");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BiomassBurnerMenu(containerId, playerInventory, this);
    }
}
