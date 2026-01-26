package com.moratan251.psitweaks.common.spells;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickSupplyFE extends PieceTrick {

    SpellParam<Vector3> position;
    SpellParam<Number> power;
    SpellParam<Vector3> direction;

    public PieceTrickSupplyFE(Spell spell) {
        super(spell);
        this.setStatLabel(EnumSpellStat.POTENCY, (new StatLabel("psi.spellparam.power", true)).max((double)0.5F).mul((double)20.0F).floor());
        this.setStatLabel(EnumSpellStat.COST, (new StatLabel("psi.spellparam.power", true)).max((double)0.5F).mul((double)200.0F).floor());
        this.addParam(this.direction = new ParamVector("psi.spellparam.direction", SpellParam.GREEN, true, false));


    }

    @Override
    public void initParams() {
        this.addParam(this.position = new ParamVector("psi.spellparam.position", SpellParam.BLUE, false, false));
        this.addParam(this.power = new ParamNumber("psi.spellparam.power", SpellParam.RED, false, true));
        this.addParam(this.direction = new ParamVector("psi.spellparam.direction", SpellParam.GREEN, true, false));

    }

    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        Double powerVal = (Double)this.getParamEvaluation(this.power);
        if (powerVal != null && !(powerVal <= (double)0.0F)) {
            powerVal = Math.max((double)0.5F, powerVal);
            meta.addStat(EnumSpellStat.POTENCY, (int)( 50.0 + powerVal * (double)20.0F));
            meta.addStat(EnumSpellStat.COST, (int)(powerVal * (double)200.0F));
        } else {
            throw new SpellCompilationException("psi.spellerror.nonpositivevalue", this.x, this.y);
        }
    }


    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 directionVal = (Vector3)this.getParamValue(context, this.direction);
        Vector3 positionVal = (Vector3)this.getParamValue(context, this.position);
        double powerVal = ((Number)this.getParamValue(context, this.power)).doubleValue();
        int feAmount = (int)(Math.max(0,powerVal * 1000));
        Direction facing = Direction.UP;
        if (directionVal != null) {
            facing = Direction.getNearest(directionVal.x, directionVal.y, directionVal.z);
        }

        if (positionVal == null) {
            throw new SpellRuntimeException("psi.spellerror.nullvector");
        } else if (!context.isInRadius(positionVal)) {
            throw new SpellRuntimeException("psi.spellerror.outsideradius");
        } else {
            BlockPos pos = positionVal.toBlockPos();
            Level world = context.caster.level();
            // サーバーサイドでのみ実行
            if (world.isClientSide) {
                return null;
            }

            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity == null) {
                throw new SpellRuntimeException(SpellRuntimeException.NO_MESSAGE);
            }

            blockEntity.getCapability(ForgeCapabilities.ENERGY, facing).ifPresent(storage -> {
                if (storage.canReceive()) {
                    storage.receiveEnergy(feAmount, false);
                }
            });



            return null;
        }
    }
}