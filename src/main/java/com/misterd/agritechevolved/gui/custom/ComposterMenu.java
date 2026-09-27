package com.misterd.agritechevolved.gui.custom;

import com.misterd.agritechevolved.block.ATEBlocks;
import com.misterd.agritechevolved.blockentity.custom.ComposterBlockEntity;
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

public class ComposterMenu extends AbstractContainerMenu {

    private static final int INPUT_SLOTS_START = 0;
    private static final int INPUT_SLOTS_COUNT = 12;
    private static final int OUTPUT_SLOTS_START = 12;
    private static final int OUTPUT_SLOTS_COUNT = 3;
    private static final int MODULE_SLOT = 15;

    private static final int PLAYER_SLOTS = 36;
    private static final int TE_INPUT_START = PLAYER_SLOTS;
    private static final int TE_INPUT_END = TE_INPUT_START + INPUT_SLOTS_COUNT;
    private static final int TE_OUTPUT_START = TE_INPUT_END;
    private static final int TE_OUTPUT_END = TE_OUTPUT_START + OUTPUT_SLOTS_COUNT;
    private static final int TE_MODULE_SLOT = TE_OUTPUT_END;
    private static final int TE_LAST_SLOT = TE_MODULE_SLOT + 1;

    private static final String SM_MK1 = "community_agritechevolved:sm_mk1";
    private static final String SM_MK2 = "community_agritechevolved:sm_mk2";
    private static final String SM_MK3 = "community_agritechevolved:sm_mk3";

    public final ComposterBlockEntity blockEntity;
    private final Level level;

    private int lastEnergyStored = 0;
    private int lastProgress = 0;

    public ComposterMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public ComposterMenu(int containerId, Inventory inv, BlockEntity blockEntity) {
        super(ATEMenuTypes.COMPOSTER_MENU.get(), containerId);
        this.blockEntity = (ComposterBlockEntity) blockEntity;
        this.level = inv.player.level();

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        addBlockEntitySlots();
        addDataSlots();
    }

    private void addBlockEntitySlots() {
        int idx = INPUT_SLOTS_START;
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 4; col++)
                addSlot(new SlotItemHandler(blockEntity.inventory, idx++, 8 + col * 18, 19 + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return blockEntity.isCompostableItem(stack);
                    }
                });

        for (int i = 0; i < OUTPUT_SLOTS_COUNT; i++)
            addSlot(new SlotItemHandler(blockEntity.inventory, OUTPUT_SLOTS_START + i, 98, 19 + i * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });

        addSlot(new SlotItemHandler(blockEntity.inventory, MODULE_SLOT, 134, 19) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isSpeedModule(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
    }

    private void addDataSlots() {
        addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.getEnergyStored(); }
            @Override public void set(int value) { lastEnergyStored = value; }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.getProgress(); }
            @Override public void set(int value) { lastProgress = value; }
        });
    }

    public int getEnergyStored() { return level.isClientSide() ? lastEnergyStored : blockEntity.getEnergyStored(); }
    public int getMaxEnergyStored() { return blockEntity.getMaxEnergyStored(); }
    public int getProgress() { return level.isClientSide() ? lastProgress : blockEntity.getProgress(); }
    public int getMaxProgress() { return blockEntity.getMaxProgress(); }
    public int getOrganicItemsCollected() { return blockEntity.getCompostValueCollected(); }
    public int getRequiredOrganicItems() { return blockEntity.getCompostValueRequired(); }

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
        if (isSpeedModule(stack))
            return moveItemStackTo(stack, TE_MODULE_SLOT, TE_LAST_SLOT, false);

        if (blockEntity.isCompostableItem(stack))
            return moveItemStackTo(stack, TE_INPUT_START, TE_INPUT_END, false);

        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ATEBlocks.COMPOSTER.get());
    }

    private void addPlayerInventory(Inventory inv) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 88 + row * 18));
    }

    private void addPlayerHotbar(Inventory inv) {
        for (int i = 0; i < 9; i++)
            addSlot(new Slot(inv, i, 8 + i * 18, 146));
    }

    private static boolean isSpeedModule(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String id = RegistryHelper.getItemId(stack);
        return id.equals(SM_MK1) || id.equals(SM_MK2) || id.equals(SM_MK3);
    }
}
