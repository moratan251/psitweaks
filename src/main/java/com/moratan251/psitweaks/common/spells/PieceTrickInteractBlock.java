package com.moratan251.psitweaks.common.spells;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * スペルピース：対象座標のブロックに右クリック動作を実行
 */
public class PieceTrickInteractBlock extends PieceTrick {

    ParamVector position;

    public PieceTrickInteractBlock(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        // 右クリックする座標を指定するパラメータ
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);

        // 消費MP設定（適度な値に設定）
        meta.addStat(EnumSpellStat.POTENCY, 20);
        meta.addStat(EnumSpellStat.COST, 40);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = (Vector3)this.getParamValue(context, this.position);

        if (positionVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }//else if (!context.isInRadius(positionVal)) {
        //    throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
      //  }

        Level world = context.caster.level();
        Player player = context.caster;
        BlockPos pos = positionVal.toBlockPos();

        // ワールドの範囲チェック
        //if (!world.isLoaded(targetPos)) {
        //    throw new SpellRuntimeException("psitweaks.spellerror.chunkNotLoaded");
       // }

        BlockState blockState = world.getBlockState(pos);

        if (blockState.isAir()) {
            return null;
        }

        // 適切な面を決定（通常は上面）
        Direction face = Direction.UP;
        Vec3 hitVec = Vec3.atCenterOf(pos);

        // BlockHitResultを作成（右クリック処理に必要）
        BlockHitResult hitResult = new BlockHitResult(
                hitVec,
                face,
                pos,
                false
        );

        try {
            //オフハンドで右クリック
            InteractionResult result = blockState.use(
                    world,
                    player,
                    InteractionHand.OFF_HAND,
                    hitResult
            );


            // ブロック自体が右クリックを処理しない場合、
            // ブロックエンティティがあればそれも試す
            if (result == InteractionResult.PASS) {
                if (world.getBlockEntity(pos) != null) {
                    // ブロックエンティティの右クリック処理
                    // 必要に応じてここでカスタム処理を追加
                }
            }

        } catch (Exception e) {
            // エラーハンドリング
            throw new SpellRuntimeException("psitweaks.spellerror.failedToInteract");
        }

        return null;
    }
/*
    @Override
    public Class<?> getReturnType() {
        return Void.class;
    }

 */

}