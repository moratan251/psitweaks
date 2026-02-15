package com.moratan251.psitweaks.compat.tconstruct;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import slimeknights.mantle.data.predicate.damage.DamageSourcePredicate;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ModifyDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import vazkii.psi.common.core.handler.PlayerDataHandler;

public class PsiBufferModifier extends NoLevelsModifier implements ModifyDamageModifierHook {
    private static final int PSI_PER_DAMAGE = 25;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MODIFY_DAMAGE);
    }

    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slot, DamageSource source, float amount, boolean isDirectDamage) {
        if (amount <= 0.0F) {
            return amount;
        }
        if (!DamageSourcePredicate.CAN_PROTECT.matches(source)) {
            return amount;
        }

        if (!(context.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return amount;
        }

        PlayerDataHandler.PlayerData playerData = PlayerDataHandler.get(player);
        int availablePsi = playerData.getAvailablePsi();
        if (availablePsi <= 0) {
            return amount;
        }

        float maxReducibleByPsi = (float) availablePsi / PSI_PER_DAMAGE;
        float reduction = Math.min(amount, maxReducibleByPsi);
        if (reduction <= 0.0F) {
            return amount;
        }

        int psiCost;
        if (reduction >= maxReducibleByPsi) {
            psiCost = availablePsi;
        } else {
            psiCost = Math.max(1, (int) Math.ceil(reduction * PSI_PER_DAMAGE));
            psiCost = Math.min(psiCost, availablePsi);
        }
        playerData.deductPsi(psiCost, 0, true, false);
        return Math.max(0.0F, amount - reduction);
    }
}
