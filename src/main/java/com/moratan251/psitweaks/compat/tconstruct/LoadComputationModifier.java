package com.moratan251.psitweaks.compat.tconstruct;

import com.mojang.logging.LogUtils;
import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.resources.ResourceLocation;
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
    private static final int COOLDOWN_TICKS = 5;
    private static final int PSI_COST = 400;
    private static final float DAMAGE_PER_LEVEL = 5.0f;
    private static final ResourceLocation NEXT_AVAILABLE_TICK = ResourceLocation.fromNamespaceAndPath(
            Psitweaks.MOD_ID,
            "load_computation_next_available_tick"
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

        LivingEntity target = context.getLivingTarget();
        LivingEntity attacker = context.getAttacker();
        if (!(attacker instanceof Player player) || target == null) {
            return;
        }

        int currentTick = (int) attacker.level().getGameTime();
        if (isOnCooldown(tool, currentTick)) {
            return;
        }

        PlayerDataHandler.PlayerData playerData = PlayerDataHandler.get(player);
        int availablePsi = playerData.getAvailablePsi();
        if (availablePsi <= 0) {
            return;
        }

        int totalPsi = Math.max(playerData.getTotalPsi(), 1);
        int level = modifier.getLevel();

        int psiCost = PSI_COST;
        if (availablePsi < psiCost) {
            return;
        }

        float bonusDamage = level * DAMAGE_PER_LEVEL;
        boolean dealt = ToolAttackUtil.hurtNoInvulnerableTime(
                target,
                attacker,
                attacker.damageSources().indirectMagic(attacker, attacker),
                bonusDamage
        );
        if (dealt) {
            LOGGER.debug("Bonus Damage Dealt");
            playerData.deductPsi(psiCost, 0, true, false);
            tool.getPersistentData().putInt(NEXT_AVAILABLE_TICK, currentTick + COOLDOWN_TICKS);
        } else {
            LOGGER.debug("Bonus Damage Failed");
        }
    }

    private static boolean isOnCooldown(IToolStackView tool, int currentTick) {
        if (!tool.getPersistentData().contains(NEXT_AVAILABLE_TICK)) {
            return false;
        }
        return currentTick < tool.getPersistentData().getInt(NEXT_AVAILABLE_TICK);
    }
}
