package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.ZoneArea;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessing;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 領域型の魔法式オブジェクトが Create の扇風機の加工（一括洗浄・一括燻製・一括製錬）をする（Create があるときだけ読み込む、
 * {@link CreateCompat} から呼ぶ）。扇風機の風と同じく、サーバーで 1 回呼ぶと Create の処理が 1 tick ぶん進み、
 * クライアントでは Create の AirCurrent と同じく加工しているアイテムの位置にパーティクルを出す。
 */
final class CreateFanIntegration {
    private CreateFanIntegration() {
    }

    private static FanProcessingType typeOf(CreateCompat.FanProcess process) {
        return switch (process) {
            case WASHING -> AllFanProcessingTypes.SPLASHING;
            case SMOKING -> AllFanProcessingTypes.SMOKING;
            case BLASTING -> AllFanProcessingTypes.BLASTING;
        };
    }

    static void process(ServerLevel level, ZoneArea area, CreateCompat.FanProcess process, Predicate<BlockPos> itemsAt,
                        Predicate<BlockPos> mayUse) {
        if (area.isEmpty()) {
            return;
        }
        FanProcessingType type = typeOf(process);
        for (ItemEntity item : itemsInside(level, area)) {
            if (itemsAt.test(BlockPos.containing(item.getBoundingBox().getCenter())) && FanProcessing.canProcess(item, type)) {
                FanProcessing.applyProcessing(item, type);
            }
        }
        // 集めてから加工する（加工でブロックエンティティの一覧が変わっても困らないように）。
        for (TransportedItemStackHandlerBehaviour handler : handlersInside(level, area, mayUse)) {
            handler.handleProcessingOnAllItems(transported -> FanProcessing.applyProcessing(transported, level, type));
        }
    }

    /** クライアント側: Create の AirCurrent がクライアントでするのと同じく、範囲の中のアイテムの位置にパーティクルを出す。 */
    static void spawnParticles(Level level, ZoneArea area, CreateCompat.FanProcess process) {
        if (area.isEmpty()) {
            return;
        }
        FanProcessingType type = typeOf(process);
        for (ItemEntity item : itemsInside(level, area)) {
            type.spawnProcessingParticles(level, item.position());
        }
        for (TransportedItemStackHandlerBehaviour handler : handlersInside(level, area, pos -> true)) {
            handler.handleProcessingOnAllItems(transported -> {
                type.spawnProcessingParticles(level, handler.getWorldPositionOf(transported));
                return TransportedItemStackHandlerBehaviour.TransportedResult.doNothing();
            });
        }
    }

    private static List<ItemEntity> itemsInside(Level level, ZoneArea area) {
        return level.getEntitiesOfClass(ItemEntity.class, area.bounds(),
                entity -> entity.isAlive() && area.contains(BlockPos.containing(entity.getBoundingBox().getCenter())));
    }

    /** デポ・ベルト: 範囲の中のブロックエンティティのうち、上にアイテムを載せて運ぶもの。 */
    private static List<TransportedItemStackHandlerBehaviour> handlersInside(Level level, ZoneArea area,
                                                                            Predicate<BlockPos> mayUse) {
        List<TransportedItemStackHandlerBehaviour> handlers = new ArrayList<>();
        for (BlockEntity blockEntity : area.blockEntities(level)) {
            TransportedItemStackHandlerBehaviour handler =
                    BlockEntityBehaviour.get(blockEntity, TransportedItemStackHandlerBehaviour.TYPE);
            if (handler != null && mayUse.test(blockEntity.getBlockPos())) {
                handlers.add(handler);
            }
        }
        return handlers;
    }
}
