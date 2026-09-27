package com.misterd.agritechevolved.client;

import com.misterd.agritechevolved.AgritechEvolved;
import com.misterd.agritechevolved.datamap.ATEDataMaps;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AgritechEvolved.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        var soilData = ATEDataMaps.getSoilModifier(event.getItemStack().getItem());
        var fertData = ATEDataMaps.getFertilizer(event.getItemStack().getItem());

        if (soilData == null && fertData == null) return;

        if (soilData != null) {
            event.getToolTip().add(
                    Component.translatable("tooltip.community_agritechevolved.soil_type")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            event.getToolTip().add(
                    Component.translatable("tooltip.community_agritechevolved.soil_growth_modifier",
                                    String.format("%.2fx", soilData.growthModifier()))
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }

        if (fertData != null) {
            event.getToolTip().add(
                    Component.translatable("tooltip.community_agritechevolved.fertilizer_type")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            event.getToolTip().add(
                    Component.translatable("tooltip.community_agritechevolved.fertilizer_speed",
                                    String.format("%.2fx", fertData.speedMultiplier()))
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            event.getToolTip().add(
                    Component.translatable("tooltip.community_agritechevolved.fertilizer_yield",
                                    String.format("%.2fx", fertData.yieldMultiplier()))
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
    }
}