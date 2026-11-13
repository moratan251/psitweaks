package com.moratan251.psitweaksqol.client.config;

import com.moratan251.psitweaksqol.client.gui.PiecePanelSize;
import com.moratan251.psitweaksqol.client.gui.PieceSortOrder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public final class PsitweaksQolConfig {
    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        Pair<Client, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = pair.getLeft();
        CLIENT_SPEC = pair.getRight();
    }

    private PsitweaksQolConfig() {
    }

    public static final class Client {
        public final ModConfigSpec.ConfigValue<List<? extends String>> pieceBookmarks;
        public final ModConfigSpec.ConfigValue<List<? extends String>> recentPieces;
        public final ModConfigSpec.EnumValue<PiecePanelSize> piecePanelSize;
        public final ModConfigSpec.BooleanValue keepSearchText;
        public final ModConfigSpec.BooleanValue rightClickClearsSearch;
        public final ModConfigSpec.BooleanValue rightClickClosesPanel;
        public final ModConfigSpec.BooleanValue showEnglishNames;
        public final ModConfigSpec.EnumValue<PieceSortOrder> pieceSortOrder;

        private Client(ModConfigSpec.Builder builder) {
            builder.comment("Psitweaks QoL client-only settings")
                    .push("spellProgrammer");

            pieceBookmarks = builder
                    .comment("Spell piece registry IDs bookmarked in the Spell Programmer")
                    .defineListAllowEmpty(
                            "pieceBookmarks",
                            List.of(),
                            () -> "",
                            value -> value instanceof String id && ResourceLocation.tryParse(id) != null
                    );

            recentPieces = builder
                    .comment("Spell piece registry IDs recently placed from the Spell Programmer piece panel, newest first")
                    .defineListAllowEmpty(
                            "recentPieces",
                            List.of(),
                            () -> "",
                            value -> value instanceof String id && ResourceLocation.tryParse(id) != null
                    );

            piecePanelSize = builder
                    .comment("Size of one page of the Spell Programmer piece panel:",
                            "SMALL = 5 x 5 (Psi default), MEDIUM = 7 x 6, LARGE = 9 x 7")
                    .defineEnum("piecePanelSize", PiecePanelSize.SMALL);

            keepSearchText = builder
                    .comment("Keep the text typed in the piece panel search field after the panel closes")
                    .define("keepSearchText", false);

            rightClickClearsSearch = builder
                    .comment("Right-clicking the piece panel search field clears its text")
                    .define("rightClickClearsSearch", true);

            rightClickClosesPanel = builder
                    .comment("Right-clicking the piece panel anywhere except its search field closes the panel")
                    .define("rightClickClosesPanel", false);

            showEnglishNames = builder
                    .comment("Add the English name to the tooltip of pieces in the piece panel",
                            "(no effect when the game language is English)")
                    .define("showEnglishNames", true);

            pieceSortOrder = builder
                    .comment("Order of spell pieces in the piece panel:",
                            "NAME = name in the current language (Psi default), ENGLISH_NAME = English name")
                    .defineEnum("pieceSortOrder", PieceSortOrder.NAME);

            builder.pop();
        }
    }
}
