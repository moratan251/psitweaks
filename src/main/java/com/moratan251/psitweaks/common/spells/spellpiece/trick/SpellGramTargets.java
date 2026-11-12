package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.caster.CasterIds;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * 自分の常駐魔法式を対象にする作動式（魔法式回収・魔法式延長）の共通の確かめ。流星群の夜の核は常駐魔法式ではないので
 * 対象にしない。自分のものかは術者の ID（自律詠唱機は機械ごと）で見る。距離は問わない（自分の術式なので）。
 */
final class SpellGramTargets {
    static final String ERROR_NOT_SPELLGRAM = "psitweaks.spellerror.not_spellgram";
    static final String ERROR_NOT_OWN = "psitweaks.spellerror.not_own_spellgram";

    private SpellGramTargets() {
    }

    /** {@code entity} が術者の常駐魔法式なら返す。そうでなければエラー。 */
    static SpellGramObject own(SpellContext context, @Nullable Entity entity) throws SpellRuntimeException {
        if (!(entity instanceof SpellGramObject object) || !object.isResident() || object.isRemoved()) {
            throw new SpellRuntimeException(ERROR_NOT_SPELLGRAM);
        }
        if (context.caster == null || !CasterIds.matches(context.caster, object.getCasterUuid())) {
            throw new SpellRuntimeException(ERROR_NOT_OWN);
        }
        return object;
    }
}
