package com.misterd.agritechevolved.block;

import com.misterd.agritechevolved.AgritechEvolved;
import com.misterd.agritechevolved.Config;
import com.misterd.agritechevolved.component.ATEDataComponents;
import com.misterd.agritechevolved.item.ATEItems;
import com.misterd.agritechevolved.block.custom.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class ATEBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, AgritechEvolved.MODID);

    public static final RegistryObject<Block> ACACIA_PLANTER = registerBlock("basic_acacia_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> BAMBOO_PLANTER = registerBlock("basic_bamboo_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> BIRCH_PLANTER = registerBlock("basic_birch_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> CHERRY_PLANTER = registerBlock("basic_cherry_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> CRIMSON_PLANTER = registerBlock("basic_crimson_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> DARK_OAK_PLANTER = registerBlock("basic_dark_oak_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> JUNGLE_PLANTER = registerBlock("basic_jungle_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> MANGROVE_PLANTER = registerBlock("basic_mangrove_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> OAK_PLANTER = registerBlock("basic_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> SPRUCE_PLANTER = registerBlock("basic_spruce_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> WARPED_PLANTER = registerBlock("basic_warped_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> PALE_OAK_PLANTER = registerBlock("basic_pale_oak_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> ADVANCED_PLANTER = registerBlock("advanced_planter",
            p -> new AdvancedPlanterBlock(p));

    public static final RegistryObject<Block> BIOMASS_BURNER = registerBlock("biomass_burner",
            p -> new BiomassBurnerBlock(p));

    public static final RegistryObject<Block> COMPOSTER = registerBlock("composter",
            p -> new ComposterBlock(p));

    public static final RegistryObject<Block> CAPACITOR_TIER_1 = registerBlock("capacitor_tier1",
            p -> new CapacitorTier1Block(p));

    public static final RegistryObject<Block> CAPACITOR_TIER_2 = registerBlock("capacitor_tier2",
            p -> new CapacitorTier2Block(p));

    public static final RegistryObject<Block> CAPACITOR_TIER_3 = registerBlock("capacitor_tier3",
            p -> new CapacitorTier3Block(p));

    public static final RegistryObject<Block> COMPACTED_BIOMASS_BLOCK = registerBlock("compacted_biomass_block",
            p -> new Block(p));

    public static final RegistryObject<Block> INFUSED_FARMLAND = registerBlock("infused_farmland",
            p -> new InfusedFarmlandBlock(p));

    public static final RegistryObject<Block> MULCH = registerBlock("mulch",
            p -> new MulchBlock(p));

    public static final RegistryObject<Block> SILO = registerBlock("silo",
            p -> new SiloBlock(p));

    public static final RegistryObject<Block> FERT_SPREADER = registerBlock("fertilizer_spreader",
            p -> new FertilizerSpreaderBlock(p));

    public static final RegistryObject<Block> TERRACOTTA_PLANTER = registerBlock("terracotta_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> BLACK_TERRACOTTA_PLANTER = registerBlock("black_terracotta_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> BLUE_TERRACOTTA_PLANTER = registerBlock("blue_terracotta_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> BROWN_TERRACOTTA_PLANTER = registerBlock("brown_terracotta_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> CYAN_TERRACOTTA_PLANTER = registerBlock("cyan_terracotta_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> GRAY_TERRACOTTA_PLANTER = registerBlock("gray_terracotta_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> GREEN_TERRACOTTA_PLANTER = registerBlock("green_terracotta_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> LIGHT_BLUE_TERRACOTTA_PLANTER = registerBlock("light_blue_terracotta_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> LIGHT_GRAY_TERRACOTTA_PLANTER = registerBlock("light_gray_terracotta_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> LIME_TERRACOTTA_PLANTER = registerBlock("lime_terracotta_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> MAGENTA_TERRACOTTA_PLANTER = registerBlock("magenta_terracotta_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> ORANGE_TERRACOTTA_PLANTER = registerBlock("orange_terracotta_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> PINK_TERRACOTTA_PLANTER = registerBlock("pink_terracotta_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> PURPLE_TERRACOTTA_PLANTER = registerBlock("purple_terracotta_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> RED_TERRACOTTA_PLANTER = registerBlock("red_terracotta_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> WHITE_TERRACOTTA_PLANTER = registerBlock("white_terracotta_planter",
            p -> new PlanterBlock(p));

    public static final RegistryObject<Block> YELLOW_TERRACOTTA_PLANTER = registerBlock("yellow_terracotta_planter",
            p -> new PlanterBlock(p));

    private static BlockBehaviour.Properties wood() {
        return BlockBehaviour.Properties.of()
                .strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion();
    }

    private static BlockBehaviour.Properties machine() {
        return BlockBehaviour.Properties.of()
                .strength(2.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties soft() {
        return BlockBehaviour.Properties.of()
                .strength(1.0F, 3.0F).sound(SoundType.MOSS).noOcclusion();
    }

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> factory) {
        RegistryObject<T> toReturn = BLOCKS.register(name, () -> factory.apply(defaultPropertiesFor(name)));
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static BlockBehaviour.Properties defaultPropertiesFor(String name) {
        if (name.equals("compacted_biomass_block")) {
            return BlockBehaviour.Properties.of()
                    .strength(1.0F, 3.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops();
        }
        if (name.equals("infused_farmland")) {
            return soft().randomTicks();
        }
        if (name.equals("mulch")) {
            return soft();
        }
        if (name.endsWith("_planter") && !name.startsWith("basic_")) {
            return machine();
        }
        if (name.startsWith("basic_") && name.endsWith("_planter")) {
            return wood();
        }
        return machine();
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ATEItems.ITEMS.register(name, () -> {

            if (name.equals("capacitor_tier1") || name.equals("capacitor_tier2") || name.equals("capacitor_tier3")) {
                return new BlockItem(block.get(), new Item.Properties()) {

                    @Override
                    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                        int storedEnergy = ATEDataComponents.getStoredEnergy(stack);

                        if (storedEnergy > 0) {
                            NumberFormat format = NumberFormat.getNumberInstance(Locale.US);

                            tooltip.add(Component.translatable("tooltip.community_agritechevolved.capacitor.stored_energy", format.format(storedEnergy)).withStyle(ChatFormatting.GOLD));
                        }
                    }
                };
            }

            if (name.equals("compacted_biomass_block")) {
                return new BlockItem(block.get(), new Item.Properties()) {

                    @Override
                    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
                        int totalFE = Config.getBurnerCompactedBiomassBlockFeValue();
                        int burnDuration = Config.getBurnerCompactedBiomassBlockBurnDuration();
                        if (Screen.hasShiftDown()) {
                            tooltip.add(Component.translatable("tooltip.community_agritechevolved.compacted_biomass.fe_generation", fmt.format(totalFE)).withStyle(ChatFormatting.GREEN));
                            if (burnDuration > 0) {
                                double burnSeconds = burnDuration / 20.0D;
                                tooltip.add(Component.translatable("tooltip.community_agritechevolved.fuel.burn_duration", String.format("%.1f", burnSeconds)).withStyle(ChatFormatting.AQUA));
                                tooltip.add(Component.translatable("tooltip.community_agritechevolved.fuel.fe_per_second", fmt.format(Math.round(totalFE / burnSeconds))).withStyle(ChatFormatting.YELLOW));
                            }
                        } else {
                            tooltip.add(Component.translatable("tooltip.community_agritechevolved.crude_fuel.shift_info"));
                        }
                    }
                };
            }

            if (name.equals("infused_farmland")) {
                return new BlockItem(block.get(), new Item.Properties()) {
                    @Override
                    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                        tooltip.add(Component.translatable("tooltip.community_agritechevolved.infused_farmland.growth_boost").withStyle(ChatFormatting.GREEN));
                        tooltip.add(Component.translatable("tooltip.community_agritechevolved.infused_farmland.hoe_craft").withStyle(ChatFormatting.GRAY));
                        tooltip.add(Component.translatable("tooltip.community_agritechevolved.infused_farmland.hoe_craft2").withStyle(ChatFormatting.GRAY));
                    }
                };
            }

            if (name.equals("mulch")) {
                return new BlockItem(block.get(), new Item.Properties()) {
                    @Override
                    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                        tooltip.add(Component.translatable("tooltip.community_agritechevolved.mulch.growth_boost").withStyle(ChatFormatting.GREEN));
                        tooltip.add(Component.translatable("tooltip.community_agritechevolved.mulch.hoe_craft").withStyle(ChatFormatting.GRAY));
                    }
                };
            }

            if (name.startsWith("basic_") && name.endsWith("_planter")) {
                return new BlockItem(block.get(), new Item.Properties()) {
                    @Override
                    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                        tooltip.add(Component.translatable("tooltip.community_agritechevolved.planter.till").withStyle(ChatFormatting.GRAY));
                        tooltip.add(Component.translatable("tooltip.community_agritechevolved.planter.fertilize").withStyle(ChatFormatting.GRAY));
                    }
                };
            }

            if (name.equals("advanced_planter")) {
                return new BlockItem(block.get(), new Item.Properties()) {
                    @Override
                    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                        tooltip.add(Component.translatable("tooltip.community_agritechevolved.planter.till").withStyle(ChatFormatting.GRAY));
                    }
                };
            }

            if (name.equals("composter")) {
                return new BlockItem(block.get(), new Item.Properties()) {
                    @Override
                    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                        tooltip.add(Component.translatable("tooltip.community_agritechevolved.composting_info").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
                    }
                };
            }

            return new BlockItem(block.get(), new Item.Properties());
        });
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
