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
        private Client(ModConfigSpec.Builder builder) {
            builder.comment("Psitweaks client-only settings")
                    .push("spellProgrammer");

            builder.pop();
        }
    }
}
