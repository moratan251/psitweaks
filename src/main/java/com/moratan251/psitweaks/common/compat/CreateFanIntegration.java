package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.ZoneArea;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessing;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
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
    /** ブレイズバーナーのブロックエンティティ（燃料と、クリエイティブかを読む）。型の親が実行時にしかないので、リフレクションで読む。 */
    private static final String BURNER_CLASS = "com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity";
    private static Class<?> burnerClass;
    private static java.lang.reflect.Method activeFuel;
    private static java.lang.reflect.Method creative;
    private static boolean burnerLookedUp;

    /** {@link CreateCompat#isBurnerNeedingSuperheat}。 */
    static boolean isBurnerNeedingSuperheat(BlockEntity blockEntity) {
        if (!lookUpBurner() || !burnerClass.isInstance(blockEntity)
                || BlazeBurnerBlock.getHeatLevelOf(blockEntity.getBlockState()) == BlazeBurnerBlock.HeatLevel.NONE) {
            return false;
        }
        try {
            Object fuel = activeFuel.invoke(blockEntity);
            return !((Boolean) creative.invoke(blockEntity)) && !(fuel instanceof Enum<?> type && "SPECIAL".equals(type.name()));
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    /** {@link CreateCompat#burnerOwnHeat}。 */
    static int burnerOwnHeat(BlockEntity blockEntity) {
        if (!lookUpBurner() || !burnerClass.isInstance(blockEntity)
                || BlazeBurnerBlock.getHeatLevelOf(blockEntity.getBlockState()) == BlazeBurnerBlock.HeatLevel.NONE) {
            return -1;
        }
        try {
            if ((Boolean) creative.invoke(blockEntity)) {
                return 2;
            }
            Object fuel = activeFuel.invoke(blockEntity);
            if (fuel instanceof Enum<?> type) {
                return switch (type.name()) {
                    case "SPECIAL" -> 2;
                    case "NORMAL" -> 1;
                    default -> 0;
                };
            }
            return 0;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return -1;
        }
    }

    private static synchronized boolean lookUpBurner() {
        if (!burnerLookedUp) {
            burnerLookedUp = true;
            try {
                burnerClass = Class.forName(BURNER_CLASS);
                activeFuel = burnerClass.getMethod("getActiveFuel");
                creative = burnerClass.getMethod("isCreative");
            } catch (ReflectiveOperationException | LinkageError e) {
                burnerClass = null;
            }
        }
        return burnerClass != null;
    }

    private CreateFanIntegration() {
    }

    private static FanProcessingType typeOf(CreateCompat.FanProcess process) {
        return switch (process) {
            case WASHING -> AllFanProcessingTypes.SPLASHING;
            case SMOKING -> AllFanProcessingTypes.SMOKING;
            case BLASTING -> AllFanProcessingTypes.BLASTING;
        };
    }

    static void process(ServerLevel level, ZoneArea area, List<BlockEntity> blockEntities, CreateCompat.FanProcess process,
                        Predicate<BlockPos> itemsAt, Predicate<BlockPos> mayUse) {
        if (area.isEmpty()) {
            return;
        }
        FanProcessingType type = typeOf(process);
        for (ItemEntity item : itemsInside(level, area)) {
            if (itemsAt.test(BlockPos.containing(item.getBoundingBox().getCenter())) && FanProcessing.canProcess(item, type)) {
                // 加工している間は消えるまでの時間を止める（2026-11-12 ユーザー）。
                com.moratan251.psitweaks.common.entities.SpellGram.zone.ZoneItemLifetimes.hold(item, level.getGameTime());
                FanProcessing.applyProcessing(item, type);
            }
        }
        // 集めてから加工する（加工でブロックエンティティの一覧が変わっても困らないように）。
        for (TransportedItemStackHandlerBehaviour handler : handlersInside(blockEntities, mayUse)) {
            handler.handleProcessingOnAllItems(transported -> FanProcessing.applyProcessing(transported, level, type));
        }
    }

    /** クライアント側: Create の AirCurrent がクライアントでするのと同じく、範囲の中のアイテムの位置にパーティクルを出す。 */
    static void spawnParticles(Level level, ZoneArea area, List<BlockEntity> blockEntities, CreateCompat.FanProcess process) {
        if (area.isEmpty()) {
            return;
        }
        FanProcessingType type = typeOf(process);
        for (ItemEntity item : itemsInside(level, area)) {
            type.spawnProcessingParticles(level, item.position());
        }
        for (TransportedItemStackHandlerBehaviour handler : handlersInside(blockEntities, pos -> true)) {
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

    /** デポ・ベルト: 範囲の中のブロックエンティティ（{@code blockEntities}）のうち、上にアイテムを載せて運ぶもの。 */
    private static List<TransportedItemStackHandlerBehaviour> handlersInside(List<BlockEntity> blockEntities,
                                                                            Predicate<BlockPos> mayUse) {
        List<TransportedItemStackHandlerBehaviour> handlers = new ArrayList<>();
        for (BlockEntity blockEntity : blockEntities) {
            if (blockEntity.isRemoved()) {
                continue;
            }
            TransportedItemStackHandlerBehaviour handler =
                    BlockEntityBehaviour.get(blockEntity, TransportedItemStackHandlerBehaviour.TYPE);
            if (handler != null && mayUse.test(blockEntity.getBlockPos())) {
                handlers.add(handler);
            }
        }
        return handlers;
    }
}
