package com.misterd.agritechevolved.integration;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

/**
 * Fired after harvest drops have been resolved and all built-in yield modifiers
 * (fertilizer, modules, cloche, etc.) have been applied, but before the drops are
 * placed into the planter's output slots.
 * <p>
 * The seed is read-only here for context only — to modify the seed itself
 * (e.g. for stat progression), use {@link PlanterPreHarvestEvent} instead.
 * <p>
 * The drops list is fully mutable. Add, remove, or replace stacks, or modify
 * the data/count on existing stacks directly.
 */
public class PlanterPostHarvestEvent extends Event {

    private final BlockEntity planter;
    private final ItemStack seed;
    private final List<ItemStack> drops;

    public PlanterPostHarvestEvent(BlockEntity planter, ItemStack seed, List<ItemStack> drops) {
        this.planter = planter;
        this.seed = seed;
        this.drops = drops;
    }

    /**
     * @return the block entity of the planter that just harvested.
     */
    public BlockEntity getPlanter() { return planter; }

    /**
     * @return the seed that was harvested, for context only.
     */
    public ItemStack getSeed() { return seed; }

    /**
     * @return the mutable list of harvest drops, after built-in yield modifiers have been applied.
     */
    public List<ItemStack> getDrops() { return drops; }
}