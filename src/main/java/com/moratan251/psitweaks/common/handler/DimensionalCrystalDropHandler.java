package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.common.items.PsitweaksItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.spell.PreSpellCastEvent;
import vazkii.psi.api.spell.SpellCastEvent;
import vazkii.psi.api.spell.SpellContext;

public final class DimensionalCrystalDropHandler {
    // Test-friendly tuning constants; adjust later.
    // Drop chance = min(MAX_CHANCE, consumedPsi * CHANCE_PER_PSI).
    private static final float CHANCE_PER_PSI = 0.00001F;
    private static final float MAX_CHANCE = 1.0F;
    private static final String COST_CONTEXT_KEY = "psitweaks:dimensional_crystal_cost";

    private DimensionalCrystalDropHandler() {
    }

    public static void onPreSpellCast(PreSpellCastEvent event) {
        if (event.isCanceled() || event.getContext() == null) {
            return;
        }
        event.getContext().customData.put(COST_CONTEXT_KEY, Math.max(event.getCost(), 0));
    }

    public static void onSpellCast(SpellCastEvent event) {
        Player player = event.player;
        if (player.level().isClientSide) {
            return;
        }

        float chance = Math.min(MAX_CHANCE, getConsumedPsi(event.context) * CHANCE_PER_PSI);
        if (chance <= 0.0F || player.getRandom().nextFloat() >= chance) {
            return;
        }

        ItemStack stack = new ItemStack(PsitweaksItems.DIMENSIONAL_CRYSTAL.get());
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static int getConsumedPsi(SpellContext context) {
        if (context == null) {
            return 0;
        }
        Object cost = context.customData.get(COST_CONTEXT_KEY);
        return cost instanceof Integer value ? Math.max(value, 0) : 0;
    }
}
