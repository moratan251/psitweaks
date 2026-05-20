package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.spells.param.ParamString;
import com.moratan251.psitweaks.common.spells.util.StringSpellHelper;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.piece.PieceSelector;

public class PieceSelectorNbtValue extends PieceSelector {
    private static final String ID_TAG = "id";

    private SpellParam<Entity> target;
    private SpellParam<String> key;

    public PieceSelectorNbtValue(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntity("psi.spellparam.target", SpellParam.GRAY, false, false));
        addParam(key = new ParamString(PsitweaksSpellParams.STRING, PsitweaksSpellParams.STRING_COLOR, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity entity = getNotNullParamValue(context, target);
        String nbtKey = getNotNullParamValue(context, key);
        context.verifyEntity(entity);

        if (ID_TAG.equals(nbtKey)) {
            return "";
        }

        CompoundTag tag = NbtPredicate.getEntityTagToCompare(entity);
        Tag value = tag.get(nbtKey);
        return value == null ? "" : StringSpellHelper.sanitize(value.toString());
    }

    @Override
    public Class<?> getEvaluationType() {
        return String.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.string");
    }
}
