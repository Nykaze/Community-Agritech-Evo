package com.misterd.agritechevolved.blockentity.custom;

import com.misterd.agritechevolved.Config;
import com.misterd.agritechevolved.block.custom.SiloBlock;
import com.misterd.agritechevolved.blockentity.ATEBlockEntities;
import com.misterd.agritechevolved.gui.custom.SiloMenu;
import com.misterd.agritechevolved.util.ATETags;
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
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Forge 1.20.1 port. See {@link AdvancedPlanterBlockEntity} for the rationale behind the
 * substitution of the NeoForge transfer API with {@link ItemStackHandler} /
 * {@link IItemHandler} and a directly implemented {@link IEnergyStorage}.
 */
public class SiloBlockEntity extends BlockEntity implements MenuProvider, IEnergyStorage {

    private static final int STORAGE_SLOTS = 63;
    private static final int MODULE_SLOT = 63;
    private static final int TOTAL_SLOTS = 64;

    private static final String RM_MK1 = "community_agritechevolved:rm_mk1";
    private static final String RM_MK2 = "community_agritechevolved:rm_mk2";
    private static final String RM_MK3 = "community_agritechevolved:rm_mk3";

    private static final int MIN_RESCAN_INTERVAL_TICKS = 20;

    private int energyStored = 0;
    private int tickCounter = 0;
    private int scanAge = MIN_RESCAN_INTERVAL_TICKS;
    private List<BlockPos> cachedTargets = new ArrayList<>();

    public final ItemStackHandler inventory = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        public int getSlotLimit(int slot) {
            return slot == MODULE_SLOT ? 1 : super.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (stack.isEmpty()) return false;
            if (slot == MODULE_SLOT) return isRangeModule(stack);
            return slot < STORAGE_SLOTS;
        }

        @Override
        protected void onContentsChanged(int slot) {
            SiloBlockEntity.this.setChanged();
            Level lvl = SiloBlockEntity.this.level;
            if (lvl != null && !lvl.isClientSide()) {
                lvl.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    /**
     * The silo is output-only to automation: extractors may pull stored items, but nothing
     * may be pushed into it.
     */
    private final LazyOptional<IItemHandler> externalItemHandler =
            LazyOptional.of(() -> new ExternalExtractHandler(this));

    private final LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> this);

    public SiloBlockEntity(BlockPos pos, BlockState blockState) {
        super(ATEBlockEntities.SILO_BE.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SiloBlockEntity be) {
        if (level.isClientSide()) return;

        be.tickCounter++;
        if (be.tickCounter < Config.getSiloPullInterval()) return;
        be.tickCounter = 0;

        int required = Config.getSiloBasePowerConsumption();
        boolean hasPower = be.energyStored >= required;

        boolean changed = false;
        if (hasPower) {
            be.energyStored -= required;
            be.scanAge += Config.getSiloPullInterval();
            if (be.scanAge >= MIN_RESCAN_INTERVAL_TICKS) {
                be.rescanTargets(level, pos);
                be.scanAge = 0;
            }
            for (BlockPos targetPos : be.cachedTargets) {
                if (be.pullFromTarget(level, targetPos)) changed = true;
            }
        }

        if (changed) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }

        boolean shouldBePowered = hasPower;
        boolean currentlyPowered = state.getValue(SiloBlock.POWERED);
        if (shouldBePowered != currentlyPowered) {
            level.setBlock(pos, state.setValue(SiloBlock.POWERED, shouldBePowered), 3);
        }
    }

    private void rescanTargets(Level level, BlockPos pos) {
        int range = getRange();
        List<BlockPos> targets = new ArrayList<>();
        int chunkRadius = (range >> 4) + 1;
        int centerChunkX = pos.getX() >> 4;
        int centerChunkZ = pos.getZ() >> 4;

        for (int cx = -chunkRadius; cx <= chunkRadius; cx++) {
            for (int cz = -chunkRadius; cz <= chunkRadius; cz++) {
                int chunkX = centerChunkX + cx;
                int chunkZ = centerChunkZ + cz;
                if (!level.hasChunk(chunkX, chunkZ)) continue;

                LevelChunk chunk = level.getChunk(chunkX, chunkZ);
                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockPos bePos = entry.getKey();
                    if (Math.abs(bePos.getX() - pos.getX()) > range) continue;
                    if (Math.abs(bePos.getY() - pos.getY()) > range) continue;
                    if (Math.abs(bePos.getZ() - pos.getZ()) > range) continue;

                    BlockEntity be = entry.getValue();
                    if (be instanceof PlanterBlockEntity || be instanceof AdvancedPlanterBlockEntity) {
                        targets.add(bePos.immutable());
                    }
                }
            }
        }
        cachedTargets.clear();
        cachedTargets.addAll(targets);
    }

