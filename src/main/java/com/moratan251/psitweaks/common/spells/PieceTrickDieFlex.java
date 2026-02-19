package com.moratan251.psitweaks.common.spells;

import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ISocketable;
import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.ISpellAcceptor;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.ItemCircleSpellBullet;

public class PieceTrickDieFlex extends PieceTrick {
    private static final double STOP_THRESHOLD = 1.0D;

    private SpellParam<Number> target;

    public PieceTrickDieFlex(Spell spell) {
        super(spell);
        this.setStatLabel(EnumSpellStat.COMPLEXITY, new StatLabel(1.0D));
    }

    @Override
    public void initParams() {
        this.addParam(this.target = new ParamNumber("psi.spellparam.target", SpellParam.BLUE, false, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        meta.addStat(EnumSpellStat.COMPLEXITY, 1);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        double timeVal = this.getNonnullParamValue(context, this.target).doubleValue();
        if (Math.abs(timeVal) < STOP_THRESHOLD) {
            refundRemainingPsi(context);
            context.stopped = true;
        }
        return null;
    }

    private void refundRemainingPsi(SpellContext context) {
        if (context.caster == null || context.actions == null || context.actions.isEmpty()) {
            return;
        }

        int remainingRawCost = calculateRemainingRawCost(context);
        if (remainingRawCost <= 0) {
            return;
        }

        ItemStack cad = PsiAPI.getPlayerCAD(context.caster);
        ItemStack spellContainer = getCastingSpellContainer(context, cad);
        int refundCost = ItemCAD.getRealCost(cad, spellContainer, remainingRawCost);
        if (isCircleSpellBullet(spellContainer)) {
            refundCost /= 20;
        }
        if (refundCost <= 0) {
            return;
        }

        PlayerDataHandler.get(context.caster).deductPsi(-refundCost, 0, true, true);
    }

    private ItemStack getCastingSpellContainer(SpellContext context, ItemStack cad) {
        ItemStack spellContainer = ItemStack.EMPTY;

        // Prefer the tool-selected spell container (e.g. exosuit/tools).
        if (context.tool != null && !context.tool.isEmpty() && ISocketable.isSocketable(context.tool)) {
            spellContainer = ISocketable.socketable(context.tool).getSelectedBullet();
        }

        // Flash Ring is not socketable and may not be treated as "container" by Psi
        if (spellContainer.isEmpty() && context.tool != null && !context.tool.isEmpty() && ISpellAcceptor.isAcceptor(context.tool)) {
            spellContainer = context.tool;
        }

        // Fallback to CAD selected bullet.
        if (spellContainer.isEmpty() && !cad.isEmpty() && ISocketable.isSocketable(cad)) {
            spellContainer = ISocketable.socketable(cad).getSelectedBullet();
        }

        return spellContainer;
    }

    private boolean isCircleSpellBullet(ItemStack spellContainer) {
        return !spellContainer.isEmpty() && spellContainer.getItem() instanceof ItemCircleSpellBullet;
    }

    private int calculateRemainingRawCost(SpellContext context) {
        long total = 0L;

        for (CompiledSpell.Action action : context.actions) {
            SpellMetadata pieceMetadata = new SpellMetadata();
            try {
                action.piece.addToMetadata(pieceMetadata);
            } catch (SpellCompilationException ignored) {
                continue;
            }

            int pieceCost = pieceMetadata.getStat(EnumSpellStat.COST);
            total += pieceCost;
            if (total >= Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            if (total <= Integer.MIN_VALUE) {
                return Integer.MIN_VALUE;
            }
        }

        return Math.max((int) total, 0);
    }
}
