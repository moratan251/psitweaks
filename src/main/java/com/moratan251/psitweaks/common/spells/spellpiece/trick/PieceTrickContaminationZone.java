package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.compat.ZoneRadiationCompat;
import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityContaminationZone;
import com.moratan251.psitweaks.common.spells.SpellSafetyUtils;
import com.moratan251.psitweaks.common.spells.SpellWarning;
import net.minecraft.server.level.ServerLevel;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * 作動式: 汚染領域（Contamination Zone、Mekanism があるときだけ登録）。範囲の中の生き物を、威力で決まる線量率で放射線に
 * さらす（{@link EntityContaminationZone}）。「威力」は 1〜10 の定数（領域には中の威力 = 威力 + 4 を渡す、
 * {@link #resourcePower}）。コスト = 50 + ⌊広さ × (威力 + 4) × 2 / 5⌋（30 秒、時間に比例）、規模 = 10 + ⌊広さ × 威力 × 0.27⌋。
 * Mekanism の設定で放射線が切ってあれば、置かずに警告する。
 */
public class PieceTrickContaminationZone extends PieceTrickZoneBase<EntityContaminationZone> {
    private SpellParam<Number> power;

    public PieceTrickContaminationZone(Spell spell) {
        super(spell, EntityContaminationZone.class);
        setStatLabel(EnumSpellStat.POTENCY, poweredPotencyLabel());
        setStatLabel(EnumSpellStat.COST, resourceZoneCostLabel(MAX_ZONE_POWER));
    }

    @Override
    protected void addZoneParams() {
        power = addPowerParam();
    }

    @Override
    protected int potency(int area) throws SpellCompilationException {
        return poweredPotency(area, constantPower(power));
    }

    @Override
    protected int cost(int area) throws SpellCompilationException {
        return resourceZoneCost(area, constantPower(power));
    }

    @Override
    protected int runtimeCost(int size, SpellContext context) throws SpellRuntimeException {
        return resourceZoneCost(areaOf(size), powerValue(context, power));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        if (!ZoneRadiationCompat.isRadiationEnabled()) {
            SpellWarning.warn(context, SpellWarning.RADIATION_DISABLED);
            return null;
        }
        return super.execute(context);
    }

    @Override
    protected EntityContaminationZone createZone(ServerLevel level) {
        return new EntityContaminationZone(level);
    }

    @Override
    protected void configure(EntityContaminationZone zone, SpellContext context) throws SpellRuntimeException {
        zone.setPower(resourcePower(powerValue(context, power)));
        zone.setSafeToPlayers(SpellSafetyUtils.hasSafeToPlayers(context));
    }
}
