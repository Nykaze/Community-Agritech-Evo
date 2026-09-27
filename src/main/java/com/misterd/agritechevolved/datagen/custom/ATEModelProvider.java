package com.misterd.agritechevolved.datagen.custom;

import com.google.gson.JsonObject;
import com.misterd.agritechevolved.AgritechEvolved;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 1.20.1's {@code ModelProvider} has no extension point for item models, so the flat
 * item models are written straight into the generated pack.
 */
public class ATEModelProvider implements DataProvider {

    private static final Map<String, String> ITEM_TEXTURES = new LinkedHashMap<>();

    static {
        ITEM_TEXTURES.put("sm_mk1", "sm_mk1");
        ITEM_TEXTURES.put("sm_mk2", "sm_mk2");
        ITEM_TEXTURES.put("sm_mk3", "sm_mk3");
        ITEM_TEXTURES.put("ym_mk1", "ym_mk1");
        ITEM_TEXTURES.put("ym_mk2", "ym_mk2");
        ITEM_TEXTURES.put("ym_mk3", "ym_mk3");
        ITEM_TEXTURES.put("rm_mk1", "rm_mk1");
        ITEM_TEXTURES.put("rm_mk2", "rm_mk2");
        ITEM_TEXTURES.put("rm_mk3", "rm_mk3");
        ITEM_TEXTURES.put("crude_biomass", "crude_biomass");
        ITEM_TEXTURES.put("biomass", "biomass");
        ITEM_TEXTURES.put("compacted_biomass", "compacted_biomass");
        ITEM_TEXTURES.put("cloche_dome", "cloche_dome");
    }

    private final PackOutput.PathProvider pathProvider;

    public ATEModelProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Map.Entry<String, String> entry : ITEM_TEXTURES.entrySet()) {
            ResourceLocation id = new ResourceLocation(AgritechEvolved.MODID, entry.getKey());

            JsonObject textures = new JsonObject();
            textures.addProperty("layer0", AgritechEvolved.MODID + ":item/" + entry.getValue());

            JsonObject model = new JsonObject();
            model.addProperty("parent", "minecraft:item/generated");
            model.add("textures", textures);

            futures.add(DataProvider.saveStable(cache, model, pathProvider.json(id)));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Agritech Evolved Item Models";
    }
}
