package com.moratan251.psitweaks.common.effects;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PsitweaksEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Psitweaks.MOD_ID);

    public static final RegistryObject<MobEffect> PARADE =
            EFFECTS.register("parade", EffectParade::new);

    public static final RegistryObject<MobEffect> FLIGHT =
            EFFECTS.register("flight", EffectFlight::new);

    public static final RegistryObject<MobEffect> BARRIER =
            EFFECTS.register("barrier", EffectBarrier::new);

    public static final RegistryObject<MobEffect> HARDENING =
            EFFECTS.register("hardening", EffectHardening::new);



    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}