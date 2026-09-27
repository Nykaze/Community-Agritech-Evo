package com.misterd.agritechevolved.gui.custom;

import com.misterd.agritechevolved.block.ATEBlocks;
import com.misterd.agritechevolved.blockentity.custom.BiomassBurnerBlockEntity;
import com.misterd.agritechevolved.gui.ATEMenuTypes;
import com.misterd.agritechevolved.util.RegistryHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class BiomassBurnerMenu extends AbstractContainerMenu {

    private static final int PLAYER_SLOTS = 36;
    private static final int TE_FUEL_SLOT = PLAYER_SLOTS;
    private static final int TE_LAST_SLOT = TE_FUEL_SLOT + 1;

    private static final String BIOMASS = "community_agritechevolved:biomass";
    private static final String CRUDE_BIOMASS = "community_agritechevolved:crude_biomass";
    private static final String COMPACTED_BIOMASS = "community_agritechevolved:compacted_biomass";
    private static final String COMPACTED_BIOMASS_BLOCK = "community_agritechevolved:compacted_biomass_block";

    public final BiomassBurnerBlockEntity blockEntity;
    private final Level level;

    private int lastProgress = 0;
    private int lastMaxProgress = 0;
    private int lastEnergy = 0;

    public BiomassBurnerMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public BiomassBurnerMenu(int containerId, Inventory inv, BlockEntity blockEntity) {
        super(ATEMenuTypes.BURNER_MENU.get(), containerId);
        this.blockEntity = (BiomassBurnerBlockEntity) blockEntity;
        this.level = inv.player.level();

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        addSlot(new SlotItemHandler(this.blockEntity.inventory, 0, 80, 32) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isFuel(stack);
            }
        });
        addDataSlots();
    }

    private void addDataSlots() {
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getEnergyStored();
            }

            @Override
            public void set(int value) {
                lastEnergy = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getProgress();
            }

            @Override
            public void set(int value) {
                lastProgress = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getMaxProgress();
            }

            @Override
            public void set(int value) {
                lastMaxProgress = value;
            }
        });
    }

    public int getEnergyStored() {
        return level.isClientSide() ? lastEnergy : blockEntity.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return blockEntity.getMaxEnergy();
    }

    public int getProgress() {
        return level.isClientSide() ? lastProgress : blockEntity.getProgress();
    }

    public int getMaxProgress() {
        return level.isClientSide() ? lastMaxProgress : blockEntity.getMaxProgress();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot source = slots.get(index);
        if (source == null || !source.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = source.getItem();
        ItemStack copy = stack.copy();

        if (index < PLAYER_SLOTS) {
            if (!isFuel(stack)) return ItemStack.EMPTY;
            if (!moveItemStackTo(stack, TE_FUEL_SLOT, TE_LAST_SLOT, false)) return ItemStack.EMPTY;
        } else {
            if (index >= TE_LAST_SLOT) return ItemStack.EMPTY;
            if (!moveItemStackTo(stack, 0, PLAYER_SLOTS, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) source.set(ItemStack.EMPTY);
        else source.setChanged();

        source.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ATEBlocks.BIOMASS_BURNER.get());
    }

    private void addPlayerInventory(Inventory inv) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 81 + row * 18));
    }

    private void addPlayerHotbar(Inventory inv) {
        for (int i = 0; i < 9; i++)
            addSlot(new Slot(inv, i, 8 + i * 18, 139));
    }

    private static boolean isFuel(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String id = RegistryHelper.getItemId(stack);
        return id.equals(BIOMASS) || id.equals(CRUDE_BIOMASS)
                || id.equals(COMPACTED_BIOMASS) || id.equals(COMPACTED_BIOMASS_BLOCK);
    }
}
