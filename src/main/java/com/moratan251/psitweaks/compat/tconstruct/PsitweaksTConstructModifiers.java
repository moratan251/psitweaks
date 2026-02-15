package com.moratan251.psitweaks.compat.tconstruct;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraftforge.eventbus.api.IEventBus;
import slimeknights.tconstruct.library.modifiers.util.ModifierDeferredRegister;
import slimeknights.tconstruct.library.modifiers.util.StaticModifier;

public final class PsitweaksTConstructModifiers {
    private static final ModifierDeferredRegister MODIFIERS = ModifierDeferredRegister.create(Psitweaks.MOD_ID);

    public static final StaticModifier<PsiBufferModifier> PSI_BUFFER =
            MODIFIERS.register("psi_buffer", PsiBufferModifier::new);
    public static final StaticModifier<LoadComputationModifier> RESONANCE_BLADE =
            MODIFIERS.register("load_computation", LoadComputationModifier::new);
    public static final StaticModifier<CastingAssistModifier> CASTING_ASSIST =
            MODIFIERS.register("casting_assist", CastingAssistModifier::new);
    public static final StaticModifier<ErosionComputationModifier> EROSION_COMPUTATION =
            MODIFIERS.register("erosion_computation", ErosionComputationModifier::new);
    public static final StaticModifier<PsicologicalModifier> PSICOLOGICAL =
            MODIFIERS.register("psicological", PsicologicalModifier::new);
    public static final StaticModifier<GlobalTravelerModifier> GLOBAL_TRAVELER =
            MODIFIERS.register("global_traveler", GlobalTravelerModifier::new);

    private PsitweaksTConstructModifiers() {
    }

    public static void register(IEventBus eventBus) {
        MODIFIERS.register(eventBus);
    }
}
