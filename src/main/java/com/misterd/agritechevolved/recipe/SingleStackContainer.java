package com.misterd.agritechevolved.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Minecraft 1.20.1's {@code RecipeManager#getRecipeFor} takes a {@link Container} rather than
 * a {@code RecipeInput}, so single-slot lookups use this adapter.
 */
public class SingleStackContainer implements Container {
    private final ItemStack stack;

    public SingleStackContainer(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? stack : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot != 0 || stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack copy = stack.copy();
        copy.shrink(Math.min(amount, stack.getCount()));
        return copy;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return slot == 0 ? stack : ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
    }

    public Level level() {
        return null;
    }
}
