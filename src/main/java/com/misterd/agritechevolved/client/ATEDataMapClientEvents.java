package com.misterd.agritechevolved.client;

import com.misterd.agritechevolved.AgritechEvolved;
import com.misterd.agritechevolved.datamap.ATEDataMaps;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-only half of the data map loading. The logical server half lives in
 * {@link ATEDataMaps#onAddReloadListener} and is registered on the mod event bus directly.
 * <p>
 * This is a separate class because {@link RegisterClientReloadListenersEvent} is a client
 * only type; keeping it isolated means the dedicated server never loads it.
 */
@Mod.EventBusSubscriber(modid = AgritechEvolved.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ATEDataMapClientEvents {

    private ATEDataMapClientEvents() {
    }

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(ATEDataMaps.clientFertilizerListener());
        event.registerReloadListener(ATEDataMaps.clientSoilModifierListener());
    }
}
