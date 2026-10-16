package com.moratan251.psitweaks.common.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * The effect Trick: Barrier applied before 1.0.0. It now does nothing; Trick: Barrier raises a shield instead
 * (see {@code BarrierShields}). It stays registered so worlds saved with the effect still load cleanly.
 */
public class EffectBarrier extends MobEffect {
    public EffectBarrier() {
        super(MobEffectCategory.BENEFICIAL, 0x66FFE8);
    }
}
