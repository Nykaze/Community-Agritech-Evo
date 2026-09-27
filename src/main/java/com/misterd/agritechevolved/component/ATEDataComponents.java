package com.misterd.agritechevolved.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Minecraft 1.20.1 has no data component system (that arrived in 1.20.5), so the
 * capacitor's stored energy and tier are persisted in item NBT instead. The public
 * surface mirrors the original NeoForge data component accessors so call sites stay
 * unchanged.
 */
public class ATEDataComponents {

    public static final String STORED_ENERGY = "stored_energy";
    public static final String CAPACITOR_TIER = "capacitor_tier";

    public static int getStoredEnergy(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(STORED_ENERGY);
    }

    public static void setStoredEnergy(ItemStack stack, int energy) {
        if (energy <= 0) {
            removeTag(stack, STORED_ENERGY);
            return;
        }
        stack.getOrCreateTag().putInt(STORED_ENERGY, energy);
    }

    public static int getCapacitorTier(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(CAPACITOR_TIER);
    }

    public static void setCapacitorTier(ItemStack stack, int tier) {
        if (tier <= 0) {
            removeTag(stack, CAPACITOR_TIER);
            return;
        }
        stack.getOrCreateTag().putInt(CAPACITOR_TIER, tier);
    }

    private static void removeTag(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(key);
        }
    }

    public static void syncNbt(ItemStack from, ItemStack to) {
        CompoundTag tag = from.getTag();
        if (tag != null) {
            to.setTag(tag.copy());
        }
    }

    public static void register(IEventBus eventBus) {
    }
}
