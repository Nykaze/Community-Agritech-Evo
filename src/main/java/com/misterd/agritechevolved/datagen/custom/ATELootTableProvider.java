package com.misterd.agritechevolved.datagen.custom;

import com.misterd.agritechevolved.block.ATEBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class ATELootTableProvider extends BlockLootSubProvider {

    public ATELootTableProvider() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        dropSelf(ATEBlocks.OAK_PLANTER.get());
        dropSelf(ATEBlocks.ACACIA_PLANTER.get());
        dropSelf(ATEBlocks.BAMBOO_PLANTER.get());
        dropSelf(ATEBlocks.BIRCH_PLANTER.get());
        dropSelf(ATEBlocks.CHERRY_PLANTER.get());
        dropSelf(ATEBlocks.CRIMSON_PLANTER.get());
        dropSelf(ATEBlocks.DARK_OAK_PLANTER.get());
        dropSelf(ATEBlocks.JUNGLE_PLANTER.get());
        dropSelf(ATEBlocks.MANGROVE_PLANTER.get());
        dropSelf(ATEBlocks.SPRUCE_PLANTER.get());
        dropSelf(ATEBlocks.WARPED_PLANTER.get());
        dropSelf(ATEBlocks.PALE_OAK_PLANTER.get());
        dropSelf(ATEBlocks.ADVANCED_PLANTER.get());
        dropSelf(ATEBlocks.COMPOSTER.get());
        dropSelf(ATEBlocks.BIOMASS_BURNER.get());
        dropSelf(ATEBlocks.CAPACITOR_TIER_1.get());
        dropSelf(ATEBlocks.CAPACITOR_TIER_2.get());
        dropSelf(ATEBlocks.CAPACITOR_TIER_3.get());
        dropSelf(ATEBlocks.SILO.get());
        dropSelf(ATEBlocks.FERT_SPREADER.get());
        dropSelf(ATEBlocks.COMPACTED_BIOMASS_BLOCK.get());
        dropSelf(ATEBlocks.TERRACOTTA_PLANTER.get());
        dropSelf(ATEBlocks.BLACK_TERRACOTTA_PLANTER.get());
        dropSelf(ATEBlocks.BLUE_TERRACOTTA_PLANTER.get());
        dropSelf(ATEBlocks.BROWN_TERRACOTTA_PLANTER.get());
        dropSelf(ATEBlocks.CYAN_TERRACOTTA_PLANTER.get());
        dropSelf(ATEBlocks.GRAY_TERRACOTTA_PLANTER.get());
        dropSelf(ATEBlocks.GREEN_TERRACOTTA_PLANTER.get());
        dropSelf(ATEBlocks.LIGHT_BLUE_TERRACOTTA_PLANTER.get());
        dropSelf(ATEBlocks.LIGHT_GRAY_TERRACOTTA_PLANTER.get());
        dropSelf(ATEBlocks.LIME_TERRACOTTA_PLANTER.get());
        dropSelf(ATEBlocks.MAGENTA_TERRACOTTA_PLANTER.get());
        dropSelf(ATEBlocks.ORANGE_TERRACOTTA_PLANTER.get());
        dropSelf(ATEBlocks.PINK_TERRACOTTA_PLANTER.get());
        dropSelf(ATEBlocks.PURPLE_TERRACOTTA_PLANTER.get());
        dropSelf(ATEBlocks.RED_TERRACOTTA_PLANTER.get());
        dropSelf(ATEBlocks.WHITE_TERRACOTTA_PLANTER.get());
        dropSelf(ATEBlocks.YELLOW_TERRACOTTA_PLANTER.get());
        add(ATEBlocks.INFUSED_FARMLAND.get(), block -> createInfusedFarmlandTable(block, ATEBlocks.MULCH.get().asItem()));
        dropSelf(ATEBlocks.MULCH.get());
    }

    private LootTable.Builder createInfusedFarmlandTable(Block pBlock, Item pDropItem) {
        return createSilkTouchDispatchTable(
                pBlock,
                this.applyExplosionDecay(pBlock, LootItem.lootTableItem(pDropItem))
        );
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ATEBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get).toList();
    }
}
