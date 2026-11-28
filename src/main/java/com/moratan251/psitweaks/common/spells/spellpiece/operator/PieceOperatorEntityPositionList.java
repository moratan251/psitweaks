package com.moratan251.psitweaks.common.spells.spellpiece.operator;

import com.moratan251.psitweaks.common.entities.combat.EntityParadeDecoy;
import com.moratan251.psitweaks.common.spells.combat.Parades;
import com.moratan251.psitweaks.common.spells.wrapper.VectorListWrapper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntityListWrapper;
import vazkii.psi.api.spell.piece.PieceOperator;
import vazkii.psi.api.spell.wrapper.EntityListWrapper;

/**
 * The position of every entity in an Entity List, in the List's order, by the same rule as Psi's Operator: Entity
 * Position (including Trick: Parade showing other casters where the lookalike stands).
 */
public class PieceOperatorEntityPositionList extends PieceOperator {
    private SpellParam<EntityListWrapper> list;

    public PieceOperatorEntityPositionList(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(list = new ParamEntityListWrapper(SpellParam.GENERIC_NAME_LIST, SpellParam.BLUE, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        EntityListWrapper entities = getNotNullParamValue(context, list);
        List<Vector3> positions = new ArrayList<>(entities.size());
        for (Entity entity : entities) {
            positions.add(position(entity, context));
        }
        return VectorListWrapper.make(positions);
    }

    /** Feet position; a player's is raised to the eyes, as Psi does. A Parade caster shows the lookalike's eyes. */
    private static Vector3 position(Entity entity, SpellContext context) {
        EntityParadeDecoy decoy = Parades.decoyFacing(entity, context.caster);
        if (decoy != null) {
            return Vector3.fromEntity(decoy).add(0, decoy.getEyeHeight(), 0);
        }
        Vector3 position = Vector3.fromEntity(entity);
        if (entity instanceof Player) {
            position.add(0, entity.getEyeHeight(), 0);
        }
        return position;
    }

    @Override
    public Class<?> getEvaluationType() {
        return VectorListWrapper.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psitweaks.datatype.vector_list");
    }
}
