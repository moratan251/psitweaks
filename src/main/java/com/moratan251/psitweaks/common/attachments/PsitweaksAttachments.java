package com.moratan251.psitweaks.common.attachments;

import com.moratan251.psitweaks.Psitweaks;
import com.moratan251.psitweaks.common.spells.combat.BarrierShield;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class PsitweaksAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Psitweaks.MOD_ID);

    /**
     * Marker for item entities subject to fall conversion.
     * Transient by design: it is re-applied by {@code FallConversionHandler} on every entity join.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> FALL_CONVERSION =
            ATTACHMENT_TYPES.register("fall_conversion", () -> AttachmentType.builder(() -> false).build());

    /**
     * Shield of Trick: Barrier. Saved with the entity, synced to everyone who sees it (the owner reads it for the HUD)
     * and dropped on death because it is not copied to the respawned player.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BarrierShield>> BARRIER_SHIELD =
            ATTACHMENT_TYPES.register("barrier_shield", () -> AttachmentType.builder(() -> BarrierShield.NONE)
                    .serialize(BarrierShield.CODEC, BarrierShield::isPresent)
                    .sync(BarrierShield.STREAM_CODEC)
                    .build());

    private PsitweaksAttachments() {
    }

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
