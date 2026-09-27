package com.misterd.agritechevolved.blockentity;

import com.misterd.agritechevolved.AgritechEvolved;
import com.misterd.agritechevolved.block.ATEBlocks;
import com.misterd.agritechevolved.blockentity.custom.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ATEBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AgritechEvolved.MODID);

    public static final RegistryObject<BlockEntityType<PlanterBlockEntity>> PLANTER_BLOCK_BE =
            BLOCK_ENTITIES.register("planter_block_be", () -> BlockEntityType.Builder
                    .of(PlanterBlockEntity::new,
                            ATEBlocks.ACACIA_PLANTER.get(),
                            ATEBlocks.BAMBOO_PLANTER.get(),
                            ATEBlocks.BIRCH_PLANTER.get(),
                            ATEBlocks.CHERRY_PLANTER.get(),
                            ATEBlocks.CRIMSON_PLANTER.get(),
                            ATEBlocks.DARK_OAK_PLANTER.get(),
                            ATEBlocks.JUNGLE_PLANTER.get(),
                            ATEBlocks.MANGROVE_PLANTER.get(),
                            ATEBlocks.OAK_PLANTER.get(),
                            ATEBlocks.SPRUCE_PLANTER.get(),
                            ATEBlocks.WARPED_PLANTER.get(),
                            ATEBlocks.PALE_OAK_PLANTER.get(),
                            ATEBlocks.TERRACOTTA_PLANTER.get(),
                            ATEBlocks.BLACK_TERRACOTTA_PLANTER.get(),
                            ATEBlocks.BLUE_TERRACOTTA_PLANTER.get(),
                            ATEBlocks.BROWN_TERRACOTTA_PLANTER.get(),
                            ATEBlocks.CYAN_TERRACOTTA_PLANTER.get(),
                            ATEBlocks.GRAY_TERRACOTTA_PLANTER.get(),
                            ATEBlocks.GREEN_TERRACOTTA_PLANTER.get(),
                            ATEBlocks.LIGHT_BLUE_TERRACOTTA_PLANTER.get(),
                            ATEBlocks.LIGHT_GRAY_TERRACOTTA_PLANTER.get(),
                            ATEBlocks.LIME_TERRACOTTA_PLANTER.get(),
                            ATEBlocks.MAGENTA_TERRACOTTA_PLANTER.get(),
                            ATEBlocks.ORANGE_TERRACOTTA_PLANTER.get(),
                            ATEBlocks.PINK_TERRACOTTA_PLANTER.get(),
                            ATEBlocks.PURPLE_TERRACOTTA_PLANTER.get(),
                            ATEBlocks.RED_TERRACOTTA_PLANTER.get(),
                            ATEBlocks.WHITE_TERRACOTTA_PLANTER.get(),
                            ATEBlocks.YELLOW_TERRACOTTA_PLANTER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<AdvancedPlanterBlockEntity>> ADVANCED_PLANTER_BLOCK_BE =
            BLOCK_ENTITIES.register("advanced_planter_block_be", () -> BlockEntityType.Builder
                    .of(AdvancedPlanterBlockEntity::new, ATEBlocks.ADVANCED_PLANTER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<ComposterBlockEntity>> COMPOSTER_BE =
            BLOCK_ENTITIES.register("composter_be", () -> BlockEntityType.Builder
                    .of(ComposterBlockEntity::new, ATEBlocks.COMPOSTER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<CapacitorBlockEntity>> CAPACITOR_BE =
            BLOCK_ENTITIES.register("capacitor_be", () -> BlockEntityType.Builder
                    .of(CapacitorBlockEntity::new,
                            ATEBlocks.CAPACITOR_TIER_1.get(),
                            ATEBlocks.CAPACITOR_TIER_2.get(),
                            ATEBlocks.CAPACITOR_TIER_3.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<BiomassBurnerBlockEntity>> BURNER_BE =
            BLOCK_ENTITIES.register("burner_be", () -> BlockEntityType.Builder
                    .of(BiomassBurnerBlockEntity::new, ATEBlocks.BIOMASS_BURNER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<SiloBlockEntity>> SILO_BE =
            BLOCK_ENTITIES.register("silo_be", () -> BlockEntityType.Builder
                    .of(SiloBlockEntity::new, ATEBlocks.SILO.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<FertilizerSpreaderBlockEntity>> FERTILIZER_SPREADER_BE =
            BLOCK_ENTITIES.register("fertilizer_spreader_be", () -> BlockEntityType.Builder
                    .of(FertilizerSpreaderBlockEntity::new, ATEBlocks.FERT_SPREADER.get())
                    .build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
