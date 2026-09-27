package com.misterd.agritechevolved.integration;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired before harvest drops are calculated.
 * <p>
 * The seed exposed here is a mutable copy of whatever is in the planter's seed slot.
 * Whatever is returned from {@link #getSeed()} after this event finishes posting is:
 * <ul>
 *     <li>used as the input for harvest drop calculation</li>
 *     <li>written back into the planter's seed slot, replacing the original stack</li>
 * </ul>
 * Recipe matching is item-type only, so changing data components/NBT on the seed
 * will not affect crop/soil validation.
 */
public class PlanterPreHarvestEvent extends Event {

    private final BlockEntity planter;
    private ItemStack seed;

    public PlanterPreHarvestEvent(BlockEntity planter, ItemStack seed) {
        this.planter = planter;
        this.seed = seed.copy();
    }

    /**
     * @return the block entity of the planter that is about to harvest.
     */
    public BlockEntity getPlanter() { return planter; }

    /**
     * @return a mutable copy of the seed currently in the planter's seed slot.
     */
    public ItemStack getSeed() { return seed; }

    /**
     * Replaces the seed used for drop calculation and slot write-back.
     *
     * @param seed the modified seed stack.
     */
    public void setSeed(ItemStack seed) { this.seed = seed; }
}