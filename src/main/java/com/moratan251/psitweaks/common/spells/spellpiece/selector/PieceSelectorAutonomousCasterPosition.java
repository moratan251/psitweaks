package com.moratan251.psitweaks.common.spells.spellpiece.selector;

import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceSelector;

/**
 * 詠唱した自律詠唱機のブロックの中心。詠唱主体は機械の前に立つので、「取得子: 術者」の位置では機械そのものを指せない。
 * 自律詠唱機以外の詠唱ではエラーにする。
 */
public class PieceSelectorAutonomousCasterPosition extends PieceSelector {
    public PieceSelectorAutonomousCasterPosition(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        if (!(context.caster instanceof AutonomousCasterPlayer caster)) {
            throw new SpellRuntimeException("psitweaks.spellerror.not_autonomous_caster");
        }
        BlockPos pos = caster.machinePos();
        return new Vector3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }

    @Override
    public Class<?> getEvaluationType() {
        return Vector3.class;
    }

    @Override
    public Component getEvaluationTypeString() {
        return Component.translatable("psi.datatype.vector3");
    }
}
