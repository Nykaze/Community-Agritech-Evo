package com.misterd.agritechevolved.compat.jei;

import com.misterd.agritechevolved.recipe.DurabilityShapelessRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

public class FarmlandRecipe {

    private final Ingredient soilInput;
    private final Ingredient hoeInput;
    private final ItemStack output;

    public FarmlandRecipe(Ingredient soilInput, Ingredient hoeInput, ItemStack output) {
        this.soilInput = soilInput;
        this.hoeInput = hoeInput;
        this.output = output;
    }

    public Ingredient getSoilInput() { return soilInput; }
    public Ingredient getHoeInput() { return hoeInput; }
    public ItemStack getOutput() { return output; }

    public static @Nullable FarmlandRecipe fromShapeless(DurabilityShapelessRecipe recipe) {
        if (recipe.getRecipeIngredients().isEmpty()) return null;
        Ingredient soil = recipe.getRecipeIngredients().get(0);
        Ingredient hoe = recipe.getToolIngredient();
        ItemStack output = recipe.getResult();
        return new FarmlandRecipe(soil, hoe, output);
    }
}