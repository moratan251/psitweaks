package com.moratan251.psitweaks.common.spells.spellpiece.trick;

import com.moratan251.psitweaks.common.caster.AutonomousCasterPlayer;
import com.moratan251.psitweaks.common.spells.SpellBlockProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * スペルピース：対象座標のブロックに右クリック動作を実行
 */
public class PieceTrickInteractBlock extends PieceTrick {
    private static final String ERROR_ACCESS_DENIED = "psitweaks.spellerror.accessdenyed";

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
        }
        if (!context.isInRadius(positionVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }

        Level world = context.caster.level();
        Player player = context.caster;
        BlockPos pos = positionVal.toBlockPos();

        // 読み込まれていないチャンクを読み込み・生成しない
        if (!world.isLoaded(pos)) {
            return null;
        }

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

        //オフハンドで右クリック。自律詠唱機はオフハンドを持たないので、選択中の機械のスロット（メインハンド）を使う
        InteractionHand hand = player instanceof AutonomousCasterPlayer ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        // 保護 Mod は右クリックのイベントでブロックの使用を止める。バニラが右クリックの前に送るのと同じイベントで問い合わせる
        if (!SpellBlockProtection.mayUse(world, player, hand, hitResult)) {
            throw new SpellRuntimeException(ERROR_ACCESS_DENIED);
        }

        try {
            ItemStack heldStack = player.getItemInHand(hand);
            ItemInteractionResult itemResult = blockState.useItemOn(
                    heldStack,
                    world,
                    player,
                    hand,
                    hitResult
            );
            InteractionResult result = itemResult.result();
            if (itemResult == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION) {
                result = blockState.useWithoutItem(world, player, hitResult);
            }


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
