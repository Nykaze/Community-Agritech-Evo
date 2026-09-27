package com.misterd.agritechevolved.compat.jade;

import com.misterd.agritechevolved.trait.PlantTraits;
import com.misterd.agritechevolved.trait.TraitLevel;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.text.NumberFormat;
import java.util.Locale;

public enum PlanterClientProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public ResourceLocation getUid() {
        return PlanterProvider.UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.getBoolean("hasCrop")) return;

        String cropName = data.getString("cropName");
        int currentStage = data.getInt("currentStage");
        int maxStage = data.getInt("maxStage");
        float progressPercent = data.getFloat("progressPercent");
        String soilName = data.getString("soilName");
        float growthModifier = data.getFloat("growthModifier");

        if (progressPercent >= 100.0F) {
            tooltip.add(Component.translatable("jade.community_agritechevolved.crop_ready", cropName)
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        } else {
            tooltip.add(Component.translatable("jade.community_agritechevolved.crop_progress",
                            cropName, currentStage, maxStage, Math.round(progressPercent))
                    .withStyle(ChatFormatting.DARK_GREEN));
        }

        tooltip.add(Component.translatable("jade.community_agritechevolved.soil_info",
                        soilName, String.format("%.2fx", growthModifier))
                .withStyle(ChatFormatting.GRAY));

        if (data.getBoolean("hasTraits")) {
            for (PlantTraits.TraitKind kind : PlantTraits.TraitKind.values()) {
                int id = switch (kind) {
                    case GROWTH -> data.getInt("traitGrowth");
                    case YIELD -> data.getInt("traitYield");
                    case RESISTANCE -> data.getInt("traitResistance");
                    case MUTABILITY -> data.getInt("traitMutability");
                };
                tooltip.add(Component.translatable("gui.community_agritechevolved.trait_" + kind.key(),
                                TraitLevel.byId(id).getDisplayName())
                        .withStyle(kind.color()));
            }
        }

        if (data.getBoolean("hasFertilizer")) {
            float fertSpeed = data.getFloat("fertilizerSpeedModifier");
            float fertYield = data.getFloat("fertilizerYieldModifier");
            tooltip.add(Component.translatable("jade.community_agritechevolved.fertilizer_info",
                            data.getString("fertilizerName"),
                            String.format("%.2fx", fertSpeed),
                            String.format("%.2fx", fertYield))
                    .withStyle(ChatFormatting.YELLOW));
        }

        if (data.getBoolean("isCloched")) {
            float clocheSpeed = data.getFloat("clocheSpeedModifier");
            float clocheYield = data.getFloat("clocheYieldModifier");
            tooltip.add(Component.translatable("jade.community_agritechevolved.cloche_installed",
                            String.format("%.2fx", clocheSpeed),
                            String.format("%.2fx", clocheYield))
                    .withStyle(ChatFormatting.AQUA));
        }

        if (data.getBoolean("isAdvanced")) {
            int energy = data.getInt("energyStored");
            int maxEnergy = data.getInt("maxEnergy");
            NumberFormat fmt = NumberFormat.getInstance(Locale.US);
            tooltip.add(Component.translatable("jade.community_agritechevolved.energy_info",
                            fmt.format(energy), fmt.format(maxEnergy))
                    .withStyle(ChatFormatting.LIGHT_PURPLE));

            if (data.getBoolean("hasModules")) {
                float moduleSpeed = data.getFloat("moduleSpeedModifier");
                float moduleYield = data.getFloat("moduleYieldModifier");
                tooltip.add(Component.translatable("jade.community_agritechevolved.module_info",
                                String.format("%.2fx", moduleSpeed),
                                String.format("%.2fx", moduleYield))
                        .withStyle(ChatFormatting.GOLD));
            }
        }
    }
}