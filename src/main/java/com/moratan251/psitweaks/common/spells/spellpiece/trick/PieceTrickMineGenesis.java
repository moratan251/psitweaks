package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.combat.EntityGroundShockwave;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import com.moratan251.psitweaks.common.spells.combat.CombatTrickStats;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
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
 * Shakes the ground: a shockwave ring spreads from the position up to Radius blocks, hitting everything standing on
 * the ground and throwing it upward. Creatures in the air are not affected.
 */
public class PieceTrickMineGenesis extends PieceTrick {
    public static final String ERROR_NO_GROUND = "psitweaks.spellerror.no_ground";

    private static final double MIN_POWER = 0.5;
    private static final float BASE_DAMAGE = 16.0F;
    private static final float DAMAGE_PER_POWER = 12.0F;
    public static final double MAX_RADIUS = 12.0;
    private static final double MIN_RADIUS = 2.0;
    /** How far above the ground the position may be. */
    private static final int GROUND_SEARCH_DEPTH = 3;
    private static final double POTENCY_BASE = 100.0;
    private static final double POTENCY_PER_POWER = 40.0;
    private static final double POTENCY_PER_RADIUS = 15.0;
    private static final double COST_BASE = 300.0;
    private static final double COST_PER_POWER = 150.0;
    private static final double COST_PER_RADIUS = 60.0;

    private SpellParam<Vector3> position;
    private SpellParam<Number> power;
    private SpellParam<Number> radius;

    public PieceTrickMineGenesis(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER).mul(POTENCY_PER_POWER)
                .add(new StatLabel(SpellParam.GENERIC_NAME_RADIUS, true).max(MIN_RADIUS).min(MAX_RADIUS).mul(POTENCY_PER_RADIUS))
                .add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER).mul(COST_PER_POWER)
                .add(new StatLabel(SpellParam.GENERIC_NAME_RADIUS, true).max(MIN_RADIUS).min(MAX_RADIUS).mul(COST_PER_RADIUS))
                .add(COST_BASE).floor());
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
        addParam(radius = new ParamNumber(SpellParam.GENERIC_NAME_RADIUS, SpellParam.GREEN, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        double powerVal = Math.max(MIN_POWER, CombatTrickStats.positive(getParamEvaluation(power), x, y));
        double radiusVal = Mth.clamp(CombatTrickStats.positive(getParamEvaluation(radius), x, y), MIN_RADIUS, MAX_RADIUS);
        meta.addStat(EnumSpellStat.POTENCY, CombatTrickStats.stat(POTENCY_BASE + powerVal * POTENCY_PER_POWER + radiusVal * POTENCY_PER_RADIUS));
        meta.addStat(EnumSpellStat.COST, CombatTrickStats.stat(COST_BASE + powerVal * COST_PER_POWER + radiusVal * COST_PER_RADIUS));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = getParamValue(context, position);
        if (positionVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        if (!context.isInRadius(positionVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }
        if (!(context.caster.level() instanceof ServerLevel level)) {
            return null;
        }
        Double groundY = CombatSpellHelper.findGroundY(level, positionVal.x, positionVal.y, positionVal.z, GROUND_SEARCH_DEPTH);
        if (groundY == null) {
            throw new SpellRuntimeException(ERROR_NO_GROUND);
        }

        double powerVal = Math.max(MIN_POWER, getParamValue(context, power).doubleValue());
        float radiusVal = (float) Mth.clamp(getParamValue(context, radius).doubleValue(), MIN_RADIUS, MAX_RADIUS);
        float damage = AttackRules.scale(BASE_DAMAGE + powerVal * DAMAGE_PER_POWER,
                PsitweaksConfig.COMMON.mineGenesisDamageMultiplier.get(), context.caster);
        float launch = (float) Math.min(0.9, 0.45 + 0.05 * powerVal);

        Vec3 center = new Vec3(positionVal.x, groundY, positionVal.z);
        EntityGroundShockwave shockwave = new EntityGroundShockwave(level, center, radiusVal, launch);
        shockwave.configure(context.caster, AttackRules.Options.from(context), damage,
                CombatSpellHelper.spellColor(context.caster));
        level.addFreshEntity(shockwave);

        CombatEffects.sound(level, center, SoundEvents.GENERIC_EXPLODE, 0.8F, 0.5F);
        CombatEffects.sound(level, center, SoundEvents.MACE_SMASH_GROUND_HEAVY, 1.0F, 0.7F);
        return null;
    }
}
