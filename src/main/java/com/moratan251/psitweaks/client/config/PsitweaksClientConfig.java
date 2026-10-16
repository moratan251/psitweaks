package com.moratan251.psitweaks.client.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class PsitweaksClientConfig {
    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        Pair<Client, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = pair.getLeft();
        CLIENT_SPEC = pair.getRight();
    }

    private PsitweaksClientConfig() {
    }

    public static final class Client {
        public final ModConfigSpec.BooleanValue cameraShake;
        public final ModConfigSpec.DoubleValue cameraShakeStrength;
        public final ModConfigSpec.BooleanValue barrierHud;

        private Client(ModConfigSpec.Builder builder) {
            builder.comment("Psitweaks client-only settings")
                    .push("spellProgrammer");

            builder.pop();

            builder.comment("Visual effects of combat spells")
                    .translation("psitweaks.configuration.effects")
                    .push("effects");

            cameraShake = builder
                    .comment("Shake the camera for ground shockwaves, meteor impacts and gravity collapses")
                    .translation("psitweaks.configuration.effects.camera_shake")
                    .define("cameraShake", true);

            cameraShakeStrength = builder
                    .comment("Strength of the camera shake (1.0 = default)")
                    .translation("psitweaks.configuration.effects.camera_shake_strength")
                    .defineInRange("cameraShakeStrength", 1.0, 0.0, 2.0);

            barrierHud = builder
                    .comment("Show the remaining shield of Trick: Barrier above the health bar")
                    .translation("psitweaks.configuration.effects.barrier_hud")
                    .define("barrierHud", true);

            builder.pop();
        }
    }
}
