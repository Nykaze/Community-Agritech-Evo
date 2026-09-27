package com.misterd.agritechevolved.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ATETags {
    public static class Items {
        public static final TagKey<Item> BIOMASS = createTag("biomass");
        public static final TagKey<Item> DIRT_LIKE_BLOCK_ITEMS = createTag("dirt_like_block_items");
        public static final TagKey<Item> BASIC_PLANTER_ITEMS = createTag("basic_planter_items");
  public static final TagKey<Item> ATE_MODULES = createTag("agritechevolved_modules");
  public static final TagKey<Item> ATE_RANGE_MODULES = createTag("agritechevolved_range_modules");
        public static final TagKey<Item> FARMLAND_SOILS = createTag("farmland_soils");
        public static final TagKey<Item> DIRT_SOILS = createTag("dirt_soils");
        public static final TagKey<Item> TREE_SOILS = createTag("tree_soils");
        public static final TagKey<Item> SAND_SOILS = createTag("sand_soils");
        public static final TagKey<Item> SOUL_SAND_SOILS = createTag("soul_sand_soils");
        public static final TagKey<Item> MOSS_SOILS = createTag("moss_soils");
        public static final TagKey<Item> WATER_SOILS = createTag("water_soils");
        public static final TagKey<Item> MUSHROOM_SOILS = createTag("mushroom_soils");
        public static final TagKey<Item> NETHER_SOILS = createTag("nether_soils");
        public static final TagKey<Item> STONE_SOILS = createTag("stone_soils");
        public static final TagKey<Item> END_SOILS = createTag("end_soils");
        public static final TagKey<Item> JUNGLE_SOILS = createTag("jungle_soils");

        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(new ResourceLocation("community_agritechevolved", name));
        }
    }

    public static class Blocks {
        public static final TagKey<Block> BASIC_PLANTERS = createTag("basic_planters");

        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(new ResourceLocation("community_agritechevolved", name));
        }
    }
}
