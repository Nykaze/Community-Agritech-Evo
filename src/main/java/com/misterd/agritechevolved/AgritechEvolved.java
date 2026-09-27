package com.misterd.agritechevolved;

import com.misterd.agritechevolved.block.ATEBlocks;
import com.misterd.agritechevolved.blockentity.ATEBlockEntities;
import com.misterd.agritechevolved.blockentity.custom.AdvancedPlanterBlockEntity;
import com.misterd.agritechevolved.blockentity.custom.PlanterBlockEntity;
import com.misterd.agritechevolved.client.ber.AdvancedPlanterBlockEntityRenderer;
import com.misterd.agritechevolved.client.ber.PlanterBlockEntityRenderer;
import com.misterd.agritechevolved.component.ATEDataComponents;
import com.misterd.agritechevolved.datamap.ATEDataMaps;
import com.misterd.agritechevolved.gui.ATEMenuTypes;
import com.misterd.agritechevolved.gui.custom.*;
import com.misterd.agritechevolved.item.ATECreativeTab;
import com.misterd.agritechevolved.item.ATEItems;
import com.misterd.agritechevolved.network.ATENetwork;
import com.misterd.agritechevolved.network.ATESyncDataMapsPacket;
import com.misterd.agritechevolved.recipe.ATERecipe;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(AgritechEvolved.MODID)
public class AgritechEvolved {
    public static final String MODID = "community_agritechevolved";
    public static int RECIPE_REVISION = 0;
    public static final Logger LOGGER = LogUtils.getLogger();

    public AgritechEvolved() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus gameEventBus = MinecraftForge.EVENT_BUS;

        modEventBus.addListener(this::commonSetup);
        ATEDataMaps.register(gameEventBus);
        ATENetwork.register();

        ATEBlocks.register(modEventBus);
        ATEBlockEntities.register(modEventBus);
        ATEItems.register(modEventBus);
        ATECreativeTab.register(modEventBus);
        ATEMenuTypes.register(modEventBus);
        ATEDataComponents.register(modEventBus);
        ATERecipe.register(modEventBus);

        Config.register();
        modEventBus.register(Config.class);

        gameEventBus.addListener(this::onServerReload);
        gameEventBus.addListener(this::onDatapackSync);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }

    /**
     * Fires on login and after every {@code /reload}. Clients load data maps from their own
     * resource packs, which misses anything only the server has, so push the authoritative
     * values once the server side is done reloading.
     */
    private void onDatapackSync(OnDatapackSyncEvent event) {
        event.getPlayers().forEach(ATESyncDataMapsPacket::sendTo);
    }

    private void onServerReload(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager manager, ProfilerFiller profiler) {
                return null;
            }

            @Override
            protected void apply(Void object, ResourceManager manager, ProfilerFiller profiler) {
                RECIPE_REVISION++;
            }

            @Override
            public String getName() {
                return ResourceLocation.fromNamespaceAndPath(MODID, "recipe_revision_tracker").toString();
            }
        });
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(ATEMenuTypes.PLANTER_BLOCK_MENU.get(), BasicPlanterScreen::new);
                MenuScreens.register(ATEMenuTypes.ADVANCED_PLANTER_BLOCK_MENU.get(), AdvancedPlanterScreen::new);
                MenuScreens.register(ATEMenuTypes.COMPOSTER_MENU.get(), ComposterScreen::new);
                MenuScreens.register(ATEMenuTypes.BURNER_MENU.get(), BiomassBurnerScreen::new);
                MenuScreens.register(ATEMenuTypes.CAPACITOR_MENU.get(), CapacitorScreen::new);
                MenuScreens.register(ATEMenuTypes.FERTILIZER_SPREADER_MENU.get(), FertilizerSpreaderScreen::new);
                MenuScreens.register(ATEMenuTypes.SILO_MENU.get(), SiloScreen::new);
            });
        }

        @SubscribeEvent
        public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ATEBlockEntities.PLANTER_BLOCK_BE.get(), PlanterBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(ATEBlockEntities.ADVANCED_PLANTER_BLOCK_BE.get(), AdvancedPlanterBlockEntityRenderer::new);
        }
    }
}
