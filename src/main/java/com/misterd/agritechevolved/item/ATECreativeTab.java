package com.misterd.agritechevolved.item;

import com.misterd.agritechevolved.AgritechEvolved;
import com.misterd.agritechevolved.block.ATEBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ATECreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AgritechEvolved.MODID);

    public static final RegistryObject<CreativeModeTab> AGRITECH_EVOLVED = CREATIVE_MODE_TAB.register("agritechevolved_creativetab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ATEBlocks.ADVANCED_PLANTER.get()))
                    .title(Component.translatable("creativetab.community_agritechevolved"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ATEBlocks.ACACIA_PLANTER.get());
                        output.accept(ATEBlocks.BAMBOO_PLANTER.get());
                        output.accept(ATEBlocks.BIRCH_PLANTER.get());
                        output.accept(ATEBlocks.CHERRY_PLANTER.get());
                        output.accept(ATEBlocks.CRIMSON_PLANTER.get());
                        output.accept(ATEBlocks.DARK_OAK_PLANTER.get());
                        output.accept(ATEBlocks.JUNGLE_PLANTER.get());
                        output.accept(ATEBlocks.MANGROVE_PLANTER.get());
                        output.accept(ATEBlocks.OAK_PLANTER.get());
                        output.accept(ATEBlocks.PALE_OAK_PLANTER.get());
                        output.accept(ATEBlocks.SPRUCE_PLANTER.get());
                        output.accept(ATEBlocks.WARPED_PLANTER.get());

                        output.accept(ATEBlocks.ADVANCED_PLANTER.get());

                        output.accept(ATEBlocks.TERRACOTTA_PLANTER.get());
                        output.accept(ATEBlocks.BLACK_TERRACOTTA_PLANTER.get());
                        output.accept(ATEBlocks.BLUE_TERRACOTTA_PLANTER.get());
                        output.accept(ATEBlocks.BROWN_TERRACOTTA_PLANTER.get());
                        output.accept(ATEBlocks.CYAN_TERRACOTTA_PLANTER.get());
                        output.accept(ATEBlocks.GRAY_TERRACOTTA_PLANTER.get());
                        output.accept(ATEBlocks.GREEN_TERRACOTTA_PLANTER.get());
                        output.accept(ATEBlocks.LIGHT_BLUE_TERRACOTTA_PLANTER.get());
                        output.accept(ATEBlocks.LIGHT_GRAY_TERRACOTTA_PLANTER.get());
                        output.accept(ATEBlocks.LIME_TERRACOTTA_PLANTER.get());
                        output.accept(ATEBlocks.MAGENTA_TERRACOTTA_PLANTER.get());
                        output.accept(ATEBlocks.ORANGE_TERRACOTTA_PLANTER.get());
                        output.accept(ATEBlocks.PINK_TERRACOTTA_PLANTER.get());
                        output.accept(ATEBlocks.PURPLE_TERRACOTTA_PLANTER.get());
                        output.accept(ATEBlocks.RED_TERRACOTTA_PLANTER.get());
                        output.accept(ATEBlocks.WHITE_TERRACOTTA_PLANTER.get());
                        output.accept(ATEBlocks.YELLOW_TERRACOTTA_PLANTER.get());

                        output.accept(ATEBlocks.COMPOSTER.get());

                        output.accept(ATEBlocks.BIOMASS_BURNER.get());
                        output.accept(ATEBlocks.SILO.get());
                        output.accept(ATEBlocks.FERT_SPREADER.get());
                        output.accept(ATEBlocks.CAPACITOR_TIER_1.get());
                        output.accept(ATEBlocks.CAPACITOR_TIER_2.get());
                        output.accept(ATEBlocks.CAPACITOR_TIER_3.get());

                        output.accept(ATEBlocks.MULCH.get());
                        output.accept(ATEBlocks.INFUSED_FARMLAND.get());
                        output.accept(ATEBlocks.COMPACTED_BIOMASS_BLOCK.get());

                        output.accept(ATEItems.CRUDE_BIOMASS.get());
                        output.accept(ATEItems.BIOMASS.get());
                        output.accept(ATEItems.COMPACTED_BIOMASS.get());
                        output.accept(ATEItems.YM_MK1.get());
                        output.accept(ATEItems.YM_MK2.get());
                        output.accept(ATEItems.YM_MK3.get());
                        output.accept(ATEItems.SM_MK1.get());
                        output.accept(ATEItems.SM_MK2.get());
                        output.accept(ATEItems.SM_MK3.get());
                        output.accept(ATEItems.RM_MK1.get());
                        output.accept(ATEItems.RM_MK2.get());
                        output.accept(ATEItems.RM_MK3.get());
                        output.accept(ATEItems.CLOCHE.get());
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}

