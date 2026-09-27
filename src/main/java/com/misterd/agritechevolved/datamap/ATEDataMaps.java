package com.misterd.agritechevolved.datamap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Forge 1.20.1 has no equivalent of NeoForge's data maps ({@code DataMapType} does not
 * exist in 47.x), so this class provides the same lookup surface backed by reload
 * listeners.
 *
 * <p>The on-disk layout is kept identical to the NeoForge version
 * ({@code data/<namespace>/data_maps/item/<name>.json}) so datapacks stay compatible.
 * Entries accept both {@code forge:conditions} and {@code neoforge:conditions} so
 * datapacks authored against either loader version load unchanged.
 *
 * <p>The logical server loads the server maps through {@link AddReloadListenerEvent}; the
 * physical client loads the client maps through
 * {@code net.minecraftforge.client.event.RegisterClientReloadListenersEvent} (see
 * {@code ATEDataMapClientEvents}). This split is what keeps integrated singleplayer
 * correct: {@code FMLEnvironment.dist} is {@code CLIENT} for both logical sides there,
 * so it cannot be used to pick a map.
 */
public final class ATEDataMaps {

    /** All data map files for the item registry live directly in this directory. */
    public static final String DATA_MAP_DIRECTORY = "data_maps/item";

    public static final String FERTILIZERS_FILE = "fertilizers.json";
    public static final String SOIL_MODIFIERS_FILE = "soil_modifiers.json";

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Map<Item, FertilizerData> SERVER_FERTILIZERS = new HashMap<>();
    private static final Map<Item, SoilModifierData> SERVER_SOIL_MODIFIERS = new HashMap<>();
    private static final Map<Item, FertilizerData> CLIENT_FERTILIZERS = new HashMap<>();
    private static final Map<Item, SoilModifierData> CLIENT_SOIL_MODIFIERS = new HashMap<>();

