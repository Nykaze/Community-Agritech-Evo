package com.misterd.agritechevolved.recipe;

import com.misterd.agritechevolved.AgritechEvolved;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class TreeRecipe implements Recipe<Container> {

    private ResourceLocation id;
    private final Ingredient sapling;
    private final List<Ingredient> soils;
    private final List<DropEntry> drops;

    public TreeRecipe(Ingredient sapling, List<Ingredient> soils, List<DropEntry> drops) {
        this(new ResourceLocation(AgritechEvolved.MODID, "tree"), sapling, soils, drops);
    }

    public TreeRecipe(ResourceLocation id, Ingredient sapling, List<Ingredient> soils, List<DropEntry> drops) {
        this.id = id;
        this.sapling = sapling;
        this.soils = soils;
        this.drops = drops;
    }

    public Ingredient getSapling() { return sapling; }
    public List<Ingredient> getSoils() { return soils; }
    public List<DropEntry> getDrops() { return drops; }

    public void setId(ResourceLocation id) {
        this.id = id;
    }

    public boolean matchesSapling(ItemStack stack) {
        return sapling.test(stack);
    }

    public boolean matchesSoil(ItemStack stack) {
        for (Ingredient soil : soils) {
            if (soil.test(stack)) return true;
        }
        return false;
    }

    @Override
    public boolean matches(Container container, Level level) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (sapling.test(container.getItem(i))) return true;
        }
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(sapling);
        list.addAll(soils);
        return list;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String getGroup() {
        return "";
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ATERecipeTypes.TREE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ATERecipeTypes.TREE_TYPE.get();
    }

    public static final Codec<TreeRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ATEIngredientCodecs.INGREDIENT.fieldOf("sapling").forGetter(TreeRecipe::getSapling),
            ATEIngredientCodecs.INGREDIENT.listOf().fieldOf("soils").forGetter(TreeRecipe::getSoils),
            DropEntry.CODEC.listOf().fieldOf("drops").forGetter(TreeRecipe::getDrops)
    ).apply(instance, TreeRecipe::new));

    public void toNetwork(FriendlyByteBuf buf) {
        sapling.toNetwork(buf);
        buf.writeVarInt(soils.size());
        for (Ingredient soil : soils) {
            soil.toNetwork(buf);
        }
        buf.writeVarInt(drops.size());
        for (DropEntry drop : drops) {
            drop.toNetwork(buf);
        }
    }

    public static TreeRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        Ingredient sapling = Ingredient.fromNetwork(buf);
        int soilCount = buf.readVarInt();
        List<Ingredient> soils = new ArrayList<>(soilCount);
        for (int i = 0; i < soilCount; i++) {
            soils.add(Ingredient.fromNetwork(buf));
        }
        int dropCount = buf.readVarInt();
        List<DropEntry> drops = new ArrayList<>(dropCount);
        for (int i = 0; i < dropCount; i++) {
            drops.add(DropEntry.fromNetwork(buf));
        }
        return new TreeRecipe(id, sapling, soils, drops);
    }
}
