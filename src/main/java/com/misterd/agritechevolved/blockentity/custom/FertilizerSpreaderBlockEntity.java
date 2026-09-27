package com.misterd.agritechevolved.blockentity.custom;

import com.misterd.agritechevolved.Config;
import com.misterd.agritechevolved.block.custom.FertilizerSpreaderBlock;
import com.misterd.agritechevolved.blockentity.ATEBlockEntities;
import com.misterd.agritechevolved.datamap.ATEDataMaps;
import com.misterd.agritechevolved.gui.custom.FertilizerSpreaderMenu;
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
 * Forge 1.20.1 port. The spreader exposes an insert-only item capability and an
 * insert-only energy buffer, both backed by the block entity itself.
 */
public class FertilizerSpreaderBlockEntity extends BlockEntity implements MenuProvider, IEnergyStorage {

    private static final int STORAGE_SLOTS = 63;
    private static final int MODULE_SLOT = 63;
    private static final int TOTAL_SLOTS = 64;

    private static final String RM_MK1 = "community_agritechevolved:rm_mk1";
    private static final String RM_MK2 = "community_agritechevolved:rm_mk2";
    private static final String RM_MK3 = "community_agritechevolved:rm_mk3";

    private static final int MIN_RESCAN_INTERVAL_TICKS = 20;

    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new InsertOnlyHandler(this));
    private final LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> this);

    private int energyStored = 0;
    private int tickCounter = 0;
    private int roundRobinIndex = 0;
    private int scanAge = MIN_RESCAN_INTERVAL_TICKS;
    private final List<BlockPos> cachedTargets = new ArrayList<>();

    public final ItemStackHandler inventory = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        public int getSlotLimit(int slot) {
            if (slot == MODULE_SLOT) return 1;
            ItemStack current = getStackInSlot(slot);
            return current.isEmpty() ? 64 : current.getMaxStackSize();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (stack.isEmpty()) return false;
            if (slot == MODULE_SLOT) return isRangeModule(stack);
            return slot < STORAGE_SLOTS && isFertilizer(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            FertilizerSpreaderBlockEntity.this.setChanged();
            Level lvl = FertilizerSpreaderBlockEntity.this.level;
            if (lvl != null && !lvl.isClientSide()) {
                lvl.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public FertilizerSpreaderBlockEntity(BlockPos pos, BlockState blockState) {
        super(ATEBlockEntities.FERTILIZER_SPREADER_BE.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FertilizerSpreaderBlockEntity be) {
        if (level.isClientSide()) return;

        be.tickCounter++;
        if (be.tickCounter < Config.getFertilizerSpreaderPushInterval()) return;
        be.tickCounter = 0;

        int required = Config.getFertilizerSpreaderBasePowerConsumption();
        boolean hasPower = be.energyStored >= required;

        boolean changed = false;
        if (hasPower) {
            be.energyStored -= required;
            be.scanAge += Config.getFertilizerSpreaderPushInterval();
            if (be.scanAge >= MIN_RESCAN_INTERVAL_TICKS) {
                be.rescanTargets(level, pos);
                be.scanAge = 0;
            }
            changed = be.distributeFertilizer(level);
        }

        if (changed) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }

        boolean shouldBePowered = hasPower;
        boolean currentlyPowered = state.getValue(FertilizerSpreaderBlock.POWERED);
        if (shouldBePowered != currentlyPowered) {
            level.setBlock(pos, state.setValue(FertilizerSpreaderBlock.POWERED, shouldBePowered), 3);
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
        if (!cachedTargets.isEmpty()) {
            roundRobinIndex %= cachedTargets.size();
        } else {
            roundRobinIndex = 0;
        }
    }

    private boolean distributeFertilizer(Level level) {
        if (cachedTargets.isEmpty()) return false;

        int amountToPush = Config.getFertilizerSpreaderPushAmount();
        int count = cachedTargets.size();
        boolean changed = false;

        for (int i = 0; i < count; i++) {
            int idx = (roundRobinIndex + i) % count;
            BlockPos targetPos = cachedTargets.get(idx);
            if (pushToTarget(level, targetPos, amountToPush)) changed = true;
        }
        roundRobinIndex = (roundRobinIndex + 1) % count;
        return changed;
    }

    /**
     * Moves fertilizer into a planter while keeping both inventories consistent: the amount is
     * first probed on both sides, then only the accepted amount is really moved.
     */
    private boolean pushToTarget(Level level, BlockPos targetPos, int amountToPush) {
        BlockEntity target = level.getBlockEntity(targetPos);
        IItemHandler sink = getInsertHandlerFor(target);
        if (sink == null) return false;

        for (int storageSlot = 0; storageSlot < STORAGE_SLOTS; storageSlot++) {
            ItemStack source = inventory.getStackInSlot(storageSlot);
            if (source.isEmpty()) continue;

            int available = Math.min(amountToPush, source.getCount());
            if (available <= 0) continue;

            for (int targetSlot = 0; targetSlot < sink.getSlots(); targetSlot++) {
                if (!sink.isItemValid(targetSlot, source)) continue;

                ItemStack probe = source.copyWithCount(available);
                ItemStack leftover = sink.insertItem(targetSlot, probe, true);
                int accepted = available - leftover.getCount();
                if (accepted <= 0) continue;

                ItemStack notRemovable = inventory.extractItem(storageSlot, accepted, true);
                int removable = accepted - notRemovable.getCount();
                if (removable <= 0) continue;

                int moved = Math.min(removable, accepted);
                ItemStack toInsert = source.copyWithCount(moved);
                ItemStack rejected = sink.insertItem(targetSlot, toInsert, false);
                int actuallyInserted = moved - rejected.getCount();
                if (actuallyInserted <= 0) continue;

                inventory.extractItem(storageSlot, actuallyInserted, false);
                return true;
            }
        }
        return false;
    }

    @Nullable
    private IItemHandler getInsertHandlerFor(@Nullable BlockEntity be) {
        if (be instanceof AdvancedPlanterBlockEntity planter) return planter.getInsertHandler();
        if (be instanceof PlanterBlockEntity planter) return planter.getInsertHandler();
        return null;
    }

    private boolean isFertilizer(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Level lvl = this.level;
        return lvl != null
                ? ATEDataMaps.getFertilizer(lvl, stack.getItem()) != null
                : ATEDataMaps.getFertilizer(stack.getItem()) != null;
    }

    private boolean isRangeModule(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(ATETags.Items.ATE_RANGE_MODULES);
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
        return Config.getFertilizerSpreaderBaseRange() + getRangeBonus();
    }

    public IItemHandler getItemHandler() {
        return itemHandler.resolve().orElse(null);
    }

    /**
     * The spreader only accepts fertilizer, it never gives items back out.
     */
    private static class InsertOnlyHandler implements IItemHandler {
        private final FertilizerSpreaderBlockEntity be;

        InsertOnlyHandler(FertilizerSpreaderBlockEntity be) {
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
            return be.inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return stackOf(slot, amount);
        }

        private ItemStack stackOf(int slot, int amount) {
            ItemStack current = be.inventory.getStackInSlot(slot);
            if (current.isEmpty() || amount <= 0) return ItemStack.EMPTY;
            return current.copyWithCount(amount);
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
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = Math.min(maxReceive, Config.getFertilizerSpreaderEnergyBuffer() - energyStored);
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

    @Override
    public int getEnergyStored() {
        return energyStored;
    }

    @Override
    public int getMaxEnergyStored() {
        return Config.getFertilizerSpreaderEnergyBuffer();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", inventory.serializeNBT());
        tag.putInt("energyStored", energyStored);
        tag.putInt("tickCounter", tickCounter);
        tag.putInt("roundRobinIndex", roundRobinIndex);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            inventory.deserializeNBT(tag.getCompound("Items"));
        }
        energyStored = tag.getInt("energyStored");
        tickCounter = tag.getInt("tickCounter");
        roundRobinIndex = tag.getInt("roundRobinIndex");
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
        return Component.translatable("gui.community_agritechevolved.fertilizer_spreader");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new FertilizerSpreaderMenu(containerId, playerInventory, this);
    }
}
