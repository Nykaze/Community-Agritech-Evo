package com.misterd.agritechevolved.recipe;

import com.misterd.agritechevolved.AgritechEvolved;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class DurabilityShapelessRecipe extends CustomRecipe {
    private final ItemStack result;
    private final NonNullList<Ingredient> ingredients;
    private final Ingredient toolIngredient;
    private final int durabilityPerItem;
    private final CraftingBookCategory bookCategory;

    public DurabilityShapelessRecipe(CraftingBookCategory category, ItemStack result,
                                     NonNullList<Ingredient> ingredients, Ingredient toolIngredient,
                                     int durabilityPerItem) {
        this(new ResourceLocation(AgritechEvolved.MODID, "durability_shapeless"), category, result,
                ingredients, toolIngredient, durabilityPerItem);
    }

    public DurabilityShapelessRecipe(ResourceLocation id, CraftingBookCategory category, ItemStack result,
                                     NonNullList<Ingredient> ingredients, Ingredient toolIngredient,
                                     int durabilityPerItem) {
        super(id, category);
        this.bookCategory = category;
        this.result = result;
        this.ingredients = ingredients;
        this.toolIngredient = toolIngredient;
        this.durabilityPerItem = durabilityPerItem;
    }

    @Override
    public CraftingBookCategory category() {
        return bookCategory;
    }

    @Override
    public boolean matches(CraftingContainer input, Level level) {
        NonNullList<Ingredient> remaining = NonNullList.create();
        remaining.addAll(ingredients);
        boolean foundTool = false;
        int totalProcessableItems = 0;
        ItemStack foundToolStack = ItemStack.EMPTY;

        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (toolIngredient.test(stack)) {
                if (foundTool) return false;
                foundTool = true;
                foundToolStack = stack;
            } else {
                boolean matched = false;
                for (int j = 0; j < remaining.size(); j++) {
                    if (remaining.get(j).test(stack)) {
                        totalProcessableItems++;
                        remaining.remove(j);
                        matched = true;
                        break;
                    }
                }
                if (!matched) return false;
            }
        }

        if (!foundTool || !remaining.isEmpty()) return false;

        if (foundToolStack.isDamageableItem()) {
            int neededDurability = totalProcessableItems * durabilityPerItem;
            return foundToolStack.getDamageValue() + neededDurability < foundToolStack.getMaxDamage();
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registryAccess) {
        int processableItems = 0;
        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty() || toolIngredient.test(stack)) continue;
            for (Ingredient ingredient : ingredients) {
                if (ingredient.test(stack)) {
                    processableItems++;
                    break;
                }
            }
        }
        ItemStack out = result.copy();
        out.setCount(processableItems);
        return out;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.getContainerSize(), ItemStack.EMPTY);
        ItemStack toolStack = ItemStack.EMPTY;
        int toolSlot = -1;
        int processableItems = 0;

        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (toolIngredient.test(stack)) {
                toolStack = stack.copy();
                toolSlot = i;
            } else {
                for (Ingredient ingredient : ingredients) {
                    if (ingredient.test(stack)) {
                        processableItems++;
                        break;
                    }
                }
            }
        }

        if (!toolStack.isEmpty() && toolSlot != -1) {
            int totalDurability = processableItems * durabilityPerItem;
            if (toolStack.isDamageableItem()) {
                int newDamage = toolStack.getDamageValue() + totalDurability;
                if (newDamage < toolStack.getMaxDamage()) {
                    toolStack.setDamageValue(newDamage);
                    remaining.set(toolSlot, toolStack);
                }
            } else {
                remaining.set(toolSlot, toolStack);
            }
        }

        return remaining;
    }

    @Override
    public RecipeSerializer<? extends Recipe<?>> getSerializer() {
        return ATERecipeTypes.DURABILITY_SHAPELESS_SERIALIZER.get();
    }

    public ItemStack getResult() {
        return result.copy();
    }

    public NonNullList<Ingredient> getRecipeIngredients() {
        return ingredients;
    }

    public Ingredient getToolIngredient() {
        return toolIngredient;
    }

    public int getDurabilityPerItem() {
        return durabilityPerItem;
    }
}
