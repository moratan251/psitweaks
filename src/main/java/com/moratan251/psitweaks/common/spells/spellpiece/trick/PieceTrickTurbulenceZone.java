package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityTurbulenceZone;
import net.minecraft.server.level.ServerLevel;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;

/**
 * 作動式: 乱流領域（仮、Turbulence Zone）。範囲の中に暴風を起こす: 他者の飛び道具をそらし、残留ポーションなどの雲を
 * 吹き散らし、同じ術者のほかの領域と重なると強める（熱乱流・吹雪・暴風雨・雷雲）。中のエンティティは運ばない（運ぶのは
 * 加重領域）。風量（WU）を持ち、Mekanism Generators の風力発電機に風を足す（{@link EntityTurbulenceZone}）。強さは「威力」
 * （コストを決めるため 1 以上の定数。領域には中の威力 = 威力 + 4 を渡す、{@link #resourcePower}）。
 */
public class PieceTrickTurbulenceZone extends PieceTrickZoneBase<EntityTurbulenceZone> {
    // 仮の数値: 規模 = 10 + ⌊広さ × 威力 × 0.27⌋（威力のある領域で共通）、コスト = 50 + ⌊広さ × (威力 + 4) × 2 / 5⌋（加熱・凍結・荷電と
    // 同じ。広さ = サイズ²）

    private SpellParam<Number> power;

    public PieceTrickTurbulenceZone(Spell spell) {
        super(spell, EntityTurbulenceZone.class);
        setStatLabel(EnumSpellStat.POTENCY, poweredPotencyLabel());
        setStatLabel(EnumSpellStat.COST, resourceZoneCostLabel());
    }

    /** 総量を持つ領域なので、威力は 10 より上も入れられる（10 を超えた分はコスト＝総量だけを増やす）。 */
    @Override
    protected double maxPower() {
        return MAX_RESOURCE_ZONE_POWER;
    }

    @Override
    protected void addZoneParams() {
        addParam(power = new ParamNumber(SpellParam.GENERIC_NAME_POWER, SpellParam.RED, false, true));
    }

    @Override
    protected int potency(int area) throws SpellCompilationException {
        return poweredPotency(area, constantPower(power));
    }

    @Override
    protected int cost(int area) throws SpellCompilationException {
        return costFor(area, constantPower(power));
    }

    /** コスト（30 秒）: 50 + ⌊広さ × (威力 + 4) × 2 / 5⌋（{@link #resourceZoneCost}、威力は入れた威力）。 */
    public static int costFor(int area, double power) {
        return resourceZoneCost(area, power);
    }

    @Override
    protected int runtimeCost(int size, SpellContext context) throws SpellRuntimeException {
        return costFor(areaOf(size), powerValue(context, power));
    }

    @Override
    protected EntityTurbulenceZone createZone(ServerLevel level) {
        return new EntityTurbulenceZone(level);
    }

    @Override
    protected void configure(EntityTurbulenceZone zone, SpellContext context) throws SpellRuntimeException {
        zone.configureWind(resourcePower(powerValue(context, power)));
    }
}
