package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityFreezeZone;
import net.minecraft.server.level.ServerLevel;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * 作動式: 凍結領域（仮、Freeze Zone）。範囲の中の生物を凍えさせ、上が空気の水源の水を氷にする（{@link EntityFreezeZone}）。
 * 「威力」（1 以上の定数）は冷やす強さで、凍えのたまる速さと、Mekanism の機械を冷やせる下限を決める（領域には中の威力 =
 * 威力 + 4 を渡す、{@link #resourcePower}）。
 */
public class PieceTrickFreezeZone extends PieceTrickZoneBase<EntityFreezeZone> {
    // 仮の数値: 規模 = 10 + ⌊広さ × 威力 × 0.27⌋（威力のある領域で共通）、コスト = 50 + ⌊広さ × (威力 + 4) × 2 / 5⌋（広さ = サイズ²）

    private SpellParam<Number> power;

    public PieceTrickFreezeZone(Spell spell) {
        super(spell, EntityFreezeZone.class);
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
        power = addPowerParam();
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
    protected EntityFreezeZone createZone(ServerLevel level) {
        return new EntityFreezeZone(level);
    }

    @Override
    protected void configure(EntityFreezeZone zone, SpellContext context) throws SpellRuntimeException {
        zone.setPower(resourcePower(powerValue(context, power)));
    }
}
