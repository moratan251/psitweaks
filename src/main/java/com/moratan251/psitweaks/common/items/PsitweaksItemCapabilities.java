package com.moratan251.psitweaks.common.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.ComponentItemHandler;
import net.neoforged.neoforge.registries.DeferredItem;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.common.item.ItemSpellBullet;
import vazkii.psi.common.item.armor.ItemPsimetalArmor;
import vazkii.psi.common.item.base.ModDataComponents;
import vazkii.psi.common.item.tool.ToolSocketable;
import com.moratan251.psitweaks.common.items.curios.ItemCuriosCompat;

public final class PsitweaksItemCapabilities {
    private PsitweaksItemCapabilities() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        registerInlineCaster(event, PsitweaksItems.INLINE_CASTER, 1);
        registerInlineCaster(event, PsitweaksItems.SECONDARY_CASTER, 5);
        registerInlineCaster(event, PsitweaksItems.PARALLEL_CASTER, 11);
        registerMovalSuitArmor(event);
        registerAutoCasters(event);
        registerSpellMagazine(event);
        registerPsimetalBow(event);
        registerSpellBullets(event);
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

    private static void registerMovalSuitArmor(RegisterCapabilitiesEvent event) {
        ItemLike[] armorItems = {
                PsitweaksItems.MOVAL_SUIT_HELMET.get(),
                PsitweaksItems.MOVAL_SUIT_CHESTPLATE.get(),
                PsitweaksItems.MOVAL_SUIT_LEGGINGS.get(),
                PsitweaksItems.MOVAL_SUIT_BOOTS.get()
        };

        event.registerItem(
                Capabilities.ItemHandler.ITEM,
                (stack, context) -> new ComponentItemHandler(stack, ModDataComponents.BULLETS.get(), 3),
                armorItems
        );
        event.registerItem(
                PsiAPI.PSI_BAR_DISPLAY_CAPABILITY,
                (stack, context) -> new ItemPsimetalArmor.ArmorSocketable(stack, 3),
                armorItems
        );
        event.registerItem(
                PsiAPI.SPELL_ACCEPTOR_CAPABILITY,
                (stack, context) -> new ItemPsimetalArmor.ArmorSocketable(stack, 3),
                armorItems
        );
        event.registerItem(
                PsiAPI.SOCKETABLE_CAPABILITY,
                (stack, context) -> new ItemPsimetalArmor.ArmorSocketable(stack, 3),
                armorItems
        );
    }

    private static void registerAutoCasters(RegisterCapabilitiesEvent event) {
        ItemLike[] autoCasters = {
                PsitweaksItems.AUTO_CASTER_TICK.get(),
                PsitweaksItems.AUTO_CASTER_CUSTOM_TICK.get()
        };

        event.registerItem(
                Capabilities.ItemHandler.ITEM,
                (stack, context) -> ItemInlineCasterBase.createBulletHandler(stack, ItemCuriosCompat.SLOT_COUNT),
                autoCasters
        );
        event.registerItem(
                PsiAPI.PSI_BAR_DISPLAY_CAPABILITY,
                (stack, context) -> new ItemCuriosCompat.CurioSocketable(stack),
                autoCasters
        );
        event.registerItem(
                PsiAPI.SPELL_ACCEPTOR_CAPABILITY,
                (stack, context) -> new ItemCuriosCompat.CurioSocketable(stack),
                autoCasters
        );
        event.registerItem(
                PsiAPI.SOCKETABLE_CAPABILITY,
                (stack, context) -> new ItemCuriosCompat.CurioSocketable(stack),
                autoCasters
        );
    }

