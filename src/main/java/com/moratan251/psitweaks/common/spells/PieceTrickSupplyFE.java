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

    private static final double MIN_POWER = 0.5D;
    // powerVal 1.0 あたりに加算する Potency 量
    private static final double POTENCY_PER_POWER = 80.0D;
    // powerVal に関係なく加算する Potency 固定値
    private static final double POTENCY_BASE = 20.0D;
    // powerVal 1.0 あたりの Psi Cost
    private static final double COST_PER_POWER = 200.0D;
    // powerVal 1.0 あたりに供給する FE 量
    private static final int FE_PER_POWER = 4000;

    SpellParam<Vector3> position;
    SpellParam<Number> power;
    SpellParam<Vector3> direction;

    public PieceTrickSupplyFE(Spell spell) {
        super(spell);
        this.setStatLabel(EnumSpellStat.POTENCY, (new StatLabel("psi.spellparam.power", true)).max(MIN_POWER).mul(POTENCY_PER_POWER).floor());
        this.setStatLabel(EnumSpellStat.COST, (new StatLabel("psi.spellparam.power", true)).max(MIN_POWER).mul(COST_PER_POWER).floor());
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
            powerVal = Math.max(MIN_POWER, powerVal);
            meta.addStat(EnumSpellStat.POTENCY, (int)(POTENCY_BASE + powerVal * POTENCY_PER_POWER));
            meta.addStat(EnumSpellStat.COST, (int)(powerVal * COST_PER_POWER));
        } else {
            throw new SpellCompilationException("psi.spellerror.nonpositivevalue", this.x, this.y);
        }
    }


    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 directionVal = (Vector3)this.getParamValue(context, this.direction);
        Vector3 positionVal = (Vector3)this.getParamValue(context, this.position);
        double powerVal = ((Number)this.getParamValue(context, this.power)).doubleValue();
        int feAmount = (int)(Math.max(0, powerVal * FE_PER_POWER));
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
