package com.misterd.agritechevolved.recipe;

import com.misterd.agritechevolved.AgritechEvolved;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ATERecipeTypes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, AgritechEvolved.MODID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, AgritechEvolved.MODID);

    public static final RegistryObject<RecipeType<CropRecipe>> CROP_TYPE =
            RECIPE_TYPES.register("crop", () -> new RecipeType<>() {
                @Override
                public String toString() { return AgritechEvolved.MODID + ":crop"; }
            });

    public static final RegistryObject<RecipeType<TreeRecipe>> TREE_TYPE =
            RECIPE_TYPES.register("tree", () -> new RecipeType<>() {
                @Override
                public String toString() { return AgritechEvolved.MODID + ":tree"; }
            });

    public static final RegistryObject<RecipeSerializer<CropRecipe>> CROP_SERIALIZER =
            RECIPE_SERIALIZERS.register("crop", () -> new RecipeSerializer<>() {
                @Override
                public CropRecipe fromJson(ResourceLocation id, JsonObject json) {
                    CropRecipe recipe = ATECodecs.parse(CropRecipe.CODEC, json, id.toString());
                    recipe.setId(id);
                    return recipe;
                }

                @Override
                public CropRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
                    return CropRecipe.fromNetwork(id, buf);
                }

                @Override
                public void toNetwork(FriendlyByteBuf buf, CropRecipe recipe) {
                    recipe.toNetwork(buf);
                }
            });

    public static final RegistryObject<RecipeSerializer<TreeRecipe>> TREE_SERIALIZER =
            RECIPE_SERIALIZERS.register("tree", () -> new RecipeSerializer<>() {
                @Override
                public TreeRecipe fromJson(ResourceLocation id, JsonObject json) {
                    TreeRecipe recipe = ATECodecs.parse(TreeRecipe.CODEC, json, id.toString());
                    recipe.setId(id);
                    return recipe;
                }

                @Override
                public TreeRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
                    return TreeRecipe.fromNetwork(id, buf);
                }

                @Override
                public void toNetwork(FriendlyByteBuf buf, TreeRecipe recipe) {
                    recipe.toNetwork(buf);
                }
            });

    public static final RegistryObject<RecipeSerializer<DurabilityShapelessRecipe>> DURABILITY_SHAPELESS_SERIALIZER =
            RECIPE_SERIALIZERS.register("durability_shapeless", () -> DurabilityShapelessRecipeSerializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}