    private static void registerSpellMagazine(RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.ItemHandler.ITEM,
                (stack, context) -> ItemSpellMagazine.createBulletHandler(stack),
                PsitweaksItems.SPELL_MAGAZINE.get()
        );
        event.registerItem(
                PsiAPI.SPELL_ACCEPTOR_CAPABILITY,
                (stack, context) -> new ItemSpellMagazine.SpellMagazineSocketable(stack),
                PsitweaksItems.SPELL_MAGAZINE.get()
        );
        event.registerItem(
                PsiAPI.SOCKETABLE_CAPABILITY,
                (stack, context) -> new ItemSpellMagazine.SpellMagazineSocketable(stack),
                PsitweaksItems.SPELL_MAGAZINE.get()
        );
    }

    private static void registerPsimetalBow(RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.ItemHandler.ITEM,
                (stack, context) -> ItemInlineCasterBase.createBulletHandler(stack, ItemPsimetalBow.SLOT_COUNT),
                PsitweaksItems.PSIMETAL_BOW.get()
        );
        event.registerItem(
                PsiAPI.PSI_BAR_DISPLAY_CAPABILITY,
                (stack, context) -> new ToolSocketable(stack, ItemPsimetalBow.SLOT_COUNT),
                PsitweaksItems.PSIMETAL_BOW.get()
        );
        event.registerItem(
                PsiAPI.SPELL_ACCEPTOR_CAPABILITY,
                (stack, context) -> new ToolSocketable(stack, ItemPsimetalBow.SLOT_COUNT),
                PsitweaksItems.PSIMETAL_BOW.get()
        );
        event.registerItem(
                PsiAPI.SOCKETABLE_CAPABILITY,
                (stack, context) -> new ToolSocketable(stack, ItemPsimetalBow.SLOT_COUNT),
                PsitweaksItems.PSIMETAL_BOW.get()
        );
    }

    private static void registerSpellBullets(RegisterCapabilitiesEvent event) {
        ItemLike[] spellBulletItems = {
                PsitweaksItems.ADVANCED_SPELL_BULLET.get(),
                PsitweaksItems.ADVANCED_SPELL_BULLET_LOOP.get(),
                PsitweaksItems.ADVANCED_SPELL_BULLET_MINE.get(),
                PsitweaksItems.ADVANCED_SPELL_BULLET_CHARGE.get(),
                PsitweaksItems.ADVANCED_SPELL_BULLET_GRENADE.get(),
                PsitweaksItems.ADVANCED_SPELL_BULLET_PROJECTILE.get(),
                PsitweaksItems.ADVANCED_SPELL_BULLET_CIRCLE.get(),
                PsitweaksItems.RESONANT_SPELL_BULLET.get(),
                PsitweaksItems.RESONANT_SPELL_BULLET_LOOP.get(),
                PsitweaksItems.RESONANT_SPELL_BULLET_MINE.get(),
                PsitweaksItems.RESONANT_SPELL_BULLET_CHARGE.get(),
                PsitweaksItems.RESONANT_SPELL_BULLET_GRENADE.get(),
                PsitweaksItems.RESONANT_SPELL_BULLET_PROJECTILE.get(),
                PsitweaksItems.RESONANT_SPELL_BULLET_CIRCLE.get(),
                PsitweaksItems.SUBLIMATED_SPELL_BULLET.get(),
                PsitweaksItems.SUBLIMATED_SPELL_BULLET_LOOP.get(),
                PsitweaksItems.SUBLIMATED_SPELL_BULLET_MINE.get(),
                PsitweaksItems.SUBLIMATED_SPELL_BULLET_CHARGE.get(),
                PsitweaksItems.SUBLIMATED_SPELL_BULLET_GRENADE.get(),
                PsitweaksItems.SUBLIMATED_SPELL_BULLET_PROJECTILE.get(),
                PsitweaksItems.SUBLIMATED_SPELL_BULLET_CIRCLE.get(),
                PsitweaksItems.AWAKENED_SPELL_BULLET.get(),
                PsitweaksItems.AWAKENED_SPELL_BULLET_LOOP.get(),
                PsitweaksItems.AWAKENED_SPELL_BULLET_MINE.get(),
                PsitweaksItems.AWAKENED_SPELL_BULLET_CHARGE.get(),
                PsitweaksItems.AWAKENED_SPELL_BULLET_GRENADE.get(),
                PsitweaksItems.AWAKENED_SPELL_BULLET_PROJECTILE.get(),
                PsitweaksItems.AWAKENED_SPELL_BULLET_CIRCLE.get(),
                PsitweaksItems.TRANSCENDENT_SPELL_BULLET.get(),
                PsitweaksItems.TRANSCENDENT_SPELL_BULLET_LOOP.get(),
                PsitweaksItems.TRANSCENDENT_SPELL_BULLET_MINE.get(),
                PsitweaksItems.TRANSCENDENT_SPELL_BULLET_CHARGE.get(),
                PsitweaksItems.TRANSCENDENT_SPELL_BULLET_GRENADE.get(),
                PsitweaksItems.TRANSCENDENT_SPELL_BULLET_PROJECTILE.get(),
                PsitweaksItems.TRANSCENDENT_SPELL_BULLET_CIRCLE.get()
        };

        event.registerItem(
                PsiAPI.SPELL_ACCEPTOR_CAPABILITY,
                (stack, context) -> new ItemSpellBullet.SpellAcceptor(stack),
                spellBulletItems
        );
    }
}
