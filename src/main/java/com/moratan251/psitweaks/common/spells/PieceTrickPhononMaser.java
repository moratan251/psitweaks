package com.moratan251.psitweaks.common.spells;

import com.moratan251.psitweaks.common.entities.EntityPhononMaserBeam;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickPhononMaser extends PieceTrick {

    private static final double MAX_RANGE = 32.0; // 射程32m

    SpellParam<Number> power;

    public PieceTrickPhononMaser(Spell spell) {
        super(spell);
        this.setStatLabel(EnumSpellStat.POTENCY,
                (new StatLabel("psi.spellparam.power", true)).mul(50.0).floor());
        this.setStatLabel(EnumSpellStat.COST,
                (new StatLabel("psi.spellparam.power", true)).mul(100.0).floor());
    }

    @Override
    public void initParams() {
        this.addParam(this.power = new ParamNumber("psi.spellparam.power", SpellParam.RED, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        Double powerVal = (Double) this.getParamEvaluation(this.power);
        if (powerVal == null || powerVal <= 0.0) {
            throw new SpellCompilationException("psi.spellerror.nonpositivevalue", this.x, this.y);
        }
        meta.addStat(EnumSpellStat.POTENCY, (int) (powerVal * 50.0));
        meta.addStat(EnumSpellStat.COST, (int) (powerVal * 100.0));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        double powerVal = this.getParamValue(context, this.power).doubleValue();

        if (powerVal <= 0) {
            throw new SpellRuntimeException("psi.spellerror.nonpositivevalue");
        }

        Level level = context.caster.level();
        if (level.isClientSide) {
            return null;
        }

        LivingEntity caster = context.caster;

        // プレイヤーの視線方向を取得
        Vec3 startPos = caster.getEyePosition();
        Vec3 lookVec = caster.getLookAngle();
        Vec3 endPos = startPos.add(lookVec.scale(MAX_RANGE));

        // ブロックへのレイトレース
        BlockHitResult blockHit = level.clip(new ClipContext(
                startPos,
                endPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                caster
        ));

        Vec3 actualEndPos = endPos;
        BlockPos hitBlockPos = null;

        if (blockHit.getType() != HitResult.Type.MISS) {
            actualEndPos = blockHit.getLocation();
            hitBlockPos = blockHit.getBlockPos();
        }

        // レーザービームエンティティをスポーン
        EntityPhononMaserBeam beam = new EntityPhononMaserBeam(
                level,
                caster,
                startPos,
                actualEndPos,
                powerVal,
                hitBlockPos
        );
        level.addFreshEntity(beam);

        return null;
    }
}