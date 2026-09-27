package com.misterd.agritechevolved.compat.jei;

import com.misterd.agritechevolved.block.ATEBlocks;
import com.misterd.agritechevolved.recipe.DropEntry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public class PlanterRecipeCategory implements IRecipeCategory<PlanterRecipe> {

    public static final ResourceLocation UID = new ResourceLocation("community_agritechevolved", "planter");
    public static final ResourceLocation TEXTURE = new ResourceLocation("community_agritechevolved", "textures/gui/jei/jei_planters_gui.png");
    public static final RecipeType<PlanterRecipe> PLANTER_RECIPE_TYPE = new RecipeType<>(UID, PlanterRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public PlanterRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 134, 72);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ATEBlocks.OAK_PLANTER.get()));
    }

    @Override
    public RecipeType<PlanterRecipe> getRecipeType() {
        return PLANTER_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.community_agritechevolved.planter.tooltip");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PlanterRecipe recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder plantSlot = builder.addSlot(RecipeIngredientRole.INPUT, 10, 10);
        plantSlot.addIngredients(recipe.getPlant());

        IRecipeSlotBuilder soilSlot = builder.addSlot(RecipeIngredientRole.INPUT, 10, 46);
        for (Ingredient soil : recipe.getSoils()) {
            soilSlot.addIngredients(soil);
        }

        List<DropEntry> drops = recipe.getDrops();
        int outputIndex = 0;
        for (ItemStack output : recipe.getOutputs()) {
            if (outputIndex >= 12) break;
            int x = 52 + outputIndex % 4 * 18;
            int y = 10 + outputIndex / 4 * 18;

            DropEntry entry = outputIndex < drops.size() ? drops.get(outputIndex) : null;
            IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT, x, y);
            slot.addItemStack(output);

            if (entry != null) {
                slot.addTooltipCallback((slotView, tooltip) -> {
                    String countStr = entry.min() == entry.max()
                            ? String.valueOf(entry.min())
                            : entry.min() + "-" + entry.max();
                    tooltip.add(Component.literal("Count: " + countStr).withStyle(ChatFormatting.GRAY));
                    if (entry.chance() < 1.0F) {
                        tooltip.add(Component.literal(String.format("%.0f%% chance", entry.chance() * 100)).withStyle(ChatFormatting.GOLD));
                    }
                });
            }

            outputIndex++;
        }
    }
}
