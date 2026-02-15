package com.moratan251.psitweaks.compat.tconstruct;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.helper.ToolAttackUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import vazkii.psi.common.core.handler.PlayerDataHandler;


public class LoadComputationModifier extends Modifier implements MeleeHitModifierHook {


    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (damageDealt <= 0.0F) {
            return;
        }

        LivingEntity target = context.getLivingTarget();
        LivingEntity attacker = context.getAttacker();
        if (!(attacker instanceof Player player) || target == null) {
            return;
        }

        PlayerDataHandler.PlayerData playerData = PlayerDataHandler.get(player);
        int availablePsi = playerData.getAvailablePsi();
        if (availablePsi <= 0) {
            return;
        }

        int totalPsi = Math.max(playerData.getTotalPsi(), 1);
        int level = modifier.getLevel();

        if (availablePsi * 2 < totalPsi) {
            return;
        }

        int psiCost = 200;
        if (availablePsi < psiCost) {
            return;
        }

        float bonusDamage = level * 8.0F;
        boolean dealt = ToolAttackUtil.hurtNoInvulnerableTime(
                target,
                attacker,
                attacker.damageSources().indirectMagic(attacker, attacker),
                bonusDamage
        );
        if (dealt) {
            LOGGER.debug("Bonus Damage Dealt");
            playerData.deductPsi(psiCost, 0, true, false);
        } else {
            LOGGER.debug("Bonus Damage Failed");
        }
    }
}
