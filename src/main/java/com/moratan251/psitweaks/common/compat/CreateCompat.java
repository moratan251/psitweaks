package com.moratan251.psitweaks.common.compat;

import com.moratan251.psitweaks.common.entities.SpellGram.zone.ZoneArea;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;

import java.util.List;
import java.util.function.Predicate;

/**
 * Create との連携の入口。Create のクラスは {@link CreateFanIntegration} にだけ置き、Create がないときは読み込まない。
 */
public final class CreateCompat {
    private static final String CREATE_MOD_ID = "create";

    /** 領域がする Create の扇風機の加工。 */
    public enum FanProcess {
        /** 一括洗浄（作動式: 湿潤領域）。 */
        WASHING,
        /** 一括燻製（作動式: 加熱領域）。 */
        SMOKING,
        /** 一括製錬（氷炎地獄の灼熱になった加熱領域）。 */
        BLASTING
    }

    private CreateCompat() {
    }

    public static boolean isCreateLoaded() {
        return ModList.get().isLoaded(CREATE_MOD_ID);
    }

    /**
     * 範囲の中を 1 tick ぶん加工する（Create の扇風機の風と同じ）。落ちているアイテムと、デポ・ベルトの上のアイテムが対象。
     * 落ちているアイテムはそのブロックで {@code itemsAt} が真のものだけ、デポ・ベルトは {@code machinesAt} が真のブロック
     * （保護されていない、他者の領域干渉で止められていない）のものだけ。デポ・ベルトは {@code blockEntities}（範囲の中の
     * ブロックエンティティ、領域が覚えている一覧）から探す。Create がなければ何もしない。
     */
    public static void processZone(ServerLevel level, ZoneArea area, List<BlockEntity> blockEntities, FanProcess process,
                                   Predicate<BlockPos> itemsAt, Predicate<BlockPos> machinesAt) {
        if (isCreateLoaded()) {
            CreateFanIntegration.process(level, area, blockEntities, process, itemsAt, machinesAt);
        }
    }

    /**
     * {@code blockEntity} が、ブレイズの入ったブレイズバーナーで、自分の燃料では極度の加熱になっていない（ブレイズケーキでも
     * クリエイティブでもない）か。氷炎地獄の灼熱が極度の加熱にしてやると、加熱領域の耐久値を余分に使う。Create がなければ偽。
     */
    public static boolean isBurnerNeedingSuperheat(BlockEntity blockEntity) {
        return isCreateLoaded() && CreateFanIntegration.isBurnerNeedingSuperheat(blockEntity);
    }

    /**
     * クライアント側、毎 tick: ケース入りファンの風を当てたときと同じ加工のパーティクルを、範囲の中の落ちているアイテムと
     * デポ・ベルトの上のアイテムに出す（Create と同じく、出るかどうかは Create の側で毎回くじを引く）。Create がなければ何もしない。
     */
    public static void spawnProcessingParticles(Level level, ZoneArea area, List<BlockEntity> blockEntities, FanProcess process) {
        if (isCreateLoaded()) {
            CreateFanIntegration.spawnParticles(level, area, blockEntities, process);
        }
    }
}
