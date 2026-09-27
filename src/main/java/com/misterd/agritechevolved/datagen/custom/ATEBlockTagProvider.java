package com.misterd.agritechevolved.datagen.custom;

import com.misterd.agritechevolved.AgritechEvolved;
import com.misterd.agritechevolved.block.ATEBlocks;
import com.misterd.agritechevolved.util.ATETags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.concurrent.CompletableFuture;

public class ATEBlockTagProvider extends BlockTagsProvider {

    public ATEBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existing) {
        super(output, lookupProvider, AgritechEvolved.MODID, existing);
    }

    private static ResourceKey<Block> key(Block block) {
        return ResourceKey.create(Registries.BLOCK, ForgeRegistries.BLOCKS.getKey(block));
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_AXE)
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
                .add(key(ATEBlocks.PALE_OAK_PLANTER.get()));

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(key(ATEBlocks.ADVANCED_PLANTER.get()))
                .add(key(ATEBlocks.BIOMASS_BURNER.get()))
                .add(key(ATEBlocks.COMPOSTER.get()))
                .add(key(ATEBlocks.CAPACITOR_TIER_1.get()))
                .add(key(ATEBlocks.CAPACITOR_TIER_2.get()))
                .add(key(ATEBlocks.CAPACITOR_TIER_3.get()))
                .add(key(ATEBlocks.SILO.get()))
                .add(key(ATEBlocks.FERT_SPREADER.get()))
                .add(key(ATEBlocks.COMPACTED_BIOMASS_BLOCK.get()))
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

        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(key(ATEBlocks.INFUSED_FARMLAND.get()))
                .add(key(ATEBlocks.MULCH.get()));

        tag(ATETags.Blocks.BASIC_PLANTERS)
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
    }
}
