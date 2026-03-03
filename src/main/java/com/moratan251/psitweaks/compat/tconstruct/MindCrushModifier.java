package com.moratan251.psitweaks.compat.tconstruct;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

        tryApplyMindCrush(mob, modifier.getLevel());
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

        tryApplyMindCrush(mob, modifier.getLevel());

        return amount;
    }

    private static void tryApplyMindCrush(Mob mob, int modifierLevel) {
        if (mob.isNoAi()) {
            return;
        }

        float chance = modifierLevel * CHANCE_PER_LEVEL;
        if (mob.level().random.nextFloat() >= chance) {
            return;
        }

        mob.setNoAi(true);

        if (mob.level() instanceof ServerLevel serverLevel) {
            playIceBreakEffects(serverLevel, mob);
        }
    }

    private static void playIceBreakEffects(ServerLevel serverLevel, Mob mob) {
        double x = mob.getX();
        double y = mob.getY(0.5D);
        double z = mob.getZ();

        serverLevel.playSound(
                null,
                x,
                y,
                z,
                SoundEvents.GLASS_BREAK,
                SoundSource.PLAYERS,
                0.9F,
                1.1F
        );
        serverLevel.sendParticles(
                ParticleTypes.SNOWFLAKE,
                x,
                y,
                z,
                18,
                0.3D,
                0.35D,
                0.3D,
                0.01D
        );
        serverLevel.sendParticles(
                ParticleTypes.ITEM_SNOWBALL,
                x,
                y,
                z,
                10,
                0.25D,
                0.25D,
                0.25D,
                0.02D
        );
    }
}