    /**
     * Pulls from a planter into the silo. Both sides are probed before anything is really moved,
     * so a failed extraction can never leave ghost items inside the silo.
     */
    private boolean pullFromTarget(Level level, BlockPos targetPos) {
        BlockEntity target = level.getBlockEntity(targetPos);
        IItemHandler source = getExtractHandlerFor(target);
        if (source == null) return false;

        boolean changed = false;
        for (int slot = 0; slot < source.getSlots(); slot++) {
            ItemStack stack = source.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            int available = stack.getCount();

            for (int storageSlot = 0; storageSlot < STORAGE_SLOTS && available > 0; storageSlot++) {
                ItemStack probe = stack.copyWithCount(available);
                ItemStack leftover = inventory.insertItem(storageSlot, probe, true);
                int accepted = available - leftover.getCount();
                if (accepted <= 0) continue;

                ItemStack notRemovable = source.extractItem(slot, accepted, true);
                int removable = accepted - notRemovable.getCount();
                if (removable <= 0) continue;

                ItemStack toStore = stack.copyWithCount(removable);
                ItemStack rejected = inventory.insertItem(storageSlot, toStore, false);
                int stored = removable - rejected.getCount();
                if (stored <= 0) continue;

                source.extractItem(slot, stored, false);
                available -= stored;
                changed = true;
            }
        }
        return changed;
    }

    @Nullable
    private IItemHandler getExtractHandlerFor(@Nullable BlockEntity be) {
        if (be instanceof AdvancedPlanterBlockEntity planter) return planter.getExtractHandler();
        if (be instanceof PlanterBlockEntity planter) return planter.getExtractHandler();
        return null;
    }

    private boolean isRangeModule(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ATETags.Items.ATE_RANGE_MODULES);
    }

    private int getRangeBonus() {
        ItemStack module = getStack(MODULE_SLOT);
        if (module.isEmpty()) return 0;
        return switch (RegistryHelper.getItemId(module)) {
            case RM_MK1 -> 16;
            case RM_MK2 -> 32;
            case RM_MK3 -> 64;
            default -> 0;
        };
    }

    public int getRange() {
        return Config.getSiloBaseRange() + getRangeBonus();
    }

    public IItemHandler getExternalItemHandler() {
        return externalItemHandler.resolve().orElse(null);
    }

    // ------------------------------------------------------------------ energy

    @Override
    public int getEnergyStored() {
        return energyStored;
    }

    @Override
    public int getMaxEnergyStored() {
        return Config.getSiloEnergyBuffer();
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

    private static class ExternalExtractHandler implements IItemHandler {
        private final SiloBlockEntity be;

        ExternalExtractHandler(SiloBlockEntity be) {
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
            if (slot >= STORAGE_SLOTS) return ItemStack.EMPTY;
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

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyCapability.cast();
        if (cap == ForgeCapabilities.ITEM_HANDLER) return externalItemHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCapability.invalidate();
        externalItemHandler.invalidate();
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

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", inventory.serializeNBT());
        tag.putInt("energyStored", energyStored);
        tag.putInt("tickCounter", tickCounter);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            inventory.deserializeNBT(tag.getCompound("Items"));
        }
        energyStored = tag.getInt("energyStored");
        tickCounter = tag.getInt("tickCounter");
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
        return Component.translatable("gui.community_agritechevolved.silo");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SiloMenu(containerId, playerInventory, this);
    }
}
