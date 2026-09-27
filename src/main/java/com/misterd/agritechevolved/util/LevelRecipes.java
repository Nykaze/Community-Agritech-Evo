package com.misterd.agritechevolved.util;

import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

import com.misterd.agritechevolved.client.ClientRecipeAccess;

/**
 * Resolves the {@link RecipeManager} for a {@link Level} on both logical sides.
 *
 * <p>On 1.20.1 {@link Level} has no {@code getRecipeManager()}: it only exists on
 * {@link ServerLevel} and on the client level. Since slot validation runs on the client too,
 * returning {@code null} there would make the planter screens reject valid items, so the client
 * side is resolved through {@link ClientRecipeAccess}, which is only ever touched after the
 * {@link FMLEnvironment#dist} check.</p>
 */
public final class LevelRecipes {

    private LevelRecipes() {
    }

    public static RecipeManager get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getRecipeManager();
        }
        if (level != null && FMLEnvironment.dist == Dist.CLIENT) {
            return ClientRecipeAccess.getRecipeManager();
        }
        return null;
    }
}
