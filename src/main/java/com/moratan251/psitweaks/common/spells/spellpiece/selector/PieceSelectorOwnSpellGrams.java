package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.caster.CasterIds;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramIndex;
import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.entities.SpellGram.marker.SpellGramMarker;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceSelector;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 取得子: 自分の魔法式オブジェクト（2026-11-11 ユーザー）。術者（自律詠唱機なら機械ごと）が置いた、術者のいるディメンションの
 * 魔法式オブジェクト（サークルと領域）を、距離に関係なくエンティティのリストで返す。目印型（流星群の夜）は別の扱いにする予定なので
 * 含めない。Psi のエンティティのリストはエンティティの番号の順に並び、番号は作られた順に増えるので、並びは置いた順（古い順）になる。
 */
public class PieceSelectorOwnSpellGrams extends PieceSelector {
    public PieceSelectorOwnSpellGrams(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        if (context.caster == null) {
            return EntityListWrapper.EMPTY;
        }
        List<Entity> owned = new ArrayList<>();
        for (SpellGramObject object : SpellGramIndex.ofCaster(context.caster.level(), CasterIds.idOf(context.caster))) {
            if (!(object instanceof SpellGramMarker) && !object.isEnded()) {
                owned.add(object);
            }
        }
        return EntityListWrapper.make(owned);
    }

    @Override
    public Class<?> getEvaluationType() {
        return EntityListWrapper.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psi.datatype.entity_list_wrapper");
    }
}
