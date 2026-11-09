package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.EntitySoakZone;
import net.minecraft.server.level.ServerLevel;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;

/** 作動式: 湿潤領域（仮、Soak Zone）。範囲の中の生物を濡らし続ける。 */
public class PieceTrickSoakZone extends PieceTrickZoneBase<EntitySoakZone> {
    // 仮の数値: 規模 = 10 + 広さ / 10、コスト = 40 + 広さ（広さ = サイズ²）
    private static final int POTENCY_BASE = 10;
    private static final int POTENCY_AREA_DIVISOR = 10;
    private static final int COST_BASE = 40;

    public PieceTrickSoakZone(Spell spell) {
        super(spell, EntitySoakZone.class);
        setStatLabel(EnumSpellStat.POTENCY, areaLabel().div(POTENCY_AREA_DIVISOR).add(POTENCY_BASE).floor());
        setStatLabel(EnumSpellStat.COST, areaLabel().add(COST_BASE));
    }

    @Override
    protected int potency(int area) {
        return POTENCY_BASE + area / POTENCY_AREA_DIVISOR;
    }

    @Override
    protected int cost(int area) {
        return COST_BASE + area;
    }

    @Override
    protected EntitySoakZone createZone(ServerLevel level) {
        return new EntitySoakZone(level);
    }

    @Override
    protected void configure(EntitySoakZone zone, SpellContext context) {
    }
}
