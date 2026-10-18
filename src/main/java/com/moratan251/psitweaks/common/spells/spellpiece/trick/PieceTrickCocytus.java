package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.spells.combat.CocytusFreezes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickCocytus extends PieceTrick {
    SpellParam<Entity> target;

    public PieceTrickCocytus(Spell spell) {
        super(spell);
        this.setStatLabel(EnumSpellStat.POTENCY,new StatLabel((double)1250));
        this.setStatLabel(EnumSpellStat.COST, new StatLabel((double)5000));
    }

    public void initParams() {
        this.addParam(this.target = new ParamEntity("psi.spellparam.target", SpellParam.YELLOW, false, false));
    }

    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, 1250);
        meta.addStat(EnumSpellStat.COST, 5000);
    }

    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity targetVal = (Entity)this.getParamValue(context, this.target);
        context.verifyEntity(targetVal);
        if (!(targetVal instanceof LivingEntity)) {
            throw new SpellRuntimeException("psi.spellerror.nulltarget");
        } else if (!context.isInRadius(targetVal)) {
            throw new SpellRuntimeException("psi.spellerror.outsideradius");
        } else {
            if (!(targetVal instanceof Player)) {

                if (targetVal instanceof Mob mob && context.caster.level() instanceof ServerLevel serverLevel) {
                    CocytusFreezes.freeze(serverLevel, mob);
                }

            }
            return null;
        }
    }
}
