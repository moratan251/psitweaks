package com.moratan251.psitweaks.compat.tconstruct;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import vazkii.psi.common.core.handler.PlayerDataHandler;

public class ErosionComputationModifier extends Modifier implements MeleeHitModifierHook {
    private static final int DRAINED_PSI_PER_LEVEL = 200;

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
        if (target == null) {
            return;
        }

        int level = modifier.getLevel();
        int duration = 40 + level * 20;
        int amplifier = Math.max(0, level - 1);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, amplifier, false, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, amplifier, false, true, true));

        int drainedPsi = DRAINED_PSI_PER_LEVEL * level;

        LivingEntity attacker = context.getAttacker();
        if (attacker instanceof Player attackerPlayer) {
            PlayerDataHandler.PlayerData attackerData = PlayerDataHandler.get(attackerPlayer);
            attackerData.deductPsi(-Math.max(1, drainedPsi / 2), 0, true, false);
        }

        if (!(target instanceof Player targetPlayer)) {
            return;
        }

        PlayerDataHandler.PlayerData targetData = PlayerDataHandler.get(targetPlayer);
        int targetDrain = Math.min(targetData.getAvailablePsi(), drainedPsi);
        if (targetDrain > 0) {
            targetData.deductPsi(targetDrain, 0, true, false);
        }
    }
}
