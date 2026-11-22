package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityAccelerationZone;
import net.minecraft.server.level.ServerLevel;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * 作動式: 加速領域（Acceleration Zone）。範囲の中の育つもののランダムティックを、バニラと合わせて (1 + 威力 + 4) 倍に速め、
 * 同じ術者のほかの領域のランダムなブロックの変化も同じ倍率で速める（{@link EntityAccelerationZone}）。「威力」は 1〜10 の定数
 * （領域には中の威力 = 威力 + 4 を渡す、{@link #resourcePower}）。
 * コスト = 50 + ⌊広さ × (威力 + 4) × 2 / 5⌋（30 秒、時間に比例）、規模 = 10 + ⌊広さ × 威力 × 0.27⌋（広さ = サイズ²）。
 */
public class PieceTrickAccelerationZone extends PieceTrickZoneBase<EntityAccelerationZone> {
    private SpellParam<Number> power;

    public PieceTrickAccelerationZone(Spell spell) {
        super(spell, EntityAccelerationZone.class);
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
    protected EntityAccelerationZone createZone(ServerLevel level) {
        return new EntityAccelerationZone(level);
    }

    @Override
    protected void configure(EntityAccelerationZone zone, SpellContext context) throws SpellRuntimeException {
        zone.setPower(resourcePower(powerValue(context, power)));
    }
}
