package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.region.EntitySpellGramRegion;
import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramCapacity;
import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramProperty;
import com.moratan251.psitweaks.common.entities.SpellGram.region.SpellGramRegions;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.CombatTrickStats;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
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
 * 自分の領域型の魔法式オブジェクトに性質を足す作動式（試作）。同じ性質は威力が重なり、灼熱と凍結は打ち消し合って湿潤になる。
 * 他人の魔法式には足せない（他人の魔法式への操作は未実装）。
 */
public abstract class PieceTrickSpellGramImbueBase extends PieceTrick {
    public static final String ERROR_NOT_REGION = "psitweaks.spellerror.not_spellgram_region";
    public static final String ERROR_NOT_OWN = "psitweaks.spellerror.not_own_spellgram";

    private static final double MIN_POWER = 0.5D;
    private static final double POTENCY_BASE = 60.0D;
    private static final double POTENCY_PER_POWER = 50.0D;
    private static final double COST_BASE = 300.0D;
    private static final double COST_PER_POWER = 250.0D;

    private SpellParam<Entity> target;
    private SpellParam<Number> power;

    protected PieceTrickSpellGramImbueBase(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER)
                .mul(POTENCY_PER_POWER).add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER)
                .mul(COST_PER_POWER).add(COST_BASE).floor());
    }

    protected abstract SpellGramProperty property();

    @Override
    public void initParams() {
        addParam(target = new ParamEntity("psitweaks.spellparam.spellgram", SpellParam.YELLOW, false, false));
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        double powerVal = Math.max(MIN_POWER, CombatTrickStats.positive(getParamEvaluation(power), x, y));
        meta.addStat(EnumSpellStat.POTENCY, CombatTrickStats.stat(POTENCY_BASE + POTENCY_PER_POWER * powerVal));
        meta.addStat(EnumSpellStat.COST, CombatTrickStats.stat(COST_BASE + COST_PER_POWER * powerVal));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        EntitySpellGramRegion region = ownRegion(context, getParamValue(context, target));
        if (!(context.caster.level() instanceof ServerLevel level)) {
            return null;
        }
        double powerVal = Math.max(MIN_POWER, getParamValue(context, power).doubleValue());
        int use = SpellGramCapacity.reserve(context, (int) (COST_BASE + COST_PER_POWER * powerVal),
                (int) (POTENCY_BASE + POTENCY_PER_POWER * powerVal), region);
        SpellGramProperty property = property();
        region.addProperty(property, powerVal, SpellGramRegions.scale(property, context.caster),
                SpellGramRegions.integrity(powerVal, context.caster));
        region.addCapacityUse(use);

        CombatEffects.sound(level, region.position(), SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 0.8F);
        return null;
    }

    /** 対象が術者自身の領域型であることを確かめる。 */
    static EntitySpellGramRegion ownRegion(SpellContext context, Entity entity) throws SpellRuntimeException {
        if (entity == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }
        if (!context.isInRadius(entity)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }
        if (!(entity instanceof EntitySpellGramRegion region) || region.isRemoved()) {
            throw new SpellRuntimeException(ERROR_NOT_REGION);
        }
        if (!region.isOwnedBy(context.caster)) {
            throw new SpellRuntimeException(ERROR_NOT_OWN);
        }
        return region;
    }
}
