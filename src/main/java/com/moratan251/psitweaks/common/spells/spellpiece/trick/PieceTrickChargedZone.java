package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntityChargedZone;
import net.minecraft.server.level.ServerLevel;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * 作動式: 荷電領域（仮、Charged Zone）。範囲の中のエンティティ（持ち物）に FE を送り、AE2 のチャージ加工をし、乱流領域と
 * 重なると雷雲になる（{@link EntityChargedZone}）。
 * <p>
 * 必須の「威力」（1 以上の定数）はコストのつまみで、充電能力（置いたときのコストに比例、{@link EntityChargedZone#capacityPerTick}）は
 * 威力とともに増える。領域には中の威力 = 威力 + 4 を渡す（{@link #resourcePower}）。規模とコストは加熱領域・凍結領域と同じ式。
 */
public class PieceTrickChargedZone extends PieceTrickZoneBase<EntityChargedZone> {
    // 仮の数値: 規模 = 10 + ⌊広さ × 威力 × 0.27⌋、コスト = 50 + ⌊広さ × (威力 + 4) × 2 / 5⌋（広さ = サイズ²。加熱・凍結と同じ）

    private SpellParam<Number> power;

    public PieceTrickChargedZone(Spell spell) {
        super(spell, EntityChargedZone.class);
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
    protected EntityChargedZone createZone(ServerLevel level) {
        return new EntityChargedZone(level);
    }

    @Override
    protected void configure(EntityChargedZone zone, SpellContext context) throws SpellRuntimeException {
        zone.setPower(resourcePower(powerValue(context, power)));
    }
}
