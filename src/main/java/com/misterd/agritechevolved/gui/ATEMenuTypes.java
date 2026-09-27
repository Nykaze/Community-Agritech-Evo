package com.misterd.agritechevolved.gui;

import com.misterd.agritechevolved.AgritechEvolved;
import com.misterd.agritechevolved.gui.custom.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ATEMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, AgritechEvolved.MODID);

    public static final RegistryObject<MenuType<BasicPlanterMenu>> PLANTER_BLOCK_MENU =
            registerMenuType("planter_block_menu", BasicPlanterMenu::new);
    public static final RegistryObject<MenuType<AdvancedPlanterMenu>> ADVANCED_PLANTER_BLOCK_MENU =
            registerMenuType("advanced_planter_block_menu", AdvancedPlanterMenu::new);
    public static final RegistryObject<MenuType<ComposterMenu>> COMPOSTER_MENU =
            registerMenuType("composter_menu", ComposterMenu::new);
    public static final RegistryObject<MenuType<CapacitorMenu>> CAPACITOR_MENU =
            registerMenuType("capacitor_menu", CapacitorMenu::new);
    public static final RegistryObject<MenuType<BiomassBurnerMenu>> BURNER_MENU =
            registerMenuType("burner_menu", BiomassBurnerMenu::new);
    public static final RegistryObject<MenuType<SiloMenu>> SILO_MENU =
            registerMenuType("silo_menu", SiloMenu::new);
    public static final RegistryObject<MenuType<FertilizerSpreaderMenu>> FERTILIZER_SPREADER_MENU =
            registerMenuType("fertilizer_spreader_menu", FertilizerSpreaderMenu::new);

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
