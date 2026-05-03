package com.moratan251.psitweaks.client.spells;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import vazkii.psi.api.ClientPsiAPI;

public final class PsitweaksClientSpells {
    public static final DeferredRegister<Material> SPELL_PIECE_MATERIALS =
            DeferredRegister.create(ClientPsiAPI.SPELL_PIECE_MATERIAL, Psitweaks.MOD_ID);

    public static final DeferredHolder<Material, Material> TRICK_SUPREME_INFUSION =
            SPELL_PIECE_MATERIALS.register("trick_supreme_infusion",
                    () -> new Material(InventoryMenu.BLOCK_ATLAS, Psitweaks.location("spell/trick_supreme_infusion")));

    private PsitweaksClientSpells() {
    }

    public static void register(IEventBus eventBus) {
        SPELL_PIECE_MATERIALS.register(eventBus);
    }
}
