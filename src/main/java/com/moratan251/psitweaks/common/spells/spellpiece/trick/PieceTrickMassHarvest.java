package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.spells.PsitweaksSpellParams;
import com.moratan251.psitweaks.common.spells.param.ParamVectorListWrapper;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * 作動式: 大規模収穫。
 * Vector List(Block List も可)の座標の作物を、作動式: 収穫と同じように収穫する。重複した座標は1回だけ数え、
 * 先頭から最大数の座標までを扱う。作動式: 大規模ブロック破壊と同じく、扱う座標がすべて射程内でなければエラー。
 * 収穫はサーバーを止めないよう次のtickから少しずつ進め({@link MassHarvestScheduler})、収穫物は最後にまとめて術者へ渡す。
 */
public class PieceTrickMassHarvest extends PieceTrick {
    /** The most positions one cast handles, like Trick: Mass Block Break. */
    public static final int MAX_POSITIONS = MassBlockBreakHelper.MAX_REGION_BLOCKS;
    private static final int POTENCY_BASE = 100;
    private static final double POTENCY_PER_BLOCK = 1;
    private static final int COST_BASE = 200;
    private static final double COST_PER_BLOCK = 0.5;

    private SpellParam<VectorListWrapper> positions;
    private SpellParam<Number> maxBlocks;

    public PieceTrickMassHarvest(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(POTENCY_BASE)
                .add(new StatLabel(SpellParam.GENERIC_NAME_MAX, true).mul(POTENCY_PER_BLOCK).floor()));
        setStatLabel(EnumSpellStat.COST, new StatLabel(COST_BASE)
                .add(new StatLabel(SpellParam.GENERIC_NAME_MAX, true).mul(COST_PER_BLOCK).floor()));
    }

    @Override
    public void initParams() {
        addParam(positions = new ParamVectorListWrapper(SpellParam.GENERIC_NAME_LIST, PsitweaksSpellParams.VECTOR_LIST_COLOR, false, false));
        addParam(maxBlocks = new ParamNumber(SpellParam.GENERIC_NAME_MAX, SpellParam.RED, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        Object evaluation = getParamEvaluation(maxBlocks);
        if (!(evaluation instanceof Number number) || !isPositiveInteger(number)) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_INTEGER, x, y);
        }
        int count = Math.min(number.intValue(), MAX_POSITIONS);
        meta.addStat(EnumSpellStat.POTENCY, (int) (POTENCY_BASE + count * POTENCY_PER_BLOCK));
        meta.addStat(EnumSpellStat.COST, (int) (COST_BASE + count * COST_PER_BLOCK));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        VectorListWrapper positionsVal = getNotNullParamValue(context, positions);
        Number maxVal = getNotNullParamValue(context, maxBlocks);
        if (!isPositiveInteger(maxVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        List<BlockPos> targets = collectTargets(context, positionsVal, Math.min(maxVal.intValue(), MAX_POSITIONS));

        HarvestHelper.Harvest harvest = HarvestHelper.begin(context);
        if (harvest == null) {
            return null;
        }
        MassHarvestScheduler.schedule(harvest, targets, context.focalPoint.blockPosition());
        return null;
    }

    /** The first {@code limit} distinct block positions, each checked against the spell radius. */
    private static List<BlockPos> collectTargets(SpellContext context, VectorListWrapper vectors, int limit)
            throws SpellRuntimeException {
        Set<BlockPos> seen = new LinkedHashSet<>();
        for (Vector3 vector : vectors) {
            if (seen.size() >= limit) {
                break;
            }
            if (vector == null) {
                throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
            }
            if (seen.add(vector.toBlockPos()) && !context.isInRadius(vector)) {
                throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
            }
        }
        return new ArrayList<>(seen);
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.empty();
    }

    private static boolean isPositiveInteger(Number number) {
        double value = number.doubleValue();
        return Double.isFinite(value) && value > 0 && value == Math.floor(value);
    }
}
