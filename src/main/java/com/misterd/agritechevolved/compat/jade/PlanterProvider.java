package com.misterd.agritechevolved.compat.jade;

import com.misterd.agritechevolved.Config;
import com.misterd.agritechevolved.block.custom.AdvancedPlanterBlock;
import com.misterd.agritechevolved.block.custom.PlanterBlock;
import com.misterd.agritechevolved.blockentity.custom.AdvancedPlanterBlockEntity;
import com.misterd.agritechevolved.blockentity.custom.PlanterBlockEntity;
import com.misterd.agritechevolved.datamap.ATEDataMaps;
import com.misterd.agritechevolved.datamap.FertilizerData;
import com.misterd.agritechevolved.trait.PlantTraits;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum PlanterProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    static final ResourceLocation UID =
            new ResourceLocation("community_agritechevolved", "planter_info");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        BlockEntity be = accessor.getBlockEntity();
        if (be instanceof AdvancedPlanterBlockEntity advanced) {
            appendAdvancedPlanterData(data, advanced, accessor.getBlockState(), advanced.getLevel());
        } else if (be instanceof PlanterBlockEntity basic) {
            appendBasicPlanterData(data, basic, accessor.getBlockState(), basic.getLevel());
        }
    }

    private void appendBasicPlanterData(CompoundTag data, PlanterBlockEntity planter, BlockState state, Level level) {
        data.putBoolean("isAdvanced", false);

        ItemStack seedStack = planter.getStack(0);
        ItemStack soilStack = planter.getStack(1);
        if (seedStack.isEmpty() || soilStack.isEmpty()) {
            data.putBoolean("hasCrop", false);
            return;
        }

        appendCommonCropData(data, seedStack, soilStack,
                planter.getGrowthStage(), planter.getGrowthProgress(),
                planter.getSoilGrowthModifier(planter.getLevel(), soilStack), planter.isTree());
        appendFertilizerData(data, level, planter.getStack(2));
        appendClocheData(data, state.getValue(PlanterBlock.CLOCHED));
    }

    private void appendAdvancedPlanterData(CompoundTag data, AdvancedPlanterBlockEntity planter, BlockState state, Level level) {
        data.putBoolean("isAdvanced", true);

        ItemStack seedStack = planter.getStack(0);
        ItemStack soilStack = planter.getStack(1);
        if (seedStack.isEmpty() || soilStack.isEmpty()) {
            data.putBoolean("hasCrop", false);
            return;
        }

        appendCommonCropData(data, seedStack, soilStack,
                planter.getGrowthStage(), planter.getGrowthProgress(),
                planter.getSoilGrowthModifier(planter.getLevel(), soilStack), planter.isTree());
        appendFertilizerData(data, level, planter.getStack(4));
        appendClocheData(data, state.getValue(AdvancedPlanterBlock.CLOCHED));

        data.putInt("energyStored", planter.getEnergyStored());
        data.putInt("maxEnergy", planter.getMaxEnergyStored());

        boolean hasModules = !planter.getStack(2).isEmpty() || !planter.getStack(3).isEmpty();
        data.putBoolean("hasModules", hasModules);
        if (hasModules) {
            data.putFloat("moduleSpeedModifier", planter.getModuleSpeedModifier());
            data.putFloat("moduleYieldModifier", planter.getModuleYieldModifier());
        }
    }

    private void appendCommonCropData(CompoundTag data, ItemStack seedStack, ItemStack soilStack, int growthStage, float growthProgress, float soilModifier, boolean isTree) {
        data.putBoolean("hasCrop", true);
        data.putString("cropName", seedStack.getDisplayName().getString());
        data.putInt("currentStage", growthStage);
        data.putInt("maxStage", isTree ? 1 : 8);
        data.putFloat("progressPercent", growthProgress * 100.0F);
        data.putString("soilName", soilStack.getDisplayName().getString());
        data.putFloat("growthModifier", soilModifier);

        data.putBoolean("hasTraits", PlantTraits.hasTraits(seedStack));
        PlantTraits traits = PlantTraits.of(seedStack);
        data.putInt("traitGrowth", traits.growth().getId());
        data.putInt("traitYield", traits.yield().getId());
        data.putInt("traitResistance", traits.resistance().getId());
        data.putInt("traitMutability", traits.mutability().getId());
    }

    private void appendFertilizerData(CompoundTag data, Level level, ItemStack fertStack) {
        if (fertStack.isEmpty()) {
            data.putBoolean("hasFertilizer", false);
            return;
        }
        FertilizerData fertData = ATEDataMaps.getFertilizer(level, fertStack.getItem());
        data.putBoolean("hasFertilizer", fertData != null);
        if (fertData != null) {
            data.putString("fertilizerName", fertStack.getDisplayName().getString());
            data.putFloat("fertilizerSpeedModifier", fertData.speedMultiplier());
            data.putFloat("fertilizerYieldModifier", fertData.yieldMultiplier());
        }
    }

    private void appendClocheData(CompoundTag data, boolean cloched) {
        data.putBoolean("isCloched", cloched);
        if (cloched) {
            data.putFloat("clocheSpeedModifier", (float) Config.getClocheSpeedMultiplier());
            data.putFloat("clocheYieldModifier", (float) Config.getClocheYieldMultiplier());
        }
    }
}