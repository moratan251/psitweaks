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
                (new StatLabel("psi.spellparam.power", true)).mul(50.0)
                        .add(new StatLabel("psi.spellparam.time", true).mul(10.0)).floor());
        this.setStatLabel(EnumSpellStat.COST,
                (new StatLabel("psi.spellparam.power", true)).mul(100.0)
                        .mul(new StatLabel("psi.spellparam.time", true)).floor());
    }

    @Override
    public void initParams() {
        // 威力: 小数を許可（第4引数true）
        this.addParam(this.power = new ParamNumber("psi.spellparam.power", SpellParam.RED, false, true));
        // 時間: 整数のみ（第4引数false）
        this.addParam(this.time = new ParamNumber("psi.spellparam.time", SpellParam.BLUE, false, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);

        Double powerVal = (Double) this.getParamEvaluation(this.power);
        Double timeVal = (Double) this.getParamEvaluation(this.time);

        // 威力チェック（正の数）
        if (powerVal == null || powerVal <= 0.0) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, this.x, this.y);
        }

        // 時間チェック（正の整数）
        if (timeVal == null || timeVal <= 0.0) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, this.x, this.y);
        }
        if (timeVal != Math.floor(timeVal)) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_INTEGER, this.x, this.y);
        }

        int intTime = timeVal.intValue();



        // コスト計算: 威力 * 100 * 時間
        int cost = (int) (  (intTime + 1) * (powerVal + 1) * 2000 );
        // ポテンシー計算: 威力 * 50 + 時間 * 10
        int potency = (int) (500 + 100 * powerVal);

        meta.addStat(EnumSpellStat.POTENCY, potency);
        meta.addStat(EnumSpellStat.COST, cost);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        double powerVal = this.getParamValue(context, this.power).doubleValue();
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

        double realPower = powerVal + 2.0; // ベースで2.0ダメージ
        int realTime = timeVal + 20; // ベースで20ティック

        // レーザービームエンティティをスポーン
        EntityPhononMaserBeam beam = new EntityPhononMaserBeam(level, caster, realPower, realTime);
        level.addFreshEntity(beam);

        return null;
    }
}