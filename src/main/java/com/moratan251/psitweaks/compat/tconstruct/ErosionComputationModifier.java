package com.moratan251.psitweaks.compat.tconstruct;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.resources.ResourceLocation;
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
    private static final int DRAINED_PSI_PER_LEVEL = 150;
    private static final int COOLDOWN_TICKS = 10;
    private static final ResourceLocation NEXT_AVAILABLE_TICK = ResourceLocation.fromNamespaceAndPath(
            Psitweaks.MOD_ID,
            "erosion_computation_next_available_tick"
    );

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

        LivingEntity attacker = context.getAttacker();
        if (attacker == null) {
            return;
        }
        int currentTick = (int) attacker.level().getGameTime();
        if (isOnCooldown(tool, currentTick)) {
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

        if (attacker instanceof Player attackerPlayer) {
            PlayerDataHandler.PlayerData attackerData = PlayerDataHandler.get(attackerPlayer);
            int recoveredPsi = Math.max(1, drainedPsi);
            int maxRecoverable = Math.max(0, attackerData.getTotalPsi() - attackerData.getAvailablePsi());
            if (maxRecoverable > 0) {
                attackerData.deductPsi(-Math.min(recoveredPsi, maxRecoverable), 0, true, false);
            }
        }

        tool.getPersistentData().putInt(NEXT_AVAILABLE_TICK, currentTick + COOLDOWN_TICKS);

        if (!(target instanceof Player targetPlayer)) {
            return;
        }

        PlayerDataHandler.PlayerData targetData = PlayerDataHandler.get(targetPlayer);
        int targetDrain = Math.min(targetData.getAvailablePsi(), drainedPsi);
        if (targetDrain > 0) {
            targetData.deductPsi(targetDrain, 0, true, false);
        }
    }

    private static boolean isOnCooldown(IToolStackView tool, int currentTick) {
        if (!tool.getPersistentData().contains(NEXT_AVAILABLE_TICK)) {
            return false;
        }
        return currentTick < tool.getPersistentData().getInt(NEXT_AVAILABLE_TICK);
    }
}
