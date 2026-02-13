package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.entities.EntityPhononMaserBeam;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickPhononMaser extends PieceTrick {

    SpellParam<Number> power;
    SpellParam<Number> time;

    public PieceTrickPhononMaser(Spell spell) {
        super(spell);
        this.setStatLabel(EnumSpellStat.POTENCY,
                (new StatLabel("psi.spellparam.power", true)).mul(50.0).floor().add(500.0));
        this.setStatLabel(EnumSpellStat.COST,
                (new StatLabel("psi.spellparam.power", true))
                        .mul(new StatLabel("psi.spellparam.time", true)).mul(700.0).floor());
    }

    @Override
    public void initParams() {
        // 威力: 正の整数のみ
        this.addParam(this.power = new ParamNumber("psi.spellparam.power", SpellParam.RED, false, false));
        // 時間（秒）: 正の整数のみ
        this.addParam(this.time = new ParamNumber("psi.spellparam.time", SpellParam.BLUE, false, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);

        Double powerVal = (Double) this.getParamEvaluation(this.power);
        Double timeVal = (Double) this.getParamEvaluation(this.time);

        // 威力チェック（正の整数）
        if (powerVal == null || powerVal <= 0.0 || powerVal != Math.floor(powerVal)) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_INTEGER, this.x, this.y);
        }

        // 時間チェック（正の整数、秒）
        if (timeVal == null || timeVal <= 0.0 || timeVal != Math.floor(timeVal)) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_INTEGER, this.x, this.y);
        }

        int intPower = powerVal.intValue();
        int intTime = timeVal.intValue();

        // コスト計算: 威力 * 時間(秒) * 500
        int cost = intPower * intTime * 700;
        // ポテンシー計算: 威力 * 100 + 800
        int potency = intPower * 100 + 800;

        meta.addStat(EnumSpellStat.POTENCY, potency);
        meta.addStat(EnumSpellStat.COST, cost);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        int powerVal = this.getParamValue(context, this.power).intValue();
        int timeVal = this.getParamValue(context, this.time).intValue();

        if (powerVal <= 0) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        if (timeVal <= 0) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }

        Level level = context.caster.level();
        if (level.isClientSide) {
            return null;
        }

        LivingEntity caster = context.caster;

        // 時間を秒からtickに変換（1秒 = 20tick）
        int durationTicks = timeVal * 20;

        // レーザービームエンティティをスポーン
        EntityPhononMaserBeam beam = new EntityPhononMaserBeam(level, caster, powerVal, durationTicks);
        level.addFreshEntity(beam);

        return null;
    }
}