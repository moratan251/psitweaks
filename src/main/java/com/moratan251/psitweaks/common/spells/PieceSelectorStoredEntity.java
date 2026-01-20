package com.moratan251.psitweaks.common.spells;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceSelector;

import java.util.UUID;

public class PieceSelectorStoredEntity extends PieceSelector {

    SpellParam<Number> number;

    public PieceSelectorStoredEntity(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(number = new ParamNumber("psi.spellparam.number", SpellParam.GREEN, false, false));
    }

    @Override
    public Class<?> getEvaluationType() {
        return Entity.class;
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        int key = this.getParamValue(context, this.number).intValue();

        ItemStack cadStack = PsiAPI.getPlayerCAD(context.caster);
        if (cadStack != null && cadStack.getItem() instanceof ICAD) {
            ICAD cad = (ICAD)cadStack.getItem();

            int size = cad.getMemorySize(cadStack);
            if (key >= 0 && key < size) {

                CompoundTag tag = cadStack.getOrCreateTag();
                CompoundTag map = tag.getCompound("psitweaks_entity_map");

                String uuidStr = map.getString(String.valueOf(key));
                if (uuidStr.isEmpty()) return null;

                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    return ((ServerLevel) context.caster.level()).getEntity(uuid);
                } catch (Exception e) {
                    return null;
                }



            } else {
                throw new SpellRuntimeException("psi.spellerror.memoryoutofbounds");
            }

        }else{
            throw new SpellRuntimeException("psi.spellerror.nocad");
        }


    }
}