package com.misterd.agritechevolved.datagen.custom;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.misterd.agritechevolved.AgritechEvolved;
import com.misterd.agritechevolved.block.ATEBlocks;
import com.misterd.agritechevolved.item.ATEItems;
import com.misterd.agritechevolved.recipe.DurabilityShapelessRecipe;
import com.misterd.agritechevolved.recipe.DurabilityShapelessRecipeSerializer;
import com.misterd.agritechevolved.util.ATETags;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.NonNullList;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ATERecipeProvider extends RecipeProvider {

    private static final String MODID = AgritechEvolved.MODID;

    public ATERecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> output) {
        woodPlanter(output, ATEBlocks.ACACIA_PLANTER.get(), Items.ACACIA_PLANKS, Items.ACACIA_LOG, "has_acacia_log");
        woodPlanter(output, ATEBlocks.BAMBOO_PLANTER.get(), Items.BAMBOO_PLANKS, Items.BAMBOO_BLOCK, "has_bamboo_block");
        woodPlanter(output, ATEBlocks.BIRCH_PLANTER.get(), Items.BIRCH_PLANKS, Items.BIRCH_LOG, "has_birch_log");
        woodPlanter(output, ATEBlocks.CHERRY_PLANTER.get(), Items.CHERRY_PLANKS, Items.CHERRY_LOG, "has_cherry_log");
        woodPlanter(output, ATEBlocks.CRIMSON_PLANTER.get(), Items.CRIMSON_PLANKS, Items.CRIMSON_STEM, "has_crimson_stem");
        woodPlanter(output, ATEBlocks.DARK_OAK_PLANTER.get(), Items.DARK_OAK_PLANKS, Items.DARK_OAK_LOG, "has_dark_oak_log");
        woodPlanter(output, ATEBlocks.JUNGLE_PLANTER.get(), Items.JUNGLE_PLANKS, Items.JUNGLE_LOG, "has_jungle_log");
        woodPlanter(output, ATEBlocks.MANGROVE_PLANTER.get(), Items.MANGROVE_PLANKS, Items.MANGROVE_LOG, "has_mangrove_log");
        woodPlanter(output, ATEBlocks.OAK_PLANTER.get(), Items.OAK_PLANKS, Items.OAK_LOG, "has_oak_log");
        woodPlanter(output, ATEBlocks.SPRUCE_PLANTER.get(), Items.SPRUCE_PLANKS, Items.SPRUCE_LOG, "has_spruce_log");
        woodPlanter(output, ATEBlocks.WARPED_PLANTER.get(), Items.WARPED_PLANKS, Items.WARPED_STEM, "has_warped_stem");
        woodPlanter(output, ATEBlocks.PALE_OAK_PLANTER.get(), Items.OAK_PLANKS, Items.OAK_LOG, "has_oak_log");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ATEBlocks.OAK_PLANTER.get())
                .pattern("PHP")
                .pattern("PPP")
                .define('P', ItemTags.PLANKS)
                .define('H', Items.HOPPER)
                .unlockedBy("has_oak_log", has(Items.OAK_LOG))
                .save(output, new ResourceLocation(MODID, "zzz_basic_planter_from_any_wood"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ATEItems.CLOCHE.get(), 4)
                .pattern("III")
                .pattern("IPI")
                .pattern("III")
                .define('P', glassBlocks())
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output, RecipeBuilder.getDefaultRecipeId(ATEItems.CLOCHE.get()));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ATEBlocks.ADVANCED_PLANTER.get())
                .pattern("F F")
                .pattern("IAI")
                .pattern("RIR")
                .define('F', Items.IRON_INGOT)
                .define('I', Items.IRON_BLOCK)
                .define('A', ATETags.Items.BASIC_PLANTER_ITEMS)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_basic_planter", has(ATEBlocks.OAK_PLANTER.get().asItem()))
                .save(output, RecipeBuilder.getDefaultRecipeId(ATEBlocks.ADVANCED_PLANTER.get()));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ATEBlocks.COMPOSTER.get())
                .pattern("I I")
                .pattern("ICI")
                .pattern("IRI")
                .define('C', Items.COMPOSTER)
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_composter", has(Items.COMPOSTER))
                .save(output, RecipeBuilder.getDefaultRecipeId(ATEBlocks.COMPOSTER.get()));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ATEBlocks.BIOMASS_BURNER.get())
                .pattern("III")
                .pattern("IFI")
                .pattern("IRI")
                .define('I', Items.IRON_INGOT)
                .define('F', Items.FURNACE)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_furnace", has(Items.FURNACE))
                .save(output, RecipeBuilder.getDefaultRecipeId(ATEBlocks.BIOMASS_BURNER.get()));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ATEBlocks.SILO.get())
                .pattern("III")
                .pattern("IFI")
                .pattern("IRI")
                .define('I', Items.IRON_INGOT)
                .define('F', Tags.Items.CHESTS)
                .define('R', Items.OBSERVER)
                .unlockedBy("has_observer", has(Items.OBSERVER))
                .save(output, RecipeBuilder.getDefaultRecipeId(ATEBlocks.SILO.get()));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ATEBlocks.FERT_SPREADER.get())
                .pattern("III")
                .pattern("IFI")
                .pattern("IRI")
                .define('I', Items.IRON_INGOT)
                .define('F', Tags.Items.CHESTS)
                .define('R', Items.DISPENSER)
                .unlockedBy("has_dispenser", has(Items.DISPENSER))
                .save(output, RecipeBuilder.getDefaultRecipeId(ATEBlocks.FERT_SPREADER.get()));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ATEBlocks.CAPACITOR_TIER_1.get())
                .pattern("RRR")
                .pattern("ICI")
                .pattern("RRR")
                .define('R', Items.REDSTONE)
                .define('I', Items.IRON_INGOT)
                .define('C', Items.COPPER_BLOCK)
                .unlockedBy("has_copper_block", has(Items.COPPER_BLOCK))
                .save(output, RecipeBuilder.getDefaultRecipeId(ATEBlocks.CAPACITOR_TIER_1.get()));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ATEBlocks.CAPACITOR_TIER_2.get())
                .pattern("RRR")
                .pattern("GCG")
                .pattern("RRR")
                .define('R', Items.REDSTONE_BLOCK)
                .define('G', Items.GOLD_INGOT)
                .define('C', ATEBlocks.CAPACITOR_TIER_1.get())
                .unlockedBy("has_capacitor_tier1", has(ATEBlocks.CAPACITOR_TIER_1.get().asItem()))
                .save(output, RecipeBuilder.getDefaultRecipeId(ATEBlocks.CAPACITOR_TIER_2.get()));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ATEBlocks.CAPACITOR_TIER_3.get())
                .pattern("DDD")
                .pattern("ECE")
                .pattern("DDD")
                .define('D', Items.DIAMOND)
                .define('E', Items.EMERALD)
                .define('C', ATEBlocks.CAPACITOR_TIER_2.get())
                .unlockedBy("has_capacitor_tier2", has(ATEBlocks.CAPACITOR_TIER_2.get().asItem()))
                .save(output, RecipeBuilder.getDefaultRecipeId(ATEBlocks.CAPACITOR_TIER_3.get()));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ATEItems.SM_MK1.get())
                .pattern(" R ")
                .pattern("IGI")
                .pattern(" R ")
                .define('R', Items.REDSTONE)
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(output, RecipeBuilder.getDefaultRecipeId(ATEItems.SM_MK1.get()));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ATEItems.SM_MK2.get())
                .pattern(" D ")
                .pattern("GSG")
                .pattern(" D ")
                .define('D', Items.DIAMOND)
                .define('G', Items.GOLD_BLOCK)
                .define('S', ATEItems.SM_MK1.get())
                .unlockedBy("has_sm_mk1", has(ATEItems.SM_MK1.get()))
                .save(output, RecipeBuilder.getDefaultRecipeId(ATEItems.SM_MK2.get()));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ATEItems.SM_MK3.get())
                .pattern("ENE")
                .pattern("DSD")
                .pattern("ENE")
                .define('N', Items.NETHERITE_INGOT)
                .define('D', Items.DIAMOND_BLOCK)
                .define('E', Items.EMERALD_BLOCK)
                .define('S', ATEItems.SM_MK2.get())
                .unlockedBy("has_sm_mk2", has(ATEItems.SM_MK2.get()))
                .save(output, RecipeBuilder.getDefaultRecipeId(ATEItems.SM_MK3.get()));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ATEItems.YM_MK1.get())
                .pattern(" W ")
                .pattern("SCS")
                .pattern(" W ")
                .define('W', Items.WHEAT)
                .define('S', Items.WHEAT_SEEDS)
                .define('C', Items.COPPER_BLOCK)
                .unlockedBy("has_farmland", has(Items.FARMLAND))
                .save(output, RecipeBuilder.getDefaultRecipeId(ATEItems.YM_MK1.get()));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ATEItems.YM_MK2.get())
                .pattern(" G ")
                .pattern("CYC")
                .pattern(" G ")
                .define('G', Items.GOLD_BLOCK)
                .define('C', Items.COPPER_BLOCK)
                .define('Y', ATEItems.YM_MK1.get())
                .unlockedBy("has_ym_mk1", has(ATEItems.YM_MK1.get()))
                .save(output, RecipeBuilder.getDefaultRecipeId(ATEItems.YM_MK2.get()));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ATEItems.YM_MK3.get())
                .pattern("ENE")
                .pattern("GYG")
                .pattern("ENE")
                .define('E', Items.ENCHANTED_GOLDEN_APPLE)
                .define('G', Items.GOLD_BLOCK)
                .define('N', Items.NETHERITE_INGOT)
                .define('Y', ATEItems.YM_MK2.get())
                .unlockedBy("has_ym_mk2", has(ATEItems.YM_MK2.get()))
                .save(output, RecipeBuilder.getDefaultRecipeId(ATEItems.YM_MK3.get()));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ATEItems.RM_MK1.get())
                .pattern(" W ")
                .pattern("SCS")
                .pattern(" W ")
                .define('W', Items.REDSTONE_TORCH)
                .define('S', Items.REDSTONE)
                .define('C', Items.COPPER_BLOCK)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(output, RecipeBuilder.getDefaultRecipeId(ATEItems.RM_MK1.get()));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ATEItems.RM_MK2.get())
                .pattern(" G ")
                .pattern("CYC")
                .pattern(" G ")
                .define('G', Items.REDSTONE_BLOCK)
                .define('C', Items.IRON_BLOCK)
                .define('Y', ATEItems.RM_MK1.get())
                .unlockedBy("has_rm_mk1", has(ATEItems.RM_MK1.get()))
                .save(output, RecipeBuilder.getDefaultRecipeId(ATEItems.RM_MK2.get()));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ATEItems.RM_MK3.get())
                .pattern("ENE")
                .pattern("GYG")
                .pattern("ENE")
                .define('E', Items.COMPARATOR)
                .define('G', Items.REDSTONE_BLOCK)
                .define('N', Items.NETHERITE_INGOT)
                .define('Y', ATEItems.RM_MK2.get())
                .unlockedBy("has_rm_mk2", has(ATEItems.RM_MK2.get()))
                .save(output, RecipeBuilder.getDefaultRecipeId(ATEItems.RM_MK3.get()));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ATEItems.COMPACTED_BIOMASS.get())
                .pattern("BBB")
                .pattern("BBB")
                .pattern("BBB")
                .define('B', ATEItems.BIOMASS.get())
                .unlockedBy("has_biomass", has(ATEItems.BIOMASS.get()))
                .save(output, RecipeBuilder.getDefaultRecipeId(ATEItems.COMPACTED_BIOMASS.get()));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ATEItems.BIOMASS.get(), 9)
                .requires(ATEItems.COMPACTED_BIOMASS.get())
                .unlockedBy("has_compacted_biomass", has(ATEItems.COMPACTED_BIOMASS.get()))
                .save(output, new ResourceLocation(MODID, "biomass_from_compacted"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ATEBlocks.COMPACTED_BIOMASS_BLOCK.get())
                .pattern("CCC")
                .pattern("CCC")
                .pattern("CCC")
                .define('C', ATEItems.COMPACTED_BIOMASS.get())
                .unlockedBy("has_compacted_biomass", has(ATEItems.COMPACTED_BIOMASS.get()))
                .save(output, RecipeBuilder.getDefaultRecipeId(ATEBlocks.COMPACTED_BIOMASS_BLOCK.get()));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ATEItems.COMPACTED_BIOMASS.get(), 9)
                .requires(ATEBlocks.COMPACTED_BIOMASS_BLOCK.get())
                .unlockedBy("has_compacted_biomass_block", has(ATEBlocks.COMPACTED_BIOMASS_BLOCK.get().asItem()))
                .save(output, new ResourceLocation(MODID, "compacted_biomass_from_block"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ATEBlocks.MULCH.get(), 2)
                .pattern("BBB")
                .pattern("BFB")
                .pattern("BBB")
                .define('B', ATEItems.COMPACTED_BIOMASS.get())
                .define('F', Items.FARMLAND)
                .unlockedBy("has_farmland", has(Items.FARMLAND))
                .save(output, RecipeBuilder.getDefaultRecipeId(ATEBlocks.MULCH.get()));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ATEItems.CRUDE_BIOMASS.get())
                .pattern("LLL")
                .pattern("DDD")
                .pattern("LLL")
                .define('L', ItemTags.LEAVES)
                .define('D', ATETags.Items.DIRT_LIKE_BLOCK_ITEMS)
                .unlockedBy("has_leaves", has(ItemTags.LEAVES))
                .save(output, RecipeBuilder.getDefaultRecipeId(ATEItems.CRUDE_BIOMASS.get()));

        terracottaPlanter(output, ATEBlocks.TERRACOTTA_PLANTER.get(), Items.TERRACOTTA, "terracotta_planter");
        terracottaPlanter(output, ATEBlocks.BLACK_TERRACOTTA_PLANTER.get(), Items.BLACK_TERRACOTTA, "black_terracotta_planter");
        terracottaPlanter(output, ATEBlocks.BLUE_TERRACOTTA_PLANTER.get(), Items.BLUE_TERRACOTTA, "blue_terracotta_planter");
        terracottaPlanter(output, ATEBlocks.BROWN_TERRACOTTA_PLANTER.get(), Items.BROWN_TERRACOTTA, "brown_terracotta_planter");
        terracottaPlanter(output, ATEBlocks.CYAN_TERRACOTTA_PLANTER.get(), Items.CYAN_TERRACOTTA, "cyan_terracotta_planter");
        terracottaPlanter(output, ATEBlocks.GRAY_TERRACOTTA_PLANTER.get(), Items.GRAY_TERRACOTTA, "gray_terracotta_planter");
        terracottaPlanter(output, ATEBlocks.GREEN_TERRACOTTA_PLANTER.get(), Items.GREEN_TERRACOTTA, "green_terracotta_planter");
        terracottaPlanter(output, ATEBlocks.LIGHT_BLUE_TERRACOTTA_PLANTER.get(), Items.LIGHT_BLUE_TERRACOTTA, "light_blue_terracotta_planter");
        terracottaPlanter(output, ATEBlocks.LIGHT_GRAY_TERRACOTTA_PLANTER.get(), Items.LIGHT_GRAY_TERRACOTTA, "light_gray_terracotta_planter");
        terracottaPlanter(output, ATEBlocks.LIME_TERRACOTTA_PLANTER.get(), Items.LIME_TERRACOTTA, "lime_terracotta_planter");
        terracottaPlanter(output, ATEBlocks.MAGENTA_TERRACOTTA_PLANTER.get(), Items.MAGENTA_TERRACOTTA, "magenta_terracotta_planter");
        terracottaPlanter(output, ATEBlocks.ORANGE_TERRACOTTA_PLANTER.get(), Items.ORANGE_TERRACOTTA, "orange_terracotta_planter");
        terracottaPlanter(output, ATEBlocks.PINK_TERRACOTTA_PLANTER.get(), Items.PINK_TERRACOTTA, "pink_terracotta_planter");
        terracottaPlanter(output, ATEBlocks.PURPLE_TERRACOTTA_PLANTER.get(), Items.PURPLE_TERRACOTTA, "purple_terracotta_planter");
        terracottaPlanter(output, ATEBlocks.RED_TERRACOTTA_PLANTER.get(), Items.RED_TERRACOTTA, "red_terracotta_planter");
        terracottaPlanter(output, ATEBlocks.WHITE_TERRACOTTA_PLANTER.get(), Items.WHITE_TERRACOTTA, "white_terracotta_planter");
        terracottaPlanter(output, ATEBlocks.YELLOW_TERRACOTTA_PLANTER.get(), Items.YELLOW_TERRACOTTA, "yellow_terracotta_planter");

        dyedPlanter(output, ATEBlocks.BLACK_TERRACOTTA_PLANTER.get(), Items.BLACK_DYE, "black_terracotta_planter_from_dye");
        dyedPlanter(output, ATEBlocks.BLUE_TERRACOTTA_PLANTER.get(), Items.BLUE_DYE, "blue_terracotta_planter_from_dye");
        dyedPlanter(output, ATEBlocks.BROWN_TERRACOTTA_PLANTER.get(), Items.BROWN_DYE, "brown_terracotta_planter_from_dye");
        dyedPlanter(output, ATEBlocks.CYAN_TERRACOTTA_PLANTER.get(), Items.CYAN_DYE, "cyan_terracotta_planter_from_dye");
        dyedPlanter(output, ATEBlocks.GRAY_TERRACOTTA_PLANTER.get(), Items.GRAY_DYE, "gray_terracotta_planter_from_dye");
        dyedPlanter(output, ATEBlocks.GREEN_TERRACOTTA_PLANTER.get(), Items.GREEN_DYE, "green_terracotta_planter_from_dye");
        dyedPlanter(output, ATEBlocks.LIGHT_BLUE_TERRACOTTA_PLANTER.get(), Items.LIGHT_BLUE_DYE, "light_blue_terracotta_planter_from_dye");
        dyedPlanter(output, ATEBlocks.LIGHT_GRAY_TERRACOTTA_PLANTER.get(), Items.LIGHT_GRAY_DYE, "light_gray_terracotta_planter_from_dye");
        dyedPlanter(output, ATEBlocks.LIME_TERRACOTTA_PLANTER.get(), Items.LIME_DYE, "lime_terracotta_planter_from_dye");
        dyedPlanter(output, ATEBlocks.MAGENTA_TERRACOTTA_PLANTER.get(), Items.MAGENTA_DYE, "magenta_terracotta_planter_from_dye");
        dyedPlanter(output, ATEBlocks.ORANGE_TERRACOTTA_PLANTER.get(), Items.ORANGE_DYE, "orange_terracotta_planter_from_dye");
        dyedPlanter(output, ATEBlocks.PINK_TERRACOTTA_PLANTER.get(), Items.PINK_DYE, "pink_terracotta_planter_from_dye");
        dyedPlanter(output, ATEBlocks.PURPLE_TERRACOTTA_PLANTER.get(), Items.PURPLE_DYE, "purple_terracotta_planter_from_dye");
        dyedPlanter(output, ATEBlocks.RED_TERRACOTTA_PLANTER.get(), Items.RED_DYE, "red_terracotta_planter_from_dye");
        dyedPlanter(output, ATEBlocks.WHITE_TERRACOTTA_PLANTER.get(), Items.WHITE_DYE, "white_terracotta_planter_from_dye");
        dyedPlanter(output, ATEBlocks.YELLOW_TERRACOTTA_PLANTER.get(), Items.YELLOW_DYE, "yellow_terracotta_planter_from_dye");

        saveTillingRecipe(output, "dirt_to_farmland", Items.DIRT, Items.FARMLAND);
        saveTillingRecipe(output, "rooted_dirt_to_farmland", Items.ROOTED_DIRT, Items.FARMLAND);
        saveTillingRecipe(output, "coarse_dirt_to_farmland", Items.COARSE_DIRT, Items.FARMLAND);
        saveTillingRecipe(output, "grass_to_farmland", Items.GRASS_BLOCK, Items.FARMLAND);
        saveTillingRecipe(output, "mulch_to_infused_farmland", ATEBlocks.MULCH.get().asItem(), ATEBlocks.INFUSED_FARMLAND.get().asItem());
    }

    private static void woodPlanter(Consumer<FinishedRecipe> output, ItemLike result, ItemLike planks, ItemLike log, String unlockKey) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result)
                .pattern("PHP")
                .pattern("PPP")
                .define('P', planks)
                .define('H', Items.HOPPER)
                .unlockedBy(unlockKey, has(log))
                .save(output, RecipeBuilder.getDefaultRecipeId(result));
    }

    private static void terracottaPlanter(Consumer<FinishedRecipe> output, ItemLike result, ItemLike terracotta, String name) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result)
                .pattern("PHP")
                .pattern("PPP")
                .define('P', terracotta)
                .define('H', Items.HOPPER)
                .unlockedBy("has_hopper", has(Items.HOPPER))
                .save(output, new ResourceLocation(MODID, name));
    }

    private static void dyedPlanter(Consumer<FinishedRecipe> output, ItemLike result, ItemLike dye, String name) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result)
                .requires(ATEBlocks.TERRACOTTA_PLANTER.get())
                .requires(dye)
                .unlockedBy("has_terracotta_planter", has(ATEBlocks.TERRACOTTA_PLANTER.get().asItem()))
                .save(output, new ResourceLocation(MODID, name));
    }

    private static Ingredient glassBlocks() {
        return Ingredient.of(
                Items.GLASS,
                Items.TINTED_GLASS,
                Items.WHITE_STAINED_GLASS, Items.ORANGE_STAINED_GLASS, Items.MAGENTA_STAINED_GLASS, Items.LIGHT_BLUE_STAINED_GLASS,
                Items.YELLOW_STAINED_GLASS, Items.LIME_STAINED_GLASS, Items.PINK_STAINED_GLASS, Items.GRAY_STAINED_GLASS,
                Items.LIGHT_GRAY_STAINED_GLASS, Items.CYAN_STAINED_GLASS, Items.PURPLE_STAINED_GLASS, Items.BLUE_STAINED_GLASS,
                Items.BROWN_STAINED_GLASS, Items.GREEN_STAINED_GLASS, Items.RED_STAINED_GLASS, Items.BLACK_STAINED_GLASS);
    }

    private static void saveTillingRecipe(Consumer<FinishedRecipe> output, String name, ItemLike input, ItemLike result) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(Ingredient.of(input));

        DurabilityShapelessRecipe recipe = new DurabilityShapelessRecipe(
                CraftingBookCategory.MISC,
                new ItemStack(result),
                ingredients,
                Ingredient.of(ItemTags.HOES),
                1
        );

        output.accept(new DurabilityShapelessFinishedRecipe(new ResourceLocation(MODID, name), recipe, ItemTags.HOES));
    }

    /**
     * @param toolTag the tag the tool ingredient was built from. {@link Ingredient} keeps its
     *                tag identity private, so the shared ingredient codec cannot recover it and
     *                would serialise the unbound tag as an empty item list. The tag is carried
     *                here so the tool survives a datagen round trip.
     */
    private record DurabilityShapelessFinishedRecipe(ResourceLocation id, DurabilityShapelessRecipe recipe,
                                                    @Nullable TagKey<Item> toolTag) implements FinishedRecipe {

        @Override
        public void serializeRecipeData(JsonObject json) {
            JsonElement encoded = DurabilityShapelessRecipeSerializer.CODEC
                    .encodeStart(JsonOps.INSTANCE, recipe)
                    .getOrThrow(false, message -> {
                        throw new IllegalStateException("Failed to encode recipe " + id + ": " + message);
                    });
            for (Map.Entry<String, JsonElement> entry : encoded.getAsJsonObject().entrySet()) {
                json.add(entry.getKey(), entry.getValue());
            }
            if (toolTag != null) {
                json.addProperty("tool", "#" + toolTag.location());
            }
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return recipe.getSerializer();
        }

        @Override
        public JsonObject serializeAdvancement() {
            JsonObject advancement = new JsonObject();
            advancement.addProperty("parent", "minecraft:recipes/root");

            JsonObject criteria = new JsonObject();
            JsonObject hasTheRecipe = new JsonObject();
            hasTheRecipe.addProperty("trigger", "minecraft:recipe_unlocked");
            JsonObject conditions = new JsonObject();
            conditions.addProperty("recipe", id.toString());
            hasTheRecipe.add("conditions", conditions);
            criteria.add("has_the_recipe", hasTheRecipe);
            advancement.add("criteria", criteria);

            JsonArray requirements = new JsonArray();
            JsonArray hasTheRecipeRequirement = new JsonArray();
            hasTheRecipeRequirement.add("has_the_recipe");
            requirements.add(hasTheRecipeRequirement);
            advancement.add("requirements", requirements);

            JsonObject rewards = new JsonObject();
            JsonArray recipeReward = new JsonArray();
            recipeReward.add(id.toString());
            rewards.add("recipes", recipeReward);
            advancement.add("rewards", rewards);

            return advancement;
        }

        @Override
        public @Nullable ResourceLocation getAdvancementId() {
            return new ResourceLocation(id.getNamespace(), "recipes/" + id.getPath());
        }
    }
}
