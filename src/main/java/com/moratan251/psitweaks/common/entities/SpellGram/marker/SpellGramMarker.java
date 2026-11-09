package com.moratan251.psitweaks.common.entities.SpellGram.marker;

import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.spells.combat.AttackRules;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * 目印型の魔法式オブジェクトの基底。術者が最初に魔力を与えたきりで、作動式が発動する場所の目印になる。
 * CAD の枠は使わない。ふつうの攻撃では壊れない・保存しない・術者がいなくなると消えるのは、魔法式オブジェクト共通
 * （{@link SpellGramObject}）。
 */
public abstract class SpellGramMarker extends SpellGramObject {
    private AttackRules.Options options = AttackRules.Options.DEFAULT;

    protected SpellGramMarker(EntityType<? extends SpellGramMarker> type, Level level) {
        super(type, level);
    }

    /** 置く前に呼ぶ。強度は置いた時点の値で決まる。 */
    protected void configureMarker(LivingEntity caster, AttackRules.Options options, float integrity) {
        setCaster(caster);
        this.options = options;
        setMaxIntegrity(integrity);
    }

    @Override
    protected AttackRules.Options attackOptions() {
        return options;
    }
}
