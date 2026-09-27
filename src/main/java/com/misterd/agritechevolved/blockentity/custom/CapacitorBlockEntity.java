package com.misterd.agritechevolved.blockentity.custom;

import com.misterd.agritechevolved.Config;
import com.misterd.agritechevolved.block.ATEBlocks;
import com.misterd.agritechevolved.block.custom.CapacitorTier1Block;
import com.misterd.agritechevolved.block.custom.CapacitorTier2Block;
import com.misterd.agritechevolved.block.custom.CapacitorTier3Block;
import com.misterd.agritechevolved.blockentity.ATEBlockEntities;
import com.misterd.agritechevolved.gui.custom.CapacitorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

/**
 * Forge 1.20.1 port. The NeoForge transfer API energy handler is replaced by a directly
 * implemented {@link IEnergyStorage} exposed through {@link #getCapability}.
 */
public class CapacitorBlockEntity extends BlockEntity implements MenuProvider, IEnergyStorage {

    private final LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> this);

    private int energyStored = 0;
    private int capacity = 0;
    private int tier = 1;
    private int transferRate = 512;

    public CapacitorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ATEBlockEntities.CAPACITOR_BE.get(), pos, blockState);
        initializeCapacitor(blockState);
    }

    private void initializeCapacitor(BlockState state) {
        if (state != null && state.is(ATEBlocks.CAPACITOR_TIER_2.get())) {
            tier = 2;
            transferRate = Config.getCapacitorT2TransferRate();
            capacity = Config.getCapacitorT2Buffer();
        } else if (state != null && state.is(ATEBlocks.CAPACITOR_TIER_3.get())) {
            tier = 3;
            transferRate = Config.getCapacitorT3TransferRate();
            capacity = Config.getCapacitorT3Buffer();
        } else {
            tier = 1;
            transferRate = Config.getCapacitorT1TransferRate();
            capacity = Config.getCapacitorT1Buffer();
        }

        energyStored = Math.min(energyStored, capacity);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CapacitorBlockEntity be) {
        if (level.isClientSide()) return;

        boolean changed = false;

        if (be.energyStored > 0) {
            for (Direction dir : Direction.values()) {
                if (be.energyStored <= 0) break;

                BlockPos neighborPos = pos.relative(dir);
                BlockEntity neighborBe = level.getBlockEntity(neighborPos);
                if (neighborBe == null) continue;

                IEnergyStorage neighbor = neighborBe
                        .getCapability(ForgeCapabilities.ENERGY, dir.getOpposite())
                        .resolve()
                        .orElse(null);
                if (neighbor == null) continue;

                int toTransfer = Math.min(be.transferRate, be.energyStored);
                if (toTransfer <= 0) continue;

                int transferred = neighbor.receiveEnergy(toTransfer, false);
                if (transferred > 0) {
                    be.energyStored -= transferred;
                    changed = true;
                }
            }
        }

        if (changed) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }

        boolean hasEnergy = be.energyStored > 0;
        BooleanProperty prop = null;
        if (state.is(ATEBlocks.CAPACITOR_TIER_1.get())) prop = CapacitorTier1Block.HAS_ENERGY;
        else if (state.is(ATEBlocks.CAPACITOR_TIER_2.get())) prop = CapacitorTier2Block.HAS_ENERGY;
        else if (state.is(ATEBlocks.CAPACITOR_TIER_3.get())) prop = CapacitorTier3Block.HAS_ENERGY;

        if (prop != null && state.getValue(prop) != hasEnergy) {
            level.setBlock(pos, state.setValue(prop, hasEnergy), 3);
        }
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = Math.min(Math.min(maxReceive, transferRate), capacity - energyStored);
        if (received <= 0) return 0;
        if (!simulate) {
            energyStored += received;
            setChanged();
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = Math.min(Math.min(maxExtract, transferRate), energyStored);
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
        return true;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyCapability.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCapability.invalidate();
    }

    @Override
    public int getEnergyStored() {
        return energyStored;
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }

    public int getTier() {
        return tier;
    }

    public void forceSetEnergy(int energy) {
        energyStored = Math.min(energy, capacity);
        setChanged();
    }

    public int getTransferRate() {
        return transferRate;
    }

    public String getTierName() {
        return switch (tier) {
            case 1  -> "Tier 1";
            case 2  -> "Tier 2";
            case 3  -> "Tier 3";
            default -> "Unknown";
        };
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("energyStored", energyStored);
        tag.putInt("tier", tier);
        tag.putInt("transferRate", transferRate);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tier = tag.contains("tier") && tag.getInt("tier") > 0 ? tag.getInt("tier") : 1;
        transferRate = tag.contains("transferRate") ? tag.getInt("transferRate") : 512;
        if (getBlockState() != null) initializeCapacitor(getBlockState());
        energyStored = Math.min(tag.getInt("energyStored"), capacity);
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
        return Component.translatable("gui.community_agritechevolved.capacitor_tier" + tier);
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CapacitorMenu(containerId, playerInventory, this);
    }
}
