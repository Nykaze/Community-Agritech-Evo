package com.misterd.agritechevolved.recipe;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import org.slf4j.Logger;

/**
 * Helper for turning recipe JSON into records with DFU 6.0.8, which has no
 * {@code Codec.parse(JsonElement)} convenience overload.
 */
public final class ATECodecs {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <T> T parse(Codec<T> codec, com.google.gson.JsonObject json, String context) {
        return codec.parse(JsonOps.INSTANCE, json)
                .getOrThrow(false, message -> LOGGER.error("[ATE] Failed to parse {}: {}", context, message));
    }

    private ATECodecs() {
    }
}
