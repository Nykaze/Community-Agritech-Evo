package com.misterd.agritechevolved.gui.custom;

import com.misterd.agritechevolved.block.ATEBlocks;
import com.misterd.agritechevolved.blockentity.custom.FertilizerSpreaderBlockEntity;
import com.misterd.agritechevolved.datamap.ATEDataMaps;
import com.misterd.agritechevolved.gui.ATEMenuTypes;
import com.misterd.agritechevolved.util.ATETags;
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

public class FertilizerSpreaderMenu extends AbstractContainerMenu {

    private static final int PLAYER_SLOTS = 36;
    private static final int STORAGE_SLOTS = 63;
    private static final int MODULE_SLOT = 63;
    private static final int TE_SLOT_COUNT = 64;
    private static final int TE_FIRST_SLOT = PLAYER_SLOTS;
    private static final int TE_LAST_SLOT = TE_FIRST_SLOT + TE_SLOT_COUNT;

    public final FertilizerSpreaderBlockEntity blockEntity;
    private final Level level;

    private int lastEnergyStored = 0;

    public FertilizerSpreaderMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public FertilizerSpreaderMenu(int containerId, Inventory inv, BlockEntity blockEntity) {
        super(ATEMenuTypes.FERTILIZER_SPREADER_MENU.get(), containerId);
        this.blockEntity = (FertilizerSpreaderBlockEntity) blockEntity;
        this.level = inv.player.level();

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        addBlockEntitySlots();
        addDataSlots();
    }

    private void addBlockEntitySlots() {
        int idx = 0;
        for (int row = 0; row < 7; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new SlotItemHandler(blockEntity.inventory, idx++, 8 + col * 18, 19 + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return isFertilizer(level, stack);
                    }
                });

        addSlot(new SlotItemHandler(blockEntity.inventory, MODULE_SLOT, 176, 19) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ATETags.Items.ATE_RANGE_MODULES);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
    }

    private void addDataSlots() {
        addDataSlot(new DataSlot() {
            @Override
            public int get() { return blockEntity.getEnergyStored(); }

            @Override
            public void set(int value) { lastEnergyStored = value; }
        });
    }

    public int getEnergyStored() {
        return level.isClientSide() ? lastEnergyStored : blockEntity.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return blockEntity.getMaxEnergyStored();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot source = slots.get(index);
        if (source == null || !source.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = source.getItem();
        ItemStack copy = stack.copy();

        if (index < PLAYER_SLOTS) {
            if (!moveToBlockEntity(stack)) return ItemStack.EMPTY;
        } else {
            if (index >= TE_LAST_SLOT) return ItemStack.EMPTY;
            if (!moveItemStackTo(stack, 0, PLAYER_SLOTS, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) source.set(ItemStack.EMPTY);
        else source.setChanged();

        source.onTake(player, stack);
        return copy;
    }

    private boolean moveToBlockEntity(ItemStack stack) {
        if (isFertilizer(level, stack))
            return moveItemStackTo(stack, TE_FIRST_SLOT, TE_FIRST_SLOT + STORAGE_SLOTS, false);

        if (stack.is(ATETags.Items.ATE_RANGE_MODULES) && blockEntity.getStack(MODULE_SLOT).isEmpty())
            return moveItemStackTo(stack, TE_FIRST_SLOT + MODULE_SLOT, TE_FIRST_SLOT + MODULE_SLOT + 1, false);

        return false;
    }

    private static boolean isFertilizer(Level level, ItemStack stack) {
        if (stack.isEmpty()) return false;
        return ATEDataMaps.getFertilizer(level, stack.getItem()) != null;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ATEBlocks.FERT_SPREADER.get());
    }

    private void addPlayerInventory(Inventory inv) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 160 + row * 18));
    }

    private void addPlayerHotbar(Inventory inv) {
        for (int i = 0; i < 9; i++)
            addSlot(new Slot(inv, i, 8 + i * 18, 218));
    }
}
