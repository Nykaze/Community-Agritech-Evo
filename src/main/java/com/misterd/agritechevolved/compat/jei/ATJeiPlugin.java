package com.misterd.agritechevolved.compat.jei;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.misterd.agritechevolved.block.ATEBlocks;
import com.misterd.agritechevolved.recipe.ATERecipeTypes;
import com.misterd.agritechevolved.recipe.CropRecipe;
import com.misterd.agritechevolved.recipe.TreeRecipe;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.fml.ModList;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@JeiPlugin
public class ATJeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_ID =
            new ResourceLocation("community_agritechevolved", "jei_plugin");

    private static IJeiRuntime jeiRuntime;
    private static boolean recipesRegistered = false;

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new PlanterRecipeCategory(guiHelper),
                new CompostRecipeCategory(guiHelper),
                new FarmlandRecipeCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<PlanterRecipe> planter = buildPlanterRecipes();
        recipesRegistered = !planter.isEmpty();
        if (recipesRegistered) {
            registration.addRecipes(PlanterRecipeCategory.PLANTER_RECIPE_TYPE, planter);
        }
        registration.addRecipes(CompostRecipeCategory.COMPOST_RECIPE_TYPE, generateCompostRecipes());
        registration.addRecipes(FarmlandRecipeCategory.FARMLAND_RECIPE_TYPE, generateFarmlandRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ATEBlocks.OAK_PLANTER.get()), PlanterRecipeCategory.PLANTER_RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ATEBlocks.COMPOSTER.get()), CompostRecipeCategory.COMPOST_RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(Items.DIAMOND_HOE), FarmlandRecipeCategory.FARMLAND_RECIPE_TYPE);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        jeiRuntime = runtime;
        if (!recipesRegistered && Minecraft.getInstance().getConnection() != null) {
            List<PlanterRecipe> recipes = buildPlanterRecipes();
            if (!recipes.isEmpty()) {
                runtime.getRecipeManager().addRecipes(PlanterRecipeCategory.PLANTER_RECIPE_TYPE, recipes);
                LogUtils.getLogger().info("[ATE JEI] Injected {} planter recipes from onRuntimeAvailable", recipes.size());
            }
        }
    }

    public static IJeiRuntime getJeiRuntime() {
        return jeiRuntime;
    }

    /**
     * Called through {@link ATJeiBridge}, which verifies JEI is installed before we get here.
     */
    public static void showPlanterRecipes() {
        showRecipeType(PlanterRecipeCategory.PLANTER_RECIPE_TYPE);
    }

    /**
     * Called through {@link ATJeiBridge}, which verifies JEI is installed before we get here.
     */
    public static void showCompostRecipes() {
        showRecipeType(CompostRecipeCategory.COMPOST_RECIPE_TYPE);
    }

    private static void showRecipeType(mezz.jei.api.recipe.RecipeType<?> recipeType) {
        IJeiRuntime runtime = jeiRuntime;
        if (runtime == null) return;
        if (Minecraft.getInstance().player == null) return;
        runtime.getRecipesGui().showTypes(List.of(recipeType));
    }

    static List<PlanterRecipe> buildPlanterRecipes() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) {
            LogUtils.getLogger().warn("[ATE JEI] No connection, planter recipe injection deferred");
            return List.of();
        }
        List<PlanterRecipe> recipes = new ArrayList<>();
        DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, mc.getConnection().registryAccess());

        Path modFilePath = ModList.get().getModFileById("community_agritechevolved").getFile().getFilePath();
        try {
            if (Files.isDirectory(modFilePath)) {
                Path resourcesDir = modFilePath.getParent().getParent().getParent().resolve("resources").resolve("main");
                Path recipePath = resourcesDir.resolve("data").resolve("community_agritechevolved").resolve("recipes");
                if (!Files.exists(recipePath)) {
                    recipePath = resourcesDir.resolve("data").resolve("community_agritechevolved").resolve("recipe");
                }
                if (!Files.exists(recipePath)) {
                    recipePath = modFilePath.resolve("data").resolve("community_agritechevolved").resolve("recipes");
                }
                if (!Files.exists(recipePath)) {
                    recipePath = modFilePath.resolve("data").resolve("community_agritechevolved").resolve("recipe");
                }
                if (Files.exists(recipePath)) {
                    walkRecipes(recipePath, ops, recipes);
                } else {
                    LogUtils.getLogger().error("[ATE JEI] Recipe path not found: {}", recipePath);
                }
            } else {
                try (FileSystem fs = FileSystems.newFileSystem(modFilePath, Map.of())) {
                    Path recipePath = fs.getPath("/data/community_agritechevolved/recipes");
                    if (!Files.exists(recipePath)) {
                        recipePath = fs.getPath("/data/community_agritechevolved/recipe");
                    }
                    if (Files.exists(recipePath)) {
                        walkRecipes(recipePath, ops, recipes);
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.getLogger().error("[ATE JEI] Failed to walk mod recipes: {}", e.getMessage());
        }

        var server = mc.getSingleplayerServer();
        if (server != null) {
            try {
                Path datapackDir = server.getWorldPath(LevelResource.DATAPACK_DIR);
                if (Files.isDirectory(datapackDir)) {
                    try (var dpStream = Files.list(datapackDir)) {
                        dpStream.forEach(dp -> {
                            try {
                                if (Files.isDirectory(dp)) {
                                    Path recipePath = dp.resolve("data").resolve("community_agritechevolved").resolve("recipes");
                                    if (!Files.exists(recipePath)) {
                                        recipePath = dp.resolve("data").resolve("community_agritechevolved").resolve("recipe");
                                    }
                                    if (Files.exists(recipePath)) {
                                        walkRecipes(recipePath, ops, recipes);
                                    }
                                } else if (dp.toString().endsWith(".zip")) {
                                    try (FileSystem fs = FileSystems.newFileSystem(dp, Map.of())) {
                                        Path recipePath = fs.getPath("/data/community_agritechevolved/recipes");
                                        if (!Files.exists(recipePath)) {
                                            recipePath = fs.getPath("/data/community_agritechevolved/recipe");
                                        }
                                        if (Files.exists(recipePath)) {
                                            walkRecipes(recipePath, ops, recipes);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                LogUtils.getLogger().error("[ATE JEI] Failed to walk datapack {}: {}", dp.getFileName(), e.getMessage());
                            }
                        });
                    }
                }
            } catch (Exception e) {
                LogUtils.getLogger().error("[ATE JEI] Failed to access datapack dir: {}", e.getMessage());
            }

            try {
                for (var recipe : server.getRecipeManager().getRecipes()) {
                    try {
                        if (recipe.getType() == ATERecipeTypes.CROP_TYPE.get()) {
                            PlanterRecipe pr = PlanterRecipe.fromCrop((CropRecipe) recipe);
                            if (recipes.stream().noneMatch(r -> r.getPlant().equals(pr.getPlant()))) {
                                recipes.add(pr);
                            }
                        } else if (recipe.getType() == ATERecipeTypes.TREE_TYPE.get()) {
                            PlanterRecipe pr = PlanterRecipe.fromTree((TreeRecipe) recipe);
                            if (recipes.stream().noneMatch(r -> r.getPlant().equals(pr.getPlant()))) {
                                recipes.add(pr);
                            }
                        }
                    } catch (Exception e) {
                        LogUtils.getLogger().error("[ATE JEI] Failed to process server recipe: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                LogUtils.getLogger().error("[ATE JEI] Failed to read server RecipeManager: {}", e.getMessage());
            }
        }

        LogUtils.getLogger().info("[ATE JEI] Built {} planter recipes", recipes.size());
        return recipes;
    }

    private static void walkRecipes(Path recipePath, DynamicOps<JsonElement> ops, List<PlanterRecipe> recipes) throws Exception {
        try (var stream = Files.walk(recipePath)) {
            stream.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                try (var reader = Files.newBufferedReader(p)) {
                    JsonElement json = JsonParser.parseReader(reader);
                    if (!json.isJsonObject()) return;
                    var obj = json.getAsJsonObject();
                    var typeEl = obj.get("type");
                    if (typeEl == null) return;
                    String type = typeEl.getAsString();
                    if ("community_agritechevolved:crop".equals(type)) {
                        CropRecipe.CODEC.parse(ops, obj)
                                .result().ifPresent(crop -> recipes.add(PlanterRecipe.fromCrop(crop)));
                    } else if ("community_agritechevolved:tree".equals(type)) {
                        TreeRecipe.CODEC.parse(ops, obj)
                                .result().ifPresent(tree -> recipes.add(PlanterRecipe.fromTree(tree)));
                    }
                } catch (Exception e) {
                    LogUtils.getLogger().error("[ATE JEI] Failed to parse {}: {}", p, e.getMessage());
                }
            });
        }
    }

    private List<CompostRecipe> generateCompostRecipes() {
        List<CompostRecipe> recipes = new ArrayList<>();
        for (var item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            float chance = ComposterBlock.COMPOSTABLES.getFloat(item);
            if (chance <= 0f) continue;
            String itemId = BuiltInRegistries.ITEM.getKey(item).toString();
            try {
                recipes.add(CompostRecipe.create(itemId, chance));
            } catch (Exception e) {
                LogUtils.getLogger().error("[ATE JEI] Failed compost recipe for {}: {}", itemId, e.getMessage());
            }
        }
        LogUtils.getLogger().info("[ATE JEI] Generated {} compost recipes", recipes.size());
        return recipes;
    }

    private List<FarmlandRecipe> generateFarmlandRecipes() {
        List<FarmlandRecipe> recipes = new ArrayList<>();
        try {
            Ingredient hoe = Ingredient.of(ItemTags.HOES);
            addTilling(recipes, hoe, Items.DIRT, Items.FARMLAND);
            addTilling(recipes, hoe, Items.ROOTED_DIRT, Items.FARMLAND);
            addTilling(recipes, hoe, Items.COARSE_DIRT, Items.FARMLAND);
            addTilling(recipes, hoe, Items.GRASS_BLOCK, Items.FARMLAND);
            addTilling(recipes, hoe, ATEBlocks.MULCH.get().asItem(), ATEBlocks.INFUSED_FARMLAND.get().asItem());
            addTillingModded(recipes, hoe, "farmersdelight:rich_soil", "farmersdelight:rich_soil_farmland");
        } catch (Exception e) {
            LogUtils.getLogger().error("[ATE JEI] Failed to generate farmland recipes: {}", e.getMessage());
        }
        LogUtils.getLogger().info("[ATE JEI] Generated {} farmland recipes", recipes.size());
        return recipes;
    }

    private void addTilling(List<FarmlandRecipe> recipes, Ingredient hoe, Item input, Item result) {
        recipes.add(new FarmlandRecipe(Ingredient.of(input), hoe, new ItemStack(result)));
    }

    private void addTillingModded(List<FarmlandRecipe> recipes, Ingredient hoe, String inputId, String resultId) {
        try {
            Optional<Item> inputOpt = BuiltInRegistries.ITEM.getOptional(new ResourceLocation(inputId));
            Optional<Item> resultOpt = BuiltInRegistries.ITEM.getOptional(new ResourceLocation(resultId));
            if (inputOpt.isEmpty() || inputOpt.get() == Items.AIR) return;
            if (resultOpt.isEmpty() || resultOpt.get() == Items.AIR) return;
            addTilling(recipes, hoe, inputOpt.get(), resultOpt.get());
        } catch (Exception e) {
            LogUtils.getLogger().error("[ATE JEI] Failed modded tilling {} -> {}: {}", inputId, resultId, e.getMessage());
        }
    }
}
