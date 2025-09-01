package com.moratan251.psitweaks.common.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class EffectFlight extends MobEffect {

    public EffectFlight() {
        super(MobEffectCategory.BENEFICIAL, 0x98D982); // 色は自由
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (entity instanceof Player player && !player.level().isClientSide) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
        }
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity entity, net.minecraft.world.entity.ai.attributes.@NotNull AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);

        if (entity instanceof Player player && !player.level().isClientSide) {
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // 毎tick applyEffectTick を呼ぶ
    }
}