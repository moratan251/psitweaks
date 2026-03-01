package com.moratan251.psitweaks.compat.tconstruct;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.predicate.damage.DamageSourcePredicate;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ModifyDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class MindCrushModifier extends Modifier implements MeleeHitModifierHook, ModifyDamageModifierHook {
    private static final float CHANCE_PER_LEVEL = 0.05f;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT, ModifierHooks.MODIFY_DAMAGE);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (damageDealt <= 0.0F) {
            return;
        }

        LivingEntity target = context.getLivingTarget();
        if (!(target instanceof Mob mob) || mob.level().isClientSide()) {
            return;
        }

        float chance = modifier.getLevel() * CHANCE_PER_LEVEL;
        if (mob.level().random.nextFloat() < chance) {
            mob.setNoAi(true);
        }
    }

    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slot, DamageSource source, float amount, boolean isDirectDamage) {
        if (amount <= 0.0F) {
            return amount;
        }
        if (!DamageSourcePredicate.CAN_PROTECT.matches(source)) {
            return amount;
        }

        Entity attacker = source.getEntity();
        if (!(attacker instanceof Mob mob) || mob.level().isClientSide()) {
            return amount;
        }

        float chance = modifier.getLevel() * CHANCE_PER_LEVEL;
        if (mob.level().random.nextFloat() < chance) {
            mob.setNoAi(true);
        }

        return amount;
    }
}
