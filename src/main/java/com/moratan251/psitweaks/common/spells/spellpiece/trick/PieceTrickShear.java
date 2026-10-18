package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.spells.param.ParamEntityOrEntityList;
import com.moratan251.psitweaks.common.spells.spellpiece.trick.MassBlockBreakHelper.StackAccumulator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.IShearable;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * 作動式: 毛刈り。
 * 対象(Entity または Entity List)を、ハサミで右クリックしたときと同じように刈る。刈り取ったものは術者の
 * インベントリへ入れ、入りきらない分は術者の位置に落とす。刈れない対象・保護されている対象は飛ばす。
 */
public class PieceTrickShear extends PieceTrick {
    private static final int POTENCY = 20;
    private static final int COST = 40;

    private SpellParam<Object> target;

    public PieceTrickShear(Spell spell) {
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
        List<Entity> targets = ParamEntityOrEntityList.targets(context, getParamValue(context, target));
        if (!(context.caster instanceof ServerPlayer caster)) {
            return null;
        }
        // Modded shearables may ask what cuts them; they see plain shears.
        ItemStack shears = new ItemStack(Items.SHEARS);
        StackAccumulator drops = new StackAccumulator();
        for (Entity entity : targets) {
            Level level = entity.level();
            BlockPos pos = entity.blockPosition();
            if (!(entity instanceof IShearable shearable) || level.isClientSide
                    || !shearable.isShearable(caster, shears, level, pos)
                    || !level.mayInteract(caster, pos) || PieceTrickItemTransferBase.isProtectedFromInteraction(caster, entity)) {
                continue;
            }
            for (ItemStack drop : shearable.onSheared(caster, shears, level, pos)) {
                drops.add(drop);
            }
            entity.gameEvent(GameEvent.SHEAR, caster);
        }
        HarvestHelper.deliverToCaster(caster, drops);
        return null;
    }
}
