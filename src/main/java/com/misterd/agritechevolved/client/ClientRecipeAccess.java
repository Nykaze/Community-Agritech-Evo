package com.misterd.agritechevolved.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.crafting.RecipeManager;

/**
 * Client-only bridge used by {@link com.misterd.agritechevolved.util.LevelRecipes} to reach the
 * client recipe cache. It must only be called after checking
 * {@link net.minecraftforge.fml.loading.FMLEnvironment#dist}.
 */
public final class ClientRecipeAccess {

    private ClientRecipeAccess() {
    }

    public static RecipeManager getRecipeManager() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return null;
        ClientLevel level = mc.level;
        return level == null ? null : level.getRecipeManager();
    }
}
