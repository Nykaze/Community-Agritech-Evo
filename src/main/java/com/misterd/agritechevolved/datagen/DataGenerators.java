package com.misterd.agritechevolved.datagen;

import com.misterd.agritechevolved.AgritechEvolved;
import com.misterd.agritechevolved.datagen.custom.ATEBlockTagProvider;
import com.misterd.agritechevolved.datagen.custom.ATEItemTagProvider;
import com.misterd.agritechevolved.datagen.custom.ATELootTableProvider;
import com.misterd.agritechevolved.datagen.custom.ATEModelProvider;
import com.misterd.agritechevolved.datagen.custom.ATERecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = AgritechEvolved.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existing = event.getExistingFileHelper();

        if (event.includeServer()) {
            generator.addProvider(true, new LootTableProvider(packOutput, Collections.emptySet(),
                    List.of(new LootTableProvider.SubProviderEntry(ATELootTableProvider::new, LootContextParamSets.BLOCK))));

            generator.addProvider(true, new ATERecipeProvider(packOutput));

            ATEBlockTagProvider blockTags = new ATEBlockTagProvider(packOutput, lookupProvider, existing);
            generator.addProvider(true, blockTags);

            generator.addProvider(true, new ATEItemTagProvider(packOutput, lookupProvider, blockTags.contentsGetter(), existing));
        }

        if (event.includeClient()) {
            generator.addProvider(true, new ATEModelProvider(packOutput));
        }
    }
}
