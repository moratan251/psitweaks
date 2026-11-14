package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.combat.EntityPsiBullet;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import com.moratan251.psitweaks.common.spells.combat.CombatTrickStats;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
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
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * 作動式: サイ・ブリット（2026-11-14 ユーザー）。位置から方向へサイオンの弾（{@link EntityPsiBullet}）を撃ち出す。最初に当たった
 * 生物に {@code 2 + 威力} のダメージ（全体倍率・個別倍率・事象干渉力を掛ける）、魔法式オブジェクトの核に当たれば耐久値を
 * {@code 8 + 2 × 威力} 減らす（× 術者の事象干渉力）。規模・コスト・射程はエア・ブリットと同じ。
 */
public class PieceTrickPsiBullet extends PieceTrick {
    private static final double MIN_POWER = 0.5;
    public static final float BASE_DAMAGE = 2.0F;
    public static final float DAMAGE_PER_POWER = 1.0F;
    public static final float BASE_INTEGRITY_DAMAGE = 8.0F;
    public static final float INTEGRITY_DAMAGE_PER_POWER = 2.0F;
    private static final double POTENCY_BASE = 60.0;
    private static final double POTENCY_PER_POWER = 60.0;
    private static final double COST_BASE = 200.0;
    private static final double COST_PER_POWER = 220.0;

    private SpellParam<Vector3> position;
    private SpellParam<Vector3> direction;
    private SpellParam<Number> power;

    public PieceTrickPsiBullet(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER)
                .mul(POTENCY_PER_POWER).add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER)
                .mul(COST_PER_POWER).add(COST_BASE).floor());
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(direction = new ParamVector(SpellParam.GENERIC_NAME_DIRECTION, SpellParam.GREEN, false, false));
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        double powerVal = Math.max(MIN_POWER, CombatTrickStats.positive(getParamEvaluation(power), x, y));
        meta.addStat(EnumSpellStat.POTENCY, CombatTrickStats.stat(POTENCY_BASE + powerVal * POTENCY_PER_POWER));
        meta.addStat(EnumSpellStat.COST, CombatTrickStats.stat(COST_BASE + powerVal * COST_PER_POWER));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = getParamValue(context, position);
        Vector3 directionVal = getParamValue(context, direction);
        if (positionVal == null || directionVal == null || directionVal.isZero()) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        if (!context.isInRadius(positionVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }
        if (!(context.caster.level() instanceof ServerLevel level)) {
            return null;
        }

        double powerVal = Math.max(MIN_POWER, getParamValue(context, power).doubleValue());
        float damage = AttackRules.scale(BASE_DAMAGE + powerVal * DAMAGE_PER_POWER,
                PsitweaksConfig.COMMON.psiBulletDamageMultiplier.get(), context.caster);
        Vec3 start = positionVal.toVec3D();

        EntityPsiBullet bullet = new EntityPsiBullet(level, start, directionVal.toVec3D());
        bullet.configure(context.caster, AttackRules.Options.from(context), damage,
                CombatSpellHelper.spellColor(context.caster));
        bullet.setIntegrityDamage((float) (BASE_INTEGRITY_DAMAGE + powerVal * INTEGRITY_DAMAGE_PER_POWER));
        level.addFreshEntity(bullet);

        CombatEffects.sound(level, start, SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.6F);
        CombatEffects.sound(level, start, SoundEvents.SHULKER_SHOOT, 0.6F, 1.6F);
        return null;
    }
}
