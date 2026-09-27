package com.misterd.agritechevolved.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Minecraft 1.20.1 has no {@code Ingredient.CODEC} (it arrived in 1.20.2), so this rebuilds
 * an equivalent {@link Codec} for ingredients.
 *
 * <p>Reading accepts all three vanilla JSON forms: a single item id, a list of item ids, and
 * the object form {@code {"tag": "..."}}. Writing normalises to the item-id list form, which
 * is what the bundled datagen emits.
 */
public final class ATEIngredientCodecs {

    private static final Codec<TagKey<Item>> TAG = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("tag").forGetter(TagKey::location)
    ).apply(instance, location -> TagKey.create(Registries.ITEM, location)));

    public static final Codec<Ingredient> INGREDIENT = Codec.either(
            Codec.either(Codec.STRING, Codec.STRING.listOf()),
            TAG
    ).xmap(ATEIngredientCodecs::toIngredient, ATEIngredientCodecs::fromIngredient);

    private static Ingredient toIngredient(Either<Either<String, List<String>>, TagKey<Item>> encoded) {
        return encoded.map(
                idOrList -> idOrList.map(
                        ATEIngredientCodecs::ingredientOf,
                        ids -> ingredientOf(ids)
                ),
                tag -> Ingredient.of(tag)
        );
    }

    /**
     * Accepts a single entry or a list, where each entry is either a plain item id
     * ({@code "minecraft:dirt"}) or a tag reference ({@code "#community_agritechevolved:stone_soils"}).
     * Vanilla 1.20.1 has no single codec covering both, so tag entries are resolved here.
     */
    private static Ingredient ingredientOf(String id) {
        if (isTagReference(id)) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.tryParse(id.substring(1)));
            return Ingredient.of(tag);
        }
        return Ingredient.of(stackOf(id));
    }

    private static Ingredient ingredientOf(List<String> ids) {
        List<ItemStack> stacks = new ArrayList<>();
        for (String id : ids) {
            if (isTagReference(id)) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.tryParse(id.substring(1)));
                stacks.addAll(List.of(Ingredient.of(tag).getItems()));
            } else {
                stacks.add(stackOf(id));
            }
        }
        if (stacks.isEmpty()) {
            return Ingredient.EMPTY;
        }
        return Ingredient.of(stacks.toArray(ItemStack[]::new));
    }

    private static boolean isTagReference(String id) {
        return id.startsWith("#");
    }

    private static Either<Either<String, List<String>>, TagKey<Item>> fromIngredient(Ingredient ingredient) {
        List<String> ids = new ArrayList<>();
        for (ItemStack stack : ingredient.getItems()) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (id != null) {
                ids.add(id.toString());
            }
        }
        Either<String, List<String>> listForm = Either.right(ids);
        return Either.left(listForm);
    }

    private static ItemStack stackOf(String id) {
        ResourceLocation parsed = new ResourceLocation(id);
        return new ItemStack(BuiltInRegistries.ITEM.getOptional(parsed).orElse(Items.AIR));
    }
    private ATEIngredientCodecs() {
    }
}
