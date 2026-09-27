package com.misterd.agritechevolved.recipe;

import com.google.gson.JsonObject;
import com.misterd.agritechevolved.AgritechEvolved;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.List;

public class DurabilityShapelessRecipeSerializer {

    /**
     * The recipe body without an id. A {@code RecordCodecBuilder} cannot know which id the
     * recipe was loaded from, so the body is decoded first and stamped afterwards via
     * {@link #codec(ResourceLocation)}. Decoding the body straight into a recipe would give every
     * tilling recipe the same hardcoded id and the recipe manager would drop all but the first.
     */
    private record Body(CraftingBookCategory category, ItemStack result,
                        List<Ingredient> ingredients, Ingredient tool, int durabilityPerItem) {
    }

    private static final ResourceLocation PLACEHOLDER_ID =
            new ResourceLocation(AgritechEvolved.MODID, "durability_shapeless");

    private static final Codec<Body> BODY_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(Body::category),
                    ItemStack.CODEC.fieldOf("result").forGetter(Body::result),
                    ATEIngredientCodecs.INGREDIENT.listOf().fieldOf("ingredients").forGetter(Body::ingredients),
                    ATEIngredientCodecs.INGREDIENT.fieldOf("tool").forGetter(Body::tool),
                    Codec.INT.fieldOf("durability_per_item").forGetter(Body::durabilityPerItem)
            ).apply(instance, Body::new)
    );

    private static Body toBody(DurabilityShapelessRecipe recipe) {
        return new Body(recipe.category(), recipe.getResult(), recipe.getRecipeIngredients(),
                recipe.getToolIngredient(), recipe.getDurabilityPerItem());
    }

    private static DurabilityShapelessRecipe toRecipe(ResourceLocation id, Body body) {
        NonNullList<Ingredient> list = NonNullList.create();
        list.addAll(body.ingredients());
        return new DurabilityShapelessRecipe(id, body.category(), body.result(), list,
                body.tool(), body.durabilityPerItem());
    }

    /**
     * Id-agnostic codec, only safe for encoding (datagen writes the id from the
     * {@code FinishedRecipe}). Never use it to deserialize a datapack recipe.
     */
    public static final Codec<DurabilityShapelessRecipe> CODEC =
            BODY_CODEC.xmap(body -> toRecipe(PLACEHOLDER_ID, body), DurabilityShapelessRecipeSerializer::toBody);

    /** Decode/encode codec that keeps the recipe's real id. */
    public static Codec<DurabilityShapelessRecipe> codec(ResourceLocation id) {
        return BODY_CODEC.xmap(body -> toRecipe(id, body), DurabilityShapelessRecipeSerializer::toBody);
    }

    public static final RecipeSerializer<DurabilityShapelessRecipe> INSTANCE = new RecipeSerializer<>() {
        @Override
        public DurabilityShapelessRecipe fromJson(ResourceLocation id, JsonObject json) {
            return ATECodecs.parse(codec(id), json, id.toString());
        }

        @Override
        public DurabilityShapelessRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);
            ItemStack result = buf.readItem();
            int count = buf.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.createWithCapacity(count);
            for (int i = 0; i < count; i++) {
                ingredients.add(Ingredient.fromNetwork(buf));
            }
            Ingredient tool = Ingredient.fromNetwork(buf);
            int durability = buf.readVarInt();
            return new DurabilityShapelessRecipe(id, category, result, ingredients, tool, durability);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, DurabilityShapelessRecipe recipe) {
            buf.writeEnum(recipe.category());
            buf.writeItem(recipe.getResult());
            buf.writeVarInt(recipe.getRecipeIngredients().size());
            for (Ingredient ing : recipe.getRecipeIngredients()) {
                ing.toNetwork(buf);
            }
            recipe.getToolIngredient().toNetwork(buf);
            buf.writeVarInt(recipe.getDurabilityPerItem());
        }
    };
}

