package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import net.minecraft.core.BlockPos;
import vazkii.psi.api.internal.MathHelper;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellHelpers;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * 作動式: 収穫(列)。
 * 位置から対象の方向へ最大数ブロック並ぶ作物を、作動式: 収穫と同じように収穫する。Psi 本体の耕作(列)と同じく、
 * 射程外のブロックは飛ばす。
 */
public class PieceTrickHarvestSequence extends PieceTrick {
    private static final int POTENCY_PER_BLOCK = 20;
    private static final int COST_BASE = 48;
    private static final int COST_PER_EXTRA_BLOCK = 28;

    private SpellParam<Vector3> position;
    private SpellParam<Vector3> target;
    private SpellParam<Number> maxBlocks;

    public PieceTrickHarvestSequence(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(SpellParam.GENERIC_NAME_MAX, true).mul(POTENCY_PER_BLOCK));
        setStatLabel(EnumSpellStat.COST, new StatLabel(SpellParam.GENERIC_NAME_MAX, true).sub(1).parenthesize()
                .mul(COST_PER_EXTRA_BLOCK).add(COST_BASE));
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(target = new ParamVector(SpellParam.GENERIC_NAME_TARGET, SpellParam.GREEN, false, false));
        addParam(maxBlocks = new ParamNumber(SpellParam.GENERIC_NAME_MAX, SpellParam.RED, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        double max = SpellHelpers.ensurePositiveAndNonzero(this, maxBlocks);
        meta.addStat(EnumSpellStat.POTENCY, (int) (max * POTENCY_PER_BLOCK));
        meta.addStat(EnumSpellStat.COST, (int) (COST_BASE + (max - 1) * COST_PER_EXTRA_BLOCK));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = SpellHelpers.getVector3(this, context, position, true, false);
        Vector3 targetVal = SpellHelpers.getVector3(this, context, target, false, false);
        int max = getNotNullParamValue(context, maxBlocks).intValue();

        HarvestHelper.Harvest harvest = HarvestHelper.begin(context);
        if (harvest == null) {
            return null;
        }
        Vector3 end = positionVal.copy().add(targetVal.copy().normalize().multiply(max));
        for (BlockPos pos : MathHelper.getBlocksAlongRay(positionVal.toVec3D(), end.toVec3D(), max)) {
            if (SpellHelpers.isBlockPosInRadius(context, pos)) {
                harvest.harvest(pos);
            }
        }
        harvest.finish();
        return null;
    }
}
