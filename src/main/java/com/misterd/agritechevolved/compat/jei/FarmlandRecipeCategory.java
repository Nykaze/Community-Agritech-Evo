package com.misterd.agritechevolved.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FarmlandRecipeCategory implements IRecipeCategory<FarmlandRecipe> {

    public static final ResourceLocation UID = new ResourceLocation("community_agritechevolved", "farmland_tilling");
    public static final ResourceLocation TEXTURE = new ResourceLocation("community_agritechevolved", "textures/gui/jei/jei_hoe_farmland_gui.png");
    public static final RecipeType<FarmlandRecipe> FARMLAND_RECIPE_TYPE = new RecipeType<>(UID, FarmlandRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public FarmlandRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 108, 36);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.FARMLAND));
    }

    @Override
    public RecipeType<FarmlandRecipe> getRecipeType() {
        return FARMLAND_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.community_agritechevolved.farmland_tilling.title");
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
    public void setRecipe(IRecipeLayoutBuilder builder, FarmlandRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 10)
                .addIngredients(recipe.getSoilInput());

        builder.addSlot(RecipeIngredientRole.INPUT, 46, 10)
                .addIngredients(recipe.getHoeInput());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 82, 10)
                .addItemStack(recipe.getOutput());
    }
}
