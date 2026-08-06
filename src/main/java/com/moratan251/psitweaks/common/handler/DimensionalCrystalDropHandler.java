package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.common.items.PsitweaksItems;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.spell.PreSpellCastEvent;
import vazkii.psi.api.spell.SpellCastEvent;

public final class DimensionalCrystalDropHandler {
    private static final float CHANCE_PER_PSI = 0.00001F;
    private static final float MAX_CHANCE = 1.0F;
    private static final String CONTEXT_KEY = "psitweaks:dimensional_crystal_cost";

    private DimensionalCrystalDropHandler() {
    }

    public static void onPreSpellCast(PreSpellCastEvent event) {
        if (event.isCanceled() || event.getContext() == null) {
            return;
        }
        event.getContext().customData.put(CONTEXT_KEY, Math.max(event.getCost(), 0));
    }

    public static void onSpellCast(SpellCastEvent event) {
        if (event.player.level().isClientSide || event.context == null) {
            return;
        }

        int consumedPsi = getConsumedPsi(event);
        float chance = Math.min(MAX_CHANCE, consumedPsi * CHANCE_PER_PSI);
        if (chance <= 0.0F || event.player.getRandom().nextFloat() >= chance) {
            return;
        }

        ItemStack crystal = new ItemStack(PsitweaksItems.DIMENSIONAL_CRYSTAL.get());
        if (!event.player.getInventory().add(crystal)) {
            event.player.drop(crystal, false);
        }
    }

    private static int getConsumedPsi(SpellCastEvent event) {
        Object value = event.context.customData.get(CONTEXT_KEY);
        return value instanceof Integer cost ? Math.max(cost, 0) : 0;
    }
}
