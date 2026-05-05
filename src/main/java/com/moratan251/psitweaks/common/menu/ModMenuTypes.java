package com.moratan251.psitweaks.common.menu;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Psitweaks.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<PortableCADAssemblerMenu>> PORTABLE_CAD_ASSEMBLER =
            MENUS.register("portable_cad_assembler",
                    () -> IMenuTypeExtension.create(PortableCADAssemblerMenu::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<ProgramResearcherMenu>> PROGRAM_RESEARCHER =
            MENUS.register("program_researcher",
                    () -> IMenuTypeExtension.create(ProgramResearcherMenu::fromNetwork));

    private ModMenuTypes() {
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
