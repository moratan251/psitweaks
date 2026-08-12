package com.moratan251.psitweaksqol.client.config;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class PsitweaksQolConfig {
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        Pair<Client, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = pair.getLeft();
        CLIENT_SPEC = pair.getRight();
    }

    private PsitweaksQolConfig() {
    }

    public static final class Client {
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> pieceBookmarks;

        private Client(ForgeConfigSpec.Builder builder) {
            builder.comment("Psitweaks QoL client-only settings")
                    .push("spellProgrammer");

            pieceBookmarks = builder
                    .comment("Spell piece registry IDs bookmarked in the Spell Programmer")
                    .defineListAllowEmpty(
                            "pieceBookmarks",
                            List.of(),
                            value -> value instanceof String id && ResourceLocation.tryParse(id) != null
                    );

            builder.pop();
        }
    }
}
