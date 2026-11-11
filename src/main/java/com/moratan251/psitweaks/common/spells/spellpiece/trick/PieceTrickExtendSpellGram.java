package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * 作動式: 魔法式延長（2026-11-11 ユーザー）。対象の自分の魔法式オブジェクトの耐久値を回復する（同日、存続時間を延ばす形から
 * 変更）。考え方は「置いたときに払ったコストのうち、払った割合だけ耐久値が戻る」: このピースのコストは 威力 ×
 * {@link #COST_PER_POWER}、戻る耐久値は 置いたときの耐久値 × このピースのコスト ÷ 置いたときのコスト。多くコストを払ったもの
 * ほど回復しにくい。耐久値は置いたときの {@link #MAX_INTEGRITY_FACTOR} 倍まで。術式で削られた分から先に戻り、置いたときの
 * 耐久値を超えた分はそのぶん長く残る（{@link SpellGramObject#restoreIntegrity}）。対象は 1 つ（リストは受け付けない。コストを
 * 1 つぶんで決めるため）、距離は問わない。目印型（流星群の夜）は対象外。
 */
public class PieceTrickExtendSpellGram extends PieceTrick {
    /** 威力 1 あたりのコスト（仮）。 */
    public static final int COST_PER_POWER = 100;
    /** 威力の上限（仮）。 */
    public static final double MAX_POWER = 100.0;
    /** 耐久値の上限: 置いたときの耐久値の何倍か（2026-11-11 ユーザー）。 */
    public static final float MAX_INTEGRITY_FACTOR = 2.0F;
    // 仮の数値: 規模 = 威力
    private static final int POTENCY_PER_POWER = 1;

    private SpellParam<Entity> target;
    private SpellParam<Number> power;

    public PieceTrickExtendSpellGram(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(SpellParam.GENERIC_NAME_POWER, true).mul(POTENCY_PER_POWER).floor());
        setStatLabel(EnumSpellStat.COST, new StatLabel(SpellParam.GENERIC_NAME_POWER, true).mul(COST_PER_POWER).floor());
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntity(SpellParam.GENERIC_NAME_TARGET, SpellParam.YELLOW, false, false));
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        Object value = getParamEvaluation(power);
        if (!(value instanceof Number number) || number.doubleValue() <= 0.0) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
        }
        double powerValue = Math.min(MAX_POWER, number.doubleValue());
        meta.addStat(EnumSpellStat.POTENCY, (int) Math.floor(powerValue * POTENCY_PER_POWER));
        meta.addStat(EnumSpellStat.COST, (int) Math.floor(powerValue * COST_PER_POWER));
    }

    /** 戻る耐久値: 置いたときの耐久値 × 威力ぶんのコスト ÷ 置いたときのコスト（上限は呼ぶ側の {@link SpellGramObject#restoreIntegrity}）。 */
    public static float restoreAmount(float maxIntegrity, double power, float placementCost) {
        if (power <= 0.0 || placementCost <= 0.0F) {
            return 0.0F;
        }
        return (float) (maxIntegrity * Math.min(MAX_POWER, power) * COST_PER_POWER / placementCost);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        SpellGramObject object = SpellGramTargets.own(context, getParamValue(context, target));
        double powerValue = getNotNullParamValue(context, power).doubleValue();
        if (powerValue <= 0.0) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_POSITIVE_VALUE);
        }
        float added = object.restoreIntegrity(
                restoreAmount(object.getMaxIntegrity(), powerValue, object.getPlacementCost()), MAX_INTEGRITY_FACTOR);
        if (added > 0.0F && object.level() instanceof ServerLevel level) {
            Vec3 at = object.position();
            level.playSound(null, at.x, at.y, at.z, SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.5F, 1.5F);
            level.sendParticles(ParticleTypes.END_ROD, at.x, at.y + 0.5, at.z, 8, 0.3, 0.3, 0.3, 0.02);
        }
        return null;
    }
}
