package com.moratan251.psitweaks.common.spells.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SpellPsiRefundAllocationTest {
    @Test
    void fullRefundRestoresPlayerAndCadPayments() {
        SpellPsiRefundAllocation.RefundPlan plan =
                SpellPsiRefundAllocation.calculate(150, 100, 100, 150);

        assertEquals(100, plan.playerPaid());
        assertEquals(50, plan.cadPaid());
        assertEquals(0, plan.unpaidCost());
        assertEquals(100, plan.playerRefund());
        assertEquals(50, plan.cadRefund());
        assertEquals(150, plan.totalRefund());
    }

    @Test
    void partialRefundReversesCadPaymentBeforePlayerPayment() {
        SpellPsiRefundAllocation.RefundPlan plan =
                SpellPsiRefundAllocation.calculate(150, 100, 100, 70);

        assertEquals(20, plan.playerRefund());
        assertEquals(50, plan.cadRefund());
        assertEquals(70, plan.totalRefund());
    }

    @Test
    void cumulativeRefundTargetsMatchSingleCombinedRefund() {
        SpellPsiRefundAllocation.RefundPlan first =
                SpellPsiRefundAllocation.calculate(150, 100, 100, 20);
        SpellPsiRefundAllocation.RefundPlan combined =
                SpellPsiRefundAllocation.calculate(150, 100, 100, 70);

        assertEquals(20, first.cadRefund());
        assertEquals(0, first.playerRefund());
        assertEquals(50, combined.cadRefund());
        assertEquals(20, combined.playerRefund());
    }

    @Test
    void refundsCancelUnpaidOverflowBeforeRestoringPsi() {
        SpellPsiRefundAllocation.RefundPlan smallRefund =
                SpellPsiRefundAllocation.calculate(250, 100, 100, 30);
        SpellPsiRefundAllocation.RefundPlan largerRefund =
                SpellPsiRefundAllocation.calculate(250, 100, 100, 70);

        assertEquals(50, smallRefund.unpaidCost());
        assertEquals(0, smallRefund.totalRefund());
        assertEquals(20, largerRefund.cadRefund());
        assertEquals(0, largerRefund.playerRefund());
    }

    @Test
    void refundAndInputsAreClampedToValidRanges() {
        SpellPsiRefundAllocation.RefundPlan plan =
                SpellPsiRefundAllocation.calculate(50, -10, 20, 500);

        assertEquals(0, plan.playerPaid());
        assertEquals(20, plan.cadPaid());
        assertEquals(30, plan.unpaidCost());
        assertEquals(20, plan.cadRefund());
        assertEquals(0, plan.playerRefund());
    }
}
