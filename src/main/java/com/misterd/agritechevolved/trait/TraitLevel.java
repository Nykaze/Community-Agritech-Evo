package com.misterd.agritechevolved.trait;

import net.minecraft.network.chat.Component;

/**
 * The five levels a plant trait can be at, mirroring the scale used by the 1.21 trait
 * system: {@code None}, {@code Low}, {@code Medium}, {@code High} and {@code Very High}.
 *
 * <p>Every tunable the gameplay uses is defined here so the displayed level and the applied
 * effect can never drift apart.
 */
public enum TraitLevel {
    NONE(0, "none", 1.0F, 1.0F, 0.0F, 0.0F),
    LOW(1, "low", 1.15F, 1.15F, 0.25F, 0.0002F),
    MEDIUM(2, "medium", 1.30F, 1.30F, 0.50F, 0.0005F),
    HIGH(3, "high", 1.50F, 1.50F, 0.75F, 0.0010F),
    VERY_HIGH(4, "very_high", 1.75F, 1.80F, 1.00F, 0.0025F);

    private static final TraitLevel[] VALUES = values();

    private final int id;
    private final String name;
    private final float growthMultiplier;
    private final float yieldMultiplier;
    private final float progressRetainedOnReset;
    private final float mutationChancePerTick;

    TraitLevel(int id, String name, float growthMultiplier, float yieldMultiplier,
               float progressRetainedOnReset, float mutationChancePerTick) {
        this.id = id;
        this.name = name;
        this.growthMultiplier = growthMultiplier;
        this.yieldMultiplier = yieldMultiplier;
        this.progressRetainedOnReset = progressRetainedOnReset;
        this.mutationChancePerTick = mutationChancePerTick;
    }

    public int getId() {
        return id;
    }

    public String getSerializedName() {
        return name;
    }

    public float getGrowthMultiplier() {
        return growthMultiplier;
    }

    public float getYieldMultiplier() {
        return yieldMultiplier;
    }

    /** Fraction of the current growth progress that survives a reset caused by a missing or invalid soil. */
    public float getProgressRetainedOnReset() {
        return progressRetainedOnReset;
    }

    public float getMutationChancePerTick() {
        return mutationChancePerTick;
    }

    public boolean isMax() {
        return this == VERY_HIGH;
    }

    public TraitLevel next() {
        return VALUES[Math.min(id + 1, VALUES.length - 1)];
    }

    public Component getDisplayName() {
        return Component.translatable("gui.community_agritechevolved.trait_value." + name);
    }

    public static TraitLevel byId(int id) {
        if (id <= 0) return NONE;
        if (id >= VALUES.length) return VERY_HIGH;
        return VALUES[id];
    }

    public static TraitLevel byName(String name) {
        for (TraitLevel level : VALUES) {
            if (level.name.equals(name)) return level;
        }
        return NONE;
    }

    public static int count() {
        return VALUES.length;
    }
}
