package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.combat.EntityKamaitachiBlade;
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

/** Fires a wide crescent of wind from the position in the direction; it cuts through every creature in its path. */
public class PieceTrickKamaitachi extends PieceTrick {
    private static final double MIN_POWER = 0.5;
    private static final float BASE_DAMAGE = 4.5F;
    private static final float DAMAGE_PER_POWER = 4.5F;
    private static final float BASE_WIDTH = 1.5F;
    private static final float WIDTH_PER_POWER = 0.25F;
    private static final float MAX_WIDTH = 4.0F;
    private static final double POTENCY_BASE = 60.0;
    private static final double POTENCY_PER_POWER = 60.0;
    private static final double COST_BASE = 200.0;
    private static final double COST_PER_POWER = 220.0;

    private SpellParam<Vector3> position;
    private SpellParam<Vector3> direction;
    private SpellParam<Number> power;

    public PieceTrickKamaitachi(Spell spell) {
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
                PsitweaksConfig.COMMON.kamaitachiDamageMultiplier.get(), context.caster);
        float width = (float) Math.min(MAX_WIDTH, BASE_WIDTH + powerVal * WIDTH_PER_POWER);
        Vec3 start = positionVal.toVec3D();
        Vec3 dir = directionVal.toVec3D();

        EntityKamaitachiBlade blade = new EntityKamaitachiBlade(level, start, dir, width);
        blade.configure(context.caster, AttackRules.Options.from(context), damage,
                CombatSpellHelper.spellColor(context.caster));
        level.addFreshEntity(blade);

        CombatEffects.sound(level, start, SoundEvents.BREEZE_WIND_CHARGE_BURST, 0.8F, 1.4F);
        CombatEffects.sound(level, start, SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.2F);
        return null;
    }
}
