package com.moratan251.psitweaks.common.spells.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.ItemCircleSpellBullet;

public final class SpellPsiRefundLedger {
    public static final String CONTEXT_KEY = "psitweaks:spell_psi_refund_ledger";

    private final ItemStack castingCad;
    private final ItemStack pricingCad;
    private final ItemStack pricingSpellContainer;
    private final int chargedCost;
    private final int playerPsiBefore;
    private final int cadPsiBefore;

    private int cumulativeRawRefund;
    private int targetRefundCost;
    private int refundedToCad;
    private int refundedToPlayer;

    public SpellPsiRefundLedger(
            ItemStack castingCad,
            ItemStack pricingSpellContainer,
            int chargedCost,
            int playerPsiBefore,
            int cadPsiBefore) {
        this.castingCad = castingCad;
        this.pricingCad = castingCad.copy();
        this.pricingSpellContainer = pricingSpellContainer.copy();
        this.chargedCost = Math.max(chargedCost, 0);
        this.playerPsiBefore = Math.max(playerPsiBefore, 0);
        this.cadPsiBefore = Math.max(cadPsiBefore, 0);
    }

    public static SpellPsiRefundLedger get(SpellContext context) {
        Object value = context.customData.get(CONTEXT_KEY);
        return value instanceof SpellPsiRefundLedger ledger ? ledger : null;
    }

    public void refundRawCost(Player player, int rawRefundCost) {
        if (player == null || rawRefundCost <= 0 || chargedCost <= 0) {
            return;
        }

        cumulativeRawRefund = saturatingAdd(cumulativeRawRefund, rawRefundCost);
        int nominalRefund = ItemCAD.getRealCost(pricingCad, pricingSpellContainer, cumulativeRawRefund);
        if (pricingSpellContainer.getItem() instanceof ItemCircleSpellBullet) {
            nominalRefund /= 20;
        }

        targetRefundCost = Math.max(targetRefundCost, Math.min(Math.max(nominalRefund, 0), chargedCost));
        SpellPsiRefundAllocation.RefundPlan plan = SpellPsiRefundAllocation.calculate(
                chargedCost, playerPsiBefore, cadPsiBefore, targetRefundCost);

        int outstandingRefund = plan.totalRefund() - refundedToCad - refundedToPlayer;
        if (outstandingRefund <= 0) {
            return;
        }

        int cadTargetOutstanding = Math.max(plan.cadRefund() - refundedToCad, 0);
        int cadRefund = refundCad(player, Math.min(outstandingRefund, cadTargetOutstanding));
        refundedToCad += cadRefund;
        outstandingRefund -= cadRefund;

        if (outstandingRefund > 0) {
            refundedToPlayer += refundPlayer(player, outstandingRefund);
        }
    }

    private int refundCad(Player player, int requestedRefund) {
        if (requestedRefund <= 0
                || castingCad.isEmpty()
                || !(castingCad.getItem() instanceof ICAD cadItem)
                || !isOwnedBy(player, castingCad)) {
            return 0;
        }

        int before = Math.max(cadItem.getStoredPsi(castingCad), 0);
        cadItem.regenPsi(castingCad, requestedRefund);
        int after = Math.max(cadItem.getStoredPsi(castingCad), 0);
        return Math.max(after - before, 0);
    }

    private static int refundPlayer(Player player, int requestedRefund) {
        if (requestedRefund <= 0) {
            return 0;
        }

        PlayerDataHandler.PlayerData playerData = PlayerDataHandler.get(player);
        int before = Math.max(playerData.getAvailablePsi(), 0);
        int headroom = Math.max(playerData.getTotalPsi() - before, 0);
        int refund = Math.min(requestedRefund, headroom);
        if (refund <= 0) {
            return 0;
        }

        // Psi has no additive player-Psi API. Use its synchronized deduction path only for the
        // bounded amount that the ledger attributes to the player pool.
        playerData.deductPsi(-refund, 0, true, true);
        return Math.max(playerData.getAvailablePsi() - before, 0);
    }

    private static boolean isOwnedBy(Player player, ItemStack target) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot) == target) {
                return true;
            }
        }
        return false;
    }

    private static int saturatingAdd(int left, int right) {
        long sum = (long) left + right;
        return sum >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) sum;
    }
}
