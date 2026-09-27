package com.misterd.agritechevolved.compat.jei;

import com.misterd.agritechevolved.recipe.CropRecipe;
import com.misterd.agritechevolved.recipe.DropEntry;
import com.misterd.agritechevolved.recipe.TreeRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public class PlanterRecipe {
    private final Ingredient plant;
    private final List<Ingredient> soils;
    private final List<ItemStack> outputs;
    private final List<DropEntry> drops;
    private final PlantableType type;

    public PlanterRecipe(Ingredient plant, List<Ingredient> soils, List<ItemStack> outputs, List<DropEntry> drops, PlantableType type) {
        this.plant = plant;
        this.soils = soils;
        this.outputs = outputs;
        this.drops = drops;
        this.type = type;
    }

    public static PlanterRecipe fromCrop(CropRecipe recipe) {
        List<ItemStack> outputs = recipe.getDrops().stream()
                .filter(d -> d.chance() > 0.0F)
                .map(d -> new ItemStack(d.item(), (d.min() + d.max()) / 2))
                .toList();
        List<DropEntry> drops = recipe.getDrops().stream()
                .filter(d -> d.chance() > 0.0F)
                .toList();
        return new PlanterRecipe(recipe.getSeed(), recipe.getSoils(), outputs, drops, PlantableType.CROP);
    }

    public static PlanterRecipe fromTree(TreeRecipe recipe) {
        List<ItemStack> outputs = recipe.getDrops().stream()
                .filter(d -> d.chance() > 0.0F)
                .map(d -> new ItemStack(d.item(), (d.min() + d.max()) / 2))
                .toList();
        List<DropEntry> drops = recipe.getDrops().stream()
                .filter(d -> d.chance() > 0.0F)
                .toList();
        return new PlanterRecipe(recipe.getSapling(), recipe.getSoils(), outputs, drops, PlantableType.TREE);
    }

    public Ingredient getPlant() {
        return plant;
    }

    public List<Ingredient> getSoils() {
        return soils;
    }

    public List<ItemStack> getOutputs() {
        return outputs;
    }

    public List<DropEntry> getDrops() {
        return drops;
    }

    public PlantableType getType() {
        return type;
    }

    public enum PlantableType {
        CROP,
        TREE
    }
}