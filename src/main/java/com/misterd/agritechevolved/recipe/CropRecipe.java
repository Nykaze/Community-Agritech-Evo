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

import java.util.List;

public class CropRecipe implements Recipe<Container> {

    private ResourceLocation id;
    private final Ingredient seed;
    private final List<Ingredient> soils;
    private final List<DropEntry> drops;

    public CropRecipe(Ingredient seed, List<Ingredient> soils, List<DropEntry> drops) {
        this(new ResourceLocation(AgritechEvolved.MODID, "crop"), seed, soils, drops);
    }

    public CropRecipe(ResourceLocation id, Ingredient seed, List<Ingredient> soils, List<DropEntry> drops) {
        this.id = id;
        this.seed = seed;
        this.soils = soils;
        this.drops = drops;
    }

    public Ingredient getSeed() { return seed; }
    public List<Ingredient> getSoils() { return soils; }
    public List<DropEntry> getDrops() { return drops; }

    public void setId(ResourceLocation id) {
        this.id = id;
    }

    public boolean matchesSeed(ItemStack stack) {
        return seed.test(stack);
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
            if (seed.test(container.getItem(i))) return true;
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
        list.add(seed);
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
        return ATERecipeTypes.CROP_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ATERecipeTypes.CROP_TYPE.get();
    }

    public static final Codec<CropRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ATEIngredientCodecs.INGREDIENT.fieldOf("seed").forGetter(CropRecipe::getSeed),
            ATEIngredientCodecs.INGREDIENT.listOf().fieldOf("soils").forGetter(CropRecipe::getSoils),
            DropEntry.CODEC.listOf().fieldOf("drops").forGetter(CropRecipe::getDrops)
    ).apply(instance, CropRecipe::new));

    public void toNetwork(FriendlyByteBuf buf) {
        seed.toNetwork(buf);
        buf.writeVarInt(soils.size());
        for (Ingredient soil : soils) {
            soil.toNetwork(buf);
        }
        buf.writeVarInt(drops.size());
        for (DropEntry drop : drops) {
            drop.toNetwork(buf);
        }
    }

    public static CropRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        Ingredient seed = Ingredient.fromNetwork(buf);
        int soilCount = buf.readVarInt();
        java.util.List<Ingredient> soils = new java.util.ArrayList<>(soilCount);
        for (int i = 0; i < soilCount; i++) {
            soils.add(Ingredient.fromNetwork(buf));
        }
        int dropCount = buf.readVarInt();
        List<DropEntry> drops = new java.util.ArrayList<>(dropCount);
        for (int i = 0; i < dropCount; i++) {
            drops.add(DropEntry.fromNetwork(buf));
        }
        return new CropRecipe(id, seed, soils, drops);
    }
}
