package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.spells.combat.BarrierShields;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.piece.PieceSelector;

/**
 * The shield points the target's Trick: Barrier has left, or 0 when it has no shield (or is not a living entity).
 * Only reads the value, like Psi's Operator: Entity Health, so it skips the spell-immune check.
 */
public class PieceSelectorBarrierAmount extends PieceSelector {
    private SpellParam<Entity> target;

    public PieceSelectorBarrierAmount(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamEntity("psi.spellparam.target", SpellParam.GRAY, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity entity = getNotNullParamValue(context, target);
        return entity instanceof LivingEntity living ? (double) BarrierShields.amount(living) : 0.0;
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psi.datatype.number");
    }
}
