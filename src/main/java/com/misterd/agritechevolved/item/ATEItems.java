package com.misterd.agritechevolved.item;

import com.misterd.agritechevolved.AgritechEvolved;
import com.misterd.agritechevolved.Config;
import com.misterd.agritechevolved.item.custom.ClocheItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ATEItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, AgritechEvolved.MODID);

    public static final RegistryObject<Item> SM_MK1 = ITEMS.register("sm_mk1",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                    addSpeedModuleTooltip(tooltip,
                            Config.getSpeedModuleMk1Multiplier(),
                            Config.getSpeedModuleMk1PowerMultiplier());
                }
            });

    public static final RegistryObject<Item> SM_MK2 = ITEMS.register("sm_mk2",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                    addSpeedModuleTooltip(tooltip,
                            Config.getSpeedModuleMk2Multiplier(),
                            Config.getSpeedModuleMk2PowerMultiplier());
                }
            });

    public static final RegistryObject<Item> SM_MK3 = ITEMS.register("sm_mk3",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                    addSpeedModuleTooltip(tooltip,
                            Config.getSpeedModuleMk3Multiplier(),
                            Config.getSpeedModuleMk3PowerMultiplier());
                }
            });

    public static final RegistryObject<Item> YM_MK1 = ITEMS.register("ym_mk1",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                    addYieldModuleTooltip(tooltip,
                            Config.getYieldModuleMk1Multiplier(),
                            Config.getYieldModuleMk1SpeedPenalty());
                }
            });

    public static final RegistryObject<Item> YM_MK2 = ITEMS.register("ym_mk2",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                    addYieldModuleTooltip(tooltip,
                            Config.getYieldModuleMk2Multiplier(),
                            Config.getYieldModuleMk2SpeedPenalty());
                }
            });

    public static final RegistryObject<Item> YM_MK3 = ITEMS.register("ym_mk3",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                    addYieldModuleTooltip(tooltip,
                            Config.getYieldModuleMk3Multiplier(),
                            Config.getYieldModuleMk3SpeedPenalty());
                }
            });

    public static final RegistryObject<Item> RM_MK1 = ITEMS.register("rm_mk1",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                    addShiftTooltip(tooltip, "tooltip.community_agritechevolved.module.rm_mk1");
                }
            });

    public static final RegistryObject<Item> RM_MK2 = ITEMS.register("rm_mk2",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                    addShiftTooltip(tooltip, "tooltip.community_agritechevolved.module.rm_mk2");
                }
            });

    public static final RegistryObject<Item> RM_MK3 = ITEMS.register("rm_mk3",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                    addShiftTooltip(tooltip, "tooltip.community_agritechevolved.module.rm_mk3");
                }
            });

    public static final RegistryObject<Item> CRUDE_BIOMASS = ITEMS.register("crude_biomass",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.community_agritechevolved.crude_biomass"));
                    addFuelTooltip(tooltip,
                            Config.getBurnerCrudeBiomassRfValue(),
                            Config.getBurnerCrudeBiomassBurnDuration(),
                            50,
                            "tooltip.community_agritechevolved.crude_biomass.rf_generation",
                            "tooltip.community_agritechevolved.crude_fuel.shift_info");
                    tooltip.add(Component.translatable("tooltip.community_agritechevolved.crude_biomass.inefficient").withStyle(ChatFormatting.RED));
                }
            });

    public static final RegistryObject<Item> BIOMASS = ITEMS.register("biomass",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.community_agritechevolved.biomass"));
                    addFuelTooltip(tooltip,
                            Config.getBurnerBiomassRfValue(),
                            Config.getBurnerBiomassBurnDuration(),
                            100,
                            "tooltip.community_agritechevolved.biomass.rf_generation",
                            "tooltip.community_agritechevolved.fuel.shift_info");
                }
            });

    public static final RegistryObject<Item> COMPACTED_BIOMASS = ITEMS.register("compacted_biomass",
            () -> new Item(new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.community_agritechevolved.compacted"));
                    addFuelTooltip(tooltip,
                            Config.getBurnerCompactedBiomassRfValue(),
                            Config.getBurnerCompactedBiomassBurnDuration(),
                            180,
                            "tooltip.community_agritechevolved.compacted_biomass.rf_generation",
                            "tooltip.community_agritechevolved.fuel.shift_info");
                }
            });

    public static final RegistryObject<Item> CLOCHE = ITEMS.register("cloche_dome",
            () -> new ClocheItem(new Item.Properties()));

    private static void addShiftTooltip(List<Component> tooltip, String shiftKey) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable(shiftKey));
        } else {
            tooltip.add(Component.translatable("tooltip.community_agritechevolved.module.shift_info"));
        }
    }

    private static void addSpeedModuleTooltip(List<Component> tooltip, double speedMultiplier, double powerMultiplier) {
        if (Screen.hasShiftDown()) {
            int speedBoost = (int) Math.round((speedMultiplier - 1.0D) * 100.0D);
            int powerIncrease = (int) Math.round((powerMultiplier - 1.0D) * 100.0D);
            tooltip.add(Component.translatable("tooltip.community_agritechevolved.module.speed_boost", speedBoost));
            tooltip.add(Component.translatable("tooltip.community_agritechevolved.module.power_increase", powerIncrease));
        } else {
            tooltip.add(Component.translatable("tooltip.community_agritechevolved.module.shift_info"));
        }
    }

    private static void addYieldModuleTooltip(List<Component> tooltip, double yieldMultiplier, double speedPenalty) {
        if (Screen.hasShiftDown()) {
            int yieldBoost = (int) Math.round((yieldMultiplier - 1.0D) * 100.0D);
            int speedReduction = (int) Math.round((1.0D - speedPenalty) * 100.0D);
            tooltip.add(Component.translatable("tooltip.community_agritechevolved.module.yield_boost", yieldBoost));
            tooltip.add(Component.translatable("tooltip.community_agritechevolved.module.speed_reduction", speedReduction));
        } else {
            tooltip.add(Component.translatable("tooltip.community_agritechevolved.module.shift_info"));
        }
    }

    private static void addFuelTooltip(List<Component> tooltip, int baseRF, int burnDuration, int baseDuration,
                                       String rfKey, String shiftInfoKey) {
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
        int actualRF = (int) ((float) baseRF * ((float) burnDuration / baseDuration));
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable(rfKey, fmt.format(actualRF)).withStyle(ChatFormatting.GREEN));
            double burnSeconds = burnDuration / 20.0D;
            tooltip.add(Component.translatable("tooltip.community_agritechevolved.fuel.burn_duration", String.format("%.1f", burnSeconds)).withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.community_agritechevolved.fuel.rf_per_second", fmt.format((int) Math.round(actualRF / burnSeconds))).withStyle(ChatFormatting.YELLOW));
            if (burnDuration != baseDuration) {
                int pct = (int) Math.round(((double) burnDuration / baseDuration - 1.0D) * 100.0D);
                if (pct > 0) tooltip.add(Component.translatable("tooltip.community_agritechevolved.fuel.duration_bonus", pct).withStyle(ChatFormatting.GREEN));
                else if (pct < 0) tooltip.add(Component.translatable("tooltip.community_agritechevolved.fuel.duration_penalty", Math.abs(pct)).withStyle(ChatFormatting.RED));
            }
        } else {
            tooltip.add(Component.translatable(shiftInfoKey));
        }
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
