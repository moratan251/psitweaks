package com.moratan251.psitweaks.common.items;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import vazkii.psi.api.PsiAPI;

public final class PsitweaksItemCapabilities {
    private PsitweaksItemCapabilities() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        registerInlineCaster(event, PsitweaksItems.INLINE_CASTER, 1);
        registerInlineCaster(event, PsitweaksItems.SECONDARY_CASTER, 5);
        registerInlineCaster(event, PsitweaksItems.PARALLEL_CASTER, 11);
    }

    private static void registerInlineCaster(RegisterCapabilitiesEvent event, DeferredItem<? extends Item> item, int slotCount) {
        event.registerItem(
                Capabilities.ItemHandler.ITEM,
                (stack, context) -> ItemInlineCasterBase.createBulletHandler(stack, slotCount),
                item.get()
        );
        event.registerItem(
                PsiAPI.SOCKETABLE_CAPABILITY,
                (stack, context) -> new ItemInlineCasterBase.InlineCasterSocketable(stack, slotCount),
                item.get()
        );
        event.registerItem(
                PsiAPI.SPELL_ACCEPTOR_CAPABILITY,
                (stack, context) -> new ItemInlineCasterBase.InlineCasterSocketable(stack, slotCount),
                item.get()
        );
    }
}
