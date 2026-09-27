package com.misterd.agritechevolved.gui.custom;

import com.misterd.agritechevolved.block.custom.PlanterBlock;
import com.misterd.agritechevolved.blockentity.custom.PlanterBlockEntity;
import com.misterd.agritechevolved.datamap.ATEDataMaps;
import com.misterd.agritechevolved.gui.ATEMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class BasicPlanterMenu extends AbstractContainerMenu {

    public final PlanterBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    private static final int PLAYER_SLOTS = 36;
    private static final int TE_SLOT_PLANT = PLAYER_SLOTS;
    private static final int TE_SLOT_SOIL = PLAYER_SLOTS + 1;
    private static final int TE_SLOT_FERT = PLAYER_SLOTS + 2;
    private static final int TE_OUTPUT_START = PLAYER_SLOTS + 3;
    private static final int TE_OUTPUT_END = PLAYER_SLOTS + 15;

    public BasicPlanterMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public BasicPlanterMenu(int containerId, Inventory inv, BlockEntity blockEntity) {
        super(ATEMenuTypes.PLANTER_BLOCK_MENU.get(), containerId);
        this.blockEntity = (PlanterBlockEntity) blockEntity;
        this.level = inv.player.level();

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        addSlot(new SlotItemHandler(this.blockEntity.inventory, 0, 8, 18));
        addSlot(new SlotItemHandler(this.blockEntity.inventory, 1, 8, 54));
        addSlot(new SlotItemHandler(this.blockEntity.inventory, 2, 152, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isFertilizer(level, stack);
            }
        });

        int slotIndex = 3;
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 4; col++)
                addSlot(new OutputSlot(this.blockEntity, slotIndex++, 62 + col * 18, 18 + row * 18));

        this.data = new ContainerData() {
            @Override public int get(int index) { return index == 0 ? BasicPlanterMenu.this.blockEntity.growthProgress : 0; }
            @Override public void set(int index, int value) { if (index == 0) BasicPlanterMenu.this.blockEntity.growthProgress = value; }
            @Override public int getCount() { return 1; }
        };
        addDataSlots(this.data);
    }

    public int getGrowthProgress() {
        return data.get(0);
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
            if (!moveItemStackTo(stack, 0, PLAYER_SLOTS, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) source.set(ItemStack.EMPTY);
        else source.setChanged();

        source.onTake(player, stack);
        return copy;
    }

    private boolean moveToBlockEntity(ItemStack stack) {
        if (blockEntity.isValidPlant(stack)) {
            if (!blockEntity.getStack(0).isEmpty()) return false;
            ItemStack soil = blockEntity.getStack(1);
            if (!soil.isEmpty() && !blockEntity.isValidPlantSoilCombination(stack, soil)) return false;
            return moveItemStackTo(stack, TE_SLOT_PLANT, TE_SLOT_PLANT + 1, false);
        }

        if (blockEntity.isValidSoilForAnyRecipe(stack)) {
            if (!blockEntity.getStack(1).isEmpty()) return false;
            ItemStack plant = blockEntity.getStack(0);
            if (!plant.isEmpty() && !blockEntity.isValidPlantSoilCombination(plant, stack)) return false;
            return moveItemStackTo(stack, TE_SLOT_SOIL, TE_SLOT_SOIL + 1, false);
        }

        if (isFertilizer(level, stack)) {
            return moveItemStackTo(stack, TE_SLOT_FERT, TE_SLOT_FERT + 1, false);
        }

        return false;
    }

    private static boolean isFertilizer(Level level, ItemStack stack) {
        if (stack.isEmpty()) return false;
        return ATEDataMaps.getFertilizer(level, stack.getItem()) != null;
    }

    @Override
    public boolean stillValid(Player player) {
        Block block = blockEntity.getBlockState().getBlock();
        return block instanceof PlanterBlock
                && stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, block);
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

    private static class OutputSlot extends SlotItemHandler {
        OutputSlot(PlanterBlockEntity be, int index, int x, int y) {
            super(be.inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
