package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.spells.util.ReadOnlyTargets;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.piece.PieceSelector;

/**
 * 取得子: 魔法式耐久値（2026-11-14 ユーザー）。「対象」が魔法式オブジェクト（常駐魔法式や流星群の夜の核）なら、その今の耐久値
 * （{@link SpellGramObject#getIntegrity}）を数値で返す。魔法式オブジェクトでなければエラー。読むだけなので、Psi の耐性の判定は
 * しない（{@link ReadOnlyTargets}）。距離も問わない（Psi の読み取りの演算子と同じ）。
 */
public class PieceSelectorSpellGramIntegrity extends PieceSelector {
    public static final String ERROR_NOT_SPELLGRAM_OBJECT = "psitweaks.spellerror.not_spellgram_object";

    private SpellParam<Entity> target;

    public PieceSelectorSpellGramIntegrity(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntity(SpellParam.GENERIC_NAME_TARGET, SpellParam.YELLOW, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity entity = getParamValue(context, target);
        ReadOnlyTargets.verify(context, entity);
        if (!(entity instanceof SpellGramObject object)) {
            throw new SpellRuntimeException(ERROR_NOT_SPELLGRAM_OBJECT);
        }
        return (double) object.getIntegrity();
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }
}
