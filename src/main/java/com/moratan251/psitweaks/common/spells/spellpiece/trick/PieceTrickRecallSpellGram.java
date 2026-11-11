package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.entities.SpellGram.SpellGramObject;
import com.moratan251.psitweaks.common.spells.param.ParamEntityOrEntityList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.piece.PieceTrick;

import java.util.ArrayList;
import java.util.List;

/**
 * 作動式: 魔法式回収（2026-11-11 ユーザー）。対象の自分の魔法式オブジェクトを消す（{@link SpellGramObject.EndReason#RECALLED}）。
 * エンティティでもリストでもよく（取得子: 自分の魔法式オブジェクトをそのまま渡せる）、距離は問わない。自分のもの以外や、魔法式
 * オブジェクトでないものが混ざっているとエラーで、そのときは何も消さない。Psi は戻らない。他人のものを消すのは術式解体の役目。
 */
public class PieceTrickRecallSpellGram extends PieceTrick {
    // 仮の数値
    private static final int POTENCY = 5;
    private static final int COST = 20;

    private SpellParam<?> target;

    public PieceTrickRecallSpellGram(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(POTENCY));
        setStatLabel(EnumSpellStat.COST, new StatLabel(COST));
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntityOrEntityList(SpellParam.GENERIC_NAME_TARGET, SpellParam.YELLOW, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, POTENCY);
        meta.addStat(EnumSpellStat.COST, COST);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        List<SpellGramObject> objects = new ArrayList<>();
        for (Entity entity : ParamEntityOrEntityList.targets(context, getParamValue(context, target))) {
            objects.add(SpellGramTargets.own(context, entity));
        }
        for (SpellGramObject object : objects) {
            Vec3 at = object.position();
            object.level().playSound(null, at.x, at.y, at.z, SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.5F, 1.4F);
            object.end(SpellGramObject.EndReason.RECALLED);
        }
        return null;
    }
}
