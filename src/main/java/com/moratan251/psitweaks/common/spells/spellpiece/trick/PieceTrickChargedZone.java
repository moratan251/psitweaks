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
 * 作動式: 荷電領域（仮、Charged Zone、2026-11-12 ユーザー）。範囲の中のエンティティ（持ち物）に FE を送り、AE2 のチャージ加工を
 * し、乱流領域と重なると雷雲になる（{@link EntityChargedZone}）。
 * <p>
 * 2026-11-14 ユーザー: 必須の「威力」（1〜10 の定数）を足した。威力はコストのつまみで、充電能力（置いたときのコストに比例、
 * {@link EntityChargedZone#capacityPerTick}）は威力とともに増える。規模とコストは加熱領域・凍結領域と同じ式で、威力 5 のときが
 * 威力のなかった頃と同じコスト。
 */
public class PieceTrickChargedZone extends PieceTrickZoneBase<EntityChargedZone> {
    // 仮の数値: 規模 = 10 + ⌊広さ × 威力 × 0.27⌋、コスト = 50 + 広さ × 威力 × 2 / 5（広さ = サイズ²。加熱・凍結と同じ）
    private static final int COST_BASE = 50;
    private static final int COST_MULTIPLIER = 2;
    private static final int COST_DIVISOR = 5;

    private SpellParam<Number> power;

    public PieceTrickChargedZone(Spell spell) {
        super(spell, EntityChargedZone.class);
        setStatLabel(EnumSpellStat.POTENCY, poweredPotencyLabel(MAX_RESOURCE_ZONE_POWER));
        setStatLabel(EnumSpellStat.COST, areaLabel().mul(powerLabel(MAX_RESOURCE_ZONE_POWER)).mul(COST_MULTIPLIER).div(COST_DIVISOR).add(COST_BASE).floor());
    }

    /** 総量を持つ領域なので、威力は 10 より上も入れられる（2026-11-15 ユーザー）。 */
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

    /** コスト（30 秒）: 50 + ⌊広さ × 威力 × 2 / 5⌋。 */
    public static int costFor(int area, double power) {
        return COST_BASE + (int) Math.floor(area * power * COST_MULTIPLIER / COST_DIVISOR);
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
        zone.setPower(powerValue(context, power));
    }
}
