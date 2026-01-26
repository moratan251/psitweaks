package com.moratan251.psitweaks.common.spells;


import com.moratan251.psitweaks.common.entities.EntityTimeAccelerator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

import java.util.Optional;

public class PieceTrickTimeAccelerate extends PieceTrick {

    private SpellParam<Vector3> position;
    private SpellParam<Number> power;
    private SpellParam<Number> timeSeconds;

    public PieceTrickTimeAccelerate(Spell spell) {
        super(spell);

        // コスト設計は仮。倍率と時間で増えるのが自然
        // (あとで調整しやすいようラベルだけ置く)
        this.setStatLabel(EnumSpellStat.COST, new StatLabel("psi.spellparam.power", true).mul(200).floor());
        this.setStatLabel(EnumSpellStat.POTENCY, new StatLabel("psi.spellparam.time", true).mul(50).floor());
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(power = new ParamNumber("psi.spellparam.power", SpellParam.RED, false, false)); // 整数想定
        addParam(timeSeconds = new ParamNumber("psi.spellparam.time", SpellParam.GREEN, false, false)); // 秒（整数）
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);

        Double p = (Double) getParamEvaluation(power);
        Double t = (Double) getParamEvaluation(timeSeconds);

        if (p == null || p <= 0 || p.intValue() != p) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_INTEGER, x, y);
        }
        if (t == null || t <= 0 || t.intValue() != t) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_INTEGER, x, y);
        }

        int rate = p.intValue();
        int seconds = t.intValue();

        // ざっくり：倍率と秒数に比例して重くなるのでコストも比例
        meta.addStat(EnumSpellStat.COST, rate * seconds * 2);
        meta.addStat(EnumSpellStat.POTENCY, rate * 10);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 posVec = getParamValue(context, position);
        Number powerVal = getParamValue(context, power);
        Number timeVal = getParamValue(context, timeSeconds);

        if (posVec == null) throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);

        int rate = powerVal.intValue();
        int seconds = timeVal.intValue();
        if (rate <= 0) throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        if (seconds <= 0) throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);

        if (!context.isInRadius(posVec)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }

        if (!(context.caster.level() instanceof ServerLevel level)) {
            return null;
        }

        BlockPos pos = posVec.toBlockPos();
        if (!level.isLoaded(pos)) {
            return null;
        }

        BlockState state = level.getBlockState(pos);
        BlockEntity be = level.getBlockEntity(pos);

        // 対象適性チェック（Time in a Bottle と同じ発想）
        if (be == null && !state.isRandomlyTicking()) {
            return null;
        }

        int durationTicks = seconds * 20;

        // 既存があるなら remainingTime を最大化
        Optional<EntityTimeAccelerator> existing =
                level.getEntitiesOfClass(EntityTimeAccelerator.class, new AABB(pos)).stream().findFirst();

        if (existing.isPresent()) {
            EntityTimeAccelerator entity = existing.get();
            entity.setTimeRate(rate);
            entity.setRemainingTime(Math.max(entity.getRemainingTime(), durationTicks));
        } else {
            EntityTimeAccelerator entity = new EntityTimeAccelerator(level, pos);
            entity.setTimeRate(rate);
            entity.setRemainingTime(durationTicks);
            level.addFreshEntity(entity);
        }

        return null;
    }
}