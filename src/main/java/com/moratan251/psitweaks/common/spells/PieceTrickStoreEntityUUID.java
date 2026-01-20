package com.moratan251.psitweaks.common.spells;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickStoreEntityUUID extends PieceTrick {

    SpellParam<Number> number;
    SpellParam<Entity> target;

    public PieceTrickStoreEntityUUID(Spell spell) {
        super(spell);
    }


    public void initParams() {
        addParam(this.number = new ParamNumber("psi.spellparam.number", SpellParam.GREEN, false, false));
        addParam(this.target = new ParamEntity("psi.spellparam.target", SpellParam.BLUE, false, false));
    }

    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        meta.addStat(EnumSpellStat.COMPLEXITY, 1);
        Double numberVal = (Double)this.getParamEvaluation(this.number);
        if (numberVal != null && !(numberVal <= (double)0.0F) && numberVal == (double)numberVal.intValue()) {
            meta.addStat(EnumSpellStat.POTENCY, numberVal.intValue() * 8);
        } else {
            throw new SpellCompilationException("psi.spellerror.nonpositiveinteger", this.x, this.y);
        }
    }

    public Object execute(SpellContext context) throws SpellRuntimeException {
        int key = this.getParamValue(context, this.number).intValue();
        Entity target = this.getParamValue(context, this.target);
        context.verifyEntity(target);

        if (this.target == null) {
            throw new SpellRuntimeException("psi.spellerror.nulltarget");
        }


        ItemStack cadStack = PsiAPI.getPlayerCAD(context.caster);


        if (cadStack != null && cadStack.getItem() instanceof ICAD) {
            ICAD cad = (ICAD)cadStack.getItem();

            int size = cad.getMemorySize(cadStack);
            if (key >= 0 && key < size) {

                CompoundTag tag = cadStack.getOrCreateTag();
                CompoundTag map = tag.getCompound("psitweaks_entity_map");

                map.putString(String.valueOf(key), target.getUUID().toString());
                tag.put("psitweaks_entity_map", map);

                cadStack.setTag(tag);

            } else {
                throw new SpellRuntimeException("psi.spellerror.memoryoutofbounds");
            }

        } else {
            throw new SpellRuntimeException("psi.spellerror.nocad");
        }



        return null;
    }
}

