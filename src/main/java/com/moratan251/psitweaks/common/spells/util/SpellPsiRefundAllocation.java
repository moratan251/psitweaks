package com.moratan251.psitweaks.common.spells.util;

final class SpellPsiRefundAllocation {
    private SpellPsiRefundAllocation() {
    }

    static RefundPlan calculate(int chargedCost, int playerPsiBefore, int cadPsiBefore, int requestedRefund) {
        int charge = Math.max(chargedCost, 0);
        int playerAvailable = Math.max(playerPsiBefore, 0);
        int cadAvailable = Math.max(cadPsiBefore, 0);
        int refund = Math.min(Math.max(requestedRefund, 0), charge);

        int playerPaid = Math.min(charge, playerAvailable);
        int cadPaid = Math.min(charge - playerPaid, cadAvailable);
        int unpaidCost = charge - playerPaid - cadPaid;

        int effectiveCost = charge - refund;
        int desiredPlayerUse = Math.min(effectiveCost, playerAvailable);
        int desiredCadUse = Math.min(effectiveCost - desiredPlayerUse, cadAvailable);

        return new RefundPlan(
                playerPaid,
                cadPaid,
                unpaidCost,
                Math.max(playerPaid - desiredPlayerUse, 0),
                Math.max(cadPaid - desiredCadUse, 0));
    }

    record RefundPlan(
            int playerPaid,
            int cadPaid,
            int unpaidCost,
            int playerRefund,
            int cadRefund) {
        int totalRefund() {
            return playerRefund + cadRefund;
        }
    }
}
