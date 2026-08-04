package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.common.attachments.PsitweaksAttachments;
import com.moratan251.psitweaks.common.items.PsitweaksItems;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Hardcoded fall conversion: a dropped Quantum Factor that has fallen
 * {@value #FALL_DISTANCE_THRESHOLD} blocks turns into a Graviton Factor in mid-air.
 * <p>
 * Conversion candidates are marked once on entity join with a data attachment so the
 * per-tick check only costs an instanceof and an attachment lookup for other entities.
 */
public final class FallConversionHandler {
    private static final float FALL_DISTANCE_THRESHOLD = 380.0f;

    private FallConversionHandler() {
    }

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof ItemEntity item
                && item.getItem().is(PsitweaksItems.QUANTUM_FACTOR.get())) {
            item.setData(PsitweaksAttachments.FALL_CONVERSION, true);
        }
    }

    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity item) || item.level().isClientSide()) {
            return;
        }
        if (!item.hasData(PsitweaksAttachments.FALL_CONVERSION)
                || item.fallDistance < FALL_DISTANCE_THRESHOLD) {
            return;
        }
        ItemStack converted = new ItemStack(PsitweaksItems.GRAVITON_FACTOR.get(), item.getItem().getCount());
        item.setItem(converted);
        item.removeData(PsitweaksAttachments.FALL_CONVERSION);
    }
}
