package com.misterd.agritechevolved.compat.jei;

import com.misterd.agritechevolved.block.ATEBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CompostRecipeCategory implements IRecipeCategory<CompostRecipe> {

    public static final ResourceLocation UID = new ResourceLocation("community_agritechevolved", "composting");
    public static final ResourceLocation TEXTURE = new ResourceLocation("community_agritechevolved", "textures/gui/jei/jei_composter_gui.png");
    public static final RecipeType<CompostRecipe> COMPOST_RECIPE_TYPE = new RecipeType<>(UID, CompostRecipe.class);

    private static final float COMPOST_TARGET = 7.0f;

    private final IDrawable background;
    private final IDrawable icon;

    public CompostRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 134, 72);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ATEBlocks.COMPOSTER.get()));
    }

    @Override
    public RecipeType<CompostRecipe> getRecipeType() {
        return COMPOST_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.community_agritechevolved.composting.tooltip");
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
    public void setRecipe(IRecipeLayoutBuilder builder, CompostRecipe recipe, IFocusGroup focuses) {
        int itemsRequired = Math.max(1, (int) Math.ceil(COMPOST_TARGET / recipe.getChance()));

        IRecipeSlotBuilder inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 10, 10);
        for (ItemStack stack : recipe.getInput().getItems()) {
            inputSlot.addItemStack(stack.copyWithCount(itemsRequired));
        }

        List<ItemStack> outputs = recipe.getOutputs();
        for (int i = 0; i < outputs.size(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 106, 10 + i * 18).addItemStack(outputs.get(i));
        }
    }
}
