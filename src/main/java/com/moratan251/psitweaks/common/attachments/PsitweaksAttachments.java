package com.moratan251.psitweaks.common.attachments;

import com.moratan251.psitweaks.Psitweaks;
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

    private PsitweaksAttachments() {
    }

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