    private ATEDataMaps() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ATEDataMaps::onAddReloadListener);
    }

    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new Loader(FERTILIZERS_FILE, false));
        event.addListener(new Loader(SOIL_MODIFIERS_FILE, false));
    }

    public static PreparableReloadListener clientFertilizerListener() {
        return new Loader(FERTILIZERS_FILE, true);
    }

    public static PreparableReloadListener clientSoilModifierListener() {
        return new Loader(SOIL_MODIFIERS_FILE, true);
    }

    public static Map<Item, FertilizerData> clientFertilizers() {
        return CLIENT_FERTILIZERS;
    }

    public static Map<Item, SoilModifierData> clientSoilModifiers() {
        return CLIENT_SOIL_MODIFIERS;
    }

    public static Map<Item, FertilizerData> serverFertilizers() {
        return SERVER_FERTILIZERS;
    }

    public static Map<Item, SoilModifierData> serverSoilModifiers() {
        return SERVER_SOIL_MODIFIERS;
    }

    /**
     * Applies a payload received from the server. Only meaningful on the physical client.
     */
    public static void acceptSync(Map<Item, FertilizerData> fertilizers,
                                  Map<Item, SoilModifierData> soilModifiers) {
        CLIENT_FERTILIZERS.clear();
        CLIENT_FERTILIZERS.putAll(fertilizers);
        CLIENT_SOIL_MODIFIERS.clear();
        CLIENT_SOIL_MODIFIERS.putAll(soilModifiers);
    }

    /**
     * Client-side lookup, for tooltips and rendering where no {@link net.minecraft.world.level.Level}
     * is at hand. On an integrated server this reads the client maps, which are kept in sync
     * with the server by the network payload.
     */
    public static FertilizerData getFertilizer(Item item) {
        return CLIENT_FERTILIZERS.get(item);
    }

    public static SoilModifierData getSoilModifier(Item item) {
        return CLIENT_SOIL_MODIFIERS.get(item);
    }

    /**
     * Context aware lookup. Block entity and menu code must use this: on an integrated
     * server the logical server and the physical client share one JVM, so
     * {@code FMLEnvironment.dist} cannot tell the two apart.
     */
    public static FertilizerData getFertilizer(Level level, Item item) {
        return (level != null && level.isClientSide() ? CLIENT_FERTILIZERS : SERVER_FERTILIZERS).get(item);
    }

    public static SoilModifierData getSoilModifier(Level level, Item item) {
        return (level != null && level.isClientSide() ? CLIENT_SOIL_MODIFIERS : SERVER_SOIL_MODIFIERS).get(item);
    }

    private static final class Loader extends SimplePreparableReloadListener<JsonObject> {

        private final String fileName;
        private final boolean fertilizers;
        private final boolean client;

        private Loader(String fileName, boolean client) {
            this.fileName = fileName;
            this.fertilizers = FERTILIZERS_FILE.equals(fileName);
            this.client = client;
        }

        @Override
        protected JsonObject prepare(ResourceManager manager, ProfilerFiller profiler) {
            JsonObject result = new JsonObject();
            manager.listResources(DATA_MAP_DIRECTORY, id -> id.getPath().endsWith("/" + fileName))
                    .forEach((id, resource) -> {
                        try (BufferedReader reader = resource.openAsReader()) {
                            JsonElement parsed = JsonParser.parseReader(reader);
                            if (parsed.isJsonObject()) {
                                result.add(id.toString(), parsed.getAsJsonObject());
                            }
                        } catch (Exception e) {
                            LOGGER.error("[ATE] Failed to read data map {}:{}", DATA_MAP_DIRECTORY, id, e);
                        }
                    });
            return result;
        }

        @Override
        protected void apply(JsonObject prepared, ResourceManager manager, ProfilerFiller profiler) {
            Map<Item, FertilizerData> fertilizerLoaded = new HashMap<>();
            Map<Item, SoilModifierData> soilLoaded = new HashMap<>();

            for (Map.Entry<String, JsonElement> file : prepared.entrySet()) {
                JsonObject root = file.getValue().getAsJsonObject();
                JsonObject values = root.has("values") && root.get("values").isJsonObject()
                        ? root.getAsJsonObject("values")
                        : new JsonObject();

                for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
                    if (!testConditions(entry.getValue())) {
                        continue;
                    }

                    ResourceLocation id;
                    try {
                        id = new ResourceLocation(entry.getKey());
                    } catch (Exception e) {
                        LOGGER.error("[ATE] Invalid {} data map key: {}", fileName, entry.getKey());
                        continue;
                    }

                    Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
                    if (item == null || item == Items.AIR) {
                        continue;
                    }

                    // Conditional entries nest the payload under "value".
                    JsonElement payload = unwrapValue(entry.getValue());

                    if (fertilizers) {
                        FertilizerData.CODEC.parse(JsonOps.INSTANCE, payload)
                                .resultOrPartial(error -> LOGGER.error(
                                        "[ATE] Failed to parse fertilizer data map entry {}: {}", entry.getKey(), error))
                                .ifPresent(data -> fertilizerLoaded.put(item, data));
                    } else {
                        SoilModifierData.CODEC.parse(JsonOps.INSTANCE, payload)
                                .resultOrPartial(error -> LOGGER.error(
                                        "[ATE] Failed to parse soil modifier data map entry {}: {}", entry.getKey(), error))
                                .ifPresent(data -> soilLoaded.put(item, data));
                    }
                }
            }

            int size;
            if (fertilizers) {
                size = commit(fertilizerLoaded, CLIENT_FERTILIZERS, SERVER_FERTILIZERS)
                        ? fertilizerLoaded.size() : CLIENT_FERTILIZERS.size();
            } else {
                size = commit(soilLoaded, CLIENT_SOIL_MODIFIERS, SERVER_SOIL_MODIFIERS)
                        ? soilLoaded.size() : CLIENT_SOIL_MODIFIERS.size();
            }
            LOGGER.info("[ATE] Loaded {} {} entries on the {}",
                    size, fileName, client ? "client" : "server");
        }

        /**
         * Installs freshly parsed values. Each {@link Loader} handles one file, so at most one
         * of the two calls carries data.
         *
         * <p>On the client an empty result means this pass found nothing locally, which is the
         * normal case because the authoritative values arrive from the server through
         * {@code ATESyncDataMapsPacket}. Applying it anyway would wipe synced data whenever the
         * client reloads resources ({@code /reload}, F3+T), so an empty client pass is skipped.
         */
        private <T> boolean commit(Map<Item, T> loaded, Map<Item, T> clientTarget, Map<Item, T> serverTarget) {
            if (loaded.isEmpty() && client && !clientTarget.isEmpty()) {
                return false;
            }
            Map<Item, T> target = client ? clientTarget : serverTarget;
            target.clear();
            target.putAll(loaded);
            return true;
        }

        private static JsonElement unwrapValue(JsonElement element) {
            if (element.isJsonObject() && element.getAsJsonObject().has("value")) {
                return element.getAsJsonObject().get("value");
            }
            return element;
        }

        /**
         * Accepts both {@code forge:conditions} and {@code neoforge:conditions} so datapacks
         * written for either loader version behave the same. Condition types are namespaced
         * too, so they are rewritten from {@code neoforge:} to {@code forge:} before being
         * handed to {@link CraftingHelper}, which only knows the Forge serializers.
         */
        private static boolean testConditions(JsonElement element) {
            if (!element.isJsonObject()) {
                return true;
            }
            JsonObject object = element.getAsJsonObject();
            JsonElement conditions = object.has("neoforge:conditions")
                    ? object.get("neoforge:conditions")
                    : object.get("forge:conditions");

            if (conditions == null || !conditions.isJsonArray()) {
                return true;
            }

            JsonArray array = conditions.getAsJsonArray();
            for (JsonElement child : array) {
                if (!child.isJsonObject()) {
                    continue;
                }
                try {
                    JsonObject condition = child.getAsJsonObject().deepCopy();
                    if (condition.has("type")) {
                        String type = condition.get("type").getAsString();
                        if (type.startsWith("neoforge:")) {
                            condition.addProperty("type", "forge:" + type.substring("neoforge:".length()));
                        }
                    }
                    if (!CraftingHelper.getCondition(condition).test(ICondition.IContext.EMPTY)) {
                        return false;
                    }
                } catch (Exception e) {
                    LOGGER.error("[ATE] Failed to evaluate condition {}", child, e);
                    return false;
                }
            }
            return true;
        }
    }
}
