package com.misterd.agritechevolved.integration;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired each growth tick after all built-in modifiers (soil, fertilizer, modules,
 * cloche, etc.) have been applied to the planter's processing time.
 * <p>
 * The seed is read-only here — to modify the seed itself, use
 * {@link PlanterPreHarvestEvent} instead. This event only controls how long the
 * current growth cycle takes.
 */
public class PlanterProcessingTimeEvent extends Event {

    private final BlockEntity planter;
    private final ItemStack seed;
    private int processingTime;

    public PlanterProcessingTimeEvent(BlockEntity planter, ItemStack seed, int processingTime) {
        this.planter = planter;
        this.seed = seed;
        this.processingTime = processingTime;
    }

    /**
     * @return the block entity of the planter currently growing.
     */
    public BlockEntity getPlanter() { return planter; }

    /**
     * @return the seed currently planted, for reading stats only.
     */
    public ItemStack getSeed() { return seed; }

    /**
     * @return the processing time in ticks, after all built-in modifiers have been applied.
     */
    public int getProcessingTime() { return processingTime; }

    /**
     * Overrides the final processing time. Clamped to a minimum of 1 tick.
     *
     * @param processingTime the new processing time in ticks.
     */
    public void setProcessingTime(int processingTime) { this.processingTime = Math.max(1, processingTime); }
}