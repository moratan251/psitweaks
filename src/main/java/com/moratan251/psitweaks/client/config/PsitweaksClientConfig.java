package com.moratan251.psitweaks.client.config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

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
        public final ModConfigSpec.ConfigValue<List<? extends String>> pieceBookmarks;

        private Client(ModConfigSpec.Builder builder) {
            builder.comment("Psitweaks client-only settings")
                    .push("spellProgrammer");

            pieceBookmarks = builder
                    .comment("Spell piece registry IDs bookmarked in the Spell Programmer")
                    .defineListAllowEmpty(
                            "pieceBookmarks",
                            List.of(),
                            () -> "",
                            value -> value instanceof String id && ResourceLocation.tryParse(id) != null
                    );

            builder.pop();
        }
    }
}
