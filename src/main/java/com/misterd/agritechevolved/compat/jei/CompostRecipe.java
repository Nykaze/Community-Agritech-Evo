package com.misterd.agritechevolved.compat.jei;

import com.misterd.agritechevolved.item.ATEItems;
import com.misterd.agritechevolved.util.RegistryHelper;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public class CompostRecipe {

    private final Ingredient input;
    private final List<ItemStack> outputs;
    private final float chance;

    public CompostRecipe(Ingredient input, List<ItemStack> outputs, float chance) {
        this.input = input;
        this.outputs = outputs;
        this.chance = chance;
    }

    public Ingredient getInput() { return input; }
    public List<ItemStack> getOutputs() { return outputs; }
    public float getChance() { return chance; }

    public static CompostRecipe create(String itemId, float chance) {
        var item = RegistryHelper.getItem(itemId);
        if (item == null) {
            LogUtils.getLogger().error("Failed to create compost recipe: item not found for ID: {}", itemId);
            throw new IllegalArgumentException("Item not found for ID: " + itemId);
        }
        return new CompostRecipe(
                Ingredient.of(item),
                List.of(new ItemStack(ATEItems.BIOMASS.get(), 1)),
                chance
        );
    }
}