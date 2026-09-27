package com.misterd.agritechevolved.datagen.custom;

import com.misterd.agritechevolved.AgritechEvolved;
import com.misterd.agritechevolved.block.ATEBlocks;
import com.misterd.agritechevolved.item.ATEItems;
import com.misterd.agritechevolved.util.ATETags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.concurrent.CompletableFuture;

public class ATEItemTagProvider extends ItemTagsProvider {

    public ATEItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                              CompletableFuture<TagsProvider.TagLookup<Block>> blockLookup, ExistingFileHelper existing) {
        super(output, lookupProvider, blockLookup, AgritechEvolved.MODID, existing);
    }

    private static ResourceKey<Item> key(ItemLike itemLike) {
        return ResourceKey.create(Registries.ITEM, ForgeRegistries.ITEMS.getKey(itemLike.asItem()));
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ATETags.Items.BIOMASS)
                .add(key(ATEItems.CRUDE_BIOMASS.get()))
                .add(key(ATEItems.BIOMASS.get()))
                .add(key(ATEItems.COMPACTED_BIOMASS.get()));

        tag(ATETags.Items.ATE_MODULES)
                .add(key(ATEItems.SM_MK1.get()))
                .add(key(ATEItems.SM_MK2.get()))
                .add(key(ATEItems.SM_MK3.get()))
                .add(key(ATEItems.YM_MK1.get()))
                .add(key(ATEItems.YM_MK2.get()))
                .add(key(ATEItems.YM_MK3.get()))
                .add(key(ATEItems.RM_MK1.get()))
                .add(key(ATEItems.RM_MK2.get()))
                .add(key(ATEItems.RM_MK3.get()));

        tag(ATETags.Items.BASIC_PLANTER_ITEMS)
                .add(key(ATEBlocks.ACACIA_PLANTER.get()))
                .add(key(ATEBlocks.BAMBOO_PLANTER.get()))
                .add(key(ATEBlocks.BIRCH_PLANTER.get()))
                .add(key(ATEBlocks.CHERRY_PLANTER.get()))
                .add(key(ATEBlocks.CRIMSON_PLANTER.get()))
                .add(key(ATEBlocks.DARK_OAK_PLANTER.get()))
                .add(key(ATEBlocks.JUNGLE_PLANTER.get()))
                .add(key(ATEBlocks.MANGROVE_PLANTER.get()))
                .add(key(ATEBlocks.OAK_PLANTER.get()))
                .add(key(ATEBlocks.SPRUCE_PLANTER.get()))
                .add(key(ATEBlocks.WARPED_PLANTER.get()))
                .add(key(ATEBlocks.PALE_OAK_PLANTER.get()))
                .add(key(ATEBlocks.TERRACOTTA_PLANTER.get()))
                .add(key(ATEBlocks.BLACK_TERRACOTTA_PLANTER.get()))
                .add(key(ATEBlocks.BLUE_TERRACOTTA_PLANTER.get()))
                .add(key(ATEBlocks.BROWN_TERRACOTTA_PLANTER.get()))
                .add(key(ATEBlocks.CYAN_TERRACOTTA_PLANTER.get()))
                .add(key(ATEBlocks.GRAY_TERRACOTTA_PLANTER.get()))
                .add(key(ATEBlocks.GREEN_TERRACOTTA_PLANTER.get()))
                .add(key(ATEBlocks.LIGHT_BLUE_TERRACOTTA_PLANTER.get()))
                .add(key(ATEBlocks.LIGHT_GRAY_TERRACOTTA_PLANTER.get()))
                .add(key(ATEBlocks.LIME_TERRACOTTA_PLANTER.get()))
                .add(key(ATEBlocks.MAGENTA_TERRACOTTA_PLANTER.get()))
                .add(key(ATEBlocks.ORANGE_TERRACOTTA_PLANTER.get()))
                .add(key(ATEBlocks.PINK_TERRACOTTA_PLANTER.get()))
                .add(key(ATEBlocks.PURPLE_TERRACOTTA_PLANTER.get()))
                .add(key(ATEBlocks.RED_TERRACOTTA_PLANTER.get()))
                .add(key(ATEBlocks.WHITE_TERRACOTTA_PLANTER.get()))
                .add(key(ATEBlocks.YELLOW_TERRACOTTA_PLANTER.get()));

        tag(ATETags.Items.DIRT_LIKE_BLOCK_ITEMS)
                .add(key(Blocks.DIRT.asItem()))
                .add(key(Blocks.PODZOL.asItem()))
                .add(key(Blocks.MYCELIUM.asItem()))
                .add(key(Blocks.COARSE_DIRT.asItem()))
                .add(key(Blocks.ROOTED_DIRT.asItem()))
                .add(key(Blocks.GRASS_BLOCK.asItem()));
    }
}
