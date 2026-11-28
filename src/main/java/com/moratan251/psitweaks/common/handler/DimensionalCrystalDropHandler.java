package com.moratan251.psitweaks.common.handler;

import com.moratan251.psitweaks.common.items.PsitweaksItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.spell.PreSpellCastEvent;
import vazkii.psi.api.spell.SpellCastEvent;
import vazkii.psi.api.spell.SpellContext;

public final class DimensionalCrystalDropHandler {
    // Drop chance = min(MAX_CHANCE, consumedPsi * CHANCE_PER_PSI).
    private static final float CHANCE_PER_PSI = 0.000001F;
    private static final float MAX_CHANCE = 1.0F;
    private static final String COST_CONTEXT_KEY = "psitweaks:dimensional_crystal_cost";
    private static final String OBTAINED_MESSAGE = "message.psitweaks.dimensional_crystal.obtained";

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
        Component name = stack.getDisplayName();
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        notifyObtained(player, name);
        if (player instanceof ServerPlayer serverPlayer) {
            PsitweaksAdvancements.award(serverPlayer, PsitweaksAdvancements.DIMENSIONAL_CRYSTAL_FROM_CAST);
        }
    }

    /**
     * Rings an amethyst chime for the caster alone and shows a message above their hotbar, since the crystal is rare and
     * easy to miss.
     */
    private static void notifyObtained(Player player, Component crystalName) {
        player.playNotifySound(SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable(OBTAINED_MESSAGE, crystalName)
                .withStyle(ChatFormatting.LIGHT_PURPLE), true);
    }

    private static int getConsumedPsi(SpellContext context) {
        if (context == null) {
            return 0;
        }
        Object cost = context.customData.get(COST_CONTEXT_KEY);
        return cost instanceof Integer value ? Math.max(value, 0) : 0;
    }
}
