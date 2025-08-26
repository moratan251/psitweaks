package com.moratan251.psitweaks.client.guis;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, "psitweaks");

    public static final RegistryObject<MenuType<FlashRingMenu>> FLASH_RING =
            MENUS.register("flash_ring",
                    () -> IForgeMenuType.create(FlashRingMenu::new));
}