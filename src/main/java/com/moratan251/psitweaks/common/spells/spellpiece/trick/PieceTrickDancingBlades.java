package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.config.PsitweaksConfig;
import com.moratan251.psitweaks.common.entities.combat.EntityDancingBlade;
import com.moratan251.psitweaks.common.spells.SpellSafetyUtils;
import com.moratan251.psitweaks.common.spells.combat.CombatEffects;
import com.moratan251.psitweaks.common.spells.combat.CombatSpellHelper;
import com.moratan251.psitweaks.common.spells.combat.CombatTrickStats;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * Summons Count spectral blades around the caster. Each one darts at a hostile creature nearby, cuts it, comes
 * back, and does so twice before fading after five seconds at most.
 */
public class PieceTrickDancingBlades extends PieceTrick {
    private static final double MIN_POWER = 0.5;
    private static final float BASE_DAMAGE = 2.0F;
    private static final float DAMAGE_PER_POWER = 2.0F;
    public static final int MAX_BLADES = 8;
    private static final double POTENCY_BASE = 780.0;
    private static final double POTENCY_PER_BLADE = 15.0;
    private static final double POTENCY_PER_BLADE_POWER = 15.0;
    private static final double COST_BASE = 200.0;
    private static final double COST_PER_BLADE = 60.0;
    private static final double COST_PER_BLADE_POWER = 60.0;

    private SpellParam<Number> count;
    private SpellParam<Number> power;

    public PieceTrickDancingBlades(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER)
                .mul(POTENCY_PER_BLADE_POWER).add(POTENCY_PER_BLADE).parenthesize()
                .mul(new StatLabel("psitweaks.spellparam.count", true).min(MAX_BLADES)).add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, new StatLabel(SpellParam.GENERIC_NAME_POWER, true).max(MIN_POWER)
                .mul(COST_PER_BLADE_POWER).add(COST_PER_BLADE).parenthesize()
                .mul(new StatLabel("psitweaks.spellparam.count", true).min(MAX_BLADES)).add(COST_BASE).floor());
    }

    @Override
    public void initParams() {
        addParam(count = new ParamNumber("psitweaks.spellparam.count", SpellParam.GREEN, false, true));
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        int blades = Math.min(MAX_BLADES, CombatTrickStats.positiveInteger(getParamEvaluation(count), x, y));
        double powerVal = Math.max(MIN_POWER, CombatTrickStats.positive(getParamEvaluation(power), x, y));
        meta.addStat(EnumSpellStat.POTENCY, CombatTrickStats.stat(POTENCY_BASE
                + blades * (POTENCY_PER_BLADE + powerVal * POTENCY_PER_BLADE_POWER)));
        meta.addStat(EnumSpellStat.COST, CombatTrickStats.stat(COST_BASE
                + blades * (COST_PER_BLADE + powerVal * COST_PER_BLADE_POWER)));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        if (!(context.caster.level() instanceof ServerLevel level)) {
            return null;
        }
        int blades = Mth.clamp(getParamValue(context, count).intValue(), 1, MAX_BLADES);
        double powerVal = Math.max(MIN_POWER, getParamValue(context, power).doubleValue());
        float damage = CombatSpellHelper.scaleDamage(BASE_DAMAGE + powerVal * DAMAGE_PER_POWER,
                PsitweaksConfig.COMMON.dancingBladesDamageMultiplier.get(), context.caster);
        boolean safeToPlayers = SpellSafetyUtils.hasSafeToPlayers(context);
        int color = CombatSpellHelper.spellColor(context.caster);
        // Recasting replaces the caster's earlier blades instead of piling more up.
        int ownerId = context.caster.getId();
        for (EntityDancingBlade old : level.getEntitiesOfClass(EntityDancingBlade.class,
                context.caster.getBoundingBox().inflate(64.0), blade -> blade.getOwnerId() == ownerId)) {
            old.discard();
        }
        for (int i = 0; i < blades; i++) {
            EntityDancingBlade blade = new EntityDancingBlade(level, context.caster, i, blades);
            blade.configure(context.caster, safeToPlayers, damage, color);
            level.addFreshEntity(blade);
        }
        CombatEffects.sound(level, context.caster.position(), SoundEvents.TRIDENT_RETURN, 0.9F, 1.3F);
        CombatEffects.sound(level, context.caster.position(), SoundEvents.ENCHANTMENT_TABLE_USE, 0.8F, 1.6F);
        return null;
    }
}
