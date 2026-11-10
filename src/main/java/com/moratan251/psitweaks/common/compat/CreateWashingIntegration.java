package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.ZoneArea;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessing;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 作動式: 湿潤領域の Create の一括洗浄（Create があるときだけ読み込む、{@link CreateCompat} から呼ぶ）。
 * 扇風機の水の流れと同じく、サーバーで 1 回呼ぶと Create の処理が 1 tick ぶん進み、クライアントでは Create の AirCurrent と
 * 同じく洗っているアイテムの位置にパーティクルを出す。
 */
final class CreateWashingIntegration {
    private CreateWashingIntegration() {
    }

    static void wash(ServerLevel level, ZoneArea area, Predicate<BlockPos> mayUse) {
        if (area.isEmpty()) {
            return;
        }
        for (ItemEntity item : itemsInside(level, area)) {
            if (FanProcessing.canProcess(item, AllFanProcessingTypes.SPLASHING)) {
                FanProcessing.applyProcessing(item, AllFanProcessingTypes.SPLASHING);
            }
        }
        // 集めてから洗う（洗い上がりでブロックエンティティの一覧が変わっても困らないように）。
        for (TransportedItemStackHandlerBehaviour handler : handlersInside(level, area, mayUse)) {
            handler.handleProcessingOnAllItems(
                    transported -> FanProcessing.applyProcessing(transported, level, AllFanProcessingTypes.SPLASHING));
        }
    }

    /** クライアント側: Create の AirCurrent がクライアントでするのと同じく、範囲の中のアイテムの位置にパーティクルを出す。 */
    static void spawnParticles(Level level, ZoneArea area) {
        if (area.isEmpty()) {
            return;
        }
        for (ItemEntity item : itemsInside(level, area)) {
            AllFanProcessingTypes.SPLASHING.spawnProcessingParticles(level, item.position());
        }
        for (TransportedItemStackHandlerBehaviour handler : handlersInside(level, area, pos -> true)) {
            handler.handleProcessingOnAllItems(transported -> {
                AllFanProcessingTypes.SPLASHING.spawnProcessingParticles(level, handler.getWorldPositionOf(transported));
                return TransportedItemStackHandlerBehaviour.TransportedResult.doNothing();
            });
        }
    }

    private static List<ItemEntity> itemsInside(Level level, ZoneArea area) {
        return level.getEntitiesOfClass(ItemEntity.class, area.bounds(),
                entity -> entity.isAlive() && area.contains(BlockPos.containing(entity.getBoundingBox().getCenter())));
    }

    /** デポ・ベルト: 範囲にかかる読み込み済みのチャンクのブロックエンティティから探す（範囲の全ブロックは見ない）。 */
    private static List<TransportedItemStackHandlerBehaviour> handlersInside(Level level, ZoneArea area,
                                                                            Predicate<BlockPos> mayUse) {
        AABB box = area.bounds();
        int minChunkX = SectionPos.blockToSectionCoord((int) Math.floor(box.minX));
        int maxChunkX = SectionPos.blockToSectionCoord((int) Math.floor(box.maxX) - 1);
        int minChunkZ = SectionPos.blockToSectionCoord((int) Math.floor(box.minZ));
        int maxChunkZ = SectionPos.blockToSectionCoord((int) Math.floor(box.maxZ) - 1);
        List<TransportedItemStackHandlerBehaviour> handlers = new ArrayList<>();
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) {
                    continue;
                }
                LevelChunk chunk = level.getChunk(chunkX, chunkZ);
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    BlockPos pos = blockEntity.getBlockPos();
                    if (!area.contains(pos)) {
                        continue;
                    }
                    TransportedItemStackHandlerBehaviour handler =
                            BlockEntityBehaviour.get(blockEntity, TransportedItemStackHandlerBehaviour.TYPE);
                    if (handler != null && mayUse.test(pos)) {
                        handlers.add(handler);
                    }
                }
            }
        }
        return handlers;
    }
}
