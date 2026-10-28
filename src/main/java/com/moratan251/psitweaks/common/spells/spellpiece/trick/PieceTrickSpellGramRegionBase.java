package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.compat.SableRangeCompat;
import com.moratan251.psitweaks.common.entities.SpellGram.region.EntitySpellGramRegion;
import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramCapacity;
import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramProperty;
import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramRegions;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import com.moratan251.psitweaks.common.spells.combat.CombatTrickStats;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
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
 * 位置・半径・威力から領域型の魔法式オブジェクトを置く作動式（試作）。性質つき（灼熱領域など）と素の魔法式（魔法式展開）が継承する。
 * 半径は {@link EntitySpellGramRegion#MIN_RADIUS} から {@link EntitySpellGramRegion#MAX_RADIUS} に収める。
 */
public abstract class PieceTrickSpellGramRegionBase extends PieceTrick {
    protected static final double MIN_POWER = 0.5D;

    /** 規模とコスト: 基礎 + 威力あたり × 威力 + 半径あたり × 半径。 */
    protected record Stats(double potencyBase, double potencyPerPower, double potencyPerRadius,
                           double costBase, double costPerPower, double costPerRadius) {
        double potency(double power, double radius) {
            return potencyBase + potencyPerPower * power + potencyPerRadius * radius;
        }

        double cost(double power, double radius) {
            return costBase + costPerPower * power + costPerRadius * radius;
        }
    }

    private SpellParam<Vector3> position;
    private SpellParam<Number> radius;
    private SpellParam<Number> power;

    protected PieceTrickSpellGramRegionBase(Spell spell) {
        super(spell);
        Stats stats = stats();
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER)
                .mul(stats.potencyPerPower())
                .add(new StatLabel(SpellParam.GENERIC_NAME_RADIUS, true).max(EntitySpellGramRegion.MIN_RADIUS)
                        .min(EntitySpellGramRegion.MAX_RADIUS).mul(stats.potencyPerRadius()))
                .add(stats.potencyBase()).floor());
        setStatLabel(EnumSpellStat.COST, new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER)
                .mul(stats.costPerPower())
                .add(new StatLabel(SpellParam.GENERIC_NAME_RADIUS, true).max(EntitySpellGramRegion.MIN_RADIUS)
                        .min(EntitySpellGramRegion.MAX_RADIUS).mul(stats.costPerRadius()))
                .add(stats.costBase()).floor());
    }

    protected abstract Stats stats();

    /** 置いたときに付ける性質。素の魔法式は null。 */
    @Nullable
    protected abstract SpellGramProperty property();

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(radius = new ParamNumber(SpellParam.GENERIC_NAME_RADIUS, SpellParam.GREEN, false, true));
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        double powerVal = Math.max(MIN_POWER, CombatTrickStats.positive(getParamEvaluation(power), x, y));
        double radiusVal = EntitySpellGramRegion.clampRadius(CombatTrickStats.positive(getParamEvaluation(radius), x, y));
        meta.addStat(EnumSpellStat.POTENCY, CombatTrickStats.stat(stats().potency(powerVal, radiusVal)));
        meta.addStat(EnumSpellStat.COST, CombatTrickStats.stat(stats().cost(powerVal, radiusVal)));
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
        double powerVal = Math.max(MIN_POWER, getParamValue(context, power).doubleValue());
        double radiusVal = EntitySpellGramRegion.clampRadius(getParamValue(context, radius).doubleValue());
        // 数の入力は定数なので、規模とコストは組み立てたときと同じ値になる。
        int use = SpellGramCapacity.reserve(context, (int) stats().cost(powerVal, radiusVal),
                (int) stats().potency(powerVal, radiusVal), null);

        SpellGramProperty property = property();
        Vec3 at = SableRangeCompat.projectForEffect(level, positionVal);
        EntitySpellGramRegion region = new EntitySpellGramRegion(level);
        region.setPos(at.x, at.y + 0.01D, at.z);
        float powerIntegrity = SpellGramRegions.integrity(powerVal, context.caster);
        int color = property != null ? property.color() : CombatSpellHelper.spellColor(context.caster);
        region.configure(context.caster, AttackRules.Options.from(context), radiusVal,
                property != null ? 0.0F : powerIntegrity, color);
        if (property != null) {
            region.addProperty(property, powerVal, SpellGramRegions.scale(property, context.caster), powerIntegrity);
        }
        region.setCapacityUse(use);
        level.addFreshEntity(region);

        CombatEffects.sound(level, region.position(), SoundEvents.BEACON_ACTIVATE, 0.6F, 1.6F);
        return null;
    }
}
