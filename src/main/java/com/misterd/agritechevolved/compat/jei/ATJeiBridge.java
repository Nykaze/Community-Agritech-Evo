package com.misterd.agritechevolved.compat.jei;

import net.minecraftforge.fml.ModList;

/**
 * JEI-free entry point for calling into the JEI integration.
 *
 * <p>The screens must not name any {@code mezz.jei} type directly: JEI is an optional dependency,
 * so a method that declares an {@code IJeiRuntime} local would fail class verification (or throw
 * {@code NoClassDefFoundError}) the moment a player clicked the button without JEI installed,
 * breaking the planter and composter screens altogether. Every call is funnelled through here so
 * the optional classes are only ever resolved after {@link ModList} confirms JEI is present.
 */
public final class ATJeiBridge {

    private static final String JEI_MOD_ID = "jei";

    private ATJeiBridge() {
    }

    public static boolean isJeiLoaded() {
        return ModList.get() != null && ModList.get().isLoaded(JEI_MOD_ID);
    }

    /** Opens the planter recipe category, or does nothing when JEI is absent. */
    public static void showPlanterRecipes() {
        if (!isJeiLoaded()) return;
        ATJeiPlugin.showPlanterRecipes();
    }

    /** Opens the compost recipe category, or does nothing when JEI is absent. */
    public static void showCompostRecipes() {
        if (!isJeiLoaded()) return;
        ATJeiPlugin.showCompostRecipes();
    }
}
