package com.moratan251.psitweaks.common.effects;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PsitweaksEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, Psitweaks.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> PARADE =
            EFFECTS.register("parade", EffectParade::new);
    public static final DeferredHolder<MobEffect, MobEffect> FLIGHT =
            EFFECTS.register("flight", EffectFlight::new);
    public static final DeferredHolder<MobEffect, MobEffect> BARRIER =
            EFFECTS.register("barrier", EffectBarrier::new);
    public static final DeferredHolder<MobEffect, MobEffect> HARDENING =
            EFFECTS.register("hardening", EffectHardening::new);
    public static final DeferredHolder<MobEffect, MobEffect> RADIATION_FILTER =
            EFFECTS.register("radiation_filter", EffectRadiationFilter::new);

    private PsitweaksEffects() {
    }

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}
