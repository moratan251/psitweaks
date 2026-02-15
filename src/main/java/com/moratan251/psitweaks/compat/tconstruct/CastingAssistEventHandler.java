package com.moratan251.psitweaks.compat.tconstruct;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import vazkii.psi.api.spell.PreSpellCastEvent;

public class CastingAssistEventHandler {
    private static final float COST_REDUCTION = 0.2F;

    @SubscribeEvent
    public void onPreSpellCast(PreSpellCastEvent event) {
        Player player = event.getPlayer();
        if (!hasCastingAssist(player)) {
            return;
        }

        int originalCost = event.getCost();
        if (originalCost <= 0) {
            return;
        }

        int adjustedCost = Math.max(0, Math.round(originalCost * (1.0F - COST_REDUCTION)));
        if (adjustedCost < originalCost) {
            event.setCost(adjustedCost);
        }
    }

    private static boolean hasCastingAssist(Player player) {
        if (hasCastingAssist(player.getMainHandItem()) || hasCastingAssist(player.getOffhandItem())) {
            return true;
        }
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (hasCastingAssist(armorStack)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasCastingAssist(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IModifiable) || !ToolStack.isInitialized(stack)) {
            return false;
        }

        ToolStack toolStack = ToolStack.from(stack);
        return toolStack.getModifierLevel(PsitweaksTConstructModifiers.CASTING_ASSIST.getId()) > 0;
    }
}
