package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.spells.util.ReadOnlyTargets;
import com.moratan251.psitweaks.common.spells.wrapper.NumberListWrapper;
import com.moratan251.psitweaksqol.api.PsitweaksModeOptions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntityListWrapper;
import vazkii.psi.api.spell.piece.PieceSelector;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

/**
 * 取得子: 魔法式耐久値リスト。リストの魔法式オブジェクトそれぞれの今の耐久値（{@link SpellGramObject#getIntegrity}）を、
 * リストと同じ順の Number List で返す。順が揃うので、演算子: 基準値順要素などの基準値にそのまま使える。
 * 魔法式オブジェクトでない要素が1つでもあればエラー（取得子: 魔法式耐久値と同じ）。読むだけなので、Psi の耐性の判定はしない
 * （{@link ReadOnlyTargets}）。距離も問わない。
 */
public class PieceSelectorSpellGramIntegrityList extends PieceSelector {
    private SpellParam<EntityListWrapper> list;

    public PieceSelectorSpellGramIntegrityList(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(list = new ParamEntityListWrapper(SpellParam.GENERIC_NAME_LIST, SpellParam.BLUE, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        EntityListWrapper entities = getNotNullParamValue(context, list);
        List<Double> integrities = new ArrayList<>();
        for (Entity entity : entities) {
            ReadOnlyTargets.verify(context, entity);
            if (!(entity instanceof SpellGramObject object)) {
                throw new SpellRuntimeException(PieceSelectorSpellGramIntegrity.ERROR_NOT_SPELLGRAM_OBJECT);
            }
            integrities.add((double) object.getIntegrity());
        }
        return NumberListWrapper.make(integrities);
    }

    @Override
    public Class<?> getEvaluationType() {
        return NumberListWrapper.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable(PsitweaksModeOptions.NUMBER.listTranslationKey());
    }
}
