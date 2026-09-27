package com.misterd.agritechevolved.trait;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * The four traits a planted seed can carry: growth speed, yield, resistance and mutability.
 *
 * <p>Traits live in the seed's NBT under {@value #TAG}, one byte per trait. Storing them on
 * the item means a mutated seed is a genuinely different item from a fresh one of the same
 * type, and because {@link ItemStack#isSameItemSameTags} compares tags, seeds with different
 * traits will not merge in a slot.
 */
public final class PlantTraits {

    public static final String TAG = "Traits";
    public static final String GROWTH = "growth";
    public static final String YIELD = "yield";
    public static final String RESISTANCE = "resistance";
    public static final String MUTABILITY = "mutability";

    private static final String[] KEYS = {GROWTH, YIELD, RESISTANCE, MUTABILITY};

    private final TraitLevel growth;
    private final TraitLevel yield;
    private final TraitLevel resistance;
    private final TraitLevel mutability;

    public PlantTraits(TraitLevel growth, TraitLevel yield, TraitLevel resistance, TraitLevel mutability) {
        this.growth = growth;
        this.yield = yield;
        this.resistance = resistance;
        this.mutability = mutability;
    }

    public static PlantTraits neutral() {
        return new PlantTraits(TraitLevel.NONE, TraitLevel.NONE, TraitLevel.NONE, TraitLevel.NONE);
    }

    public TraitLevel growth() {
        return growth;
    }

    public TraitLevel yield() {
        return yield;
    }

    public TraitLevel resistance() {
        return resistance;
    }

    public TraitLevel mutability() {
        return mutability;
    }

    public TraitLevel get(TraitKind kind) {
        return switch (kind) {
            case GROWTH -> growth;
            case YIELD -> yield;
            case RESISTANCE -> resistance;
            case MUTABILITY -> mutability;
        };
    }

    public PlantTraits with(TraitKind kind, TraitLevel level) {
        return switch (kind) {
            case GROWTH -> new PlantTraits(level, yield, resistance, mutability);
            case YIELD -> new PlantTraits(growth, level, resistance, mutability);
            case RESISTANCE -> new PlantTraits(growth, yield, level, mutability);
            case MUTABILITY -> new PlantTraits(growth, yield, resistance, level);
        };
    }

    public float growthMultiplier() {
        return growth.getGrowthMultiplier();
    }

    public float yieldMultiplier() {
        return yield.getYieldMultiplier();
    }

    public float progressRetainedOnReset() {
        return resistance.getProgressRetainedOnReset();
    }

    public float mutationChancePerTick() {
        return mutability.getMutationChancePerTick();
    }

    public boolean isNeutral() {
        return growth == TraitLevel.NONE && yield == TraitLevel.NONE
                && resistance == TraitLevel.NONE && mutability == TraitLevel.NONE;
    }

    public boolean isFullyMaxed() {
        for (TraitKind kind : TraitKind.values()) {
            if (!get(kind).isMax()) return false;
        }
        return true;
    }

    /** Rolls the starting traits a fresh seed receives. Weighted towards the low end. */
    public static PlantTraits roll(RandomSource random) {
        TraitLevel growth = weighted(random);
        TraitLevel yield = weighted(random);
        TraitLevel resistance = weighted(random);
        TraitLevel mutability = weighted(random);
        return new PlantTraits(growth, yield, resistance, mutability);
    }

    private static TraitLevel weighted(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 40) return TraitLevel.NONE;
        if (roll < 70) return TraitLevel.LOW;
        if (roll < 88) return TraitLevel.MEDIUM;
        if (roll < 97) return TraitLevel.HIGH;
        return TraitLevel.VERY_HIGH;
    }

    /**
     * Picks one trait that is not yet maxed and bumps it a level.
     *
     * @return the improved traits, or {@code null} when everything is already maxed
     */
    public PlantTraits improve(RandomSource random) {
        TraitKind[] candidates = new TraitKind[TraitKind.values().length];
        int count = 0;
        for (TraitKind kind : TraitKind.values()) {
            if (!get(kind).isMax()) candidates[count++] = kind;
        }
        if (count == 0) return null;
        TraitKind chosen = candidates[random.nextInt(count)];
        return with(chosen, get(chosen).next());
    }

    public void writeToNBT(CompoundTag parent) {
        CompoundTag tag = new CompoundTag();
        tag.putByte(GROWTH, (byte) growth.getId());
        tag.putByte(YIELD, (byte) yield.getId());
        tag.putByte(RESISTANCE, (byte) resistance.getId());
        tag.putByte(MUTABILITY, (byte) mutability.getId());
        parent.put(TAG, tag);
    }

    public static PlantTraits readFromNBT(CompoundTag parent) {
        if (parent == null || !parent.contains(TAG, Tag.TAG_COMPOUND)) return null;
        CompoundTag tag = parent.getCompound(TAG);
        return new PlantTraits(
                TraitLevel.byId(tag.getByte(GROWTH)),
                TraitLevel.byId(tag.getByte(YIELD)),
                TraitLevel.byId(tag.getByte(RESISTANCE)),
                TraitLevel.byId(tag.getByte(MUTABILITY)));
    }

    /** Reads the traits of a stack, returning {@link #neutral()} when it has never been mutated. */
    public static PlantTraits of(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return neutral();
        PlantTraits traits = readFromNBT(stack.getTag());
        return traits == null ? neutral() : traits;
    }

    public static boolean hasTraits(ItemStack stack) {
        return stack != null && !stack.isEmpty() && readFromNBT(stack.getTag()) != null;
    }

    public static void applyTo(ItemStack stack, PlantTraits traits) {
        if (stack == null || stack.isEmpty()) return;
        stack.getOrCreateTag().put(TAG, toTag(traits));
    }

    private static CompoundTag toTag(PlantTraits traits) {
        CompoundTag tag = new CompoundTag();
        tag.putByte(GROWTH, (byte) traits.growth().getId());
        tag.putByte(YIELD, (byte) traits.yield().getId());
        tag.putByte(RESISTANCE, (byte) traits.resistance().getId());
        tag.putByte(MUTABILITY, (byte) traits.mutability().getId());
        return tag;
    }

    /**
     * The four trait lines, ready to be rendered by a screen or an overlay. Seeds are vanilla
     * items, so the traits are surfaced through the planter GUI and the block overlay instead of
     * an item tooltip.
     */
    public List<Component> asTooltipLines() {
        List<Component> lines = new ArrayList<>(TraitKind.values().length);
        for (TraitKind kind : TraitKind.values()) {
            lines.add(Component.translatable("gui.community_agritechevolved.trait_" + kind.key(),
                    get(kind).getDisplayName()).withStyle(kind.color()));
        }
        return lines;
    }

    public enum TraitKind {
        GROWTH("growth", ChatFormatting.GREEN),
        YIELD("yield", ChatFormatting.GOLD),
        RESISTANCE("resistance", ChatFormatting.AQUA),
        MUTABILITY("mutability", ChatFormatting.LIGHT_PURPLE);

        private final String key;
        private final ChatFormatting color;

        TraitKind(String key, ChatFormatting color) {
            this.key = key;
            this.color = color;
        }

        public String key() {
            return key;
        }

        public ChatFormatting color() {
            return color;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlantTraits other)) return false;
        return growth == other.growth && yield == other.yield
                && resistance == other.resistance && mutability == other.mutability;
    }

    @Override
    public int hashCode() {
        int result = growth.hashCode();
        result = 31 * result + yield.hashCode();
        result = 31 * result + resistance.hashCode();
        result = 31 * result + mutability.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PlantTraits{growth=" + growth + ", yield=" + yield
                + ", resistance=" + resistance + ", mutability=" + mutability + '}';
    }
}
