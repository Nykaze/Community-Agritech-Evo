package com.misterd.agritechevolved.recipe;

import net.minecraftforge.eventbus.api.IEventBus;

public class ATERecipe {

    public static void register(IEventBus eventBus) {
        ATERecipeTypes.register(eventBus);
    }
}
