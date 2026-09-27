package com.misterd.agritechevolved;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.slf4j.Logger;

public class Config {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.DoubleValue SPEED_MODULE_MK1_MULTIPLIER;
    public static ForgeConfigSpec.DoubleValue SPEED_MODULE_MK1_POWER_MULTIPLIER;
    public static ForgeConfigSpec.DoubleValue SPEED_MODULE_MK2_MULTIPLIER;
    public static ForgeConfigSpec.DoubleValue SPEED_MODULE_MK2_POWER_MULTIPLIER;
    public static ForgeConfigSpec.DoubleValue SPEED_MODULE_MK3_MULTIPLIER;
    public static ForgeConfigSpec.DoubleValue SPEED_MODULE_MK3_POWER_MULTIPLIER;
    public static ForgeConfigSpec.DoubleValue YIELD_MODULE_MK1_MULTIPLIER;
    public static ForgeConfigSpec.DoubleValue YIELD_MODULE_MK1_SPEED_PENALTY;
    public static ForgeConfigSpec.DoubleValue YIELD_MODULE_MK2_MULTIPLIER;
    public static ForgeConfigSpec.DoubleValue YIELD_MODULE_MK2_SPEED_PENALTY;
    public static ForgeConfigSpec.DoubleValue YIELD_MODULE_MK3_MULTIPLIER;
    public static ForgeConfigSpec.DoubleValue YIELD_MODULE_MK3_SPEED_PENALTY;

    public static ForgeConfigSpec.IntValue PLANTER_BASE_POWER_CONSUMPTION;
    public static ForgeConfigSpec.IntValue PLANTER_BASE_PROCESSING_TIME;
    public static ForgeConfigSpec.IntValue PLANTER_ENERGY_BUFFER;
    public static ForgeConfigSpec.IntValue ADVANCED_PLANTER_BASE_PROCESSING_TIME;

    public static ForgeConfigSpec.DoubleValue CLOCHE_SPEED_MULTIPLIER;
    public static ForgeConfigSpec.DoubleValue CLOCHE_YIELD_MULTIPLIER;

    public static ForgeConfigSpec.IntValue COMPOSTER_BASE_POWER_CONSUMPTION;
    public static ForgeConfigSpec.IntValue COMPOSTER_BASE_PROCESSING_TIME;
    public static ForgeConfigSpec.IntValue COMPOSTER_ENERGY_BUFFER;

    public static ForgeConfigSpec.IntValue BURNER_ENERGY_BUFFER;
    public static ForgeConfigSpec.IntValue BURNER_BIOMASS_FE_VALUE;
    public static ForgeConfigSpec.IntValue BURNER_BIOMASS_BURN_DURATION;
    public static ForgeConfigSpec.IntValue BURNER_COMPACTED_BIOMASS_FE_VALUE;
    public static ForgeConfigSpec.IntValue BURNER_COMPACTED_BIOMASS_BURN_DURATION;
    public static ForgeConfigSpec.IntValue BURNER_COMPACTED_BIOMASS_BLOCK_FE_VALUE;
    public static ForgeConfigSpec.IntValue BURNER_COMPACTED_BIOMASS_BLOCK_BURN_DURATION;
    public static ForgeConfigSpec.IntValue BURNER_CRUDE_BIOMASS_FE_VALUE;
    public static ForgeConfigSpec.IntValue BURNER_CRUDE_BIOMASS_BURN_DURATION;

    public static ForgeConfigSpec.IntValue CAPACITOR_T1_BUFFER;
    public static ForgeConfigSpec.IntValue CAPACITOR_T1_TRANSFER_RATE;
    public static ForgeConfigSpec.IntValue CAPACITOR_T2_BUFFER;
    public static ForgeConfigSpec.IntValue CAPACITOR_T2_TRANSFER_RATE;
    public static ForgeConfigSpec.IntValue CAPACITOR_T3_BUFFER;
    public static ForgeConfigSpec.IntValue CAPACITOR_T3_TRANSFER_RATE;

    public static ForgeConfigSpec.IntValue SILO_BASE_RANGE;
    public static ForgeConfigSpec.IntValue SILO_ENERGY_BUFFER;
    public static ForgeConfigSpec.IntValue SILO_BASE_POWER_CONSUMPTION;
    public static ForgeConfigSpec.IntValue SILO_PULL_INTERVAL;

