package com.moratan251.psitweaks.client.gui.machine;

import com.moratan251.psitweaks.common.menu.PortableCADAssemblerMenu;
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

    public static final RegistryObject<MenuType<PortableCADAssemblerMenu>> PORTABLE_CAD_ASSEMBLER =
            MENUS.register("portable_cad_assembler",
                    () -> IForgeMenuType.create(PortableCADAssemblerMenu::fromNetwork));

    public static final RegistryObject<MenuType<com.moratan251.psitweaks.common.menu.IdeaStorageMenu>> IDEA_STORAGE =
            MENUS.register("idea_storage", () -> IForgeMenuType.create(com.moratan251.psitweaks.common.menu.IdeaStorageMenu::fromNetwork));
    public static final RegistryObject<MenuType<com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu>> IDEASPACE_CONNECTOR = MENUS.register("ideaspace_connector", () -> IForgeMenuType.create(com.moratan251.psitweaks.common.menu.IdeaspaceConnectorMenu::fromNetwork));
    public static final RegistryObject<MenuType<com.moratan251.psitweaks.common.menu.PortableSpellProgrammerMenu>> PORTABLE_SPELL_PROGRAMMER = MENUS.register("portable_spell_programmer", () -> IForgeMenuType.create(com.moratan251.psitweaks.common.menu.PortableSpellProgrammerMenu::fromNetwork));
}
