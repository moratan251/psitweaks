package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.Psitweaks;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Advancements that code awards; their only criterion is {@link #CODE_TRIGGERED} (minecraft:impossible). */
public final class PsitweaksAdvancements {
    public static final String CODE_TRIGGERED = "code_triggered";
    /** Hidden: Trick: Phonon Maser lit a Fusion Reactor. Loads only with Mekanism Generators. */
    public static final ResourceLocation PHONON_MASER_FUSION_IGNITION = Psitweaks.location("main/phonon_maser_fusion_ignition");
    /** Hidden: a Heat Zone and a Freeze Zone of one caster split one box and started the Inferno. */
    public static final ResourceLocation INFERNO = Psitweaks.location("main/inferno");
    /** Hidden: the scorching half of an Inferno superheated a Create Blaze Burner. Loads only with Create. */
    public static final ResourceLocation INFERNO_SUPERHEAT = Psitweaks.location("main/inferno_superheat");

    private PsitweaksAdvancements() {
    }

    /** Does nothing when the advancement is not loaded (its mod is missing, or a datapack removed it). */
    public static void award(ServerPlayer player, ResourceLocation id) {
        AdvancementHolder advancement = player.server.getAdvancements().get(id);
        if (advancement != null) {
            player.getAdvancements().award(advancement, CODE_TRIGGERED);
        }
    }
}