    public static ForgeConfigSpec.IntValue FERTILIZER_SPREADER_BASE_RANGE;
    public static ForgeConfigSpec.IntValue FERTILIZER_SPREADER_ENERGY_BUFFER;
    public static ForgeConfigSpec.IntValue FERTILIZER_SPREADER_BASE_POWER_CONSUMPTION;
    public static ForgeConfigSpec.IntValue FERTILIZER_SPREADER_PUSH_INTERVAL;
    public static ForgeConfigSpec.IntValue FERTILIZER_SPREADER_PUSH_AMOUNT;

    public static void register() {
        moduleConfig();
        machineConfig();
        COMMON_CONFIG = COMMON_BUILDER.build();
        SPEC = COMMON_CONFIG;
        net.minecraftforge.fml.ModLoadingContext.get()
                .registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG);
    }

    private static void moduleConfig() {
        COMMON_BUILDER.comment("Module Effectiveness Settings").push("modules");

        COMMON_BUILDER.comment("Speed Module Configuration").push("speed_modules");
        SPEED_MODULE_MK1_MULTIPLIER = COMMON_BUILDER.comment("Speed multiplier for Speed Module-MK1").defineInRange("mk1_speed_multiplier", 1.5D, 0.1D, 10.0D);
        SPEED_MODULE_MK1_POWER_MULTIPLIER = COMMON_BUILDER.comment("Power consumption multiplier for Speed Module-MK1").defineInRange("mk1_power_multiplier", 1.5D, 0.1D, 10.0D);
        SPEED_MODULE_MK2_MULTIPLIER = COMMON_BUILDER.comment("Speed multiplier for Speed Module-MK2").defineInRange("mk2_speed_multiplier", 1.75D, 0.1D, 10.0D);
        SPEED_MODULE_MK2_POWER_MULTIPLIER = COMMON_BUILDER.comment("Power consumption multiplier for Speed Module-MK2").defineInRange("mk2_power_multiplier", 1.75D, 0.1D, 10.0D);
        SPEED_MODULE_MK3_MULTIPLIER = COMMON_BUILDER.comment("Speed multiplier for Speed Module-MK3").defineInRange("mk3_speed_multiplier", 2.0D, 0.1D, 10.0D);
        SPEED_MODULE_MK3_POWER_MULTIPLIER = COMMON_BUILDER.comment("Power consumption multiplier for Speed Module-MK3").defineInRange("mk3_power_multiplier", 2.0D, 0.1D, 10.0D);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Yield Module Configuration").push("yield_modules");
        YIELD_MODULE_MK1_MULTIPLIER = COMMON_BUILDER.comment("Yield multiplier for Yield Module-MK1").defineInRange("mk1_yield_multiplier", 1.5D, 0.1D, 10.0D);
        YIELD_MODULE_MK1_SPEED_PENALTY = COMMON_BUILDER.comment("Speed penalty for Yield Module-MK1 (multiplier)").defineInRange("mk1_speed_penalty", 0.95D, 0.1D, 1.0D);
        YIELD_MODULE_MK2_MULTIPLIER = COMMON_BUILDER.comment("Yield multiplier for Yield Module-MK2").defineInRange("mk2_yield_multiplier", 1.75D, 0.1D, 10.0D);
        YIELD_MODULE_MK2_SPEED_PENALTY = COMMON_BUILDER.comment("Speed penalty for Yield Module-MK2 (multiplier)").defineInRange("mk2_speed_penalty", 0.90D, 0.1D, 1.0D);
        YIELD_MODULE_MK3_MULTIPLIER = COMMON_BUILDER.comment("Yield multiplier for Yield Module-MK3").defineInRange("mk3_yield_multiplier", 2.0D, 0.1D, 10.0D);
        YIELD_MODULE_MK3_SPEED_PENALTY = COMMON_BUILDER.comment("Speed penalty for Yield Module-MK3 (multiplier)").defineInRange("mk3_speed_penalty", 0.75D, 0.1D, 1.0D);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.pop();
    }

    private static void machineConfig() {
        COMMON_BUILDER.comment("Machine Settings").push("machines");
        planterConfig();
        composterConfig();
        burnerConfig();
        capacitorConfig();
        siloConfig();
        fertilizerSpreaderConfig();
        COMMON_BUILDER.pop();
    }

    private static void planterConfig() {
        COMMON_BUILDER.comment("Advanced Planter Configuration").push("advanced_planter");
        PLANTER_BASE_POWER_CONSUMPTION = COMMON_BUILDER.comment("Base power consumption for Advanced Planter (FE/t)").defineInRange("base_power_consumption", 128, 1, 100000);
        PLANTER_BASE_PROCESSING_TIME = COMMON_BUILDER.comment("Base processing time for basic planters (ticks)").defineInRange("base_processing_time", 1200, 1, 72000);
        ADVANCED_PLANTER_BASE_PROCESSING_TIME = COMMON_BUILDER.comment("Base processing time for the Advanced Planter (ticks)").defineInRange("advanced_base_processing_time", 600, 1, 72000);
        PLANTER_ENERGY_BUFFER = COMMON_BUILDER.comment("Energy buffer capacity for Advanced Planter (FE)").defineInRange("energy_buffer", 100000, 1000, 10000000);
        CLOCHE_SPEED_MULTIPLIER = COMMON_BUILDER.comment("Speed multiplier applied when a cloche is attached to a planter").defineInRange("cloche_speed_multiplier", 1.15D, 0.1D, 10.0D);
        CLOCHE_YIELD_MULTIPLIER = COMMON_BUILDER.comment("Yield multiplier applied when a cloche is attached to a planter").defineInRange("cloche_yield_multiplier", 1.10D, 0.1D, 10.0D);
        COMMON_BUILDER.pop();
    }

    private static void composterConfig() {
        COMMON_BUILDER.comment("Composter Configuration").push("composter");
        COMPOSTER_BASE_POWER_CONSUMPTION = COMMON_BUILDER.comment("Base power consumption for Composter (FE/t)").defineInRange("base_power_consumption", 128, 1, 100000);
        COMPOSTER_BASE_PROCESSING_TIME = COMMON_BUILDER.comment("Base processing time for Composter (ticks)").defineInRange("base_processing_time", 600, 1, 72000);
        COMPOSTER_ENERGY_BUFFER = COMMON_BUILDER.comment("Energy buffer capacity for Composter (FE)").defineInRange("energy_buffer", 100000, 1000, 10000000);
        COMMON_BUILDER.pop();
    }

    private static void burnerConfig() {
        COMMON_BUILDER.comment("Burner Configuration").push("burner");
        BURNER_ENERGY_BUFFER = COMMON_BUILDER.comment("Energy buffer capacity for Burner (FE)").defineInRange("energy_buffer", 100000, 1000, 10000000);
        BURNER_BIOMASS_FE_VALUE = COMMON_BUILDER.comment("FE generated per biomass item").defineInRange("biomass_fe_value", 2500, 100, 100000);
        BURNER_BIOMASS_BURN_DURATION = COMMON_BUILDER.comment("Burn duration for biomass in ticks (20 ticks = 1 second)").defineInRange("biomass_burn_duration", 100, 20, 72000);
        BURNER_COMPACTED_BIOMASS_FE_VALUE = COMMON_BUILDER.comment("FE generated per compacted biomass item").defineInRange("compacted_biomass_fe_value", 22500, 1000, 1000000);
        BURNER_COMPACTED_BIOMASS_BURN_DURATION = COMMON_BUILDER.comment("Burn duration for compacted biomass in ticks (20 ticks = 1 second)").defineInRange("compacted_biomass_burn_duration", 180, 20, 72000);
        BURNER_COMPACTED_BIOMASS_BLOCK_FE_VALUE = COMMON_BUILDER.comment("FE generated per compacted biomass block").defineInRange("compacted_biomass_block_fe_value", 225000, 1000, 1000000);
        BURNER_COMPACTED_BIOMASS_BLOCK_BURN_DURATION = COMMON_BUILDER.comment("Burn duration for compacted biomass block in ticks (20 ticks = 1 second)").defineInRange("compacted_biomass_block_burn_duration", 1800, 20, 72000);
        BURNER_CRUDE_BIOMASS_FE_VALUE = COMMON_BUILDER.comment("FE generated per crude biomass item").defineInRange("crude_biomass_fe_value", 250, 50, 50000);
        BURNER_CRUDE_BIOMASS_BURN_DURATION = COMMON_BUILDER.comment("Burn duration for crude biomass in ticks (20 ticks = 1 second)").defineInRange("crude_biomass_burn_duration", 50, 20, 72000);
        COMMON_BUILDER.pop();
    }

    private static void capacitorConfig() {
        COMMON_BUILDER.comment("Capacitor Configuration").push("capacitors");
        COMMON_BUILDER.comment("Tier 1 Capacitor").push("tier_1");
        CAPACITOR_T1_BUFFER = COMMON_BUILDER.comment("Energy buffer capacity for T1 Capacitor (FE)").defineInRange("buffer_capacity", 500000, 10000, 100000000);
        CAPACITOR_T1_TRANSFER_RATE = COMMON_BUILDER.comment("Energy transfer rate for T1 Capacitor (FE/t)").defineInRange("transfer_rate", 512, 1, 100000);
        COMMON_BUILDER.pop();
        COMMON_BUILDER.comment("Tier 2 Capacitor").push("tier_2");
        CAPACITOR_T2_BUFFER = COMMON_BUILDER.comment("Energy buffer capacity for T2 Capacitor (FE)").defineInRange("buffer_capacity", 1000000, 10000, 100000000);
        CAPACITOR_T2_TRANSFER_RATE = COMMON_BUILDER.comment("Energy transfer rate for T2 Capacitor (FE/t)").defineInRange("transfer_rate", 2048, 1, 100000);
        COMMON_BUILDER.pop();
        COMMON_BUILDER.comment("Tier 3 Capacitor").push("tier_3");
        CAPACITOR_T3_BUFFER = COMMON_BUILDER.comment("Energy buffer capacity for T3 Capacitor (FE)").defineInRange("buffer_capacity", 4000000, 10000, 100000000);
        CAPACITOR_T3_TRANSFER_RATE = COMMON_BUILDER.comment("Energy transfer rate for T3 Capacitor (FE/t)").defineInRange("transfer_rate", 8192, 1, 100000);
        COMMON_BUILDER.pop();
        COMMON_BUILDER.pop();
    }

    private static void siloConfig() {
        COMMON_BUILDER.comment("Silo Configuration").push("silo");
        SILO_BASE_RANGE = COMMON_BUILDER.comment("Base range for Silo (blocks)").defineInRange("base_range", 16, 4, 64);
        SILO_ENERGY_BUFFER = COMMON_BUILDER.comment("Energy buffer capacity for Silo (FE)").defineInRange("energy_buffer", 100000, 1000, 1000000);
        SILO_BASE_POWER_CONSUMPTION = COMMON_BUILDER.comment("Base power consumption for Silo (FE/t)").defineInRange("base_power_consumption", 128, 1, 100000);
        SILO_PULL_INTERVAL = COMMON_BUILDER.comment("How often the Silo pulls from planters in range (ticks)").defineInRange("pull_interval", 60, 2, 6000);
        COMMON_BUILDER.pop();
    }

    private static void fertilizerSpreaderConfig() {
        COMMON_BUILDER.comment("Fertilizer Spreader Configuration").push("fertilizer_spreader");
        FERTILIZER_SPREADER_BASE_RANGE = COMMON_BUILDER.comment("Base range for Fertilizer Spreader (blocks)").defineInRange("base_range", 16, 4, 64);
        FERTILIZER_SPREADER_ENERGY_BUFFER = COMMON_BUILDER.comment("Energy buffer capacity for Fertilizer Spreader (FE)").defineInRange("energy_buffer", 100000, 1000, 1000000);
        FERTILIZER_SPREADER_BASE_POWER_CONSUMPTION = COMMON_BUILDER.comment("Base power consumption for Fertilizer Spreader (FE/t)").defineInRange("base_power_consumption", 128, 1, 100000);
        FERTILIZER_SPREADER_PUSH_INTERVAL = COMMON_BUILDER.comment("How often the Fertilizer Spreader pushes to planters in range (ticks)").defineInRange("push_interval", 60, 2, 6000);
        FERTILIZER_SPREADER_PUSH_AMOUNT = COMMON_BUILDER.comment("How many fertilizer items the Fertilizer Spreader pushes per planter per push").defineInRange("push_amount", 8, 1, 64);
        COMMON_BUILDER.pop();
    }

    public static double getSpeedModuleMk1Multiplier() { return SPEED_MODULE_MK1_MULTIPLIER.get(); }
    public static double getSpeedModuleMk1PowerMultiplier() { return SPEED_MODULE_MK1_POWER_MULTIPLIER.get(); }
    public static double getSpeedModuleMk2Multiplier() { return SPEED_MODULE_MK2_MULTIPLIER.get(); }
    public static double getSpeedModuleMk2PowerMultiplier() { return SPEED_MODULE_MK2_POWER_MULTIPLIER.get(); }
    public static double getSpeedModuleMk3Multiplier() { return SPEED_MODULE_MK3_MULTIPLIER.get(); }
    public static double getSpeedModuleMk3PowerMultiplier() { return SPEED_MODULE_MK3_POWER_MULTIPLIER.get(); }
    public static double getYieldModuleMk1Multiplier() { return YIELD_MODULE_MK1_MULTIPLIER.get(); }
    public static double getYieldModuleMk1SpeedPenalty() { return YIELD_MODULE_MK1_SPEED_PENALTY.get(); }
    public static double getYieldModuleMk2Multiplier() { return YIELD_MODULE_MK2_MULTIPLIER.get(); }
    public static double getYieldModuleMk2SpeedPenalty() { return YIELD_MODULE_MK2_SPEED_PENALTY.get(); }
    public static double getYieldModuleMk3Multiplier() { return YIELD_MODULE_MK3_MULTIPLIER.get(); }
    public static double getYieldModuleMk3SpeedPenalty() { return YIELD_MODULE_MK3_SPEED_PENALTY.get(); }

    public static double getClocheSpeedMultiplier() { return CLOCHE_SPEED_MULTIPLIER.get(); }
    public static double getClocheYieldMultiplier() { return CLOCHE_YIELD_MULTIPLIER.get(); }

    public static int getPlanterBasePowerConsumption() { return PLANTER_BASE_POWER_CONSUMPTION.get(); }
    public static int getPlanterBaseProcessingTime() { return PLANTER_BASE_PROCESSING_TIME.get(); }
    public static int getAdvancedPlanterBaseProcessingTime() { return ADVANCED_PLANTER_BASE_PROCESSING_TIME.get(); }
    public static int getPlanterEnergyBuffer() { return PLANTER_ENERGY_BUFFER.get(); }

    public static int getComposterBasePowerConsumption() { return COMPOSTER_BASE_POWER_CONSUMPTION.get(); }
    public static int getComposterBaseProcessingTime() { return COMPOSTER_BASE_PROCESSING_TIME.get(); }
    public static int getComposterEnergyBuffer() { return COMPOSTER_ENERGY_BUFFER.get(); }

    public static int getBurnerEnergyBuffer() { return BURNER_ENERGY_BUFFER.get(); }
    public static int getBurnerBiomassFeValue() { return BURNER_BIOMASS_FE_VALUE.get(); }
    public static int getBurnerBiomassBurnDuration() { return BURNER_BIOMASS_BURN_DURATION.get(); }
    public static int getBurnerCompactedBiomassFeValue() { return BURNER_COMPACTED_BIOMASS_FE_VALUE.get(); }
    public static int getBurnerCompactedBiomassBurnDuration() { return BURNER_COMPACTED_BIOMASS_BURN_DURATION.get(); }
    public static int getBurnerCompactedBiomassBlockFeValue() { return BURNER_COMPACTED_BIOMASS_BLOCK_FE_VALUE.get(); }
    public static int getBurnerCompactedBiomassBlockBurnDuration() { return BURNER_COMPACTED_BIOMASS_BLOCK_BURN_DURATION.get(); }
    public static int getBurnerCrudeBiomassFeValue() { return BURNER_CRUDE_BIOMASS_FE_VALUE.get(); }
    public static int getBurnerCrudeBiomassBurnDuration() { return BURNER_CRUDE_BIOMASS_BURN_DURATION.get(); }

    public static int getCapacitorT1Buffer() { return CAPACITOR_T1_BUFFER.get(); }
    public static int getCapacitorT1TransferRate() { return CAPACITOR_T1_TRANSFER_RATE.get(); }
    public static int getCapacitorT2Buffer() { return CAPACITOR_T2_BUFFER.get(); }
    public static int getCapacitorT2TransferRate() { return CAPACITOR_T2_TRANSFER_RATE.get(); }
    public static int getCapacitorT3Buffer() { return CAPACITOR_T3_BUFFER.get(); }
    public static int getCapacitorT3TransferRate() { return CAPACITOR_T3_TRANSFER_RATE.get(); }

    public static int getSiloBaseRange() { return SILO_BASE_RANGE.get(); }
    public static int getSiloEnergyBuffer() { return SILO_ENERGY_BUFFER.get(); }
    public static int getSiloBasePowerConsumption() { return SILO_BASE_POWER_CONSUMPTION.get(); }
    public static int getSiloPullInterval() { return SILO_PULL_INTERVAL.get(); }

    public static int getFertilizerSpreaderBaseRange() { return FERTILIZER_SPREADER_BASE_RANGE.get(); }
    public static int getFertilizerSpreaderEnergyBuffer() { return FERTILIZER_SPREADER_ENERGY_BUFFER.get(); }
    public static int getFertilizerSpreaderBasePowerConsumption() { return FERTILIZER_SPREADER_BASE_POWER_CONSUMPTION.get(); }
    public static int getFertilizerSpreaderPushInterval() { return FERTILIZER_SPREADER_PUSH_INTERVAL.get(); }
    public static int getFertilizerSpreaderPushAmount() { return FERTILIZER_SPREADER_PUSH_AMOUNT.get(); }

    public static void loadConfig() {
        LOGGER.info("AgriTech: Evolved config reloaded");
    }

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        LOGGER.info("AgriTech: Evolved configuration loaded");
    }
}
