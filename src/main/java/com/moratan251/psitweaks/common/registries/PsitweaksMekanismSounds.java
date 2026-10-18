package com.moratan251.psitweaks.common.registries;

import com.moratan251.psitweaks.Psitweaks;
import mekanism.common.registration.impl.SoundEventDeferredRegister;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;

public final class PsitweaksMekanismSounds {
    /**
     * Mekanism only tracks (and can later stop) looping machine sounds whose namespace starts with "mekanism"
     * ({@code SoundHandler.onTilePlaySound}); under "psitweaks" the sound would keep looping after the machine stops.
     * Assets for these events live in {@code assets/mekanism_psitweaks/}.
     */
    private static final String SOUND_NAMESPACE = "mekanism_" + Psitweaks.MOD_ID;
    public static final SoundEventDeferredRegister SOUND_EVENTS = new SoundEventDeferredRegister(SOUND_NAMESPACE);

    public static final SoundEventRegistryObject<SoundEvent> PROGRAM_RESEARCHER = SOUND_EVENTS.register("tile.machine.program_researcher");

    private PsitweaksMekanismSounds() {
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
